package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.ai.goal.GhostStatusEffectTargetGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.NPCFollowOwnerGoal;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.KeepInventoryTypes;
import cc.thonly.reverie_dreams.registry.content.effect.RDStatusEffects;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.skin.MobSkinTypes;
import cc.thonly.reverie_dreams.world.RDBuiltInGameRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.UUID;

public class Ghost extends BaseNPCLikeEntity {
    private static final int MAX_SURVIVAL_TICKS = 10 * 60 * 20;
    private static final int HAUNT_TICKS = 2 * 60 * 20;
    private int survivalTime;
    private UUID hauntedPlayer;
    private long hauntUntil;

    public Ghost(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world, MobSkinTypes.GHOST);
    }

    public static boolean canStartHaunt(ServerLevel level) {
        return level == level.getServer().overworld()
                && level.getDifficulty() != Difficulty.PEACEFUL
                && ReverieDreams.config().enableGhostSpawn
                && level.getGameRules().get(RDBuiltInGameRules.DO_GHOST.value());
    }

    public static boolean tryHaunt(ServerLevel level, ServerPlayer player) {
        if (!canStartHaunt(level) || player.isCreative() || player.isSpectator() || player.isSleeping()
                || !player.isAlive() || GhostStatusEffectTargetGoal.hasSilverArmor(player)
                || player.hasEffect(RDStatusEffects.MENTAL_DISORDER.builtInHolder())) {
            return false;
        }
        UUID owner = player.getUUID();
        if (!level.getEntitiesOfClass(Ghost.class, player.getBoundingBox().inflate(24),
                ghost -> ghost.isAlive() && owner.equals(ghost.hauntedPlayer)).isEmpty()) {
            return false;
        }
        var type = RDEntityTypes.GHOST.value();
        var random = player.getRandom();
        for (int i = 0; i < 12; i++) {
            int x = random.nextInt(13) - 6;
            int z = random.nextInt(13) - 6;
            if (x * x + z * z < 9) continue;
            BlockPos column = player.blockPosition().offset(x, 0, z);
            if (!level.hasChunkAt(column) || !level.getWorldBorder().isWithinBounds(column)) continue;
            for (int dy : new int[]{0, 1, -1, 2, -2}) {
                BlockPos pos = column.offset(0, dy, 0);
                if (level.isOutsideBuildHeight(pos.below()) || level.isOutsideBuildHeight(pos.above())
                        || !SpawnPlacementTypes.ON_GROUND.isSpawnPositionOk(level, pos, type)
                        || !level.noCollision(type.getSpawnAABB(pos.getX() + .5, pos.getY(), pos.getZ() + .5))) continue;
                Ghost ghost = type.create(level, EntitySpawnReason.TRIGGERED);
                if (ghost == null) return false;
                ghost.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
                if (!level.noCollision(ghost) || !level.isUnobstructed(ghost)) continue;
                ghost.hauntedPlayer = owner;
                ghost.hauntUntil = level.getGameTime() + HAUNT_TICKS;
                ghost.setTarget(player);
                if (level.addFreshEntity(ghost)) {
                    player.addEffect(new MobEffectInstance(RDStatusEffects.MENTAL_DISORDER.builtInHolder(), 20 * 60 * 5));
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new GroundPathNavigation(this, level) {
            @Override
            protected boolean canUpdatePath() {
                return super.canUpdatePath() || Ghost.this.isNoGravity();
            }
        };
    }

    @Override
    public void tick() {
        if (this.level() instanceof ServerLevel level && this.isAlive()) {
            boolean protectedGhost = this.hasCustomName() || this.isTame() || this.isPersistenceRequired()
                    || this.isPassenger() || this.isVehicle();
            if (this.hauntedPlayer != null) {
                var target = level.getPlayerByUUID(this.hauntedPlayer);
                if (!canStartHaunt(level) || level.getGameTime() >= this.hauntUntil || target == null
                        || !target.hasEffect(RDStatusEffects.MENTAL_DISORDER.builtInHolder())
                        || (target instanceof ServerPlayer serverPlayer && serverPlayer.hasDisconnected())
                        || target.isSleeping() || !this.validTarget(target) || this.isTame()) {
                    this.setTarget(null);
                    this.getNavigation().stop();
                    if (!protectedGhost) {
                        this.discard();
                        return;
                    }
                } else if (this.getTarget() != target) {
                    this.setTarget(target);
                }
            } else if (!protectedGhost && (level.getDifficulty() == Difficulty.PEACEFUL
                    || ++this.survivalTime >= MAX_SURVIVAL_TICKS)) {
                this.discard();
                return;
            }
            if (this.getTarget() != null && !this.validTarget(this.getTarget())) {
                this.setTarget(null);
                this.getNavigation().stop();
            }
            this.updateHover(level);
            if (this.tickCount % 12 == 0) {
                level.sendParticles(ParticleTypes.WHITE_ASH, this.getX(), this.getY() + .14, this.getZ(),
                        1, .12, .04, .12, .01);
            }
        }
        super.tick();
    }

    /** Follow ordinary ground paths and hover just above a nearby supporting surface. */
    private void updateHover(ServerLevel level) {
        if (this.isPassenger() || this.isVehicle() || this.isOrderedToSit() || this.isSleeping() || this.isInLiquid()) {
            this.setNoGravity(false);
            return;
        }
        var box = this.getBoundingBox();
        if (level.noCollision(this, box.move(0, -.68, 0))) {
            this.setNoGravity(false);
            return;
        }
        this.setNoGravity(true);
        double lift = level.noCollision(this, box.move(0, -.26, 0)) ? -.035 : .05;
        Vec3 velocity = this.getDeltaMovement();
        this.setDeltaMovement(velocity.x, Mth.clamp(velocity.y * .5 + lift, -.07, .10), velocity.z);
    }

    private boolean validTarget(LivingEntity target) {
        return target != this && target.isAlive() && target.level() == this.level()
                && this.distanceToSqr(target) <= 16 * 16 && !(target instanceof Ghost)
                && !this.isAlliedTo(target) && !this.isOwnedBy(target)
                && !(target instanceof Player player && (player.isCreative() || player.isSpectator()))
                && !GhostStatusEffectTargetGoal.hasSilverArmor(target);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && this.isAlive() && this.hauntedPlayer == null && this.canFight()
                && source.getEntity() instanceof LivingEntity attacker && this.validTarget(attacker)) {
            this.setTarget(attacker);
        }
        return hurt;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new LungeGoal());

        this.goalSelector.addGoal(6, new NPCFollowOwnerGoal(this, 1.0, 2.0f, 10.0f));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, .8));

        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, BaseNPCLikeEntity.class, 8.0f));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));

    }

    private boolean canFight() {
        return !this.isTame() && !this.isOrderedToSit() && !this.isSleeping()
                && this.level().getDifficulty() != Difficulty.PEACEFUL;
    }

    @Override
    protected void updateAttackType() {
        // This entity uses LungeGoal rather than the equipment-based NPC attack goals.
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel level, DamageSource source) {
        // Run the loot table without exposing the NPC's inventory or equipment.
        if (this.hauntedPlayer == null) {
            if (this.shouldDropLoot(level)) {
                this.dropFromLootTable(level, source, this.lastHurtByPlayerMemoryTime > 0);
            }
            this.dropExperience(level, source.getEntity());
        }
    }

    private final class LungeGoal extends Goal {
        private static final int WINDUP = 12;
        private static final int DASH = 5;
        private static final int RECOVERY = 23;
        private long nextWindup, strikeAt, recoverAt;
        private int repath;
        private LivingEntity victim;
        private Vec3 direction = Vec3.ZERO;

        private LungeGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override public boolean canUse() {
            LivingEntity target = Ghost.this.getTarget();
            return Ghost.this.canFight() && target != null && Ghost.this.validTarget(target);
        }

        @Override public boolean requiresUpdateEveryTick() { return true; }

        @Override public void stop() {
            this.victim = null;
            this.repath = 0;
            Ghost.this.setAggressive(false);
            Ghost.this.getNavigation().stop();
        }

        @Override public void tick() {
            var ghost = Ghost.this;
            if (!(ghost.level() instanceof ServerLevel level) || !this.canUse()) return;
            LivingEntity target = ghost.getTarget();
            long now = level.getGameTime();
            ghost.getLookControl().setLookAt(target, 35, 30);
            boolean visible = ghost.getSensing().hasLineOfSight(target);
            if (this.victim != null) {
                if (this.victim != target || !visible || ghost.horizontalCollision || now >= this.recoverAt) {
                    this.retreat(now);
                } else if (now >= this.strikeAt) {
                    if (now == this.strikeAt) {
                        ghost.swing(InteractionHand.MAIN_HAND);
                        ghost.playSound(SoundEvents.VEX_CHARGE, .45F, .75F);
                    }
                    Vec3 offset = target.position().subtract(ghost.position());
                    Vec3 flat = new Vec3(offset.x, 0, offset.z);
                    boolean inFront = flat.lengthSqr() < .16 || flat.normalize().dot(this.direction) >= .5;
                    if (inFront && offset.lengthSqr() <= 2.1 * 2.1) {
                        ghost.doHurtTarget(level, target);
                        this.retreat(now);
                    } else {
                        this.push(.52);
                    }
                } else {
                    this.push(0);
                }
                return;
            }
            if (now < this.nextWindup) {
                if (now >= this.nextWindup - RECOVERY && now < this.nextWindup - RECOVERY + 4) this.push(-.14);
                return;
            }
            if (visible && ghost.distanceToSqr(target) <= 4 * 4 && Math.abs(ghost.getY() - target.getY()) < 1.4) {
                Vec3 offset = target.position().subtract(ghost.position());
                this.direction = new Vec3(offset.x, 0, offset.z).normalize();
                this.victim = target;
                this.strikeAt = now + WINDUP;
                this.recoverAt = this.strikeAt + DASH;
                ghost.getNavigation().stop();
                this.push(0);
                ghost.setAggressive(true);
                ghost.playSound(SoundEvents.SOUL_ESCAPE.value(), .8F, .85F);
                level.sendParticles(ParticleTypes.SOUL, ghost.getX(), ghost.getY() + .9, ghost.getZ(), 5, .26, .28, .26, .02);
            } else if (--this.repath <= 0) {
                ghost.getNavigation().moveTo(target, 1);
                this.repath = 10;
            }
        }

        private void retreat(long now) {
            Ghost.this.setAggressive(false);
            this.push(-.14);
            this.victim = null;
            this.nextWindup = now + RECOVERY;
            this.repath = 0;
        }

        private void push(double speed) {
            var ghost = Ghost.this;
            ghost.setDeltaMovement(this.direction.x * speed, ghost.getDeltaMovement().y, this.direction.z * speed);
        }
    }

    @Override
    public void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        view.putInt("SurvivalTime", this.survivalTime);
        if (this.hauntedPlayer != null) {
            view.store("HauntedPlayer", UUIDUtil.CODEC, this.hauntedPlayer);
            view.putLong("HauntUntil", this.hauntUntil);
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        this.survivalTime = Mth.clamp(view.getIntOr("SurvivalTime", 0), 0, MAX_SURVIVAL_TICKS);
        this.hauntedPlayer = view.read("HauntedPlayer", UUIDUtil.CODEC).orElse(null);
        this.hauntUntil = view.getLongOr("HauntUntil", 0);
    }

    @Override
    public KeepInventoryTypes getKeepInventoryType() {
        return KeepInventoryTypes.NOT_DROP_ANY;
    }

    @Override
    public Boolean canPickItem() {
        return false;
    }
}
