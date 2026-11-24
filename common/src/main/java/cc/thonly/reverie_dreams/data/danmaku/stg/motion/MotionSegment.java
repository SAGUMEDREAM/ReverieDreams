package cc.thonly.reverie_dreams.data.danmaku.stg.motion;

import cc.thonly.reverie_dreams.data.danmaku.stg.MovingLaserSpec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * 子弹运动时间线上的一个行为段（STG 弹幕引擎）。
 *
 * <p>子弹可以在飞行过程中按 tick 切换行为：
 * <ul>
 *     <li>{@link #ROTATE} —— 从 at 起以 value 度/tick 围绕 axis 旋转飞行方向（花型弹幕“出膛后转向”）</li>
 *     <li>{@link #ORBIT} —— 从 at 起围绕圆心以 value 度/tick 公转（花型/绕点弹幕）；
 *         圆心由 {@code center_mode} 决定：{@code self} 为“激活瞬间自身位置 + center”，
 *         {@code origin} 为“子弹出膛点 + center”（文档中绕 circle_O 旋转的花型弹幕）</li>
 *     <li>{@link #ACCELERATE} —— 从 at 起每 tick 沿当前方向额外加速 value 格/tick</li>
 *     <li>{@link #ACCELERATE_VECTOR} —— 从 at 起按固定世界加速度向量 axis 更新速度，持续 duration tick</li>
 *     <li>{@link #HOMING} —— 从 at 起以 value 度/tick 向目标转向，最多持续 duration tick（-1 表示无限）</li>
 *     <li>{@link #LINEAR} —— 从 at 起沿用当前方向，不应用前一个 ACCELERATE 段</li>
 *     <li>{@link #SET_SPEED} —— 到 at 时一次性把速度标量设为 value；0 表示停止但保留方向</li>
 *     <li>{@link #TURN} —— 到 at 时一次性围绕 axis 把当前方向旋转 value 度；不进行隐式瞄准</li>
 *     <li>{@link #FREEZE} —— 从 at 起冻结 duration tick：位移为 0、不累积加速度，结束后自然恢复原速原向</li>
 *     <li>{@link #BOUNCE} —— 从 at 起若与 motion.origin 的水平距离超过 value（半径格数），
 *         沿圆边界反射“径向水平分量”并回到边界内侧</li>
 *     <li>{@link #SPLIT} —— 到 at 时按 payload 从当前位置扇形展开新子弹（仅服务端生成实体）</li>
 *     <li>{@link #TRANSFORM} —— 到 at 时按 payload 把当前子弹变形为新类型（仅服务端执行）</li>
 * </ul>
 *
 * <p>行为段按 {@link #at()} 升序排列。同帧段按稳定排序后的列表顺序处理：
 * 一次性段立即生效，连续段中最后一个成为该帧后的持续行为；
 * {@link #SET_SPEED} 的激活帧赋值优先于该帧的加速度累加。
 * 未激活任何行为段时子弹沿初始方向匀速飞行。
 */
public record MotionSegment(int at, String type, float value, Vec3 axis, Vec3 center, String centerMode, int duration,
                            @Nullable MotionPayload payload, @Nullable MovingLaserSpec beam) {
    public static final String ROTATE = "rotate";
    public static final String LATERAL_WAVE = "lateral_wave";
    public static final String ORBIT = "orbit";
    public static final String ACCELERATE = "accelerate";
    public static final String ACCELERATE_VECTOR = "accelerate_vector";
    public static final String HOMING = "homing";
    public static final String LINEAR = "linear";
    public static final String SET_SPEED = "set_speed";
    public static final String TURN = "turn";
    public static final String FREEZE = "freeze";
    public static final String BOUNCE = "bounce";
    public static final String SPLIT = "split";
    public static final String TRANSFORM = "transform";
    public static final String FIRE_BEAM = "fire_beam";

    public static final String CENTER_SELF = "self";
    public static final String CENTER_ORIGIN = "origin";

    public static final Codec<MotionSegment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("at", 0).forGetter(MotionSegment::at),
            Codec.STRING.optionalFieldOf("type", ROTATE).forGetter(MotionSegment::type),
            Codec.FLOAT.optionalFieldOf("value", 0f).forGetter(MotionSegment::value),
            Vec3.CODEC.optionalFieldOf("axis", new Vec3(0, 1, 0)).forGetter(MotionSegment::axis),
            Vec3.CODEC.optionalFieldOf("center", Vec3.ZERO).forGetter(MotionSegment::center),
            Codec.STRING.optionalFieldOf("center_mode", CENTER_SELF).forGetter(MotionSegment::centerMode),
            Codec.INT.optionalFieldOf("duration", -1).forGetter(MotionSegment::duration),
            // DFU 组合字段时必须保留 Optional，合并完成后才可转成 null。
            MotionPayload.CODEC.optionalFieldOf("payload").forGetter(segment -> Optional.ofNullable(segment.payload())),
            MovingLaserSpec.CODEC.optionalFieldOf("beam").forGetter(segment -> Optional.ofNullable(segment.beam()))
    ).apply(instance, (at, type, value, axis, center, centerMode, duration, payload, beam) ->
            new MotionSegment(at, type, value, axis, center, centerMode, duration, payload.orElse(null), beam.orElse(null))));

    public MotionSegment(int at, String type, float value, Vec3 axis, Vec3 center, String centerMode, int duration,
                         @Nullable MotionPayload payload) {
        this(at, type, value, axis, center, centerMode, duration, payload, null);
    }

    /**
     * 旧参数顺序的兼容构造器（无副作用参数）。
     */
    public MotionSegment(int at, String type, float value, Vec3 axis, Vec3 center, String centerMode, int duration) {
        this(at, type, value, axis, center, centerMode, duration, null);
    }

    public static MotionSegment rotate(int at, float degreesPerTick) {
        return new MotionSegment(at, ROTATE, degreesPerTick, new Vec3(0, 1, 0), Vec3.ZERO, CENTER_SELF, -1);
    }

    public static MotionSegment rotate(int at, float degreesPerTick, Vec3 axis) {
        return new MotionSegment(at, ROTATE, degreesPerTick, axis, Vec3.ZERO, CENTER_SELF, -1);
    }

    public static MotionSegment lateralWave(Vec3 side, Vec3 up, float radius) {
        return new MotionSegment(0, LATERAL_WAVE, radius, side, up, CENTER_SELF, -1);
    }

    /**
     * 绕“激活瞬间自身位置 + centerOffset”公转。
     */
    public static MotionSegment orbit(int at, float degreesPerTick, Vec3 centerOffset) {
        return new MotionSegment(at, ORBIT, degreesPerTick, new Vec3(0, 1, 0), centerOffset, CENTER_SELF, -1);
    }

    /**
     * 绕“子弹出膛点 + centerOffset”公转（花型弹幕：子弹飞行一段时间后开始绕轴心旋转）。
     */
    public static MotionSegment orbitAroundOrigin(int at, float degreesPerTick, Vec3 centerOffset) {
        return new MotionSegment(at, ORBIT, degreesPerTick, new Vec3(0, 1, 0), centerOffset, CENTER_ORIGIN, -1);
    }

    public static MotionSegment accelerate(int at, float acceleration) {
        return new MotionSegment(at, ACCELERATE, acceleration, new Vec3(0, 1, 0), Vec3.ZERO, CENTER_SELF, -1);
    }

    /**
     * The axis field stores the acceleration vector itself (blocks/tick²), not a unit axis.
     */
    public static MotionSegment accelerateVector(int at, Vec3 acceleration, int duration) {
        return new MotionSegment(at, ACCELERATE_VECTOR, 0f, acceleration, Vec3.ZERO, CENTER_SELF, duration);
    }

    public static MotionSegment homing(int at, float turnRateDegrees, int duration) {
        return new MotionSegment(at, HOMING, turnRateDegrees, new Vec3(0, 1, 0), Vec3.ZERO, CENTER_SELF, duration);
    }

    public static MotionSegment linear(int at) {
        return new MotionSegment(at, LINEAR, 0f, new Vec3(0, 1, 0), Vec3.ZERO, CENTER_SELF, -1);
    }

    public static MotionSegment setSpeed(int at, float speed) {
        return new MotionSegment(at, SET_SPEED, speed, new Vec3(0, 1, 0), Vec3.ZERO, CENTER_SELF, -1);
    }

    public static MotionSegment turn(int at, float degrees, Vec3 axis) {
        return new MotionSegment(at, TURN, degrees, axis, Vec3.ZERO, CENTER_SELF, -1);
    }

    /**
     * 持续段：激活时冻结 duration tick，位移为 0 且不累积加速度，过后自然恢复原速原向。
     */
    public static MotionSegment freeze(int at, int duration) {
        return new MotionSegment(at, FREEZE, 0f, new Vec3(0, 1, 0), Vec3.ZERO, CENTER_SELF, Math.max(0, duration));
    }

    /**
     * 持续段：以子弹出膛点（motion.origin）为圆心、value 为半径的水平圆边界反射。
     */
    public static MotionSegment bounce(int at, float radius) {
        return new MotionSegment(at, BOUNCE, radius, new Vec3(0, 1, 0), Vec3.ZERO, CENTER_SELF, -1);
    }

    /**
     * Release bullets at each real boundary impact, using the reflected heading.
     */
    public static MotionSegment bounceWithSplit(int at, float radius, MotionPayload impact) {
        return new MotionSegment(at, BOUNCE, radius, new Vec3(0, 1, 0), Vec3.ZERO, CENTER_SELF, -1, impact);
    }

    public static MotionSegment bounceWithBeam(int at, float radius, MovingLaserSpec beam) {
        return new MotionSegment(at, BOUNCE, radius, new Vec3(0, 1, 0), Vec3.ZERO, CENTER_SELF, -1, null, beam);
    }

    /**
     * Once at the scheduled age, launch a flying finite beam using the current heading.
     */
    public static MotionSegment fireBeam(int at, MovingLaserSpec beam) {
        return new MotionSegment(at, FIRE_BEAM, 0f, new Vec3(0, 1, 0), Vec3.ZERO, CENTER_SELF, -1, null, beam);
    }

    /**
     * 一次性段：从当前位置按 payload 扇形展开新子弹（仅服务端生成实体）。
     */
    public static MotionSegment split(int at, MotionPayload payload) {
        return new MotionSegment(at, SPLIT, 0f, new Vec3(0, 1, 0), Vec3.ZERO, CENTER_SELF, -1, payload);
    }

    /**
     * 一次性段：把当前子弹变形为 payload 描述的新类型（仅服务端执行）。
     */
    public static MotionSegment transform(int at, MotionPayload payload) {
        return new MotionSegment(at, TRANSFORM, 0f, new Vec3(0, 1, 0), Vec3.ZERO, CENTER_SELF, -1, payload);
    }

    public boolean is(String segmentType) {
        return this.type.equalsIgnoreCase(segmentType);
    }

    public boolean isOneShot() {
        return this.is(SET_SPEED) || this.is(TURN) || this.is(SPLIT) || this.is(TRANSFORM) || this.is(FIRE_BEAM);
    }

    public boolean usesOriginCenter() {
        return CENTER_ORIGIN.equalsIgnoreCase(this.centerMode);
    }
}
