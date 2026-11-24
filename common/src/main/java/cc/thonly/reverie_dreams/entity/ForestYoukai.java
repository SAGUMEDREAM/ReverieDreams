package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.api.entity.type.FriendlyFaction;
import cc.thonly.reverie_dreams.entity.ai.YoukaiFeeding;
import cc.thonly.reverie_dreams.entity.ai.YoukaiRelations;
import cc.thonly.reverie_dreams.entity.ai.goal.ForestYoukaiFeedingGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.ForestYoukaiRetaliationGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.attack.ForestYoukaiCombatGoal;
import cc.thonly.reverie_dreams.entity.variant.YoukaiSpecies;
import cc.thonly.reverie_dreams.entity.variant.WalkingLegs;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Shared behaviour of the three forest species; deliberately has no companion inventory or taming. */
public final class ForestYoukai extends PathfinderMob implements GeoEntity, FriendlyFaction {
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(ForestYoukai.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> RANGED = SynchedEntityData.defineId(ForestYoukai.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ACTION = SynchedEntityData.defineId(ForestYoukai.class, EntityDataSerializers.INT);
    private static final int MEAT_CAPACITY = 64;
    private static final int COMBAT_BALANCE_VERSION = 1;
    private static final int DEFENCE_TICKS = 600;
    private static final double ASSIST_RADIUS = 24;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final WalkingLegs gait = new WalkingLegs();
    private final YoukaiSpecies species;
    private final Map<UUID, Long> reputationHits = new HashMap<>();
    private boolean initialized;
    private boolean naturalSpawn;
    private int storedMeat;
    private int pickupReadyAt;
    private int actionUntil;
    private int actionRevision;
    private @Nullable UUID retaliationTarget;
    private long retaliationUntil;
    private @Nullable String retaliationDimension;
    private long lastSawTarget;
    private long nextAllyAlert;
    private @Nullable UUID lastAlertTarget;

    public ForestYoukai(EntityType<? extends ForestYoukai> type, Level level, YoukaiSpecies species) {
        super(type, level);
        this.species = species;
        this.xpReward = 5;
    }

    public YoukaiSpecies species() { return this.species; }
    public int variant() { return this.entityData.get(VARIANT); }
    public boolean isRanged() { return this.entityData.get(RANGED); }
    public int storedMeat() { return this.storedMeat; }
    @Override public String getFactionId() { return "reverie_dreams:forest_youkai"; }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, 0);
        builder.define(RANGED, false);
        builder.define(ACTION, 0);
    }

