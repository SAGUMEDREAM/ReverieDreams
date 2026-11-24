package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.entity.type.FriendlyFaction;
import cc.thonly.reverie_dreams.api.entity.type.VariantData;
import cc.thonly.reverie_dreams.api.entity.type.YouseiType;
import cc.thonly.reverie_dreams.entity.ai.goal.attack.RabbitUnitCombatGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.attack.RangedAttackUtil;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.KeepInventoryTypes;
import cc.thonly.reverie_dreams.entity.variant.RabbitUnitVariant;
import cc.thonly.reverie_dreams.registry.content.variant.RabbitUnitVariants;
import cc.thonly.reverie_dreams.entity.variant.RabbitUnitAction;
import cc.thonly.reverie_dreams.entity.variant.RabbitUnitEvacuationPhase;
import cc.thonly.reverie_dreams.entity.variant.RabbitUnitLoadout;
import cc.thonly.reverie_dreams.entity.variant.WalkingLegs;
import cc.thonly.reverie_dreams.entity.ai.goal.attack.NPCSpearUseGoal;
import cc.thonly.reverie_dreams.item.weapon.LunarBulletDamageSource;
import cc.thonly.reverie_dreams.item.weapon.WeaponOfTheMoon;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.tag.RDDamageTypeTags;
import cc.thonly.reverie_dreams.util.entity.EntityHelper;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.Identifier;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.List;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.util.Mth;

