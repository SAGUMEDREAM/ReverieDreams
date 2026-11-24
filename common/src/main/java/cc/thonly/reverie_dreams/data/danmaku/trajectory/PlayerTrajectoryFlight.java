package cc.thonly.reverie_dreams.data.danmaku.trajectory;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuMath;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.DanmakuMotion;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.entity.misc.BaseDanmakuEntity;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Server-only state for trajectories that interact with terrain, targets or other projectiles.
 */
public final class PlayerTrajectoryFlight {
    private static final int SPLIT_TICK = 7;
    private static final int LIFETIME = 65;
    private static final Map<UUID, WeakReference<DanmakuEntity>> SEEDS = new HashMap<>();
    private final PlayerTrajectoryType type;
    private final PlayerTrajectory.Frame aim;
    @Nullable
    private LivingEntity guidanceTarget;
    @Nullable
    private UUID pierced;
    private boolean changed;
    private boolean seedChild;

    private PlayerTrajectoryFlight(PlayerTrajectoryType type, PlayerTrajectory.Frame aim) {
        this.type = type;
        this.aim = aim;
    }

    @Nullable
    static PlayerTrajectoryFlight create(PlayerTrajectoryType type, ServerLevel level, @Nullable LivingEntity owner,
                                         Vec3 source, PlayerTrajectory.Frame aim) {
        return switch (type) {
            case WALL_RICOCHET, NEEDLE_PIERCE, STAR_FORK, GROUND_SKIMMER, PROXIMITY_SEED, INTERCEPT_SHOT ->
                    new PlayerTrajectoryFlight(type, aim);
            case SHORT_GUIDANCE -> {
                var flight = new PlayerTrajectoryFlight(type, aim);
                flight.guidanceTarget = selectEnemy(level, owner, source, aim.forward(), 12, 12, true);
                yield flight;
            }
            default -> null;
        };
    }

    @Nullable
    static DanmakuEntity launch(ServerLevel level, @Nullable Entity owner, ItemStack stack, Vec3 source, Vec3 position,
                                DanmakuMotion motion, float damage, @Nullable PlayerTrajectoryFlight flight) {
        if (!level.isPositionEntityTicking(BlockPos.containing(position))
                || (!position.equals(source) && !clearLine(level, owner, source, position))) {
            return null;
        }
        var properties = stack.get(RDDataComponentTypes.DANMAKU_PROPERTIES.value());
        if (properties == null) {
            return null;
        }

        var projectile = new DanmakuEntity(position.x, position.y, position.z, level, stack.copyWithCount(1));
        if (owner != null) {
            projectile.setOwner(owner);
        }
        projectile.setDanmakuProperties(properties.withDamage(damage));
        projectile.setNoGravity(true);
        projectile.setDuration(LIFETIME);
        projectile.setPlayerTrajectoryFlight(flight);
        Vec3 velocity = motion.velocity();
        float[] angles = DanmakuMath.pitchYaw(Vec3.ZERO, velocity);
        projectile.shootFromRotation(angles[0], angles[1], 0, (float) velocity.length(), 0);
        projectile.setDanmakuMotion(motion.withOrigin(position));
        if (!level.addFreshEntity(projectile)) {
            return null;
        }
        if (flight != null && flight.isSeed() && owner != null) {
            var previous = SEEDS.put(owner.getUUID(), new WeakReference<>(projectile));
            DanmakuEntity old = previous == null ? null : previous.get();
            if (old != null) {
                old.discard();
            }
        }
        return projectile;
    }

    static void pruneSeeds() {
        SEEDS.values().removeIf(reference -> {
            var seed = reference.get();
            return seed == null || seed.isRemoved();
        });
    }

    static void clearSeeds() {
        for (var reference : SEEDS.values()) {
            var seed = reference.get();
            if (seed != null) {
                seed.discard();
            }
        }
        SEEDS.clear();
    }

    public boolean canHit(Entity target, @Nullable Entity owner) {
        return (this.pierced == null || !this.pierced.equals(target.getUUID()))
                && (this.type != PlayerTrajectoryType.PROXIMITY_SEED
                || this.seedChild && target instanceof LivingEntity living && eligibleEnemy(living, owner, false));
    }

    public boolean continueAfterHit(BaseDanmakuEntity projectile, Entity target, boolean damaged) {
        if (this.type != PlayerTrajectoryType.NEEDLE_PIERCE || this.pierced != null || !damaged
                || !(target instanceof LivingEntity living) || !eligibleEnemy(living, projectile.getOwner(), true)) {
            return false;
        }
        this.pierced = target.getUUID();
        projectile.redirect(projectile.getDeltaMovement().scale(0.82));
        return true;
    }