    @Override protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ForestYoukaiCombatGoal(this));
        this.goalSelector.addGoal(2, new ForestYoukaiFeedingGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, .75) {
            @Override public boolean canUse() {
                return (this.mob.level().isDarkOutside() || this.mob.getRandom().nextInt(3) == 0) && super.canUse();
            }
        });
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new ForestYoukaiRetaliationGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false,
                (target, level) -> YoukaiRelations.mayHunt(this, target)));
    }

    @Override public boolean canAttack(LivingEntity target) {
        if (target instanceof ForestYoukai || target.level() != this.level() || !super.canAttack(target)) return false;
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) return false;
        return this.isRetaliatingAgainst(target) || YoukaiRelations.mayHunt(this, target);
    }

    public boolean isRetaliatingAgainst(LivingEntity target) {
        return target.getUUID().equals(this.retaliationTarget) && this.level().getGameTime() < this.retaliationUntil
                && this.level().dimension().identifier().toString().equals(this.retaliationDimension);
    }

    public @Nullable LivingEntity retaliationTarget() {
        return this.level() instanceof ServerLevel level && this.retaliationTarget != null
                && level.getEntity(this.retaliationTarget) instanceof LivingEntity target
                && this.isRetaliatingAgainst(target) ? target : null;
    }

    public boolean isWalking() { return this.gait.active(); }
    public double walkingPhase(double partialTick) { return this.gait.phase(partialTick); }

    public boolean canContinueFight(LivingEntity target) {
        return target.isAlive() && !this.isAlliedTo(target) && this.canAttack(target)
                && this.distanceToSqr(target) <= Math.pow(this.species.followRange + 8, 2)
                && this.level().getGameTime() - this.lastSawTarget < 80;
    }

    @Override public void tick() {
        double x = this.getX(), z = this.getZ();
        if (!this.level().isClientSide() && !this.initialized) this.initializeLoadout();
        super.tick();
        double distance = Math.hypot(this.getX() - x, this.getZ() - z);
        this.gait.update(distance, this.getScale(), this.onGround(), !this.isDeadOrDying()
                && !this.isPassenger() && !this.isInWater() && !this.isInLava() && distance < 4 * this.getScale(),
                this.species == YoukaiSpecies.TANUKI && this.variant() == 2 ? 16 : 12);
        if (!(this.level() instanceof ServerLevel level)) return;
        if (this.entityData.get(ACTION) != 0 && this.tickCount >= this.actionUntil) this.entityData.set(ACTION, 0);
        LivingEntity target = this.getTarget();
        if (target != null) {
            if (this.getSensing().hasLineOfSight(target)) this.lastSawTarget = level.getGameTime();
            if (!this.canContinueFight(target)) {
                this.setTarget(null);
                this.getNavigation().stop();
                this.entityData.set(ACTION, 0);
            }
        }
    }

    @Override public void setTarget(@Nullable LivingEntity target) {
        if (target != null && !this.canAttack(target)) return;
        if (target != null && target != this.getTarget()) this.lastSawTarget = this.level().getGameTime();
        super.setTarget(target);
    }

    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean accepted = super.hurtServer(level, source, amount);
        if (!accepted || amount <= 0) return accepted;
        if (source.getEntity() instanceof ServerPlayer player && level.dimension().equals(Level.OVERWORLD)) {
            long now = level.getGameTime();
            this.reputationHits.entrySet().removeIf(entry -> now - entry.getValue() >= 20);
            if (!this.reputationHits.containsKey(player.getUUID())) {
                YoukaiRelations.reputation(player).addYokai(-10);
                YoukaiRelations.invalidatePeacePlayers();
                this.reputationHits.put(player.getUUID(), now);
            }
        }
        if (source.getEntity() instanceof LivingEntity attacker && attacker.isAlive() && attacker.level() == level && !(attacker instanceof ForestYoukai)
                && !(attacker instanceof Player player && (player.isCreative() || player.isSpectator()))
                && !this.isAlliedTo(attacker)) {
            this.defendAgainst(attacker);
            if (!attacker.getUUID().equals(this.lastAlertTarget) || level.getGameTime() >= this.nextAllyAlert) {
                this.lastAlertTarget = attacker.getUUID();
                this.nextAllyAlert = level.getGameTime() + 20;
                for (ForestYoukai ally : level.getEntitiesOfClass(ForestYoukai.class,
                        this.getBoundingBox().inflate(ASSIST_RADIUS, 8, ASSIST_RADIUS),
                        ally -> ally != this && ally.isAlive() && ally.distanceToSqr(this.getX(), ally.getY(), this.getZ()) <= ASSIST_RADIUS * ASSIST_RADIUS)) {
                    ally.defendAgainst(attacker);
                }
            }
        }
        return accepted;
    }

    private void defendAgainst(LivingEntity attacker) {
        if (!this.isAlive() || !super.canAttack(attacker) || this.isAlliedTo(attacker)) return;
        this.retaliationTarget = attacker.getUUID();
        this.retaliationUntil = this.level().getGameTime() + DEFENCE_TICKS;
        this.retaliationDimension = this.level().dimension().identifier().toString();
        this.lastSawTarget = this.level().getGameTime();
        this.setTarget(attacker);
    }

    @Override public boolean killedEntity(ServerLevel level, LivingEntity victim, DamageSource source) {
        boolean result = super.killedEntity(level, victim, source);
        if (result && YoukaiRelations.isHuman(victim) && level.getGameRules().get(GameRules.MOB_DROPS)) {
            ItemEntity meat = new ItemEntity(level, victim.getX(), victim.getY(), victim.getZ(),
                    new ItemStack(Items.BEEF, 1 + this.random.nextInt(2)));
            meat.setDefaultPickUpDelay();
            level.addFreshEntity(meat);
        }
        return result;
    }

    public boolean canAcceptMeat(ItemEntity item) {
        return this.level() instanceof ServerLevel level && this.isAlive() && this.storedMeat < MEAT_CAPACITY
                && !item.isRemoved() && !item.hasPickUpDelay() && YoukaiFeeding.isMeat(item.getItem())
                && YoukaiFeeding.canAccept(item, level);
    }

    @Override public boolean canPickUpLoot() { return true; }
    @Override public boolean wantsToPickUp(ServerLevel level, ItemStack stack) {
        return this.storedMeat < MEAT_CAPACITY && YoukaiFeeding.isMeat(stack);
    }

    @Override protected void pickUpItem(ServerLevel level, ItemEntity item) {
        if (this.tickCount < this.pickupReadyAt || !this.canAcceptMeat(item)) return;
        if (!YoukaiFeeding.accept(item, level)) return;
        boolean fed = YoukaiFeeding.feeder(item) != null;
        int count = fed ? 1 : Math.min(item.getItem().getCount(), MEAT_CAPACITY - this.storedMeat);
        this.storedMeat += count;
        this.onItemPickup(item);
        this.take(item, count);
        item.getItem().shrink(count);
        if (item.getItem().isEmpty()) item.discard();
        else item.setItem(item.getItem());
        this.pickupReadyAt = this.tickCount + 20;
        if (fed) {
            this.playSound(SoundEvents.GENERIC_EAT.value(), .5F, 1.1F);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getEyeY(), this.getZ(), 3, .25, .15, .25, .01);
        }
    }

    @Override protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        int count = this.storedMeat / 2;
        this.storedMeat = 0;
        if (count > 0) this.spawnAtLocation(level, new ItemStack(Items.BEEF, count));
    }

    public void beginAction(int action) {
        this.actionRevision = (this.actionRevision + 1) & 0x1fffffff;
        this.entityData.set(ACTION, (this.actionRevision << 2) | action);
        this.actionUntil = this.tickCount + (action == 3 ? 18 : action == 1 ? 11 : 12);
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // The action controller only authors upper-body tracks; locomotion keeps running underneath it.
        controllers.add(new AnimationController<ForestYoukai>("forest_youkai_locomotion", 3, state -> {
            state.controller().setTransitionTicks(this.isWalking() ? 0 : 3);
            return state.setAndContinue(RawAnimation.begin().thenLoop(this.isWalking() ? "animation.walk" : "animation.idle"));
        }) {
            @Override
            protected boolean checkControllerState(ForestYoukai actor, GeoRenderState state,
                                                    AnimatableManager<ForestYoukai> manager, GeoModel<ForestYoukai> model) {
                boolean result = super.checkControllerState(actor, state, manager, model);
                if (!actor.isWalking() || this.timeline == null) return result;
                this.timelineTime = actor.walkingPhase(state.getPartialTick());
                this.transitionFromPoint = null;
                this.animationPoint = this.timeline.createAnimationPoint(this.timelineTime, null, this.easingOverride);
                return this.isAnimatingBones();
            }
        });
        int[] previousAction = {-1};
        controllers.add(new AnimationController<ForestYoukai>("forest_youkai_action", 2, state -> {
            int token = this.entityData.get(ACTION);
            if (token != previousAction[0]) state.controller().reset();
            previousAction[0] = token;
            String hand = this.getMainArm() == HumanoidArm.LEFT ? "_left" : "";
            String clip = this.isUsingItem() ? "animation.use_item" + hand : switch (token & 3) {
                case 1 -> "animation.melee" + hand;
                case 2 -> "animation.ranged" + hand;
                case 3 -> "animation.use_item" + hand;
                default -> null;
            };
            if (clip == null) return PlayState.STOP;
            RawAnimation raw = RawAnimation.begin();
            return state.setAndContinue(this.isUsingItem() || (token & 3) == 3 ? raw.thenLoop(clip) : raw.thenPlay(clip));
        }));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.geoCache; }

    private void initializeLoadout() {
        this.initialized = true;
        this.entityData.set(VARIANT, this.random.nextInt(3));
        this.entityData.set(RANGED, this.random.nextBoolean());
        ItemStack weapon = this.isRanged() ? DanmakuTypes.random(DanmakuTypes.RICE).create()
                : new ItemStack(switch (this.random.nextInt(3)) {
                    case 0 -> Items.STONE_SWORD; case 1 -> Items.GOLDEN_SWORD; default -> Items.IRON_SWORD;
                });
        this.setItemSlot(EquipmentSlot.MAINHAND, weapon);
        this.setDropChance(EquipmentSlot.MAINHAND, .085F);
    }

    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                   EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        this.naturalSpawn = reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION;
        if (!this.initialized) this.initializeLoadout();
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    @Override public boolean removeWhenFarAway(double distanceSquared) {
        return this.naturalSpawn && !this.hasCustomName() && !this.isPersistenceRequired()
                && !this.isLeashed() && !this.isPassenger() && !this.isVehicle();
    }

    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(VARIANT, Math.clamp(input.getIntOr("YoukaiVariant", 0), 0, 2));
        this.entityData.set(RANGED, input.getBooleanOr("YoukaiRanged", false));
        this.initialized = input.getBooleanOr("YoukaiInitialized", true);
        this.naturalSpawn = input.getBooleanOr("NaturalYoukaiSpawn", false);
        this.storedMeat = Math.clamp(input.getIntOr("YoukaiMeat", 0), 0, MEAT_CAPACITY);
        this.retaliationTarget = input.read("YoukaiRetaliationTarget", UUIDUtil.CODEC).orElse(null);
        this.retaliationUntil = input.getLongOr("YoukaiRetaliationUntil", 0);
        this.retaliationDimension = input.getStringOr("YoukaiRetaliationDimension", "");
        if (input.getIntOr("YoukaiCombatBalanceVersion", 0) < COMBAT_BALANCE_VERSION) {
            this.upgradeDefaultAttribute(Attributes.MOVEMENT_SPEED, switch (this.species) {
                case CAT -> .30; case TANUKI -> .24; case FOX -> .27;
            }, this.species.movementSpeed);
            this.upgradeDefaultAttribute(Attributes.FOLLOW_RANGE, switch (this.species) {
                case CAT -> 18; case TANUKI -> 16; case FOX -> 24;
            }, this.species.followRange);
            this.upgradeDefaultAttribute(Attributes.KNOCKBACK_RESISTANCE, switch (this.species) {
                case CAT -> 0; case TANUKI -> .25; case FOX -> .05;
            }, this.species.knockbackResistance);
        }
    }

    private void upgradeDefaultAttribute(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> type,
                                         double oldDefault, double newDefault) {
        var attribute = this.getAttribute(type);
        if (attribute != null && Math.abs(attribute.getBaseValue() - oldDefault) < 1.0e-6) attribute.setBaseValue(newDefault);
    }

    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("YoukaiVariant", this.variant());
        output.putBoolean("YoukaiRanged", this.isRanged());
        output.putBoolean("YoukaiInitialized", this.initialized);
        output.putBoolean("NaturalYoukaiSpawn", this.naturalSpawn);
        output.putInt("YoukaiMeat", this.storedMeat);
        output.putInt("YoukaiCombatBalanceVersion", COMBAT_BALANCE_VERSION);
        output.storeNullable("YoukaiRetaliationTarget", UUIDUtil.CODEC, this.retaliationTarget);
        output.putLong("YoukaiRetaliationUntil", this.retaliationUntil);
        if (this.retaliationDimension != null) output.putString("YoukaiRetaliationDimension", this.retaliationDimension);
    }
}
