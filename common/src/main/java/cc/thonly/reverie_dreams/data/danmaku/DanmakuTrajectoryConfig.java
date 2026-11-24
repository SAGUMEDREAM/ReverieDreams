package cc.thonly.reverie_dreams.data.danmaku;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;

/**
 * Per-item trajectory parameters. The trajectory registry still decides which
 * behavior runs; this record only stores the numbers that behavior needs.
 *
 * <p>All numeric fields use {@code -1} as "use the built-in default". This keeps
 * old items created before this component existed fully compatible.</p>
 */
public record DanmakuTrajectoryConfig(
        int count,
        float spread,
        float radius,
        int layers,
        float spacing,
        float forward,
        int faceOutward
) {
    public static final int UNSET_INT = -1;
    public static final float UNSET_FLOAT = -1.0f;
    public static final int FACE_DEFAULT = -1;
    public static final int FACE_FALSE = 0;
    public static final int FACE_TRUE = 1;

    public static final DanmakuTrajectoryConfig EMPTY = new DanmakuTrajectoryConfig(
            UNSET_INT, UNSET_FLOAT, UNSET_FLOAT, UNSET_INT, UNSET_FLOAT, UNSET_FLOAT, FACE_DEFAULT
    );

    public static final Codec<DanmakuTrajectoryConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("count", UNSET_INT).forGetter(DanmakuTrajectoryConfig::count),
            Codec.FLOAT.optionalFieldOf("spread", UNSET_FLOAT).forGetter(DanmakuTrajectoryConfig::spread),
            Codec.FLOAT.optionalFieldOf("radius", UNSET_FLOAT).forGetter(DanmakuTrajectoryConfig::radius),
            Codec.INT.optionalFieldOf("layers", UNSET_INT).forGetter(DanmakuTrajectoryConfig::layers),
            Codec.FLOAT.optionalFieldOf("spacing", UNSET_FLOAT).forGetter(DanmakuTrajectoryConfig::spacing),
            Codec.FLOAT.optionalFieldOf("forward", UNSET_FLOAT).forGetter(DanmakuTrajectoryConfig::forward),
            Codec.INT.optionalFieldOf("face_outward", FACE_DEFAULT).forGetter(DanmakuTrajectoryConfig::faceOutward)
    ).apply(instance, DanmakuTrajectoryConfig::new));

    public boolean isEmpty() {
        return this.count < 0
                && this.spread < 0.0f
                && this.radius < 0.0f
                && this.layers < 0
                && this.spacing < 0.0f
                && this.forward < 0.0f
                && this.faceOutward < 0;
    }

    public int countOr(int fallback) {
        return this.count > 0 ? Mth.clamp(this.count, 1, 64) : fallback;
    }

    public float spreadOr(float fallback) {
        return this.spread >= 0.0f ? Mth.clamp(this.spread, 0.0f, 360.0f) : fallback;
    }

    public float radiusOr(float fallback) {
        return this.radius >= 0.0f ? Mth.clamp(this.radius, 0.0f, 16.0f) : fallback;
    }

    public int layersOr(int fallback) {
        return this.layers >= 0 ? Mth.clamp(this.layers, 0, 8) : fallback;
    }

    public float spacingOr(float fallback) {
        return this.spacing >= 0.0f ? Mth.clamp(this.spacing, 0.0f, 4.0f) : fallback;
    }

    public float forwardOr(float fallback) {
        return this.forward >= 0.0f ? Mth.clamp(this.forward, 0.0f, 16.0f) : fallback;
    }

    public boolean faceOutwardOr(boolean fallback) {
        return this.faceOutward < 0 ? fallback : this.faceOutward != FACE_FALSE;
    }
}
