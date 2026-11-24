package cc.thonly.reverie_dreams.entity.misc;

import cc.thonly.reverie_dreams.data.danmaku.trajectory.PlayerTrajectoryFlight;
import cc.thonly.reverie_dreams.api.entity.type.FriendlyFaction;
import cc.thonly.reverie_dreams.util.GrazeHandler;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.DanmakuMotion;
import cc.thonly.reverie_dreams.data.danmaku.stg.runtime.DanmakuPatternRuntime;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.LegacySpellcardRuntime;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.DanmakuMotionState;
import cc.thonly.reverie_dreams.item.weapon.LunarBulletDamageSource;
import cc.thonly.reverie_dreams.item.weapon.LunarBulletVisuals;
import cc.thonly.reverie_dreams.mixin.accessor.ProjectileAccessor;
import cc.thonly.reverie_dreams.mixin.accessor.ThrowableProjectileAccessor;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.RDDamageTypes;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.util.PlatformContext;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"resource", "unused", "LombokGetterMayBeUsed"})
public abstract class BaseDanmakuEntity extends ThrowableItemProjectile {
    public static final EntityDataAccessor<DanmakuProperties> DANMAKU_PROPERTIES = SynchedEntityData.defineId(BaseDanmakuEntity.class, DanmakuProperties.SERIALIZER);
    public static final EntityDataAccessor<Float> ROLL = SynchedEntityData.defineId(BaseDanmakuEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<DanmakuMotion> DANMAKU_MOTION = SynchedEntityData.defineId(BaseDanmakuEntity.class, DanmakuMotion.SERIALIZER);
    /**
     * 新引擎子弹离出膛点的最远存活距离（格）。超出后直接销毁，避免子弹飞出战场后
     * 继续占用实体/tick 资源（旧路径没有 origin，不受此限制）。
     */
    public static final double MAX_DISTANCE_FROM_ORIGIN = 96.0D;
    private static final double MAX_DISTANCE_FROM_ORIGIN_SQR = MAX_DISTANCE_FROM_ORIGIN * MAX_DISTANCE_FROM_ORIGIN;
    private DanmakuMotionState motionState = new DanmakuMotionState();
    private float _initXRot = 0f;
    private float _initYRot = 0f;
    private float _initZRot = 0f;
    private float rollO = 0f;
    private int duration = 200;
    @Setter
    @Getter
    private int ejections = 0;
    private boolean playerTrajectoryShot;
    private boolean trajectoryInterceptable;
    @Nullable
    private PlayerTrajectoryFlight playerTrajectoryFlight;
    @Nullable
    @Setter
    protected HitCallback hitCallback = null;
    private boolean grazed = false;

    public BaseDanmakuEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @SuppressWarnings("DataFlowIssue")
    @Deprecated
    public BaseDanmakuEntity(Level level, LivingEntity owner) {
        super(null, owner, level, ItemStack.EMPTY);
    }

    public BaseDanmakuEntity(EntityType<? extends ThrowableItemProjectile> entityType, @NotNull LivingEntity owner, Level level, ItemStack item) {
        super(entityType, owner, level, item.copy());
        this.getEntityData().set(DANMAKU_PROPERTIES, this.getItemStack().getOrDefault(RDDataComponentTypes.DANMAKU_PROPERTIES.value(), DanmakuProperties.ofDefault()));
    }

    public BaseDanmakuEntity(EntityType<? extends ThrowableItemProjectile> entityType, double x, double y, double z, Level level, ItemStack item) {
        super(entityType, x, y, z, level, item.copy());
        this.getEntityData().set(DANMAKU_PROPERTIES, this.getItemStack().getOrDefault(RDDataComponentTypes.DANMAKU_PROPERTIES.value(), DanmakuProperties.ofDefault()));
    }

    public BaseDanmakuEntity(EntityType<? extends ThrowableItemProjectile> entityType, @NotNull LivingEntity livingEntity, double x, double y, double z, Level level, ItemStack item) {
        super(entityType, x, y, z, level, item.copy());
        this.setOwner(livingEntity);
        this.getEntityData().set(DANMAKU_PROPERTIES, this.getItemStack().getOrDefault(RDDataComponentTypes.DANMAKU_PROPERTIES.value(), DanmakuProperties.ofDefault()));
    }

    @Override
    public void shootFromRotation(Entity shooter, float x, float y, float z, float velocity, float inaccuracy) {
        super.shootFromRotation(shooter, x, y, z, velocity, inaccuracy);
        this._initXRot = x;
        this._initYRot = y;
        this._initZRot = z;
        if (LunarBulletVisuals.isBullet(this.getItemStack())) {
            // Use the final velocity, including spread and inherited shooter movement.
            this.updateRotation();
            this.xRotO = this.getXRot();
            this.yRotO = this.getYRot();
        }
    }

    @Override
    protected void updateRotation() {
        if (!LunarBulletVisuals.isBullet(this.getItemStack())) {
            super.updateRotation();
            return;
        }
        Vec3 velocity = this.getDeltaMovement();
        if (velocity.lengthSqr() < 1.0E-8D) return;

        // The +Z bullet models and fixed ItemDisplay use look angles, whose signs
        // are opposite to Projectile's yaw and pitch. Retain yaw for vertical shots.
        double horizontalSpeed = velocity.horizontalDistance();
        if (horizontalSpeed > 1.0E-8D) {
            this.setYRot((float) Math.toDegrees(Mth.atan2(-velocity.x, velocity.z)));
        }
        this.setXRot((float) Math.toDegrees(Mth.atan2(-velocity.y, horizontalSpeed)));
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        super.shoot(x, y, z, velocity, inaccuracy);
        this._initXRot = (float) x;
        this._initYRot = (float) y;
        this._initZRot = (float) z;
    }

    public void shootFromRotation(float x, float y, float z, float velocity, float inaccuracy) {
        float f = -Mth.sin(y * (float) (Math.PI / 180.0)) * Mth.cos(x * (float) (Math.PI / 180.0));
        float f1 = -Mth.sin((x + z) * (float) (Math.PI / 180.0));
        float f2 = Mth.cos(y * (float) (Math.PI / 180.0)) * Mth.cos(x * (float) (Math.PI / 180.0));
        this.shoot(f, f1, f2, velocity, inaccuracy);
        this._initXRot = x;
        this._initYRot = y;
        this._initZRot = z;
    }

    @Override
    public void tick() {
        if ((this.spellRuntime != null && this.spellRuntime.isCasterDefeated())
                || (this.legacySpell != null && this.legacySpell.isCasterDefeated())) {
            this.discard();
            return;
        }
        this.rollO = this.getRoll();
        if (this.playerTrajectoryFlight != null && this.level() instanceof ServerLevel level) {
            this.playerTrajectoryFlight.beforeMotion(this, level);
            if (this.isRemoved()) {
                return;
            }
        }
        this.applyDanmakuMotion();
        if (this.isRemoved()) {
            return;
        }
        if (this.playerTrajectoryFlight != null && this.level() instanceof ServerLevel level) {
            this.playerTrajectoryFlight.beforeCollision(this, level, this::canHitEntity);
            if (this.isRemoved()) {
                return;
            }
        }
        if (!PlatformContext.hasPolymer()) {
            super.tick();
        }
        this.b$polymer_tick();
        if (!this.level().isClientSide() && !this.isRemoved() && (this.tickCount & 1) == 0) {
            GrazeHandler.tryGraze(this);
        }
        if (!this.level().isClientSide()) {
            float spin = this.getDanmakuMotion().spin();
            if (spin != 0f) {
                this.entityData.set(ROLL, (float) ((this.entityData.get(ROLL) + Math.toRadians(spin)) % Mth.TWO_PI));
            } else if (!this.onGround()) {
                this.entityData.set(ROLL, (float) (this.entityData.get(ROLL) - Mth.DEG_TO_RAD * this.getDeltaMovement().lengthSqr() * 15) % Mth.TWO_PI);
            }
            if (this.tickCount >= this.getDuration()) {
                this.discardBullet();
            } else if (this.getOwner() instanceof LivingEntity livingEntity && livingEntity.deathTime > 19) {
                this.discardBullet();
            } else if (this.isOutsideMaxRadius()) {
                this.discardBullet();
            }
        }
        frozenParticles(this, this.level());
    }

    public void setPlayerTrajectoryFlight(@Nullable PlayerTrajectoryFlight flight) {
        this.playerTrajectoryShot = true;
        this.playerTrajectoryFlight = flight;
    }

    public void setTrajectoryInterceptable(boolean interceptable) {
        this.trajectoryInterceptable = interceptable;
    }

    public boolean isTrajectoryInterceptable() {
        return this.trajectoryInterceptable && this.getClass() == DanmakuEntity.class;
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target)
                && (this.playerTrajectoryFlight == null || this.playerTrajectoryFlight.canHit(target, this.getOwner()));
    }

