package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.polymer.CommonPolymerHolderEntity;
import cc.thonly.reverie_dreams.api.polymer.callback.PolymerEntityGetterCallback;
import cc.thonly.reverie_dreams.component.RoleFollowerArchive;
import cc.thonly.reverie_dreams.data.npc.NPCState;
import cc.thonly.reverie_dreams.data.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.entity.NPCFishingHook;
import cc.thonly.reverie_dreams.entity.ai.goal.AvoidCreeperExplosionEntityGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.AvoidFireDamageEntityGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.attack.*;
import cc.thonly.reverie_dreams.entity.npc.container.NPCFoodDataContainer;
import cc.thonly.reverie_dreams.inventory.NPCInventoryImpl;
import cc.thonly.reverie_dreams.mixin.accessor.EntityTrackerAccessor;
import cc.thonly.reverie_dreams.mixin.accessor.LivingEntityAccessor;
import cc.thonly.reverie_dreams.mixin.accessor.ServerChunkLoadingManagerAccessor;
import cc.thonly.reverie_dreams.mixin.accessor.ServerEntityAccessor;
import cc.thonly.reverie_dreams.networking.payload.SyncEntityPacket;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.NPCStates;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.content.skin.MobSkinTypes;
import cc.thonly.reverie_dreams.util.PlatformContext;
import com.mojang.authlib.properties.Property;
import com.mojang.logging.LogUtils;
import dev.architectury.networking.NetworkManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import cc.thonly.reverie_dreams.entity.ai.goal.util.EntityTargetUtil;
import cc.thonly.reverie_dreams.entity.ai.NPCWorkController;
import cc.thonly.reverie_dreams.entity.variant.GensokyoFairyActions;
import cc.thonly.reverie_dreams.entity.variant.GensokyoFairyAnimation;
import cc.thonly.reverie_dreams.entity.variant.GensokyoFairyEntity;
import cc.thonly.reverie_dreams.inventory.NPCWorkInventory;
import cc.thonly.reverie_dreams.networking.NPCSyncSnapshot;

@SuppressWarnings({"resource", "SpellCheckingInspection", "unused"})
@Getter
@Setter
public abstract class BaseNPCLikeEntity extends AbstractNPCEntity implements RangedAttackMob, NPCSettings {
    public static final EntityDataAccessor<SkinType> SKIN_TYPE = SynchedEntityData.defineId(BaseNPCLikeEntity.class, SkinType.SERIALIZER);
    public static final EntityDataAccessor<SkinType> DEFAULT_SKIN_TYPE = SynchedEntityData.defineId(BaseNPCLikeEntity.class, SkinType.SERIALIZER);

    // 实体信息
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    protected NPCState npcState = NPCStates.NORMAL;
    protected NPCState lastNPCState = NPCStates.NORMAL;
    protected NPCWorkMode workMode = NPCWorkModes.COMBAT;
    protected boolean sit = false;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    protected String npcOwner = "";
    protected String seatUUID = "";
    protected ArmorStand seat;
    protected boolean paused = false;
    // 背包
    protected NPCInventoryImpl inventory;
    private NPCWorkController workController;

    public NPCWorkController getWorkController() {
        if (this.workController == null) this.workController = new NPCWorkController(this);
        return this.workController;
    }
    // 回血
    protected int healthTick = 20;
    protected int maxHealthTick = 20 * 8;
    // 攻击tick
    protected int updateAttackTick = 0;
    protected int maxUpdateAttackTick = 20 * 2 + 1;
    // 经验
    protected int storedExperience = 0;
    // 工作
    protected BlockPos workingPos = new BlockPos(0, 0, 0);
    protected int workTick = 0;
    // 睡眠
    protected int bedWakeCd = 0;
    protected Vec3 prevPos;
    // 其他
    protected boolean autoPick = false;
    public static final HashSet<Item> ARROW_ITEMS = new HashSet<>();
    protected int movePacketFreshTick = 0;
    protected int neoForgeSyncPacketTick = 0;
    protected boolean saveChanged = false;
    protected boolean lockSlot = false;
    protected boolean fairyFlight = false;
    private static final double FAIRY_MAX_ABOVE_GROUND = 2.0;
    private static final double FAIRY_MAX_ABOVE_TARGET = 2.0;
    public @Nullable NPCFishingHook fishing;
    public int pickItemTick = 20;

    static {
        ARROW_ITEMS.add(Items.ARROW);
        ARROW_ITEMS.add(Items.TIPPED_ARROW);
        ARROW_ITEMS.add(Items.SPECTRAL_ARROW);
    }

    protected final NPCBowAttackGoal<BaseNPCLikeEntity> bowAttackGoal = new NPCBowAttackGoal<>(this, 1.0, 20, 15.0f);
    protected final NPCCrossbowAttackGoal crossBowAttackGoal = new NPCCrossbowAttackGoal(this, 1.0, 20);
    protected final NPCDanmakuItemGoal<BaseNPCLikeEntity> danmakuItemGoal = new NPCDanmakuItemGoal<>(this, 1.0, 20, 15.0f);

    protected final NPCMeleeAttackGoal meleeAttackGoal = new NPCMeleeAttackGoal(this, 1.5, false) {
        @Override
        public void stop() {
            super.stop();
            BaseNPCLikeEntity.this.setAggressive(false);
        }

        @Override
        public void start() {
            super.start();
            BaseNPCLikeEntity.this.setAggressive(true);
        }
    };
    @Getter
    private final NPCFoodDataContainer foodDataContainer;
    @Getter
    private final ItemPickContainer itemPickContainer;

