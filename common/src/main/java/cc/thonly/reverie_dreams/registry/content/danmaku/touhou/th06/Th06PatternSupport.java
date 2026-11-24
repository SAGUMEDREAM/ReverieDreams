package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeRange;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.DanmakuMotion;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/** Reusable pattern primitives for TH06 only. */
final class Th06PatternSupport {
    private Th06PatternSupport() {}

    /** TH06 像素/帧 → MC 格/tick 的走位适配系数。 */
    private static final float SPEED_SCALE = 0.15f;
    static final Identifier ARROWHEAD = ReverieDreams.id("arrowhead");
    static final Identifier BALL = ReverieDreams.id("ball");
    static final Identifier RICE = ReverieDreams.id("rice");

    static float sp(float pxPerFrame) {
        return pxPerFrame * SPEED_SCALE;
    }

    static DanmakuBulletSpec bullet(Identifier type, float pxPerFrame, int lifetime) {
        return new DanmakuBulletSpec(type, DanmakuBulletSpec.DEFAULT_COLOR, false,
                sp(pxPerFrame), 1f, 0f, lifetime, DanmakuMotion.STRAIGHT);
    }

    /** 多层速度环：TH06 速度层 s_j = s1 − (s1−s2)·j/M（末层不取 s2）与 ringSpeedStep 线性插值一一对应。 */
    static float layerStep(float s1, float s2, int layers) {
        return layers <= 1 ? 0f : -(1f - s2 / s1) / layers;
    }

    static DanmakuEmitter volley(EmitShape shape, DanmakuBulletSpec spec, int delay, EmitAim aim) {
        return DanmakuEmitter.of(shape, spec).withTiming(delay, 0, 1).withAim(aim, 0f, 0f);
    }

    static DanmakuEmitter stream(EmitShape shape, DanmakuBulletSpec spec, int delay, int duration,
                                         int interval, EmitAim aim, float pitch, float yaw,
                                         @Nullable KeyframeRange yawRange) {
        DanmakuEmitter emitter = DanmakuEmitter.of(shape, spec)
                .withTiming(delay, duration, interval)
                .withAim(aim, pitch, yaw);
        if (yawRange != null) {
            emitter = emitter.withRanges(KeyframeRange.empty(0f, 0f), yawRange);
        }
        return emitter;
    }
}
