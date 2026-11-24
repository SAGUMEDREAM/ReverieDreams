package cc.thonly.reverie_dreams.data.danmaku.stg;

import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeRange;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

/**
 * STG 发射器：弹幕的“枪口”。
 *
 * <p>发射器由五部分组成：
 * <ol>
 *     <li><b>计时</b>：{@code delay / duration / interval} 控制开火窗口与频率；</li>
 *     <li><b>位置</b>：锚点 {@link EmitAnchor} + 相对锚点的偏移 {@code offset}，
 *         可通过 {@code rotation} 关键帧函数绕锚点旋转（螺旋/旋转弹幕的核心）；</li>
 *     <li><b>朝向</b>：由 {@link EmitAim} 与 {@code pitch/yaw、pitch_range、yaw_range} 决定；</li>
 *     <li><b>散布</b>：{@code jitter}（度）在出膛瞬间对每发施加随机角度抖动（仅影响生成时刻的初始方向）；</li>
 *     <li><b>射击</b>：{@link EmitShape 形状} + {@link DanmakuBulletSpec 子弹规格}。</li>
 * </ol>
 *
 * <p>{@code pitch_range / yaw_range} 使用关键帧函数（linear/quadratic/sine/cosine/triangle/saw），
 * 输入为发射器生命周期的进度 [0,1]，输出为角度偏移：
 * 匀速旋转用 linear、匀加速旋转用 quadratic、“波与粒子的境界”用 sine。
 */
