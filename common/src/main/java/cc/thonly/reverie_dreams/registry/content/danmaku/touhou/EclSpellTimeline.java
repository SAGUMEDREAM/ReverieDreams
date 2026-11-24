package cc.thonly.reverie_dreams.registry.content.danmaku.touhou;

import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeRange;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuLaser;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuScatter;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Authoring helpers for ECL ports. All time inputs here are original 60 Hz frames. */
public final class EclSpellTimeline {
    private final int deadline;
    private final int intro;
    private final int cycle;
    private final List<DanmakuEmitter> emitters = new ArrayList<>();
    private final List<DanmakuLaser> lasers = new ArrayList<>();

    public EclSpellTimeline(int deadline, int intro, int cycle) {
        this.deadline = deadline;
        this.intro = intro;
        this.cycle = cycle;
    }

    public DanmakuPattern build() {
        return new DanmakuPattern(List.copyOf(emitters)).withLasers(List.copyOf(lasers))
                .withTiming(new SpellTiming(ticks(deadline), cueTick(intro), ticks(cycle)));
    }

    public void ring(int frame, DanmakuBulletSpec shot, int count, float yaw) {
        emit(frame, EmitShape.ring(count, 0f, true), shot, yaw, Vec3.ZERO);
    }

    public void ring(int frame, DanmakuBulletSpec shot, int count, float yaw, Vec3 offset) {
        emit(frame, EmitShape.ring(count, 0f, true), shot, yaw, offset);
    }

    /** Spread is the full opening angle, not the spacing between adjacent bullets. */
    public void fan(int frame, DanmakuBulletSpec shot, int count, float spread, float yaw, Vec3 offset) {
        emit(frame, EmitShape.fan(count, spread), shot, yaw, offset);
    }

    public void emit(int frame, EmitShape shape, DanmakuBulletSpec shot, float yaw, Vec3 offset) {
        emitters.add(cue(frame, shape, shot, 0f, yaw, offset));
    }

    public void sweep(int firstFrame, int lastFrame, int stepFrames, EmitShape shape, DanmakuBulletSpec shot,
                      float fromYaw, float toYaw, Vec3 offset) {
        requireFrame(lastFrame);
        emitters.add(cue(firstFrame, shape, shot, 0f, fromYaw, offset)
                .withTiming(cueTick(firstFrame), cueTick(lastFrame) - cueTick(firstFrame), Math.max(1, ticks(stepFrames)))
                .withRanges(KeyframeRange.empty(0f, 0f), KeyframeRange.empty(fromYaw, toYaw)));
    }

    public void rain(int frame, DanmakuBulletSpec shot, int count, float pitch, float yaw, Vec3 offset,
                     float width, float depth, float jitter) {
        emitters.add(cue(frame, EmitShape.fan(count, 0f), shot, pitch, yaw, offset)
                .withScatter(new DanmakuScatter(width, depth, jitter, 0.82f)));
    }

    public void beam(int frame, int warning, int active, float lengthPx, float widthPx, float yaw,
                     float rotationPerFrame, float sweepLimit, int color, Vec3 offset) {
        requireFrame(frame);
        lasers.add(new DanmakuLaser(cueTick(frame), 0f, yaw, lengthPx / 30f, widthPx / 30f,
                ticks(warning), ticks(active), rotationPerFrame * 3f, color)
                .withOffset(offset).withSweepLimit(sweepLimit));
    }

    private DanmakuEmitter cue(int frame, EmitShape shape, DanmakuBulletSpec shot, float pitch, float yaw, Vec3 offset) {
        requireFrame(frame);
        return DanmakuEmitter.of(shape, shot).withTiming(cueTick(frame), 0, 1)
                .withAim(EmitAim.TARGET_OR_SOURCE_FACING, 0, 0).withOffset(offset)
                .withRanges(KeyframeRange.empty(pitch, pitch), KeyframeRange.empty(yaw, yaw));
    }

    private void requireFrame(int frame) {
        int end = cycle == 0 ? deadline : Math.min(deadline, intro + cycle);
        if (frame < 0 || frame >= end) {
            throw new IllegalArgumentException("ECL cue " + frame + " outside timeline ending at frame " + end);
        }
    }

    // A cue in the last three frames of a loop belongs to the last tick, not the next loop.
    private static int cueTick(int frames) {
        return frames / 3;
    }

    public static int ticks(int frames) {
        return SpellTiming.framesToTicks(frames);
    }

    public static MotionSegment speed(int frame, float pixelsPerFrame) {
        return MotionSegment.setSpeed(ticks(frame), pixelsPerFrame / 10f);
    }

    public static MotionSegment curve(int frame, float degreesPerFrame) {
        return MotionSegment.rotate(ticks(frame), degreesPerFrame * 3f);
    }

    public static MotionSegment pause(int frame, int length) {
        return MotionSegment.freeze(ticks(frame), ticks(length));
    }
}
