package cc.thonly.reverie_dreams.entity.skill;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.entity.type.DanmakuShooter;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.entity.IceFairy;
import cc.thonly.reverie_dreams.entity.ai.TargetReachability;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.entity.projectile.ThrownIceBlock;
import cc.thonly.reverie_dreams.entity.projectile.ThrownIceSnowball;
import cc.thonly.reverie_dreams.registry.content.ItemColor;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.sound.RDSoundEvents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * 冰精（琪露诺）的战斗技能。
 *
 * <p>状态循环：
 * <ul>
 *     <li>雪球连射：短前摇后连续抛出多波三连雪球</li>
 *     <li>冰块抛射：明显的前摇，抛射带弧线的魔法冰块，命中附带范围冰缓</li>
 *     <li>弹幕：完美冻结（环形冰弹）+ 冰柱坠落（扇形冰锥）</li>
 *     <li>逃跑：玩家贴脸时后跳并拉开距离，逃不掉就转身打雪球</li>
 * </ul>
 * 所有状态数据放在 {@link SkillContainer#getDataMap()} 中，技能实例本身保持无状态。</p>
 */
public class IceFairySkill extends Skill<IceFairy> {
    public static final Identifier KEY = ReverieDreams.id("ice_fairy");
    private static final String STATE_KEY = "IceFairySkillState";

    private enum State {
        REPOSITION,
        SNOWBALL,
        ICE_BLOCK,
        DANMAKU,
        FLEE
    }

    private static final class StateData {
        private final TargetReachability reachability;
        private State state = State.REPOSITION;
        private int stateTicks = 30;
        private int snowballCooldown = 10;
        private int iceBlockCooldown = 60;
        private int danmakuCooldown = 120;
        private int fleeCooldown = 40;
        private int shotTimer;
        private int shotsLeft;
        private int patternStep;
        private int pathRecalcTicks;
        private int strafeDirection = 1;
        private int fleePathFailures;
        private int unreachableTicks;
        private double preferredRange;
        private String targetUuid = "";

        private StateData(IceFairy fairy) {
            this.reachability = new TargetReachability(fairy);
            this.preferredRange = 7.0 + fairy.getRandom().nextDouble() * 3.0;
        }

        private void tickCooldowns() {
            if (this.snowballCooldown > 0) this.snowballCooldown--;
            if (this.iceBlockCooldown > 0) this.iceBlockCooldown--;
            if (this.danmakuCooldown > 0) this.danmakuCooldown--;
            if (this.fleeCooldown > 0) this.fleeCooldown--;
        }
    }

    @Override
    public void onStarted(SkillContainer<IceFairy> container) {
        IceFairy fairy = container.getLivingEntity();
        if (fairy.level().isClientSide()) {
            return;
        }

        StateData data = new StateData(fairy);
        container.getDataMap().put(STATE_KEY, data);
        data.stateTicks = 20 + fairy.getRandom().nextInt(15);
        data.snowballCooldown = 10;
        data.iceBlockCooldown = 40 + fairy.getRandom().nextInt(20);
        data.danmakuCooldown = 100 + fairy.getRandom().nextInt(60);
        data.fleeCooldown = 0;
        fairy.setAggressive(true);
    }

    @Override
    public void onTick(SkillContainer<IceFairy> container) {
        IceFairy fairy = container.getLivingEntity();
        if (fairy.level().isClientSide()) {
            return;
        }

        LivingEntity target = fairy.getTarget();
        if (!isValidTarget(fairy, target)) {
            fairy.setTarget(null);
            container.stop();
            return;
        }

        StateData data = getState(container);
        fairy.setAggressive(true);
        data.tickCooldowns();

        String targetUuid = target.getUUID().toString();
        if (!targetUuid.equals(data.targetUuid)) {
            data.targetUuid = targetUuid;
            resetForNewTarget(fairy, data);
        }

        if (data.state == State.FLEE) {
            // 逃跑时身体朝移动方向，只回头用视线看着玩家，避免“倒着跑”
            fairy.getLookControl().setLookAt(target, 30.0f, 30.0f);
        } else {
            faceTarget(fairy, target);
        }

        // 看不见且没有路可以靠近时先等待，不隔墙/隔山无限放技能
        if (!data.reachability.canEngage(target)) {
            data.unreachableTicks++;
            fairy.getNavigation().stop();
            if (data.state != State.REPOSITION) {
                data.state = State.REPOSITION;
                data.stateTicks = 20;
            }
            // 长时间完全够不到，放弃这个目标，回到普通游荡
            if (data.unreachableTicks > 100) {
                fairy.setTarget(null);
                container.stop();
                return;
            }
            return;
        }
        data.unreachableTicks = 0;

        double distanceSq = fairy.distanceToSqr(target);
        boolean hasLineOfSight = fairy.getSensing().hasLineOfSight(target);

        // 玩家贴脸时优先逃跑；逃跑有冷却，避免无限后跳
        if (distanceSq < 3.5 * 3.5 && data.fleeCooldown <= 0 && data.state != State.FLEE) {
            startFlee(fairy, data, target);
            return;
        }

        // 看不见目标时中断攻击，先绕到有视线/有路的位置，避免隔墙放技能
        if (!hasLineOfSight && data.state != State.FLEE && data.state != State.REPOSITION) {
            data.state = State.REPOSITION;
            data.stateTicks = 5 + fairy.getRandom().nextInt(6);
            data.pathRecalcTicks = 0;
        }

        switch (data.state) {
            case REPOSITION -> tickReposition(fairy, data, target);
            case SNOWBALL -> tickSnowball(fairy, data, target);
            case ICE_BLOCK -> tickIceBlock(fairy, data, target);
            case DANMAKU -> tickDanmaku(fairy, data, target);
            case FLEE -> tickFlee(fairy, data, target);
        }
    }

    @Override
    public void onEnd(SkillContainer<IceFairy> container) {
        IceFairy fairy = container.getLivingEntity();
        container.getDataMap().remove(STATE_KEY);
        fairy.getNavigation().stop();
        fairy.setAggressive(false);
    }

    @Override
    public Identifier id() {
        return KEY;
    }

    private static StateData getState(SkillContainer<IceFairy> container) {
        Object value = container.getDataMap().get(STATE_KEY);
        if (value instanceof StateData data) {
            return data;
        }
        StateData data = new StateData(container.getLivingEntity());
        container.getDataMap().put(STATE_KEY, data);
        return data;
    }

    private static boolean isValidTarget(IceFairy fairy, LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        if (fairy.getOwner() == target) {
            return false;
        }
        if (fairy.getOwner() != null
                && target instanceof TamableAnimal tameable
                && tameable.getOwner() == fairy.getOwner()) {
            return false;
        }
        return true;
    }

    private static void resetForNewTarget(IceFairy fairy, StateData data) {
        data.state = State.REPOSITION;
        data.stateTicks = 20 + fairy.getRandom().nextInt(10);
        data.snowballCooldown = 10;
        data.iceBlockCooldown = 40 + fairy.getRandom().nextInt(20);
        data.danmakuCooldown = 100 + fairy.getRandom().nextInt(60);
        data.fleeCooldown = 0;
        data.shotTimer = 0;
        data.shotsLeft = 0;
        data.patternStep = 0;
        data.fleePathFailures = 0;
        data.unreachableTicks = 0;
    }

    private static void faceTarget(IceFairy fairy, LivingEntity target) {
        float[] pitchYaw = DanmakuShooter.getPitchYaw(fairy, target);
        fairy.getLookControl().setLookAt(target, 30.0f, 30.0f);
        fairy.setXRot(pitchYaw[0]);
        fairy.setYRot(pitchYaw[1]);
    }

    private static void tickReposition(IceFairy fairy, StateData data, LivingEntity target) {
        data.stateTicks--;
        if (data.pathRecalcTicks > 0) {
            data.pathRecalcTicks--;
        } else {
            data.pathRecalcTicks = 10 + fairy.getRandom().nextInt(10);
            moveAroundTarget(fairy, data, target);
        }
        if (data.stateTicks <= 0) {
            chooseAttack(fairy, data, target);
        }
    }

    private static void chooseAttack(IceFairy fairy, StateData data, LivingEntity target) {
        double distance = Math.sqrt(fairy.distanceToSqr(target));
        RandomSource random = fairy.getRandom();

        if (!fairy.getSensing().hasLineOfSight(target)) {
            data.stateTicks = 10 + random.nextInt(10);
            return;
        }
        if (distance < 4.5 && data.fleeCooldown <= 0) {
            startFlee(fairy, data, target);
            return;
        }
        // 弹幕是大招，优先在玩家还有一定距离时放
        if (data.danmakuCooldown <= 0 && distance > 5.0 && random.nextFloat() < 0.45f) {
            startDanmaku(fairy, data);
            return;
        }
        if (data.iceBlockCooldown <= 0 && random.nextFloat() < 0.6f) {
            startIceBlock(fairy, data);
            return;
        }
        if (data.snowballCooldown <= 0) {
            startSnowball(fairy, data);
            return;
        }
        data.stateTicks = 10 + random.nextInt(10);
    }

    private static void moveAroundTarget(IceFairy fairy, StateData data, LivingEntity target) {
        RandomSource random = fairy.getRandom();
        double dx = fairy.getX() - target.getX();
        double dz = fairy.getZ() - target.getZ();
        double angle = Math.atan2(dz, dx);

        if (random.nextFloat() < 0.2f) {
            data.strafeDirection *= -1;
        }
        angle += data.strafeDirection * (0.55 + random.nextDouble() * 0.35);

        double radius = data.preferredRange + (random.nextDouble() - 0.5) * 2.0;
        double x = target.getX() + Math.cos(angle) * radius;
        double z = target.getZ() + Math.sin(angle) * radius;
        double y = target.getY();

        if (!fairy.getNavigation().moveTo(x, y, z, 1.2)) {
            fairy.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.2);
        }
    }

    // ---------------------------------------------------------------------
    // 雪球连射
    // ---------------------------------------------------------------------

    private static void startSnowball(IceFairy fairy, StateData data) {
        data.state = State.SNOWBALL;
        data.stateTicks = 8 + fairy.getRandom().nextInt(5);
        data.shotsLeft = 3 + fairy.getRandom().nextInt(3);
        data.shotTimer = 8;
        fairy.getNavigation().stop();
    }

    private static void tickSnowball(IceFairy fairy, StateData data, LivingEntity target) {
        data.stateTicks--;
        if (data.stateTicks > 0) {
            if (data.stateTicks % 3 == 0) {
                spawnSnowflakeRing(fairy, 6, 1.0);
            }
            return;
        }

        if (--data.shotTimer > 0) {
            return;
        }

        fireSnowballVolley(fairy, target);
        data.shotsLeft--;
        data.shotTimer = 6 + fairy.getRandom().nextInt(4);

        if (data.shotsLeft <= 0) {
            data.snowballCooldown = 30 + fairy.getRandom().nextInt(20);
            enterReposition(data, 15 + fairy.getRandom().nextInt(10));
        }
    }

    private static void fireSnowballVolley(IceFairy fairy, LivingEntity target) {
        if (!(fairy.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        float[] pitchYaw = DanmakuShooter.getPitchYaw(fairy, target);
        RandomSource random = fairy.getRandom();

        for (float yawOffset : new float[]{-10.0f, 0.0f, 10.0f}) {
            ThrownIceSnowball snowball = new ThrownIceSnowball(serverLevel, fairy, new ItemStack(Items.SNOWBALL));
            snowball.shootFromRotation(
                    fairy,
                    pitchYaw[0] - 4.0f,
                    pitchYaw[1] + yawOffset,
                    0.0f,
                    1.2f,
                    2.5f
            );
            serverLevel.addFreshEntity(snowball);
        }

        fairy.swing(InteractionHand.MAIN_HAND);
        serverLevel.playSound(
                null,
                fairy.getX(), fairy.getY(), fairy.getZ(),
                SoundEvents.SNOW_GOLEM_SHOOT,
                SoundSource.HOSTILE,
                1.0f,
                1.0f + random.nextFloat() * 0.2f
        );
        spawnSnowBurst(fairy, 6);
    }

    // ---------------------------------------------------------------------
    // 冰块抛射
    // ---------------------------------------------------------------------

    private static void startIceBlock(IceFairy fairy, StateData data) {
        data.state = State.ICE_BLOCK;
        data.stateTicks = 14 + fairy.getRandom().nextInt(6);
        data.shotsLeft = 2 + fairy.getRandom().nextInt(2);
        data.shotTimer = 10;
        fairy.getNavigation().stop();
        if (fairy.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(
                    null,
                    fairy.getX(), fairy.getY(), fairy.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.HOSTILE,
                    1.0f,
                    0.9f
            );
        }
        spawnIceChargeParticles(fairy);
    }

    private static void tickIceBlock(IceFairy fairy, StateData data, LivingEntity target) {
        data.stateTicks--;
        if (data.stateTicks > 0) {
            if (data.stateTicks % 2 == 0) {
                spawnIceChargeParticles(fairy);
            }
            return;
        }

        if (--data.shotTimer > 0) {
            return;
        }

        throwIceBlock(fairy, target);
        data.shotsLeft--;
        data.shotTimer = 10 + fairy.getRandom().nextInt(6);

        if (data.shotsLeft <= 0) {
            data.iceBlockCooldown = 80 + fairy.getRandom().nextInt(60);
            enterReposition(data, 20 + fairy.getRandom().nextInt(15));
        }
    }

    private static void throwIceBlock(IceFairy fairy, LivingEntity target) {
        if (!(fairy.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        float[] pitchYaw = DanmakuShooter.getPitchYaw(fairy, target);
        RandomSource random = fairy.getRandom();

        // 冰块体积大，先从冰精前方一点生成，避免出生就卡进自己/旁边的方块
        Vec3 look = fairy.getLookAngle();
        ThrownIceBlock iceBlock = new ThrownIceBlock(
                serverLevel,
                fairy.getX() + look.x * 0.8,
                fairy.getEyeY() + look.y * 0.8,
                fairy.getZ() + look.z * 0.8,
                RDBlocks.MAGIC_ICE_BLOCK.toStack()
        );
        iceBlock.setOwner(fairy);
        iceBlock.shootFromRotation(
                fairy,
                pitchYaw[0] - 10.0f,
                pitchYaw[1] + (random.nextFloat() - 0.5f) * 4.0f,
                0.0f,
                1.05f,
                0.8f
        );
        serverLevel.addFreshEntity(iceBlock);

        fairy.swing(InteractionHand.MAIN_HAND);
        serverLevel.playSound(
                null,
                fairy.getX(), fairy.getY(), fairy.getZ(),
                SoundEvents.SNOWBALL_THROW,
                SoundSource.HOSTILE,
                1.0f,
                0.65f
        );
        spawnIceChargeParticles(fairy);
    }

    // ---------------------------------------------------------------------
    // 琪露诺弹幕：完美冻结 + 冰柱坠落
    // ---------------------------------------------------------------------

    private static void startDanmaku(IceFairy fairy, StateData data) {
        data.state = State.DANMAKU;
        data.stateTicks = 20;
        data.patternStep = 0;
        data.shotTimer = 1;
        fairy.getNavigation().stop();

        if (fairy.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(
                    null,
                    fairy.getX(), fairy.getY(), fairy.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.HOSTILE,
                    1.0f,
                    1.4f
            );
        }
        spawnDanmakuChargeParticles(fairy);
    }

    private static void tickDanmaku(IceFairy fairy, StateData data, LivingEntity target) {
        data.stateTicks--;
        if (data.stateTicks > 0) {
            if (data.stateTicks % 2 == 0) {
                spawnDanmakuChargeParticles(fairy);
            }
            return;
        }

        if (--data.shotTimer > 0) {
            return;
        }

        switch (data.patternStep) {
            case 0 -> {
                fireFreezeRing(fairy, 0.0f);
                data.shotTimer = 7;
            }
            case 1 -> {
                fireFreezeRing(fairy, 11.25f);
                data.shotTimer = 10;
            }
            case 2, 3, 4 -> {
                fireIcicleFan(fairy, target);
                data.shotTimer = 6;
            }
            default -> {
                data.danmakuCooldown = 200 + fairy.getRandom().nextInt(100);
                enterReposition(data, 20 + fairy.getRandom().nextInt(15));
                return;
            }
        }
        data.patternStep++;
    }

    private static void fireFreezeRing(IceFairy fairy, float yawOffset) {
        if (!(fairy.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        int count = 16;
        DanmakuType type = yawOffset == 0.0f ? DanmakuTypes.STAR : DanmakuTypes.BALL;
        ItemColor color = yawOffset == 0.0f ? ItemColor.WHITE : ItemColor.LIGHT_BLUE;

        for (int i = 0; i < count; i++) {
            float yaw = i * (360.0f / count) + yawOffset;
            spawnIceDanmaku(serverLevel, fairy, type, color, -8.0f, yaw, 0.45f, 0.0f, 0.7f);
        }

        serverLevel.playSound(
                null,
                fairy.getX(), fairy.getY(), fairy.getZ(),
                yawOffset == 0.0f ? RDSoundEvents.SPELL_CARD : RDSoundEvents.BIU,
                SoundSource.HOSTILE,
                1.0f,
                yawOffset == 0.0f ? 1.2f : 1.5f
        );
        spawnSnowflakeRing(fairy, 16, 1.4);
    }

    private static void fireIcicleFan(IceFairy fairy, LivingEntity target) {
        if (!(fairy.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        float[] pitchYaw = DanmakuShooter.getPitchYaw(fairy, target);
        int count = 7;
        float spread = 60.0f;

        for (int i = 0; i < count; i++) {
            float yaw = pitchYaw[1] - spread / 2.0f + i * (spread / (count - 1));
            spawnIceDanmaku(
                    serverLevel,
                    fairy,
                    DanmakuTypes.ARROWHEAD,
                    ItemColor.LIGHT_BLUE,
                    pitchYaw[0],
                    yaw,
                    0.95f,
                    1.0f,
                    0.6f
            );
        }

        serverLevel.playSound(
                null,
                fairy.getX(), fairy.getY(), fairy.getZ(),
                RDSoundEvents.FIRE,
                SoundSource.HOSTILE,
                1.0f,
                1.3f
        );
    }

    private static void spawnIceDanmaku(ServerLevel level,
                                        IceFairy fairy,
                                        DanmakuType type,
                                        ItemColor color,
                                        float pitch,
                                        float yaw,
                                        float speed,
                                        float divergence,
                                        float offset) {
        ItemStackTemplate template = DanmakuTypes.withColor(type, color);
        if (template == null) {
            return;
        }

        ItemStack stack = template.create();
        DanmakuEntity danmaku = DanmakuShooter.spawn(level, fairy, stack, pitch, yaw, speed, divergence, offset);
        if (danmaku != null) {
            danmaku.setHitCallback((entity, target, damage) -> {
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 0));
                if (target.canFreeze()) {
                    target.setTicksFrozen(Math.min(
                            target.getTicksRequiredToFreeze() * 4,
                            target.getTicksFrozen() + 50
                    ));
                }
            });
        }
    }

    // ---------------------------------------------------------------------
    // 逃跑 / 位移
    // ---------------------------------------------------------------------

    private static void startFlee(IceFairy fairy, StateData data, LivingEntity target) {
        data.state = State.FLEE;
        data.stateTicks = 30;
        data.fleeCooldown = 100;
        data.fleePathFailures = 0;
        data.pathRecalcTicks = 6;
        fairy.getNavigation().stop();

        RandomSource random = fairy.getRandom();
        Vec3 away = new Vec3(fairy.getX() - target.getX(), 0.0, fairy.getZ() - target.getZ());
        if (away.lengthSqr() < 1.0E-4) {
            away = new Vec3(random.nextDouble() - 0.5, 0.0, random.nextDouble() - 0.5);
        }
        away = away.normalize();
        fairy.setDeltaMovement(fairy.getDeltaMovement().add(away.x * 0.35, 0.4, away.z * 0.35));

        if (fairy.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(
                    null,
                    fairy.getX(), fairy.getY(), fairy.getZ(),
                    SoundEvents.SNOW_GOLEM_HURT,
                    SoundSource.HOSTILE,
                    1.0f,
                    1.25f
            );
        }
        spawnSnowBurst(fairy, 12);
    }

    private static void tickFlee(IceFairy fairy, StateData data, LivingEntity target) {
        data.stateTicks--;

        if (data.pathRecalcTicks > 0) {
            data.pathRecalcTicks--;
        } else if (data.stateTicks % 8 == 0 || fairy.getNavigation().isDone()) {
            data.pathRecalcTicks = 7;
            moveAwayFromTarget(fairy, data, target);
        }

        boolean hasLineOfSight = fairy.getSensing().hasLineOfSight(target);

        if (data.fleePathFailures >= 3) {
            // 被逼到角落：看得见才转身打雪球，看不见就先回到走位状态
            if (hasLineOfSight) {
                startSnowball(fairy, data);
            } else {
                enterReposition(data, 10 + fairy.getRandom().nextInt(10));
            }
            return;
        }

        if (data.stateTicks <= 0 || fairy.distanceToSqr(target) >= 100.0) {
            if (hasLineOfSight) {
                startSnowball(fairy, data);
            } else {
                enterReposition(data, 10 + fairy.getRandom().nextInt(10));
            }
        }
    }

    private static void moveAwayFromTarget(IceFairy fairy, StateData data, LivingEntity target) {
        RandomSource random = fairy.getRandom();
        Vec3 away = new Vec3(fairy.getX() - target.getX(), 0.0, fairy.getZ() - target.getZ());
        if (away.lengthSqr() < 1.0E-4) {
            away = new Vec3(random.nextDouble() - 0.5, 0.0, random.nextDouble() - 0.5);
        }
        away = away.normalize();

        double distance = 9.0 + random.nextDouble() * 4.0;
        double y = fairy.getY();
        boolean moved = fairy.getNavigation().moveTo(
                fairy.getX() + away.x * distance,
                y,
                fairy.getZ() + away.z * distance,
                1.6
        );
        if (!moved) {
            moved = fairy.getNavigation().moveTo(
                    fairy.getX() - away.z * distance,
                    y,
                    fairy.getZ() + away.x * distance,
                    1.6
            );
        }
        if (!moved) {
            moved = fairy.getNavigation().moveTo(
                    fairy.getX() + away.z * distance,
                    y,
                    fairy.getZ() - away.x * distance,
                    1.6
            );
        }

        if (moved) {
            data.fleePathFailures = 0;
        } else {
            data.fleePathFailures++;
        }
    }

    private static void enterReposition(StateData data, int ticks) {
        data.state = State.REPOSITION;
        data.stateTicks = ticks;
        data.pathRecalcTicks = 0;
    }

    // ---------------------------------------------------------------------
    // 粒子
    // ---------------------------------------------------------------------

    private static void spawnSnowBurst(IceFairy fairy, int count) {
        if (fairy.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.SNOWFLAKE,
                    fairy.getX(), fairy.getEyeY(), fairy.getZ(),
                    count,
                    0.4, 0.4, 0.4,
                    0.02
            );
        }
    }

    private static void spawnSnowflakeRing(IceFairy fairy, int count, double radius) {
        if (!(fairy.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        for (int i = 0; i < count; i++) {
            double angle = i * Math.PI * 2.0 / count;
            serverLevel.sendParticles(
                    ParticleTypes.SNOWFLAKE,
                    fairy.getX() + Math.cos(angle) * radius,
                    fairy.getY() + 1.0,
                    fairy.getZ() + Math.sin(angle) * radius,
                    1,
                    0.0, 0.0, 0.0,
                    0.0
            );
        }
    }

    private static void spawnIceChargeParticles(IceFairy fairy) {
        if (!(fairy.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockParticleOption particle = new BlockParticleOption(
                ParticleTypes.BLOCK,
                RDBlocks.MAGIC_ICE_BLOCK.defaultBlockState()
        );
        serverLevel.sendParticles(
                particle,
                fairy.getX(), fairy.getY() + 1.0, fairy.getZ(),
                10,
                0.6, 0.6, 0.6,
                0.02
        );
    }

    private static void spawnDanmakuChargeParticles(IceFairy fairy) {
        spawnSnowflakeRing(fairy, 12, 1.0);
        if (fairy.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.CLOUD,
                    fairy.getX(), fairy.getY() + 1.0, fairy.getZ(),
                    6,
                    0.4, 0.2, 0.4,
                    0.01
            );
        }
    }
}
