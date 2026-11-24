package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.ReverieDreams;

import cc.thonly.reverie_dreams.api.entity.type.FriendlyFaction;
import cc.thonly.reverie_dreams.api.player.PlayerComponentManager;
import cc.thonly.reverie_dreams.entity.ai.goal.YoukaiRabbitGatherGoal;
import cc.thonly.reverie_dreams.entity.villager.YoukaiRabbitTradeGui;
import cc.thonly.reverie_dreams.server.player.FactionReputationComponent;
import cc.thonly.reverie_dreams.server.dialog.NPCSpeech;
import cc.thonly.reverie_dreams.entity.ai.goal.attack.YoukaiRabbitCombatGoal;
import cc.thonly.reverie_dreams.entity.variant.YoukaiRabbitTradeState;
import cc.thonly.reverie_dreams.entity.variant.YoukaiRabbitSupportState;
import cc.thonly.reverie_dreams.entity.variant.YoukaiRabbitAppearance;
import cc.thonly.reverie_dreams.entity.variant.WalkingLegs;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import cc.thonly.reverie_dreams.world.LandEntitySpawnRules;

public final class YoukaiRabbit extends PathfinderMob implements GeoEntity, FriendlyFaction {
    public static final EntityDataAccessor<Boolean> FEMALE = SynchedEntityData.defineId(YoukaiRabbit.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(YoukaiRabbit.class, EntityDataSerializers.INT);
    private static final byte ACCEPT_EVENT = 70, TALK_EVENT = 71, MELEE_EVENT = 72, RANGED_EVENT = 73;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final YoukaiRabbitTradeState tradeState = new YoukaiRabbitTradeState();
    private @Nullable YoukaiRabbitTradeGui tradeGui;
    private final YoukaiRabbitSupportState support = new YoukaiRabbitSupportState();
    private final WalkingLegs walkingLegs = new WalkingLegs();
    private @Nullable BlockPos home;
    private long angerUntil;
    private long lastSawTarget;
    private String action;
    private int actionUntil;
    private int revision;
    private int previousHurt;
    private int airTicks;
    private int landUntil;

    public YoukaiRabbit(EntityType<? extends YoukaiRabbit> type, Level level) {
        super(type, level);
        this.setFemale(true);
        this.setVariant(this.random.nextInt(YoukaiRabbitAppearance.COUNT));
        this.xpReward = 2;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.MOVEMENT_SPEED, .23)
                .add(Attributes.ATTACK_DAMAGE, 2).add(Attributes.FOLLOW_RANGE, 16).add(Attributes.TEMPT_RANGE, 10);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder); builder.define(FEMALE, false); builder.define(VARIANT, 0);
    }
    public boolean isFemale() { return this.entityData.get(FEMALE); }
    public void setFemale(boolean female) { this.entityData.set(FEMALE, female); }
    public int getVariant() { return this.entityData.get(VARIANT); }
    public void setVariant(int variant) { this.entityData.set(VARIANT, Math.clamp(variant, 0, YoukaiRabbitAppearance.COUNT - 1)); }
    public YoukaiRabbitTradeState tradeState() { return this.tradeState; }
    public long tradeDay() { return Math.floorDiv(((ServerLevel)this.level()).getOverworldClockTime(), 24000L); }
    public boolean isTrading() { return this.tradeGui != null; }
    public boolean isCurrentTrade(YoukaiRabbitTradeGui gui) { return this.tradeGui == gui; }
    public boolean withinRabbitHome(BlockPos pos) { return this.home == null || pos.distSqr(this.home) <= 24 * 24; }
    @Override public String getFactionId() { return ReverieDreams.MOD_ID + ":house_of_eternity"; }
    @Override public boolean canAttack(LivingEntity target) {
        return !(target instanceof YoukaiRabbit) && !(target instanceof RabbitUnit) && super.canAttack(target);
    }
    public boolean canTradeWith(ServerPlayer player) {
        return this.isAlive() && !this.support.isReinforcement() && this.getTarget() == null && this.hurtTime == 0
                && player.isAlive() && !player.isSpectator() && player.level() == this.level() && this.distanceToSqr(player) <= 64;
    }
    public void closeTrade(YoukaiRabbitTradeGui gui) {
        if (this.tradeGui == gui) this.tradeGui = null;
    }
    private void closeTrading() {
        if (this.tradeGui != null) { var gui = this.tradeGui; this.tradeGui = null; gui.close(); }
    }
    public void completeTrade(ServerPlayer player, YoukaiRabbitTradeState.Offer offer, long day) {
        if (!this.tradeState.complete(offer, day)) return;
        FactionReputationComponent reputation = PlayerComponentManager.serverAccess()
                .getOrCreatePlayerComponent(player, FactionReputationComponent.class).get();
        reputation.addHouseOfEternity(1);
        this.beginAction("talk");
        this.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, .4F, 1.2F);
    }
    public int animationRevision() { return this.revision; }
    public double walkAnimationSpeed() { return Math.clamp(this.getDeltaMovement().horizontalDistance() / .085, .6, 1.7); }
    public boolean hasWalkingLegAnimation() { return !this.isDeadOrDying() && this.walkingLegs.active(); }
    public double walkingLegPhase(double partialTick) { return this.walkingLegs.phase(partialTick); }

    @Override protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new YoukaiRabbitCombatGoal(this));
        this.goalSelector.addGoal(2, new Goal() {
            { this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP)); }
            @Override public boolean canUse() { return YoukaiRabbit.this.isInteracting() || YoukaiRabbit.this.isTrading(); }
            @Override public void tick() { YoukaiRabbit.this.getNavigation().stop(); }
        });
        this.goalSelector.addGoal(3, new MoveTowardsRestrictionGoal(this, .85));
        this.goalSelector.addGoal(4, new YoukaiRabbitGatherGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, .65));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    public boolean isInteracting() {
        return this.getTarget() == null && this.tickCount < this.actionUntil && ("accept".equals(this.action) || "talk".equals(this.action));
    }

    public void beginAction(String action) {
        byte event = switch (action) { case "accept" -> ACCEPT_EVENT; case "talk" -> TALK_EVENT; case "melee" -> MELEE_EVENT; case "ranged" -> RANGED_EVENT; default -> throw new IllegalArgumentException(action); };
        this.startAction(action);
        if (!this.level().isClientSide()) this.level().broadcastEntityEvent(this, event);
    }
    private void startAction(String action) {
        this.action = action;
        this.actionUntil = this.tickCount + switch (action) { case "accept" -> 16; case "talk" -> 24; case "melee" -> 10; default -> 12; };
        this.revision++;
    }
    @Override public void handleEntityEvent(byte event) {
        switch (event) {
            case ACCEPT_EVENT -> this.startAction("accept"); case TALK_EVENT -> this.startAction("talk");
            case MELEE_EVENT -> this.startAction("melee"); case RANGED_EVENT -> this.startAction("ranged");
            default -> super.handleEntityEvent(event);
        }
    }

    public String modelAnimation() {
        if (this.isDeadOrDying()) return "death";
        if (this.isAttacking()) return this.action;
        if (this.hurtTime > 0) return "hurt";
        if (this.tickCount < this.actionUntil) return this.action;
        if (!this.onGround() && !this.isInWater() && !this.isInLava()) return this.getDeltaMovement().y > .05 ? "jump" : "fall";
        if (this.tickCount < this.landUntil) return "land";
        if (this.getDeltaMovement().horizontalDistanceSqr() > .0001) return "walk";
        if (this.getTarget() == null && this.tickCount % 320 >= 240 && this.tickCount % 320 < 272) return "observe";
        if (this.getTarget() == null && this.tickCount % 640 >= 560 && this.tickCount % 640 < 584) return "gather";
        return "idle";
    }
    private boolean isAttacking() {
        return this.tickCount < this.actionUntil && ("melee".equals(this.action) || "ranged".equals(this.action));
    }
    public static boolean loopingAnimation(String name) { return "idle".equals(name) || "walk".equals(name); }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        String[] previous = {null}; int[] previousRevision = {-1};
        controllers.add(new AnimationController<YoukaiRabbit>("rabbit", 2, state -> {
            String next = this.modelAnimation();
            if (next == null) return PlayState.STOP;
            if (!next.equals(previous[0]) || (!loopingAnimation(next) && previousRevision[0] != this.revision)) state.controller().reset();
            previous[0] = next; previousRevision[0] = this.revision;
            state.controller().setAnimationSpeed("walk".equals(next) ? this.walkAnimationSpeed() : 1);
            var raw = RawAnimation.begin();
            return state.setAndContinue(loopingAnimation(next) ? raw.thenLoop(next) : raw.thenPlayAndHold(next));
        }));
        controllers.add(WalkingLegs.<YoukaiRabbit>controller(this::hasWalkingLegAnimation, this::walkingLegPhase));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.geoCache; }

    @Override public void tick() {
        double x = this.getX(), z = this.getZ();
        super.tick();
        if (this.hurtTime > this.previousHurt && !this.isAttacking()) { this.revision++; this.actionUntil = 0; }
        this.previousHurt = this.hurtTime;
        if (!this.onGround() && !this.isInWater() && !this.isInLava()) this.airTicks++;
        else { if (this.airTicks >= 2) this.landUntil = this.tickCount + 6; this.airTicks = 0; }
        this.walkingLegs.update(Math.hypot(this.getX() - x, this.getZ() - z), this.getScale(),
                this.onGround() && !this.isInWater() && !this.isInLava() && !this.isPassenger(),
                !this.isDeadOrDying() && (this.hurtTime > 0 || "walk".equals(this.modelAnimation())));
        if (!(this.level() instanceof ServerLevel level)) return;
        if (this.getType() == RDEntityTypes.LEGACY_MOON_RABBIT.value()) {
            this.convertTo(RDEntityTypes.YOUKAI_RABBIT.value(), ConversionParams.single(this, true, true), replacement -> {
                replacement.setFemale(this.isFemale());
                replacement.setVariant(this.getVariant());
                replacement.setHealth(Math.min(this.getHealth(), replacement.getMaxHealth()));
                replacement.tradeState.restore(this.tradeState.day(), this.tradeState.counts());
                replacement.support.restore(this.support.nextCall(), this.support.isReinforcement(), this.support.expires());
            });
            return;
        }
        if (this.support.expired(level.getGameTime())) {
            level.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + .8, this.getZ(), 6, .3, .4, .3, .02);
            this.discard();
            return;
        }
        if (this.home == null) { this.home = this.blockPosition(); this.setHomeTo(this.home, 16); }
        LivingEntity target = this.getTarget();
        if (target != null) {
            if (this.getSensing().hasLineOfSight(target)) this.lastSawTarget = level.getGameTime();
            if (!this.canDefendAgainst(target)) { this.setTarget(null); this.getNavigation().stop(); this.actionUntil = 0; }
        }
    }

    public boolean canDefendAgainst(LivingEntity target) {
        if (!target.isAlive() || !this.canAttack(target) || this.isAlliedTo(target)) return false;
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) return false;
        long now = this.level().getGameTime();
        return now < this.angerUntil && now - this.lastSawTarget < 80 && this.distanceToSqr(target) <= 16 * 16
                && (this.home == null || target.blockPosition().distSqr(this.home) <= 24 * 24);
    }
    private void defendFrom(LivingEntity attacker) {
        if (!this.canAttack(attacker) || this.isAlliedTo(attacker)) return;
        this.closeTrading();
        this.angerUntil = this.level().getGameTime() + 600;
        this.lastSawTarget = this.level().getGameTime();
        if (this.getTarget() != attacker) {
            this.actionUntil = 0;
            this.setTarget(attacker);
        }
    }
    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean accepted = super.hurtServer(level, source, amount);
        if (accepted) this.closeTrading();
        if (accepted && this.isAlive() && source.getEntity() instanceof LivingEntity attacker
                && this.canAttack(attacker) && !this.isAlliedTo(attacker)
                && !(attacker instanceof Player player && (player.isCreative() || player.isSpectator()))) {
            var companions = level.getEntitiesOfClass(YoukaiRabbit.class, this.getBoundingBox().inflate(12, 6, 12),
                    other -> other != this && other.isAlive());
            this.defendFrom(attacker);
            if (this.support.claimCall(companions.size(), level.getGameTime())) this.callForHelp(level, attacker);
            companions.forEach(other -> { if (other.getTarget() == null) other.defendFrom(attacker); });
        }
        return accepted;
    }

    private void callForHelp(ServerLevel level, LivingEntity attacker) {
        int summoned = 0;
        for (int attempt = 0; attempt < 72 && summoned < 3; attempt++) {
            double angle = this.random.nextDouble() * Math.PI * 2;
            double radius = 3 + this.random.nextDouble() * 3;
            BlockPos xz = BlockPos.containing(this.getX() + Math.cos(angle) * radius, this.getY(), this.getZ() + Math.sin(angle) * radius);
            for (int dy : new int[]{0, 1, -1, 2, -2, 3, -3}) {
                BlockPos pos = xz.offset(0, dy, 0);
                if (!level.hasChunkAt(pos) || !level.getWorldBorder().isWithinBounds(pos)
                        || !level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)
                        || !level.getFluidState(pos).isEmpty() || !level.getFluidState(pos.above()).isEmpty()) continue;
                YoukaiRabbit ally = RDEntityTypes.YOUKAI_RABBIT.value().create(level, EntitySpawnReason.TRIGGERED);
                if (ally == null) return;
                ally.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
                if (!level.noCollision(ally) || !level.isUnobstructed(ally)) continue;
                ally.support.markReinforcement(level.getGameTime());
                ally.xpReward = 0;
                ally.home = this.home == null ? this.blockPosition() : this.home;
                ally.setHomeTo(ally.home, 16);
                ally.defendFrom(attacker);
                if (level.addFreshEntity(ally)) {
                    summoned++;
                    level.sendParticles(ParticleTypes.POOF, ally.getX(), ally.getY() + .8, ally.getZ(), 6, .3, .4, .3, .02);
                }
                break;
            }
        }
    }
    @Override
    public boolean canPickUpLoot() {
        // Do not let CanPickUpLoot from old saves enable equipment pickup.
        return false;
    }

    @Override protected boolean shouldDropLoot(ServerLevel level) { return !this.support.isReinforcement() && super.shouldDropLoot(level); }

    @Override protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (this.level().isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
        if (!this.canTradeWith(serverPlayer)) { this.message(player, "busy"); return InteractionResult.SUCCESS_SERVER; }
        this.closeTrading();
        this.getLookControl().setLookAt(player, 30, 30);
        this.getNavigation().stop();
        this.beginAction("talk");
        this.tradeGui = new YoukaiRabbitTradeGui(serverPlayer, this);
        if (!this.tradeGui.open()) this.tradeGui = null;
        return InteractionResult.SUCCESS_SERVER;
    }
    private void message(Player player, String key) {
        if (player instanceof ServerPlayer listener) {
            NPCSpeech.sayTo(this, listener, Component.translatable("entity.reverie_dreams.youkai_rabbit." + key));
        }
    }
    @Override protected Component getTypeName() { return Component.translatable("entity.reverie_dreams.youkai_rabbit"); }

    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setFemale(input.getBooleanOr("Female", this.isFemale()));
        this.setVariant(input.getIntOr("Variant", 0));
        this.home = input.read("RabbitHome", BlockPos.CODEC).orElse(null);
        if (this.home != null) this.setHomeTo(this.home, 16);
        this.angerUntil = input.getLongOr("RabbitAngerUntil", 0);
        this.tradeState.restore(input.getLongOr("RabbitTradeDay", Long.MIN_VALUE), input.getIntArray("RabbitTradeStock").orElse(new int[0]));
        this.support.restore(input.getLongOr("RabbitNextCallForHelp", 0), input.getBooleanOr("RabbitReinforcement", false),
                input.getLongOr("RabbitReinforcementUntil", 0));
        if (this.support.isReinforcement()) this.xpReward = 0;
    }
    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Female", this.isFemale());
        output.putInt("Variant", this.getVariant());
        if (this.home != null) output.store("RabbitHome", BlockPos.CODEC, this.home);
        output.putLong("RabbitAngerUntil", this.angerUntil);
        output.putLong("RabbitTradeDay", this.tradeState.day());
        output.putIntArray("RabbitTradeStock", this.tradeState.counts());
        output.putLong("RabbitNextCallForHelp", this.support.nextCall());
        output.putBoolean("RabbitReinforcement", this.support.isReinforcement());
        output.putLong("RabbitReinforcementUntil", this.support.expires());
    }

    public static boolean checkSpawnRules(EntityType<YoukaiRabbit> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return LandEntitySpawnRules.rabbit(type, level, reason, pos, random);
    }
}
