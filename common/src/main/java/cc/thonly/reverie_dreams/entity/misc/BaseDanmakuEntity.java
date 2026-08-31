package cc.thonly.reverie_dreams.entity.misc;

import cc.thonly.reverie_dreams.api.entity.type.FriendlyFaction;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.mixin.accessor.ProjectileAccessor;
import cc.thonly.reverie_dreams.mixin.accessor.ThrowableProjectileAccessor;
import cc.thonly.reverie_dreams.registry.content.RDDamageTypes;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.util.PlatformContext;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
    private float _initXRot = 0f;
    private float _initYRot = 0f;
    private float _initZRot = 0f;
    @Setter
    @Getter
    private int ejections = 0;
    @Nullable
    @Setter
    protected HitCallback hitCallback = null;

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
        if (!PlatformContext.hasPolymer()) {
            super.tick();
        }
        this.b$polymer_tick();
        if (!this.level().isClientSide()) {
            if (!this.onGround()) {
                this.entityData.set(ROLL, (float) (this.entityData.get(ROLL) - Mth.DEG_TO_RAD * this.getDeltaMovement().lengthSqr() * 15) % Mth.TWO_PI);
            }
            if (this.tickCount >= this.getDuration()) {
                this.discardBullet();
            }
            if (this.getOwner() instanceof LivingEntity livingEntity && livingEntity.deathTime > 19) {
                this.discardBullet();
            }
        }
        frozenParticles(this, this.level());
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
        this.setXRot(this._initXRot);
        this.setYRot(this._initYRot);
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
        float damage = this.applyDamage(entity, owner);
        if (this.hitCallback != null && entity instanceof LivingEntity livingEntity) {
            this.hitCallback.handle(this, livingEntity, damage);
        }
        kill(serverLevel);
    }

    @Override
    public void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        Level level = level();
        if (!(level instanceof ServerLevel serverLevel)) {
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

            this.setDeltaMovement(newMotion);

            double horizontal = newMotion.horizontalDistance();
            this.setYRot((float) (Math.atan2(newMotion.x, newMotion.z) * 180F / Math.PI));
            this.setXRot((float) (Math.atan2(newMotion.y, horizontal) * 180F / Math.PI));

            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();

            Vec3 offset = Vec3.atLowerCornerOf(face.getUnitVec3i()).scale(0.05);
            this.setPos(this.getX() + offset.x, this.getY() + offset.y, this.getZ() + offset.z);

            if (this.ejections > 5) {
                kill(serverLevel);
            }
        } else {
            kill(serverLevel);
        }
        BlockState block = level.getBlockState(result.getBlockPos());
        blockHitParticles(this.position(), block, this.level(), this.getDanmakuProperties().damage() * this.getDeltaMovement().length());
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
            if (FriendlyFaction.isFriendLy(entity, owner)) {
                return;
            }
        }
        super.onHit(result);
    }

    @SuppressWarnings({"deprecation", "PointlessBooleanExpression"})
    public float applyDamage(Entity target, Entity owner) {
        DanmakuProperties danmakuProperties = this.getDanmakuProperties();
        ResourceKey<DamageType> resourceKey = danmakuProperties.damageType();
        float damage = danmakuProperties.damage();
        target.hurt(RDDamageTypes.create(this.registryAccess(), resourceKey, owner), damage);
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
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        view.storeNullable("DanmakuProperties", DanmakuProperties.CODEC, DanmakuProperties.ofDefault());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        SynchedEntityData entityData = this.getEntityData();
        view.read("DanmakuProperties", DanmakuProperties.CODEC).ifPresent(value -> entityData.set(DANMAKU_PROPERTIES, value));
    }

    public DanmakuProperties getDanmakuProperties() {
        return this.getEntityData().get(DANMAKU_PROPERTIES);
    }

    public static float getSoundPitch(RandomSource random) {
        return 1.0f + (random.nextFloat() - 0.5f) * 0.1f;
    }

    public void setDanmakuProperties(DanmakuProperties properties) {
        this.getEntityData().set(DANMAKU_PROPERTIES, properties);
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
        return 200;
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