public record DanmakuEmitter(
        int delay,
        int duration,
        int interval,
        EmitAim aim,
        boolean attached,
        Vec3 offset,
        KeyframeRange rotation,
        float pitch,
        float yaw,
        KeyframeRange pitchRange,
        KeyframeRange yawRange,
        EmitShape shape,
        DanmakuBulletSpec bullet,
        EmitAnchor anchor,
        float jitter,
        DanmakuScatter scatter
) {
    public static final Codec<DanmakuEmitter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("delay", 0).forGetter(DanmakuEmitter::delay),
            Codec.INT.optionalFieldOf("duration", 100).forGetter(DanmakuEmitter::duration),
            Codec.INT.optionalFieldOf("interval", 10).forGetter(DanmakuEmitter::interval),
            EmitAim.CODEC.optionalFieldOf("aim", EmitAim.TARGET).forGetter(DanmakuEmitter::aim),
            Codec.BOOL.optionalFieldOf("attached", true).forGetter(DanmakuEmitter::attached),
            Vec3.CODEC.optionalFieldOf("offset", Vec3.ZERO).forGetter(DanmakuEmitter::offset),
            KeyframeRange.CODEC.optionalFieldOf("rotation", KeyframeRange.empty(0f, 0f)).forGetter(DanmakuEmitter::rotation),
            Codec.FLOAT.optionalFieldOf("pitch", 0f).forGetter(DanmakuEmitter::pitch),
            Codec.FLOAT.optionalFieldOf("yaw", 0f).forGetter(DanmakuEmitter::yaw),
            KeyframeRange.CODEC.optionalFieldOf("pitch_range", KeyframeRange.empty(0f, 0f)).forGetter(DanmakuEmitter::pitchRange),
            KeyframeRange.CODEC.optionalFieldOf("yaw_range", KeyframeRange.empty(0f, 0f)).forGetter(DanmakuEmitter::yawRange),
            EmitShape.CODEC.optionalFieldOf("shape", EmitShape.aim()).forGetter(DanmakuEmitter::shape),
            DanmakuBulletSpec.CODEC.optionalFieldOf("bullet", DanmakuBulletSpec.of(DanmakuDefaults.DEFAULT_TYPE)).forGetter(DanmakuEmitter::bullet),
            EmitAnchor.CODEC.optionalFieldOf("anchor", EmitAnchor.SOURCE).forGetter(DanmakuEmitter::anchor),
            Codec.FLOAT.optionalFieldOf("jitter", 0f).forGetter(DanmakuEmitter::jitter),
            DanmakuScatter.CODEC.optionalFieldOf("scatter", DanmakuScatter.NONE).forGetter(DanmakuEmitter::scatter)
    ).apply(instance, DanmakuEmitter::new));

    /**
     * 旧参数顺序的兼容构造器（默认锚点 SOURCE、无抖动）。
     */
    public DanmakuEmitter(int delay, int duration, int interval, EmitAim aim, boolean attached, Vec3 offset,
                          KeyframeRange rotation, float pitch, float yaw,
                          KeyframeRange pitchRange, KeyframeRange yawRange,
                          EmitShape shape, DanmakuBulletSpec bullet) {
        this(delay, duration, interval, aim, attached, offset, rotation, pitch, yaw,
                pitchRange, yawRange, shape, bullet, EmitAnchor.SOURCE, 0f);
    }

    public DanmakuEmitter(int delay, int duration, int interval, EmitAim aim, boolean attached, Vec3 offset,
                          KeyframeRange rotation, float pitch, float yaw,
                          KeyframeRange pitchRange, KeyframeRange yawRange,
                          EmitShape shape, DanmakuBulletSpec bullet, EmitAnchor anchor, float jitter) {
        this(delay, duration, interval, aim, attached, offset, rotation, pitch, yaw,
                pitchRange, yawRange, shape, bullet, anchor, jitter, DanmakuScatter.NONE);
    }

    public DanmakuEmitter withScatter(DanmakuScatter scatter) {
        return new DanmakuEmitter(this.delay, this.duration, this.interval, this.aim, this.attached, this.offset,
                this.rotation, this.pitch, this.yaw, this.pitchRange, this.yawRange, this.shape, this.bullet,
                this.anchor, this.jitter, scatter);
    }

    public static DanmakuEmitter of(EmitShape shape, DanmakuBulletSpec bullet) {
        return new DanmakuEmitter(0, 100, 10, EmitAim.TARGET, true, Vec3.ZERO,
                KeyframeRange.empty(0f, 0f), 0f, 0f,
                KeyframeRange.empty(0f, 0f), KeyframeRange.empty(0f, 0f),
                shape, bullet);
    }

    public DanmakuEmitter withTiming(int delayTicks, int durationTicks, int intervalTicks) {
        return new DanmakuEmitter(delayTicks, durationTicks, intervalTicks, this.aim, this.attached, this.offset,
                this.rotation, this.pitch, this.yaw, this.pitchRange, this.yawRange, this.shape, this.bullet,
                this.anchor, this.jitter, this.scatter);
    }

    public DanmakuEmitter withAim(EmitAim aim, float pitchDegrees, float yawDegrees) {
        return new DanmakuEmitter(this.delay, this.duration, this.interval, aim, this.attached, this.offset,
                this.rotation, pitchDegrees, yawDegrees, this.pitchRange, this.yawRange, this.shape, this.bullet,
                this.anchor, this.jitter, this.scatter);
    }

    public DanmakuEmitter withOffset(Vec3 offset) {
        return new DanmakuEmitter(this.delay, this.duration, this.interval, this.aim, this.attached, offset,
                this.rotation, this.pitch, this.yaw, this.pitchRange, this.yawRange, this.shape, this.bullet,
                this.anchor, this.jitter, this.scatter);
    }

    public DanmakuEmitter withAttached(boolean attached) {
        return new DanmakuEmitter(this.delay, this.duration, this.interval, this.aim, attached, this.offset,
                this.rotation, this.pitch, this.yaw, this.pitchRange, this.yawRange, this.shape, this.bullet,
                this.anchor, this.jitter, this.scatter);
    }

    public DanmakuEmitter withRotation(KeyframeRange rotation) {
        return new DanmakuEmitter(this.delay, this.duration, this.interval, this.aim, this.attached, this.offset,
                rotation, this.pitch, this.yaw, this.pitchRange, this.yawRange, this.shape, this.bullet,
                this.anchor, this.jitter, this.scatter);
    }

    public DanmakuEmitter withRanges(KeyframeRange pitchRange, KeyframeRange yawRange) {
        return new DanmakuEmitter(this.delay, this.duration, this.interval, this.aim, this.attached, this.offset,
                this.rotation, this.pitch, this.yaw, pitchRange, yawRange, this.shape, this.bullet,
                this.anchor, this.jitter, this.scatter);
    }

    public DanmakuEmitter withAnchor(EmitAnchor anchor) {
        return new DanmakuEmitter(this.delay, this.duration, this.interval, this.aim, this.attached, this.offset,
                this.rotation, this.pitch, this.yaw, this.pitchRange, this.yawRange, this.shape, this.bullet,
                anchor, this.jitter, this.scatter);
    }

    public DanmakuEmitter withJitter(float jitterDegrees) {
        return new DanmakuEmitter(this.delay, this.duration, this.interval, this.aim, this.attached, this.offset,
                this.rotation, this.pitch, this.yaw, this.pitchRange, this.yawRange, this.shape, this.bullet,
                this.anchor, jitterDegrees, this.scatter);
    }
}
