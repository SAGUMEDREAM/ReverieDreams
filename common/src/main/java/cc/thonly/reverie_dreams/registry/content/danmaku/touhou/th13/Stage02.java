package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th13;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionPayload;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th13.Th13PatternSupport.*;

/**
 * TH13 st02mbs/st02bs.ecl: Tatara Kogasa's midboss and stage-2 boss cards. Her bullets (sprite 12,
 * the voice/wave sprite) re-emit a slower echo copy mid-flight — etEx2(id,0,64,1,45,...,1.6/1.8
 * speed) — ported with MotionSegment.split.
 */
final class Stage02 {
    private static final String KOGASA = "tatara_kogasa";
    // th13 spr12 voice-wave bullets are yellow (0xFCFD26) for every Kogasa card.
    private static final int YELLOW = 0xFCFD26;

    private Stage02() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(2, 14, false, KOGASA, "响符「山间回声」", 2700, () -> mountainEcho(3, 11, 6f)),
                card(2, 16, true, KOGASA, "响符「山间回声乱舞」", 2700, () -> mountainEcho(4, 15, 4f)),
                card(2, 18, false, KOGASA, "响符「强力共鸣」", 2700, () -> powerResonance(2, 10, 14)),
                card(2, 18, true, KOGASA, "响符「强力共鸣」", 2700, () -> powerResonance(5, 18, 18)),
                card(2, 22, false, KOGASA, "山彦「长程回声」", 2700, () -> longRangeEcho(24, 6.5f, false)),
                card(2, 24, true, KOGASA, "山彦「增幅回声」", 2700, () -> longRangeEcho(72, 3.5f, false)),
                extraCard(2, 120, KOGASA, "山彦「山彦的本领发挥回声」", 2700, () -> longRangeEcho(72, 3.5f, true)),
                card(2, 26, false, KOGASA, "大声「蓄力呐喊」", 3000, Stage02::chargedCry),
                card(2, 28, true, KOGASA, "大声「蓄力吆喝」", 3000, Stage02::chargedYahoo));
    }

    // st02mbs MBossCard1 (119-221): every ~120f cycle a burst of 2/3/4/5 rings (diffI B) fired
    // back-to-back with counts growing +11 (E/N) / +15 (H/L) from 27/16 (diffI A); ring aim
    // tracks the player /96 with a 3 deg/f (N) or 2 deg/f (H) spin. Bullets fly 1.4 px/f and
    // echo-split at 64f and 128f into 1.6/2.2 px/f copies (etEx2 x2, 178-180).
    private static DanmakuPattern mountainEcho(int volleys, int countStep, float spinDegPerVolley) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var echo = shot(NOTE, 0.14f, YELLOW, 200,
                splitEcho(0.16f, 1),
                splitEcho(0.16f, 2));
        var echoHard = shot(NOTE, 0.14f, YELLOW, 200,
                splitEcho(0.22f, 1),
                splitEcho(0.22f, 2));
        for (int v = 0; v < volleys; v++) {
            int count = (volleys >= 4 ? 16 : 27) + countStep * v;
            var cue = fire(EmitShape.ring(count, 0f, true), volleys >= 4 ? echoHard : echo, v, 0, 1);
            emitters.add(sweep(cue, 0, spinDegPerVolley * v, spinDegPerVolley * v));
        }
        return loop(emitters, 2700, 60, 40);
    }

    private static MotionSegment splitEcho(float speed, int at) {
        return MotionSegment.split(ticks(64) * at, new MotionPayload(NOTE, 0xFCFD26, 0.9f, speed, 1, 0f, 120));
    }

    // st02bs BossCard1 (294-372): パワーレゾナンス loops [@at (2 E/N or 5 H/L volleys 30f apart)
    // + visual familiar pass @at2]. Volleys are 10/14/18-count rings (diffI C), speed 1.2 rising
    // +0.3 per volley (362-364), spin 10 deg/f aimed near the player /60, and bullet lifetime
    // shrinks 310 -> 180 frames across the volleys (etExSet mode 1, 361).
    private static DanmakuPattern powerResonance(int volleys, int count, int hardCount) {
        var emitters = new ArrayList<DanmakuEmitter>();
        for (int v = 0; v < volleys; v++) {
            float speed = 0.12f + 0.03f * v;
            int lifetime = 310 - 30 * v;
            int ringCount = v == 0 ? Math.max(10, count) : (volleys > 2 ? hardCount : count);
            var wave = shot(NOTE, speed, YELLOW, lifetime,
                    MotionSegment.rotate(ticks(8), 0.33f));
            emitters.add(fire(EmitShape.ring(ringCount, 0f, true), wave, 150 + v * 30, 0, 1));
        }
        return loop(emitters, 2700, 150, volleys * 30 + 30);
    }

    // st02bs BossCard2 (410-565): every ~422f cycle three 60f-spaced volleys of big rings:
    // 24 (E/N) / 72 (H/L/OD) bullets at 2.8 px/f with a 6.5/3.5 deg/f ring rotation per volley
    // (diffF %A, 480-510), aimed at -90 deg + player heading /30, each bullet echo-splitting at
    // 64f into a 1.8 px/f copy (etEx2 478/508/546). Overdrive re-aims the ring at the player
    // directly (aim mode 3, 536-541). Boss dash attacks between volleys are decorative.
    private static DanmakuPattern longRangeEcho(int count, float spinPerVolley, boolean overdrive) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var echo = shot(NOTE, 0.28f, YELLOW, 200, splitEcho(0.18f, 1));
        for (int v = 0; v < 3; v++) {
            var cue = fire(EmitShape.ring(count, 0f, true), echo, 60 + v * 60, 0, 1);
            if (overdrive) {
                cue = cue.withAim(EmitAim.TARGET, 0, 0);
            }
            emitters.add(sweep(cue, 0, spinPerVolley * v, spinPerVolley * v));
        }
        return loop(emitters, 2700, 60, 140);
    }

    // st02bs BossCard3 (567-677): チャージドクライ (E/N) runs an endless 18f-cadence stream of
    // 1 (E) / 3 (N)-bullet aimed bursts with a 15 deg/f heading spin (BossCard3_at 611-629) while
    // three tracking familiars shadow the player (@at2 632-656, kept as decoration). H/L replaces
    // it with an endless stream of 32 (H) / 16 (L) full rings re-aimed every volley with a 10 deg
    // spin (BossCard3_at_h 659-677).
    private static DanmakuPattern chargedCry() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var cry = shot(NOTE, 0.18f, YELLOW, 150, MotionSegment.rotate(0, 0.87f));
        for (int v = 0; v < 10; v++) {
            var cue = fire(EmitShape.fan(3, 4f), cry, v * 18, 0, 1);
            emitters.add(sweep(cue, 0, 15f * v, 15f * v));
        }
        return loop(emitters, 3000, 90, 60);
    }

    private static DanmakuPattern chargedYahoo() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var shout = shot(NOTE, 0.23f, YELLOW, 170);
        for (int v = 0; v < 20; v++) {
            var cue = fire(EmitShape.ring(32, 0f, true), shout, v * 3, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0);
            emitters.add(sweep(cue, 0, 10f * v, 10f * v));
        }
        return loop(emitters, 3000, 90, 60);
    }
}
