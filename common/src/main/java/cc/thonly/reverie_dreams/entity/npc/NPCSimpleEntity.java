package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.server.dialog.NPCSpeech;
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
import cc.thonly.reverie_dreams.entity.npc.dialog.NPCConversationContext;
import cc.thonly.reverie_dreams.ai.AIMessage;
import cc.thonly.reverie_dreams.ai.ChatProvider;
import cc.thonly.reverie_dreams.ai.AIRequestContext;
import cc.thonly.reverie_dreams.ai.AIUsageBudget;
import cc.thonly.reverie_dreams.ai.AIUsageSavedData;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.NPCStates;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import cc.thonly.reverie_dreams.restaurant.Settlement;
import cc.thonly.reverie_dreams.restaurant.SettlementLedger;
import cc.thonly.reverie_dreams.restaurant.SettlementParty;
import cc.thonly.reverie_dreams.restaurant.SettlementRewards;
import cc.thonly.reverie_dreams.restaurant.RestaurantService;
import cc.thonly.reverie_dreams.server.ChatAIManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Holder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
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
import java.util.concurrent.ConcurrentHashMap;

import cc.thonly.reverie_dreams.data.npc.NPCState;
import cc.thonly.reverie_dreams.ai.npc.NPCContainerAccess;
import cc.thonly.reverie_dreams.ai.npc.NPCMemory;
import cc.thonly.reverie_dreams.ai.npc.NPCSkillTask;
import cc.thonly.reverie_dreams.ai.ProviderRegistry;
import cc.thonly.reverie_dreams.server.PlayerSettings;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@Slf4j
@SuppressWarnings({"resource", "SpellCheckingInspection"})
@Getter
@Setter
public class NPCSimpleEntity extends BaseNPCLikeEntity implements Leashable, ChatAIEntity<NPCSimpleEntity>, TamableNPC, SettlementParty {
    public static final EntityDataAccessor<RoleType> ROLE_TYPE = SynchedEntityData.defineId(NPCSimpleEntity.class, RoleType.SERIALIZER);
    private static final EntityDataAccessor<String> YSM_ACTION = SynchedEntityData.defineId(NPCSimpleEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Long> YSM_ACTION_START = SynchedEntityData.defineId(NPCSimpleEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> YSM_ACTION_REVISION = SynchedEntityData.defineId(NPCSimpleEntity.class, EntityDataSerializers.INT);
    private static final int ACTION_WHEEL_TIMEOUT = 60;

    /**
     * Above-the-hotbar "thinking" hint is refreshed at this interval while a request is running.
     */
    private static final int THINKING_NOTIFY_INTERVAL_TICKS = 20;
    /**
     * Minimum delay between two chat requests from the same player.
     */
    private static final int CHAT_REQUEST_COOLDOWN_TICKS = 20;

    private final NPCCustomerContainer customerContainer;
    private final FavorabilityContainer favorabilityContainer;
    //    private final FishingContainer fishingContainer;
    private final ChatAIContainer chatAIContainer;
    private NPCChestClassificationGoal chestClassificationGoal;
    private @Nullable ActionWheelSession actionWheel;

    /**
     * Players with an AI request currently in flight (server thread only).
     */
    private final Set<UUID> pendingChatRequests = ConcurrentHashMap.newKeySet();
    /**
     * Last request tick per player, used to rate limit retries (server thread only).
     */
    private final Map<UUID, Long> lastChatRequestTicks = new HashMap<>();
    private final Map<UUID, AIRequestContext> chatRequests = new HashMap<>();
    private AIRequestContext reflectionRequest;
    private long chatRevision;
    private int thinkingNotifyTimer = 0;
    private Vec3 chatMoveTarget;
    private long chatMoveDeadline;
    private final NPCSkillTask skillTask = new NPCSkillTask();
    private final NPCContainerAccess containerAccess = new NPCContainerAccess();
    private final NPCMemory memory = new NPCMemory();
    private final SettlementLedger settlementLedger = new SettlementLedger();
    private UUID reflectionOwner;
    private String reflectionKey;
    private long nextReflectionTick;
    private boolean reflectionInFlight;

    private AIRequestContext createAIRequest(ServerPlayer player, String input, boolean background) {
        var server = this.level().getServer();
        var originalLevel = this.level();
        var originalOwner = this.getOwnerUuid();
        var originalRole = this.getRoleType().identify();
        var originalEvent = this.skillTask.eventKey();
        var provider = ChatAIManager.chatType();
        long revision = this.chatRevision;
        var settings = ChatAIManager.requestSettings();
        return new AIRequestContext(server, () -> server.isRunning() && this.chatRevision == revision
                && this.level() == originalLevel && !this.isRemoved() && this.isAlive()
                && ChatAIManager.canChat(player) && player.level() == originalLevel && this.distanceToSqr(player) <= 1024
                && Objects.equals(originalOwner, this.getOwnerUuid()) && Objects.equals(originalRole, this.getRoleType().identify())
                && (!background || originalEvent.equals(this.skillTask.eventKey()))
                && Objects.equals(provider, ChatAIManager.chatType()) && settings.equals(ChatAIManager.requestSettings()),
                settings, input, inputBytes -> {
            var config = ReverieDreams.config();
            return server.overworld().getDataStorage().computeIfAbsent(AIUsageSavedData.TYPE).budget().reserve(
                    AIUsageBudget.today(), player.getUUID(), background, (long) inputBytes + settings.maxTokens(),
                    new AIUsageBudget.Limits(Math.clamp(config.aiDailyRequests, 0, 10000),
                            Math.clamp(config.aiPlayerDailyRequests, 0, 10000), Math.clamp(config.aiBackgroundDailyRequests, 0, 10000),
                            Math.clamp(config.aiDailyTokenBudget, 0, 100000000), Math.clamp(config.aiPlayerDailyTokenBudget, 0, 100000000)));
        }, TimeUnit.SECONDS.toNanos(180));
    }

    /**
     * Manual controls and lifecycle changes invalidate every older model decision. Server thread only.
     */
    public void cancelChatRequests() {
        this.chatRevision++;
        if (this.chatRequests == null)
            return; // Entity initialization can set role before fields are initialized.
        for (var entry : this.chatRequests.entrySet()) {
            entry.getValue().cancel();
            this.rollbackUserMessage(entry.getKey(), entry.getValue().input());
            var player = this.level().getServer() == null ? null : this.level().getServer().getPlayerList().getPlayer(entry.getKey());
            if (player != null)
                player.sendSystemMessage(Component.empty(), true);
        }
        this.chatRequests.clear();
        this.pendingChatRequests.clear();
        if (this.reflectionRequest != null)
            this.reflectionRequest.cancel();
        this.reflectionRequest = null;
        this.reflectionInFlight = false;
        this.reflectionKey = null;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        this.cancelChatRequests();
        super.remove(reason);
    }

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
        builder.define(YSM_ACTION, "");
        builder.define(YSM_ACTION_START, 0L);
        builder.define(YSM_ACTION_REVISION, 0);
    }

    @Override
    public synchronized void reverie_dreams$setModelId(String modelId) {
        String previous = this.reverie_dreams$getModelId();
        super.reverie_dreams$setModelId(modelId);
        if (!previous.equals(this.reverie_dreams$getModelId())) {
            this.actionWheel = null;
            if (!this.getYsmAction().isEmpty()) {
                this.setYsmAction("");
            }
        }
    }

    public String getYsmAction() {
        return this.entityData.get(YSM_ACTION);
    }

    public long getYsmActionStart() {
        return this.entityData.get(YSM_ACTION_START);
    }

    public int getYsmActionRevision() {
        return this.entityData.get(YSM_ACTION_REVISION);
    }

    public void setYsmAction(String animation) {
        this.entityData.set(YSM_ACTION, animation);
        this.entityData.set(YSM_ACTION_START, this.level().getGameTime());
        this.entityData.set(YSM_ACTION_REVISION, this.getYsmActionRevision() + 1);
    }

    public @Nullable UUID beginActionWheel(ServerPlayer viewer) {
        if (this.actionWheel != null && this.actionWheel.viewer() != viewer && this.isActionWheelValid(this.actionWheel)) {
            return null;
        }
        UUID token = UUID.randomUUID();
        this.actionWheel = new ActionWheelSession(viewer, token, this.level().getGameTime() + ACTION_WHEEL_TIMEOUT);
        this.getNavigation().stop();
        this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
        this.setJumping(false);
        return token;
    }

    public boolean hasActionWheel(ServerPlayer viewer, UUID token) {
        return this.actionWheel != null && this.actionWheel.viewer() == viewer && this.actionWheel.token().equals(token)
                && this.isActionWheelValid(this.actionWheel);
    }

    public void refreshActionWheel(ServerPlayer viewer, UUID token) {
        this.actionWheel = new ActionWheelSession(viewer, token, this.level().getGameTime() + ACTION_WHEEL_TIMEOUT);
    }

    public void closeActionWheel() {
        this.actionWheel = null;
    }

    public boolean canTakeRestaurantBreak() {
        return this.actionWheel == null && this.chatMoveTarget == null && !this.skillTask.active();
    }

    @Override
    protected boolean isImmobile() {
        return this.actionWheel != null || super.isImmobile();
    }

    @Override
    public void move(MoverType type, Vec3 movement) {
        super.move(type, this.actionWheel == null ? movement : new Vec3(0.0, movement.y, 0.0));
    }

    private void tickActionWheel() {
        if (this.actionWheel == null) {
            return;
        }
        if (!this.isActionWheelValid(this.actionWheel)) {
            this.closeActionWheel();
            return;
        }
        this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
    }

    private boolean isActionWheelValid(ActionWheelSession session) {
        ServerPlayer viewer = session.viewer();
        return this.level().getGameTime() < session.expiresAt() && this.isAlive() && viewer.isAlive()
                && !viewer.hasDisconnected() && !viewer.isRemoved() && viewer.level() == this.level()
                && this.isAllowOpenInventory(viewer) && viewer.isWithinEntityInteractionRange(this, 0.0);
    }

    private record ActionWheelSession(ServerPlayer viewer, UUID token, long expiresAt) {
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Vanilla sits tamed mobs whenever the owner is offline, which would block every MOVE work goal.
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this) {
            @Override
            public boolean canUse() {
                return NPCSimpleEntity.this.getNPCState() != NPCStates.WORKING && super.canUse();
            }
        });
        this.goalSelector.addGoal(1, new NPCEatPlateGoal(this, 1, 15, 1));
        this.goalSelector.addGoal(2, new NPCInventoryEatGoal(this));
        this.goalSelector.addGoal(3, new SleepAtNightGoal(this, 1.0));
        this.goalSelector.addGoal(2, new NPCChatMoveGoal(this));
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
        this.goalSelector.addGoal(6, new NPCBreedGoal(this));
        this.goalSelector.addGoal(6, new NPCSheepShearGoal(this));
        this.targetSelector.addGoal(2, new NPCAttackWithOwnerGoal(this));
        NPCHurtByTargetGoal hurtByTargetGoal = new NPCHurtByTargetGoal(this);
        this.targetSelector.addGoal(3, hurtByTargetGoal);

