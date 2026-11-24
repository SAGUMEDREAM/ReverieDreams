package cc.thonly.reverie_dreams.entity.ai.goal.attack;

import cc.thonly.reverie_dreams.entity.RabbitUnit;
import cc.thonly.reverie_dreams.entity.variant.RabbitUnitAction;
import cc.thonly.reverie_dreams.entity.variant.RabbitUnitLoadout;
import cc.thonly.reverie_dreams.item.weapon.LunarWeaponType;
import cc.thonly.reverie_dreams.item.weapon.WeaponOfTheMoon;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * One exclusive activity owns the hands; navigation and visuals follow that activity.
 */
public final class RabbitUnitCombatGoal extends Goal {
    private enum Activity {READY, AIM, SHOOT_RECOVERY, RELOAD, GUARD, MELEE_WINDUP, MELEE_RECOVERY}

    private static final int SEARCH_TICKS = 60;
    private final RabbitUnit actor;
    private final RabbitUnitTactics tactics;
    private Activity activity = Activity.READY;
    private int activityTicks;
    private ItemStack heldStack = ItemStack.EMPTY, shieldStack = ItemStack.EMPTY;
    private WeaponOfTheMoon weapon;
    private LivingEntity trackedTarget, meleeTarget;
    private Vec3 lastSeen, lastThreatEye;
    private int unseenTicks, pathCooldown, shotCooldown, burstRemaining;
    private int meleeCooldown, guardCooldown, targetSwitchCooldown, retreatTicks;
    private boolean aimed;
    private int strafeDirection;

    public RabbitUnitCombatGoal(RabbitUnit actor) {
        this.actor = actor;
        this.tactics = new RabbitUnitTactics(actor);
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.actor.getTarget();
        return !this.actor.isEvacuating() && !this.actor.isDeadOrDying() && !this.actor.isOrderedToSit()
                && target != null && target.isAlive() && this.actor.canAttack(target);
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.reset();
        this.strafeDirection = (this.actor.getUUID().hashCode() & 1) == 0 ? 1 : -1;
        this.actor.setAggressive(true);
    }

    @Override
    public void stop() {
        this.reset();
        this.actor.setAggressive(false);
    }

    private void reset() {
        this.cancelActivity();
        this.weapon = null;
        this.heldStack = this.shieldStack = ItemStack.EMPTY;
        this.trackedTarget = null;
        this.lastSeen = this.lastThreatEye = null;
        this.unseenTicks = this.pathCooldown = this.shotCooldown = 0;
        this.meleeCooldown = this.guardCooldown = this.targetSwitchCooldown = 0;
        this.retreatTicks = 0;
        this.tactics.reset();
    }

    private void cancelActivity() {
        this.activity = Activity.READY;
        this.activityTicks = this.burstRemaining = 0;
        this.aimed = false;
        this.meleeTarget = null;
        this.actor.stopUsingItem();
        this.stopMovement();
        this.actor.clearCombatAction();
    }

    private void stopMovement() {
        this.actor.getNavigation().stop();
        this.actor.getMoveControl().strafe(0, 0);
    }

