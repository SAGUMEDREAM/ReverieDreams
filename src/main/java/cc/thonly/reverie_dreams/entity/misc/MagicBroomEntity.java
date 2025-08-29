package cc.thonly.reverie_dreams.entity.misc;

import cc.thonly.reverie_dreams.entity.ModEntityHolders;
import cc.thonly.reverie_dreams.entity.holder.MagicBroomHolder;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.server.PlayerInputManager;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.component.DataComponentTypes;
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
import net.minecraft.item.ItemDisplayContext;
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
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Optional;
import java.util.WeakHashMap;

@Setter
@Getter
@ToString
public class MagicBroomEntity extends PathAwareEntity implements PolymerEntity, JumpingMount {
    public static final WeakHashMap<Entity, ItemDisplayElement> ELEMENTS = new WeakHashMap<>();
    public ItemStack summonItem = Items.AIR.getDefaultStack();
    public int damageTick = 0;
    public final int maxDamageTick = 20 * 8;
    public String ownerUUID = "";

    public MagicBroomEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        this.onCreate(this);
    }

    public MagicBroomEntity(EntityType<? extends PathAwareEntity> entityType, World world, int x, int y, int z, ItemStack summonItem) {
        this(entityType, world);
        this.setPosition(x, y, z);
        this.summonItem = summonItem;
    }

    public MagicBroomEntity(EntityType<? extends PathAwareEntity> entityType, World world, int x, int y, int z, ItemStack summonItem, String ownerUUID) {
        this(entityType, world, x, y, z, summonItem);
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

    public void onCreate(Entity entity) {
        this.setNoGravity(true);
        var x = new ItemDisplayElement();
        var holder = new MagicBroomHolder(this);
        var stack = new ItemStack(ModEntityHolders.MAGIC_BROOM_DISPLAY);
        if (this.summonItem.hasGlint()) {
            stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        x.setItem(stack);
        x.setItemDisplayContext(ItemDisplayContext.HEAD);
        x.setInvisible(true);
        x.setTeleportDuration(3);
        x.setScale(new Vector3f(1.2f));
        holder.setElement(x);
        holder.addElement(x);
        EntityAttachment.ofTicking(holder, entity);
        VirtualEntityUtils.addVirtualPassenger(entity, x.getEntityId());
        ELEMENTS.put(entity, x);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld() instanceof ServerWorld world) {
            this.setNoGravity(this.hasPassengers());
            if (!this.hasStatusEffect(StatusEffects.INVISIBILITY)) {
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
            }
            if (!this.summonItem.isEmpty() && this.summonItem.isDamageable() && this.summonItem.getDamage() >= this.summonItem.getMaxDamage()) {
                this.damage(world, this.getDamageSources().magic(), Integer.MAX_VALUE);
            }
        }
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        Entity attacker = source.getAttacker();
        if (attacker != null && attacker.isSneaking() && this.ownerUUID.intern().equalsIgnoreCase(attacker.getUuid().toString())) {
            if (!this.summonItem.isEmpty()) {
                ItemStack copiedStack = this.summonItem.copy();
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

        if (!this.summonItem.isEmpty() && !controllingPlayer.isInCreativeMode() && this.summonItem.isDamageable()) {
            this.damageTick++;
            if (this.damageTick > this.maxDamageTick) {
                this.summonItem.damage(1, this, EquipmentSlot.MAINHAND);
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
        if (this.summonItem.isEmpty()) {
            return;
        }
        ItemStack copiedStack = this.summonItem.copy();
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
        if (this.summonItem != null && !this.summonItem.isEmpty()) {
            String json = ItemStackWrapper.toJson(ItemStackWrapper.of(this.summonItem));
            boolean isNull = json == null;
            if (!isNull) {
                boolean isEmpty = json.isEmpty();
                if(!isEmpty) {
                    view.putString("SummonedItem", json);
                }
            }
        }
        view.putString("OwnerUUID", this.ownerUUID);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        DynamicRegistryManager registryManager = this.getRegistryManager();
        Optional<ItemStackWrapper> summonedItemOptional = ItemStackWrapper.toWrapper(view.getString("SummonedItem", ""));
        summonedItemOptional.ifPresent(itemStack -> this.summonItem = itemStack.getItemStack());


        this.ownerUUID = view.getString("OwnerUUID", "null");

    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.PIG;
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
