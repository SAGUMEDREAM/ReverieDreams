package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.registry.content.ItemColor;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.server.DelayedTask;
import cc.thonly.reverie_dreams.registry.content.sound.RDSoundEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 毘沙门天的宝塔。
 *
 * <p>右击释放金色激光扇；潜行右击展开跟随自身的宝塔结界，
 * 周期性轰击附近敌人并为友方提供财富与战斗加护。</p>
 */
public class VaisravanasPagodaItem extends Item {
    private static final int ATTACK_COOLDOWN_TICKS = 20 * 6;
    private static final int DOMAIN_COOLDOWN_TICKS = 20 * 45;
    private static final int DOMAIN_PULSES = 7;
    private static final int DOMAIN_INTERVAL_TICKS = 20 * 2;
    private static final int DOMAIN_DURABILITY_COST = 10;
    private static final double ATTACK_RANGE = 24.0D;
    private static final double DOMAIN_RADIUS = 14.0D;
    private static final float DIRECT_DAMAGE = 7.0F;
    private static final float DOMAIN_DAMAGE = 5.0F;

    public VaisravanasPagodaItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(world instanceof ServerLevel serverLevel) || !(user instanceof ServerPlayer player)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        ItemCooldowns cooldowns = player.getCooldowns();
        if (cooldowns.isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        if (player.isShiftKeyDown()) {
            activateDomain(serverLevel, player, stack);
        } else {
            fireBarrage(serverLevel, player, stack);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        return this.use(context.getLevel(), player, context.getHand());
    }

    private static void fireBarrage(ServerLevel level, ServerPlayer player, ItemStack stack) {
        Vec3 origin = player.getEyePosition();
        LivingEntity target = findNearestTarget(level, player, origin, ATTACK_RANGE);

        float pitch;
        float yaw;
        if (target != null) {
            float[] rotation = rotationTo(origin, target.getEyePosition());
            pitch = rotation[0];
            yaw = rotation[1];
        } else {
            pitch = player.getXRot();
            yaw = player.getYRot();
        }

        // Five golden lasers in a narrow fan, each retaining a chance to shake loose treasure.
        for (int i = -2; i <= 2; i++) {
            fireLaser(level, player, origin, pitch, yaw + i * 6.0F, true);
        }
        level.playSound(null, player.blockPosition(), RDSoundEvents.FIRE.value(),
                SoundSource.PLAYERS, 1.0F, 0.8F);

        player.getCooldowns().addCooldown(stack, ATTACK_COOLDOWN_TICKS);
        damage(stack, 1, player);
    }

    private static void activateDomain(ServerLevel level, ServerPlayer player, ItemStack stack) {
        level.playSound(null, player.blockPosition(), RDSoundEvents.SPELL_CARD.value(),
                SoundSource.PLAYERS, 1.0F, 1.0F);
        spawnDomainParticles(level, player);

        for (int pulse = 0; pulse < DOMAIN_PULSES; pulse++) {
            int delay = pulse * DOMAIN_INTERVAL_TICKS;
            DelayedTask.create(level.getServer(), delay, () -> {
                if (!player.isAlive() || player.isRemoved() || player.level() != level) {
                    return;
                }
                pulseDomain(level, player);
            });
        }

        player.getCooldowns().addCooldown(stack, DOMAIN_COOLDOWN_TICKS);
        damage(stack, DOMAIN_DURABILITY_COST, player);
    }

    private static void pulseDomain(ServerLevel level, ServerPlayer player) {
        Vec3 origin = player.getEyePosition();
        List<LivingEntity> targets = findTargets(level, player, origin, DOMAIN_RADIUS);

        for (int i = 0; i < Math.min(3, targets.size()); i++) {
            LivingEntity target = targets.get(i);
            float[] rotation = rotationTo(origin, target.getEyePosition());
            fireLaser(level, player, origin, rotation[0], rotation[1], false);
        }

        AABB aura = new AABB(
                player.getX() - DOMAIN_RADIUS, player.getY() - 4.0D, player.getZ() - DOMAIN_RADIUS,
                player.getX() + DOMAIN_RADIUS, player.getY() + 6.0D, player.getZ() + DOMAIN_RADIUS
        );
        for (Player ally : level.getEntitiesOfClass(Player.class, aura, p -> p.isAlive() && !p.isSpectator())) {
            if (ally != player && !player.isAlliedTo(ally)) {
                continue;
            }
            ally.addEffect(new MobEffectInstance(MobEffects.LUCK, 120, 1));
            ally.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 100, 0));
            ally.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0));
        }

        spawnDomainParticles(level, player);
        level.playSound(null, player.blockPosition(), RDSoundEvents.FIRE.value(),
                SoundSource.PLAYERS, 0.8F, 1.25F);
    }

    private static void spawnDomainParticles(ServerLevel level, ServerPlayer player) {
        // Three shrinking rings form a faint tiered pagoda silhouette.
        spawnRing(level, player, 3.0D, 0.15D);
        spawnRing(level, player, 2.2D, 1.2D);
        spawnRing(level, player, 1.4D, 2.4D);
        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                player.getX(), player.getY() + 0.9D, player.getZ(),
                12, 1.6D, 0.5D, 1.6D, 0.02D
        );
    }

    private static void spawnRing(ServerLevel level, ServerPlayer player, double radius, double height) {
        for (int i = 0; i < 16; i++) {
            double angle = Math.PI * 2.0D * i / 16.0D;
            level.sendParticles(
                    ParticleTypes.END_ROD,
                    player.getX() + Math.cos(angle) * radius,
                    player.getY() + height,
                    player.getZ() + Math.sin(angle) * radius,
                    1, 0.0D, 0.0D, 0.0D, 0.0D
            );
        }
    }

    private static void fireLaser(ServerLevel level, ServerPlayer owner, Vec3 origin,
                                  float pitch, float yaw, boolean direct) {
        ItemStack laserStack = DanmakuTypes.withColor(DanmakuTypes.LASER, ItemColor.YELLOW).create();
        DanmakuProperties properties = laserStack.get(RDDataComponentTypes.DANMAKU_PROPERTIES.value());
        if (properties != null) {
            laserStack.set(RDDataComponentTypes.DANMAKU_PROPERTIES.value(), properties
                    .withDamage(direct ? DIRECT_DAMAGE : DOMAIN_DAMAGE)
                    .withSpeed(direct ? 2.5F : 2.7F)
                    .withScale(direct ? 1.25F : 1.0F)
                    .withHitboxScale(direct ? 1.0F : 0.85F));
        }

        DanmakuEntity laser = DanmakuTrajectory.spawnByItemStack(
                level, owner, origin.x, origin.y, origin.z,
                laserStack, pitch, yaw, 0.0F, 0.9F
        );
        if (laser == null) {
            return;
        }

        laser.setDuration(direct ? 120 : 100);
        laser.setHitCallback((self, target, damage) -> {
            target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), direct ? 80 : 60));
            dropTreasure(level, target, direct ? 0.15F : 0.06F);
        });
    }

    private static List<LivingEntity> findTargets(ServerLevel level, ServerPlayer owner, Vec3 center, double radius) {
        AABB area = new AABB(
                center.x - radius, center.y - 8.0D, center.z - radius,
                center.x + radius, center.y + 8.0D, center.z + radius
        );
        List<LivingEntity> targets = new ArrayList<>(level.getEntitiesOfClass(LivingEntity.class, area,
                target -> target != owner
                        && target.isAlive()
                        && !target.isSpectator()
                        && target instanceof Enemy
                        && owner.hasLineOfSight(target)));
        targets.sort(Comparator.comparingDouble(target -> center.distanceToSqr(target.position())));
        return targets;
    }

    @Nullable
    private static LivingEntity findNearestTarget(ServerLevel level, ServerPlayer owner, Vec3 center, double radius) {
        List<LivingEntity> targets = findTargets(level, owner, center, radius);
        return targets.isEmpty() ? null : targets.get(0);
    }

    private static float[] rotationTo(Vec3 from, Vec3 to) {
        Vec3 delta = to.subtract(from);
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        float yaw = (float) Math.toDegrees(Math.atan2(-delta.x, delta.z));
        float pitch = (float) -Math.toDegrees(Math.atan2(delta.y, horizontal));
        return new float[]{pitch, yaw};
    }

    private static void dropTreasure(ServerLevel level, LivingEntity target, float chance) {
        if (level.getRandom().nextFloat() >= chance) {
            return;
        }
        Item coin = level.getRandom().nextFloat() < 0.2F
                ? RDItems.SILVER_COIN.asItem()
                : RDItems.COPPER_COIN.asItem();
        int count = level.getRandom().nextIntBetweenInclusive(1, 2);
        ItemEntity itemEntity = new ItemEntity(
                level,
                target.getX(),
                target.getY() + target.getBbHeight() * 0.5D,
                target.getZ(),
                new ItemStack(coin, count)
        );
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
    }

    private static void damage(ItemStack stack, int amount, ServerPlayer player) {
        if (!player.hasInfiniteMaterials()) {
            stack.hurtWithoutBreaking(amount, player);
        }
    }
}