    @Override
    public void tick() {
        // Also guard direct calls and the tick on which an existing goal becomes invalid.
        if (!this.canUse() || !(this.actor.level() instanceof ServerLevel world)) {
            this.stop();
            return;
        }
        this.pathCooldown = Math.max(0, this.pathCooldown - 1);
        this.shotCooldown = Math.max(0, this.shotCooldown - 1);
        this.meleeCooldown = Math.max(0, this.meleeCooldown - 1);
        this.guardCooldown = Math.max(0, this.guardCooldown - 1);
        this.targetSwitchCooldown = Math.max(0, this.targetSwitchCooldown - 1);

        ItemStack held = this.actor.getMainHandItem(), offhand = this.actor.getOffhandItem();
        // Identity matters: a different stack of the same weapon must not inherit a reload.
        if (this.heldStack != held || this.shieldStack != offhand) {
            this.cancelActivity();
            this.tactics.reset();
            this.heldStack = held;
            this.shieldStack = offhand;
            this.weapon = held.getItem() instanceof WeaponOfTheMoon gun ? gun : null;
            this.shotCooldown = this.meleeCooldown = this.guardCooldown = 0;
            this.retreatTicks = 0;
        }
        LivingEntity target = this.actor.getTarget();
        RabbitUnitTactics.Threat threat = this.tactics.assessThreat(target);
        if ((this.activity == Activity.READY || this.activity == Activity.SHOOT_RECOVERY && this.activityTicks <= 1)
                && this.targetSwitchCooldown == 0
                && threat.priority() != null && threat.priority() != target && threat.priority().isAlive()
                && this.actor.canAttack(threat.priority()) && this.actor.getSensing().hasLineOfSight(threat.priority())) {
            this.actor.setTarget(threat.priority());
            target = this.actor.getTarget();
            this.targetSwitchCooldown = 40;
        }
        if (target != this.trackedTarget) {
            this.cancelActivity();
            this.tactics.clearDestinations();
            this.trackedTarget = target;
            this.lastSeen = this.lastThreatEye = null;
            this.unseenTicks = 0;
            this.retreatTicks = 0;
        }

        boolean visible = this.actor.getSensing().hasLineOfSight(target);
        if (visible) {
            this.lastSeen = target.position();
            this.lastThreatEye = target.getEyePosition();
            this.unseenTicks = 0;
            this.actor.getLookControl().setLookAt(target, 30, 30);
        } else {
            // Never seed memory from an unseen target, including a newly assigned target.
            if (this.lastSeen == null || ++this.unseenTicks > SEARCH_TICKS) {
                this.actor.setTarget(null);
                this.stop();
                return;
            }
            this.actor.getLookControl().setLookAt(this.lastThreatEye.x, this.lastThreatEye.y, this.lastThreatEye.z);
        }
        double distance = visible ? this.actor.distanceToSqr(target) : this.actor.position().distanceToSqr(this.lastSeen);
        if (this.tickActivity(world, target, visible, distance))
            return;

        if (visible && this.actor.isWithinMeleeAttackRange(target) && this.meleeCooldown == 0) {
            this.beginActivity(Activity.MELEE_WINDUP, RabbitUnitAction.MELEE, 14);
            this.activityTicks = 6;
            this.meleeTarget = target;
            this.actor.swing(InteractionHand.MAIN_HAND);
            return;
        }
        if (this.weapon != null && this.weapon.getRounds(held) == 0) {
            this.beginActivity(Activity.RELOAD, RabbitUnitAction.RELOAD, this.weapon.getWeaponType().reloadTicks());
            this.tactics.takeCover(this.lastThreatEye);
            world.playSound(null, this.actor.blockPosition(), SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.HOSTILE, .7f, 1.2f);
            return;
        }
        if (offhand.is(Items.SHIELD) && this.guardCooldown == 0 && visible
                && (threat.imminent() || threat.ranged() && distance < 144 || this.weapon == null && distance < 9 && this.meleeCooldown > 0)) {
            this.beginActivity(Activity.GUARD, RabbitUnitAction.GUARD, 12);
            this.actor.startUsingItem(InteractionHand.OFF_HAND);
            return;
        }
        if (!visible) {
            this.aimed = false;
            this.burstRemaining = 0;
            this.searchLastSeen();
            return;
        }
        if (this.weapon == null) {
            if (this.actor.isWithinMeleeAttackRange(target)) {
                this.stopMovement();
                return;
            }
            if (distance > 9 && this.tactics.maintainSpacing())
                return;
            this.pursue(this.lastSeen, 1.15);
            return;
        }

        double range = effectiveRange(this.weapon.getWeaponType());
        if ((threat.imminent() || threat.ranged() && this.actor.getHealth() < this.actor.getMaxHealth() * .45f)
                && this.tactics.takeCover(this.lastThreatEye)) {
            this.aimed = false;
            this.burstRemaining = 0;
            return;
        }
        if (distance > range * range) {
            this.aimed = false;
            this.pursue(this.lastSeen, 1.05);
            return;
        }
        if (!this.aimed && this.retreatTicks < 20 && this.tactics.gainDistance(this.lastSeen, RabbitUnitLoadout.forWeapon(this.weapon.getWeaponType()).minimumRange())) {
            this.retreatTicks++;
            this.aimed = false;
            this.burstRemaining = 0;
            return;
        }
        this.keepDistance(distance);
        if (this.shotCooldown > 0)
            return;
        if (!this.hasClearShot(target)) {
            this.aimed = false;
            this.burstRemaining = 0;
            this.tactics.clearFiringLane(this.lastThreatEye);
            return;
        }
        if (!this.aimed) {
            this.beginActivity(Activity.AIM, RabbitUnitAction.AIM, RabbitUnitLoadout.forWeapon(this.weapon.getWeaponType()).aimTicks());
            return;
        }
        this.fire(world, target, held);
    }