        this.goalSelector.addGoal(1, new NPCOpenDoorGoal(this));
        this.goalSelector.addGoal(1, new NPCOpenSilverChestGoal(this));
        this.goalSelector.addGoal(6, new NPCSmeltGoal(this));
        this.goalSelector.addGoal(5, new NPCWorkLogisticsGoal(this));
        this.goalSelector.addGoal(5, new NPCWorkCollectGoal(this));
        this.chestClassificationGoal = new NPCChestClassificationGoal(this);
        // Let safety, eating, sleep and combat interrupt a storage trip.
        this.goalSelector.addGoal(6, this.chestClassificationGoal);
        this.goalSelector.addGoal(6, new NPCCustomerGoal(this));
        this.goalSelector.addGoal(6, new NPCCookingGoal(this));
        this.goalSelector.addGoal(6, new NPCServingGoal(this));
        this.goalSelector.addGoal(6, new NPCFarmGoal(this));
        this.goalSelector.addGoal(6, new NPCFishingGoal(this));
//        this.goalSelector.addGoal(1, new NPCAutoPickItemGoal(this));
        this.goalSelector.addGoal(6, new NPCHoneycombHarvestingGoal(this));
        this.goalSelector.addGoal(6, new NPCMusicGoal(this));
        this.goalSelector.addGoal(6, new NPCGuardPatrolGoal(this));

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
    protected boolean isNavigatingWorkTarget() {
        return super.isNavigatingWorkTarget() || this.chestClassificationGoal != null && this.chestClassificationGoal.canContinueToUse();
    }

