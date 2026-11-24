package cc.thonly.reverie_dreams.data.danmaku.stg;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

/**
 * A beam cue. Direction and cast-relative offset are sampled on its start tick.
 */
public record DanmakuLaser(int at, float pitch, float yaw, float length, float width,
                           int warnTicks, int activeTicks, float sweepPerTick, int color,
                           Vec3 offset, float sweepHalfAngle) {
    public static final Codec<DanmakuLaser> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, 72000).fieldOf("at").forGetter(DanmakuLaser::at),
            Codec.FLOAT.optionalFieldOf("pitch", 0f).forGetter(DanmakuLaser::pitch),
            Codec.FLOAT.fieldOf("yaw").forGetter(DanmakuLaser::yaw),
            Codec.floatRange(0.1f, 128f).fieldOf("length").forGetter(DanmakuLaser::length),
            Codec.floatRange(0.05f, 16f).fieldOf("width").forGetter(DanmakuLaser::width),
            Codec.intRange(1, 72000).fieldOf("warn_ticks").forGetter(DanmakuLaser::warnTicks),
            Codec.intRange(1, 72000).fieldOf("active_ticks").forGetter(DanmakuLaser::activeTicks),
            Codec.FLOAT.optionalFieldOf("sweep_per_tick", 0f).forGetter(DanmakuLaser::sweepPerTick),
            Codec.intRange(-1, 0xFFFFFF).optionalFieldOf("color", -1).forGetter(DanmakuLaser::color),
            Vec3.CODEC.optionalFieldOf("offset", Vec3.ZERO).forGetter(DanmakuLaser::offset),
            Codec.floatRange(0f, 180f).optionalFieldOf("sweep_half_angle", 0f).forGetter(DanmakuLaser::sweepHalfAngle)
    ).apply(instance, DanmakuLaser::new));

    public DanmakuLaser(int at, float pitch, float yaw, float length, float width,
                        int warnTicks, int activeTicks, float sweepPerTick, int color) {
        this(at, pitch, yaw, length, width, warnTicks, activeTicks, sweepPerTick, color, Vec3.ZERO, 0f);
    }

    public DanmakuLaser(int at, float pitch, float yaw, float length, float width,
                        int warnTicks, int activeTicks, float sweepPerTick) {
        this(at, pitch, yaw, length, width, warnTicks, activeTicks, sweepPerTick, -1);
    }

    public DanmakuLaser withYaw(float yaw) {
        return this.withDirection(this.pitch, yaw);
    }

    public DanmakuLaser withDirection(float pitch, float yaw) {
        return new DanmakuLaser(this.at, pitch, yaw, this.length, this.width,
                this.warnTicks, this.activeTicks, this.sweepPerTick, this.color, this.offset, this.sweepHalfAngle);
    }

    public DanmakuLaser withOffset(Vec3 offset) {
        return new DanmakuLaser(this.at, this.pitch, this.yaw, this.length, this.width,
                this.warnTicks, this.activeTicks, this.sweepPerTick, this.color, offset, this.sweepHalfAngle);
    }

    /**
     * Reflect the sweep at yaw ± halfAngle. Zero keeps the legacy continuous rotation.
     */
    public DanmakuLaser withSweepLimit(float halfAngle) {
        return new DanmakuLaser(this.at, this.pitch, this.yaw, this.length, this.width,
                this.warnTicks, this.activeTicks, this.sweepPerTick, this.color, this.offset, halfAngle);
    }

    public static float sweepOffset(float degreesPerTick, float halfAngle, int activeAge) {
        if (halfAngle <= 0f) {
            return degreesPerTick * activeAge;
        }
        double period = 4.0 * halfAngle;
        double phase = (Math.abs((double) degreesPerTick) * activeAge + halfAngle) % period;
        double offset = (phase <= 2.0 * halfAngle ? phase : period - phase) - halfAngle;
        return (float) Math.copySign(1.0, degreesPerTick) * (float) offset;
    }
}
