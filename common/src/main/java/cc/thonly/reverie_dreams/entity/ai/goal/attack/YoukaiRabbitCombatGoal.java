package cc.thonly.reverie_dreams.entity.ai.goal.attack;

import cc.thonly.reverie_dreams.api.entity.type.DanmakuShooter;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.entity.YoukaiRabbit;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Short, readable self-defence with bounded pursuit; owns both navigation and attack timing.
 */
public final class YoukaiRabbitCombatGoal extends Goal {
    private static final int ATTACK_RECOVERY = 34;
    private static final int MISSED_STRIKE_RETRY = 8;
    private static final int SHOT_LIFETIME = 35;
    private static final float SHOT_SPEED = .32F;
    private static final int RANGED_WINDUP = 6;

    private final YoukaiRabbit rabbit;
    private LivingEntity strikeTarget;
    private int strikeAt;
    private int readyAt;
    private int retreatReadyAt;
    private int pathReadyAt;
    private boolean melee;

    public YoukaiRabbitCombatGoal(YoukaiRabbit rabbit) {
        this.rabbit = rabbit;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return this.rabbit.getTarget() != null && this.rabbit.canDefendAgainst(this.rabbit.getTarget());
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
        this.strikeTarget = null;
        this.strikeAt = 0;
        this.rabbit.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity target = this.rabbit.getTarget();
        if (target == null || !(this.rabbit.level() instanceof ServerLevel level))
            return;
        int now = this.rabbit.tickCount;
        this.rabbit.getLookControl().setLookAt(target, 30, 30);
        boolean visible = this.rabbit.getSensing().hasLineOfSight(target);
        if (this.strikeAt > 0) {
            if (target == this.strikeTarget) {
                this.rabbit.getNavigation().stop();
                if (now < this.strikeAt)
                    return;
                boolean attempted = visible && this.strike(level, target);
                this.readyAt = now + (attempted ? ATTACK_RECOVERY : MISSED_STRIKE_RETRY);
                this.strikeAt = 0;
                this.strikeTarget = null;
                return;
            }
            this.strikeAt = 0;
            this.strikeTarget = null;
        }
        if (!visible || this.rabbit.distanceToSqr(target) > 8 * 8) {
            this.approach(target, now, false);
            return;
        }
        if (now < this.readyAt) {
            if (this.rabbit.distanceToSqr(target) < 3 * 3 && now >= this.retreatReadyAt && this.retreat(target))
                this.retreatReadyAt = now + 80;
            return;
        }
        this.melee = this.rabbit.isWithinMeleeAttackRange(target);
        if (!this.melee) {
            float[] aim = DanmakuShooter.getPitchYaw(this.rabbit, target);
            if (!this.clearShot(level, target, this.rabbit.getEyePosition(), aim[0], aim[1])) {
                this.approach(target, now, true);
                return;
            }
        }
        this.rabbit.getNavigation().stop();
        this.strikeTarget = target;
        this.strikeAt = now + (this.melee ? 5 : RANGED_WINDUP);
        this.rabbit.beginAction(this.melee ? "melee" : "ranged");
    }

    private boolean strike(ServerLevel level, LivingEntity target) {
        if (!this.rabbit.canDefendAgainst(target))
            return false;
        if (!this.melee)
            return this.rabbit.distanceToSqr(target) <= 100 && this.fire(level, target);
        if (!this.rabbit.isWithinMeleeAttackRange(target))
            return false;
        this.rabbit.doHurtTarget(level, target);
        return true;
    }

    private void approach(LivingEntity target, int now, boolean flank) {
        if (now < this.pathReadyAt)
            return;
        this.pathReadyAt = now + 10;
        if (flank) {
            Vec3 toward = target.position().subtract(this.rabbit.position()).multiply(1, 0, 1).normalize();
            Vec3 side = new Vec3(-toward.z, 0, toward.x).scale(2.5);
            if (this.tryStep(side, 1) || this.tryStep(side.reverse(), 1))
                return;
        }
        this.rabbit.getNavigation().moveTo(target, 1);
    }

    private boolean retreat(LivingEntity target) {
        if (!this.rabbit.onGround())
            return false;
        Vec3 away = this.rabbit.position().subtract(target.position()).multiply(1, 0, 1).normalize().scale(2.5);
        if (!this.tryStep(away, 1.05))
            return false;
        this.rabbit.getJumpControl().jump();
        return true;
    }

    private boolean tryStep(Vec3 offset, double speed) {
        if (offset.lengthSqr() < 1)
            return false;
        BlockPos destination = BlockPos.containing(this.rabbit.position().add(offset));
        var level = this.rabbit.level();
        if (!this.rabbit.withinRabbitHome(destination) || !level.hasChunkAt(destination)
                || !level.getBlockState(destination.below()).isFaceSturdy(level, destination.below(), Direction.UP)
                || !level.getBlockState(destination).isAir() || !level.getBlockState(destination.above()).isAir())
            return false;
        var path = this.rabbit.getNavigation().createPath(destination, 0);
        return path != null && path.canReach() && this.rabbit.getNavigation().moveTo(path, speed);
    }

    private boolean clearShot(ServerLevel level, LivingEntity target, Vec3 start, float pitch, float yaw) {
        Vec3 end = start.add(Vec3.directionFromRotation(pitch, yaw).scale(SHOT_SPEED * SHOT_LIFETIME));
        // The search box is only a broad phase. A nearby mob must actually intersect this shot.
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(start, end).inflate(.3),
                entity -> entity != this.rabbit && entity != target && entity.isAlive()
                        && (entity.getBoundingBox().inflate(.3).contains(start)
                        || entity.getBoundingBox().inflate(.3).clip(start, end).isPresent())).isEmpty();
    }

    private boolean fire(ServerLevel level, LivingEntity target) {
        Vec3 start = this.rabbit.getEyePosition();
        float[] aim = DanmakuShooter.getPitchYaw(this.rabbit, target);
        if (!this.clearShot(level, target, start, aim[0], aim[1]))
            return false;
        var stack = DanmakuTypes.random(DanmakuTypes.RICE).create();
        var properties = DanmakuProperties.ofDefault().withDamage(2).withSpeed(SHOT_SPEED).withSpeedVersion(1).withScale(.6F).withHitboxScale(.6F);
        stack.set(RDDataComponentTypes.DANMAKU_PROPERTIES.value(), properties);
        boolean fired = false;
        for (float offset : new float[]{0, -12, 12}) {
            float yaw = aim[1] + offset;
            if (offset != 0 && !this.clearShot(level, target, start, aim[0], yaw))
                continue;
            DanmakuEntity bullet = DanmakuEntity.create(level, this.rabbit, stack, start.x, start.y, start.z, aim[0], yaw, 0);
            bullet.setDuration(SHOT_LIFETIME);
            fired |= level.addFreshEntity(bullet);
        }
        if (fired)
            DanmakuShooter.soundDefault(this.rabbit);
        return fired;
    }
}
