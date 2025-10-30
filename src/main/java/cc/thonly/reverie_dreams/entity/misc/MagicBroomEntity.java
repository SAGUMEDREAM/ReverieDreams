package cc.thonly.reverie_dreams.entity.misc;

import cc.thonly.polymer.entity.MagicBroomImpl;
import cc.thonly.polymer.entity.WheelChairImpl;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.server.PlayerInputManager;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.PlayerRideableJumping;
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

@Setter
@Getter
@ToString
public class MagicBroomEntity extends PathfinderMob implements PlayerRideableJumping {
    public ItemStackWrapper itemWrapper = ItemStackWrapper.of(Items.AIR.getDefaultInstance());
    public int damageTick = 0;
    public final int maxDamageTick = 20 * 8;
    public String ownerUUID = "";

    public MagicBroomEntity(EntityType<? extends PathfinderMob> entityType, Level world) {
        super(entityType, world);
    }

    public MagicBroomEntity(EntityType<? extends PathfinderMob> entityType, Level world, int x, int y, int z, ItemStackWrapper wrapper) {
        this(entityType, world);
        this.setPos(x, y, z);
        this.itemWrapper = wrapper;
    }

    public MagicBroomEntity(EntityType<? extends PathfinderMob> entityType, Level world, int x, int y, int z, ItemStackWrapper wrapper, String ownerUUID) {
        this(entityType, world, x, y, z, wrapper);
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
        PolymerEntity polymerEntity = PolymerEntity.get(this);
        if (polymerEntity instanceof MagicBroomImpl impl) {
            impl.onTrackingStopped(player);
            impl.onCreated();
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        PolymerEntity polymerEntity = PolymerEntity.get(this);
        if (polymerEntity instanceof MagicBroomImpl impl) {
            impl.onTrackingStopped(player);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker != null && attacker.isShiftKeyDown() && this.ownerUUID.intern().equalsIgnoreCase(attacker.getUUID().toString())) {
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

            float speedMultiplier = keySpeedUp ? 1.8f : 1.0f;

            this.moveRelative(0.245f * speedMultiplier, new Vec3(strafe, vertical, forward));
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.hasImpulse = true;
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
        Level world = player.level();
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld) {

        }
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
                .add(Attributes.MAX_HEALTH, 15.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FLYING_SPEED, 0.15)
                .add(Attributes.KNOCKBACK_RESISTANCE, 10.0)
                .build();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        view.store("SummonedItem", ItemStackWrapper.CODEC, this.itemWrapper);
        view.putString("OwnerUUID", this.ownerUUID);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        RegistryAccess registryManager = this.registryAccess();
        this.itemWrapper = view.read("SummonedItem", ItemStackWrapper.CODEC).orElse(ItemStackWrapper.of(Items.AIR));

        this.ownerUUID = view.getStringOr("OwnerUUID", "null");

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
