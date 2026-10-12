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
import cc.thonly.reverie_dreams.openai.ChatProvider;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import cc.thonly.reverie_dreams.server.ChatAIManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Holder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
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
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@SuppressWarnings({"resource", "SpellCheckingInspection"})
@Getter
@Setter
public class NPCSimpleEntity extends BaseNPCLikeEntity implements Leashable, ChatAIEntity<NPCSimpleEntity>, TamableNPC {
    public static final EntityDataAccessor<RoleType> ROLE_TYPE = SynchedEntityData.defineId(NPCSimpleEntity.class, RoleType.SERIALIZER);

    /** Above-the-hotbar "thinking" hint is refreshed at this interval while a request is running. */
    private static final int THINKING_NOTIFY_INTERVAL_TICKS = 20;
    /** Minimum delay between two chat requests from the same player. */
    private static final int CHAT_REQUEST_COOLDOWN_TICKS = 20;

    private final NPCCustomerContainer customerContainer;
    private final FavorabilityContainer favorabilityContainer;
    //    private final FishingContainer fishingContainer;
    private final ChatAIContainer chatAIContainer;

    /** Players with an AI request currently in flight (server thread only). */
    private final Set<UUID> pendingChatRequests = ConcurrentHashMap.newKeySet();
    /** Last request tick per player, used to rate limit retries (server thread only). */
    private final Map<UUID, Long> lastChatRequestTicks = new HashMap<>();
    private int thinkingNotifyTimer = 0;

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

        this.goalSelector.addGoal(6, new NPCFollowOwnerGoal(this, 1.4, 2.0f, 16.0f));
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
        this.goalSelector.addGoal(1, new NPCHoneycombHarvestingGoal(this));

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
        this.tickChatRequests(world);
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

        if (player.hasDisconnected() || this.level().isClientSide()) {
            return CompletableFuture.completedFuture(null);
        }

        // One request per player per NPC at a time: keeps the history
        // consistent and prevents spamming the API.
        if (this.pendingChatRequests.contains(uid)) {
            player.sendSystemMessage(Component.translatable("gui.reverie_dreams.ai_busy"), true);
            return CompletableFuture.completedFuture(null);
        }

        long now = this.level().getGameTime();
        Long lastRequest = this.lastChatRequestTicks.get(uid);
        if (lastRequest != null && now - lastRequest < CHAT_REQUEST_COOLDOWN_TICKS) {
            player.sendSystemMessage(Component.translatable("gui.reverie_dreams.ai_busy"), true);
            return CompletableFuture.completedFuture(null);
        }

        MinecraftServer server = ReverieDreams.getServer();
        if (server == null) {
            return CompletableFuture.completedFuture(null);
        }

        int maxHistory = Math.max(ReverieDreams.config().maxChatHistory, 2);
        List<AIMessage> history = this.chatAIContainer.getOrCreateHistory(uid);

        if (history.isEmpty()) {
            history.add(new AIMessage(AIMessage.SYSTEM, getStartPrompt(this, player)));
        }

        history.add(new AIMessage(AIMessage.USER, msg));
        this.chatAIContainer.trim(uid, maxHistory);

        // Everything the background task needs is computed on the server thread.
        String npcState = this.submitData(player, this);
        List<AIMessage> snapshot = AIMessage.copy(history);

        this.lastChatRequestTicks.put(uid, now);
        this.pendingChatRequests.add(uid);
        player.sendSystemMessage(this.thinkingMessage(), true);

