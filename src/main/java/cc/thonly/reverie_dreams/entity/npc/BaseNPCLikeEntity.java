package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.minecraft.util.TagValueFunction;
import cc.thonly.minecraft.util.TagValueOutput;
import cc.thonly.minecraft.util.ValueInput;
import cc.thonly.minecraft.util.ValueOutput;
import cc.thonly.polymer.entity.PlayerPolymerEntity;
import cc.thonly.reverie_dreams.component.RoleFollowerArchive;
import cc.thonly.reverie_dreams.data.npc.NPCState;
import cc.thonly.reverie_dreams.data.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.entity.ai.goal.attack.NPCBowAttackGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.attack.NPCCrossbowAttackGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.attack.NPCDanmakuItemGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.attack.RangedAttackUtil;
import cc.thonly.reverie_dreams.inventory.NPCInventoryImpl;
import cc.thonly.reverie_dreams.mixin.accessor.EntityTrackerAccessor;
import cc.thonly.reverie_dreams.mixin.accessor.ServerChunkLoadingManagerAccessor;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.content.NPCStates;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.content.skin.MobSkinTypes;
import cc.thonly.reverie_dreams.util.item.ItemUtils;
import com.google.common.collect.ImmutableList;
import com.mojang.authlib.properties.Property;
import com.mojang.logging.LogUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Getter
@Setter
public abstract class BaseNPCLikeEntity extends AbstractNPCEntity implements RangedAttackMob, NPCSettings {
    // 皮肤
    protected SkinType skinType = MobSkinTypes.DEFAULT;
    // 实体信息
    protected NPCState npcState = NPCStates.NORMAL;
    protected NPCState lastNpcState = NPCStates.NORMAL;
    protected NPCWorkMode workMode = NPCWorkModes.COMBAT;
    protected boolean sit = false;
    protected String npcOwner = "";
    protected String seatUUID = "";
    protected ArmorStand seat;
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
    // 好感度
    protected int goodwill = 100;
    // 工作
    protected BlockPos workingPos = new BlockPos(0, 0, 0);
    protected int workTick = 0;
    // 睡眠
    protected int bedWakeCd = 0;
    protected Vec3 prevPos;
    protected int freshTick = 0;
    // 其他
    protected boolean autoPick = false;
    public static final HashSet<Item> ARROW_ITEMS = new HashSet<>();

    static {
        ARROW_ITEMS.add(Items.ARROW);
        ARROW_ITEMS.add(Items.TIPPED_ARROW);
        ARROW_ITEMS.add(Items.SPECTRAL_ARROW);
    }

    private final NPCBowAttackGoal<BaseNPCLikeEntity> bowAttackGoal = new NPCBowAttackGoal<>(this, 1.0, 20, 15.0f);
    private final NPCCrossbowAttackGoal crossBowAttackGoal = new NPCCrossbowAttackGoal(this, 1.0, 20);
    private final NPCDanmakuItemGoal<BaseNPCLikeEntity> danmakuItemGoal = new NPCDanmakuItemGoal<>(this, 1.0, 20, 15.0f);

    private final MeleeAttackGoal meleeAttackGoal = new MeleeAttackGoal(this, 1.5, false) {
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
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(MemoryModuleType.DOORS_TO_CLOSE, MemoryModuleType.NEAREST_BED);
    private static final ImmutableList<SensorType<? extends Sensor<? super BaseNPCLikeEntity>>> SENSORS = ImmutableList.of();

    public BaseNPCLikeEntity(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world);
        this.init();
        this.updateAttackType();
    }

    public BaseNPCLikeEntity(EntityType<? extends TamableAnimal> entityType, Level world, SkinType skinType) {
        this(entityType, world);
        this.skinType = skinType;
    }

    protected Brain.Provider<BaseNPCLikeEntity> brainProvider() {
        return Brain.provider(MEMORY_MODULES, SENSORS);
    }

