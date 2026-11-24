package cc.thonly.reverie_dreams.entity.ai.goal.attack;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

import cc.thonly.reverie_dreams.entity.Oni;

@SuppressWarnings("SimplifiableConditionalExpression")
public class NPCMeleeAttackGoal extends Goal {
    protected final PathfinderMob mob;
    private double speedModifier;
    private final boolean followingTargetEvenIfNotSeen;
    private Path path;
    private double pathedTargetX;
    private double pathedTargetY;
    private double pathedTargetZ;
    private int ticksUntilNextPathRecalculation;
    private int ticksUntilNextAttack;
    protected int attackInterval = 20;
    protected int attackWindup = 0;
    private long lastCanUseCheck;
    private static final long COOLDOWN_BETWEEN_CAN_USE_CHECKS = 20L;

    public NPCMeleeAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.followingTargetEvenIfNotSeen = followingTargetEvenIfNotSeen;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        long time = this.mob.level().getGameTime();
        if (time - this.lastCanUseCheck < COOLDOWN_BETWEEN_CAN_USE_CHECKS) {
            return false;
        } else {
            this.lastCanUseCheck = time;
            LivingEntity target = this.mob.getTarget();
            if (target == null) {
                return false;
            } else if (!target.isAlive()) {
                return false;
            } else {
                this.path = this.mob.getNavigation().createPath(target, 0);
                return this.path != null ? true : this.mob.isWithinMeleeAttackRange(target);
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            return false;
        } else if (!target.isAlive()) {
            return false;
        } else if (!this.followingTargetEvenIfNotSeen) {
            return !this.mob.getNavigation().isDone();
        } else {
            return !this.mob.isWithinHome(target.blockPosition()) ? false : !(target instanceof Player player && (player.isSpectator() || player.isCreative()));
        }
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.path, this.speedModifier);
        this.mob.setAggressive(true);
        this.ticksUntilNextPathRecalculation = 0;
        this.ticksUntilNextAttack = this.getAttackWindup();
    }

    @Override
    public void stop() {
        LivingEntity target = this.mob.getTarget();
        if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target)) {
            this.mob.setTarget(null);
        }

        this.mob.setAggressive(false);
        this.mob.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target != null) {
            boolean planted = this.mob instanceof Oni oni && oni.isPlantingFeetForAttack();
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
            if (!planted && (this.followingTargetEvenIfNotSeen || this.mob.getSensing().hasLineOfSight(target))
                    && this.ticksUntilNextPathRecalculation <= 0
                    && (
                    this.pathedTargetX == 0.0 && this.pathedTargetY == 0.0 && this.pathedTargetZ == 0.0
                            || target.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 1.0
                            || this.mob.getRandom().nextFloat() < 0.05F
            )) {
                this.pathedTargetX = target.getX();
                this.pathedTargetY = target.getY();
                this.pathedTargetZ = target.getZ();
                this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
                double targetDistanceSqr = this.mob.distanceToSqr(target);
                if (targetDistanceSqr > 1024.0) {
                    this.ticksUntilNextPathRecalculation += 10;
                } else if (targetDistanceSqr > 256.0) {
                    this.ticksUntilNextPathRecalculation += 5;
                }

                if (!this.mob.getNavigation().moveTo(target, this.speedModifier)) {
                    this.ticksUntilNextPathRecalculation += 15;
                }

                this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);
            }

            if (this.attackWindup > 0 && !this.mob.isWithinMeleeAttackRange(target)
                    && !(this.mob instanceof Oni oni && oni.isAttackAnimationRunning())) {
                this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack, this.getAttackWindup());
            }
            if (this.mob instanceof Oni oni
                    && this.ticksUntilNextAttack == this.getAttackWindup()
                    && this.mob.isWithinMeleeAttackRange(target) && this.hasLineOfSight(target)) {
                oni.beginAttackAnimation();
            }
            if (this.mob instanceof Oni oni
                    && (oni.isPlantingFeetForAttack() || this.mob.isWithinMeleeAttackRange(target))) {
                this.mob.getNavigation().stop();
                this.mob.getMoveControl().setWantedPosition(this.mob.getX(), this.mob.getY(), this.mob.getZ(), 0);
                this.mob.setSpeed(0);
                this.ticksUntilNextPathRecalculation = 0;
            }
            this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
            this.checkAndPerformAttack(target);
        }
    }

    protected void checkAndPerformAttack(LivingEntity target) {
        if (this.canPerformAttack(target)) {
            this.resetAttackCooldown();
            this.mob.swing(InteractionHand.MAIN_HAND);
            this.mob.doHurtTarget(getServerLevel(this.mob), target);
        } else if (this.isTimeToAttack() && this.mob instanceof Oni oni
                && oni.isAttackAnimationRunning()) {
            this.resetAttackCooldown();
        }
    }

    protected void resetAttackCooldown() {
        this.ticksUntilNextAttack = this.getAttackInterval();
    }

    protected boolean isTimeToAttack() {
        return this.ticksUntilNextAttack <= 0;
    }

    protected boolean canPerformAttack(LivingEntity target) {
        return this.isTimeToAttack()
                && this.mob.isWithinMeleeAttackRange(target)
                && this.hasLineOfSight(target);
    }

    protected boolean hasLineOfSight(LivingEntity target) {
        Vec3 start = this.mob.getEyePosition();

        AABB box = target.getBoundingBox();
        double minX = box.minX;
        double maxX = box.maxX;
        double minY = box.minY;
        double maxY = box.maxY;
        double minZ = box.minZ;
        double maxZ = box.maxZ;

        Vec3[] points = new Vec3[]{
                new Vec3((minX + maxX) * 0.5, minY + 0.1, (minZ + maxZ) * 0.5),
                new Vec3((minX + maxX) * 0.5, (minY + maxY) * 0.5, (minZ + maxZ) * 0.5),
                new Vec3((minX + maxX) * 0.5, maxY - 0.1, (minZ + maxZ) * 0.5),
                new Vec3(minX + 0.1, (minY + maxY) * 0.5, (minZ + maxZ) * 0.5),
                new Vec3(maxX - 0.1, (minY + maxY) * 0.5, (minZ + maxZ) * 0.5),
                new Vec3((minX + maxX) * 0.5, (minY + maxY) * 0.5, minZ + 0.1),
                new Vec3((minX + maxX) * 0.5, (minY + maxY) * 0.5, maxZ - 0.1)
        };

        for (Vec3 point : points) {
            HitResult result = this.mob.level().clip(new ClipContext(
                    start,
                    point,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    this.mob
            ));

            if (result.getType() == HitResult.Type.MISS) {
                return true;
            }
        }

        return false;
    }

    protected int getTicksUntilNextAttack() {
        return this.ticksUntilNextAttack;
    }

    public NPCMeleeAttackGoal setAttackInterval(int attackInterval) {
        this.attackInterval = attackInterval;
        return this;
    }

    public NPCMeleeAttackGoal setSpeedModifier(double speedModifier) {
        this.speedModifier = speedModifier;
        return this;
    }

    public int getAttackInterval() {
        return this.adjustedTickDelay(this.attackInterval);
    }

    public NPCMeleeAttackGoal setAttackWindup(int attackWindup) {
        this.attackWindup = attackWindup;
        return this;
    }

    public int getAttackWindup() {
        return this.adjustedTickDelay(this.attackWindup);
    }
}
