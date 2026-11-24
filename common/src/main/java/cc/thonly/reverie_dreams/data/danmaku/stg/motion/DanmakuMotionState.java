package cc.thonly.reverie_dreams.data.danmaku.stg.motion;

import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuMath;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuSpawner;
import cc.thonly.reverie_dreams.data.danmaku.stg.MovingLaserSpec;
import cc.thonly.reverie_dreams.entity.misc.BaseDanmakuEntity;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
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
    private int processedIndex = -1;
    private int segmentAge = 0;
    private boolean speedAssignedThisTick = false;
    private Vec3 capturedPosition = Vec3.ZERO;
    private Vec3 capturedDirection = Vec3.ZERO;
    private Vec3 orbitCenter = Vec3.ZERO;
    /**
     * 服务端图案操作（FREEZE_OWNED）写入的冻结截止 tick（相对 age 的绝对值）。
     */
    private int serverFreezeUntilTick = 0;

    public void tick(BaseDanmakuEntity entity, DanmakuMotion motion) {
        if (!this.initialized) {
            this.initialize(entity, motion);
        }
        this.age++;

        List<MotionSegment> segments = motion.segments();
        this.speedAssignedThisTick = false;
        this.activateDueSegments(entity, motion, segments);
        if (entity.isRemoved()) {
            return;
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
        // 服务端图案操作触发的冻结：位移为 0、不累积加速度
        if (this.serverFreezeUntilTick > this.age) {
            return Vec3.ZERO;
        }
        // FREEZE 段：冻结期位移为 0 且不累积加速度；过后自然恢复原速原向
        if (active != null && active.is(MotionSegment.FREEZE)) {
            if (active.duration() < 0 || this.segmentAge < active.duration()) {
                this.segmentAge++;
                return Vec3.ZERO;
            }
        }

        boolean assignedSpeed = this.speedAssignedThisTick;
        double acceleration = motion.acceleration();
        if (active != null && active.is(MotionSegment.ACCELERATE)) {
            acceleration += active.value();
        }
        if (this.speedAssignedThisTick) {
            this.speedAssignedThisTick = false;
        } else {
            this.speed = Mth.clamp(this.speed + acceleration, 0.0D, MAX_SPEED);
        }

        if (active != null && active.is(MotionSegment.ACCELERATE_VECTOR)) {
            if (active.duration() < 0 || this.segmentAge < active.duration()) {
                this.segmentAge++;
                if (!assignedSpeed) {
                    this.applyVectorAcceleration(active.axis());
                }
            }
        } else if (active != null && active.is(MotionSegment.ROTATE)) {
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

        // BOUNCE 段：超出以 motion.origin 为圆心的水平圆边界时反射径向水平分量并回到边界内侧
        if (active != null && active.is(MotionSegment.BOUNCE)) {
            Vec3 bounceDisplacement = this.stepBounce(entity, motion, active);
            if (bounceDisplacement != null) {
                return bounceDisplacement;
            }
        }

        Vec3 displacement = this.direction.scale(this.speed);
        if (active != null && active.is(MotionSegment.LATERAL_WAVE)) {
            double oldPhase = this.segmentAge++ * Math.PI / 6;
            double phase = this.segmentAge * Math.PI / 6;
            displacement = displacement
                    .add(active.axis().scale((Math.sin(phase) - Math.sin(oldPhase)) * active.value()))
                    .add(active.center().scale((Math.cos(oldPhase) - Math.cos(phase)) * active.value()));
        }
        return displacement;
    }

    void applyVectorAcceleration(Vec3 acceleration) {
        Vec3 velocity = this.direction.scale(this.speed).add(acceleration);
        double magnitude = velocity.length();
        this.speed = Math.min(MAX_SPEED, magnitude);
        // Keep the old heading if speed is zero. A later acceleration can restart it
        // in either direction without requiring a look-angle or target fallback.
        if (magnitude > 0) {
            this.direction = velocity.scale(1.0D / magnitude);
        }
    }

    /**
     * BOUNCE 反射检查；返回 {@code null} 表示本 tick 未越界，按普通位移继续。
     */
    @Nullable
    private Vec3 stepBounce(BaseDanmakuEntity entity, DanmakuMotion motion, MotionSegment segment) {
        double radius = segment.value();
        if (radius <= 0.0D) {
            return null;
        }
        Vec3 origin = motion.origin();
        if (origin.lengthSqr() < 1.0E-8D && segment.payload() == null && segment.beam() == null) {
            return null;
        }
        Vec3 delta = entity.position().subtract(origin);
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        if (horizontal <= radius || horizontal < 1.0E-8D) {
            return null;
        }
        double radialX = delta.x / horizontal;
        double radialZ = delta.z / horizontal;
        // 反射方向的水平径向分量：v - 2(v·r)r（r 为水平径向单位向量）
        double radialDot = this.direction.x * radialX + this.direction.z * radialZ;
        this.direction = new Vec3(
                this.direction.x - 2.0D * radialDot * radialX,
                this.direction.y,
                this.direction.z - 2.0D * radialDot * radialZ
        );
        // 本 tick 的位移改为把子弹拉回边界，下一 tick 沿反射方向向内飞
        double overshoot = horizontal - radius;
        Vec3 backToBoundary = new Vec3(-radialX * overshoot, 0.0D, -radialZ * overshoot);
        Vec3 impact = entity.position().add(backToBoundary);
        if (segment.payload() != null) {
            this.applySplit(entity, segment.payload(), impact);
        }
        if (segment.beam() != null) {
            this.fireBeam(entity, segment.beam(), impact);
        }
        return backToBoundary;
    }

    private void activateDueSegments(BaseDanmakuEntity entity, DanmakuMotion motion, List<MotionSegment> segments) {
        while (this.processedIndex + 1 < segments.size() && segments.get(this.processedIndex + 1).at() <= this.age) {
            MotionSegment segment = segments.get(++this.processedIndex);
            if (segment.isOneShot()) {
                this.applyOneShot(entity, segments, segment);
                if (entity.isRemoved()) {
                    return;
                }
            } else {
                this.activeIndex = this.processedIndex;
                this.activate(entity, motion, segment);
            }
        }
    }

    private void applyOneShot(BaseDanmakuEntity entity, List<MotionSegment> segments, MotionSegment segment) {
        if (segment.is(MotionSegment.SET_SPEED)) {
            this.speed = Mth.clamp(segment.value(), 0.0f, (float) MAX_SPEED);
            this.speedAssignedThisTick = true;
        } else if (segment.is(MotionSegment.TURN)) {
            Vec3 turnedDirection = rotate(this.direction, segment.axis(), segment.value());
            this.direction = turnedDirection;
            if (this.activeIndex >= 0) {
                MotionSegment active = segments.get(this.activeIndex);
                if (active.is(MotionSegment.ROTATE)) {
                    float accumulatedDegrees = active.value() * this.segmentAge;
                    this.capturedDirection = rotate(turnedDirection, active.axis(), -accumulatedDegrees);
                }
            }
        } else if (segment.is(MotionSegment.SPLIT)) {
            this.applySplit(entity, segment.payload(), entity.position());
        } else if (segment.is(MotionSegment.TRANSFORM)) {
            this.applyTransform(entity, segment);
        } else if (segment.is(MotionSegment.FIRE_BEAM) && segment.beam() != null) {
            this.fireBeam(entity, segment.beam(), entity.position());
        }
    }

    /**
     * SPLIT：仅服务端执行。以当前方向为基准按 payload 扇形展开新子弹，
     * owner 沿用原弹 owner；本弹自身运动不受影响，双端确定性保持。
     */
    private void applySplit(BaseDanmakuEntity entity, @Nullable MotionPayload payload, Vec3 position) {
        if (payload == null || !(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        int count = Math.max(1, payload.count());
        Vec3 base = this.direction;
        double horizontal = Math.sqrt(base.x * base.x + base.z * base.z);
        float basePitch = (float) Math.toDegrees(Math.atan2(-base.y, Math.max(horizontal, 1.0E-8D)));
        float baseYaw = DanmakuMath.yawOf(base);
        float speed = payload.speed() > 0f ? payload.speed() : (float) this.speed;
        float spread = payload.spread();
        float step = count > 1 ? spread / (count - 1) : 0f;
        float first = baseYaw - spread / 2.0f;
        Identifier typeId = payload.type();
        if (typeId == null) {
            DanmakuType currentType = DanmakuTypes.fromItem(entity.getItemStack());
            typeId = currentType == null ? null : currentType.getId();
        }
        Entity owner = entity.getOwner();
        for (int i = 0; i < count; i++) {
            float yaw = count > 1 ? first + step * i : baseYaw;
            var child = DanmakuSpawner.spawn(serverLevel, owner, position, basePitch, yaw,
                    typeId, payload.color(), payload.scale(), speed, payload.lifetime());
            if (entity.getSpellRuntime() != null) {
                entity.getSpellRuntime().track(child);
            }
        }
    }

    private void fireBeam(BaseDanmakuEntity source, MovingLaserSpec spec, Vec3 position) {
        if (!(source.level() instanceof ServerLevel world)) {
            return;
        }
        var beam = DanmakuSpawner.spawnMovingBeam(world, source.getOwner(), position, this.direction, spec);
        if (beam == null) {
            return;
        }
        if (source.getSpellRuntime() != null) {
            source.getSpellRuntime().track(beam);
        }
        if (spec.consumeSource()) {
            source.discard();
        }
    }

    /**
     * TRANSFORM：仅服务端执行。通过 {@link BaseDanmakuEntity#transformBullet} 换物品与属性，
     * 客户端靠既有物品/属性同步自动生效。
     */
    private void applyTransform(BaseDanmakuEntity entity, MotionSegment segment) {
        MotionPayload payload = segment.payload();
        if (payload == null || payload.type() == null || entity.level().isClientSide()) {
            return;
        }
        entity.transformBullet(payload.type(), payload.color(), payload.scale());
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

    /**
     * 回到未初始化状态；改写 motion（REDIRECT 等）后调用，使双端从新初值重新确定性推进。
     */
    public void reset() {
        this.initialized = false;
        this.direction = Vec3.ZERO;
        this.speed = 0.0D;
        this.age = -1;
        this.activeIndex = -1;
        this.processedIndex = -1;
        this.segmentAge = 0;
        this.speedAssignedThisTick = false;
        this.capturedPosition = Vec3.ZERO;
        this.capturedDirection = Vec3.ZERO;
        this.orbitCenter = Vec3.ZERO;
        this.serverFreezeUntilTick = 0;
    }

    /**
     * 当前推进方向（服务端供图案操作读取）。
     */
    public Vec3 currentDirection() {
        return this.direction;
    }

    /**
     * 当前速度标量（格/tick，服务端供图案操作读取）。
     */
    public double currentSpeed() {
        return this.speed;
    }

    /**
     * 当前步进 tick（自出膛起，服务端供图案操作计算冻结截止）。
     */
    public int currentAge() {
        return this.age;
    }

    /**
     * 服务端图案操作：冻结到相对 age 的绝对 tick。
     */
    public void freezeUntilTick(int tick) {
        this.serverFreezeUntilTick = Math.max(this.serverFreezeUntilTick, tick);
    }

    public boolean isFrozen(int age) {
        return this.serverFreezeUntilTick > age;
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