    public BaseNPCLikeEntity(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world);
        this.inventory = new NPCInventoryImpl(this, NPCInventoryImpl.MAX_SIZE);
        this.itemPickContainer = new ItemPickContainer(this, this.getRandom());
        this.foodDataContainer = new NPCFoodDataContainer(this, this.getRandom());
        this.init();
        this.updateAttackType();
        if (this.getSkinType() == null) {
            this.setSkinType(MobSkinTypes.DEFAULT);
        }
    }

    public BaseNPCLikeEntity(EntityType<? extends TamableAnimal> entityType, Level world, SkinType skinType) {
        this(entityType, world);
        this.setSkinType(skinType);
    }

    public void init() {
        AttributeMap attributeContainer = this.getAttributes();
        AttributeInstance scale = attributeContainer.getInstance(Attributes.SCALE);
        if (scale != null) {
            scale.setBaseValue(0.9);
        }
        this.setNoGravity(false);
        this.setCanPickUpLoot(true);
        this.setTame(false, false);
        this.setCanPickUpLoot(true);

        this.setPathfindingMalus(PathType.FIRE_IN_NEIGHBOR, 16.0f);
        this.setPathfindingMalus(PathType.FIRE_IN_NEIGHBOR, -1.0f);
    }

    /**
     * Enables three-dimensional fairy flight. Call after the subclass constructor
     * has completed, so the level and attributes are available.
     */
    protected void enableFairyFlight() {
        this.fairyFlight = true;
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.navigation = new FlyingPathNavigation(this, this.level());
        this.setNoGravity(true);
    }

    /**
     * Fairies should hover near the ground, or near their current target.
     * Without this, FlyingPathNavigation can keep them at a high Y after combat
     * and the player cannot reach them with a melee attack.
     */
    private void limitFairyFlightHeight() {
        LivingEntity target = this.getTarget();
        double maxY;
        if (target != null && target.isAlive()) {
            maxY = target.getY() + FAIRY_MAX_ABOVE_TARGET;
        } else {
            // Tamed fairies should still be able to follow their owner onto
            // hills and platforms instead of being pulled back to the ground.
            LivingEntity owner = this.getOwner();
            if (owner != null && owner.isAlive() && this.distanceToSqr(owner) <= 64.0 * 64.0) {
                maxY = owner.getY() + FAIRY_MAX_ABOVE_TARGET;
            } else {
                BlockPos pos = this.blockPosition();
                int groundY = this.level().getHeight(
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        pos.getX(),
                        pos.getZ()
                );
                if (groundY <= this.level().getMinY()) {
                    return;
                }
                maxY = groundY + FAIRY_MAX_ABOVE_GROUND;
            }
        }

        if (this.getY() <= maxY + 0.5) {
            return;
        }

        // Stop a high flight path and pull the fairy down to the allowed band.
        this.getNavigation().stop();
        this.getMoveControl().setWantedPosition(
                this.getX(),
                maxY - 0.5,
                this.getZ(),
                1.0
        );
        Vec3 movement = this.getDeltaMovement();
        if (movement.y > -0.08) {
            this.setDeltaMovement(movement.x, -0.08, movement.z);
        }
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        if (this.fairyFlight) {
            this.resetFallDistance();
            return;
        }
        super.checkFallDamage(y, onGround, state, pos);
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource source) {
        if (this.fairyFlight) {
            this.resetFallDistance();
            return false;
        }
        return super.causeFallDamage(fallDistance, damageMultiplier, source);
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public boolean isWithinMeleeAttackRange(LivingEntity target) {
        boolean withinMeleeAttackRange = super.isWithinMeleeAttackRange(target);
        double maxRange = 3.5;
        double minRange = 0;
        LivingEntityAccessor accessor = (LivingEntityAccessor) target;
        if (!withinMeleeAttackRange) {
            AABB hitbox = accessor.reverie_dreams$getHitbox();
            return this.getAttackBoundingBox(maxRange).intersects(hitbox) && (minRange <= 0.0 || !this.getAttackBoundingBox(minRange).intersects(hitbox));
        }
        return withinMeleeAttackRange;
    }

    public boolean addItem(ItemStack itemStack) {
        NPCWorkInventory.insert(this.inventory, itemStack);
        return itemStack.isEmpty();
    }

    //    @Override
//    public boolean isShiftKeyDown() {
//        return this.getNPCState().equals(NPCStates.SNAKING);
//    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SKIN_TYPE, MobSkinTypes.DEFAULT);
        builder.define(DEFAULT_SKIN_TYPE, MobSkinTypes.UNDEFINED);
    }

    @Override
    public void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        this.swinging = view.getBooleanOr("IsSwing", false);
        this.sit = view.getBooleanOr("IsSit", false);
        this.foodDataContainer.readAdditionalSaveData(view);

        NPCState loadedState = NPCStates.get(Identifier.parse(view.getStringOr("NPCStateId", NPCState.DEFAULT_ID.toString())));
        NPCWorkMode loadedWorkMode = NPCWorkModes.get(Identifier.parse(view.getStringOr("NPCWorkStateId", NPCWorkMode.DEFAULT_ID.toString())));
        this.npcState = loadedState != null ? loadedState : NPCStates.NORMAL;
        this.workMode = loadedWorkMode != null ? loadedWorkMode : NPCWorkModes.COMBAT;
        // 重载后不要把保存的工作原点误判为“刚切换到 WORKING”。
        this.lastNPCState = this.npcState;
        this.npcOwner = view.getStringOr("NPCOwner", view.getStringOr("NpcOwner", view.getStringOr("Owner", "")));

        NPCInventoryImpl inventory = new NPCInventoryImpl(this, NPCInventoryImpl.MAX_SIZE);
        ContainerHelper.loadAllItems(view, inventory.getItems());

        this.autoPick = view.getBooleanOr("AutoPick", false);

        this.inventory = inventory;
        this.getWorkController().load(view);

        this.seatUUID = view.getStringOr("SeatUUID", "null");

        Optional<Long> workingPosOptional = view.getLong("WorkingPos");
        this.workingPos = workingPosOptional
                .map(BlockPos::of)
                .orElseGet(this::blockPosition);

        this.storedExperience = view.getIntOr("ExperienceAmount", 0);
        this.lockSlot = view.getBooleanOr("LockSlot", false);

        view.read("Skin", SkinType.MERGED_CODEC).ifPresent(this::setSkinType);
        view.read("DefaultSkinType", SkinType.MERGED_CODEC).ifPresent(this::setDefaultSkinType);
        this.updateAttackType();
    }

    @Override
    public void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        view.putBoolean("IsSwing", this.swinging);
        view.putBoolean("IsSit", this.sit);
        view.putString("NPCOwner", this.npcOwner);
        view.putString("NPCStateId", Optional.ofNullable(BuiltInRegistryProviders.NPC_STATE.getKey(this.npcState)).orElse(NPCState.DEFAULT_ID).toString());
        view.putString("NPCWorkStateId", Optional.ofNullable(BuiltInRegistryProviders.NPC_WORK_MODE.getKey(this.workMode)).orElse(NPCWorkMode.DEFAULT_ID).toString());
        this.foodDataContainer.addAdditionalSaveData(view);

        ContainerHelper.saveAllItems(view, this.inventory.getItems());
        this.getWorkController().save(view);
        view.putLong("WorkingPos", this.workingPos.asLong());

        view.putBoolean("AutoPick", this.autoPick);

        if (!this.seatUUID.isEmpty() && !this.seatUUID.equals("null")) {
            view.putString("SeatUUID", this.seatUUID);
        }

        view.putInt("ExperienceAmount", this.storedExperience);
        view.putBoolean("LockSlot", this.lockSlot);

        view.store("Skin", SkinType.MERGED_CODEC, this.getSkinType());
        view.store("DefaultSkinType", SkinType.MERGED_CODEC, this.getDefaultSkinType());
    }

    public NPCFoodDataContainer getFoodData() {
        return this.foodDataContainer;
    }

    public boolean isEnableTamableFeature() {
        return false;
    }

    @Override
    public void swing(InteractionHand hand, boolean updateSelf) {
        super.swing(hand, updateSelf);
        if (this instanceof GensokyoFairyEntity fairy) {
            fairy.fairyActions().begin(GensokyoFairyAnimation.MELEE, this.tickCount);
        }
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (this instanceof GensokyoFairyEntity fairy) {
            if (event == GensokyoFairyActions.RANGED_EVENT) {
                fairy.fairyActions().begin(GensokyoFairyAnimation.RANGED, this.tickCount);
                return;
            }
            if (event == 60) return;
        }
        super.handleEntityEvent(event);
    }

    @Override
    protected void tickDeath() {
        if (this instanceof GensokyoFairyEntity
                && !GensokyoFairyActions.advanceDeath(this)) return;
        super.tickDeath();
    }

    public void sendForceSyncPacket() {
        if (!this.level().isClientSide()) {
            MinecraftServer server = this.level().getServer();
            if (server == null) {
                return;
            }
            CompoundTag data = this.writeUpdateTag();
            if (data == null) return;
            final var packet = new SyncEntityPacket(this.getId(), data);
            if (ReverieDreams.getServer() != null) {
                NetworkManager.sendToPlayers(ReverieDreams.getServer().getPlayerList().getPlayers().stream().filter(player -> Objects.equals(player.level().dimension(), this.level().dimension())).toList(), packet);
            }
        }
    }

    public @Nullable CompoundTag writeUpdateTag() {
        return NPCSyncSnapshot.encode(this.getUUID(), this.registryAccess(), this::addAdditionalSaveData);
    }

    @Override
    public void stopSleeping() {
        super.stopSleeping();
        this.bedWakeCd = 20 * 5;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new AvoidCreeperExplosionEntityGoal(this, 6.0F, (double) 1.0F, 1.2));
        this.goalSelector.addGoal(1, new AvoidFireDamageEntityGoal(this, 6.0F, (double) 1.0F, 1.2));
        this.goalSelector.addGoal(3, new NPCSpearUseGoal<>(this, 1.0, 1.0, 10.0F, 2.0F));
    }

    public void addExperience(int xp) {
        this.storedExperience += xp;
    }

    @Override
    public ItemStack getProjectile(ItemStack stack) {
        if (stack.getItem() instanceof ProjectileWeaponItem) {
            Predicate<ItemStack> predicate = ((ProjectileWeaponItem) stack.getItem()).getSupportedHeldProjectiles();
            ItemStack itemStack = ProjectileWeaponItem.getHeldProjectile(this, predicate);
            return itemStack.isEmpty() ? new ItemStack(Items.ARROW) : itemStack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float pullProgress) {
        ItemStack itemStack = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this,
                this.inventory.findHand((stack -> stack.is(Items.BOW) || stack.getItem() instanceof BowItem)) != null ? Items.BOW : Items.CROSSBOW));
