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

/** TH16 st02bs.ecl: Sakata Nemuno's stage-2 cards. */
final class Stage02 {
    private static final String NEMUNO = "sakata_nemuno";
    private static final int CYAN = 0x28FFFF;
    private static final int BLUE_VIOLET = 0x3C00FF;
    private static final int RED = 0xFF0000;

    private Stage02() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(2, 8, false, NEMUNO, "雨符「被囚的秋雨」", 1800, () -> captiveAutumnRain(false)),
                card(2, 8, true, NEMUNO, "雨符「被诅咒的柴榑雨」", 1800, () -> captiveAutumnRain(true)),
                card(2, 12, false, NEMUNO, "刃符「山姥的磨刀」", 1800, () -> kitchenKnife(false)),
                card(2, 12, true, NEMUNO, "刃符「山姥的鬼磨刀」", 1800, () -> kitchenKnife(true)),
                card(2, 16, false, NEMUNO, "尽符「山间谋杀」", 1800, () -> mountainMurder(false)),
                card(2, 16, true, NEMUNO, "尽符「血腥山间谋杀」", 1800, () -> mountainMurder(true)));
    }

    // st02bs BossCard1 (338-451): an accelerating rain spiral — 12/16-count rings (sprite 8/6,
    // speed 1.5 -> 2.6 px/f) whose heading accelerates +44.5 deg per volley (379: %D += 0.7766
    // rad) at 60/45/28/20f cadence, spawn distance creeping outward; plus aimed 1-count knives
    // with etEx2 splitters whose count grows to 16 (at2 397-451).
    private static DanmakuPattern captiveAutumnRain(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var rain = shot(BALL, hard ? 0.26f : 0.15f, CYAN, 220, MotionSegment.rotate(0, 0.2f));
        int period = hard ? 7 : 15;
        float yawStep = 44.5f;
        for (int v = 0; v < 18; v++) {
            var cue = fire(EmitShape.ring(hard ? 16 : 12, 0f, true), rain, 120 + v * period, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0);
            emitters.add(sweep(cue, 0, yawStep * v, yawStep * v));
        }
        var knife = shot(KUNAI, 0.15f, CYAN, 200,
                MotionSegment.split(ticks(16), new cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionPayload(
                        KUNAI, CYAN, 0.8f, 0.16f, 2, 20f, 120)));
        for (int v = 0; v < 10; v++) {
            emitters.add(fire(EmitShape.aim(), knife, 130 + v * 14, 0, 1).withAim(EmitAim.TARGET, 0, 0));
        }
        return loop(emitters, 1800, 120, ticks(270));
    }

    // st02bs BossCard2 (453-631): alternating knife-swing volleys — ±60 deg aimed fan bursts at
    // ±3.0 px/f drift, 2 passes (N) / 3 (H) / 4 (L) per cycle with 60/40/30f gaps (499-535).
    private static DanmakuPattern kitchenKnife(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var knife = shot(KUNAI, 0.3f, BLUE_VIOLET, 160);
        int passes = hard ? 4 : 2;
        int gap = hard ? 10 : 20;
        for (int p = 0; p < passes; p++) {
            for (int side = 0; side < 2; side++) {
                var cue = fire(EmitShape.fan(5, 12f), knife, 120 + p * gap * 2 + side * gap, 0, 1)
                        .withAim(EmitAim.TARGET, 0, 0);
                emitters.add(sweep(cue, 0, (side == 0 ? -60f : 60f), (side == 0 ? -60f : 60f)));
            }
        }
        return loop(emitters, 1800, 120, ticks(230));
    }

    // st02bs BossCard3 (632-785): mountain-murder spiral — 180 volleys of 1/3/5-count aimed
    // bursts (3.0 px/f decaying to 1.0, sprite 21/1) rotating 222.5 deg per volley at 2f cadence
    // (725-741), plus an eternal 1/2-count rain from 80 px (at2 744-785).
    private static DanmakuPattern mountainMurder(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var burst = shot(BULLET, 0.3f, BLUE_VIOLET, 140, MotionSegment.setSpeed(ticks(40), 0.1f));
        int count = hard ? 5 : 3;
        for (int v = 0; v < 40; v++) {
            float yaw = 222.5f * v;
            var cue = fire(EmitShape.fan(count, 4f), burst, v * 2, 0, 1).withAim(EmitAim.TARGET, 0, 0);
            emitters.add(sweep(cue, 0, yaw, yaw));
        }
        var rain = shot(PELLET, 0.15f, RED, 180, MotionSegment.rotate(0, -0.35f));
        for (int v = 0; v < 30; v++) {
            emitters.add(fire(EmitShape.ring(hard ? 2 : 1, 0f, true), rain, v * 4, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0).withJitter(24f));
        }
        return loop(emitters, 1800, 120, ticks(120));
    }
}
