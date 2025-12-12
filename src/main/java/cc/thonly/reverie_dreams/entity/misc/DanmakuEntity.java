package cc.thonly.reverie_dreams.entity.misc;

import cc.thonly.minecraft.util.TagValueFunction;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.entity.interfaces.FriendlyFaction;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Setter
@Getter
@ToString
public class DanmakuEntity extends AbstractArrow {
    public static final EntityDataAccessor<Float> ROLL = SynchedEntityData.defineId(DanmakuEntity.class, EntityDataSerializers.FLOAT);
    private static final Map<String, Long> PARTICLE_COOLDOWN = new HashMap<>();
    private static final long PARTICLE_INTERVAL_MS = 50;
    private static final double GRAZE_RADIUS = 0.5;
    private static final Map<Integer, Long> GRAZE_CACHE = new HashMap<>(); // 记录 entityId -> lastTick
    public static final int MAX_FLIGHT_TICK = 20 * 20;
    protected Item danmakuItem;
    protected ItemStack itemStack = Items.SNOWBALL.getDefaultInstance();
    protected OnHitFactory onHitEffect = (livingEntity, damage) -> {

    };
    public DanmakuProperties properties = DanmakuProperties.ofDefault();
    private float originPitch;
    private float originYaw;

    public int flyAge = 0;
    protected int fluidAge = 0;
    protected int fightTick = 0;
    protected int particleTick = 0;
    private int remainingBounces = 16;

    public DanmakuEntity(@Nullable Entity livingEntity,
                         ServerLevel world,
                         Double x, Double y, Double z,
                         ItemStack stack,
                         DanmakuProperties properties,
                         Float pitch, Float yaw,
                         Float divergence, Float offsetDist
    ) {
        this(livingEntity, world, x, y, z, stack, properties, pitch, yaw, divergence, offsetDist, true);
    }

    public DanmakuEntity(@Nullable Entity livingEntity,
                         ServerLevel world,
                         Double x, Double y, Double z,
                         ItemStack stack,
                         DanmakuProperties properties,
                         Float pitch, Float yaw,
                         Float divergence, Float offsetDist,
                         boolean entityDelta
    ) {
        super(RDEntityTypes.DANMAKU,
                x,
                y + (livingEntity != null ? livingEntity.getEyeHeight() : 0),
                z,
                world,
                stack.copy(),
                stack.copy()
        );
        this.originPitch = pitch;
        this.originYaw = yaw;
        this.properties = properties;
        double offsetX = -Math.sin(Math.toRadians(yaw)) * offsetDist;
        double offsetZ = Math.cos(Math.toRadians(yaw)) * offsetDist;

        double newX = x + offsetX;
        double newY = y + (livingEntity != null ? livingEntity.getEyeHeight() : 0) + (entityDelta ? 0 : -0.5);
        double newZ = z + offsetZ;
        this.setPosRaw(newX, newY, newZ);

        this.setOwner(livingEntity);
        if (livingEntity != null && entityDelta) {
            this.shootFromRotation(livingEntity, pitch, yaw, 0.0F, this.properties.getSpeed(), divergence);
            this.setDeltaMovement(this.getDeltaMovement().subtract(livingEntity.getDeltaMovement()));
        } else {
            this.shootFromRotation(pitch, yaw);
        }

        this.setYRot(yaw);
        this.setXRot(pitch);
        this.pickup = Pickup.CREATIVE_ONLY;
        this.setBaseDamage(properties.getDamage() * 1.5);
        this.setCustomPierceLevel((byte) 1);
        this.setItemStack(stack.copy());
        this.setDanmakuItem(stack.getItem());
        this.setNoGravity(true);
    }

    public DanmakuEntity(EntityType<DanmakuEntity> danmakuEntityEntityType, Level world) {
        super(danmakuEntityEntityType, world);
        this.setDanmakuItem(null);
        this.originPitch = 0;
        this.originYaw = 0;
    }

