package cc.thonly.reverie_dreams.entity.misc;

import cc.thonly.polymer.entity.MagicBroomImpl;
import cc.thonly.polymer.entity.WheelChairImpl;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.server.PlayerInputManager;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@Setter
@Getter
@ToString
public class MagicBroomEntity extends PathAwareEntity implements JumpingMount {
    public ItemStackWrapper itemWrapper = ItemStackWrapper.of(Items.AIR.getDefaultStack());
    public int damageTick = 0;
    public final int maxDamageTick = 20 * 8;
    public String ownerUUID = "";

    public MagicBroomEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    public MagicBroomEntity(EntityType<? extends PathAwareEntity> entityType, World world, int x, int y, int z, ItemStackWrapper wrapper) {
        this(entityType, world);
        this.setPosition(x, y, z);
        this.itemWrapper = wrapper;
    }

    public MagicBroomEntity(EntityType<? extends PathAwareEntity> entityType, World world, int x, int y, int z, ItemStackWrapper wrapper, String ownerUUID) {
        this(entityType, world, x, y, z, wrapper);
        this.ownerUUID = ownerUUID;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(0, new SwimGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld() instanceof ServerWorld world) {
            this.setNoGravity(this.hasPassengers());
            if (!this.hasStatusEffect(StatusEffects.INVISIBILITY)) {
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
            }
            if (!this.itemWrapper.isEmpty() && this.itemWrapper.getItemStack().isDamageable() && this.itemWrapper.getItemStack().getDamage() >= this.itemWrapper.getItemStack().getMaxDamage()) {
                this.damage(world, this.getDamageSources().magic(), Integer.MAX_VALUE);
            }
        }
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        PolymerEntity polymerEntity = PolymerEntity.get(this);
        if (polymerEntity instanceof MagicBroomImpl impl) {
            impl.onTrackingStopped(player);
            impl.onCreated();
        }
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        PolymerEntity polymerEntity = PolymerEntity.get(this);
        if (polymerEntity instanceof MagicBroomImpl impl) {
            impl.onTrackingStopped(player);
        }
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        Entity attacker = source.getAttacker();
        if (attacker != null && attacker.isSneaking() && this.ownerUUID.intern().equalsIgnoreCase(attacker.getUuid().toString())) {
            if (!this.itemWrapper.isEmpty()) {
                ItemStack copiedStack = this.itemWrapper.getItemStack().copy();
                ItemEntity itemEntity = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), copiedStack);
                world.spawnEntity(itemEntity);
                this.discard();
            }
        }
        return super.damage(world, source, amount);
    }

    @Override
    public void onDamaged(DamageSource damageSource) {
        if (damageSource.isOf(DamageTypes.FALL)) {
            return;
        }
        super.onDamaged(damageSource);
    }

    @Override
    protected void applyDamage(ServerWorld world, DamageSource source, float amount) {
        if (source.isOf(DamageTypes.FALL)) {
            return;
        }
        super.applyDamage(world, source, amount);
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        PlayerEntity playerEntity;
        Entity entity;
        if ((entity = this.getFirstPassenger()) instanceof PlayerEntity && (playerEntity = (PlayerEntity) entity).isAlive()) {
            return playerEntity;
        }
        return super.getControllingPassenger();
    }

    @Override
    protected void tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput) {
        super.tickControlled(controllingPlayer, movementInput);
        Vec2f vec2f = this.getControlledRotation(controllingPlayer);
        this.setRotation(vec2f.y, vec2f.x);
        this.bodyYaw = this.headYaw = this.getYaw();
        this.lastBodyYaw = this.headYaw;

        if (!this.itemWrapper.isEmpty() && !controllingPlayer.isInCreativeMode() && this.itemWrapper.getItemStack().isDamageable()) {
            this.damageTick++;
            if (this.damageTick > this.maxDamageTick) {
                this.itemWrapper.getItemStack().damage(1, this, EquipmentSlot.MAINHAND);
                this.damageTick = 0;
            }
        }

        if (controllingPlayer instanceof ServerPlayerEntity player) {
            boolean keyLeft = PlayerInputManager.isKeyDown(player, PlayerInputManager.InputKey.LEFT);
            boolean keyRight = PlayerInputManager.isKeyDown(player, PlayerInputManager.InputKey.RIGHT);
            boolean keyForward = PlayerInputManager.isKeyDown(player, PlayerInputManager.InputKey.FORWARD);
            boolean keyBack = PlayerInputManager.isKeyDown(player, PlayerInputManager.InputKey.BACKWARD);
            boolean keySpeedUp = PlayerInputManager.isKeyDown(player, PlayerInputManager.InputKey.SPRINT);

            float strafe = keyLeft ? 0.5f : (keyRight ? -0.5f : 0);
            float vertical = keyForward ? -(player.getPitch() - 10) / 22.5f : 0;
            float forward = keyForward ? 3 : (keyBack ? -0.5f : 0);

            float speedMultiplier = keySpeedUp ? 1.8f : 1.0f;

            this.updateVelocity(0.245f * speedMultiplier, new Vec3d(strafe, vertical, forward));
            this.move(MovementType.SELF, this.getVelocity());
            this.velocityDirty = true;
        }
    }

    protected Vec2f getControlledRotation(LivingEntity controllingPassenger) {
        return new Vec2f(controllingPassenger.getPitch() * 0.5f, controllingPassenger.getYaw());
    }

    @Override
    protected Vec3d getControlledMovementInput(PlayerEntity controllingPlayer, Vec3d movementInput) {
        float f = controllingPlayer.sidewaysSpeed * 0.5f;
        float g = controllingPlayer.forwardSpeed;
        if (g <= 0.0f) {
            g *= 0.25f;
        }
        return new Vec3d(f, 0.0, g);
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);
        World world = this.getWorld();
        if (this.itemWrapper.isEmpty()) {
            return;
        }
        ItemStack copiedStack = this.itemWrapper.getItemStack().copy();
        ItemEntity itemEntity = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), copiedStack);
        world.spawnEntity(itemEntity);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        World world = player.getWorld();
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {

        }
        if (!this.hasPassengers() && !player.shouldCancelInteraction()) {
            if (!this.getWorld().isClient) {
                player.startRiding(this);
            }
            return ActionResult.SUCCESS;
        }
        return super.interactMob(player, hand);
    }

    public static DefaultAttributeContainer createAttributes() {
        return AnimalEntity.createAnimalAttributes()
                .add(EntityAttributes.MAX_HEALTH, 15.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.FLYING_SPEED, 0.15)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 10.0)
                .build();
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.put("SummonedItem", ItemStackWrapper.CODEC, this.itemWrapper);
        view.putString("OwnerUUID", this.ownerUUID);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        DynamicRegistryManager registryManager = this.getRegistryManager();
        this.itemWrapper = view.read("SummonedItem", ItemStackWrapper.CODEC).orElse(ItemStackWrapper.of(Items.AIR));

        this.ownerUUID = view.getString("OwnerUUID", "null");

    }

    @Override
    public void setJumpStrength(int strength) {

    }

    @Override
    public boolean canJump() {
        return true;
    }

    @Override
    public void startJumping(int height) {

    }

    @Override
    public void stopJumping() {

    }
}
