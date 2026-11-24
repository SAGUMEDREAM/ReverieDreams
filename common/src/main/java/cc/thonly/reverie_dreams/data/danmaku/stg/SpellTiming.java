package cc.thonly.reverie_dreams.data.danmaku.stg;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Stage deadline and the portion of the attack timeline that repeats after its intro.
 */
public record SpellTiming(int duration, int loopStart, int loopLength) {
    public static final SpellTiming UNTIMED = new SpellTiming(0, 0, 0);
    public static final Codec<SpellTiming> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("duration").forGetter(SpellTiming::duration),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("loop_start", 0).forGetter(SpellTiming::loopStart),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("loop_length", 0).forGetter(SpellTiming::loopLength)
    ).apply(instance, SpellTiming::new));

    public SpellTiming {
        if (duration < 0 || loopStart < 0 || loopLength < 0
                || (loopLength > 0 && (long) loopStart + loopLength > duration)) {
            throw new IllegalArgumentException("Invalid spell timeline");
        }
    }

    public static int framesToTicks(int frames) {
        return Math.ceilDiv(frames, 3);
    }

    public boolean isTimed() {
        return this.duration > 0;
    }

    public boolean hasExpired(int elapsed) {
        return this.isTimed() && elapsed >= this.duration;
    }

    public int attackTick(int elapsed) {
        if (this.loopLength == 0 || elapsed < this.loopStart) {
            return elapsed;
        }
        return this.loopStart + (elapsed - this.loopStart) % this.loopLength;
    }
}