    @Override
    public void tick() {
        Level world = this.level();
        if (!world.isClientSide())
            this.tickActionWheel();
        if (!world.isClientSide())
            this.skillTask.checkBeforeWork(this);
        if (!world.isClientSide() && this.chatMoveTarget != null
                && (world.getGameTime() >= this.chatMoveDeadline || this.distanceToSqr(this.chatMoveTarget) <= 1
                || this.getNPCState() != NPCStates.NORMAL)) {
            this.chatMoveTarget = null;
            this.getNavigation().stop();
            if (this.getNPCState() == NPCStates.NORMAL) {
                this.setNPCState(NPCStates.NO_WALK);
            }
        }
        if (!world.isClientSide() && this.isSleeping() && (world.isBrightOutside() || this.tickCount % 20 == 0
                && RestaurantService.isRestaurantShift(this) && RestaurantService.findForStaff(this).isPresent())) {
            this.stopSleeping();
        }
        this.customerContainer.tick();
        this.favorabilityContainer.tick();
        this.tickChatRequests(world);
        super.tick();
        if (!world.isClientSide()) {
            this.skillTask.tick(this);
            this.tickTaskReflection();
        }
    }

    @Override
    protected void pickUpItem(ServerLevel world, ItemEntity itemEntity) {
        super.pickUpItem(world, itemEntity);
    }

