package cc.thonly.reverie_dreams.entity.misc;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.damage.DanmakuDamageType;
import cc.thonly.reverie_dreams.entity.FriendlyFaction;
import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlocksAttacksComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;


@Setter
@Getter
@ToString
public class DanmakuEntity extends PersistentProjectileEntity {
    public static final TrackedData<Float> ROLL = DataTracker.registerData(DanmakuEntity.class, TrackedDataHandlerRegistry.FLOAT);
    public static final int MAX_FLIGHT_TICK = 20 * 20;
    protected Item danmakuItem;
    protected ItemStack itemStack = Items.SNOWBALL.getDefaultStack();
    protected Float scale;
    protected Boolean tile = false;
    protected DanmakuDamageType danmakuDamageType;
    protected Float acceleration = 0f;
    protected OnHitFactory onHitEffect = (livingEntity, damage) -> {

    };
    private final float setupPitch;
    private final float setupYaw;
    private double damage;

    public int flyAge = 0;
    protected int fluidAge = 0;
    protected int fightTick = 0;
    protected int particleTick = 0;

    public DanmakuEntity(@Nullable Entity livingEntity, ServerWorld world, Double x, Double y, Double z, ItemStack stack, Float pitch, Float yaw, Float speed, Float acceleration, Float divergence, Float offsetDist) {
        super(ModEntities.DANMAKU_ENTITY_TYPE,
                x,
                y + (livingEntity != null ? livingEntity.getStandingEyeHeight() : 0),
                z,
                world,
                stack.copy(),
                stack.copy());
        this.setupPitch = pitch;
        this.setupYaw = yaw;
        double offsetX = -Math.sin(Math.toRadians(yaw)) * offsetDist;
        double offsetZ = Math.cos(Math.toRadians(yaw)) * offsetDist;

        double newX = x + offsetX;
        double newY = y + (livingEntity != null ? livingEntity.getStandingEyeHeight() : 0);
        double newZ = z + offsetZ;
        this.setPos(newX, newY, newZ);
        this.acceleration = acceleration;

        this.setOwner(livingEntity);
        if (livingEntity != null) {
            this.setVelocity(livingEntity, pitch, yaw, 0.0F, speed, divergence);
        } else {
            this.setVelocity(this, pitch, yaw, 0.0F, speed, divergence);
        }
        this.setYaw(yaw);
        this.setPitch(pitch);
        this.pickupType = PickupPermission.CREATIVE_ONLY;
        this.setDamage(stack.getOrDefault(ModDataComponentTypes.Danmaku.DAMAGE, 1.0f) * 1.5);
        this.setScale(stack.getOrDefault(ModDataComponentTypes.Danmaku.SCALE, 1.0f) * 0.65F);
        String type = stack.getOrDefault(ModDataComponentTypes.Danmaku.DAMAGE_TYPE, Touhou.id("generic").toString());
        this.setTile(stack.getOrDefault(ModDataComponentTypes.Danmaku.TILE, false));
        this.setDanmakuDamageType(RegistryManager.DANMAKU_DAMAGE_TYPE.get(Identifier.of(type)));
        this.setCustomPierceLevel((byte) 1);
        this.setItemStack(stack.copy());
        this.setDanmakuItem(stack.getItem());
        this.setNoGravity(true);
    }

