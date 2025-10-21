package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.polymer.entity.PlayerPolymerEntity;
import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.component.RoleFollowerArchive;
import cc.thonly.reverie_dreams.entity.ai.goal.attack.NPCBowAttackGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.attack.NPCCrossbowAttackGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.attack.NPCDanmakuItemGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.attack.RangedAttackUtil;
import cc.thonly.reverie_dreams.entity.skin.MobSkins;
import cc.thonly.reverie_dreams.entity.skin.NPCSkin;
import cc.thonly.reverie_dreams.inventory.NPCInventoryImpl;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.mixin.accessor.EntityTrackerAccessor;
import cc.thonly.reverie_dreams.mixin.accessor.ServerChunkLoadingManagerAccessor;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import cc.thonly.reverie_dreams.util.item.ItemUtils;
import com.google.common.collect.ImmutableList;
import com.mojang.authlib.properties.Property;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.*;
import net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Getter
@Setter
public abstract class NPCEntityImpl extends AbstractNPCEntity implements RangedAttackMob, NPCSettings {
    // 皮肤
    protected Supplier<Property> skin;
    // 实体信息
    protected NPCState npcState = NPCStates.NORMAL;
    protected NPCState lastNpcState = NPCStates.NORMAL;
    protected NPCWorkMode workMode = NPCWorkModes.COMBAT;
    protected boolean sit = false;
    protected String npcOwner = "";
    protected String seatUUID = "";
    protected ArmorStandEntity seat;
    protected boolean paused = false;
    // 背包
    protected NPCInventoryImpl inventory = new NPCInventoryImpl(NPCInventoryImpl.MAX_SIZE);
    // 回血
    protected int healthTick = 20;
    protected int maxHealthTick = 20 * 8;
    // 攻击tick
    protected int updateAttackTick = 0;
    protected int maxUpdateAttackTick = 20 * 2 + 1;
    // 饥饿
    protected int nutrition = 20;
    protected int saturation = 20;//饱食
    protected float exhaustionLevel = 0;//消耗
    protected int hungerTick = 20;
    // 经验
    protected int storedExperience = 0;
    // 工作
    protected BlockPos workingPos = new BlockPos(0, 0, 0);
    protected int workTick = 0;
    // 睡眠
    protected int bedWakeCd = 0;
    protected Vec3d prevPos;
    protected int freshTick = 0;
    // 其他
    protected boolean autoPick = false;
    public static final HashSet<Item> ARROW_ITEMS = new HashSet<>();

    static {
        ARROW_ITEMS.add(Items.ARROW);
        ARROW_ITEMS.add(Items.TIPPED_ARROW);
        ARROW_ITEMS.add(Items.SPECTRAL_ARROW);
    }

    private final NPCBowAttackGoal<NPCEntityImpl> bowAttackGoal = new NPCBowAttackGoal<>(this, 1.0, 20, 15.0f);
    private final NPCCrossbowAttackGoal crossBowAttackGoal = new NPCCrossbowAttackGoal(this, 1.0, 20);
    private final NPCDanmakuItemGoal<NPCEntityImpl> danmakuItemGoal = new NPCDanmakuItemGoal<>(this, 1.0, 20, 15.0f);

    private final MeleeAttackGoal meleeAttackGoal = new MeleeAttackGoal(this, 1.5, false) {
        @Override
        public void stop() {
            super.stop();
            NPCEntityImpl.this.setAttacking(false);
        }

        @Override
        public void start() {
            super.start();
            NPCEntityImpl.this.setAttacking(true);
        }
    };
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(MemoryModuleType.DOORS_TO_CLOSE, MemoryModuleType.NEAREST_BED);
    private static final ImmutableList<SensorType<? extends Sensor<? super NPCEntityImpl>>> SENSORS = ImmutableList.of();

    public NPCEntityImpl(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
        this.skin = MobSkins.DEFAULT::get;
        this.init();
        this.updateAttackType();
    }

    public NPCEntityImpl(EntityType<? extends TameableEntity> entityType, World world, NPCSkin skinSupplier) {
        this(entityType, world, skinSupplier::get);
    }

    public NPCEntityImpl(EntityType<? extends TameableEntity> entityType, World world, Supplier<Property> skin) {
        super(entityType, world);
        this.skin = skin;
        this.init();
        this.updateAttackType();
    }

