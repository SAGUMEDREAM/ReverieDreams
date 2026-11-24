package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.api.entity.type.FriendlyFaction;
import cc.thonly.reverie_dreams.entity.ai.goal.UniversalLivingAngerGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.attack.RangedAttackUtil;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.KeepInventoryTypes;
import cc.thonly.reverie_dreams.entity.variant.OniVariant;
import cc.thonly.reverie_dreams.entity.variant.OniEquipment;
import cc.thonly.reverie_dreams.registry.content.variant.OniVariants;
import cc.thonly.reverie_dreams.inventory.NPCInventoryImpl;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Objects;
import cc.thonly.reverie_dreams.api.entity.type.VariantData;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;

@SuppressWarnings({"RedundantMethodOverride", "DuplicatedCode", "ConstantValue", "resource"})
public class Oni extends BaseNPCLikeEntity implements FriendlyFaction, VariantData, GeoEntity {
    public static final double MOVEMENT_SPEED = 0.18;
    public static final int ATTACK_WINDUP_TICKS = 12;
    public static final int ATTACK_INTERVAL_TICKS = 40;
    public static final int ATTACK_ANIMATION_TICKS = 24;
    public static final int PLANTED_ATTACK_TICKS = 16;
    public static final int WALK_CYCLE_TICKS = 20;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int attackAnimationUntil;
    private int attackRevision;
    private double walkingDistanceThisTick;
    public static final byte ATTACK_WINDUP_EVENT = 111;
    private OniVariant variant;

    public void beginAttackAnimation() {
        if (this.tickCount < this.attackAnimationUntil) return;
        this.attackAnimationUntil = this.tickCount + ATTACK_ANIMATION_TICKS;
        this.attackRevision++;
        if (!this.level().isClientSide()) this.level().broadcastEntityEvent(this, ATTACK_WINDUP_EVENT);
    }

    public int attackAnimationRevision() { return this.attackRevision; }

    public boolean isAttackAnimationRunning() { return this.tickCount < this.attackAnimationUntil; }

    public boolean isPlantingFeetForAttack() {
        return this.isAttackAnimationRunning()
                && this.attackAnimationUntil - this.tickCount > ATTACK_ANIMATION_TICKS - PLANTED_ATTACK_TICKS;
    }

    public double walkAnimationSpeed() {
        double stride = 4 * (12.0 / 16) * Math.sin(Math.toRadians(28)) * this.getScale();
        return Mth.clamp(this.walkingDistanceThisTick * 20 / Math.max(.1, stride), .5, 2.5);
    }

    public String modelAnimation() {
        if (this.isDeadOrDying()) return "death";
        if (this.hurtTime > 0) return "hurt";
        if (this.tickCount < this.attackAnimationUntil) return "melee";
        if (this.getDeltaMovement().horizontalDistanceSqr() > 0.0001) return "walk";
        return OniEquipment.isClub(this.getMainHandItem()) || OniEquipment.isClub(this.getOffhandItem()) ? "heavy_ready" : "idle";
    }

    @Override public void handleEntityEvent(byte event) {
        if (event == ATTACK_WINDUP_EVENT) { this.beginAttackAnimation(); return; }
        super.handleEntityEvent(event);
    }

    @Override public void swing(InteractionHand hand, boolean updateSelf) {
        super.swing(hand, updateSelf);
        this.beginAttackAnimation();
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        String[] previous = {null};
        int[] revision = {-1};
        int[] hurt = {0};
        controllers.add(new AnimationController<Oni>("oni", 2, state -> {
            String next = this.modelAnimation();
            if (!next.equals(previous[0]) || (next.equals("melee") && revision[0] != this.attackRevision)
                    || (next.equals("hurt") && this.hurtTime > hurt[0])) state.controller().reset();
            state.controller().setTransitionTicks(next.equals("death") ? 0 : 2);
            state.controller().setAnimationSpeed(next.equals("walk") ? this.walkAnimationSpeed() : 1);
            previous[0] = next; revision[0] = this.attackRevision; hurt[0] = this.hurtTime;
            var raw = RawAnimation.begin();
            return state.setAndContinue(next.equals("idle") || next.equals("walk") || next.equals("heavy_ready") ? raw.thenLoop("animation." + next)
                    : raw.thenPlayAndHold("animation." + next));
        }));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.geoCache; }

