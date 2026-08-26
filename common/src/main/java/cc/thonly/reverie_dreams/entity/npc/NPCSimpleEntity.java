package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.entity.callback.CompatGoalAddedCallback;
import cc.thonly.reverie_dreams.api.entity.type.ChatAIEntity;
import cc.thonly.reverie_dreams.data.npc.NPCRoleType;
import cc.thonly.reverie_dreams.data.npc.RoleType;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.entity.ai.goal.*;
import cc.thonly.reverie_dreams.entity.ai.goal.work.*;
import cc.thonly.reverie_dreams.entity.npc.container.ChatAIContainer;
import cc.thonly.reverie_dreams.entity.npc.container.FavorabilityContainer;
import cc.thonly.reverie_dreams.entity.npc.container.NPCCustomerContainer;
import cc.thonly.reverie_dreams.entity.npc.container.NPCFoodDataContainer;
import cc.thonly.reverie_dreams.openai.AIMessage;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import cc.thonly.reverie_dreams.server.ChatAIManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@SuppressWarnings({"resource", "SpellCheckingInspection"})
@Getter
@Setter
public class NPCSimpleEntity extends BaseNPCLikeEntity implements Leashable, ChatAIEntity<NPCSimpleEntity>, TamableNPC {
    public static final EntityDataAccessor<RoleType> ROLE_TYPE = SynchedEntityData.defineId(NPCSimpleEntity.class, RoleType.SERIALIZER);

    private final NPCCustomerContainer customerContainer;
    private final FavorabilityContainer favorabilityContainer;
    //    private final FishingContainer fishingContainer;
    private final ChatAIContainer chatAIContainer;

    public NPCSimpleEntity(EntityType<? extends NPCSimpleEntity> entityType, Level world) {
        super(entityType, world);
        this.customerContainer = new NPCCustomerContainer(this, this.getRandom());
        this.favorabilityContainer = new FavorabilityContainer(this, this.getRandom());
//        this.fishingContainer = new FishingContainer(this, this.getRandom());
        this.chatAIContainer = new ChatAIContainer(new HashMap<>());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ROLE_TYPE, NPCRoleType.empty());
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(1, new NPCEatPlateGoal(this, 1, 15, 1));
        this.goalSelector.addGoal(2, new NPCInventoryEatGoal(this));
        this.goalSelector.addGoal(3, new SleepAtNightGoal(this, 1.0));
        //        this.goalSelector.add(4, this.bowAttackGoal);
        //        this.goalSelector.add(4, this.meleeAttackGoal);

        this.goalSelector.addGoal(6, new NPCFollowOwnerGoal(this, 1.0, 2.0f, 10.0f));
        this.goalSelector.addGoal(7, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(8, new NPCWanderAroundFarGoal(this, 1.0));

        this.goalSelector.addGoal(10, new NPCLookAroundGoal(this));
        this.goalSelector.addGoal(10, new NPCLookAtEntityGoal(this, Player.class, 8.0f, 0.02f, true));
        this.goalSelector.addGoal(10, new NPCLookAtEntityGoal(this, BaseNPCLikeEntity.class, 8.0f, 0.02f, true));

        this.targetSelector.addGoal(1, new NPCTrackOwnerAttackerGoal(this));
        this.targetSelector.addGoal(1, new NPCCleanMonsterGoal(this));
        this.targetSelector.addGoal(1, new NPCBreedGoal(this));
        this.targetSelector.addGoal(1, new NPCSheepShearGoal(this));
        this.targetSelector.addGoal(2, new NPCAttackWithOwnerGoal(this));
        NPCHurtByTargetGoal hurtByTargetGoal = new NPCHurtByTargetGoal(this);
        this.targetSelector.addGoal(3, hurtByTargetGoal);

        this.goalSelector.addGoal(1, new NPCOpenDoorGoal(this));
        this.goalSelector.addGoal(1, new NPCOpenSilverChestGoal(this));
        this.goalSelector.addGoal(1, new NPCSmeltGoal(this));
        this.goalSelector.addGoal(1, new NPCChestClassificationGoal(this));
        this.goalSelector.addGoal(1, new NPCCustomerGoal(this));
        this.goalSelector.addGoal(1, new NPCFarmGoal(this));
        this.goalSelector.addGoal(1, new NPCFishingGoal(this));
//        this.goalSelector.addGoal(1, new NPCAutoPickItemGoal(this));
        this.goalSelector.addGoal(2, new NPCCloseToCropGoal(this, 1));

        if (this.isEnableTamableFeature()) {
            hurtByTargetGoal.setAlertOthers();
            this.goalSelector.addGoal(4, new NPCTemptGoal(this, 1.2, stack -> stack.is(RDItemTags.ROLE_TAME_FOOD), false));

            for (Tuple<Integer, Goal> tuple : CompatGoalAddedCallback.EVENT.invoker().handle(this)) {
                this.goalSelector.addGoal(tuple.getA(), tuple.getB());
            }
        }

        this.getNavigation().setCanOpenDoors(true);
        this.getNavigation().setCanFloat(true);
    }