    @Override
    public void setNPCState(NPCState state) {
        // Internal arrival/timeout clears the target before changing state. Manual state controls do not.
        if (this.skillTask != null && this.skillTask.active() && this.chatMoveTarget != null
                && state != NPCStates.NORMAL && state != this.getNPCState()) {
            this.skillTask.cancel(this, "Task replaced by manual state control.");
        }
        super.setNPCState(state);
    }

    public void queueTaskReflection(UUID owner, String key) {
        this.reflectionOwner = owner;
        this.reflectionKey = key;
    }

    private void tickTaskReflection() {
        if (this.tickCount % 20 != 0 || this.reflectionKey == null || this.reflectionInFlight
                || !this.pendingChatRequests.isEmpty() || this.level().getGameTime() < this.nextReflectionTick)
            return;
        if (!this.reflectionKey.equals(this.skillTask.eventKey())) {
            this.reflectionKey = null;
            return;
        }
        var server = this.level().getServer();
        var player = server.getPlayerList().getPlayer(this.reflectionOwner);
        if (player == null || player.hasDisconnected() || !player.isAlive() || !this.isOwnedBy(player)
                || player.level() != this.level() || this.distanceToSqr(player) > 1024)
            return;
        if (!ReverieDreams.config().enableAIReplacesGeneralChat || Boolean.TRUE.equals(
                PlayerSettings.get(player).get(PlayerSettings.DISABLE_CHAT_AI))) {
            this.reflectionKey = null;
            return;
        }
        var provider = ProviderRegistry.get(ChatAIManager.chatType());
        if (provider == null || !provider.supportsWorldTools() || ChatAIManager.apiKey().isBlank()) {
            this.reflectionKey = null;
            return;
        }
        String key = this.reflectionKey;
        String state = this.submitData(player, this);
        var snapshot = AIMessage.copy(this.getChatHistory(player));
        if (snapshot.isEmpty())
            snapshot.add(new AIMessage(AIMessage.SYSTEM, getStartPrompt(this, player)));
        snapshot.add(new AIMessage(AIMessage.USER, """
                [Server-observed task event; not a new owner command.]
                Explain the current verified task result or blocker in character, in the player's last language.
                Read task_status and relevant observations if useful. Suggest the exact supplies or owner action
                needed next. You may only inspect state; do not issue actions, change the plan or save memories.
                Do not claim the whole plan finished if only a step finished. Keep the reply to 1-3 sentences.
                """));
        var request = this.createAIRequest(player, "", true);
        this.reflectionRequest = request;
        this.reflectionInFlight = true;
        this.nextReflectionTick = this.level().getGameTime() + 1200;
        try {
            CompletableFuture.supplyAsync(() -> {
                        try {
                            return request.run(() -> provider.chatEvent(snapshot, this, player, state));
                        } finally {
                            request.close();
                        }
                    }, ChatProvider.EXECUTOR)
                    .whenComplete((reply, failure) -> server.execute(() -> {
                        if (this.reflectionRequest != request)
                            return;
                        this.reflectionRequest = null;
                        this.reflectionInFlight = false;
                        if (!request.validOnServer() || failure != null || reply == null || this.isRemoved() || !this.isAlive() || player.hasDisconnected()
                                || player.level() != this.level() || this.distanceToSqr(player) > 1024
                                || !this.isOwnedBy(player) || !this.pendingChatRequests.isEmpty()
                                || !key.equals(this.skillTask.eventKey()))
                            return;
                        this.chatAIContainer.addMessage(player.getUUID(), reply);
                        NPCSpeech.sayReply(this, player, reply.getContent());
                    }));
            this.reflectionKey = null;
        } catch (RejectedExecutionException exception) {
            request.cancel();
            request.close();
            this.reflectionRequest = null;
            this.reflectionInFlight = false;
            this.nextReflectionTick = this.level().getGameTime() + 100;
        }
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel world, DamageSource damageSource) {
        this.customerContainer.clearHands();
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
            this.memory.event(player.getUUID(), "hurt_by_player", "This player hurt this NPC.", this.level().getGameTime());
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
        return this.chatAIContainer.getHistory(uid);
    }