//        ItemStack itemStack2 = this.getProjectileType(itemStack);

        if (itemStack.getItem() instanceof CrossbowItem) {
            ChargedProjectiles chargedProjectilesComponent = itemStack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
            if (chargedProjectilesComponent != null) {
                for (ItemStack projStack : chargedProjectilesComponent.itemCopies()) {
                    Projectile projectile = this.createArrowProjectile(projStack, 3.15f, itemStack);
                    shoot(target, projStack, projectile);
                    shoot(target, projStack, projectile);
                }
            }
//            shoot(this,3.15f);
            //CrossBowItem L86 玩家射箭的箭矢速度
            return;
        } else {
            ItemStack arrow = RangedAttackUtil.getArrowStack(this);
            if (arrow != null) {
                Projectile projectile = this.createArrowProjectile(arrow, pullProgress, itemStack);
                arrow.shrink(1);
                shoot(target, arrow, projectile);
            }
        }


    }

    public void setWorkMode(NPCWorkMode workMode) {
        if (!NPCWorkModes.canAssign(workMode) || Objects.equals(this.workMode, workMode)) {
            return;
        }

        this.getWorkController().reset();
        this.workMode.onStop(this);
        this.workMode = workMode;
        this.workMode.onStarted(this);
    }

    public NPCState getNPCState() {
        return this.npcState;
    }

    public void setNPCState(NPCState npcState) {
        if (Objects.equals(this.npcState, npcState)) {
            return;
        }

        this.getWorkController().reset();
        this.npcState.onStop(this);
        this.npcState = npcState;
        this.npcState.onStarted(this);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target != null && this.npcState == NPCStates.WORKING && this.workMode == NPCWorkModes.COMBAT
                && !this.getWorkController().area().contains(target.blockPosition())) target = null;
        super.setTarget(target);
    }

    private void shoot(Entity target, ItemStack arrow, Projectile arrowEntity) {
//        ItemStack arrow = RangedAttackUtil.getArrowStack(this);
//        if (arrow==null)return;

        //PersistentProjectileEntity persistentProjectileEntity = this.createArrowProjectile(arrow, pullProgress, itemStack);
        double d = target.getX() - this.getX();
        double e = target.getY(0.3333333333333333) - arrowEntity.getY();
        double f = target.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f);
        Level world = this.level();
        if (world instanceof ServerLevel serverWorld) {
            Projectile.spawnProjectileUsingShoot(arrowEntity, serverWorld, arrow, d, e + g * (double) 0.2f, f, 1.6f, 14 - serverWorld.getDifficulty().getId() * 4);
        }
        this.level().playSound(null, this.getOnPos(), SoundEvents.SKELETON_SHOOT, SoundSource.NEUTRAL, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
    }


    protected Projectile createArrowProjectile(ItemStack arrow, float damageModifier, @Nullable ItemStack shotFrom) {
        if (arrow.getItem() instanceof FireworkRocketItem)
            return new FireworkRocketEntity(this.level(), arrow, this, this.getX(), this.getEyeY() - 0.15F, this.getZ(), true);
        return ProjectileUtil.getMobArrow(this, arrow, damageModifier, shotFrom);
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        Level world = this.level();
        if (this.storedExperience > 0) {
            ExperienceOrb orbEntity = new ExperienceOrb(world, this.position(), this.getDeltaMovement(), this.storedExperience);
            world.addFreshEntity(orbEntity);
            // 经验已经以经验球形式归还，档案里不能再保存一份。
            this.storedExperience = 0;
        }
        KeepInventoryTypes keepInventoryType = this.getKeepInventoryType();
        if (keepInventoryType == KeepInventoryTypes.ARCHIVED) {
            ItemStack archive = this.toArchive();
            ItemEntity itemEntity = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), archive);
            world.addFreshEntity(itemEntity);
        } else if (keepInventoryType == KeepInventoryTypes.DROP_ALL_ITEM) {
            for (int i = 0; i < this.inventory.getContainerSize(); i++) {
                if (this.getDonDropSlotIndex().contains(i))
                    continue;
                ItemStack copiedStack = this.inventory.getItem(i).copy();
                ItemEntity itemEntity = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), copiedStack);
                world.addFreshEntity(itemEntity);
            }
            List<ItemStack> stacks = List.of(
                    this.inventory.getHead(),
                    this.inventory.getChest(),
                    this.inventory.getLegs(),
                    this.inventory.getFeet()
            );
            for (ItemStack stack : stacks) {
                ItemStack copiedStack = stack.copy();
                ItemEntity itemEntity = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), copiedStack);
                world.addFreshEntity(itemEntity);
            }
        } else if (keepInventoryType == KeepInventoryTypes.ONLY_HAND_AND_ARMOR) {
            List<ItemStack> stacks = List.of(
                    this.inventory.getMainHand(),
                    this.inventory.getOffHand(),
                    this.inventory.getHead(),
                    this.inventory.getChest(),
                    this.inventory.getLegs(),
                    this.inventory.getFeet()
            );
            for (ItemStack stack : stacks) {
                ItemStack copiedStack = stack.copy();
                ItemEntity itemEntity = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), copiedStack);
                world.addFreshEntity(itemEntity);
            }
        } else if (keepInventoryType == KeepInventoryTypes.ONLY_ARMOR) {
            List<ItemStack> stacks = List.of(
                    this.inventory.getHead(),
                    this.inventory.getChest(),
                    this.inventory.getLegs(),
                    this.inventory.getFeet()
            );
            for (ItemStack stack : stacks) {
                ItemStack copiedStack = stack.copy();
                ItemEntity itemEntity = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), copiedStack);
                world.addFreshEntity(itemEntity);
            }
        }
