package cc.thonly.reverie_dreams.entity.misc;

import cc.thonly.reverie_dreams.api.player.PlayerInputManagerAccess;
import cc.thonly.reverie_dreams.api.polymer.CommonPolymerHolderEntity;
import cc.thonly.reverie_dreams.api.polymer.callback.PolymerEntityGetterCallback;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.server.InputKey;
import cc.thonly.reverie_dreams.util.PlatformContext;
import cc.thonly.reverie_dreams.util.codec.UUIDCodec;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

@SuppressWarnings("resource")
@Setter
@Getter
@ToString
public class MagicBroom extends PathfinderMob implements PlayerRideableJumping {
    public static final EntityDataAccessor<IngredientStack> STACK =
            SynchedEntityData.defineId(MagicBroom.class, IngredientStack.SERIALIZER);
    private float FCMP_THRE = 1e-4f;
    public int damageTick = 0;
    public final int maxDamageTick = 20 * 8;
    public int jumpingPower = 0;
    @Nullable
    public UUID owner;

    public MagicBroom(EntityType<? extends PathfinderMob> entityType, Level world) {
        super(entityType, world);
    }

    public MagicBroom(EntityType<? extends PathfinderMob> entityType, Level world, float x, float y, float z, IngredientStack wrapper) {
        this(entityType, world);
        this.setPos(x, y, z);
        this.setItemStack(wrapper);
    }