    @Override
    public void clearChatHistory() {
        this.cancelChatRequests();
        this.chatAIContainer.clearAll();
    }

    @Override
    public void clearChatHistory(ServerPlayer player) {
        this.cancelChatRequests();
        this.chatAIContainer.clear(player.getUUID());
    }

    @Override
    public CompletableFuture<Void> send(ServerPlayer player, String msg) {
        UUID uid = player.getUUID();

        if (this.level().isClientSide() || !ChatAIManager.canChat(player) || !this.isAlive() || this.isRemoved()
                || player.level() != this.level() || this.distanceToSqr(player) > 1024 || msg == null || msg.isBlank()) {
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

        if (this.reflectionRequest != null) {
            this.reflectionRequest.cancel();
            this.reflectionRequest = null;
            this.reflectionInFlight = false;
        }
        int maxHistory = Math.clamp(ReverieDreams.config().maxChatHistory, 2, 80);
        List<AIMessage> history = this.chatAIContainer.getOrCreateHistory(uid);

        if (history.isEmpty()) {
            history.add(new AIMessage(AIMessage.SYSTEM, getStartPrompt(this, player)));
        } else if (AIMessage.SYSTEM.equals(history.getFirst().getRole())) {
            history.set(0, new AIMessage(AIMessage.SYSTEM, getStartPrompt(this, player)));
        }

        history.add(new AIMessage(AIMessage.USER, msg));
        this.chatAIContainer.trim(uid, maxHistory);

        // Everything the background task needs is computed on the server thread.
        String npcState = this.submitData(player, this);
        List<AIMessage> snapshot = AIMessage.copy(history);

        this.lastChatRequestTicks.put(uid, now);
        this.pendingChatRequests.add(uid);
        var request = this.createAIRequest(player, msg, false);
        this.chatRequests.put(uid, request);
        player.sendSystemMessage(this.thinkingMessage(), true);

        try {
            return CompletableFuture
                    .supplyAsync(() -> {
                        try {
                            return request.run(() -> this.callChatAI(snapshot, player, npcState));
                        } finally {
                            request.close();
                        }
                    }, ChatProvider.EXECUTOR)
                    .thenAcceptAsync(reply -> {
                        if (!this.chatRequests.remove(uid, request))
                            return;
                        if (!request.validOnServer()) {
                            this.pendingChatRequests.remove(uid);
                            this.rollbackUserMessage(uid, msg);
                            if (!player.hasDisconnected())
                                player.sendSystemMessage(Component.empty(), true);
                            return;
                        }
                        if (request.budgetDenied()) {
                            this.pendingChatRequests.remove(uid);
                            this.rollbackUserMessage(uid, msg);
                            player.sendSystemMessage(Component.empty(), true);
                            player.sendSystemMessage(Component.translatable("gui.reverie_dreams.ai_budget_exhausted"));
                            return;
                        }
                        this.finishChatRequest(player, uid, msg, reply, maxHistory);
                    }, server)
                    .exceptionally(throwable -> {
                        server.execute(() -> {
                            if (this.chatRequests.remove(uid, request))
                                this.failChatRequest(player, uid, msg);
                        });
                        return null;
                    });
        } catch (RejectedExecutionException exception) {
            request.cancel();
            request.close();
            this.chatRequests.remove(uid, request);
            this.failChatRequest(player, uid, msg);
            return CompletableFuture.completedFuture(null);
        }
    }

    private void failChatRequest(ServerPlayer player, UUID uid, String userInput) {
        this.pendingChatRequests.remove(uid);
        this.rollbackUserMessage(uid, userInput);
        if (player.hasDisconnected()) {
            return;
        }
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
        if (!AIMessage.SYSTEM.equals(history.getFirst().getRole()))
            history.addFirst(new AIMessage(AIMessage.SYSTEM, getStartPrompt(this, player)));
        this.chatAIContainer.trim(uid, maxHistory);

        NPCSpeech.sayReply(this, player, reply.getContent());
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
        if (!world.isClientSide()) {
            long now = world.getGameTime();
            this.lastChatRequestTicks.entrySet().removeIf(entry -> now - entry.getValue() >= CHAT_REQUEST_COOLDOWN_TICKS);
            var iterator = this.chatRequests.entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                if (entry.getValue().validOnServer())
                    continue;
                entry.getValue().cancel();
                this.rollbackUserMessage(entry.getKey(), entry.getValue().input());
                this.pendingChatRequests.remove(entry.getKey());
                var player = world.getServer().getPlayerList().getPlayer(entry.getKey());
                if (player != null)
                    player.sendSystemMessage(Component.empty(), true);
                iterator.remove();
            }
            if (this.reflectionRequest != null && !this.reflectionRequest.validOnServer()) {
                this.reflectionRequest.cancel();
                this.reflectionRequest = null;
                this.reflectionInFlight = false;
            }
        }
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
        return NPCConversationContext.build(this, player);
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
        this.skillTask.load(view);
        this.containerAccess.load(view);
        this.memory.loadEncoded(view.getStringOr("ChatMemory", "{}"));
        this.settlementLedger.read(view);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        view.store("RoleType", RoleType.CODEC, this.getRoleType());
        this.customerContainer.addAdditionalSaveData(view);
        this.favorabilityContainer.addAdditionalSaveData(view);
        this.chatAIContainer.addAdditionalSaveData(view);
        this.skillTask.save(view);
        this.containerAccess.save(view);
        view.putString("ChatMemory", this.memory.saveEncoded());
        this.settlementLedger.write(view);
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
        if (!Objects.equals(this.getRoleType().identify(), roleType.identify()))
            this.cancelChatRequests();
        this.getEntityData().set(ROLE_TYPE, roleType);
    }

    @Override
    public LivingEntity getBaseEntity() {
        return this;
    }

    @Override
    public void settle(Entity source, Settlement settlement) {
        SettlementRewards.dropCoins(source, settlement.coins());
        this.settlementLedger.add(new Settlement(settlement.beneficiary(), settlement.outcome(), 0,
                settlement.experience(), settlement.feedback()));
        if (this.isAlive() && this.getNPCState() == NPCStates.WORKING) {
            this.getWorkController().status("settlement_pending");
        }
    }

    public boolean claimSettlements(ServerPlayer player) {
        if (!this.isAlive() || player.level() != this.level() || this.distanceToSqr(player) > 1024.0) {
            return false;
        }
        return this.settlementLedger.claim(player, this).isPresent();
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