@Setter
@Getter
public class RabbitUnit extends BaseNPCLikeEntity implements Leashable, FriendlyFaction, VariantData, YouseiType, GeoEntity {
    public static final int EVACUATION_TICKS = RabbitUnitEvacuationPhase.TOTAL_TICKS;
    private static final int MIN_BULLET_DEFENSE = 10;
    private static final int MAX_BULLET_DEFENSE = 30;
    private static final EntityDataAccessor<Integer> COMBAT_ACTION = SynchedEntityData.defineId(RabbitUnit.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ACTION_REVISION = SynchedEntityData.defineId(RabbitUnit.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> ACTION_STARTED_AT = SynchedEntityData.defineId(RabbitUnit.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> ACTION_DURATION = SynchedEntityData.defineId(RabbitUnit.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EVACUATION_PROGRESS = SynchedEntityData.defineId(RabbitUnit.class, EntityDataSerializers.INT);
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private RabbitUnitVariant variant = null;
    private final RabbitUnitCombatGoal combatGoal = new RabbitUnitCombatGoal(this);
    private int actionUntil;
    private int actionStartedTick;
    private double walkingDistance;
    private final WalkingLegs walkingLegs = new WalkingLegs();
    @Setter(lombok.AccessLevel.NONE)
    private float bulletDefense;
    private boolean evacuationStarted;
    private boolean evacuationSettled;

    public RabbitUnit(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType,
                world,
                (
                        RabbitUnitVariants.isEmpty()
                                ? (BuiltInRegistryProviders.RABBIT_UNIT_VARIANT.getAny().isPresent() ? BuiltInRegistryProviders.RABBIT_UNIT_VARIANT.getAny().get().value().getSkinType() : RabbitUnitVariants.RABBIT_UNIT_0.getSkinType())
                                : Objects.requireNonNull(RabbitUnitVariants.random()).getSkinType()
                )
        );
        this.xpReward = 5;
        this.variant = RabbitUnitVariants.getFromSkinType(this.getSkinType());
        // Spawn equipment is authoritative on the server. Initial empty slots have no equipment packet.
        if (!world.isClientSide()) {
            this.bulletDefense = this.getRandom().nextIntBetweenInclusive(MIN_BULLET_DEFENSE, MAX_BULLET_DEFENSE);
            RabbitUnitLoadout.random(this.getRandom()).equip(this);
            this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        }
        var followRange = this.getAttribute(Attributes.FOLLOW_RANGE);
        if (followRange != null) followRange.setBaseValue(32);
        this.updateAttackType();
    }

    /** The spawner loads an NBT template after construction, which clears unsaved equipment slots. */
    public void restoreSpawnerLoadout() {
        if (!this.getMainHandItem().isEmpty()) return;
        RabbitUnitLoadout.random(this.getRandom()).equip(this);
        if (this.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
            this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        }
        this.updateAttackType();
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(COMBAT_ACTION, RabbitUnitAction.IDLE.ordinal());
        builder.define(ACTION_REVISION, 0);
        builder.define(ACTION_STARTED_AT, 0L);
        builder.define(ACTION_DURATION, 1);
        builder.define(EVACUATION_PROGRESS, 0);
    }

    public void setCombatAction(RabbitUnitAction action, int ticks, boolean restart) {
        if (this.level().isClientSide()) return;
        if (this.isEvacuating() && action != RabbitUnitAction.EVACUATE) return;
        if (action == RabbitUnitAction.GUARD && !restart && this.modelAction() == RabbitUnitAction.GUARD_HIT
                && this.combatActionElapsedTicks() < this.combatActionDuration()) return;
        if (restart || this.entityData.get(COMBAT_ACTION) != action.ordinal()) {
            this.entityData.set(COMBAT_ACTION, action.ordinal());
            this.entityData.set(ACTION_REVISION, this.entityData.get(ACTION_REVISION) + 1);
            this.entityData.set(ACTION_STARTED_AT, this.level().getGameTime());
            this.entityData.set(ACTION_DURATION, Math.max(1, ticks));
            this.actionStartedTick = this.tickCount;
        }
        this.actionUntil = this.tickCount + ticks;
    }

    public void clearCombatAction() {
        if (!this.isDeadOrDying()) this.setCombatAction(RabbitUnitAction.IDLE, 0, false);
    }

    public int actionRevision() { return this.entityData.get(ACTION_REVISION); }

    public int combatActionDuration() { return this.entityData.get(ACTION_DURATION); }

    public int combatActionElapsedTicks() {
        long elapsed = this.level().isClientSide() ? this.level().getGameTime() - this.entityData.get(ACTION_STARTED_AT)
                : this.tickCount - this.actionStartedTick;
        return (int) Math.max(0, Math.min(this.combatActionDuration(), elapsed));
    }

    public float reloadProgress() {
        return this.modelAction() == RabbitUnitAction.RELOAD ? (float) this.combatActionElapsedTicks() / this.combatActionDuration() : 0;
    }

    public boolean isEvacuating() {
        return this.evacuationStarted || this.entityData.get(EVACUATION_PROGRESS) > 0 || this.isDeadOrDying();
    }

    public int evacuationTicks() { return this.entityData.get(EVACUATION_PROGRESS); }
    public float evacuationProgress() { return (float) this.evacuationTicks() / EVACUATION_TICKS; }
    public RabbitUnitEvacuationPhase evacuationPhase() { return RabbitUnitEvacuationPhase.at(this.evacuationTicks()); }

    public RabbitUnitAction modelAction() {
        if (this.isEvacuating()) return RabbitUnitAction.EVACUATE;
        int index = this.entityData.get(COMBAT_ACTION);
        if (index > 0 && index < RabbitUnitAction.values().length) return RabbitUnitAction.values()[index];
        if (this.hurtTime > 0) return RabbitUnitAction.HURT;
        boolean gun = this.getMainHandItem().getItem() instanceof WeaponOfTheMoon;
        boolean moving = this.walkingDistance > .005 || this.getDeltaMovement().horizontalDistanceSqr() > .0001;
        return moving ? (gun ? RabbitUnitAction.GUN_WALK : RabbitUnitAction.WALK)
                : gun ? RabbitUnitAction.HOLD : RabbitUnitAction.IDLE;
    }

    public String modelAnimationName() {
        var action = this.modelAction();
        if (this.getMainHandItem().getItem() instanceof WeaponOfTheMoon gun
                && switch (action) { case HOLD, GUN_WALK, AIM, SHOOT, RELOAD, MELEE -> true; default -> false; }) {
            return action.animationName() + "_type_" + gun.getWeaponType().number();
        }
        return action.animationName();
    }

    public double walkAnimationSpeed() {
        double stride = 4 * (12.0 / 16) * Math.sin(Math.toRadians(25)) * this.getScale();
        return Mth.clamp(this.walkingDistance * 20 / Math.max(.1, stride), .5, 2.5);
    }

    public boolean hasWalkingLegAnimation() { return this.walkingLegs.active() && !this.isEvacuating(); }
    public double walkingLegPhase(double partialTick) { return this.walkingLegs.phase(partialTick); }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        int[] revision = {-1};
        int[] previousHurt = {0};
        String[] previousAnimation = {null};
        controllers.add(new AnimationController<RabbitUnit>("rabbit_unit", 2, state -> {
            var action = this.modelAction();
            String animation = this.modelAnimationName();
            if (!animation.equals(previousAnimation[0]) || !action.looping() && this.actionRevision() != revision[0]
                    || action == RabbitUnitAction.HURT && this.hurtTime > previousHurt[0]) state.controller().reset();
            state.controller().setTransitionTicks(action.looping() || action == RabbitUnitAction.HURT ? 2 : 0);
            state.controller().setAnimationSpeed(action == RabbitUnitAction.WALK || action == RabbitUnitAction.GUN_WALK ? this.walkAnimationSpeed() : 1);
            revision[0] = this.actionRevision(); previousHurt[0] = this.hurtTime;
            previousAnimation[0] = animation;
            var raw = RawAnimation.begin();
            return state.setAndContinue(action.looping() ? raw.thenLoop(animation) : raw.thenPlayAndHold(animation));
        }) {
            @Override protected boolean checkControllerState(RabbitUnit actor, GeoRenderState state,
                    AnimatableManager<RabbitUnit> manager, GeoModel<RabbitUnit> model) {
                boolean result = super.checkControllerState(actor, state, manager, model);
                var action = actor.modelAction();
                if (this.timeline == null || action.looping() || action == RabbitUnitAction.HURT) return result;
                float partial = state.getPartialTick();
                double time = action == RabbitUnitAction.EVACUATE ? Math.min(EVACUATION_TICKS, actor.evacuationTicks() + partial) / 20.0
                        : action == RabbitUnitAction.RELOAD ? Math.min(1, actor.reloadProgress() + partial / actor.combatActionDuration())
                        : (actor.combatActionElapsedTicks() + partial) / 20.0;
                // GeckoLib initializes a new timeline after its handler, and skips delta=0 progression.
                // Sample the shared clock after initialization, including a newly tracked frozen entity.
                this.timelineTime = Math.max(0, Math.min(time, this.timeline.lastAnimationEndTime()));
                this.transitionFromPoint = null;
                this.animationPoint = this.timeline.createAnimationPoint(this.timelineTime, null, this.easingOverride);
                return this.isAnimatingBones();
            }
        });
        controllers.add(WalkingLegs.<RabbitUnit>controller(this::hasWalkingLegAnimation, this::walkingLegPhase));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.geoCache; }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        for(var goal:this.goalSelector.getAvailableGoals().stream().map(w->w.getGoal()).filter(g->g instanceof NPCSpearUseGoal<?>).toList())
            this.goalSelector.removeGoal(goal);

        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Mob.class, 6.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Zombie.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Skeleton.class, true));
        EntityHelper.registerHostilityAllYousei(this, this.targetSelector);

    }

    @Override
    public void tick() {
        if (this.isEvacuating()) {
            if (this.getHealth() > 0) this.setHealth(0);
            this.getNavigation().stop();
            this.stopUsingItem();
            this.setAggressive(false);
            if (this.getTarget() != null) super.setTarget(null);
        }
        if (this.getTarget() != null && !this.canAttack(this.getTarget())) {
            this.setTarget(null);
        }
        double x = this.getX(), z = this.getZ();
        super.tick();
        this.walkingDistance = Math.hypot(this.getX() - x, this.getZ() - z);
        if (!this.level().isClientSide() && this.isAlive() && this.tickCount >= this.actionUntil) this.clearCombatAction();
        var pose = this.modelAction();
        this.walkingLegs.update(this.walkingDistance, this.getScale(), this.onGround() && !this.isInWater()
                        && !this.isInLava() && !this.isPassenger(),
                !this.isEvacuating() && (pose == RabbitUnitAction.WALK || pose == RabbitUnitAction.GUN_WALK || pose == RabbitUnitAction.HURT));
    }

    @Override public boolean canAttack(LivingEntity target) {
        if (this.isEvacuating() || target == null || target instanceof RabbitUnit || target instanceof YoukaiRabbit || target == this.getOwner()
                || FriendlyFaction.isFriendLy(this, target)) return false;
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) return false;
        if (target instanceof BaseNPCLikeEntity npc && this.isAlliedTo(npc)) return false;
        return super.canAttack(target);
    }

    @Override public boolean isWithinMeleeAttackRange(LivingEntity target) {
        // The generic NPC's 3.5-block expansion is too long for a soldier's sword or rifle butt.
        return target != null && this.getAttackBoundingBox(this.getScale()).intersects(target.getBoundingBox());
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (this.isEvacuating() && target != null) return;
        if (target != null && !this.canAttack(target)) {
            return;
        }
        super.setTarget(target);
    }

    @SuppressWarnings("ConstantValue")
    @Override
    protected void updateAttackType() {
        if (this.level() == null || this.level().isClientSide()) {
            return;
        }
        // BaseNPCLikeEntity calls this during super construction, before our goal exists.
        if (this.combatGoal == null) return;
        Goal selected;
        if (RangedAttackUtil.getArrowStack(this) != null && (this.inventory.findHand((stack -> stack.is(Items.BOW) || stack.getItem() instanceof BowItem)) != null)) {
            int i = this.getRegularAttackInterval();
            this.bowAttackGoal.setAttackInterval(i);
            selected=this.bowAttackGoal;
        } else if (RangedAttackUtil.getCrossBowAmmoStack(this) != null && (this.inventory.findHand((stack -> stack.is(Items.CROSSBOW) || stack.getItem() instanceof CrossbowItem)) != null)) {
            selected=this.crossBowAttackGoal;
        } else if (RangedAttackUtil.isDanmakuInHand(this)) {
            selected=this.danmakuItemGoal;
        } else {
            selected=this.combatGoal;
        }
        for(Goal goal:List.of(this.meleeAttackGoal,this.combatGoal,this.bowAttackGoal,this.crossBowAttackGoal,this.danmakuItemGoal))
            if(goal!=selected)this.goalSelector.removeGoal(goal);
        // Equipment polling must not cancel reloads longer than its 42-tick interval.
        if(this.goalSelector.getAvailableGoals().stream().noneMatch(w->w.getGoal()==selected))this.goalSelector.addGoal(2,selected);
    }

    @Override
    public void aiStep() {
        super.aiStep();
    }

    @Override
    public void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        float savedDefense = view.getFloatOr("BulletDefense", this.bulletDefense);
        if (Float.isFinite(savedDefense)) this.bulletDefense = Mth.clamp(savedDefense, 0, MAX_BULLET_DEFENSE);
        ItemStack oldWeapon=this.getMainHandItem();
        if(oldWeapon.is(RDItems.SILVER_SPEAR.asItem())){
            ItemStack sword=oldWeapon.transmuteCopy(RDItems.SILVER_SWORD.asItem());
            if(sword.isDamageableItem())sword.setDamageValue(Math.min(sword.getDamageValue(),Math.max(0,sword.getMaxDamage()-1)));
            this.setItemSlot(EquipmentSlot.MAINHAND,sword);
            if(this.getOffhandItem().isEmpty())this.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SHIELD));
        }
        String youseiVariantId = view.getStringOr("UnitVariant", RabbitUnitVariants.DEFAULT_ID.toString());
        Identifier variantId = Identifier.tryParse(youseiVariantId);
        this.variant = variantId == null ? null : BuiltInRegistryProviders.RABBIT_UNIT_VARIANT.getValue(variantId);
        if (this.variant == null) this.variant = RabbitUnitVariants.RABBIT_UNIT_0;
        this.setSkinType(this.variant.getSkinType());
        this.evacuationStarted = view.getBooleanOr("UnitEvacuating", false) || this.isDeadOrDying() || this.deathTime > 0;
        if (this.evacuationStarted) {
            int elapsed = Mth.clamp(view.getIntOr("UnitEvacTicks", this.deathTime), 0, EVACUATION_TICKS);
            this.entityData.set(EVACUATION_PROGRESS, elapsed);
            // A loaded death is resumed, never replayed: die/loot settled before the world could save it.
            this.evacuationSettled = true;
            this.setHealth(0);
            this.stopUsingItem();
            this.getNavigation().stop();
            super.setTarget(null);
            this.setCombatAction(RabbitUnitAction.EVACUATE, EVACUATION_TICKS, true);
        }
        this.updateAttackType();
    }

    @Override
    public void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        view.putFloat("BulletDefense", this.bulletDefense);
        view.putString("UnitVariant", this.getVariantData().toString());
        view.putBoolean("UnitEvacuating", this.isEvacuating());
        if (this.isEvacuating()) {
            view.putInt("UnitEvacTicks", this.evacuationTicks());
            view.putBoolean("UnitEvacSettled", this.evacuationSettled);
        }
    }

    @Override
    public Boolean canPickItem() {
        return false;
    }

    @Override
    public String getFactionId() {
        return ReverieDreams.MOD_ID + ":lunar_army";
    }

    @Override
    public void setVariantData(Identifier id) {
        this.variant = BuiltInRegistryProviders.RABBIT_UNIT_VARIANT.getValue(id);
        if (this.variant == null) this.variant = RabbitUnitVariants.RABBIT_UNIT_0;
        this.setSkinType(this.variant.getSkinType());
    }

    @Override
    public KeepInventoryTypes getKeepInventoryType() {
        return KeepInventoryTypes.NOT_DROP_ANY;
    }

    @Override
    public Identifier getVariantData() {
        return this.variant != null ? this.variant.getId() : RabbitUnitVariants.DEFAULT_ID;
    }

    @Override protected SoundEvent getDeathSound() { return SoundEvents.ENDERMAN_TELEPORT; }

    @Override public void die(DamageSource source) {
        if (this.evacuationStarted) return;
        this.evacuationStarted = true;
        this.combatGoal.stop();
        this.getNavigation().stop();
        this.stopUsingItem();
        this.setAggressive(false);
        super.setTarget(null);
        this.setCombatAction(RabbitUnitAction.EVACUATE, EVACUATION_TICKS, true);
        super.die(source);
        this.evacuationSettled = true;
    }

    @Override public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        return !this.isEvacuating() && super.hurtServer(world, source, amount);
    }

    @Override protected void actuallyHurt(ServerLevel world, DamageSource source, float amount) {
        // This is the accepted portion of the hit, after shield blocking and the hurt cooldown.
        if (amount > 0 && this.bulletDefense > 0 && !this.isInvulnerableTo(world, source)) {
            boolean bullet = source instanceof LunarBulletDamageSource;
            if (!bullet && source.is(RDDamageTypeTags.DANMAKU_HIT)) {
                this.bulletDefense = Math.max(0, this.bulletDefense - amount);
                amount = Math.min(amount * 1.25F, Float.MAX_VALUE);
            } else {
                if (bullet) amount *= .75F;
                float absorbed = Math.min(this.bulletDefense, bullet ? amount : amount * .5F);
                this.bulletDefense -= absorbed;
                amount -= absorbed;
            }
        }
        super.actuallyHurt(world, source, amount);
    }

    @Override public float applyItemBlocking(ServerLevel world, DamageSource source, float amount) {
        float blocked = super.applyItemBlocking(world, source, amount);
        if (blocked > 0 && !this.isEvacuating()) this.setCombatAction(RabbitUnitAction.GUARD_HIT, 6, true);
        return blocked;
    }

    @Override protected void tickDeath() {
        this.setDeltaMovement(0, 0, 0);
        this.setNoGravity(true);
        if (!this.evacuationStarted) { this.evacuationStarted = true; this.evacuationSettled = true; }
        this.deathTime++;
        if (this.level() instanceof ServerLevel world) {
            int elapsed = Math.min(EVACUATION_TICKS, this.evacuationTicks() + 1);
            this.entityData.set(EVACUATION_PROGRESS, elapsed);
            if (elapsed % 3 == 0) world.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    this.getX(), this.getY() + .8 + Math.max(0, elapsed - 16) * .04, this.getZ(), 5, .3, .4, .3, .025);
            if (elapsed == 16) world.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + .2, this.getZ(), 10, .3, .1, .3, .08);
            if (elapsed >= EVACUATION_TICKS) {
                this.deathTime = 19;
                super.tickDeath();
            }
        }
    }

    @Override
    public boolean requiresCustomPersistence() {
        return this.isEvacuating() || super.requiresCustomPersistence();
    }
}