    public boolean hitBlock(BaseDanmakuEntity projectile, ServerLevel level, BlockHitResult impact) {
        Vec3 normal = Vec3.atLowerCornerOf(impact.getDirection().getUnitVec3i());
        Vec3 velocity = projectile.getDeltaMovement();
        if (this.type == PlayerTrajectoryType.WALL_RICOCHET && !this.changed
                && impact.getDirection().getAxis().isHorizontal() && velocity.dot(normal) < -0.01
                && level.getBlockState(impact.getBlockPos()).isFaceSturdy(level, impact.getBlockPos(), impact.getDirection())) {
            this.changed = true;
            Vec3 reflected = velocity.subtract(normal.scale(2 * velocity.dot(normal))).scale(0.72);
            projectile.setPos(impact.getLocation().add(normal.scale(0.04)));
            projectile.redirect(reflected);
            projectile.setDanmakuProperties(projectile.getDanmakuProperties()
                    .withDamage(projectile.getDanmakuProperties().damage() * 0.72f));
            return true;
        }
        if (impact.getDirection() != Direction.UP || this.changed) {
            return false;
        }
        if (this.type == PlayerTrajectoryType.GROUND_SKIMMER || this.isSeed()) {
            this.changed = true;
            projectile.setPos(impact.getLocation().add(0, 0.19, 0));
            if (this.type == PlayerTrajectoryType.PROXIMITY_SEED) {
                // A zero initial velocity falls back to the projectile's current motion.
                projectile.setDanmakuMotion(DanmakuMotion.of(this.aim.forward(), 0, 0,
                        List.of(MotionSegment.setSpeed(0, 0))).withOrigin(projectile.getDanmakuMotion().origin()));
                projectile.setDeltaMovement(Vec3.ZERO);
            }
            return true;
        }
        return false;
    }

    public void beforeMotion(BaseDanmakuEntity projectile, ServerLevel level) {
        if (this.seedChild) {
            return;
        }
        switch (this.type) {
            case STAR_FORK -> {
                if (projectile.tickCount >= SPLIT_TICK) {
                    // Both children continue with the parent's reduced damage and have no split state.
                    for (int yaw : new int[]{-18, 18}) {
                        Vec3 heading = this.aim.direction(yaw);
                        launchChild(projectile, level, heading);
                    }
                    projectile.discard();
                }
            }
            case GROUND_SKIMMER -> {
                if (this.changed) {
                    this.followGround(projectile, level);
                }
            }
            case PROXIMITY_SEED -> {
                if (this.changed && projectile.tickCount % 3 == 0) {
                    this.tryTrigger(projectile, level);
                }
            }
            case SHORT_GUIDANCE -> this.guide(projectile, level);
            default -> {
            }
        }
    }

    public void beforeCollision(BaseDanmakuEntity projectile, ServerLevel level, Predicate<Entity> canHit) {
        if (this.type != PlayerTrajectoryType.INTERCEPT_SHOT || projectile.getOwner() == null) {
            return;
        }
        Vec3 start = projectile.position();
        Vec3 end = start.add(projectile.getDeltaMovement());
        HitResult obstruction = ProjectileUtil.getHitResultOnMoveVector(projectile, canHit);
        if (obstruction.getType() != HitResult.Type.MISS) {
            end = obstruction.getLocation();
        }
        final Vec3 clipped = end;
        BaseDanmakuEntity closest = null;
        double distance = start.distanceToSqr(clipped);
        for (BaseDanmakuEntity other : level.getEntitiesOfClass(BaseDanmakuEntity.class,
                new AABB(start, clipped).inflate(0.25), candidate -> candidate.isTrajectoryInterceptable()
                        && candidate.getOwner() != null && candidate.getOwner() != projectile.getOwner()
                        && !(candidate.getOwner() instanceof Player)
                        && !projectile.getOwner().isAlliedTo(candidate.getOwner()))) {
            AABB bounds = other.getBoundingBox().inflate(0.10);
            Vec3 intersection = bounds.contains(start) ? start : bounds.clip(start, clipped).orElse(null);
            if (intersection != null && start.distanceToSqr(intersection) < distance) {
                closest = other;
                distance = start.distanceToSqr(intersection);
            }
        }
        if (closest != null) {
            closest.discard();
            projectile.discard();
        }
    }

    public boolean isSeed() {
        return this.type == PlayerTrajectoryType.PROXIMITY_SEED && !this.seedChild;
    }