    @Override
    public void tick() {
        Level world = this.level();
        if (!world.isClientSide() && world.isBrightOutside()) {
            this.stopSleeping();
        }
        this.customerContainer.tick();
        this.favorabilityContainer.tick();
        super.tick();
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel world, DamageSource damageSource) {
//        super.drop(world, damageSource);
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity livingEntity &&
                livingEntity.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() &&
                livingEntity.getItemInHand(InteractionHand.OFF_HAND).isEmpty() &&
                livingEntity.isShiftKeyDown() && this.isOwnedBy(livingEntity)
        ) {
            this.setTarget(null);
            this.setLastHurtByMob(null);
            return false;
        }
        if (attacker instanceof Player player) {
            FavorabilityContainer favorabilityContainer = this.getFavorabilityContainer();
            favorabilityContainer.add(player.getUUID(), -(this.getRandom().nextInt(3, 15)));
        }
        return super.hurtServer(world, source, amount);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        Level world = this.level();
        if (world.isClientSide() || hand != InteractionHand.MAIN_HAND || !(world instanceof ServerLevel serverWorld) || !(player instanceof ServerPlayer serverPlayerEntity)) {
            return super.mobInteract(player, hand);
        }
        return NPCLikeInteractionEvents.emit(serverWorld, serverPlayerEntity, hand, this);
    }

    @Override
    public List<AIMessage> getChatHistory(ServerPlayer player) {
        UUID uid = player.getUUID();
        return this.chatAIContainer.getHistories().getOrDefault(uid, new ArrayList<>());
    }

    @Override
    public void clearChatHistory() {
        this.chatAIContainer.clearAll();
    }

    @Override
    public void clearChatHistory(ServerPlayer player) {
        this.chatAIContainer.clear(player.getUUID());
    }

    @Override
    public CompletableFuture<Void> send(ServerPlayer player, String msg) {
        UUID uid = player.getUUID();

        List<AIMessage> history = this.chatAIContainer.getHistories()
                                                      .computeIfAbsent(uid, _ -> new ArrayList<>());

        if (history.isEmpty()) {
            history.add(new AIMessage(
                    "system",
                    getStartPrompt(this, player)
            ));
        }

        AIMessage userMsg = new AIMessage("user", msg);
        history.add(userMsg);

        MinecraftServer server = ReverieDreams.getServer();
        if (server == null)
            return CompletableFuture.completedFuture(null);

        return CompletableFuture.supplyAsync(() -> {
            return this.callChatAI(history, player, msg);

        }).thenAccept(reply -> {
            if (reply != null) {
                server.execute(() -> {
                    player.sendSystemMessage(
                            Component.empty()
                                     .append("<")
                                     .append(this.getDisplayName())
                                     .append("> ")
                                     .append(reply.getContent())
                    );

                    history.add(reply);
                    if (history.size() > 40) {
                        history.remove(1);
                    }
                });
            }
        });
    }

    @Override
    public String submitData(ServerPlayer player, NPCSimpleEntity entity) {
        NPCFoodDataContainer foodData = this.getFoodData();
        Map<String, String> kw = new LinkedHashMap<>();
        kw.put("Health", "%s/%s".formatted(this.getHealth(), this.getMaxHealth()));
        kw.put("Nutrition", "%s/20".formatted(foodData.getNutrition()));
        kw.put("Saturation", "%s/20".formatted(foodData.getSaturation()));
        kw.put("StoredExperience", "%s".formatted(this.getStoredExperience()));
        kw.put("Favorability", "%s".formatted(this.getFavorabilityContainer().get(player.getUUID())));
        LivingEntity owner = this.getOwner();
        if (owner != null) {
            kw.put("Owner", "%s".formatted(owner.getPlainTextName()));
        }
        return ChatAIEntity.GSON.toJson(kw);
    }

    @Override
    public void handleCommand(String msg) {
        // noop
    }

    @Override
    public void openChatAIGUI(ServerPlayer player) {
        ChatAIEntity<NPCSimpleEntity> chatAIEntity = ChatAIManager.of(this);
        Holder<Dialog> holder = ChatAIManager.buildDialog(player, chatAIEntity);
        if (holder != null) {
            player.openDialog(holder);
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        view.read("RoleType", RoleType.CODEC).ifPresent(this::setRoleType);
        this.updateRoleData();
        this.customerContainer.readAdditionalSaveData(view);
        this.favorabilityContainer.readAdditionalSaveData(view);
        this.chatAIContainer.readAdditionalSaveData(view);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        view.store("RoleType", RoleType.CODEC, this.getRoleType());
        this.customerContainer.addAdditionalSaveData(view);
        this.favorabilityContainer.addAdditionalSaveData(view);
        this.chatAIContainer.addAdditionalSaveData(view);
    }

    private void updateRoleData() {
        if (this.getRoleType().isVirtual()) {
            SkinType skinType = this.getSkinType();
            List<NPCRoleType> list = BuiltInRegistryProviders.NPC_ROLE_TYPE.stream().filter(role -> Objects.equals(skinType, role.getSkinType())).toList();
            if (!list.isEmpty()) {
                this.setRoleType(list.getFirst());
            }
        }
    }

    public RoleType getRoleType() {
        return this.getEntityData().get(ROLE_TYPE);
    }

    public void setRoleType(RoleType roleType) {
        this.getEntityData().set(ROLE_TYPE, roleType);
    }

    @Override
    public KeepInventoryTypes getKeepInventoryType() {
        return KeepInventoryTypes.ARCHIVED;
    }

    @Override
    public boolean canBeLeashed() {
        return true;
    }

    @Override
    public Boolean canFeed() {
        return true;
    }

    @Override
    public Boolean canDamageEquipment() {
        return true;
    }

    @Override
    public Boolean consumeHunger() {
        return true;
    }

}