    /**
     * True means this tick belongs entirely to the in-progress activity.
     */
    private boolean tickActivity(ServerLevel world, LivingEntity target, boolean visible, double distance) {
        switch (this.activity) {
            case READY -> {
                return false;
            }
            case SHOOT_RECOVERY -> {
                // Keep pump/bolt operation and the last shot ahead of a magazine change or melee.
                if (--this.activityTicks > 0) {
                    this.stopMovement();
                    return true;
                }
                this.activity = Activity.READY;
                return false;
            }
            case RELOAD -> {
                if (--this.activityTicks <= 0) {
                    if (this.weapon != null && this.actor.getMainHandItem() == this.heldStack)
                        this.weapon.setRounds(this.heldStack, this.weapon.capacity());
                    this.finishActivity();
                    this.tactics.releaseCover();
                } else if (!this.tactics.takeCover(this.lastThreatEye))
                    this.keepDistance(distance);
                return true;
            }
            case GUARD -> {
                this.stopMovement();
                boolean reacting = this.actor.modelAction() == RabbitUnitAction.GUARD_HIT
                        && this.actor.combatActionElapsedTicks() < this.actor.combatActionDuration();
                if (--this.activityTicks <= 0 && !reacting) {
                    this.actor.stopUsingItem();
                    this.guardCooldown = 28;
                    this.finishActivity();
                } else if (!reacting && this.actor.modelAction() != RabbitUnitAction.GUARD)
                    this.actor.setCombatAction(RabbitUnitAction.GUARD, Math.max(1, this.activityTicks), false);
                return true;
            }
            case MELEE_WINDUP -> {
                this.stopMovement();
                if (--this.activityTicks <= 0) {
                    if (this.meleeTarget == target && visible && this.actor.isWithinMeleeAttackRange(target))
                        this.actor.doHurtTarget(world, target);
                    this.activity = Activity.MELEE_RECOVERY;
                    this.activityTicks = 8;
                    this.meleeCooldown = 24;
                }
                return true;
            }
            case MELEE_RECOVERY -> {
                this.stopMovement();
                if (--this.activityTicks <= 0)
                    this.finishActivity();
                return true;
            }
            case AIM -> {
                this.stopMovement();
                if (!visible || this.weapon == null || this.weapon.getRounds(this.heldStack) == 0
                        || distance > Math.pow(effectiveRange(this.weapon.getWeaponType()), 2) || !this.hasClearShot(target)) {
                    this.finishActivity();
                    return true;
                }
                if (--this.activityTicks > 0)
                    return true;
                this.activity = Activity.READY;
                this.aimed = true;
                return false;
            }
        }
        return true;
    }

