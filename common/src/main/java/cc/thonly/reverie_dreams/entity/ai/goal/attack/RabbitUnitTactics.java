package cc.thonly.reverie_dreams.entity.ai.goal.attack;

import cc.thonly.reverie_dreams.api.entity.type.FriendlyFaction;
import cc.thonly.reverie_dreams.entity.RabbitUnit;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.item.weapon.LunarWeaponType;
import cc.thonly.reverie_dreams.item.weapon.WeaponOfTheMoon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Small, bounded tactical queries; this helper never acquires an unseen target.
 */
final class RabbitUnitTactics {
    record Threat(boolean imminent, boolean ranged, LivingEntity priority) {
    }

    private record Candidate(Vec3 position, double score) {
    }

    // Projectile.getMovementToShoot uses a bounded triangle sample on each axis in MC 26.1.2.
    private static final double SPREAD_AXIS = .0172275;
    // Includes vanilla's sweep margin and the .25 hitbox scale used by lunar bullets.
    private static final double HIT_MARGIN = .3;
    private final RabbitUnit actor;
    private Vec3 cover, spacingDestination, retreat;
    private int retreatUntil, nextRetreatScan;
    private int coverUntil, nextCoverScan, nextCoverRepath, spacingUntil, nextSpacingScan, nextLaneAttempt;
    private int nextThreatScan;
    private Threat threat = new Threat(false, false, null);

    RabbitUnitTactics(RabbitUnit actor) {
        this.actor = actor;
    }

    void reset() {
        this.clearDestinations();
        this.nextCoverScan = this.nextSpacingScan = this.nextLaneAttempt = this.nextThreatScan = 0;
        this.nextRetreatScan = 0;
        this.threat = new Threat(false, false, null);
    }

    void clearDestinations() {
        this.cover = this.spacingDestination = this.retreat = null;
        this.coverUntil = this.spacingUntil = this.retreatUntil = 0;
    }

    /**
     * Walk out of a gun's close-range band; at most three loaded, supported paths every forty ticks.
     */
    boolean gainDistance(Vec3 hostile, double minimumRange) {
        int now = this.actor.tickCount;
        if (this.actor.position().distanceToSqr(hostile) >= minimumRange * minimumRange) {
            if (this.retreat != null)
                this.actor.getNavigation().stop();
            this.retreat = null;
            return false;
        }
        if (this.retreat != null && now < this.retreatUntil && !this.actor.getNavigation().isDone()) {
            this.actor.getMoveControl().strafe(0, 0);
            return true;
        }
        this.retreat = null;
        if (now < this.nextRetreatScan || !this.navigationReady())
            return false;
        this.nextRetreatScan = now + 40;
        Vec3 away = this.actor.position().subtract(hostile).multiply(1, 0, 1);
        double distance = away.length(), step = Math.min(6, Math.max(2, minimumRange - distance + 1));
        double bearing = distance > .01 ? Math.atan2(away.z, away.x) : 0;
        for (double turn : new double[]{0, Math.PI / 4, -Math.PI / 4}) {
            Vec3 point = this.findSupported(this.actor.position().add(Math.cos(bearing + turn) * step, 0, Math.sin(bearing + turn) * step));
            if (point == null || point.distanceToSqr(hostile) < this.actor.distanceToSqr(hostile) + 4)
                continue;
            if (this.moveToKnownPosition(point, 1.05)) {
                this.retreat = point;
                this.retreatUntil = now + 40;
                this.spacingDestination = null;
                return true;
            }
        }
        return false;
    }

    Threat assessThreat(LivingEntity target) {
        if (this.actor.tickCount < this.nextThreatScan)
            return this.threat;
        this.nextThreatScan = this.actor.tickCount + 10;
        boolean visible = this.actor.getSensing().hasLineOfSight(target);
        boolean ranged = visible && isRanged(target);
        LivingEntity candidate = this.actor.getLastHurtByMob();
        boolean recentlyHit = this.actor.tickCount - this.actor.getLastHurtByMobTimestamp() <= 60;
        if (!recentlyHit || !this.visibleHostile(candidate))
            candidate = null;
        boolean imminent = candidate != null && this.actor.distanceToSqr(candidate) < 64;
        int inspected = 0;
        for (Projectile projectile : this.actor.level().getEntitiesOfClass(Projectile.class,
                this.actor.getBoundingBox().inflate(8), projectile -> projectile.isAlive() && projectile.getOwner() != this.actor)) {
            if (++inspected > 16)
                break;
            if (projectile.getOwner() instanceof LivingEntity owner && isProtected(this.actor, owner))
                continue;
            Vec3 from = projectile.position(), to = from.add(projectile.getDeltaMovement().scale(6));
            var collision = this.actor.getBoundingBox().inflate(.4).clip(from, to);
            if (collision.isEmpty() || !this.clearRay(from, collision.get()))
                continue;
            imminent = true;
            if (projectile.getOwner() instanceof LivingEntity owner && this.visibleHostile(owner)
                    && (candidate == null || this.threatScore(owner) > this.threatScore(candidate)))
                candidate = owner;
        }
        if (candidate == null || candidate == target || this.threatScore(candidate) < this.threatScore(target) + 4)
            candidate = null;
        this.threat = new Threat(imminent, ranged, candidate);
        return this.threat;
    }

