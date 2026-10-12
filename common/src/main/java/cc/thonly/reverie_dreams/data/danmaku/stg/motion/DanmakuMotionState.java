package cc.thonly.reverie_dreams.data.danmaku.stg.motion;

import cc.thonly.reverie_dreams.entity.misc.BaseDanmakuEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;

/**
 * {@link DanmakuMotion} 的每颗子弹运行时步进状态。
 *
 * <p>服务端与客户端各自持有一份，从相同的初始状态出发逐 tick 确定性推进；
 * ORBIT 段直接根据圆解析解反推位移，避免离散积分带来的半径漂移。
 */
public final class DanmakuMotionState {
    private static final double MAX_SPEED = 32.0D;
    private static final double MAX_STEP = 16.0D;

    private boolean initialized = false;
    private Vec3 direction = Vec3.ZERO;
    private double speed = 0.0D;
    private int age = -1;
    private int activeIndex = -1;
    private int segmentAge = 0;
    private Vec3 capturedPosition = Vec3.ZERO;
    private Vec3 capturedDirection = Vec3.ZERO;
    private Vec3 orbitCenter = Vec3.ZERO;

    public void tick(BaseDanmakuEntity entity, DanmakuMotion motion) {
        if (!this.initialized) {
            this.initialize(entity, motion);
        }
        this.age++;

        List<MotionSegment> segments = motion.segments();
        int nextIndex = -1;
        for (int i = 0; i < segments.size(); i++) {
            if (segments.get(i).at() <= this.age) {
                nextIndex = i;
            } else {
                break;
            }
        }
        if (nextIndex != this.activeIndex) {
            this.activeIndex = nextIndex;
            this.activate(entity, motion, nextIndex < 0 ? null : segments.get(nextIndex));
        }

        MotionSegment active = this.activeIndex < 0 ? null : segments.get(this.activeIndex);
        Vec3 displacement;
        if (active != null && active.is(MotionSegment.ORBIT)) {
            displacement = this.stepOrbit(entity, active);
        } else {
            displacement = this.stepDirectional(entity, motion, active);
        }

        if (displacement.lengthSqr() > MAX_STEP * MAX_STEP) {
            displacement = displacement.normalize().scale(MAX_STEP);
        }
        entity.setDeltaMovement(displacement);
    }

    private Vec3 stepOrbit(BaseDanmakuEntity entity, MotionSegment segment) {
        this.segmentAge++;
        Vec3 radiusVector = this.capturedPosition.subtract(this.orbitCenter);
        Vec3 desired = this.orbitCenter.add(rotate(radiusVector, segment.axis(), segment.value() * this.segmentAge));
        Vec3 displacement = desired.subtract(entity.position());
        if (displacement.lengthSqr() > 1.0E-8D) {
            this.direction = displacement.normalize();
        }
        return displacement;
    }

    private Vec3 stepDirectional(BaseDanmakuEntity entity, DanmakuMotion motion, @Nullable MotionSegment active) {
        double acceleration = motion.acceleration();
        if (active != null && active.is(MotionSegment.ACCELERATE)) {
            acceleration += active.value();
        }
        this.speed = Mth.clamp(this.speed + acceleration, 0.0D, MAX_SPEED);

        if (active != null && active.is(MotionSegment.ROTATE)) {
            this.segmentAge++;
            if (active.duration() < 0 || this.segmentAge <= active.duration()) {
                this.direction = rotate(this.capturedDirection, active.axis(), active.value() * this.segmentAge);
            }
        } else if (active != null && active.is(MotionSegment.HOMING)) {
            if (active.duration() < 0 || this.segmentAge < active.duration()) {
                this.segmentAge++;
                if (!entity.level().isClientSide()) {
                    LivingEntity target = this.resolveTarget(entity);
                    if (target != null) {
                        Vec3 toTarget = target.getEyePosition().subtract(entity.position());
                        if (toTarget.lengthSqr() > 1.0E-6D) {
                            this.direction = rotateTowards(this.direction, toTarget.normalize(), Math.toRadians(active.value()));
                        }
                    }
                }
            }
        }

        return this.direction.scale(this.speed);
    }

    private void initialize(BaseDanmakuEntity entity, DanmakuMotion motion) {
        this.initialized = true;
        Vec3 velocity = motion.velocity();
        if (velocity.lengthSqr() < 1.0E-8D) {
            velocity = entity.getDeltaMovement();
        }
        if (velocity.lengthSqr() < 1.0E-8D) {
            velocity = entity.getLookAngle();
        }
        if (velocity.lengthSqr() < 1.0E-8D) {
            velocity = new Vec3(0.0D, 0.0D, 1.0D);
        }
        this.speed = velocity.length();
        this.direction = velocity.normalize();
        this.capturedPosition = entity.position();
        this.capturedDirection = this.direction;
    }

    private void activate(BaseDanmakuEntity entity, DanmakuMotion motion, @Nullable MotionSegment segment) {
        this.segmentAge = 0;
        this.capturedPosition = entity.position();
        this.capturedDirection = this.direction;
        if (segment != null && segment.is(MotionSegment.ORBIT)) {
            Vec3 base = this.capturedPosition;
            if (segment.usesOriginCenter()) {
                Vec3 origin = motion.origin();
                base = origin.lengthSqr() < 1.0E-8D ? this.capturedPosition : origin;
            }
            this.orbitCenter = base.add(segment.center());
        }
    }

    @Nullable
    private LivingEntity resolveTarget(BaseDanmakuEntity entity) {
        if (entity.getOwner() instanceof Mob mob && mob.getTarget() != null && mob.getTarget().isAlive()) {
            return mob.getTarget();
        }
        if (entity.level() instanceof ServerLevel level) {
            return level.getNearestPlayer(entity.getX(), entity.getY(), entity.getZ(), 64.0D, false);
        }
        return null;
    }

    public static Vec3 rotate(Vec3 vector, Vec3 axis, float degrees) {
        if (vector.lengthSqr() < 1.0E-8D || degrees == 0f) {
            return vector;
        }
        Vec3 normalizedAxis = axis.lengthSqr() < 1.0E-8D ? new Vec3(0.0D, 1.0D, 0.0D) : axis.normalize();
        Vector3f vec = new Vector3f((float) vector.x, (float) vector.y, (float) vector.z);
        vec.rotateAxis((float) Math.toRadians(degrees), (float) normalizedAxis.x, (float) normalizedAxis.y, (float) normalizedAxis.z);
        return new Vec3(vec.x, vec.y, vec.z);
    }

    public static Vec3 rotateTowards(Vec3 current, Vec3 target, double maxRadians) {
        double dot = Mth.clamp(current.dot(target), -1.0D, 1.0D);
        double angle = Math.acos(dot);
        if (angle < 1.0E-6D || angle <= maxRadians) {
            return target;
        }
        Vec3 axis = current.cross(target);
        if (axis.lengthSqr() < 1.0E-8D) {
            return current;
        }
        return rotate(current, axis.normalize(), (float) Math.toDegrees(maxRadians));
    }
}