    public void init() {
        AttributeMap attributeContainer = this.getAttributes();
        //noinspection ConstantValue
        if (attributeContainer != null) {
            AttributeInstance scale = attributeContainer.getInstance(Attributes.SCALE);
            if (scale != null) {
                scale.setBaseValue(0.9);
            }
        }
        this.setNoGravity(false);
        this.setCanPickUpLoot(true);
        this.setTame(false, false);
        this.setCanPickUpLoot(true);

        this.setPathfindingMalus(PathType.DANGER_FIRE, 16.0f);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0f);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }


    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        RegistryAccess registryManager = this.registryAccess();
        TagValueFunction.ofInput(compoundTag, registryManager, view -> {
            this.sit = view.getBooleanOr("IsSit", false);

            this.npcState = NPCStates.get(ResourceLocation.parse(view.getStringOr("NPCStateId", NPCState.DEFAULT_ID.toString())));
            this.workMode = NPCWorkModes.get(ResourceLocation.parse(view.getStringOr("NPCWorkStateId", NPCWorkMode.DEFAULT_ID.toString())));
            this.npcOwner = view.getStringOr("NpcOwner", "");

            NPCInventoryImpl inventory = new NPCInventoryImpl(NPCInventoryImpl.MAX_SIZE);
            ContainerHelper.loadAllItems(compoundTag, inventory.items, this.registryAccess());

            view.getBooleanOr("AutoPick", false);

            this.inventory = inventory;

            this.seatUUID = view.getStringOr("SeatUUID", "null");

            this.nutrition = view.getIntOr("FoodNutrition", 20);
            this.saturation = view.getIntOr("FoodSaturation", 20);

            this.exhaustionLevel = view.getIntOr("FoodExhaustionLevel", 0);
            Optional<Long> workingPosOptional = view.getLong("WorkingPos");
            this.workingPos = workingPosOptional
                    .map(BlockPos::of)
                    .orElseGet(() -> BlockPos.of(new BlockPos(0, 0, 0).asLong()));

            this.storedExperience = view.getIntOr("ExperienceAmount", 0);
            this.goodwill = view.getIntOr("GoodWIll", 100);

            this.readSkinData(view);

            this.updateAttackType();
        });

    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        TagValueFunction.ofOutput(compoundTag, this.registryAccess(), view -> {
            view.putBoolean("IsSit", this.sit);
            view.putString("NpcOwner", this.npcOwner);
            view.putString("NPCStateId", Optional.ofNullable(RegistryHandlers.NPC_STATE.getKey(this.npcState)).orElse(NPCState.DEFAULT_ID).toString());
            view.putString("NPCWorkStateId", Optional.ofNullable(RegistryHandlers.NPC_WORK_MODE.getKey(this.workMode)).orElse(NPCWorkMode.DEFAULT_ID).toString());
            view.putFloat("FoodNutrition", this.nutrition);
            view.putFloat("FoodSaturation", this.saturation);
            view.putFloat("FoodExhaustionLevel", this.exhaustionLevel);

            ContainerHelper.saveAllItems(compoundTag, this.inventory.items, this.registryAccess());

            view.putLong("WorkingPos", this.workingPos.asLong());

            view.putBoolean("AutoPick", this.autoPick);

            if (!this.seatUUID.isEmpty() && !this.seatUUID.equals("null")) {
                view.putString("SeatUUID", this.seatUUID);
            }

            view.putInt("ExperienceAmount", this.storedExperience);
            view.putInt("GoodWill", this.goodwill);
            this.writeSkinData(view);
        });
    }

    public void writeSkinData(ValueOutput view) {
        RegistryHandlers.SKIN_TYPE.getKey(this.skinType);
        view.store("Skin", SkinType.CODEC, this.skinType);
    }

    public void readSkinData(ValueInput view) {
        view.read("Skin", SkinType.CODEC).ifPresent(value -> {
            this.skinType = value;
        });
    }

    @Override
    public void stopSleeping() {
        super.stopSleeping();
        this.bedWakeCd = 20 * 5;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
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
                for (ItemStack projStack : chargedProjectilesComponent.getItems()) {
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
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
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
            Vec3 position = this.position();
            ExperienceOrb orbEntity = new ExperienceOrb(world, position.x, position.y, position.z, this.storedExperience);
            world.addFreshEntity(orbEntity);
        }
        KeepInventoryTypes keepInventoryType = this.getKeepInventoryType();
        if (keepInventoryType == KeepInventoryTypes.ARCHIVED) {
            ItemStack archive = this.toArchive();
            ItemEntity itemEntity = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), archive);
            world.addFreshEntity(itemEntity);
        } else if (keepInventoryType == KeepInventoryTypes.DROP_ALL_ITEM) {
            for (int i = 0; i < this.inventory.getContainerSize(); i++) {
                if (this.getDonDropSlotIndex().contains(i)) continue;
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

    @Override
    public void setOwnerUUID(@Nullable UUID uuid) {
        if (uuid != null) {
            this.npcOwner = uuid.toString();
            this.setTame(true, true);
        }
    }

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
    public void aiStep() {
        Level world = this.level();
//        long start = System.nanoTime();
        if (world instanceof ServerLevel serverWorld) {
            if (this.isSleeping()) return;
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
        if (this.freshTick >= 1) {
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
                                entry.broadcastAndSend(ClientboundEntityPositionSyncPacket.of(this));
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
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        super.onSyncedDataUpdated(data);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        PolymerEntity polymerEntity = PolymerEntity.get(this);
        if (polymerEntity instanceof PlayerPolymerEntity playerPolymerEntity) {
            playerPolymerEntity.onCreated();
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        PolymerEntity polymerEntity = PolymerEntity.get(this);
        if (polymerEntity instanceof PlayerPolymerEntity playerPolymerEntity) {
            playerPolymerEntity.onTrackingStopped(player);
        }
    }

    @Override
    protected void pickUpItem(ServerLevel world, ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        if (this.inventory.canAddItem(itemStack)) {
            if (ItemUtils.isArmorItem(itemStack)) {
                Equippable equippableComponent = itemStack.get(DataComponents.EQUIPPABLE);
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
                        this.inventory.addItem(itemStack.copy());
                    }
                } else {
                    this.inventory.addItem(itemStack.copy());
                }
            } else if ((itemStack.getItem() instanceof ShieldItem) || (itemStack.getItem() == Items.TORCH)) {
                if (this.inventory.getOffHand().isEmpty()) {
                    this.inventory.setOffHand(itemStack.copy());
                } else {
                    this.inventory.addItem(itemStack.copy());
                }
            } else {
                if (this.inventory.getMainHand().isEmpty()) {
                    this.inventory.setMainHand(itemStack.copy());
                } else {
                    this.inventory.addItem(itemStack.copy());
                }
            }
            itemEntity.discard();
        }
    }

    protected void updateHealth() {
        if (this.level().isClientSide) return;
        if (this.getHealth() < this.getMaxHealth()) {
            this.healthTick--;
        } else {
//            this.healthTick = 10;
        }
        if (this.consumeHunger() && this.getHealth() < this.getMaxHealth() && this.healthTick <= 0) {
            if (this.isDeadOrDying()) return;
            if (this.nutrition == 20 && this.saturation > 1) {
                this.setHealth(getHealth() + Math.min(1, saturation / 6));
                this.healthTick = 10;
                this.exhaustionLevel += 6;
            } else if (nutrition >= 18) {
                this.setHealth(getHealth() + 1);
                this.healthTick = 80;
                this.exhaustionLevel += 6;
            } else if (nutrition == 0 && this.getHealth() > this.getMaxHealth() / 2) {
                this.hurtServer((ServerLevel) this.level(), this.damageSources().starve(), 1);
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
            MobEffectInstance hungerEff = this.getEffect(MobEffects.HUNGER);
            if (hungerEff != null) {
                hungerEffectLevel = hungerEff.getAmplifier();
                // System.out.println("饥饿消耗 "+ hungerEffectLevel);
            }
            this.exhaustionLevel += (float) (hungerEffectLevel * 0.1);
            if (this.getNavigation().isInProgress()) {
                this.exhaustionLevel += 0.015F;//无法检测具体行为 按0.015计算 略微提高消耗
                // System.out.println("寻路增加消耗");
            }
        }


    }


    protected void updateAttackType() {
        if (this.level() == null || this.level().isClientSide) {
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
        if (this.isSleeping()) return this.npcState;
        int rawId = RegistryHandlers.NPC_STATE.getId(this.npcState);
        NPCState next = NPCStates.fromInt(rawId + 1);
        return next != null ? next : NPCStates.fromInt(0);
    }

    public NPCState getPreviousState() {
        if (this.isSleeping()) return this.npcState;
        int rawId = RegistryHandlers.NPC_STATE.getId(this.npcState);
        NPCState next = NPCStates.fromInt(rawId - 1);
        Map<Integer, Holder.Reference<NPCState>> rawToEntry = RegistryHandlers.NPC_STATE.getIdToEntryMap();
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
            BlockPos blockPos = this.blockPosition();
            if (this.workingPos != null) {
                double distance = blockPos.distSqr(this.workingPos);

                if (distance > 8 * 8) {
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
        float delta = Math.abs(this.yBodyRot - this.getYRot());
        if (delta > 20.0f) {
            this.setYBodyRot(this.getYRot());
        }
    }

    @Override
    public void tick() {
        Level world = this.level();
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
        this.prevPos = this.position();

        if (this.npcState == NPCStates.SNAKING) {
            this.getNavigation().stop();
            if (this.getPose() != Pose.CROUCHING) {
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
        if (this.getPose() == Pose.CROUCHING) {
            this.setPose(Pose.STANDING);
        }
        this.lastNpcState = this.npcState;
        super.tick();
    }

    private void spawnSeatAndSit() {
        ArmorStand as = EntityType.ARMOR_STAND.create(this.level(), EntitySpawnReason.TRIGGERED);
        if (as == null) return;

        as.setPos(new Vec3(this.getX(), this.getY(), this.getZ()));
        as.setXRot(this.getXRot());
        as.setYRot(this.getYRot());
        as.setInvisible(true);
        as.setNoGravity(true);
        as.setMarker(true);
        this.level().addFreshEntity(as);

        this.seat = as;
        this.seatUUID = this.seat.getUUID().toString();
        this.startRiding(as, true);
    }


    @Override
    public float getSpeed() {
        if (this.npcState == NPCStates.NO_WALK || this.npcState == NPCStates.SNAKING) return 0;
        if (this.paused) return 0;
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
                mainHand.hurtAndBreak(1, (LivingEntity) this, EquipmentSlot.MAINHAND);
            }
        }
        return result;
    }

    public ItemStack toArchive() {
        ItemStack itemStack = RDItems.ROLE_ARCHIVE.getDefaultInstance();
        CompoundTag nbtCompound;
        ProblemReporter logging = new ProblemReporter.Collector();
        TagValueOutput view = TagValueOutput.createWithContext(logging, this.registryAccess());
        this.addAdditionalSaveData(view.buildResult());
        nbtCompound = view.buildResult();

        MutableComponent mutableComponent = Component.empty();
        mutableComponent.append(itemStack.getItemName()).append("(").append(this.getName()).append(")");
        itemStack.set(RDDataComponents.ROLE_FOLLOWER_ARCHIVE, new RoleFollowerArchive(this.getName(), this.getMaxHealth(), nbtCompound));
        itemStack.set(RDDataComponents.ROLE_CAN_RESPAWN, false);
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
    public ItemStack getMainHandItem() {
        ItemStack stack = this.inventory.getItem(NPCInventoryImpl.MAIN_HAND);
        if (stack.isEmpty()) return super.getMainHandItem();
        return stack;
    }

    @Override
    public ItemStack getOffhandItem() {
        ItemStack stack = this.inventory.getItem(NPCInventoryImpl.OFF_HAND);
        if (stack.isEmpty()) return super.getOffhandItem();
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
        if (idx >= 0) this.inventory.setItem(idx, stack);
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
        if (!this.level().isClientSide) {
            this.updateAttackType();
        }
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    public static AttributeSupplier createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.1)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.TEMPT_RANGE, 10.0)
                .add(Attributes.ENTITY_INTERACTION_RANGE, 3)
                .build();
    }

    public boolean isOwner(Entity player) {
        return this.getOwner() == player;
    }

    public boolean isAllowOpenInventory(ServerPlayer player) {
        return ((this.isOwnedBy(player) || (player.isCreative())) && this.isTame());
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
        return this.level().getPlayerByUUID(UUID.fromString(this.npcOwner));
//        return null;
    }

    //    @Override
    public @Nullable UUID getOwnerUuid() {
        if (this.npcOwner.equalsIgnoreCase("")) return null;
        return UUID.fromString(this.npcOwner);
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
    public Property getSkin() {
        return this.skinType.get();
    }

}
