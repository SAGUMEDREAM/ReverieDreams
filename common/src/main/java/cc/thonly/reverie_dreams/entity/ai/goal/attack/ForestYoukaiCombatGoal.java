package cc.thonly.reverie_dreams.entity.ai.goal.attack;

import cc.thonly.reverie_dreams.api.entity.type.DanmakuShooter;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.entity.ForestYoukai;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.entity.variant.YoukaiSpecies;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * Both roles share target validation, readable windup and a bounded navigation budget.
 */
public final class ForestYoukaiCombatGoal extends Goal {
    private static final float SHOT_SPEED = .4F;
    private static final int SHOT_LIFETIME = 45;
    private final ForestYoukai mob;
    private @Nullable LivingEntity strikeTarget;
    private int strikeAt;
    private int readyAt;
    private int nextPath;
    private int nextManeuver;
    private int strafeDirection = 1;

    public ForestYoukaiCombatGoal(ForestYoukai mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.mob.getTarget() != null && this.mob.canContinueFight(this.mob.getTarget());
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
    public void stop() {
        this.strikeAt = 0;
        this.strikeTarget = null;
        this.mob.getNavigation().stop();
        this.nextPath = 0;
        this.nextManeuver = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null || !(this.mob.level() instanceof ServerLevel level))
            return;
        int now = this.mob.tickCount;
        this.mob.getLookControl().setLookAt(target, 30, 30);
        float yaw = (float) (Mth.atan2(target.getZ() - this.mob.getZ(), target.getX() - this.mob.getX()) * Mth.RAD_TO_DEG) - 90;
        this.mob.setYRot(Mth.rotLerp(.35F, this.mob.getYRot(), yaw));
        this.mob.setYBodyRot(this.mob.getYRot());
        boolean visible = this.mob.getSensing().hasLineOfSight(target);
        double distance = this.mob.distanceToSqr(target);
        this.moveForCombat(target, visible, distance, now);
        if (this.strikeAt != 0) {
            if (target != this.strikeTarget) {
                this.strikeAt = 0;
                this.strikeTarget = null;
                this.readyAt = now + 4;
                return;
            }
            if (now < this.strikeAt)
                return;
            boolean hit = target == this.strikeTarget && visible && this.mob.canContinueFight(target)
                    && (this.mob.isRanged() ? this.fire(level, target)
                    : this.mob.isWithinMeleeAttackRange(target) && this.mob.doHurtTarget(level, target));
            this.readyAt = now + (hit ? (this.mob.isRanged() ? this.mob.species().rangedInterval : this.mob.species().meleeInterval) : 8);
            this.strikeAt = 0;
            this.strikeTarget = null;
            return;
        }
        if (!visible || (this.mob.isRanged() ? distance > 12 * 12 : !this.mob.isWithinMeleeAttackRange(target))) {
            return;
        }
        if (now < this.readyAt)
            return;
        this.strikeTarget = target;
        this.strikeAt = now + (this.mob.isRanged() ? 6 : 5);
        this.mob.beginAction(this.mob.isRanged() ? 2 : 1);
    }

    private void moveForCombat(LivingEntity target, boolean visible, double distance, int now) {
        if (!this.mob.isRanged()) {
            // Repath during the windup and recovery too: a moving victim cannot leave us planted in place.
            if (now >= this.nextPath) {
                this.nextPath = now + 6;
                this.mob.getNavigation().moveTo(target, 1.25);
            }
            return;
        }
        if (!visible || distance > 10 * 10) {
            if (now >= this.nextPath) {
                this.nextPath = now + 6;
                this.mob.getNavigation().moveTo(target, 1.15);
            }
            return;
        }
        if (now < this.nextManeuver) return;
        this.nextManeuver = now + (this.mob.species() == YoukaiSpecies.FOX ? 12 : 16);
        Vec3 away = this.mob.position().subtract(target.position()).multiply(1, 0, 1).normalize();
        if (away.lengthSqr() < .001) away = Vec3.directionFromRotation(0, this.mob.getYRot());
        Vec3 sideways = new Vec3(-away.z, 0, away.x).scale(this.strafeDirection);
        Vec3 step = distance < 6 * 6 ? away.scale(4).add(sideways.scale(1.5)) : sideways.scale(3);
        if (!this.maneuver(step)) {
            this.strafeDirection = -this.strafeDirection;
            this.maneuver(distance < 6 * 6 ? away.scale(3).subtract(sideways) : sideways.scale(-3));
        }
    }

    private boolean maneuver(Vec3 step) {
        BlockPos pos = BlockPos.containing(this.mob.position().add(step));
        if (!this.mob.level().hasChunkAt(pos) || !this.mob.level().getBlockState(pos.below()).isFaceSturdy(this.mob.level(), pos.below(), Direction.UP)
                || !this.mob.level().getBlockState(pos).isAir() || !this.mob.level().getBlockState(pos.above()).isAir())
            return false;
        var path = this.mob.getNavigation().createPath(pos, 0);
        return path != null && path.canReach() && this.mob.getNavigation().moveTo(path, 1.2);
    }

    private boolean clearShot(ServerLevel level, LivingEntity target, Vec3 start, float pitch, float yaw) {
        Vec3 end = start.add(Vec3.directionFromRotation(pitch, yaw).scale(SHOT_SPEED * SHOT_LIFETIME));
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(start, end).inflate(.3),
                entity -> entity != this.mob && entity != target && entity.isAlive()
                        && (entity.getBoundingBox().inflate(.3).contains(start)
                        || entity.getBoundingBox().inflate(.3).clip(start, end).isPresent())).isEmpty();
    }

    private boolean fire(ServerLevel level, LivingEntity target) {
        if (this.mob.distanceToSqr(target) > 14 * 14 || !this.mob.canAttack(target))
            return false;
        Vec3 start = this.mob.getEyePosition();
        float[] aim = DanmakuShooter.getPitchYaw(this.mob, target);
        var stack = DanmakuTypes.random(DanmakuTypes.RICE).create();
        stack.set(RDDataComponentTypes.DANMAKU_PROPERTIES.value(), DanmakuProperties.ofDefault()
                .withDamage(3).withSpeed(SHOT_SPEED).withSpeedVersion(1).withScale(.6F).withHitboxScale(.6F));
        float[] offsets = this.mob.species() == YoukaiSpecies.FOX ? new float[]{0, -10, 10} : new float[]{0};
        boolean fired = false;
        for (float offset : offsets) {
            float yaw = aim[1] + offset;
            if (!this.clearShot(level, target, start, aim[0], yaw))
                continue;
            DanmakuEntity bullet = DanmakuEntity.create(level, this.mob, stack, start.x, start.y, start.z, aim[0], yaw, 0);
            bullet.setDuration(SHOT_LIFETIME);
            fired |= level.addFreshEntity(bullet);
        }
        if (fired)
            DanmakuShooter.soundDefault(this.mob);
        return fired;
    }
}