    @Override public void setVariantData(Identifier id) {
        var selected = BuiltInRegistryProviders.ONI_VARIANT.getValue(id);
        this.variant = selected != null ? selected : OniVariants.GREEN;
        this.setSkinType(this.variant.getSkinType());
    }

    @Override public Identifier getVariantData() {
        return this.variant != null ? this.variant.getId() : OniVariants.GREEN.getId();
    }

    public Oni(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world, (
                OniVariants.isEmpty()
                        ? (BuiltInRegistryProviders.ONI_VARIANT.getAny().isPresent() ? BuiltInRegistryProviders.ONI_VARIANT.getAny().get().value().getSkinType() : OniVariants.GREEN.getSkinType())
                        : Objects.requireNonNull(OniVariants.random()).getSkinType()
                )
        );
        this.xpReward = 5;
        this.variant = OniVariants.getFromSkinType(this.getSkinType());
        NPCInventoryImpl inventory = this.getInventory();
        inventory.setMainHand(RDItems.IRON_BAR.createStack());
        this.meleeAttackGoal.setSpeedModifier(1.0);
        this.meleeAttackGoal.setAttackInterval(ATTACK_INTERVAL_TICKS);
        this.meleeAttackGoal.setAttackWindup(ATTACK_WINDUP_TICKS);
    }

    @Override
    public boolean isWithinMeleeAttackRange(LivingEntity target) {
        if (target == null) {
            return false;
        }
        double interactionRange = this.getAttributes().hasAttribute(Attributes.ENTITY_INTERACTION_RANGE)
                ? this.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)
                : 2.2;
        double reach = Math.max(0.5, interactionRange - (this.getBbWidth() * 0.5));
        return this.getAttackBoundingBox(reach).intersects(target.getBoundingBox());
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Mob.class, 6.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(3, new UniversalLivingAngerGoal<>(this, false));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, Oni::canTargetWillingly));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Villager.class, true));
    }

    /**
     * 佩戴鬼面的玩家会被鬼族视为同类，从而不会被主动敌视（被动反击仍然保留）。
     */
    private static boolean canTargetWillingly(LivingEntity target, ServerLevel level) {
        return !(target instanceof Player player)
                || !player.getItemBySlot(EquipmentSlot.HEAD).is(RDItems.ONI_MASK.asItem());
    }

    @Override
    protected void updateAttackType() {
        if (this.level() == null || this.level().isClientSide()) {
            return;
        }
        this.goalSelector.removeGoal(this.meleeAttackGoal);
        this.goalSelector.removeGoal(this.bowAttackGoal);
        this.goalSelector.removeGoal(this.crossBowAttackGoal);
        this.goalSelector.removeGoal(this.danmakuItemGoal);

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
    public void tick() {
        double previousX = this.getX();
        double previousZ = this.getZ();
        if (!this.level().isClientSide()) {
            this.setSkinType(this.variant != null ? this.variant.getSkinType() : OniVariants.GREEN.getSkinType());
        }
        super.tick();
        this.walkingDistanceThisTick = Math.hypot(this.getX() - previousX, this.getZ() - previousZ);
    }

    @Override
    public void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        String variantId = view.getStringOr("Variant", OniVariants.GREEN.getId().toString());
        Identifier variantIdentifier = Identifier.tryParse(variantId);
        this.setVariantData(variantIdentifier != null ? variantIdentifier : OniVariants.GREEN.getId());
        var speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && Math.abs(speed.getBaseValue() - 0.22) < 0.000001) {
            speed.setBaseValue(MOVEMENT_SPEED);
        }
    }

    @Override
    public void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        view.putString("Variant", this.getVariantData().toString());
    }

    @Override
    public KeepInventoryTypes getKeepInventoryType() {
        return KeepInventoryTypes.NOT_DROP_ANY;
    }

    @Override
    public String getFactionId() {
        return "oni";
    }
}
