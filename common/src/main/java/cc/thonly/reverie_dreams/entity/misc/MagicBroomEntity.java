package cc.thonly.reverie_dreams.entity.misc;

import cc.thonly.reverie_dreams.api.polymer.PolymerEntityGetter;
import cc.thonly.reverie_dreams.inf.IHolderEntity;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.server.PlayerInputManager;
import cc.thonly.reverie_dreams.util.codec.UUIDCodec;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
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

@Setter
@Getter
@ToString
public class MagicBroomEntity extends PathfinderMob implements PlayerRideableJumping {
    public ItemStackWrapper itemWrapper = ItemStackWrapper.of(Items.AIR.getDefaultInstance());
    public int damageTick = 0;
    public final int maxDamageTick = 20 * 8;
    @Nullable
    public UUID owner;

    public MagicBroomEntity(EntityType<? extends PathfinderMob> entityType, Level world) {
        super(entityType, world);
    }

    public MagicBroomEntity(EntityType<? extends PathfinderMob> entityType, Level world, int x, int y, int z, ItemStackWrapper wrapper) {
        this(entityType, world);
        this.setPos(x, y, z);
        this.itemWrapper = wrapper;
    }

    public MagicBroomEntity(EntityType<? extends PathfinderMob> entityType, Level world, int x, int y, int z, ItemStackWrapper wrapper, UUID owner) {
        this(entityType, world, x, y, z, wrapper);
        this.owner = owner;
    }

    @Override
    public Component getName() {
        if (this.itemWrapper == null || this.itemWrapper.isEmpty()) {
            return super.getName();
        }
        ItemStack itemStack = this.itemWrapper.getItemStack();
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
        if (this.level() instanceof ServerLevel world) {
            this.setNoGravity(this.isVehicle());
            if (!this.hasEffect(MobEffects.INVISIBILITY)) {
                this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
            }
            if (!this.itemWrapper.isEmpty() && this.itemWrapper.getItemStack().isDamageableItem() && this.itemWrapper.getItemStack().getDamageValue() >= this.itemWrapper.getItemStack().getMaxDamage()) {
                this.hurtServer(world, this.damageSources().magic(), Integer.MAX_VALUE);
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
        if (attacker != null && attacker.isShiftKeyDown()) {
            if (!this.itemWrapper.isEmpty()) {
                ItemStack copiedStack = this.itemWrapper.getItemStack().copy();
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
    protected void tickRidden(Player controllingPlayer, Vec3 movementInput) {
        super.tickRidden(controllingPlayer, movementInput);
        Vec2 vec2f = this.getControlledRotation(controllingPlayer);
        this.setRot(vec2f.y, vec2f.x);
        this.yBodyRot = this.yHeadRot = this.getYRot();
        this.yBodyRotO = this.yHeadRot;

        if (!this.itemWrapper.isEmpty() && !controllingPlayer.hasInfiniteMaterials() && this.itemWrapper.getItemStack().isDamageableItem()) {
            this.damageTick++;
            if (this.damageTick > this.maxDamageTick) {
                this.itemWrapper.getItemStack().hurtAndBreak(1, this, EquipmentSlot.MAINHAND);
                this.damageTick = 0;
            }
        }

        if (controllingPlayer instanceof ServerPlayer player) {
            boolean keyLeft = PlayerInputManager.isKeyDown(player, PlayerInputManager.InputKey.LEFT);
            boolean keyRight = PlayerInputManager.isKeyDown(player, PlayerInputManager.InputKey.RIGHT);
            boolean keyForward = PlayerInputManager.isKeyDown(player, PlayerInputManager.InputKey.FORWARD);
            boolean keyBack = PlayerInputManager.isKeyDown(player, PlayerInputManager.InputKey.BACKWARD);
            boolean keySpeedUp = PlayerInputManager.isKeyDown(player, PlayerInputManager.InputKey.SPRINT);

            float strafe = keyLeft ? 0.5f : (keyRight ? -0.5f : 0);
            float vertical = keyForward ? -(player.getXRot() - 10) / 22.5f : 0;
            float forward = keyForward ? 3 : (keyBack ? -0.5f : 0);

            float speedMultiplier = 1.5f * (keySpeedUp ? 1.8f : 1.0f);

            this.moveRelative(0.245f * speedMultiplier, new Vec3(strafe, vertical, forward));
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
        if (this.itemWrapper.isEmpty()) {
            return;
        }
        ItemStack copiedStack = this.itemWrapper.getItemStack().copy();
        ItemEntity itemEntity = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), copiedStack);
        world.addFreshEntity(itemEntity);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.isVehicle() && !player.isSecondaryUseActive()) {
            if (!this.level().isClientSide()) {
                player.startRiding(this);
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
        view.store("Item", ItemStackWrapper.CODEC, this.itemWrapper);
        if (this.owner != null) {
            view.store("Owner", UUIDCodec.CODEC, this.owner);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        RegistryAccess registryManager = this.registryAccess();
        this.itemWrapper = view.read("Item", ItemStackWrapper.CODEC).orElse(ItemStackWrapper.of(Items.AIR));
        view.read("Owner", UUIDCodec.CODEC).ifPresent(value -> this.owner = value);

    }

    @Override
    public Vec3 getPassengerRidingPosition(Entity entity) {
        Vec3 position = super.getPassengerRidingPosition(entity);
        position = new Vec3(position.x, position.y - 1, position.z);
        return position;
    }

    @Override
    public void onPlayerJump(int strength) {

    }

    @Override
    public boolean canJump() {
        return true;
    }

    @Override
    public void handleStartJump(int height) {

    }

    @Override
    public void handleStopJump() {

    }
}
