package cc.thonly.reverie_dreams.data.danmaku.stg;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * A finite beam fired from a projectile. All dimensions are in blocks; speed is blocks per tick.
 */
public record MovingLaserSpec(float speed, float initialLength, float length, float width, int duration, int color,
                              boolean consumeSource) {
    public static final Codec<MovingLaserSpec> CODEC = RecordCodecBuilder.<MovingLaserSpec>create(instance -> instance.group(
            Codec.floatRange(0.01f, 4f).fieldOf("speed").forGetter(MovingLaserSpec::speed),
            Codec.floatRange(0.1f, 128f).fieldOf("initial_length").forGetter(MovingLaserSpec::initialLength),
            Codec.floatRange(0.1f, 128f).fieldOf("length").forGetter(MovingLaserSpec::length),
            Codec.floatRange(0.05f, 16f).fieldOf("width").forGetter(MovingLaserSpec::width),
            Codec.intRange(1, 72000).fieldOf("duration").forGetter(MovingLaserSpec::duration),
            Codec.intRange(-1, 0xFFFFFF).optionalFieldOf("color", -1).forGetter(MovingLaserSpec::color),
            Codec.BOOL.optionalFieldOf("consume_source", false).forGetter(MovingLaserSpec::consumeSource)
    ).apply(instance, MovingLaserSpec::new)).validate(spec -> spec.initialLength <= spec.length
            ? DataResult.success(spec)
            : DataResult.error(() -> "Moving laser starts longer than its maximum length"));

    /**
     * While the beam grows, its tip keeps advancing at the same speed.
     */
    public float nextLength(float currentLength) {
        return Math.min(length, currentLength + speed);
    }

    public float tailAdvance(float currentLength, float nextLength) {
        return Math.max(0f, speed - (nextLength - currentLength));
    }
}