    private void followGround(BaseDanmakuEntity projectile, ServerLevel level) {
        double speed = Math.min(0.85, projectile.getDanmakuProperties().actualSpeed() * 0.7);
        Vec3 current = projectile.position();
        Vec3 ahead = current.add(this.aim.horizontal().scale(speed));
        if (!level.isPositionEntityTicking(BlockPos.containing(ahead))) {
            projectile.discard();
            return;
        }
        Vec3 above = ahead.add(0, 0.35, 0);
        var floor = level.clip(new ClipContext(above, ahead.add(0, -0.55, 0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, projectile));
        if (floor.getType() != HitResult.Type.BLOCK || floor.getDirection() != Direction.UP
                || !level.getFluidState(floor.getBlockPos()).isEmpty()) {
            projectile.discard();
            return;
        }
        Vec3 next = floor.getLocation().add(0, 0.19, 0);
        if (!clearLine(level, projectile, current, next)) {
            projectile.discard();
            return;
        }
        Vec3 velocity = next.subtract(current);
        if (projectile.getDanmakuMotion().velocity().distanceToSqr(velocity) > 1.0E-6) {
            projectile.redirect(velocity);
        }
    }

    private void tryTrigger(BaseDanmakuEntity projectile, ServerLevel level) {
        Entity owner = projectile.getOwner();
        if (owner == null || !owner.isAlive() || owner.level() != level || owner.distanceToSqr(projectile) > 24 * 24) {
            projectile.discard();
            return;
        }
        Vec3 source = projectile.position();
        level.sendParticles(ParticleTypes.END_ROD, source.x, source.y + 0.16, source.z, 2, 0.25, 0.1, 0.25, 0.01);
        LivingEntity target = selectEnemy(level, owner, source, this.aim.forward(), 2.75, 180, false);
        if (target != null) {
            var childFlight = new PlayerTrajectoryFlight(PlayerTrajectoryType.PROXIMITY_SEED, this.aim);
            childFlight.seedChild = true;
            launchChild(projectile, level, target.getBoundingBox().getCenter().subtract(source).normalize(), childFlight);
            projectile.discard();
        }
    }

    private void guide(BaseDanmakuEntity projectile, ServerLevel level) {
        var target = this.guidanceTarget;
        if (target == null) {
            return;
        }
        Vec3 source = projectile.position();
        Vec3 goal = target.getBoundingBox().getCenter();
        Vec3 toward = goal.subtract(source).normalize();
        if (projectile.tickCount >= 8 || !target.isAlive() || target.level() != level
                || this.aim.forward().dot(toward) < Math.cos(Math.toRadians(24))
                || !clearLine(level, projectile, source, goal)) {
            this.guidanceTarget = null;
            return;
        }
        Vec3 previous = projectile.currentDirection();
        if (previous.lengthSqr() < 1.0E-8) {
            previous = this.aim.forward();
        }
        Vec3 turn = toward.subtract(previous).scale(0.24);
        if (turn.lengthSqr() > 0.0025) {
            turn = turn.normalize().scale(0.05);
        }
        projectile.redirect(previous.add(turn).normalize().scale(projectile.getDanmakuProperties().actualSpeed()));
    }

    private static void launchChild(BaseDanmakuEntity parent, ServerLevel level, Vec3 heading) {
        launchChild(parent, level, heading, null);
    }

    private static void launchChild(BaseDanmakuEntity parent, ServerLevel level, Vec3 heading,
                                    @Nullable PlayerTrajectoryFlight flight) {
        var properties = parent.getDanmakuProperties();
        Vec3 source = parent.position();
        var child = launch(level, parent.getOwner(), parent.getItemStack(), source, source,
                DanmakuMotion.straight(heading.scale(properties.actualSpeed())), properties.damage(), flight);
        if (child != null && parent.getSpellRuntime() != null) {
            parent.getSpellRuntime().track(child);
        }
    }

    @Nullable
    private static LivingEntity selectEnemy(ServerLevel level, @Nullable Entity owner, Vec3 source,
                                            Vec3 axis, double range, double degrees, boolean minorOnly) {
        double minimumDot = Math.cos(Math.toRadians(degrees));
        LivingEntity closest = null;
        double distance = range * range;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                new AABB(source, source).inflate(range), entity -> eligibleEnemy(entity, owner, minorOnly))) {
            Vec3 center = target.getBoundingBox().getCenter();
            Vec3 offset = center.subtract(source);
            double length = offset.lengthSqr();
            if (length < distance && axis.dot(offset.normalize()) >= minimumDot && clearLine(level, owner, source, center)) {
                closest = target;
                distance = length;
            }
        }
        return closest;
    }

    private static boolean eligibleEnemy(LivingEntity target, @Nullable Entity owner, boolean minorOnly) {
        return target.isAlive() && target != owner && !(target instanceof Player)
                && target instanceof Enemy && (!minorOnly || target.getMaxHealth() <= 50 && target.getBbWidth() <= 1.8)
                && (owner == null || !owner.isAlliedTo(target));
    }

    private static boolean clearLine(ServerLevel level, @Nullable Entity source, Vec3 start, Vec3 end) {
        return level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, source))
                .getType() == HitResult.Type.MISS;
    }
}
