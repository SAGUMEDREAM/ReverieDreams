package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th08;

import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeRange;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;

/** Reusable pattern primitives for TH08 only. */
final class Th08PatternSupport {
    private Th08PatternSupport() {}

    /** 以 yawRange 线性关键帧实现「每波旋转」的扫射发射器（角度为 MC 适配值）。 */
    static DanmakuEmitter sweep(DanmakuEmitter emitter, int delay, int duration, int interval, float sweepDegrees) {
        return emitter.withTiming(delay, duration, interval)
                .withRanges(KeyframeRange.empty(0f, 0f), KeyframeRange.empty(0f, sweepDegrees));
    }
}
