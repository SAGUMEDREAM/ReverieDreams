package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.api.entity.type.FriendlyFaction;
import cc.thonly.reverie_dreams.api.entity.type.VariantData;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.KeepInventoryTypes;
import cc.thonly.reverie_dreams.entity.variant.TenguAppearance;
import cc.thonly.reverie_dreams.registry.content.skin.MobSkinTypes;
import cc.thonly.reverie_dreams.server.dialog.NPCSpeech;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import cc.thonly.reverie_dreams.entity.ai.goal.util.EntityTargetUtil;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

@SuppressWarnings("UnstableApiUsage")
public abstract class Tengu extends BaseNPCLikeEntity implements GeoEntity, FriendlyFaction, VariantData {
    public static final String WALKING_LEGS_CLIP = "animation.walk_legs";
    private static final String NEUTRAL_LEGS_CLIP = "animation.walk_legs_neutral";
    private static final String NATURAL_SPAWN = "NaturalTenguSpawn";
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(Tengu.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FEMALE = SynchedEntityData.defineId(Tengu.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ACTION = SynchedEntityData.defineId(Tengu.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> ACTION_START = SynchedEntityData.defineId(Tengu.class, EntityDataSerializers.LONG);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    protected BlockPos post;
    private boolean naturalSpawn;
    private final TenguLocomotion.Gait gait = new TenguLocomotion.Gait();

    public abstract boolean isCrow();

    protected Tengu(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level, MobSkinTypes.DEFAULT);
        this.xpReward = 5;
        if (!level.isClientSide()) {
            this.entityData.set(VARIANT, this.random.nextInt(4));
            this.setFemale(this.random.nextBoolean());
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, 0);
        builder.define(FEMALE, false);
        builder.define(ACTION, 0);
        builder.define(ACTION_START, -100L);
    }

    public boolean isFemale() {
        return this.entityData.get(FEMALE);
    }

    public void setFemale(boolean female) {
        this.entityData.set(FEMALE, female);
    }

    public TenguAppearance appearance() {
        return TenguAppearance.of(this.isCrow(), this.isFemale(), this.entityData.get(VARIANT));
    }

    @Override
    public void setVariantData(Identifier id) {
        this.entityData.set(VARIANT, TenguAppearance.of(this.isCrow(), id).index());
    }

    @Override
    public Identifier getVariantData() {
        return TenguAppearance.of(this.isCrow(), false, this.entityData.get(VARIANT)).modelId();
    }

    public int actionDuration(int action) {
        return switch (action) {
            case 1, 3, 4 -> 20;
            case 2 -> 14;
            default -> 0;
        };
    }

    public int actionElapsed() {
        return (int) Math.max(0, this.level().getGameTime() - this.entityData.get(ACTION_START));
    }

    public int action() {
        int a = this.entityData.get(ACTION);
        return this.actionElapsed() < this.actionDuration(a) ? a : 0;
    }

    public long actionRevision() {
        return this.entityData.get(ACTION_START);
    }

    public void beginAction(int action) {
        if (this.level().isClientSide() || this.isDeadOrDying())
            return;
        this.entityData.set(ACTION, action);
        this.entityData.set(ACTION_START, this.level().getGameTime());
    }

    public String modelAnimation() {
        if (this.isDeadOrDying())
            return "death";
        if (this.hurtTime > 0)
            return "hurt";
        return switch (this.action()) {
            case 1 -> "melee";
            case 2 -> "ranged";
            case 3 -> "block";
            case 4 -> this.photoAnimation();
            default -> this.locomotionAnimation();
        };
    }

    protected String locomotionAnimation() {
        return this.gait.active() ? "walk" : "idle";
    }

    protected String photoAnimation() {
        return "photo";
    }

    protected int locomotionAnimationElapsed() {
        return 0;
    }

    protected void updateFlightAnimation(boolean grounded, boolean eligible, double horizontal, double vertical) {
    }

    public boolean hasWalkingLegAnimation() {
        return !this.isCrow() && !this.isDeadOrDying() && this.gait.active();
    }

    public double walkingLegPhase(double partialTick) {
        return this.gait.phase(partialTick);
    }

    private static boolean looping(String name) {
        return switch (name) {
            case "idle", "walk", "fly", "glide" -> true;
            default -> false;
        };
    }

    /**
     * Seconds into an explicitly sampled clip, or -1 for a clip that plays on its own.
     */
    public double modelAnimationTime(String name, double partialTick) {
        return switch (name) {
            case "walk" -> this.walkingLegPhase(partialTick);
            case "idle", "fly", "glide", "hurt" -> -1;
            case "takeoff", "land" -> (this.locomotionAnimationElapsed() + partialTick) / 20;
            case "death" -> (this.deathTime + partialTick) / 20;
            default -> (this.actionElapsed() + partialTick) / 20;
        };
    }

    public boolean restartsModelAnimation(String name, long lastRevision, int lastHurtTime) {
        return switch (name) {
            case "melee", "ranged", "block", "photo", "photo_air" -> this.actionRevision() != lastRevision;
            case "hurt" -> this.hurtTime > lastHurtTime;
            default -> false;
        };
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        String[] previous = {null};
        long[] revision = {-1};
        int[] hurt = {0};
        controllers.add(new AnimationController<Tengu>("tengu", 2, state -> {
            String next = this.modelAnimation();
            if (next.equals(previous[0]) && this.restartsModelAnimation(next, revision[0], hurt[0])) {
                state.controller().reset();
            }
            state.controller().setTransitionTicks(this.modelAnimationTime(next, 0) < 0 ? 2 : 0);
            previous[0] = next;
            revision[0] = this.actionRevision();
            hurt[0] = this.hurtTime;
            var raw = RawAnimation.begin();
            return state.setAndContinue(looping(next) ? raw.thenLoop("animation." + next) : raw.thenPlayAndHold("animation." + next));
        }) {
            @Override
            protected boolean checkControllerState(Tengu actor, GeoRenderState state,
                                                   AnimatableManager<Tengu> manager, GeoModel<Tengu> model) {
                boolean result = super.checkControllerState(actor, state, manager, model);
                double time = actor.modelAnimationTime(actor.modelAnimation(), state.getPartialTick());
                if (this.timeline == null || time < 0)
                    return result;
                this.timelineTime = Math.clamp(time, 0, this.timeline.lastAnimationEndTime());
                this.transitionFromPoint = null;
                this.animationPoint = this.timeline.createAnimationPoint(this.timelineTime, null, this.easingOverride);
                return this.isAnimatingBones();
            }
        });
        if (!this.isCrow())
            controllers.add(this.wolfLegController());
    }

    private AnimationController<Tengu> wolfLegController() {
        return new AnimationController<Tengu>("walking_legs", 2, state -> {
            if (this.isDeadOrDying())
                return PlayState.STOP;
            boolean walking = this.hasWalkingLegAnimation();
            state.controller().setTransitionTicks(walking ? 0 : 2);
            return state.setAndContinue(RawAnimation.begin().thenLoop(walking ? WALKING_LEGS_CLIP : NEUTRAL_LEGS_CLIP));
        }) {
            @Override
            protected boolean checkControllerState(Tengu actor, GeoRenderState state,
                                                   AnimatableManager<Tengu> manager, GeoModel<Tengu> model) {
                boolean result = super.checkControllerState(actor, state, manager, model);
                if (!actor.hasWalkingLegAnimation() || this.timeline == null)
                    return result;
                this.timelineTime = actor.walkingLegPhase(state.getPartialTick());
                this.transitionFromPoint = null;
                this.animationPoint = this.timeline.createAnimationPoint(this.timelineTime, null, this.easingOverride);
                return this.isAnimatingBones();
            }
        };
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 7));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(Tengu.class));
    }

    @Override
    public void tick() {
        double x = this.getX(), y = this.getY(), z = this.getZ();
        super.tick();
        double horizontal = Math.hypot(this.getX() - x, this.getZ() - z);
        double vertical = this.getY() - y;
        boolean eligible = !this.isDeadOrDying() && !this.isInWater() && !this.isInLava()
                && !this.isPassenger() && !this.isSleeping();
        this.gait.update(horizontal, this.getScale(), eligible && this.onGround()
                && !this.isOrderedToSit() && Math.abs(vertical) <= 1);
        this.updateFlightAnimation(this.onGround(), eligible, horizontal, vertical);
        if (!this.level().isClientSide()) {
            if (this.post == null)
                this.post = this.blockPosition();
            if (this.getTarget() != null && this.distanceToSqr(this.post.getX() + .5, this.post.getY(), this.post.getZ() + .5) > 32 * 32) {
                this.setTarget(null);
                this.getNavigation().stop();
            }
            if (this.getTarget() != null && this.tickCount % 20 == 0) {
                int count = 0;
                for (var ally : this.level().getEntitiesOfClass(Tengu.class, this.getBoundingBox().inflate(12), e -> e != this && e.isAlive() && e.getTarget() == null)) {
                    if (count++ >= 8)
                        break;
                    if (EntityTargetUtil.canAttack(this.getTarget(), ally))
                        ally.setTarget(this.getTarget());
                }
            }
        }
    }

    @Override
    public String getFactionId() {
        return "tengu";
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        EntitySpawnReason reason, SpawnGroupData groupData) {
        this.naturalSpawn = reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION;
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    public boolean removeWhenFarAway(double distanceSquared) {
        return this.naturalSpawn && this.post == null && !this.isTame() && !this.hasCustomName()
                && !this.isLeashed() && !this.isPersistenceRequired() && !this.isPassenger() && !this.isVehicle();
    }

    public boolean isNaturallySpawned() {
        return this.naturalSpawn;
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setVariantData(Identifier.tryParse(input.getStringOr("Variant", TenguAppearance.of(this.isCrow(), 0).modelId().toString())));
        this.setFemale(input.getBooleanOr("Female", false));
        this.post = input.read("TenguPost", BlockPos.CODEC).orElse(null);
        this.naturalSpawn = input.getBooleanOr(NATURAL_SPAWN, false);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Variant", this.getVariantData().toString());
        output.putBoolean("Female", this.isFemale());
        if (this.post != null)
            output.store("TenguPost", BlockPos.CODEC, this.post);
        output.putBoolean(NATURAL_SPAWN, this.naturalSpawn);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && player.getItemInHand(hand).isEmpty() && !player.isShiftKeyDown() && this.getTarget() == null) {
            if (player instanceof ServerPlayer listener) {
                NPCSpeech.sayTo(this, listener, Component.translatable(this.isCrow()
                        ? "reverie_dreams.tengu.crow_greeting" : "reverie_dreams.tengu.guard_greeting"));
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public Boolean canPickItem() {
        return false;
    }

    @Override
    public KeepInventoryTypes getKeepInventoryType() {
        return KeepInventoryTypes.NOT_DROP_ANY;
    }
}
