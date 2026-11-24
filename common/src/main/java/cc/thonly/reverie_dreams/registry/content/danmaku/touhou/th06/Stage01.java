package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06;

import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeRange;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuLaser;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.implemented;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.ARROWHEAD;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.BALL;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.bullet;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.layerStep;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.sp;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.stream;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.volley;

final class Stage01 {
    private Stage01() {}

    // 1 面 · 露米娅
    static List<TouhouSpell> spells() {
        return List.of(
                // 道中月符：仅 H/L 可达（ecldata1 Sub8 L157-158 `!HL ins_113(500); ins_114("Sub9")`；Normal 超时直接离场）。
                // 42/48 路自机狙环 + 两条固定角长激光（L288/290 ins_85 宽 32 预警 30），随后反向扫掠。
                implemented("touhou/th06/s01_000_hard", "月符「Moonlight Ray」", 1320, () -> moonlightRay()),
                // 夜符：Sub22 L740-814。四段摆角、逐波降速；H 双速度层。
                implemented("touhou/th06/s01_001_normal", "夜符「Night Bird」", 1500, () -> nightBird(false)),
                implemented("touhou/th06/s01_001_hard", "夜符「Night Bird」", 1500, () -> nightBird(true)),
                // 闇符：Sub23 L818-905。三组错角狙环（N 16 路，环组相位差 11.25°，速度 3.0）+ 夜符式摆臂狙流。
                implemented("touhou/th06/s01_002_normal", "暗符「Demarcation」", 1500, () -> demarcation(16, 11.25f, 0.6f)),
                implemented("touhou/th06/s01_002_hard", "暗符「Demarcation」", 1500, () -> demarcation(28, 9f, 0.6f))
        );
    }

    // Sub9: first fire at frame 120, 30-frame warning, then two opposite sweeps.
    private static DanmakuPattern moonlightRay() {
        var ring = volley(EmitShape.ring(42, 0, true), bullet(BALL, 2.5f, 60), 40, EmitAim.TARGET);
        float sweep = (float) Math.toDegrees(0.008267349) * 3;
        return DanmakuPattern.of(ring).withLasers(List.of(
                // Original Sub9 ins_85 uses the blue palette index 6 for both beams.
                new DanmakuLaser(40, 0, 67.5f, 24, 1.35f, 10, 40, -sweep, 0x60A8FF),
                new DanmakuLaser(40, 0, -67.5f, 24, 1.35f, 10, 40, sweep, 0x60A8FF)
        )).withTiming(new SpellTiming(440, 40, SpellTiming.framesToTicks(211)));
    }

    // Sub22: four 16-shot sweeps every two frames; repeat twice before the 100-frame lull.
    private static DanmakuPattern nightBird(boolean hard) {
        float[] starts = {-32.72727f, 32.72727f, -45f, 45f};
        float[] steps = {8.181818f, -8.181818f, 11.25f, -11.25f};
        int[] colors = {0x9366DF, 0x55CA78, 0x73C5FF, 0xDD597D};
        List<DanmakuEmitter> emitters = new java.util.ArrayList<>();
        for (int pass = 0; pass < 2; pass++) {
            for (int arm = 0; arm < starts.length; arm++) {
                for (int wave = 0; wave < 16; wave++) {
                    int tick = 40 + SpellTiming.framesToTicks((pass * 64 + arm * 16 + wave + 1) * 2);
                    float speed = Math.max(0.2f, 1f - 0.2f * wave);
                    EmitShape shape = hard ? EmitShape.multiRing(2, 1, 0, false, 0, 0,
                            layerStep(speed, 0.8f, 2)) : EmitShape.aim();
                    emitters.add(volley(shape, bullet(ARROWHEAD, speed, 80).withColor(colors[arm]), tick, EmitAim.TARGET)
                            .withAim(EmitAim.TARGET, 0, starts[arm] + steps[arm] * wave));
                }
            }
        }
        return new DanmakuPattern(emitters).withTiming(new SpellTiming(500, 40, 120));
    }

    /**
     * 闇符「ディマーケイション」：三组错角狙环（每 20 tick 一组，环组相位差 offset）+ 四段摆臂狙流。
     * ECL Sub23 L818-905。
     */
    private static DanmakuPattern demarcation(int count, float offset, float streamSpeed) {
        return DanmakuPattern.of(
                volley(EmitShape.multiRing(2, count, 0f, true, offset, 0f, 0f),
                        bullet(BALL, 3f, 45), 0, EmitAim.TARGET),
                volley(EmitShape.multiRing(2, count, 0f, true, offset, 0f, 0f),
                        bullet(BALL, 3f, 45), 20, EmitAim.TARGET),
                volley(EmitShape.multiRing(2, count, 0f, true, offset, 0f, 0f),
                        bullet(BALL, 3f, 45), 40, EmitAim.TARGET),
                demarcationStream(0, -32.7f, 98f, streamSpeed),
                demarcationStream(14, 32.7f, -98f, streamSpeed),
                demarcationStream(28, -45f, 87f, streamSpeed),
                demarcationStream(42, 45f, -87f, streamSpeed)
        );
    }

    private static DanmakuEmitter demarcationStream(int delay, float start, float sweep, float speed) {
        return stream(EmitShape.aim(), bullet(ARROWHEAD, speed, 45), delay, 12, 1,
                EmitAim.TARGET, 0f, 0f, KeyframeRange.empty(start, start + sweep));
    }
}