    protected Brain.Profile<NPCEntityImpl> createBrainProfile() {
        return Brain.createProfile(MEMORY_MODULES, SENSORS);
    }

    public void init() {
        AttributeContainer attributeContainer = this.getAttributes();
        if (attributeContainer != null) {
            EntityAttributeInstance scale = attributeContainer.getCustomInstance(EntityAttributes.SCALE);
            if (scale != null) {
                scale.setBaseValue(0.9);
            }
        }
        this.setNoGravity(false);
        this.setCanPickUpLoot(true);
        this.setTamed(false, false);
        this.setCanPickUpLoot(true);

        this.setPathfindingPenalty(PathNodeType.DANGER_FIRE, 16.0f);
        this.setPathfindingPenalty(PathNodeType.DAMAGE_FIRE, -1.0f);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
    }

    @Override
    public void readCustomData(ReadView view) {
        super.readCustomData(view);
        DynamicRegistryManager registryManager = this.getRegistryManager();

        this.sit = view.getBoolean("IsSit", false);

        this.npcState = NPCStates.get(Identifier.of(view.getString("NPCStateId", NPCState.DEFAULT_ID.toString())));
        this.workMode = NPCWorkModes.get(Identifier.of(view.getString("NPCWorkStateId", NPCWorkMode.DEFAULT_ID.toString())));
        this.npcOwner = view.getString("NpcOwner", "");

        NPCInventoryImpl inventory = new NPCInventoryImpl(NPCInventoryImpl.MAX_SIZE);
        Inventories.readData(view, inventory.heldStacks);

        view.getBoolean("AutoPick", false);

        this.inventory = inventory;

        this.seatUUID = view.getString("SeatUUID", "null");

        this.nutrition = view.getInt("FoodNutrition", 20);
        this.saturation = view.getInt("FoodSaturation", 20);

        this.exhaustionLevel = view.getInt("FoodExhaustionLevel", 0);
        Optional<Long> workingPosOptional = view.getOptionalLong("WorkingPos");
        this.workingPos = workingPosOptional
                .map(BlockPos::fromLong)
                .orElseGet(() -> BlockPos.fromLong(new BlockPos(0, 0, 0).asLong()));

        this.storedExperience = view.getInt("ExperienceAmount", 0);

        this.updateAttackType();
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putBoolean("IsSit", this.sit);
        view.putString("NpcOwner", this.npcOwner);
        view.putString("NPCStateId", Optional.ofNullable(RegistryManager.NPC_STATE.getId(this.npcState)).orElse(NPCState.DEFAULT_ID).toString());
        view.putString("NPCWorkStateId", Optional.ofNullable(RegistryManager.NPC_WORK_MODE.getId(this.workMode)).orElse(NPCWorkMode.DEFAULT_ID).toString());
        view.putFloat("FoodNutrition", this.nutrition);
        view.putFloat("FoodSaturation", this.saturation);
        view.putFloat("FoodExhaustionLevel", this.exhaustionLevel);

        Inventories.writeData(view, this.inventory.heldStacks);

        view.putLong("WorkingPos", this.workingPos.asLong());

        view.putBoolean("AutoPick", this.autoPick);

        if (!this.seatUUID.isEmpty() && !this.seatUUID.equals("null")) {
            view.putString("SeatUUID", this.seatUUID);
        }

        view.putInt("ExperienceAmount", this.storedExperience);
    }

    @Override
    public void wakeUp() {
        super.wakeUp();
        this.bedWakeCd = 20 * 5;
    }

    @Override
    protected void initGoals() {
        super.initGoals();
    }

    public void addExperience(int xp) {
        this.storedExperience += xp;
    }

    @Override
    public ItemStack getProjectileType(ItemStack stack) {
        if (stack.getItem() instanceof RangedWeaponItem) {
            Predicate<ItemStack> predicate = ((RangedWeaponItem) stack.getItem()).getHeldProjectiles();
            ItemStack itemStack = RangedWeaponItem.getHeldProjectile(this, predicate);
            return itemStack.isEmpty() ? new ItemStack(Items.ARROW) : itemStack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        ItemStack itemStack = this.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this,
                this.inventory.findHand((stack -> stack.isOf(Items.BOW) || stack.getItem() instanceof BowItem)) != null ? Items.BOW : Items.CROSSBOW));
//        ItemStack itemStack2 = this.getProjectileType(itemStack);

