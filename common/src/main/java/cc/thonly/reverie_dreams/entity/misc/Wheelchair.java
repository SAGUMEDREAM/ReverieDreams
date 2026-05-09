package cc.thonly.reverie_dreams.entity.misc;

import cc.thonly.reverie_dreams.api.polymer.PolymerEntityGetter;
import cc.thonly.reverie_dreams.inf.IHolderEntity;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.server.IPlayerInputManager;
import cc.thonly.reverie_dreams.server.InputKey;
import cc.thonly.reverie_dreams.util.PlatformContext;
import cc.thonly.reverie_dreams.util.codec.UUIDCodec;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("resource")
@Setter
@Getter
@ToString
public class Wheelchair extends PathfinderMob implements PlayerRideableJumping {
    @Nullable
    public UUID owner;
    public int jumpingPower = 0;

    public Wheelchair(EntityType<? extends PathfinderMob> entityType, Level world) {
        super(entityType, world);
        AttributeMap attributes = this.getAttributes();
        AttributeInstance scaleInstance = attributes.getInstance(Attributes.SCALE);
        if (scaleInstance != null) {
            scaleInstance.setBaseValue(0.25);
        }
    }

    public Wheelchair(EntityType<? extends PathfinderMob> entityType, Level world, float x, float y, float z) {
        this(entityType, world);
        this.setPos(x, y, z);
    }

    public Wheelchair(EntityType<? extends PathfinderMob> entityType, Level world, float x, float y, float z, Entity owner) {
        this(entityType, world, x, y, z);
        if (owner != null) {
            this.owner = owner.getUUID();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel world)) {
            return;
        }
        if (PlatformContext.hasPolymer()) {
            if (!this.hasEffect(MobEffects.INVISIBILITY)) {
                this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
            }
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        Object polymerEntity = PolymerEntityGetter.getPolymerEntity(this);
        if (polymerEntity instanceof IHolderEntity impl) {
            impl.onTrackingStopped(player);
            impl.onCreated();
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        Object polymerEntity = PolymerEntityGetter.getPolymerEntity(this);
        if (polymerEntity instanceof IHolderEntity impl) {
            impl.onTrackingStopped(player);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker != null && attacker.isShiftKeyDown() && Objects.equals(this.owner, attacker.getUUID())) {
            ItemEntity itemEntity = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), new ItemStack(RDBlocks.WHEEL_CHAIR));
            world.addFreshEntity(itemEntity);
            this.discard();
        }
        return super.hurtServer(world, source, amount);
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        Player playerEntity;
        Entity entity;
        if ((entity = this.getFirstPassenger()) instanceof Player && (playerEntity = (Player) entity).isAlive()) {
            return playerEntity;
        }
        return super.getControllingPassenger();
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (PlatformContext.hasPolymer()) {
            super.travel(travelVector);
            return;
        }

        if (!this.isVehicle() || !(this.getControllingPassenger() instanceof Player player)) {
            super.travel(travelVector);
            return;
        }

        this.setNoGravity(true);

        this.setDeltaMovement(this.getDeltaMovement().scale(0.8));
        Vec3 motion = this.getDeltaMovement();

        float strafe = player.xxa;
        float forward = player.zza;

        if (forward < 0) forward *= 0.25f;

        float speed = 0.1f * (player.isSprinting() ? 1.8f : 1.0f);

        this.moveRelative(speed, new Vec3(strafe, 0, forward));

        motion = this.getDeltaMovement();
        motion = motion.add(0, -0.08, 0);

        if (this.jumpingPower > 0 && this.onGround()) {
            motion = new Vec3(motion.x, 0.42, motion.z);
            this.jumpingPower = 0;
        }

        if (player.isShiftKeyDown()) {
            motion = new Vec3(motion.x, -0.15, motion.z);
        }

        this.setDeltaMovement(motion);
        this.move(MoverType.SELF, motion);
    }

    @Override
    protected void tickRidden(Player controllingPlayer, Vec3 movementInput) {
        super.tickRidden(controllingPlayer, movementInput);
        Vec2 vec2f = this.getControlledRotation(controllingPlayer);
        this.setRot(vec2f.y, vec2f.x);
        this.yBodyRot = this.yHeadRot = this.getYRot();
        this.yBodyRot = controllingPlayer.getVisualRotationYInDegrees();
        this.yBodyRotO = this.yBodyRot;

        if (PlatformContext.hasPolymer() && controllingPlayer instanceof ServerPlayer player) {
            IPlayerInputManager inputManager = IPlayerInputManager.polymerAccess();
            boolean keyLeft = inputManager.isKeyDown(player, InputKey.LEFT);
            boolean keyRight = inputManager.isKeyDown(player, InputKey.RIGHT);
            boolean keyForward = inputManager.isKeyDown(player, InputKey.FORWARD);
            boolean keyBack = inputManager.isKeyDown(player, InputKey.BACKWARD);
            boolean keySpeedUp = inputManager.isKeyDown(player, InputKey.SPRINT);

            float strafe = keyLeft ? 0.5f : (keyRight ? -0.5f : 0);
            float forward = keyForward ? 3 : (keyBack ? -0.5f : 0);

            float speedMultiplier = keySpeedUp ? 1.8f : 1.0f;

            this.moveRelative(0.245f * speedMultiplier, new Vec3(strafe, -1, forward));
            this.move(MoverType.SELF, this.getDeltaMovement());
//            this.hasImpulse = true;
        }
    }

    protected Vec2 getControlledRotation(LivingEntity controllingPassenger) {
        return new Vec2(controllingPassenger.getXRot() * 0.5f, controllingPassenger.getYRot());
    }

    @Override
    protected Vec3 getRiddenInput(Player controllingPlayer, Vec3 movementInput) {
        float f = controllingPlayer.xxa * 0.5f;
        float g = controllingPlayer.zza;
        if (g <= 0.0f) {
            g *= 0.25f;
        }
        return new Vec3(f, 0.0, g);
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        Level world = this.level();
        ItemEntity itemEntity = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), new ItemStack(RDBlocks.WHEEL_CHAIR));
        world.addFreshEntity(itemEntity);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        Level world = player.level();
        if (!this.isVehicle() && !player.isSecondaryUseActive()) {
            if (!this.level().isClientSide()) {
                player.startRiding(this);
                return InteractionResult.SUCCESS_SERVER;
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    public static AttributeSupplier.@NonNull Builder createLivingAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FLYING_SPEED, 0.15)
                .add(Attributes.KNOCKBACK_RESISTANCE, 10.0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        view.storeNullable("OwnerUUID", UUIDCodec.CODEC, this.owner);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        this.owner = view.read("OwnerUUID", UUIDCodec.CODEC).orElse(null);
    }

    @Override
    public Vec3 getPassengerRidingPosition(Entity entity) {
        Vec3 position = super.getPassengerRidingPosition(entity);
        if (PlatformContext.hasPolymer()) {
            return new Vec3(position.x, position.y - 0.5, position.z);
        }
        return position;
    }

    @Override
    public boolean canJump() {
        return this.onGround();
    }

    @Override
    public void onPlayerJump(int strength) {
        this.jumpingPower = strength;
    }

    @Override
    public void handleStartJump(int height) {
        if (this.onGround()) {
            this.push(0, 0.42D, 0);
        }
        this.jumping = true;
        this.jumpingPower = height;
    }

    @Override
    public void handleStopJump() {
        this.jumping = false;
        this.jumpingPower = 0;
    }
}