    public MagicBroom(EntityType<? extends PathfinderMob> entityType, Level world, float x, float y, float z, IngredientStack wrapper, UUID owner) {
        this(entityType, world, x, y, z, wrapper);
        this.owner = owner;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STACK, IngredientStack.of(Items.AIR.getDefaultInstance()));
    }

    public void setItemStack(IngredientStack itemWrapper) {
        this.getEntityData().set(STACK, itemWrapper);
    }

    public IngredientStack getIngredientStack() {
        return this.getEntityData().get(STACK);
    }

    @Override
    public Component getName() {
        if (this.getIngredientStack() == null || this.getIngredientStack().isEmpty()) {
            return super.getName();
        }
        ItemStack itemStack = this.getIngredientStack().getLazyStack();
        return itemStack.getHoverName();
    }

    @Override
    public @Nullable Component getCustomName() {
        return this.getName();
    }

    @Override
    public Component getDisplayName() {
        return this.getName();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel world) {
            this.fallDistance = 0;
            this.jumpingPower -= 1;
            this.setNoGravity(this.isVehicle());
            if (PlatformContext.hasPolymer()) {
                if (!this.hasEffect(MobEffects.INVISIBILITY)) {
                    this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
                }
            }
            if (!this.getIngredientStack().isEmpty() && this.getIngredientStack().getLazyStack().isDamageableItem() && this.getIngredientStack().getLazyStack().getDamageValue() >= this.getIngredientStack().getLazyStack().getMaxDamage()) {
                this.hurtServer(world, this.damageSources().magic(), Integer.MAX_VALUE);
            }
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        Object polymerEntity = PolymerEntityGetterCallback.getPolymerEntity(this);
        if (polymerEntity instanceof CommonPolymerHolderEntity impl) {
            impl.onTrackingStopped(player);
            impl.onCreated();
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        Object polymerEntity = PolymerEntityGetterCallback.getPolymerEntity(this);
        if (polymerEntity instanceof CommonPolymerHolderEntity impl) {
            impl.onTrackingStopped(player);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker != null && attacker.isShiftKeyDown()) {
            if (!this.getIngredientStack().isEmpty()) {
                ItemStack copiedStack = this.getIngredientStack().getLazyStack().copy();
                ItemEntity itemEntity = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), copiedStack);
                world.addFreshEntity(itemEntity);
                this.discard();
            }
        }
        return super.hurtServer(world, source, amount);
    }

    @Override
    public void handleDamageEvent(DamageSource damageSource) {
        if (damageSource.is(DamageTypes.FALL)) {
            return;
        }
        super.handleDamageEvent(damageSource);
    }

    @Override
    protected void actuallyHurt(ServerLevel world, DamageSource source, float amount) {
        if (source.is(DamageTypes.FALL)) {
            return;
        }
        super.actuallyHurt(world, source, amount);
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
        if (this.isVehicle() && this.getControllingPassenger() instanceof Player player) {

            this.setDeltaMovement(this.getDeltaMovement().scale(0.8));
            float strafe = player.xxa * 0.5f;
            float forward = player.zza;

            if (forward <= 0.0f) {
                forward *= 0.25f;
            }
            float vertical = 0;
            boolean sprint = player.isSprinting();
            boolean sneak = player.isShiftKeyDown();

            float speed = 0.15f * (sprint ? 1.8f : 1.0f);

            if (forward != 0) {
                vertical = -player.getXRot() / 90f + 0.05f;
            }

//            if (this.jumpingPower > 0) {
//                vertical += 0.6f;
//            }

            if (sneak) vertical -= 0.6f;

            this.moveRelative(speed, new Vec3(strafe, vertical, forward));
            this.move(MoverType.SELF, this.getDeltaMovement());

            return;
        }

        super.travel(travelVector);
    }

    @Override
    protected void tickRidden(Player controllingPlayer, Vec3 movementInput) {
        super.tickRidden(controllingPlayer, movementInput);
        Vec2 vec2f = this.getControlledRotation(controllingPlayer);
        this.setRot(vec2f.y, vec2f.x);
        this.yBodyRot = this.yHeadRot = this.getYRot();
        this.yBodyRotO = this.yHeadRot;

        if (!this.getIngredientStack().isEmpty() && !controllingPlayer.hasInfiniteMaterials() && this.getIngredientStack().getLazyStack().isDamageableItem()) {
            this.damageTick++;
            if (this.damageTick > this.maxDamageTick) {
                this.getIngredientStack().getLazyStack().hurtAndBreak(1, this, EquipmentSlot.MAINHAND);
                this.damageTick = 0;
            }
        }

        if (PlatformContext.hasPolymer() && controllingPlayer instanceof ServerPlayer player) {
            PlayerInputManagerAccess inputManager = PlayerInputManagerAccess.polymerAccess();
            boolean keyLeft = inputManager.isKeyDown(player, InputKey.LEFT);
            boolean keyRight = inputManager.isKeyDown(player, InputKey.RIGHT);
            boolean keyForward = inputManager.isKeyDown(player, InputKey.FORWARD);
            boolean keyBack = inputManager.isKeyDown(player, InputKey.BACKWARD);
            boolean keySpeedUp = inputManager.isKeyDown(player, InputKey.SPRINT);

            float strafe = keyLeft ? 0.5f : (keyRight ? -0.5f : 0);
            float vertical = keyForward ? -(player.getXRot() - 10) / 22.5f : 0;
            float forward = keyForward ? 3 : (keyBack ? -0.5f : 0);

            float speedMultiplier = 1.5f * (keySpeedUp ? 1.8f : 1.0f);

            this.moveRelative(0.245f * speedMultiplier, new Vec3(strafe, vertical, forward));
            this.move(MoverType.SELF, this.getDeltaMovement());
//            this.hasImpulse = true;
        }
    }

    @Override
    public boolean canControlVehicle() {
        return true;
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
        if (this.getIngredientStack().isEmpty()) {
            return;
        }
        ItemStack copiedStack = this.getIngredientStack().getLazyStack().copy();
        ItemEntity itemEntity = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), copiedStack);
        world.addFreshEntity(itemEntity);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
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
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FLYING_SPEED, 0.15)
                .add(Attributes.KNOCKBACK_RESISTANCE, 10.0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        view.store("Item", IngredientStack.CODEC, this.getIngredientStack());
        if (this.owner != null) {
            view.store("Owner", UUIDCodec.CODEC, this.owner);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        RegistryAccess registryManager = this.registryAccess();
        this.setItemStack(view.read("Item", IngredientStack.CODEC).orElse(IngredientStack.of(Items.AIR)));
        view.read("Owner", UUIDCodec.CODEC).ifPresent(value -> this.owner = value);

    }

    @Override
    public Vec3 getPassengerRidingPosition(Entity entity) {
        Vec3 position = super.getPassengerRidingPosition(entity);
        if (PlatformContext.hasPolymer()) {
            return new Vec3(position.x, position.y - 1, position.z);
        }
        return new Vec3(position.x, position.y - 0.6, position.z);
    }

    @Override
    public boolean canJump() {
        return true;
    }

    @Override
    public void onPlayerJump(int strength) {
        this.jumpingPower = strength;
    }

    @Override
    public void handleStartJump(int height) {
        this.jumpingPower = height;
    }

    @Override
    public void handleStopJump() {
        this.jumpingPower = 0;
    }
}