    private void beginActivity(Activity activity, RabbitUnitAction action, int ticks) {
        this.cancelActivity();
        this.activity = activity;
        this.activityTicks = ticks;
        this.actor.setCombatAction(action, ticks, true);
    }

    private void finishActivity() {
        this.activity = Activity.READY;
        this.activityTicks = 0;
        this.meleeTarget = null;
        this.aimed = false;
        this.actor.clearCombatAction();
    }

    private void fire(ServerLevel world, LivingEntity target, ItemStack held) {
        Vec3 aim = target.getBoundingBox().getCenter().subtract(this.actor.getEyePosition());
        this.actor.setXRot((float) -Math.toDegrees(Math.atan2(aim.y, aim.horizontalDistance())));
        this.actor.setYRot((float) Math.toDegrees(Math.atan2(aim.z, aim.x)) - 90);
        this.actor.yBodyRot = this.actor.getYRot();
        this.weapon.setRounds(held, this.weapon.getRounds(held) - 1);
        this.weapon.tryShoot(this.actor, world, InteractionHand.MAIN_HAND);
        this.retreatTicks = 0;
        LunarWeaponType type = this.weapon.getWeaponType();
        var profile = RabbitUnitLoadout.forWeapon(type);
        int actionTicks = type == LunarWeaponType.SHOTGUN || type == LunarWeaponType.SNIPER ? 16 : 5;
        this.actor.setCombatAction(RabbitUnitAction.SHOOT, actionTicks, true);
        this.activity = Activity.SHOOT_RECOVERY;
        this.activityTicks = Math.min(actionTicks, type.shotInterval() * 2);
        if (this.burstRemaining <= 0)
            this.burstRemaining = profile.burstSize();
        this.burstRemaining--;
        if (this.burstRemaining == 0)
            this.aimed = false;
        this.shotCooldown = this.burstRemaining > 0 ? type.shotInterval() * 2 : Math.max(profile.burstPause(), type.shotInterval() * 2);
    }

    public static double effectiveRange(LunarWeaponType type) {
        return RabbitUnitLoadout.forWeapon(type).maximumRange();
    }

    public boolean hasClearShot(LivingEntity target) {
        WeaponOfTheMoon gun = this.actor.getMainHandItem().getItem() instanceof WeaponOfTheMoon held ? held : null;
        return RabbitUnitTactics.hasClearShot(this.actor, target, gun == null ? LunarWeaponType.RIFLE : gun.getWeaponType());
    }

    private void pursue(Vec3 destination, double speed) {
        this.actor.getMoveControl().strafe(0, 0);
        if (this.pathCooldown > 0 || destination == null)
            return;
        this.tactics.moveToKnownPosition(destination, speed);
        this.pathCooldown = 12;
    }

    private void searchLastSeen() {
        // The target's live position is deliberately not consulted here.
        Vec3 destination = this.lastSeen;
        if (this.actor.position().distanceToSqr(this.lastSeen) < 4 || this.unseenTicks > 24) {
            int phase = this.unseenTicks / 12 + (this.strafeDirection > 0 ? 0 : 2);
            destination = this.lastSeen.add(Math.cos(phase * Math.PI / 2) * 2, 0, Math.sin(phase * Math.PI / 2) * 2);
        }
        this.pursue(destination, .9);
    }

    private void keepDistance(double distanceSq) {
        if (this.tactics.maintainSpacing())
            return;
        this.actor.getNavigation().stop();
        if (this.actor.tickCount % 40 == 0)
            this.strafeDirection = -this.strafeDirection;
        var profile = RabbitUnitLoadout.forWeapon(this.weapon.getWeaponType());
        float backwards = distanceSq < profile.minimumRange() * profile.minimumRange() ? -.45f : 0;
        float sideways = profile == RabbitUnitLoadout.SNIPER ? 0 : this.strafeDirection * .18f;
        this.actor.getMoveControl().strafe(backwards, sideways);
    }
}