    public DanmakuEntity(EntityType<DanmakuEntity> danmakuEntityEntityType, World world) {
        super(danmakuEntityEntityType, world);
        this.setDanmakuItem(null);
        this.scale = 1f;
        this.setupPitch = 0;
        this.setupYaw = 0;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(ROLL, 0f);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        if (!this.itemStack.isEmpty()) {
            view.put("Item", ItemStackWrapper.FLEXIBLE_ITEMSTACK_CODEC, this.itemStack.copy());
        }
        view.putBoolean("IsTile", this.tile);
        Identifier danmakuDamageTypeId = RegistryManager.DANMAKU_DAMAGE_TYPE.getId(this.danmakuDamageType);
        if (danmakuDamageTypeId != null) {
            view.putString("DamageType", danmakuDamageTypeId.toString());
        }
        view.putInt("FlyAge", this.flyAge);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.itemStack = view.read("Item", ItemStackWrapper.FLEXIBLE_ITEMSTACK_CODEC).orElse(ItemStack.EMPTY);
        this.tile = view.getBoolean("IsTile", true);
        this.danmakuDamageType = RegistryManager.DANMAKU_DAMAGE_TYPE.get(Identifier.of(view.getString("DamageType", Touhou.id("generic").toString())));

        this.flyAge = view.getInt("FlyAge", 0);

    }

