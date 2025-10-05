package cc.thonly.reverie_dreams.entity.misc;

import cc.thonly.polymer.entity.WheelChairImpl;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.server.PlayerInputManager;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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
public class WheelchairEntity extends PathAwareEntity implements JumpingMount {
    public String ownerUUID = "";

    public WheelchairEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        AttributeContainer attributes = this.getAttributes();
        EntityAttributeInstance scaleInstance = attributes.getCustomInstance(EntityAttributes.SCALE);
        if (scaleInstance != null) {
            scaleInstance.setBaseValue(0.25);
        }
    }

    public WheelchairEntity(EntityType<? extends PathAwareEntity> entityType, World world, int x, int y, int z) {
        this(entityType, world);
        this.setPosition(x, y, z);
    }

    public WheelchairEntity(EntityType<? extends PathAwareEntity> entityType, World world, int x, int y, int z, String ownerUUID) {
        this(entityType, world, x, y, z);
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
        if (!(this.getWorld() instanceof ServerWorld world)) {
            return;
        }
        if (!this.hasStatusEffect(StatusEffects.INVISIBILITY)) {
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
        }
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        PolymerEntity polymerEntity = PolymerEntity.get(this);
        if (polymerEntity instanceof WheelChairImpl impl) {
            impl.onTrackingStopped(player);
            impl.onCreated();
        }
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        PolymerEntity polymerEntity = PolymerEntity.get(this);
        if (polymerEntity instanceof WheelChairImpl impl) {
            impl.onTrackingStopped(player);
        }
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        Entity attacker = source.getAttacker();
        if (attacker != null && attacker.isSneaking() && this.ownerUUID.equalsIgnoreCase(attacker.getUuidAsString())) {
                ItemEntity itemEntity = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), new ItemStack(ModBlocks.WHEEL_CHAIR));
                world.spawnEntity(itemEntity);
                this.discard();
        }
        return super.damage(world, source, amount);
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
        this.bodyYaw = controllingPlayer.getBodyYaw();
        this.lastBodyYaw = this.bodyYaw;

        if (controllingPlayer instanceof ServerPlayerEntity player) {
            boolean keyLeft = PlayerInputManager.isKeyDown(player, PlayerInputManager.InputKey.LEFT);
            boolean keyRight = PlayerInputManager.isKeyDown(player, PlayerInputManager.InputKey.RIGHT);
            boolean keyForward = PlayerInputManager.isKeyDown(player, PlayerInputManager.InputKey.FORWARD);
            boolean keyBack = PlayerInputManager.isKeyDown(player, PlayerInputManager.InputKey.BACKWARD);
            boolean keySpeedUp = PlayerInputManager.isKeyDown(player, PlayerInputManager.InputKey.SPRINT);

            float strafe = keyLeft ? 0.5f : (keyRight ? -0.5f : 0);
            float forward = keyForward ? 3 : (keyBack ? -0.5f : 0);

            float speedMultiplier = keySpeedUp ? 1.8f : 1.0f;

            this.updateVelocity(0.245f * speedMultiplier, new Vec3d(strafe, -1, forward));
            this.move(MovementType.SELF, this.getVelocity());
            this.velocityDirty = true;
        }
    }

    @Override
    public boolean hasNoGravity() {
        return false;
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
        ItemEntity itemEntity = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), new ItemStack(ModBlocks.WHEEL_CHAIR));
        world.spawnEntity(itemEntity);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        World world = player.getWorld();
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
                .add(EntityAttributes.MAX_HEALTH, 20.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.FLYING_SPEED, 0.15)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 10.0)
                .build();
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putString("OwnerUUID", this.ownerUUID);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.ownerUUID = view.getString("OwnerUUID", "null");
    }
    @Override
    public void setJumpStrength(int strength) {

    }

    @Override
    public boolean canJump() {
        return this.isOnGround();
    }

    @Override
    public void startJumping(int height) {
        if (this.isOnGround()) {
            this.addVelocity(0, 0.42D, 0);
        }
    }


    @Override
    public void stopJumping() {

    }
}
