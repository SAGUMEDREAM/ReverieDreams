package cc.thonly.reverie_dreams.data.danmaku.stg.motion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

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
 *     <li>{@link #HOMING} —— 从 at 起以 value 度/tick 向目标转向，最多持续 duration tick（-1 表示无限）</li>
 * </ul>
 *
 * <p>行为段按 {@link #at()} 升序排列；同一时刻生效的是最后一个已激活的行为段。
 * 未激活任何行为段时子弹沿初始方向匀速飞行。
 */
public record MotionSegment(int at, String type, float value, Vec3 axis, Vec3 center, String centerMode, int duration) {
    public static final String ROTATE = "rotate";
    public static final String ORBIT = "orbit";
    public static final String ACCELERATE = "accelerate";
    public static final String HOMING = "homing";

    public static final String CENTER_SELF = "self";
    public static final String CENTER_ORIGIN = "origin";

    public static final Codec<MotionSegment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("at", 0).forGetter(MotionSegment::at),
            Codec.STRING.optionalFieldOf("type", ROTATE).forGetter(MotionSegment::type),
            Codec.FLOAT.optionalFieldOf("value", 0f).forGetter(MotionSegment::value),
            Vec3.CODEC.optionalFieldOf("axis", new Vec3(0, 1, 0)).forGetter(MotionSegment::axis),
            Vec3.CODEC.optionalFieldOf("center", Vec3.ZERO).forGetter(MotionSegment::center),
            Codec.STRING.optionalFieldOf("center_mode", CENTER_SELF).forGetter(MotionSegment::centerMode),
            Codec.INT.optionalFieldOf("duration", -1).forGetter(MotionSegment::duration)
    ).apply(instance, MotionSegment::new));

    public static MotionSegment rotate(int at, float degreesPerTick) {
        return new MotionSegment(at, ROTATE, degreesPerTick, new Vec3(0, 1, 0), Vec3.ZERO, CENTER_SELF, -1);
    }

    public static MotionSegment rotate(int at, float degreesPerTick, Vec3 axis) {
        return new MotionSegment(at, ROTATE, degreesPerTick, axis, Vec3.ZERO, CENTER_SELF, -1);
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

    public static MotionSegment homing(int at, float turnRateDegrees, int duration) {
        return new MotionSegment(at, HOMING, turnRateDegrees, new Vec3(0, 1, 0), Vec3.ZERO, CENTER_SELF, duration);
    }

    public boolean is(String segmentType) {
        return this.type.equalsIgnoreCase(segmentType);
    }

    public boolean usesOriginCenter() {
        return CENTER_ORIGIN.equalsIgnoreCase(this.centerMode);
    }
}