    @Override
    public void tick() {
        super.tick();
        this.fightTick++;
        this.particleTick++;
        if (!this.isInGround()) {
            this.dataTracker.set(ROLL, (float) (this.dataTracker.get(ROLL) - MathHelper.RADIANS_PER_DEGREE * this.getVelocity().lengthSquared() * 15) % MathHelper.TAU);
        }
        this.setPitch(this.setupPitch);
        this.setYaw(this.setupYaw);
        if (this.particleTick > 2) {
            World world = this.getWorld();
            if (!world.isClient() && world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(
                        ParticleTypes.SNOWFLAKE,
                        this.getPos().x,
                        this.getPos().y,
                        this.getPos().z,
                        1,
                        0,
                        0,
                        0,
                        0.1
                );
            }
            this.particleTick = 0;
        }

        if (this.isTouchingWater()) {
//            this.setVelocity(this.getVelocity().multiply(0.8));
            this.fluidAge++;
            if (this.fluidAge > 80) {
                this.discard();
            }
        }

        if (this.fightTick > MAX_FLIGHT_TICK) {
            this.discard();
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        this.setPosition(blockHitResult.getPos());
        if (blockHitResult.getType() == HitResult.Type.BLOCK) {
            BlockState block = this.getWorld().getBlockState(blockHitResult.getBlockPos());
            blockHitParticles(this.getPos(), block, this.getWorld(), this.getDamage() * this.getVelocity().length());
            SoundEvent soundEvent = block.getSoundGroup().getHitSound();
            setSilent(false);
            playSound(soundEvent, 0.2F, 1.0F);
            setSilent(true);
        }
        this.setOnFire(true);
        super.onBlockHit(blockHitResult);
        this.setOnFire(false);
        this.discard();
    }

    protected boolean canDamage(Entity entity, Entity owner) {
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
    protected void onEntityHit(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        Entity owner = this.getOwner();
        if (!this.canDamage(entity, owner)) {
            return;
        }

        if (entity instanceof PlayerEntity player) {
            if (player.isBlocking()) {
                boolean isInAttackRange = false;
                ItemStack activeItem = player.getActiveItem();
                if (!activeItem.isEmpty()) {
                    BlocksAttacksComponent blocksAttacksComponent = activeItem.get(DataComponentTypes.BLOCKS_ATTACKS);
                    if (blocksAttacksComponent != null) {
                        List<BlocksAttacksComponent.DamageReduction> damageReductions = blocksAttacksComponent.damageReductions();
                        for (BlocksAttacksComponent.DamageReduction damageReduction : damageReductions) {
                            float blockingAngle = damageReduction.horizontalBlockingAngle();

                            // ① 使用 EyePos，避免高度误差
                            Vec3d toProjectile = this.getPos().subtract(player.getEyePos()).normalize();

                            // ② 只取水平向量，忽略 Y
                            Vec3d playerLook = player.getRotationVec(1.0F);
                            Vec3d look2D = new Vec3d(playerLook.x, 0, playerLook.z).normalize();
                            Vec3d toProj2D = new Vec3d(toProjectile.x, 0, toProjectile.z).normalize();

                            // ③ 点积求角度，clamp 防止 NaN
                            double dot = MathHelper.clamp(look2D.dotProduct(toProj2D), -1.0, 1.0);
                            double angle = Math.toDegrees(Math.acos(dot));

                            // ④ 给一个小容错（比如 +5°）
                            if (angle <= (blockingAngle / 2.0F) + 5.0F) {
                                isInAttackRange = true;
                                break; // 找到一个满足条件的就可以退出循环了
                            }
                        }
                    }
                    if (isInAttackRange) {
                        activeItem.damage(1, player);
                        this.discard(); // 拦截并移除投射物
                        return;
                    }
                }
            }

        }

        this.setPosition(entityHitResult.getPos());
        this.setSilent(false);
        this.setSilent(true);

        if (entityHitResult.getEntity() != null) {
            this.setDamage(this.getDamage());
            this.entityHitParticles(entityHitResult.getEntity(), this.getDamage() * this.getVelocity().length());
        }

        this.hitDamage(entityHitResult, this.getWorld());
        this.discard();
    }

    protected void hitDamage(EntityHitResult entityHitResult, World world) {
        if (world instanceof ServerWorld serverWorld) {
            Entity target = entityHitResult.getEntity();
            Entity owner = this.getOwner();

            if (target instanceof LivingEntity livingTarget && this.getOwner() != entityHitResult.getEntity()) {
                DamageSource damageSource;

                if (owner instanceof LivingEntity attacker) {
                    damageSource = world.getDamageSources().mobProjectile(this, attacker);
                } else {
                    damageSource = this.danmakuDamageType.mapToSource(world.getDamageSources());
                }

                float damageAmount = (float) this.getDamage();
                livingTarget.damage(serverWorld, damageSource, damageAmount);
                livingTarget.setInvulnerable(false);
                livingTarget.lastDamageTaken = 0;
                this.onHitEffect.damage(livingTarget, this.getDamage());
            }
        }
    }

    protected void entityHitParticles(Entity livingEntity, double damage) {
        if (livingEntity.getWorld() instanceof ServerWorld world) {
            Vec3d pos = livingEntity.getPos();
            int particleCount = (int) damage * 4;
            double radius = livingEntity.getWidth() / 2 + 0.5;
            double heightOffset = livingEntity.getHeight();

            for (int i = 0; i < particleCount; i++) {
                double angle = (2 * Math.PI / particleCount) * i;
                double xOffset = radius * Math.cos(angle);
                double zOffset = radius * Math.sin(angle);

                ItemStackParticleEffect itemStackParticleEffect = new ItemStackParticleEffect(ParticleTypes.ITEM, this.getDefaultItemStack());
                world.spawnParticles(
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

    protected void blockHitParticles(Vec3d pos, BlockState blockState, World worldTemp, double damage) {
        if (worldTemp instanceof ServerWorld world) {
            int particleCount = (int) damage * 4;
            double radius = 1;
            double heightOffset = 1;


            for (int i = 0; i < particleCount; i++) {
                double angle = (2 * Math.PI / particleCount) * i;
                double xOffset = radius * Math.cos(angle);
                double zOffset = radius * Math.sin(angle);

                BlockStateParticleEffect blockStateParticleEffect = new BlockStateParticleEffect(ParticleTypes.BLOCK, blockState);
                world.spawnParticles(
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
        if (!tryInvokeMethod(PersistentProjectileEntity.class, "setPierceLevel", byte.class, level)) {
            tryInvokeMethod(PersistentProjectileEntity.class, "method_7451", byte.class, level);
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
    protected ItemStack getDefaultItemStack() {
        if (this.danmakuItem != null && !this.danmakuItem.getDefaultStack().isEmpty()) {
            return this.danmakuItem.getDefaultStack();
        } else {
            return new ItemStack(ModItems.ICON);
        }
    }

    public interface OnHitFactory {
        void damage(LivingEntity livingEntity, double damage);
    }

}
