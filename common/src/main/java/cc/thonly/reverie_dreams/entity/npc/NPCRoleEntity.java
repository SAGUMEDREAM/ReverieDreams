package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.entity.ExperienceOrbEntityDataModifier;
import cc.thonly.reverie_dreams.api.entity.callback.CompatGoalAddedCallback;
import cc.thonly.reverie_dreams.data.npc.NPCRole;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.entity.ai.goal.*;
import cc.thonly.reverie_dreams.entity.ai.goal.work.*;
import cc.thonly.reverie_dreams.api.entity.type.ChatAIEntity;
import cc.thonly.reverie_dreams.openai.AIMessage;
import cc.thonly.reverie_dreams.openai.ChatAIData;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
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
@SuppressWarnings("resource")
@Getter
@Setter
public class NPCRoleEntity extends BaseNPCLikeEntity implements Leashable, ChatAIEntity<NPCRoleEntity> {
    public static final EntityDataAccessor<NPCRole> ROLE_TYPE = SynchedEntityData.defineId(NPCRoleEntity.class, NPCRole.SERIALIZER);
    private ChatAIData chatAIData;

    public NPCRoleEntity(EntityType<? extends NPCRoleEntity> entityType, Level world) {
        super(entityType, world);
        this.chatAIData = new ChatAIData(new HashMap<>());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ROLE_TYPE, NPCRole.empty());
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(1, new NPCEatFoodDisplayGoal(this, 1, 15, 1));
        this.goalSelector.addGoal(2, new EatGoal(this));
        this.goalSelector.addGoal(3, new SleepAtNightGoal(this, 1.0));

        this.goalSelector.addGoal(4, new NPCTemptGoal(this, 1.2, stack -> stack.is(RDItemTags.ROLE_TAME_FOOD), false));
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
        this.targetSelector.addGoal(3, new NPCHurtByTargetGoal(this).setAlertOthers());

        this.goalSelector.addGoal(1, new NPCOpenDoorGoal(this));
        this.goalSelector.addGoal(1, new NPCOpenSilverChestGoal(this));
        this.goalSelector.addGoal(1, new NPCSmeltGoal(this));
        this.goalSelector.addGoal(1, new NPCChestClassificationGoal(this));
        this.goalSelector.addGoal(1, new NPCFarmGoal(this));
        this.goalSelector.addGoal(1, new NPCAutoPickItemGoal(this));
        this.goalSelector.addGoal(2, new NPCCloseToCropGoal(this, 1));

        for (Tuple<Integer, Goal> tuple : CompatGoalAddedCallback.EVENT.invoker().handle(this)) {
            this.goalSelector.addGoal(tuple.getA(), tuple.getB());
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
        this.attractNearbyExperienceOrbs();
        super.tick();
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel world, DamageSource damageSource) {
//        super.drop(world, damageSource);
    }

    public void attractNearbyExperienceOrbs() {
        if (this.level().isClientSide())
            return; // 只在服务端处理

        double radius = 7.0;
        List<ExperienceOrb> orbs = this.level().getEntitiesOfClass(
                ExperienceOrb.class,
                this.getBoundingBox().inflate(radius),
                Entity::isAlive
        );

        for (ExperienceOrb orb : orbs) {
            ((ExperienceOrbEntityDataModifier) (Object) orb).reverie_dreams$setNPCTarget(this);
        }
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
        return super.hurtServer(world, source, amount);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        Level world = this.level();
        if (world.isClientSide() || hand != InteractionHand.MAIN_HAND || !(world instanceof ServerLevel serverWorld) || !(player instanceof ServerPlayer serverPlayerEntity)) {
            return super.mobInteract(player, hand);
        }
        return NPCRoleInteractionEvents.emit(serverWorld, serverPlayerEntity, hand, this);
    }

    @Override
    public List<AIMessage> getChatHistory(ServerPlayer player) {
        UUID uid = player.getUUID();
        return this.chatAIData.getHistories().getOrDefault(uid, new ArrayList<>());
    }

    @Override
    public void clearChatHistory() {
        this.chatAIData.getHistories().clear();
    }

    @Override
    public void clearChatHistory(ServerPlayer player) {
        this.chatAIData.getHistories().remove(player.getUUID());
    }

    @Override
    public CompletableFuture<Void> send(ServerPlayer player, String msg) {
        UUID uid = player.getUUID();

        List<AIMessage> history = this.chatAIData.getHistories()
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
        if (server == null) return CompletableFuture.completedFuture(null);

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
    public String submitData(ServerPlayer player, NPCRoleEntity entity) {
        Map<String, String> kw = new LinkedHashMap<>();
        kw.put("Health", "%s/%s".formatted(this.getHealth(), this.getMaxHealth()));
        kw.put("Nutrition", "%s/20".formatted(this.getNutrition()));
        kw.put("Saturation", "%s/20".formatted(this.getSaturation()));
        kw.put("StoredExperience", "%s".formatted(this.getStoredExperience()));
        kw.put("Goodwill", "%s".formatted(this.getGoodwill()));
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
        ChatAIEntity<NPCRoleEntity> chatAIEntity = ChatAIManager.of(this);
        Holder<Dialog> holder = ChatAIManager.buildDialog(player, chatAIEntity);
        if (holder != null) {
            player.openDialog(holder);
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        view.read("RoleType", NPCRole.BY_REGISTRY).ifPresent(this::setRoleType);
        this.updateRoleData();
        view.read("ChatAIData", ChatAIData.CODEC).ifPresent(data -> this.chatAIData = data);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        view.store("RoleType", NPCRole.BY_REGISTRY, this.getRoleType());
        view.store("ChatAIData", ChatAIData.CODEC, this.chatAIData);
    }

    private void updateRoleData() {
        if (this.getRoleType().isVirtual()) {
            SkinType skinType = this.getSkinType();
            List<NPCRole> list = RegistryImpls.NPC_ROLE.stream().filter(role -> Objects.equals(skinType, role.getSkinType())).toList();
            if (!list.isEmpty()) {
                this.setRoleType(list.getFirst());
            }
        }
    }

    public NPCRole getRoleType() {
        return this.getEntityData().get(ROLE_TYPE);
    }

    public void setRoleType(NPCRole roleType) {
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
