package cc.thonly.reverie_dreams.data.danmaku.stg;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Uniform horizontal scatter: one position per volley, independent angle and speed per shot.
 */
public record DanmakuScatter(float width, float depth, float yawHalfRange, float minSpeedScale) {
    public static final DanmakuScatter NONE = new DanmakuScatter(0, 0, 0, 1);
    public static final Codec<DanmakuScatter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.floatRange(0, 128).optionalFieldOf("width", 0f).forGetter(DanmakuScatter::width),
            Codec.floatRange(0, 128).optionalFieldOf("depth", 0f).forGetter(DanmakuScatter::depth),
            Codec.floatRange(0, 180).optionalFieldOf("yaw_half_range", 0f).forGetter(DanmakuScatter::yawHalfRange),
            Codec.floatRange(0, 1).optionalFieldOf("min_speed_scale", 1f).forGetter(DanmakuScatter::minSpeedScale)
    ).apply(instance, DanmakuScatter::new));
}
