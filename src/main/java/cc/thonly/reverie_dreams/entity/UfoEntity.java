package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

@Getter
public class UfoEntity extends Monster implements Enemy {
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING = SynchedEntityData.defineId(UfoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final byte DEFAULT_EXPLOSION_POWER = 1;
    private int explosionPower = 1;

    public UfoEntity(Level level) {
        super(RDEntityTypes.UFO, level);
        this.xpReward = 5;
        this.moveControl = new Ghast.GhastMoveControl(this, false, () -> false);
    }

    public UfoEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static boolean checkSpawnRules(
            EntityType<UfoEntity> type,
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random
    ) {
        int max = 3;
        int radius = 32;

        List<UfoEntity> nearby = level.getEntitiesOfClass(
                UfoEntity.class,
                new AABB(pos).inflate(radius)
        );

        return nearby.size() < max;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(5, new Ghast.RandomFloatAroundGoal(this));
        this.goalSelector.addGoal(7, new Ghast.GhastLookGoal(this));
        this.goalSelector.addGoal(7, new ShootGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<Player>(this, Player.class, 16, true, false, (livingEntity, serverLevel) -> Math.abs(livingEntity.getY() - this.getY()) <= 4.0));
        this.goalSelector.addGoal(8, new FloatUpDownGoal(this));
        this.navigation = new FlyingPathNavigation(this, this.level());
    }

    public boolean isCharging() {
        return this.entityData.get(DATA_IS_CHARGING);
    }

    public void setCharging(boolean bl) {
        this.entityData.set(DATA_IS_CHARGING, bl);
    }

    private static boolean isReflectedFireball(DamageSource damageSource) {
        return damageSource.getDirectEntity() instanceof LargeFireball && damageSource.getEntity() instanceof Player;
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel serverLevel, DamageSource damageSource) {
        return this.isInvulnerable() && !damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || !isReflectedFireball(damageSource) && super.isInvulnerableTo(serverLevel, damageSource);
    }

    @Override
    protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public void travel(Vec3 vec3) {
        this.travelFlying(vec3, 0.02f);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        if (isReflectedFireball(damageSource)) {
            super.hurtServer(serverLevel, damageSource, 1000.0f);
            return true;
        }
        if (this.isInvulnerableTo(serverLevel, damageSource)) {
            return false;
        }
        return super.hurtServer(serverLevel, damageSource, f);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_IS_CHARGING, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 40.0).add(Attributes.FOLLOW_RANGE, 100.0).add(Attributes.CAMERA_DISTANCE, 8.0).add(Attributes.FLYING_SPEED, 0.06);
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ANVIL_LAND;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEventInit.BIU;
    }

    @Override
    protected float getSoundVolume() {
        return 5.0f;
    }

    public static boolean checkGhastSpawnRules(EntityType<Ghast> entityType, LevelAccessor levelAccessor, EntitySpawnReason entitySpawnReason, BlockPos blockPos, RandomSource randomSource) {
        return levelAccessor.getDifficulty() != Difficulty.PEACEFUL && randomSource.nextInt(20) == 0 && Ghast.checkMobSpawnRules(entityType, levelAccessor, entitySpawnReason, blockPos, randomSource);
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 1;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putByte("ExplosionPower", (byte)this.explosionPower);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.explosionPower = valueInput.getByteOr("ExplosionPower", (byte)1);
    }

    @Override
    public boolean supportQuadLeashAsHolder() {
        return true;
    }

    @Override
    public double leashElasticDistance() {
        return 10.0;
    }

    @Override
    public double leashSnapDistance() {
        return 16.0;
    }

    public static void faceMovementDirection(Mob mob) {
        if (mob.getTarget() == null) {
            Vec3 vec3 = mob.getDeltaMovement();
            mob.setYRot(-((float) Mth.atan2(vec3.x, vec3.z)) * 57.295776f);
            mob.yBodyRot = mob.getYRot();
        } else {
            LivingEntity livingEntity = mob.getTarget();
            double d = 64.0;
            if (livingEntity.distanceToSqr(mob) < 4096.0) {
                double e = livingEntity.getX() - mob.getX();
                double f = livingEntity.getZ() - mob.getZ();
                mob.setYRot(-((float)Mth.atan2(e, f)) * 57.295776f);
                mob.yBodyRot = mob.getYRot();
            }
        }
    }

    static class ShootGoal
            extends Goal {
        private final UfoEntity entity;
        public int chargeTime;

        public ShootGoal(UfoEntity ghast) {
            this.entity = ghast;
        }

        @Override
        public boolean canUse() {
            return this.entity.getTarget() != null;
        }

        @Override
        public void start() {
            this.chargeTime = 0;
        }

        @Override
        public void stop() {
            this.entity.setCharging(false);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity livingEntity = this.entity.getTarget();
            if (livingEntity == null) {
                return;
            }
            double d = 64.0;
            if (livingEntity.distanceToSqr(this.entity) < 4096.0 && this.entity.hasLineOfSight(livingEntity)) {
                Level level = this.entity.level();
                ++this.chargeTime;
                if (this.chargeTime == 10 && !this.entity.isSilent()) {
                    level.levelEvent(null, 1015, this.entity.blockPosition(), 0);
                }
                if (this.chargeTime == 20) {
                    double e = 4.0;
                    Vec3 vec3 = this.entity.getViewVector(1.0f);
                    double f = livingEntity.getX() - (this.entity.getX() + vec3.x * 4.0);
                    double g = livingEntity.getY(0.5) - (0.5 + this.entity.getY(0.5));
                    double h = livingEntity.getZ() - (this.entity.getZ() + vec3.z * 4.0);
                    Vec3 vec32 = new Vec3(f, g, h);
                    if (!this.entity.isSilent()) {
                        level.levelEvent(null, 1016, this.entity.blockPosition(), 0);
                    }
                    WindCharge windCharge = new WindCharge(level, this.entity.getX(), this.entity.getY(), this.entity.getZ(), vec32.normalize());
                    windCharge.setPos(this.entity.getX() + vec3.x * 4.0, this.entity.getY(0.5) + 0.5, windCharge.getZ() + vec3.z * 4.0);
                    level.addFreshEntity(windCharge);
                    this.chargeTime = -40;
                }
            } else if (this.chargeTime > 0) {
                --this.chargeTime;
            }
            this.entity.setCharging(this.chargeTime > 10);
        }
    }

    static class FloatUpDownGoal extends Goal {
        private final UfoEntity ufo;
        private int timer = 0;

        public FloatUpDownGoal(UfoEntity ufo) {
            this.ufo = ufo;
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            timer++;

            if (timer > 40) {
                timer = 0;
                double dy = (ufo.getRandom().nextDouble() * 0.5) - 0.25;
                ufo.setDeltaMovement(ufo.getDeltaMovement().add(0, dy, 0));
            }
        }
    }

}
