package cc.thonly.reverie_dreams.data.danmaku.stg;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

/**
 * One melee box in a source-frame interval [fromFrame, untilFrame), at 60 frames/second.
 * Boxes in the same hitGroup share one hit per target for the current pattern loop.
 * Local coordinates use +Z forward and +Y up, relative to the body's 55% point,
 * normalized to a 1.8-block owner. The runtime applies the owner's current height once.
 */
public record DanmakuStrike(int fromFrame, int untilFrame, int hitGroup, float damage, Vec3 min, Vec3 max) {
    public static final Codec<DanmakuStrike> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, Integer.MAX_VALUE - 1).fieldOf("from_frame").forGetter(DanmakuStrike::fromFrame),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("until_frame").forGetter(DanmakuStrike::untilFrame),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("hit_group").forGetter(DanmakuStrike::hitGroup),
            Codec.floatRange(0, Float.MAX_VALUE).fieldOf("damage").forGetter(DanmakuStrike::damage),
            Vec3.CODEC.fieldOf("min").forGetter(DanmakuStrike::min),
            Vec3.CODEC.fieldOf("max").forGetter(DanmakuStrike::max)
    ).apply(instance, DanmakuStrike::new));

    public DanmakuStrike {
        if (fromFrame < 0 || untilFrame <= fromFrame || hitGroup < 0 || damage < 0 || !Float.isFinite(damage)
                || !finite(min) || !finite(max) || min.x >= max.x || min.y >= max.y || min.z >= max.z) {
            throw new IllegalArgumentException("Invalid melee strike interval or volume");
        }
    }

    public boolean activeAt(int tick) {
        return tick >= 0 && (long) tick * 3 < this.untilFrame && ((long) tick + 1) * 3 > this.fromFrame;
    }

    public OrientedBoxVolume volume(Vec3 origin, float pitch, float yaw, double scale) {
        Vec3 forward = DanmakuMath.direction(pitch, yaw);
        double radians = Math.toRadians(yaw);
        Vec3 right = new Vec3(Math.cos(radians), 0, Math.sin(radians));
        Vec3 up = forward.cross(right);
        Vec3 localCenter = this.min.add(this.max).scale(0.5 * scale);
        Vec3 center = origin.add(right.scale(localCenter.x)).add(up.scale(localCenter.y)).add(forward.scale(localCenter.z));
        return new OrientedBoxVolume(center, this.max.subtract(this.min).scale(0.5 * scale), right, up, forward);
    }

    private static boolean finite(Vec3 point) {
        return point != null && Double.isFinite(point.x) && Double.isFinite(point.y) && Double.isFinite(point.z);
    }
}
