
package cc.thonly.reverie_dreams.entity.projectile;

import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class BaguaFireball extends SmallFireball {
    private static final float DAMAGE = 5.0F;
    private static final float BONUS_FIRE_DAMAGE = 3.0F;

    private static final double SPLASH_RADIUS = 2.5D;
    private static final float SPLASH_DAMAGE = 3.0F;
    private static final int BURN_TICKS = 40;

    public BaguaFireball(EntityType<? extends BaguaFireball> type, Level level) {
        super(type, level);
    }

    public BaguaFireball(ServerLevel level, LivingEntity owner) {
        super(RDEntityTypes.BAGUA_FIREBALL.value(), level);
        this.setOwner(owner);
        this.setPos(owner.getX(), owner.getEyeY(), owner.getZ());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Entity target = result.getEntity();
        Entity owner = this.getOwner();

        DamageSource source = this.damageSources().fireball(this, owner);

        if (hurtWithFire(serverLevel, target, source, DAMAGE)) {
            if (target.isAlive()) {
                target.hurtServer(
                        serverLevel,
                        target.damageSources().onFire(),
                        BONUS_FIRE_DAMAGE
                );
            }
        }

        this.spawnImpactEffects(serverLevel, result.getLocation(), target);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos pos = result.getBlockPos();
        BlockState state = this.level().getBlockState(pos);

        state.onProjectileHit(this.level(), state, result, this);

        this.spawnImpactEffects(serverLevel, result.getLocation(), null);
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.FLAME;
    }

    /**
     * 生成火焰溅射效果。
     */
    private void spawnImpactEffects(ServerLevel level, Vec3 position, Entity directHit) {
        this.spawnSplashDamage(level, position, directHit);

        // 中心火焰
        level.sendParticles(
                ParticleTypes.FLAME,
                position.x, position.y, position.z,
                32,
                0.65D, 0.45D, 0.65D,
                0.12D
        );

        // 火焰烟尘
        level.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                position.x, position.y, position.z,
                14,
                0.45D, 0.35D, 0.45D,
                0.035D
        );

        // 飞溅火星
        level.sendParticles(
                ParticleTypes.LAVA,
                position.x, position.y, position.z,
                12,
                0.55D, 0.45D, 0.55D,
                0.08D
        );

        level.playSound(
                null,
                BlockPos.containing(position),
                SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.PLAYERS,
                0.7F,
                1.4F
        );
    }

    /**
     * 对命中点附近的生物造成范围火焰伤害。
     */
    private void spawnSplashDamage(ServerLevel level, Vec3 position, Entity directHit) {
        AABB area = AABB.ofSize(
                position,
                SPLASH_RADIUS * 2.0D,
                SPLASH_RADIUS * 2.0D,
                SPLASH_RADIUS * 2.0D
        );

        Entity owner = this.getOwner();
        DamageSource source = this.damageSources().fireball(this, owner);

        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity -> entity.isAlive()
                        && !entity.isSpectator()
                        && entity != owner
                        && entity != directHit
        )) {
            Vec3 targetCenter = target.position().add(
                    0.0D,
                    target.getBbHeight() * 0.5D,
                    0.0D
            );

            double distance = targetCenter.distanceTo(position);

            if (distance > SPLASH_RADIUS) {
                continue;
            }

            // 距离越远，伤害越低。
            float damage = SPLASH_DAMAGE * (float) (
                    1.0D - distance / SPLASH_RADIUS
            );

            damage = Math.max(0.5F, damage);

            hurtWithFire(level, target, source, damage);
        }
    }

    private static boolean hurtWithFire(ServerLevel level, Entity target, DamageSource source, float damage) {
        int previousFireTicks = target.getRemainingFireTicks();
        // Lethal damage evaluates loot immediately, so cooking must see the fire now.
        target.igniteForTicks(BURN_TICKS);
        if (target.hurtServer(level, source, damage)) {
            return true;
        }
        target.setRemainingFireTicks(previousFireTicks);
        return false;
    }
}