    private boolean visibleHostile(LivingEntity entity) {
        return entity != null && entity.isAlive() && this.actor.canAttack(entity)
                && this.actor.distanceToSqr(entity) <= 24 * 24 && this.actor.getSensing().hasLineOfSight(entity);
    }

    private int threatScore(LivingEntity entity) {
        if (entity == null || !this.actor.getSensing().hasLineOfSight(entity))
            return 0;
        int score = this.actor.distanceToSqr(entity) < 36 ? 4 : 0;
        if (entity instanceof Mob mob && mob.getTarget() == this.actor)
            score += 4;
        if (isRanged(entity))
            score += 4;
        if (entity == this.actor.getLastHurtByMob()
                && this.actor.tickCount - this.actor.getLastHurtByMobTimestamp() <= 60)
            score += 8;
        return score;
    }

    private static boolean isRanged(LivingEntity entity) {
        var item = entity.getMainHandItem().getItem();
        return entity instanceof RangedAttackMob || item instanceof WeaponOfTheMoon || item instanceof BowItem || item instanceof CrossbowItem;
    }

    boolean takeCover(Vec3 hostileEye) {
        if (hostileEye == null)
            return false;
        int now = this.actor.tickCount;
        if (this.cover != null) {
            if (now >= this.coverUntil || !this.safePosition(this.cover) || !this.protectedPosition(this.cover, hostileEye)) {
                this.releaseCover();
                return false;
            }
            this.actor.getMoveControl().strafe(0, 0);
            if (this.actor.position().distanceToSqr(this.cover) < 1)
                this.actor.getNavigation().stop();
            else if (now >= this.nextCoverRepath && this.navigationReady()) {
                this.nextCoverRepath = now + 20;
                if (!this.moveToKnownPosition(this.cover, 1.15)) {
                    this.releaseCover();
                    return false;
                }
            }
            return true;
        }
        // GroundPathNavigation cannot create a path before the first landing tick.
        // Waiting for that prerequisite must not consume the forty-tick failed-search budget.
        if (!this.navigationReady() || now < this.nextCoverScan)
            return false;
        this.nextCoverScan = now + 40;
        if (this.safePosition(this.actor.position()) && this.protectedPosition(this.actor.position(), hostileEye)) {
            this.cover = this.actor.position();
            this.coverUntil = now + 60;
            this.actor.getNavigation().stop();
            this.actor.getMoveControl().strafe(0, 0);
            return true;
        }
        List<Candidate> candidates = new ArrayList<>(24);
        double bearing = Math.atan2(this.actor.getZ() - hostileEye.z, this.actor.getX() - hostileEye.x);
        // 24 horizontal candidates, with at most three supported heights at each candidate.
        for (int radius = 2; radius <= 6; radius += 2)
            for (int direction = 0; direction < 8; direction++) {
                double angle = bearing + direction * Math.PI / 4;
                Vec3 point = this.findSupported(this.actor.position().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius));
                if (point == null || point.distanceToSqr(hostileEye) < 16 || !this.protectedPosition(point, hostileEye))
                    continue;
                double score = this.actor.position().distanceToSqr(point) - Math.min(16, point.distanceToSqr(hostileEye)) * .1;
                candidates.add(new Candidate(point, score));
            }
        candidates.sort(Comparator.comparingDouble(Candidate::score));
        for (int attempt = 0; attempt < Math.min(3, candidates.size()); attempt++) {
            Vec3 point = candidates.get(attempt).position();
            if (!this.moveToKnownPosition(point, 1.15))
                continue;
            this.cover = point;
            this.coverUntil = now + 60;
            this.nextCoverRepath = now + 20;
            this.spacingDestination = null;
            return true;
        }
        return false;
    }

    void releaseCover() {
        if (this.cover != null)
            this.actor.getNavigation().stop();
        this.cover = null;
        this.nextCoverScan = Math.max(this.nextCoverScan, this.actor.tickCount + 40);
    }

    private boolean navigationReady() {
        return this.actor.onGround() || this.actor.isInLiquid() || this.actor.isPassenger();
    }

    private boolean protectedPosition(Vec3 feet, Vec3 hostileEye) {
        // A hidden torso alone is insufficient when the head remains exposed.
        Vec3 head = feet.add(0, this.actor.getEyeHeight(), 0), torso = feet.add(0, this.actor.getBbHeight() * .5, 0);
        return loadedRay(this.actor, hostileEye, head) && loadedRay(this.actor, hostileEye, torso)
                && !this.clearRay(hostileEye, head) && !this.clearRay(hostileEye, torso);
    }

    boolean maintainSpacing() {
        int now = this.actor.tickCount;
        if (this.spacingDestination != null && now < this.spacingUntil
                && this.actor.position().distanceToSqr(this.spacingDestination) > .5 && !this.actor.getNavigation().isDone()) {
            this.actor.getMoveControl().strafe(0, 0);
            return true;
        }
        this.spacingDestination = null;
        if (now < this.nextSpacingScan)
            return false;
        this.nextSpacingScan = now + 12;
        Vec3 separation = Vec3.ZERO;
        var allies = new ArrayList<>(this.actor.level().getEntitiesOfClass(RabbitUnit.class, this.actor.getBoundingBox().inflate(4, 2, 4),
                ally -> ally != this.actor && ally.isAlive() && !ally.isEvacuating()));
        allies.sort(Comparator.comparingDouble((RabbitUnit unit) -> this.actor.distanceToSqr(unit)).thenComparing(RabbitUnit::getUUID));
        int inspected = 0;
        for (RabbitUnit ally : allies) {
            if (++inspected > 8)
                break;
            // Stable priority prevents two units repeatedly choosing opposite evasions.
            if (this.actor.getUUID().compareTo(ally.getUUID()) < 0)
                continue;
            Vec3 away = this.actor.position().subtract(ally.position()).multiply(1, 0, 1);
            double distance = away.length();
            if (distance >= 2.2)
                continue;
            if (distance < .01)
                away = new Vec3((this.actor.getUUID().hashCode() & 1) == 0 ? 1 : -1, 0, 0);
            separation = separation.add(away.normalize().scale(2.2 - distance));
        }
        if (separation.lengthSqr() < .01)
            return false;
        Vec3 point = this.findSupported(this.actor.position().add(separation.normalize().scale(1.8)));
        return point != null && this.startSpacing(point);
    }

    void clearFiringLane(Vec3 hostileEye) {
        if (hostileEye == null || this.actor.tickCount < this.nextLaneAttempt)
            return;
        this.nextLaneAttempt = this.actor.tickCount + 20;
        Vec3 toward = hostileEye.subtract(this.actor.position()).multiply(1, 0, 1).normalize();
        int side = (this.actor.getUUID().hashCode() & 1) == 0 ? 1 : -1;
        for (int direction : new int[]{side, -side}) {
            Vec3 point = this.findSupported(this.actor.position().add(-toward.z * 2 * direction, 0, toward.x * 2 * direction));
            if (point != null && this.clearRay(point.add(0, this.actor.getEyeHeight(), 0), hostileEye) && this.startSpacing(point))
                return;
        }
    }

    private boolean startSpacing(Vec3 point) {
        if (!this.moveToKnownPosition(point, .95))
            return false;
        this.spacingDestination = point;
        this.spacingUntil = this.actor.tickCount + 24;
        return true;
    }

    boolean moveToKnownPosition(Vec3 point, double speed) {
        int range = Math.min(32, Math.max(8, (int) Math.ceil(this.actor.position().distanceTo(point)) + 2));
        BlockPos origin = this.actor.blockPosition();
        // PathNavigation builds a region with an eight-block margin; require it to be loaded first.
        if (!this.actor.level().hasChunksAt(origin.offset(-range - 8, 0, -range - 8), origin.offset(range + 8, 0, range + 8)))
            return false;
        Vec3 supported = this.findSupported(point);
        if (supported == null)
            return false;
        Path path = this.actor.getNavigation().createPath(BlockPos.containing(supported), 0, range);
        if (path == null || !path.canReach())
            return false;
        this.actor.getMoveControl().strafe(0, 0);
        return this.actor.getNavigation().moveTo(path, speed);
    }

    private Vec3 findSupported(Vec3 point) {
        BlockPos base = BlockPos.containing(point);
        for (int dy : new int[]{0, 1, -1}) {
            Vec3 candidate = Vec3.atBottomCenterOf(base.offset(0, dy, 0));
            if (this.safePosition(candidate))
                return candidate;
        }
        return null;
    }

    private boolean safePosition(Vec3 feet) {
        BlockPos pos = BlockPos.containing(feet);
        var world = this.actor.level();
        AABB body = this.actor.getBoundingBox().move(feet.subtract(this.actor.position()));
        if (!world.hasChunksAt(BlockPos.containing(body.minX, body.minY - 1, body.minZ), BlockPos.containing(body.maxX, body.maxY, body.maxZ))
                || !world.getWorldBorder().isWithinBounds(pos) || world.isOutsideBuildHeight(pos)
                || !world.getBlockState(pos.below()).isFaceSturdy(world, pos.below(), Direction.UP)
                || !world.noCollision(this.actor, body))
            return false;
        for (BlockPos checked : new BlockPos[]{pos.below(), pos, pos.above()}) {
            var state = world.getBlockState(checked);
            if (!state.getFluidState().isEmpty() || state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE) || state.is(Blocks.CACTUS)
                    || state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.SWEET_BERRY_BUSH) || state.is(Blocks.POWDER_SNOW))
                return false;
        }
        return true;
    }

    private boolean clearRay(Vec3 from, Vec3 to) {
        return loadedRay(this.actor, from, to) && this.actor.level().clip(new ClipContext(from, to,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.actor)).getType() == HitResult.Type.MISS;
    }

    private static boolean loadedRay(RabbitUnit actor, Vec3 from, Vec3 to) {
        return actor.level().hasChunksAt(BlockPos.containing(Math.min(from.x, to.x), Math.min(from.y, to.y), Math.min(from.z, to.z)),
                BlockPos.containing(Math.max(from.x, to.x), Math.max(from.y, to.y), Math.max(from.z, to.z)));
    }

    static boolean hasClearShot(RabbitUnit actor, LivingEntity target, LunarWeaponType type) {
        if (target == null || !target.isAlive() || !actor.canAttack(target))
            return false;
        // This is the actual DanmakuEntity muzzle, shared by native and Polymer firing.
        Vec3 from = actor.getEyePosition(), to = target.getBoundingBox().getCenter();
        if (!loadedRay(actor, from, to) || actor.level().clip(new ClipContext(from, to,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, actor)).getType() != HitResult.Type.MISS)
            return false;
        Vec3 inherited = actor.getKnownMovement();
        if (actor.onGround())
            inherited = inherited.multiply(1, 0, 1);
        Vec3 velocity = to.subtract(from).normalize().scale(type.speed()).add(inherited);
        Vec3 flightToTarget = from.add(velocity.scale(from.distanceTo(to) / type.speed()));
        if (!loadedRay(actor, from, flightToTarget) || actor.level().clip(new ClipContext(from, flightToTarget,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, actor)).getType() != HitResult.Type.MISS)
            return false;
        double deviation = type.speed() * SPREAD_AXIS * type.spread(false);
        double lifetime = type.lifetime();
        // Short sections avoid querying the enormous AABB of a long diagonal sniper trajectory.
        for (double tick = 0; tick < lifetime; tick += 8) {
            double endTick = Math.min(lifetime, tick + 8);
            AABB corridor = new AABB(from.add(velocity.scale(tick)), from.add(velocity.scale(endTick)))
                    .inflate(deviation * endTick + HIT_MARGIN + 2);
            for (LivingEntity friend : actor.level().getEntitiesOfClass(LivingEntity.class, corridor,
                    entity -> entity != actor && entity != target && entity.isAlive() && isProtected(actor, entity))) {
                AABB swept = friend.getBoundingBox().expandTowards(friend.getKnownMovement().scale(Math.min(6, lifetime))).inflate(HIT_MARGIN);
                // Includes pellets which miss the target and continue for the remainder of their lifetime.
                if (intersectsSpread(from, velocity, deviation, lifetime, swept))
                    return false;
            }
        }
        return true;
    }

    private static boolean isProtected(RabbitUnit actor, LivingEntity entity) {
        return entity instanceof RabbitUnit || entity == actor.getOwner() || actor.isAlliedTo(entity)
                || entity instanceof BaseNPCLikeEntity npc && actor.isAlliedTo(npc) || FriendlyFaction.isFriendLy(actor, entity);
    }

    /**
     * Exact intersection of a finite, per-axis bounded spread envelope with an AABB.
     */
    static boolean intersectsSpread(Vec3 origin, Vec3 velocity, double deviation, double lifetime, AABB box) {
        double[] time = {0, lifetime};
        return axis(time, origin.x, velocity.x, deviation, box.minX, box.maxX)
                && axis(time, origin.y, velocity.y, deviation, box.minY, box.maxY)
                && axis(time, origin.z, velocity.z, deviation, box.minZ, box.maxZ);
    }

    private static boolean axis(double[] time, double origin, double velocity, double deviation, double min, double max) {
        return constrain(time, -velocity - deviation, origin - min) && constrain(time, velocity - deviation, max - origin);
    }

    private static boolean constrain(double[] time, double slope, double bound) {
        if (Math.abs(slope) < 1.0e-10)
            return bound >= 0;
        if (slope > 0)
            time[1] = Math.min(time[1], bound / slope);
        else
            time[0] = Math.max(time[0], bound / slope);
        return time[0] <= time[1];
    }
}