//        System.out.println("death");
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel serverLevel, DamageSource damageSource) {
        if (this.getKeepInventoryType() != KeepInventoryTypes.NOT_DROP_ANY) {
            super.dropAllDeathLoot(serverLevel, damageSource);
        }
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel world, AgeableMob entity) {
        return null;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        var world = this.level();
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld && player instanceof ServerPlayer serverPlayerEntity) {

        }

        return super.mobInteract(player, hand);
    }

    @Deprecated
    public void setOwnerUuid(@Nullable UUID uuid) {
        if (uuid != null) {
            this.npcOwner = uuid.toString();
            this.setTame(true, true);
        }
    }

    @Override
    public void setOwner(LivingEntity player) {
        if (player != null) {
            this.npcOwner = player.getUUID().toString();
        }
        this.setTame(true, true);
        if (player instanceof ServerPlayer serverPlayerEntity) {
            CriteriaTriggers.TAME_ANIMAL.trigger(serverPlayerEntity, this);
        }
    }

    @Override
    public boolean isOwnedBy(LivingEntity entity) {
        return entity.getUUID().toString().equalsIgnoreCase(this.npcOwner);
    }

    @Override
    public boolean canPickUpLoot() {
        return this.canPickItem();
    }

    @Override
    public void swing(InteractionHand hand) {
        super.swing(hand);
    }

    @Override
    public void aiStep() {
        this.updateSwingTime();
        this.updateBob();
        Level world = this.level();
        if (world instanceof ServerLevel serverWorld) {
            if (this.isSleeping())
                return;
            if (this.canPickUpLoot() && this.isAlive()) {
                Vec3i vec3i = this.getPickupReach();
                List<ItemEntity> list = this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(vec3i.getX(), vec3i.getY(), vec3i.getZ()));
                for (ItemEntity itemEntity : list) {
                    if (itemEntity.isRemoved() || itemEntity.getItem().isEmpty() || itemEntity.hasPickUpDelay() || !this.wantsToPickUp(serverWorld, itemEntity.getItem()))
                        continue;
                    this.pickUpItem(serverWorld, itemEntity);
                }
            }
            this.hurtMarked = true;
        }
        super.aiStep();
        if (this.fairyFlight) {
            // Sitting or leashed fairies return to the ground; otherwise they hover.
            this.setNoGravity(!(this.isOrderedToSit() || this.isLeashed()));
            if (!this.level().isClientSide()
                    && !this.isOrderedToSit()
                    && !this.isLeashed()
                    && this.tickCount % 5 == 0) {
                this.limitFairyFlightHeight();
            }
        }
        this.polymerMovPacketTick();
    }

    protected void polymerMovPacketTick() {
        if (PlatformContext.hasPolymer()) {
            if (this.movePacketFreshTick >= 1) {
                if (this.level() instanceof ServerLevel serverWorld) {
                    ChunkMap.TrackedEntity tracker = ((ServerChunkLoadingManagerAccessor) serverWorld.getChunkSource().chunkMap).getEntityTrackerMap().get(this.getId());
                    if (tracker != null) {
                        Set<ServerPlayerConnection> listeners = ((EntityTrackerAccessor) tracker).getListenerSet();
                        for (var handler : listeners) {
                            ServerPlayer player = handler.getPlayer();
                            if (!player.hasDisconnected() && player.isAlive()) {
                                ServerEntity entry = ((EntityTrackerAccessor) tracker).getTrackEntry();
                                entry.sendPairingData(handler.getPlayer(), packets -> {
                                    entry.sendDirtyEntityData();
                                    ClientboundEntityPositionSyncPacket syncPacket = ClientboundEntityPositionSyncPacket.of(this);
                                    entry.sendChanges();
                                    ServerEntity.Synchronizer synchronizer = ((ServerEntityAccessor) entry).reverie_dreams$getSynchronizer();
                                    synchronizer.sendToTrackingPlayers(syncPacket);
                                    player.connection.send(syncPacket);
                                });
                            }
                        }
                    }
                }
                this.movePacketFreshTick = 0;
            } else {
                this.movePacketFreshTick++;
            }
        }
    }

    @Override
    public void rideTick() {
        super.rideTick();
        this.avatarState().resetBob();
    }

    protected void updateBob() {
        float f;
        if (this.onGround() && !this.isDeadOrDying() && !this.isSwimming()) {
            f = Math.min(0.1F, (float) this.getDeltaMovement().horizontalDistance());
        } else {
            f = 0.0F;
        }

        this.avatarState().updateBob(f);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        super.onSyncedDataUpdated(data);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        Object polymerEntity = PolymerEntityGetterCallback.get(this);
        if (polymerEntity instanceof CommonPolymerHolderEntity playerPolymerEntity) {
            playerPolymerEntity.onCreated();
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        Object polymerEntity = PolymerEntityGetterCallback.get(this);
        if (polymerEntity instanceof CommonPolymerHolderEntity playerPolymerEntity) {
            playerPolymerEntity.onTrackingStopped(player);
        }
    }

    @Override
    protected void pickUpItem(ServerLevel world, ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        boolean hands = this.getNPCState() != NPCStates.WORKING
                && !(this instanceof NPCSimpleEntity simple && simple.getSkillTask().active());
        int moved = NPCWorkInventory.pickup(this.inventory, itemStack, hands);
        if (moved <= 0) return;
        this.take(itemEntity, moved);
        if (itemStack.isEmpty()) itemEntity.discard();
        else itemEntity.setItem(itemStack);
    }

    protected void updateHealth() {
        if (this.level().isClientSide())
            return;
        if (this.getHealth() < this.getMaxHealth()) {
            this.healthTick--;
        } else {
//            this.healthTick = 10;
        }
        if (this.consumeHunger() && this.getHealth() < this.getMaxHealth() && this.healthTick <= 0) {
            if (this.isDeadOrDying())
                return;
            NPCFoodDataContainer foodData = this.getFoodData();
            if (this.getFoodData().getNutrition() == 20 && foodData.getSaturation() > 1) {
                this.setHealth(getHealth() + Math.min(1, foodData.getSaturation() / 6));
                this.healthTick = 10;
                foodData.setExhaustionLevel(foodData.getExhaustionLevel() + 6);
            } else if (foodData.getNutrition() >= 18) {
                this.setHealth(getHealth() + 1);
                this.healthTick = 80;
                foodData.setExhaustionLevel(foodData.getExhaustionLevel() + 6);
            } else if (foodData.getNutrition() == 0 && this.getHealth() > this.getMaxHealth() / 2) {
                this.hurtServer((ServerLevel) this.level(), this.damageSources().starve(), 1);
                this.healthTick = 80;
            }
        }
    }


    @SuppressWarnings("ConstantValue")
    protected void updateAttackType() {
        if (this.level() == null || this.level().isClientSide()) {
            return;
        }
        this.goalSelector.removeGoal(this.meleeAttackGoal);
        this.goalSelector.removeGoal(this.bowAttackGoal);
        this.goalSelector.removeGoal(this.crossBowAttackGoal);
        this.goalSelector.removeGoal(this.danmakuItemGoal);
//        ItemStack itemStack = this.getMainHandStack();

        if (RangedAttackUtil.getArrowStack(this) != null && (this.inventory.findHand((stack -> stack.is(Items.BOW) || stack.getItem() instanceof BowItem)) != null)) {
            int i = this.getRegularAttackInterval();
            this.bowAttackGoal.setAttackInterval(i);
            this.goalSelector.addGoal(4, this.bowAttackGoal);
        } else if (RangedAttackUtil.getCrossBowAmmoStack(this) != null && (this.inventory.findHand((stack -> stack.is(Items.CROSSBOW) || stack.getItem() instanceof CrossbowItem)) != null)) {
            this.goalSelector.addGoal(4, this.crossBowAttackGoal);
        } else if (RangedAttackUtil.isDanmakuInHand(this)) {
            this.goalSelector.addGoal(4, this.danmakuItemGoal);
        } else {
            this.goalSelector.addGoal(4, this.meleeAttackGoal);
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData entityData) {
        SpawnGroupData initialize = super.finalizeSpawn(world, difficulty, spawnReason, entityData);
        this.updateAttackType();
        return initialize;
    }

    protected int getHardAttackInterval() {
        return 20;
    }

    protected int getRegularAttackInterval() {
        return 40;
    }

    public NPCState getNextState() {
        if (this.isSleeping())
            return this.npcState;
        int rawId = BuiltInRegistryProviders.NPC_STATE.getId(this.npcState);
        NPCState next = NPCStates.fromInt(rawId + 1);
        return next != null ? next : NPCStates.fromInt(0);
    }

    public NPCState getPreviousState() {
        if (this.isSleeping())
            return this.npcState;
        int rawId = BuiltInRegistryProviders.NPC_STATE.getId(this.npcState);
        NPCState next = NPCStates.fromInt(rawId - 1);
        Map<Integer, Holder.Reference<NPCState>> rawToEntry = BuiltInRegistryProviders.NPC_STATE.getIdToEntryMap();
        int maxKey = Collections.max(rawToEntry.keySet());
        return next != null ? next : NPCStates.fromInt(maxKey);
    }

    /** A bounded work goal may navigate its own target without competing with the home tether. */
    protected boolean isNavigatingWorkTarget() {
        return this.getWorkController().navigating();
    }

    public void updateWorking() {
        if (this.npcState != this.lastNPCState && this.npcState == NPCStates.WORKING) {
            this.workingPos = new BlockPos(this.getBlockX(), (int) Math.round(this.getY()), this.getBlockZ());
        }

        if (this.npcState == NPCStates.WORKING && this.workTick < 20) {
            this.workTick++;
        } else {
            this.workTick = 0;
        }
        if (this.npcState == NPCStates.WORKING && this.workTick >= 20) {
            this.workTick = 0;
            BlockPos blockPos = this.blockPosition();
            if (this.workingPos != null) {
                double distance = blockPos.distSqr(this.workingPos);

                if (!this.getWorkController().area().contains(blockPos) && !this.isNavigatingWorkTarget()) {
                    boolean success = this.getNavigation().moveTo(
                            this.workingPos.getX() + 0.5,
                            this.workingPos.getY(),
                            this.workingPos.getZ() + 0.5,
                            1.0D
                    );
                }
            }
        }
    }

    public void updateName() {
        this.setCustomNameVisible(this.hasCustomName());
    }

    public void fixPitchYaw() {
        if (PlatformContext.isFabric() && PlatformContext.hasPolymer()) {
            float delta = Math.abs(this.yBodyRot - this.getYRot());
            if (delta > 20.0f) {
                this.setYBodyRot(this.getYRot());
            }
        }
    }

    @Override
    public void tick() {
        Level world = this.level();
        if (!world.isClientSide()) {
            this.getWorkController().sync();
            if (this.npcState == NPCStates.WORKING && this.workMode == NPCWorkModes.COMBAT && this.getTarget() != null
                    && !EntityTargetUtil.canAttack(this.getTarget(), this)) {
                this.setTarget(null);
                this.getNavigation().stop();
            }
        }
        this.updateHealth();
        this.foodDataContainer.tick();
        this.foodDataContainer.updateHunger();
        this.foodDataContainer.updateHungerConsumption();
        this.itemPickContainer.tick();
        this.updateWorking();
        this.updateName();
//        this.fixPitchYaw();
//        this.neoForgePacketUpdate();
        if (!this.getNPCState().equals(NPCStates.SEATED)) {
            this.setOrderedToSit(false);
        }
        this.updateAttackTick++;
        if (this.updateAttackTick > this.maxUpdateAttackTick) {
            this.updateAttackType();
            this.updateAttackTick = 0;
        }
        this.prevPos = this.position();

        if (this.npcState == NPCStates.SNAKING) {
            this.getNavigation().stop();
            if (!world.isClientSide() && !this.isShiftKeyDown()) {
                this.setShiftKeyDown(true);
            }
            if (!world.isClientSide() && this.getPose() != Pose.CROUCHING) {
                this.setPose(Pose.CROUCHING);
            }
            super.tick();
            return;
        }

        if (this.npcState == NPCStates.SEATED) {
            if (this.seat == null) {
                List<ArmorStand> list = world.getEntitiesOfClass(
                                ArmorStand.class,
                                new AABB(this.getX() + 1, this.getY() + 1, this.getZ() + 1, this.getX() - 1, this.getY() - 1, this.getZ() - 1),
                                entity -> true)
                        .stream()
                        .filter(entity -> entity.getUUID().toString().equalsIgnoreCase(this.seatUUID))
                        .toList();
                if (!list.isEmpty()) {
                    this.seat = list.getFirst();
                } else {
                    this.spawnSeatAndSit();
                }
            }
            this.setSit(true);
            super.tick();
            return;
        } else {
            if (this.seat != null) {
                this.seat.discard();
                this.seatUUID = "";
                this.seat = null;
            }
            this.setSit(false);
        }
        if (!world.isClientSide() && this.getPose() == Pose.CROUCHING && !(this.getNPCState().equals(NPCStates.SNAKING))) {
            this.setPose(Pose.STANDING);
        }
        this.lastNPCState = this.npcState;
        this.avatarState.tick(this.position(), this.getDeltaMovement());

        super.tick();
    }

    private void neoForgePacketUpdate() {
        if (true) {
            return;
        }
        if (PlatformContext.isFabric()) {
            return;
        }
        if (this.neoForgeSyncPacketTick >= 3) {
            this.sendForceSyncPacket();
            this.neoForgeSyncPacketTick = 0;
        } else {
            this.neoForgeSyncPacketTick++;
        }
    }

    private void spawnSeatAndSit() {
        ArmorStand as = EntityType.ARMOR_STAND.create(this.level(), EntitySpawnReason.TRIGGERED);
        if (as == null)
            return;

        as.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        as.setInvisible(true);
        as.setNoGravity(true);
        as.setMarker(true);
        this.level().addFreshEntity(as);

        this.seat = as;
        this.seatUUID = this.seat.getUUID().toString();
        this.startRiding(as, true, true);
    }

    @Override
    public void move(MoverType type, Vec3 pos) {
        double d0 = this.getX();
        double d1 = this.getZ();
        super.move(type, pos);
        float f = (float) (this.getX() - d0);
        float f1 = (float) (this.getZ() - d1);
//        this.updateAutoJump(f, f1);
        this.addWalkedDistance(Mth.length(f, f1) * 0.6F);
    }

    public void addWalkedDistance(float walkDistance) {
        this.avatarState.addWalkDistance(walkDistance);
    }

    @Override
    public float getSpeed() {
        if (this.npcState == NPCStates.NO_WALK || this.npcState == NPCStates.SNAKING)
            return 0;
        if (this.paused)
            return 0;
        return super.getSpeed();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        if (this.isDeadOrDying()) {
            return false;
        }
        return super.hurtServer(world, source, amount);
    }

    @Override
    public void hurtArmor(DamageSource source, float amount) {
        if (this.canDamageEquipment()) {
            this.doHurtEquipment(source, amount, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD);
        }
    }

    @Override
    public void hurtHelmet(DamageSource source, float amount) {
        if (this.canDamageEquipment()) {
            this.doHurtEquipment(source, amount, EquipmentSlot.HEAD);
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel world, Entity target) {
        boolean result = super.doHurtTarget(world, target);
        if (result) {
            ItemStack mainHand = this.getMainHandItem();
            if (mainHand.isDamageableItem()) {
                mainHand.hurtAndBreak(1, this, InteractionHand.MAIN_HAND);
            }
        }
        return result;
    }

    public RoleFollowerArchive toArchiveComponent() {
        CompoundTag nbtCompound;
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(this.problemPath(), LogUtils.getLogger())) {
            TagValueOutput view = TagValueOutput.createWithContext(logging, this.registryAccess());
            this.addAdditionalSaveData(view);
            nbtCompound = view.buildResult();
        }
        return new RoleFollowerArchive(this.getName(), this.getMaxHealth(), nbtCompound);
    }

    public ItemStack toArchive() {
        ItemStack itemStack = RDItems.ROLE_ARCHIVE.createStack();
        MutableComponent mutableComponent = Component.empty();
        mutableComponent.append(itemStack.getItemName()).append("(").append(this.getName()).append(")");
        itemStack.set(DataComponents.ITEM_NAME, mutableComponent);
        itemStack.set(RDDataComponentTypes.ROLE_FOLLOWER_ARCHIVE.value(), this.toArchiveComponent());
        itemStack.set(RDDataComponentTypes.ROLE_CAN_RESPAWN.value(), false);
        return itemStack;
    }

    //    @Override
    public Iterable<ItemStack> getArmorItems() {
        return List.of(
                this.getItemBySlot(EquipmentSlot.HEAD),
                this.getItemBySlot(EquipmentSlot.CHEST),
                this.getItemBySlot(EquipmentSlot.LEGS),
                this.getItemBySlot(EquipmentSlot.FEET)
        );
    }

    @Override
    public ItemStack getItemHeldByArm(HumanoidArm arm) {
        if (arm == HumanoidArm.RIGHT) {
            return this.inventory.getMainHand();
        }
        if (arm == HumanoidArm.LEFT) {
            return this.inventory.getOffHand();
        }
        return super.getItemHeldByArm(arm);
    }

    @Override
    public ItemStack getMainHandItem() {
        ItemStack stack = this.inventory.getItem(NPCInventoryImpl.MAIN_HAND);
        if (stack.isEmpty())
            return super.getMainHandItem();
        return stack;
    }

    @Override
    public ItemStack getOffhandItem() {
        ItemStack stack = this.inventory.getItem(NPCInventoryImpl.OFF_HAND);
        if (stack.isEmpty())
            return super.getOffhandItem();
        return stack;
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        if (slot == EquipmentSlot.HEAD) {
            return this.inventory.getHead();
        } else if (slot == EquipmentSlot.CHEST) {
            return this.inventory.getChest();
        } else if (slot == EquipmentSlot.LEGS) {
            return this.inventory.getLegs();
        } else if (slot == EquipmentSlot.FEET) {
            return this.inventory.getFeet();
        } else if (slot == EquipmentSlot.MAINHAND) {
            return this.inventory.getItem(NPCInventoryImpl.MAIN_HAND);
        } else if (slot == EquipmentSlot.OFFHAND) {
            return this.inventory.getItem(NPCInventoryImpl.OFF_HAND);
        }
        return super.getItemBySlot(slot);
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        super.setItemSlot(slot, stack);
        int idx = switch (slot) {
            case MAINHAND -> NPCInventoryImpl.MAIN_HAND;
            case OFFHAND -> NPCInventoryImpl.OFF_HAND;
            case HEAD -> -11;
            case CHEST -> -12;
            case LEGS -> -13;
            case FEET -> -14;
            default -> -1;
        };
        if (idx >= 0)
            this.inventory.setItem(idx, stack);
        if (-14 <= idx && idx <= -11) {
            if (idx == -11) {
                this.inventory.setHead(stack);
            }
            if (idx == -12) {
                this.inventory.setChest(stack);
            }
            if (idx == -13) {
                this.inventory.setLegs(stack);
            }
            if (idx == -14) {
                this.inventory.setFeet(stack);
            }
        }
        if (!this.level().isClientSide()) {
            this.updateAttackType();
        }
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    public static AttributeSupplier.Builder createLivingAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.1)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.TEMPT_RANGE, 10.0)
                .add(Attributes.ENTITY_INTERACTION_RANGE, 3);
    }

    public boolean isOwner(Entity player) {
        return this.getOwner() == player;
    }

    public boolean isAllowOpenInventory(ServerPlayer player) {
        return ((this.isOwnedBy(player) || (player.isCreative())) && this.isTame());
    }

    public String getNPCOwner() {
        return this.npcOwner;
    }

    public void setNPCOwner(String npcOwner) {
        this.npcOwner = npcOwner;
    }

    @Override
    public @Nullable LivingEntity getOwner() {
        if (this.npcOwner.equalsIgnoreCase(""))
            return null;
        Player playerByUUID = this.level().getPlayerByUUID(UUID.fromString(this.npcOwner));
        MinecraftServer server = this.level().getServer();
        if (server == null) {
            return playerByUUID;
        }
        return playerByUUID;
    }

    public @Nullable UUID getOwnerUuid() {
        if (this.npcOwner.equalsIgnoreCase(""))
            return null;
        try {
            return UUID.fromString(this.npcOwner);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isAlliedTo(BaseNPCLikeEntity other) {
        if (!this.isTame() || !other.isTame()) {
            return false;
        }
        if (this.getOwnerUuid() == null || other.getOwnerUuid() == null) {
            return false;
        }
        return Objects.equals(this.getOwnerUuid(), other.getOwnerUuid());
    }

    @Override
    public boolean isOrderedToSit() {
        return this.isSit();
    }

    @Override
    public boolean isTame() {
        return this.getOwnerUuid() != null;
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
    }

    @Override
    public void onClientRemoval() {
        super.onClientRemoval();
    }

    @Override
    public Property getSkin() {
        return this.getSkinType().getProperty();
    }

    public void setSkinType(SkinType type) {
        this.getEntityData().set(SKIN_TYPE, type);
    }

    public SkinType getSkinType() {
        return this.getEntityData().get(SKIN_TYPE);
    }

    public void setDefaultSkinType(SkinType type) {
        this.getEntityData().set(DEFAULT_SKIN_TYPE, type);
    }

    public SkinType getDefaultSkinType() {
        SkinType skinType = this.getEntityData().get(DEFAULT_SKIN_TYPE);
        if (Objects.equals(skinType, MobSkinTypes.UNDEFINED)) {
            skinType = this.getSkinType();
            this.setDefaultSkinType(skinType);
        }
        return skinType;
    }
}