    private boolean isOutsideMaxRadius() {
        Vec3 origin = this.getDanmakuMotion().origin();
        return origin.lengthSqr() > 1.0E-6D
                && this.position().distanceToSqr(origin) > MAX_DISTANCE_FROM_ORIGIN_SQR;
    }

    /**
     * STG 引擎：用 {@link DanmakuMotion} 驱动运动，取代原版投掷物物理惯性。
     */
    private void applyDanmakuMotion() {
        this.motionState.tick(this, this.getDanmakuMotion());
    }

    public void b$polymer_tick() {
        if (!PlatformContext.hasPolymer()) {
            return;
        }
        ProjectileAccessor projectileAccessor = (ProjectileAccessor) this;
        ThrowableProjectileAccessor throwableProjectileAccessor = (ThrowableProjectileAccessor) this;
        throwableProjectileAccessor.invokeHandleFirstTickBubbleColumn();
        this.applyGravity();
        throwableProjectileAccessor.invokeApplyInertia();
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        Vec3 vec3;
        if (hitresult.getType() != HitResult.Type.MISS) {
            vec3 = hitresult.getLocation();
        } else {
            vec3 = this.position().add(this.getDeltaMovement());
        }

        this.setPos(vec3);
        if (LunarBulletVisuals.isBullet(this.getItemStack())) {
            this.updateRotation();
        } else {
            this.setXRot(this._initXRot);
            this.setYRot(this._initYRot);
        }
        this.applyEffectsFromBlocks();
        if (!projectileAccessor.reverie_dreams$getHasBeenShot()) {
            this.gameEvent(GameEvent.PROJECTILE_SHOOT, this.getOwner());
            projectileAccessor.reverie_dreams$setHasBeenShot(true);
        }

        this.checkLeftOwner();
        this.baseTick();
        projectileAccessor.reverie_dreams$setLeftOwnerChecked(false);
        if (hitresult.getType() != HitResult.Type.MISS && this.isAlive()) {
            this.hitTargetOrDeflectSelf(hitresult);
        }
    }