        return CompletableFuture
                .supplyAsync(() -> this.callChatAI(snapshot, player, npcState), ChatProvider.EXECUTOR)
                .thenAcceptAsync(reply -> this.finishChatRequest(player, uid, msg, reply, maxHistory), server)
                .exceptionally(throwable -> {
                    log.warn("ChatAI request for {} failed", this.getDisplayName().getString(), throwable);
                    // Never leave the "thinking" lock stuck if a stage fails.
                    server.execute(() -> this.failChatRequest(player, uid, msg));
                    return null;
                });
    }

    private void failChatRequest(ServerPlayer player, UUID uid, String userInput) {
        this.pendingChatRequests.remove(uid);
        if (player.hasDisconnected()) {
            return;
        }
        this.rollbackUserMessage(uid, userInput);
        player.sendSystemMessage(Component.empty(), true);
        player.sendSystemMessage(
                Component.translatable("gui.reverie_dreams.ai_error")
                         .withStyle(ChatFormatting.RED)
        );
    }

    private void finishChatRequest(ServerPlayer player, UUID uid, String userInput, AIMessage reply, int maxHistory) {
        this.pendingChatRequests.remove(uid);
        if (player.hasDisconnected()) {
            return;
        }
        // Clear the "thinking" hint above the hotbar.
        player.sendSystemMessage(Component.empty(), true);

        if (reply == null) {
            this.rollbackUserMessage(uid, userInput);
            player.sendSystemMessage(
                    Component.translatable("gui.reverie_dreams.ai_error")
                             .withStyle(ChatFormatting.RED)
            );
            return;
        }

        List<AIMessage> history = this.chatAIContainer.getOrCreateHistory(uid);
        history.add(reply);
        this.chatAIContainer.trim(uid, maxHistory);

        player.sendSystemMessage(
                Component.empty()
                         .append("<")
                         .append(this.getDisplayName())
                         .append("> ")
                         .append(reply.getContent())
        );
    }

    /**
     * Removes the unanswered user message after a failed request so the next
     * request does not carry a dangling turn.
     */
    private void rollbackUserMessage(UUID uid, String expectedContent) {
        List<AIMessage> history = this.chatAIContainer.getHistory(uid);
        if (history.isEmpty()) {
            return;
        }
        AIMessage last = history.getLast();
        if (AIMessage.USER.equals(last.getRole()) && Objects.equals(last.getContent(), expectedContent)) {
            history.removeLast();
        }
    }

    private Component thinkingMessage() {
        return Component.translatable("gui.reverie_dreams.ai_thinking", this.getDisplayName());
    }

    private void tickChatRequests(Level world) {
        if (world.isClientSide() || this.pendingChatRequests.isEmpty()) {
            this.thinkingNotifyTimer = 0;
            return;
        }
        if (++this.thinkingNotifyTimer < THINKING_NOTIFY_INTERVAL_TICKS) {
            return;
        }
        this.thinkingNotifyTimer = 0;
        MinecraftServer server = ReverieDreams.getServer();
        if (server == null) {
            return;
        }
        for (UUID uid : this.pendingChatRequests) {
            ServerPlayer target = server.getPlayerList().getPlayer(uid);
            if (target != null && !target.hasDisconnected()) {
                target.sendSystemMessage(this.thinkingMessage(), true);
            }
        }
    }

    @Override
    public String submitData(ServerPlayer player, NPCSimpleEntity entity) {
        NPCFoodDataContainer foodData = this.getFoodData();
        Map<String, String> kw = new LinkedHashMap<>();
        kw.put("physical_condition", describeHealth());
        kw.put("appetite", describeAppetite(foodData.getNutrition()));
        kw.put("attitude_towards_player", describeFavorability(this.getFavorabilityContainer().get(player.getUUID())));
        LivingEntity owner = this.getOwner();
        if (owner != null) {
            kw.put("owner", owner.getPlainTextName());
        }
        return ChatAIEntity.GSON.toJson(kw);
    }

    private String describeHealth() {
        float maxHealth = this.getMaxHealth();
        if (maxHealth <= 0.0f) {
            return "healthy";
        }
        float ratio = this.getHealth() / maxHealth;
        if (ratio > 0.75f) {
            return "healthy";
        }
        if (ratio > 0.5f) {
            return "slightly wounded";
        }
        if (ratio > 0.25f) {
            return "badly wounded";
        }
        return "on the verge of collapse";
    }

    private static String describeAppetite(int nutrition) {
        if (nutrition >= 16) {
            return "full";
        }
        if (nutrition >= 10) {
            return "satisfied";
        }
        if (nutrition >= 4) {
            return "hungry";
        }
        return "starving";
    }

    private static String describeFavorability(double favorability) {
        if (favorability >= 150.0) {
            return "very close";
        }
        if (favorability >= 100.0) {
            return "friendly";
        }
        if (favorability >= 60.0) {
            return "neutral";
        }
        if (favorability >= 30.0) {
            return "wary";
        }
        return "hostile";
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
