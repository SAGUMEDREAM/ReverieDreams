package cc.thonly.reverie_dreams.entity.misc;

import cc.thonly.polymer.entity.WheelChairImpl;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.server.PlayerInputManager;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
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

@Setter
@Getter
@ToString
public class WheelchairEntity extends PathfinderMob implements PlayerRideableJumping {
    public String ownerUUID = "";

    public WheelchairEntity(EntityType<? extends PathfinderMob> entityType, Level world) {
        super(entityType, world);
        AttributeMap attributes = this.getAttributes();
        AttributeInstance scaleInstance = attributes.getInstance(Attributes.SCALE);
        if (scaleInstance != null) {
            scaleInstance.setBaseValue(0.25);
        }
    }

    public WheelchairEntity(EntityType<? extends PathfinderMob> entityType, Level world, int x, int y, int z) {
        this(entityType, world);
        this.setPos(x, y, z);
    }

    public WheelchairEntity(EntityType<? extends PathfinderMob> entityType, Level world, int x, int y, int z, String ownerUUID) {
        this(entityType, world, x, y, z);
        this.ownerUUID = ownerUUID;
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
        if (!this.hasEffect(MobEffects.INVISIBILITY)) {
            this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        PolymerEntity polymerEntity = PolymerEntity.get(this);
        if (polymerEntity instanceof WheelChairImpl impl) {
            impl.onTrackingStopped(player);
            impl.onCreated();
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        PolymerEntity polymerEntity = PolymerEntity.get(this);
        if (polymerEntity instanceof WheelChairImpl impl) {
            impl.onTrackingStopped(player);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker != null && attacker.isShiftKeyDown() && this.ownerUUID.equalsIgnoreCase(attacker.getStringUUID())) {
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
    protected void tickRidden(Player controllingPlayer, Vec3 movementInput) {
        super.tickRidden(controllingPlayer, movementInput);
        Vec2 vec2f = this.getControlledRotation(controllingPlayer);
        this.setRot(vec2f.y, vec2f.x);
        this.yBodyRot = this.yHeadRot = this.getYRot();
        this.yBodyRot = controllingPlayer.getVisualRotationYInDegrees();
        this.yBodyRotO = this.yBodyRot;

        if (controllingPlayer instanceof ServerPlayer player) {
            boolean keyLeft = PlayerInputManager.isKeyDown(player, PlayerInputManager.InputKey.LEFT);
            boolean keyRight = PlayerInputManager.isKeyDown(player, PlayerInputManager.InputKey.RIGHT);
            boolean keyForward = PlayerInputManager.isKeyDown(player, PlayerInputManager.InputKey.FORWARD);
            boolean keyBack = PlayerInputManager.isKeyDown(player, PlayerInputManager.InputKey.BACKWARD);
            boolean keySpeedUp = PlayerInputManager.isKeyDown(player, PlayerInputManager.InputKey.SPRINT);

            float strafe = keyLeft ? 0.5f : (keyRight ? -0.5f : 0);
            float forward = keyForward ? 3 : (keyBack ? -0.5f : 0);

            float speedMultiplier = keySpeedUp ? 1.8f : 1.0f;

            this.moveRelative(0.245f * speedMultiplier, new Vec3(strafe, -1, forward));
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.hasImpulse = true;
        }
    }

    @Override
    public boolean isNoGravity() {
        return false;
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
            if (!this.level().isClientSide) {
                player.startRiding(this);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    public static AttributeSupplier createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FLYING_SPEED, 0.15)
                .add(Attributes.KNOCKBACK_RESISTANCE, 10.0)
                .build();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        view.putString("OwnerUUID", this.ownerUUID);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        this.ownerUUID = view.getStringOr("OwnerUUID", "null");
    }

    @Override
    public Vec3 getPassengerRidingPosition(Entity entity) {
        Vec3 position = super.getPassengerRidingPosition(entity);
        position = new Vec3(position.x, position.y - 0.5, position.z);
        return position;
    }

    @Override
    public void onPlayerJump(int strength) {

    }

    @Override
    public boolean canJump() {
        return this.onGround();
    }

    @Override
    public void handleStartJump(int height) {
        if (this.onGround()) {
            this.push(0, 0.42D, 0);
        }
    }


    @Override
    public void handleStopJump() {

    }
}