    @Override
    public void onHitEntity(EntityHitResult result) {
        Level level = level();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        Entity owner = this.getOwner();
        if (entity == owner) {
            return;
        }
        if (this.playerTrajectoryShot) {
            var properties = this.getDanmakuProperties();
            boolean damaged = (owner == null || !owner.isAlliedTo(entity))
                    && (!(owner instanceof cc.thonly.reverie_dreams.entity.ForestYoukai youkai)
                    || !(entity instanceof LivingEntity living) || youkai.canAttack(living))
                    && entity.hurtServer(serverLevel,
                    RDDamageTypes.create(this.registryAccess(), properties.damageType(), this, owner), properties.damage());
            if (this.playerTrajectoryFlight == null || !this.playerTrajectoryFlight.continueAfterHit(this, entity, damaged)) {
                this.discard();
            }
            return;
        }
        float damage = this.applyDamage(entity, owner);
        if (this.hitCallback != null && entity instanceof LivingEntity livingEntity) {
            this.hitCallback.handle(this, livingEntity, damage);
        }
        if (!this.getItemStack().is(DanmakuTypes.LUNATIC_MOON.getItemHolder().asItem())) {
            kill(serverLevel);
        }
    }

    @Override
    public void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        Level level = level();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.playerTrajectoryShot) {
            if (this.playerTrajectoryFlight == null || !this.playerTrajectoryFlight.hitBlock(this, serverLevel, result)) {
                this.discard();
            }
            return;
        }

        if (this.getItemStack().getItem() == DanmakuTypes.NOTE.getItemHolder().asItem()) {

            level.playSound(null, this.getOnPos(),
                    SoundEvents.NOTE_BLOCK_BASS.value(),
                    SoundSource.BLOCKS, 1.0F, 1.0F);

            this.ejections++;

            Vec3 motion = this.getDeltaMovement();
            Direction face = result.getDirection();

            double x = motion.x;
            double y = motion.y;
            double z = motion.z;

            switch (face) {
                case UP, DOWN -> y = -y;
                case NORTH, SOUTH -> z = -z;
                case EAST, WEST -> x = -x;
            }

            double damping = 0.8;
            Vec3 newMotion = new Vec3(x, y, z).scale(damping);

            // The STG motion model keeps its own direction and speed, so simply
            // replacing delta movement is overwritten on the next tick. Recreate
            // the note at the collision point with a fresh reflected motion.
            if (this.ejections <= 5) {
                Vec3 spawnPos = result.getLocation()
                        .add(Vec3.atLowerCornerOf(face.getUnitVec3i()).scale(0.35));
                double horizontal = newMotion.horizontalDistance();
                float yaw = (float) (Math.atan2(newMotion.x, newMotion.z) * 180F / Math.PI);
                float pitch = (float) (Math.atan2(newMotion.y, horizontal) * 180F / Math.PI);

                DanmakuEntity reflected = new DanmakuEntity(
                        spawnPos.x, spawnPos.y, spawnPos.z, serverLevel,
                        this.getItemStack().copy()
                );
                reflected.setOwner(this.getOwner());
                reflected.setDanmakuProperties(this.getDanmakuProperties().copy());
                reflected.setDanmakuMotion(DanmakuMotion.straight(newMotion));
                reflected.setDeltaMovement(newMotion);
                reflected.setYRot(yaw);
                reflected.setXRot(pitch);
                reflected.setDuration(this.getDuration());
                reflected.setEjections(this.ejections);
                reflected.setHitCallback(this.hitCallback);
                serverLevel.addFreshEntity(reflected);
            }

            this.discard();
        } else {
            kill(serverLevel);
        }
        BlockState block = level.getBlockState(result.getBlockPos());
        double particleSpeed = this.getDeltaMovement().length();
        if (LunarBulletVisuals.isBullet(this.getItemStack())) {
            // Faster gun rounds should not multiply the number of impact particle packets.
            particleSpeed = Math.min(particleSpeed, 4.0D);
        }
        blockHitParticles(this.position(), block, this.level(), this.getDanmakuProperties().damage() * particleSpeed);
        frozenParticles(this, level);
    }

    protected void blockHitParticles(Vec3 pos, BlockState blockState, Level worldTemp, double damage) {
        if (worldTemp instanceof ServerLevel world) {
            int particleCount = (int) damage * 4;
            double radius = 1;
            double heightOffset = 1;


            for (int i = 0; i < particleCount; i++) {
                double angle = (2 * Math.PI / particleCount) * i;
                double xOffset = radius * Math.cos(angle);
                double zOffset = radius * Math.sin(angle);

                BlockParticleOption blockStateParticleEffect = new BlockParticleOption(ParticleTypes.BLOCK, blockState);
                world.sendParticles(
                        blockStateParticleEffect,
                        pos.x,
                        pos.y,
                        pos.z,
                        1,
                        xOffset,
                        (heightOffset / particleCount) * i,
                        zOffset,
                        0.25
                );
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        HitResult.Type type = result.getType();
        if (type == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) result).getEntity();
            Entity owner = this.getOwner();
            if (entity == owner) {
                return;
            }
            if (FriendlyFaction.isFriendLy(entity, owner)
                    && !FriendlyFaction.isDirectTarget(owner, entity)) {
                return;
            }
        }
        super.onHit(result);
    }

    @SuppressWarnings({"deprecation", "PointlessBooleanExpression"})
    public float applyDamage(Entity target, Entity owner) {
        if (owner instanceof cc.thonly.reverie_dreams.entity.ForestYoukai youkai
                && target instanceof LivingEntity living && !youkai.canAttack(living)) return 0;
        DanmakuProperties danmakuProperties = this.getDanmakuProperties();
        ResourceKey<DamageType> resourceKey = danmakuProperties.damageType();
        float damage = danmakuProperties.damage();
        var source = LunarBulletVisuals.isBullet(this.getItemStack())
                ? new LunarBulletDamageSource(this.registryAccess(), resourceKey, owner)
                : RDDamageTypes.create(this.registryAccess(), resourceKey, owner);
        target.hurt(source, damage);
        target.invulnerableTime = 2;
        if (false && target.canFreeze()) {
            this.applyFreeze(target);
        }
        return damage;
    }

    protected void applyFreeze(Entity target) {
        DanmakuProperties danmakuProperties = this.getDanmakuProperties();
        int frozenTicks = target.getTicksFrozen();
        target.setTicksFrozen((int) Math.min(target.getTicksRequiredToFreeze() * 4, frozenTicks + 10 * danmakuProperties.damage()));
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    private static void frozenParticles(BaseDanmakuEntity entity, Level level) {
        if (true) {
            return;
        }
        if (entity.tickCount % 3 != 0) return;
        float offset = 2.0f;
        double xOffset = entity.getDeltaMovement().x * offset;
        double yOffset = entity.getDeltaMovement().y * offset;
        double zOffset = entity.getDeltaMovement().z * offset;
        level.addParticle(ParticleTypes.SNOWFLAKE, entity.getX() - xOffset, entity.getY() + entity.getBbHeight() / 2 - yOffset, entity.getZ() - zOffset, 0, 0, 0);
    }

    private void discardBullet() {
        this.discard();
        this.despawnParticle((ServerLevel) level());
    }

    private void despawnParticle(ServerLevel serverWorld) {
        serverWorld.sendParticles(ParticleTypes.SNOWFLAKE,
                position().x(), position().y() + getBbHeight() / 2, position().z(),
                1, 0, 0, 0, 0);
    }

    public void cancelParticle(ServerLevel serverWorld) {
        ItemParticleOption ispe = new ItemParticleOption(ParticleTypes.ITEM, RDItems.BOMB_FRAGMENT.asItem());
        serverWorld.sendParticles(ispe,
                position().x(), position().y() + getBbHeight() / 2, position().z(),
                1, 0, 0, 0, 0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ROLL, 0f);
        builder.define(DANMAKU_PROPERTIES, DanmakuProperties.ofDefault());
        builder.define(DANMAKU_MOTION, DanmakuMotion.STRAIGHT);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        view.storeNullable("DanmakuProperties", DanmakuProperties.CODEC, DanmakuProperties.ofDefault());
        view.storeNullable("DanmakuMotion", DanmakuMotion.CODEC, this.getDanmakuMotion());
        view.storeNullable("DanmakuDuration", Codec.INT, this.duration);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        SynchedEntityData entityData = this.getEntityData();
        view.read("DanmakuProperties", DanmakuProperties.CODEC).ifPresent(value -> entityData.set(DANMAKU_PROPERTIES, value));
        view.read("DanmakuMotion", DanmakuMotion.CODEC).ifPresent(value -> entityData.set(DANMAKU_MOTION, value));
        view.read("DanmakuDuration", Codec.INT).ifPresent(value -> this.duration = value);
    }

    public DanmakuProperties getDanmakuProperties() {
        return this.getEntityData().get(DANMAKU_PROPERTIES);
    }

    /** Physical size and sweep radius share the explicit type hitbox scale. */
    public float getCollisionScale() {
        if (this.entityData == null) {
            return 1.0F;
        }
        return this.getDanmakuProperties().collisionScale();
    }

    public float getExtraHitMargin() {
        return this.getType().getWidth() * (this.getCollisionScale() - 1.0F) * 0.5F;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(this.getCollisionScale());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (DANMAKU_PROPERTIES.equals(accessor)) {
            this.refreshDimensions();
        }
        // 客户端收到改写后的 motion（REDIRECT 等）时重置步进状态，
        // 与服务端 resetMotionState() 对齐，恢复双端确定性推进。
        if (DANMAKU_MOTION.equals(accessor)) {
            this.resetMotionState();
        }
    }

    public DanmakuMotion getDanmakuMotion() {
        return this.getEntityData().get(DANMAKU_MOTION);
    }

    public void setDanmakuMotion(DanmakuMotion motion) {
        this.getEntityData().set(DANMAKU_MOTION, motion);
    }

    /**
     * 服务端图案操作：冻结子弹 ticks（位移 0、不累积速度）；resetMotionState 时清零。
     */
    public void freezeMotion(int ticks) {
        if (ticks > 0 && !this.level().isClientSide()) {
            this.motionState.freezeUntilTick(this.motionState.currentAge() + ticks);
        }
    }

    /** 当前推进方向（服务端运动状态，供 REDIRECT/SPEED_SCALE 等图案操作读取）。 */
    public Vec3 currentDirection() {
        return this.motionState.currentDirection();
    }

    /** 当前速度标量（格/tick，服务端运动状态，供图案操作读取）。 */
    public double currentSpeed() {
        return this.motionState.currentSpeed();
    }

    /**
     * 重置 {@link DanmakuMotionState}；改写 motion 后调用，让双端从新初值重新确定性推进。
     */
    public void resetMotionState() {
        this.motionState.reset();
    }

    /**
     * 把子弹重定向为沿 newVelocity 的直线运动，保留原出膛点（origin）。
     * 服务端调用（REDIRECT / SPEED_SCALE 等图案操作）。
     */
    public void redirect(Vec3 newVelocity) {
        Vec3 origin = this.getDanmakuMotion().origin();
        this.setDanmakuMotion(DanmakuMotion.straight(newVelocity).withOrigin(origin));
        this.resetMotionState();
    }

    /**
     * 服务端把子弹变形为新弹幕类型：重建物品栈与属性（伤害/伤害类型用新类型默认，
     * 速度保持当前实际速度，scale 为类型默认 × multiplier）。客户端靠既有物品/属性同步生效。
     */
    public void transformBullet(Identifier typeId, int color, float scale) {
        DanmakuType type = typeId == null ? null : BuiltInRegistryProviders.DANMAKU_TYPE.getValue(typeId);
        if (type == null) {
            return;
        }
        ItemStack stack = type.getItemHolder().asItem().getDefaultInstance();
        if (color >= 0) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
        }
        this.setItem(stack);
        float multiplier = scale > 0f ? scale : 1f;
        float currentSpeed = this.getDanmakuProperties().actualSpeed();
        DanmakuProperties properties = type.createDanmakuProperties()
                .withSpeed(currentSpeed)
                .withScale(type.getScale() * multiplier)
                .withHitboxScale(type.effectiveHitboxScale() * multiplier);
        this.setDanmakuProperties(properties);
    }

    public static float getSoundPitch(RandomSource random) {
        return 1.0f + (random.nextFloat() - 0.5f) * 0.1f;
    }

    public void setDanmakuProperties(DanmakuProperties properties) {
        this.getEntityData().set(DANMAKU_PROPERTIES, properties);
    }

    public float getRoll() {
        return this.getEntityData().get(ROLL);
    }

    public float getRollO() {
        return this.rollO;
    }

    /**
     * Server-side once-per-projectile graze marker, kept off the network
     * because it only gates local sound feedback.
     */
    public boolean hasGrazed() {
        return this.grazed;
    }

    public void markGrazed() {
        this.grazed = true;
    }

    public float get_initXRot() {
        return this._initXRot;
    }

    public float get_initYRot() {
        return this._initYRot;
    }

    public float get_initZRot() {
        return this._initZRot;
    }

    public ItemStack getItemStack() {
        return this.getItem();
    }

    @Override
    public boolean skipAttackInteraction(Entity attacker) {
        return this.getOwner() == attacker;
    }

    @Override
    protected Item getDefaultItem() {
        return Items.AIR;
    }

    protected int getDuration() {
        return this.duration;
    }

    @Nullable
    private DanmakuPatternRuntime spellRuntime;
    @Nullable
    private LegacySpellcardRuntime legacySpell;

    public void setSpellRuntime(DanmakuPatternRuntime runtime) {
        this.spellRuntime = runtime;
    }

    @Nullable
    public DanmakuPatternRuntime getSpellRuntime() {
        return this.spellRuntime;
    }

    public void setLegacySpell(LegacySpellcardRuntime renderer) {
        this.legacySpell = renderer;
    }

    @Override
    public void onRemoval(RemovalReason reason) {
        super.onRemoval(reason);
        if (this.spellRuntime != null) {
            this.spellRuntime.untrack(this);
            this.spellRuntime = null;
        }
        if (this.legacySpell != null) {
            this.legacySpell.untrack(this);
            this.legacySpell = null;
        }
    }

    @Override
    public boolean shouldBeSaved() {
        return !this.playerTrajectoryShot && this.spellRuntime == null && this.legacySpell == null && super.shouldBeSaved();
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    @Override
    public float getPickRadius() {
        return (float) getBoundingBox().getXsize() * 1.1f;
    }

    @Override
    protected double getDefaultGravity() {
        return 0;
    }

    @FunctionalInterface
    public interface HitCallback {
        void handle(BaseDanmakuEntity danmakuEntity, LivingEntity target, double damage);
    }
}