        if (itemStack.getItem() instanceof CrossbowItem) {
            ChargedProjectilesComponent chargedProjectilesComponent = itemStack.set(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.DEFAULT);
            if (chargedProjectilesComponent != null) {
                for (ItemStack projStack : chargedProjectilesComponent.getProjectiles()) {
                    ProjectileEntity projectile = this.createArrowProjectile(projStack, 3.15f, itemStack);
                    shoot(target, projStack, projectile);
                    shoot(target, projStack, projectile);
                }
            }
//            shoot(this,3.15f);
            //CrossBowItem L86 玩家射箭的箭矢速度
            return;
        } else {
            ItemStack arrow = RangedAttackUtil.getArrowStack(this);
            ProjectileEntity projectile = this.createArrowProjectile(arrow, pullProgress, itemStack);
            arrow.decrement(1);
            shoot(target, arrow, projectile);
        }


    }

    private void shoot(Entity target, ItemStack arrow, ProjectileEntity arrowEntity) {
//        ItemStack arrow = RangedAttackUtil.getArrowStack(this);
//        if (arrow==null)return;

        //PersistentProjectileEntity persistentProjectileEntity = this.createArrowProjectile(arrow, pullProgress, itemStack);
        double d = target.getX() - this.getX();
        double e = target.getBodyY(0.3333333333333333) - arrowEntity.getY();
        double f = target.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f);
        World world = this.getWorld();
        if (world instanceof ServerWorld serverWorld) {
            ProjectileEntity.spawnWithVelocity(arrowEntity, serverWorld, arrow, d, e + g * (double) 0.2f, f, 1.6f, 14 - serverWorld.getDifficulty().getId() * 4);
        }
        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
    }


    protected ProjectileEntity createArrowProjectile(ItemStack arrow, float damageModifier, @Nullable ItemStack shotFrom) {
        if (arrow.getItem() instanceof FireworkRocketItem)
            return new FireworkRocketEntity(this.getWorld(), arrow, this, this.getX(), this.getEyeY() - 0.15F, this.getZ(), true);
        return ProjectileUtil.createArrowProjectile(this, arrow, damageModifier, shotFrom);
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);
        World world = this.getWorld();
        if (this.storedExperience > 0) {
            ExperienceOrbEntity orbEntity = new ExperienceOrbEntity(world, this.getPos(), this.getVelocity(), this.storedExperience);
            world.spawnEntity(orbEntity);
        }
        KeepInventoryTypes keepInventoryType = this.getKeepInventoryType();
        if (keepInventoryType == KeepInventoryTypes.ARCHIVED) {
            ItemStack archive = this.toArchive();
            ItemEntity itemEntity = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), archive);
            world.spawnEntity(itemEntity);
        } else if (keepInventoryType == KeepInventoryTypes.DROP_ALL_ITEM) {
            for (int i = 0; i < this.inventory.size(); i++) {
                if (this.getDonDropSlotIndex().contains(i)) continue;
                ItemStack copiedStack = this.inventory.getStack(i).copy();
                ItemEntity itemEntity = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), copiedStack);
                world.spawnEntity(itemEntity);
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
                world.spawnEntity(itemEntity);
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
                world.spawnEntity(itemEntity);
            }
        }
    }

    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        var world = this.getWorld();
        if (!world.isClient() && world instanceof ServerWorld serverWorld && player instanceof ServerPlayerEntity serverPlayerEntity) {

        }

        return super.interactMob(player, hand);
    }

    //    @Override
    public void setOwnerUuid(@Nullable UUID uuid) {
        if (uuid != null) {
            this.npcOwner = uuid.toString();
            this.setTamed(true, true);
        }
    }

    @Override
    public void setOwner(LivingEntity player) {
        if (player != null) {
            this.npcOwner = player.getUuid().toString();
        }
        this.setTamed(true, true);
        if (player instanceof ServerPlayerEntity serverPlayerEntity) {
            Criteria.TAME_ANIMAL.trigger(serverPlayerEntity, this);
        }
    }

    @Override
    public boolean isOwner(LivingEntity entity) {
        return entity.getUuid().toString().equalsIgnoreCase(this.npcOwner);
    }

    @Override
    public boolean canPickUpLoot() {
        return this.canPickItem();
    }

    @Override
    public void tickMovement() {
        World world = this.getWorld();
//        long start = System.nanoTime();
        if (world instanceof ServerWorld serverWorld) {
            if (this.isSleeping()) return;
            if (this.canPickUpLoot() && this.isAlive()) {
                Vec3i vec3i = this.getItemPickUpRangeExpander();
                List<ItemEntity> list = this.getWorld().getNonSpectatingEntities(ItemEntity.class, this.getBoundingBox().expand(vec3i.getX(), vec3i.getY(), vec3i.getZ()));
                for (ItemEntity itemEntity : list) {
                    if (itemEntity.isRemoved() || itemEntity.getStack().isEmpty() || itemEntity.cannotPickup() || !this.canGather(serverWorld, itemEntity.getStack()))
                        continue;
                    this.loot(serverWorld, itemEntity);
                }
            }
            this.velocityModified = true;
        }
        super.tickMovement();
        if (this.freshTick >= 1) {
            if (this.getWorld() instanceof ServerWorld serverWorld) {
                ServerChunkLoadingManager.EntityTracker tracker = ((ServerChunkLoadingManagerAccessor) serverWorld.getChunkManager().chunkLoadingManager).getEntityTrackerMap().get(this.getId());
                if (tracker != null) {
                    Set<PlayerAssociatedNetworkHandler> listeners = ((EntityTrackerAccessor) tracker).getListenerSet();
                    for (var handler : listeners) {
                        ServerPlayerEntity player = handler.getPlayer();
                        if (!player.isDisconnected() && player.isAlive()) {
                            EntityTrackerEntry entry = ((EntityTrackerAccessor) tracker).getTrackEntry();
                            entry.sendPackets(handler.getPlayer(), packets -> {
                                entry.syncEntityData();
                                entry.sendSyncPacket(EntityPositionSyncS2CPacket.create(this));
                            });
                        }
                    }
                }
            }
            this.freshTick = 0;
        } else {
            this.freshTick++;
        }
//
//        long end = System.nanoTime();
//        long duration = end - start; // 纳秒
//        System.out.println("耗时: " + (duration / 1_000_000) + " ms");
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        PolymerEntity polymerEntity = PolymerEntity.get(this);
        if (polymerEntity instanceof PlayerPolymerEntity playerPolymerEntity) {
            playerPolymerEntity.onCreated();
        }
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        PolymerEntity polymerEntity = PolymerEntity.get(this);
        if (polymerEntity instanceof PlayerPolymerEntity playerPolymerEntity) {
            playerPolymerEntity.onTrackingStopped(player);
        }
    }

    @Override
    protected void loot(ServerWorld world, ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getStack();
        if (this.inventory.canInsert(itemStack)) {
            if (ItemUtils.isArmorItem(itemStack)) {
                EquippableComponent equippableComponent = itemStack.get(DataComponentTypes.EQUIPPABLE);
                if (equippableComponent != null) {
                    boolean head = equippableComponent.slot() == EquipmentSlot.HEAD;
                    boolean chest = equippableComponent.slot() == EquipmentSlot.CHEST;
                    boolean legs = equippableComponent.slot() == EquipmentSlot.LEGS;
                    boolean feet = equippableComponent.slot() == EquipmentSlot.FEET;
                    if (head && this.inventory.getHead().isEmpty()) {
                        this.inventory.setHead(itemStack.copy());
                    } else if (chest && this.inventory.getChest().isEmpty()) {
                        this.inventory.setChest(itemStack.copy());
                    } else if (legs && this.inventory.getLegs().isEmpty()) {
                        this.inventory.setLegs(itemStack.copy());
                    } else if (feet && this.inventory.getFeet().isEmpty()) {
                        this.inventory.setFeet(itemStack.copy());
                    } else {
                        this.inventory.addStack(itemStack.copy());
                    }
                } else {
                    this.inventory.addStack(itemStack.copy());
                }
            } else if ((itemStack.getItem() instanceof ShieldItem) || (itemStack.getItem() == Items.TORCH)) {
                if (this.inventory.getOffHand().isEmpty()) {
                    this.inventory.setOffHand(itemStack.copy());
                } else {
                    this.inventory.addStack(itemStack.copy());
                }
            } else {
                if (this.inventory.getMainHand().isEmpty()) {
                    this.inventory.setMainHand(itemStack.copy());
                } else {
                    this.inventory.addStack(itemStack.copy());
                }
            }
            itemEntity.discard();
        }
    }

    protected void updateHealth() {
        if (this.getWorld().isClient) return;
        if (this.getHealth() < this.getMaxHealth()) {
            this.healthTick--;
        } else {
//            this.healthTick = 10;
        }
        if (this.consumeHunger() && this.getHealth() < this.getMaxHealth() && this.healthTick <= 0) {
            if (this.isDead()) return;
            if (this.nutrition == 20 && this.saturation > 1) {
                this.setHealth(getHealth() + Math.min(1, saturation / 6));
                this.healthTick = 10;
                this.exhaustionLevel += 6;
            } else if (nutrition >= 18) {
                this.setHealth(getHealth() + 1);
                this.healthTick = 80;
                this.exhaustionLevel += 6;
            } else if (nutrition == 0 && this.getHealth() > this.getMaxHealth() / 2) {
                this.damage((ServerWorld) this.getWorld(), this.getDamageSources().starve(), 1);
                this.healthTick = 80;
            }
        }
    }

    private void updateHunger() {
        if (this.consumeHunger()) {
            nutrition = Math.clamp(this.nutrition, 0, 20);
            saturation = Math.clamp(this.saturation, 0, this.nutrition);
            if (exhaustionLevel >= 4) {
                if (this.saturation > 0) {
                    this.saturation--;
                    this.exhaustionLevel = 0;
                } else if (this.nutrition > 0) {
                    this.nutrition--;
                    this.exhaustionLevel = 0;
                }
            }
        }
    }

    private void updateHungerConsumption() {
        this.hungerTick--;
        if (hungerTick <= 0) {
            hungerTick = 20;
            int hungerEffectLevel = 0;
            StatusEffectInstance hungerEff = this.getStatusEffect(StatusEffects.HUNGER);
            if (hungerEff != null) {
                hungerEffectLevel = hungerEff.getAmplifier();
                // System.out.println("饥饿消耗 "+ hungerEffectLevel);
            }
            this.exhaustionLevel += (float) (hungerEffectLevel * 0.1);
            if (this.getNavigation().isFollowingPath()) {
                this.exhaustionLevel += 0.015F;//无法检测具体行为 按0.015计算 略微提高消耗
                // System.out.println("寻路增加消耗");
            }
        }


    }


    protected void updateAttackType() {
        if (this.getWorld() == null || this.getWorld().isClient) {
            return;
        }
        this.goalSelector.remove(this.meleeAttackGoal);
        this.goalSelector.remove(this.bowAttackGoal);
        this.goalSelector.remove(this.crossBowAttackGoal);
        this.goalSelector.remove(this.danmakuItemGoal);
//        ItemStack itemStack = this.getMainHandStack();

        if (RangedAttackUtil.getArrowStack(this) != null && (this.inventory.findHand((stack -> stack.isOf(Items.BOW) || stack.getItem() instanceof BowItem)) != null)) {
            int i = this.getRegularAttackInterval();
            this.bowAttackGoal.setAttackInterval(i);
            this.goalSelector.add(4, this.bowAttackGoal);
        } else if (RangedAttackUtil.getCrossBowAmmoStack(this) != null && (this.inventory.findHand((stack -> stack.isOf(Items.CROSSBOW) || stack.getItem() instanceof CrossbowItem)) != null)) {
            this.goalSelector.add(4, this.crossBowAttackGoal);
        } else if (RangedAttackUtil.isDanmakuInHand(this)) {
            this.goalSelector.add(4, this.danmakuItemGoal);
        } else {
            this.goalSelector.add(4, this.meleeAttackGoal);
        }
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        EntityData initialize = super.initialize(world, difficulty, spawnReason, entityData);
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
        if (this.isSleeping()) return this.npcState;
        int rawId = RegistryManager.NPC_STATE.getRawId(this.npcState);
        NPCState next = NPCStates.fromInt(rawId + 1);
        return next != null ? next : NPCStates.fromInt(0);
    }

    public NPCState getPreviousState() {
        if (this.isSleeping()) return this.npcState;
        int rawId = RegistryManager.NPC_STATE.getRawId(this.npcState);
        NPCState next = NPCStates.fromInt(rawId - 1);
        Map<Integer, RegistryEntry.Reference<NPCState>> rawToEntry = RegistryManager.NPC_STATE.getIdToEntryMap();
        int maxKey = Collections.max(rawToEntry.keySet());
        return next != null ? next : NPCStates.fromInt(maxKey);
    }

    public void reduceHunger(float value) {
        float remaining = value;

        while (remaining > 0.0f) {
            if (this.saturation > 0.0f) {
                float delta = Math.min(0.5f, Math.min(this.saturation, remaining));
                this.saturation -= delta;
                remaining -= delta;
            } else if (this.nutrition > 0.0f) {
                float delta = Math.min(0.5f, Math.min(this.nutrition, remaining));
                this.nutrition -= delta;
                remaining -= delta;
            } else {
                break;
            }
        }
    }

    public void updateWorking() {
        if (this.npcState != this.lastNpcState && this.npcState == NPCStates.WORKING) {
            this.workingPos = new BlockPos(this.getBlockX(), (int) Math.round(this.getY()), this.getBlockZ());
        }

        if (this.npcState == NPCStates.WORKING && this.workTick < 20) {
            this.workTick++;
        } else {
            this.workTick = 0;
        }
        if (this.npcState == NPCStates.WORKING && this.workTick >= 20) {
            this.workTick = 0;
            BlockPos blockPos = this.getBlockPos();
            if (this.workingPos != null) {
                double distance = blockPos.getSquaredDistance(this.workingPos);

                if (distance > 8 * 8) {
                    boolean success = this.getNavigation().startMovingTo(
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
        float delta = Math.abs(this.bodyYaw - this.getYaw());
        if (delta > 20.0f) {
            this.setBodyYaw(this.getYaw());
        }
    }

    @Override
    public void tick() {
        World world = this.getWorld();
        this.updateHealth();
        this.updateHunger();
        this.updateHungerConsumption();
        this.updateWorking();
        this.updateName();
        this.fixPitchYaw();
        this.updateAttackTick++;
        if (this.updateAttackTick > this.maxUpdateAttackTick) {
            this.updateAttackType();
            this.updateAttackTick = 0;
        }
        this.prevPos = this.getPos();

        if (this.npcState == NPCStates.SNAKING) {
            this.getNavigation().stop();
            if (this.getPose() != EntityPose.CROUCHING) {
                this.setPose(EntityPose.CROUCHING);
            }
            super.tick();
            return;
        }

        if (this.npcState == NPCStates.SEATED) {
            if (this.seat == null) {
                List<ArmorStandEntity> list = world.getEntitiesByClass(
                                ArmorStandEntity.class,
                                new Box(this.getX() + 1, this.getY() + 1, this.getZ() + 1, this.getX() - 1, this.getY() - 1, this.getZ() - 1),
                                entity -> true)
                        .stream()
                        .filter(entity -> entity.getUuid().toString().equalsIgnoreCase(this.seatUUID))
                        .toList();
                if (!list.isEmpty()) {
                    this.seat = list.getFirst();
                } else {
                    spawnSeatAndSit();
                }
            }
            this.setSit(true);
            return;
        } else {
            if (this.seat != null) {
                this.seat.discard();
                this.seatUUID = "";
                this.seat = null;
            }
            this.setSit(false);
        }
        if (this.getPose() == EntityPose.CROUCHING) {
            this.setPose(EntityPose.STANDING);
        }
        this.lastNpcState = this.npcState;
        super.tick();
    }

    private void spawnSeatAndSit() {
        ArmorStandEntity as = EntityType.ARMOR_STAND.create(this.getWorld(), SpawnReason.TRIGGERED);
        if (as == null) return;

        as.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
        as.setInvisible(true);
        as.setNoGravity(true);
        as.setMarker(true);
        this.getWorld().spawnEntity(as);

        this.seat = as;
        this.seatUUID = this.seat.getUuid().toString();
        this.startRiding(as, true);
    }


    @Override
    public float getMovementSpeed() {
        if (this.npcState == NPCStates.NO_WALK || this.npcState == NPCStates.SNAKING) return 0;
        if (this.paused) return 0;
        return super.getMovementSpeed();
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (this.isDead()) {
            return false;
        }
        return super.damage(world, source, amount);
    }

    @Override
    public void damageArmor(DamageSource source, float amount) {
        if (this.canDamageEquipment()) {
            this.damageEquipment(source, amount, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD);
        }
    }

    @Override
    public void damageHelmet(DamageSource source, float amount) {
        if (this.canDamageEquipment()) {
            this.damageEquipment(source, amount, EquipmentSlot.HEAD);
        }
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        boolean result = super.tryAttack(world, target);
        if (result) {
            ItemStack mainHand = this.getMainHandStack();
            if (mainHand.isDamageable()) {
                mainHand.damage(1, (LivingEntity) this, (Hand) null);
            }
        }
        return result;
    }

    public ItemStack toArchive() {
        ItemStack itemStack = ModItems.ROLE_ARCHIVE.getDefaultStack();
        itemStack.set(ModDataComponentTypes.ROLE_FOLLOWER_ARCHIVE, new RoleFollowerArchive(this.getType(), this, this.getRegistryManager()));
        itemStack.set(ModDataComponentTypes.ROLE_CAN_RESPAWN, false);
        return itemStack;
    }

    //    @Override
    public Iterable<ItemStack> getArmorItems() {
        return List.of(
                this.getEquippedStack(EquipmentSlot.HEAD),
                this.getEquippedStack(EquipmentSlot.CHEST),
                this.getEquippedStack(EquipmentSlot.LEGS),
                this.getEquippedStack(EquipmentSlot.FEET)
        );
    }

    @Override
    public ItemStack getMainHandStack() {
        ItemStack stack = this.inventory.getStack(NPCInventoryImpl.MAIN_HAND);
        if (stack.isEmpty()) return super.getMainHandStack();
        return stack;
    }

    @Override
    public ItemStack getOffHandStack() {
        ItemStack stack = this.inventory.getStack(NPCInventoryImpl.OFF_HAND);
        if (stack.isEmpty()) return super.getOffHandStack();
        return stack;
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        if (slot == EquipmentSlot.HEAD) {
            return this.inventory.getHead();
        } else if (slot == EquipmentSlot.CHEST) {
            return this.inventory.getChest();
        } else if (slot == EquipmentSlot.LEGS) {
            return this.inventory.getLegs();
        } else if (slot == EquipmentSlot.FEET) {
            return this.inventory.getFeet();
        } else if (slot == EquipmentSlot.MAINHAND) {
            return this.inventory.getStack(NPCInventoryImpl.MAIN_HAND);
        } else if (slot == EquipmentSlot.OFFHAND) {
            return this.inventory.getStack(NPCInventoryImpl.OFF_HAND);
        }
        return super.getEquippedStack(slot);
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        super.equipStack(slot, stack);
        int idx = switch (slot) {
            case MAINHAND -> NPCInventoryImpl.MAIN_HAND;
            case OFFHAND -> NPCInventoryImpl.OFF_HAND;
            case HEAD -> -11;
            case CHEST -> -12;
            case LEGS -> -13;
            case FEET -> -14;
            default -> -1;
        };
        if (idx >= 0) this.inventory.setStack(idx, stack);
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
        if (!this.getWorld().isClient) {
            this.updateAttackType();
        }
    }

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }

    public static DefaultAttributeContainer createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(EntityAttributes.MAX_HEALTH, 20.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.ATTACK_DAMAGE, 1.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.1)
                .add(EntityAttributes.FOLLOW_RANGE, 32.0)
                .add(EntityAttributes.TEMPT_RANGE, 10.0)
                .add(EntityAttributes.ENTITY_INTERACTION_RANGE, 3)
                .build();
    }

    public boolean isOwner(Entity player) {
        return this.getOwner() == player;
    }

    public boolean isAllowOpenInventory(ServerPlayerEntity player) {
        return ((this.isOwner(player) || (player.isCreative())) && this.isTamed());
    }
    //return entity.getUuid().toString().equalsIgnoreCase(this.npcOwner);

    @Override
    public @Nullable LivingEntity getOwner() {
        if (this.npcOwner.equalsIgnoreCase("")) return null;
//        for (int i = 0; i < this.getWorld().getPlayers().size(); i++) {
//            PlayerEntity playerEntity = (PlayerEntity)this.getWorld().getPlayers().get(i);
//            if (playerEntity.getUuid().toString().equalsIgnoreCase(this.npcOwner))
//                return playerEntity;
//        }
        return this.getWorld().getPlayerByUuid(UUID.fromString(this.npcOwner));
//        return null;
    }

    //    @Override
    public @Nullable UUID getOwnerUuid() {
        if (this.npcOwner.equalsIgnoreCase("")) return null;
        return UUID.fromString(this.npcOwner);
    }

    @Override
    public boolean isSitting() {
        return this.isSit();
    }

    @Override
    public boolean isTamed() {
        return this.getOwnerUuid() != null;
    }

    @Override
    public Property getSkin() {
        return skin.get();
    }

}
