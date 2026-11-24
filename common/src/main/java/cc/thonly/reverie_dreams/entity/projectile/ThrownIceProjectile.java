package cc.thonly.reverie_dreams.entity.projectile;

import cc.thonly.reverie_dreams.api.entity.type.FriendlyFaction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 冰精使用的投掷物基类：雪球/冰块共用命中、碎冰粒子和冰缓效果。
 * 复用原版 SNOWBALL 实体类型，客户端由 ThrownItemRenderer 按 ItemStack 渲染，
 * 因此冰块会直接显示为冰块模型。
 */
public abstract class ThrownIceProjectile extends ThrowableItemProjectile {
    protected ThrownIceProjectile(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    protected ThrownIceProjectile(EntityType<? extends ThrowableItemProjectile> type, LivingEntity owner, Level level, ItemStack stack) {
        super(type, owner, level, stack);
    }

    protected ThrownIceProjectile(EntityType<? extends ThrowableItemProjectile> type, double x, double y, double z, Level level, ItemStack stack) {
        super(type, x, y, z, level, stack);
    }

    protected abstract float hitDamage();

    protected abstract int slownessTicks();

    protected abstract int slownessAmplifier();

    protected abstract int freezeTicks();

    /**
     * 直接命中后的碎冰范围伤害半径，0 表示没有范围伤害。
     */
    protected abstract int aoeRadius();

    protected abstract ParticleOptions breakParticle();

    protected abstract SoundEvent breakSound();

    @Override
    public void tick() {
        super.tick();
        // 防止抛向虚空/高空的投掷物长期滞留
        if (!this.level().isClientSide() && this.tickCount > 100) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Entity hitEntity = result.getEntity();
        Entity owner = this.getOwner();
        if (hitEntity == owner) {
            return;
        }
        if (FriendlyFaction.isFriendLy(hitEntity, owner)
                && !FriendlyFaction.isDirectTarget(owner, hitEntity)) {
            return;
        }

        if (hitEntity instanceof LivingEntity living) {
            this.applyHit(living, this.hitDamage());

            int radius = this.aoeRadius();
            if (radius > 0) {
                for (LivingEntity nearby : serverLevel.getEntitiesOfClass(
                        LivingEntity.class,
                        living.getBoundingBox().inflate(radius),
                        entity -> entity != living && entity != owner
                                && (!FriendlyFaction.isFriendLy(entity, owner)
                                || FriendlyFaction.isDirectTarget(owner, entity))
                )) {
                    this.applyHit(nearby, this.hitDamage() * 0.5f);
                }
            }

            serverLevel.sendParticles(
                    this.breakParticle(),
                    living.getX(),
                    living.getY() + living.getBbHeight() / 2.0,
                    living.getZ(),
                    12,
                    0.3, 0.3, 0.3,
                    0.05
            );
        }

        this.playBreakSound(serverLevel);
        serverLevel.broadcastEntityEvent(this, (byte) 3);
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 pos = result.getLocation();
        serverLevel.sendParticles(
                this.breakParticle(),
                pos.x, pos.y, pos.z,
                16,
                0.3, 0.3, 0.3,
                0.05
        );
        this.playBreakSound(serverLevel);
        serverLevel.broadcastEntityEvent(this, (byte) 3);
        this.discard();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            this.discard();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            for (int i = 0; i < 8; i++) {
                this.level().addParticle(
                        this.breakParticle(),
                        this.getX(), this.getY(), this.getZ(),
                        0.0, 0.0, 0.0
                );
            }
        }
    }

    @SuppressWarnings("deprecation")
    protected void applyHit(LivingEntity target, float damage) {
        Entity owner = this.getOwner();
        target.hurt(this.damageSources().thrown(this, owner), damage);

        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, this.slownessTicks(), this.slownessAmplifier()));
        if (target.canFreeze()) {
            target.setTicksFrozen(Math.min(
                    target.getTicksRequiredToFreeze() * 3,
                    target.getTicksFrozen() + this.freezeTicks()
            ));
        }

        Vec3 motion = this.getDeltaMovement();
        target.knockback(0.35, motion.x, motion.z);
    }

    private void playBreakSound(ServerLevel level) {
        level.playSound(
                null,
                this.getX(), this.getY(), this.getZ(),
                this.breakSound(),
                SoundSource.NEUTRAL,
                0.9f,
                0.9f + this.random.nextFloat() * 0.2f
        );
    }
}