    void shootFromRotation(float pitch, float yaw) {
        this.setYRot(yaw);
        this.setXRot(pitch);

        float pitchRad = (float) Math.toRadians(pitch);
        float yawRad = (float) Math.toRadians(yaw);

        double dx = -Math.sin(yawRad) * Math.cos(pitchRad);
        double dy = -Math.sin(pitchRad);
        double dz = Math.cos(yawRad) * Math.cos(pitchRad);

        double speed = 0.5 * (this.properties != null ? this.properties.getSpeed() : 0.6d);
        Vec3 motion = new Vec3(dx * speed, dy * speed, dz * speed);
        this.setDeltaMovement(motion);

        this.setNoGravity(true);
    }


    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ROLL, 0f);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        TagValueFunction.ofOutput(compoundTag, this.registryAccess(), view-> {
            if (!this.itemStack.isEmpty()) {
                view.store("Item", ItemStackWrapper.FLEXIBLE_ITEMSTACK_CODEC, this.itemStack.copy());
            }
            view.store("Properties", DanmakuProperties.CODEC, this.properties);
            view.putInt("FlyAge", this.flyAge);
            view.putInt("RemainingBounces", this.remainingBounces);
            view.store("VelocityVector", Vec3.CODEC, this.getDeltaMovement());
        });
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        TagValueFunction.ofInput(compoundTag, this.registryAccess(), view-> {
            this.itemStack = view.read("Item", ItemStackWrapper.FLEXIBLE_ITEMSTACK_CODEC).orElse(ItemStack.EMPTY);
            this.properties = view.read("Properties", DanmakuProperties.CODEC).orElse(DanmakuProperties.ofDefault());
            this.flyAge = view.getIntOr("FlyAge", 0);
            this.remainingBounces = view.getIntOr("RemainingBounces", 0);
            Optional<Vec3> velocityVector = view.read("VelocityVector", Vec3.CODEC);
            velocityVector.ifPresent(this::setDeltaMovement);
        });
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isInGround()) {
            this.entityData.set(ROLL, (float) (this.entityData.get(ROLL) - Mth.DEG_TO_RAD * this.getDeltaMovement().lengthSqr() * 15) % Mth.TWO_PI);
        }

        this.setXRot(this.originPitch);
        this.setYRot(this.originYaw);

        this.particleTick();
        this.waterTick();
        this.fightTick();
