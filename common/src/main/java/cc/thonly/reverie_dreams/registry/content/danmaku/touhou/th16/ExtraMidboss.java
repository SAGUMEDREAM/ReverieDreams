package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th16;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th16.Th16PatternSupport.*;

/** TH16 st07mbs.ecl: the two dancers (Nishida Satono and Teireida Mai) as Extra midboss. */
final class ExtraMidboss {
    private static final String SATONO = "nishida_satono";
    private static final String MAI = "teireida_mai";
    private static final int LIME = 0x9DFF2A;
    private static final int RED = 0xFF0000;
    private static final int PINK = 0xFD8BFF;

    private ExtraMidboss() {}

    static List<TouhouSpell> spells() {
        return List.of(
                extraCard(7, 106, SATONO, "鼓舞「强力欢呼」", 3000, ExtraMidboss::powerfulCheers),
                extraCard(7, 107, MAI, "狂舞「疯狂伴舞」", 3000, ExtraMidboss::crazyBackDance),
                extraCard(7, 108, MAI, "弹舞「第二个台风」", 3600, ExtraMidboss::secondTyphoon));
    }

    // MBossCard1 (137-240): aimed 1-count pairs every 60f from both dancers.
    private static DanmakuPattern powerfulCheers() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var shot1 = shot(BULLET, 0.15f, LIME, 160);
        for (int v = 0; v < 14; v++) {
            emitters.add(fire(EmitShape.aim(), shot1, 60 + v * 16, 0, 1).withAim(EmitAim.TARGET, 0, 0));
        }
        return loop(emitters, 3000, 60, ticks(240));
    }

    // MBossCard2 (247-370): mirrored aimed pairs with a back-dance sway.
    private static DanmakuPattern crazyBackDance() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var shot1 = shot(BULLET, 0.15f, LIME, 160, MotionSegment.rotate(0, 0.3f));
        for (int v = 0; v < 14; v++) {
            var cue = fire(EmitShape.aim(), shot1, 60 + v * 16, 0, 1).withAim(EmitAim.TARGET, 0, 0);
            emitters.add(sweep(cue, 0, v % 2 == 0 ? 30f : -30f, v % 2 == 0 ? 30f : -30f));
        }
        return loop(emitters, 3000, 60, ticks(240));
    }

    // MBossCard3 (455-528): 24-count rings (sprite 11/4, 3.0 px/f) every 20f whose bullets
    // split twice every 16f into +-30/120 deg branches (etEx2 487-488, 514-515).
    private static DanmakuPattern secondTyphoon() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var typhoon = shot(BALL, 0.3f, RED, 180,
                MotionSegment.split(ticks(16), new cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionPayload(
                        BALL, LIME, 0.8f, 0.2f, 2, 120f, 110)),
                MotionSegment.split(ticks(32), new cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionPayload(
                        BALL, PINK, 0.8f, 0.24f, 2, 30f, 110)));
        for (int v = 0; v < 12; v++) {
            emitters.add(fire(EmitShape.ring(24, 0f, true), typhoon, 60 + v * 20, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0));
        }
        return loop(emitters, 3600, 60, ticks(240));
    }
}