//        this.grazeTick();
    }

    private void grazeTick() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        if (serverLevel.getGameTime() % 2 != 0) return;
        List<Entity> nearby = serverLevel.getEntitiesOfClass(
                Entity.class,
                this.getBoundingBox().inflate(GRAZE_RADIUS)
        );

        for (Entity entity : nearby) {
            if (entity.isSpectator() || entity.isRemoved()) {
                continue;
            }
            if (entity == this.getOwner()) {
                continue;
            }
            if (this.getOwner() != null && this.getOwner().getControlledVehicle() == entity) {
                continue;
            }
            if (entity instanceof AbstractArrow) {
                continue;
            }
            if (!this.canDamage(entity, this.getOwner())) {
                continue;
            }
            if (this.distanceTo(entity) <= GRAZE_RADIUS) {
                continue;
            }

            int id = entity.getId();
            long now = serverLevel.getGameTime();
            long last = GRAZE_CACHE.getOrDefault(id, -100L);
            if (now - last < 10) continue;

            GRAZE_CACHE.put(id, now);
            SoundEventInit.playSound(entity, SoundEventInit.GRAZE, 1.0f, 1.0f);
        }
    }


    private void particleTick() {
        this.particleTick++;
        if (this.particleTick > 2) {
            Level world = this.level();
            if (!world.isClientSide() && world instanceof ServerLevel serverWorld) {
                long now = System.currentTimeMillis();
                String key = this.getType().toString(); // 或 "danmaku"
                Long last = PARTICLE_COOLDOWN.getOrDefault(key, 0L);

                if (now - last >= PARTICLE_INTERVAL_MS) {
                    serverWorld.sendParticles(
                            ParticleTypes.SNOWFLAKE,
                            this.position().x,
                            this.position().y,
                            this.position().z,
                            1,
                            0,
                            0,
                            0,
                            0.1
                    );
                    PARTICLE_COOLDOWN.put(key, now);
                }
            }
            this.particleTick = 0;
        }
    }

    private void waterTick() {
        if (this.isInWater()) {
            this.fluidAge++;
            if (this.fluidAge > 80) {
                this.discard();
            }
        }
    }

    private void fightTick() {
        this.fightTick++;
        if (this.fightTick > MAX_FLIGHT_TICK) {
            this.discard();
        }
    }

    @Override
    public void onHitBlock(BlockHitResult blockHitResult) {
        if (this.itemStack.getItem() == DanmakuTypes.NOTE.getItem()) {
            playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 1.0F, 1.0F);
            if (this.remainingBounces <= 0) {
                this.discard();
                return;
            }

            var dir = blockHitResult.getDirection();

            Vec3 normal = new Vec3(dir.getStepX(), dir.getStepY(), dir.getStepZ()).normalize();
            Vec3 velocity = this.getDeltaMovement();

            double dot = velocity.dot(normal);
            Vec3 reflected = velocity.subtract(normal.scale(2.0 * dot));

            float damping = 0.95f;
            reflected = reflected.scale(damping);

            if (reflected.lengthSqr() < 1e-4) {
                this.discard();
                return;
            }

            this.setDeltaMovement(reflected);
            this.setPos(this.position().add(normal.scale(0.05)));

            float yaw = this.getYRot();
            float pitch = this.getXRot();

            switch (dir) {
                case EAST, WEST, NORTH, SOUTH -> {
                    yaw = 180f - yaw;
                }
                case UP, DOWN -> {
                    pitch = -pitch;
                }
            }

            yaw = (yaw % 360 + 360) % 360;
            pitch = Mth.clamp(pitch, -90f, 90f);
            this.setYRot(yaw);
            this.setXRot(pitch);
            this.setYBodyRot(yaw);
            this.setOriginPitch(pitch);
            this.setOriginYaw(yaw);

            this.remainingBounces--;
            return;
        }

        this.setPos(blockHitResult.getLocation());
        if (blockHitResult.getType() == HitResult.Type.BLOCK) {
            BlockState block = this.level().getBlockState(blockHitResult.getBlockPos());
            blockHitParticles(this.position(), block, this.level(), this.properties.getDamage() * this.getDeltaMovement().length());
            SoundEvent soundEvent = block.getSoundType().getHitSound();
            setSilent(false);
            playSound(soundEvent, 0.2F, 1.0F);
            setSilent(true);
        }

        this.setSharedFlagOnFire(true);
        super.onHitBlock(blockHitResult);
        this.setSharedFlagOnFire(false);
        this.discard();
    }

    public boolean canDamage(Entity entity, Entity owner) {
        if (entity instanceof BypassHitEntity) {
            return false;
        }
        if (owner == entity) {
            return false;
        }
        if (owner instanceof FriendlyFaction ownerFaction &&
                entity instanceof FriendlyFaction entityFaction) {
            return !ownerFaction.getFactionId().equals(entityFaction.getFactionId());
        }
        return true;
    }

    @Override
    public void onHitEntity(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        Entity owner = this.getOwner();
        if (!this.canDamage(entity, owner)) {
            return;
        }

        if (entity instanceof Player player) {
            if (player.isBlocking()) {
                boolean isInAttackRange = false;
                ItemStack activeItem = player.getUseItem();
                if (!activeItem.isEmpty()) {
                    BlocksAttacks blocksAttacksComponent = activeItem.get(DataComponents.BLOCKS_ATTACKS);
                    if (blocksAttacksComponent != null) {
                        List<BlocksAttacks.DamageReduction> damageReductions = blocksAttacksComponent.damageReductions();
                        for (BlocksAttacks.DamageReduction damageReduction : damageReductions) {
                            float blockingAngle = damageReduction.horizontalBlockingAngle();

                            // ① 使用 EyePos，避免高度误差
                            Vec3 toProjectile = this.position().subtract(player.getEyePosition()).normalize();

                            // ② 只取水平向量，忽略 Y
                            Vec3 playerLook = player.getViewVector(1.0F);
                            Vec3 look2D = new Vec3(playerLook.x, 0, playerLook.z).normalize();
                            Vec3 toProj2D = new Vec3(toProjectile.x, 0, toProjectile.z).normalize();

                            // ③ 点积求角度，clamp 防止 NaN
                            double dot = Mth.clamp(look2D.dot(toProj2D), -1.0, 1.0);
                            double angle = Math.toDegrees(Math.acos(dot));

                            // ④ 给一个小容错（比如 +5°）
                            if (angle <= (blockingAngle / 2.0F) + 5.0F) {
                                isInAttackRange = true;
                                break; // 找到一个满足条件的就可以退出循环了
                            }
                        }
                    }
                    if (isInAttackRange) {
                        activeItem.hurtWithoutBreaking(1, player);
                        this.discard(); // 拦截并移除投射物
                        return;
                    }
                }
            }

        }

        this.setPos(entityHitResult.getLocation());
        this.setSilent(false);
        this.setSilent(true);

        this.setBaseDamage(this.properties.getDamage());
        this.entityHitParticles(entityHitResult.getEntity(), this.properties.getDamage() * this.getDeltaMovement().length());

        this.hitDamage(entityHitResult, this.level());
        this.discard();
    }

    protected void hitDamage(EntityHitResult entityHitResult, Level world) {
        if (world instanceof ServerLevel serverWorld) {
            Entity target = entityHitResult.getEntity();
            Entity owner = this.getOwner();
            boolean bypassHurtTick = true;

            if (target instanceof EnderDragonPart part && part.parentMob instanceof EnderDragon dragon) {
                target = dragon;
                bypassHurtTick = false;
            }

            if (target instanceof LivingEntity livingTarget && this.getOwner() != entityHitResult.getEntity()) {
                DamageSource damageSource;

                if (owner instanceof LivingEntity attacker) {
                    damageSource = world.damageSources().mobProjectile(this, attacker);
                } else {
                    RegistryAccess registryAccess = this.registryAccess();
                    Holder.Reference<DamageType> damageType = registryAccess.lookupOrThrow(Registries.DAMAGE_TYPE)
                            .getOrThrow(this.properties.getDamageType());
                    damageSource = new DamageSource(damageType, livingTarget);
                }

                float damageAmount = this.properties.getDamage();
                livingTarget.hurtServer(serverWorld, damageSource, damageAmount);
                livingTarget.setInvulnerable(false);
                if (bypassHurtTick) {
                    livingTarget.lastHurt = 0;
                } else {
                    livingTarget.lastHurt = 5;
                }
                this.onHitEffect.damage(livingTarget, this.properties.getDamage());
            }
        }

        if (this.itemStack.getItem() == DanmakuTypes.NOTE.getItem()) {
            playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 1.0F, 1.0F);
        }
    }

    protected void entityHitParticles(Entity livingEntity, double damage) {
        if (livingEntity.level() instanceof ServerLevel world) {
            Vec3 pos = livingEntity.position();
            int particleCount = (int) damage * 4;
            double radius = livingEntity.getBbWidth() / 2 + 0.5;
            double heightOffset = livingEntity.getBbHeight();

            for (int i = 0; i < particleCount; i++) {
                double angle = (2 * Math.PI / particleCount) * i;
                double xOffset = radius * Math.cos(angle);
                double zOffset = radius * Math.sin(angle);

                ItemParticleOption itemStackParticleEffect = new ItemParticleOption(ParticleTypes.ITEM, this.getDefaultPickupItem());
                world.sendParticles(
                        itemStackParticleEffect,
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


    public void setCustomPierceLevel(byte level) {
        if (!tryInvokeMethod(AbstractArrow.class, "setPierceLevel", byte.class, level)) {
            tryInvokeMethod(AbstractArrow.class, "method_7451", byte.class, level);
        }
    }

    private boolean tryInvokeMethod(Class<?> targetClass, String methodName, Class<?> paramType, Object paramValue) {
        try {
            Method method = targetClass.getDeclaredMethod(methodName, paramType);
            method.setAccessible(true);
            method.invoke(this, paramValue);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        if (this.danmakuItem != null && !this.danmakuItem.getDefaultInstance().isEmpty()) {
            return this.danmakuItem.getDefaultInstance();
        } else {
            return new ItemStack(RDItems.ICON);
        }
    }

    public interface OnHitFactory {
        void damage(LivingEntity livingEntity, double damage);
    }

}
