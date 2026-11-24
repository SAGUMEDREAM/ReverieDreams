package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th13;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th13.Th13PatternSupport.*;

/** TH13 st01bs.ecl: Saigyouji Yuyuko's stage-1 boss spell cards. */
final class Stage01 {
    private static final String YUYUKO = "yuyuko";
    // True bullet colours sampled from th13 bullet.anm (sprite index -> fill colour):
    // spr22 butterfly = 0x2800FF blue, spr8 sakura petal = 0x28FFFF cyan, spr12 = yellow.
    private static final int BLUE = 0x2800FF;
    private static final int CYAN = 0x28FFFF;
    private static final int YELLOW = 0xFCFD26;

    private Stage01() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(1, 0, false, YUYUKO, "符牒「死蝶之舞」", 2400, () -> deathButterflyDance(false)),
                card(1, 2, true, YUYUKO, "符牒「死蝶之舞 -樱花-」", 2100, () -> deathButterflyDance(true)),
                card(1, 4, false, YUYUKO, "幽蝶「幽灵聚集点」", 2400, () -> ghostSpot(false)),
                card(1, 6, true, YUYUKO, "幽蝶「幽灵聚集点 -樱花-」", 2400, () -> ghostSpot(true)),
                card(1, 8, false, YUYUKO, "冥符「常夜樱」", 3000, Stage01::evernightSakura),
                card(1, 8, true, YUYUKO, "冥符「常夜樱」", 3000, Stage01::evernightSakura),
                card(1, 12, true, YUYUKO, "樱符「西行樱吹雪」", 3000, () -> saigyouBlizzard(false)),
                extraCard(1, 119, YUYUKO, "樱符「樱吹雪地狱」", 3000, () -> saigyouBlizzard(true)));
    }

    // st01bs BossCard1 (297-482): every volley a 12 (E/N) / 24 (H) / 32 (L) butterfly ring is aimed
    // at the player (BossCard1_at 399-414, at2 465-480). Butterflies fly at 2.0 px/f, accelerate
    // ~+0.017 px/f^2 across frames 8-120 (etEx mode 0) and curve ~1.5-2.25 deg/f; the mirrored
    // at2 sub flips the curve. HL adds a companion 8/16 ring split into +-45 deg halves (etCopy,
    // 391-396) that trails small butterflies every 16f (etExSet2). Volley cadence 40f, cycle
    // shrinks 720 -> 560 frames as the inter-sub wait drops 200 -> 120.
    private static DanmakuPattern deathButterflyDance(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        int count = hard ? 24 : 12;
        var butterfly = shot(BUTTERFLY, 0.2f, BLUE, 240,
                MotionSegment.setSpeed(ticks(120), 0.38f),
                MotionSegment.rotate(ticks(8), 0.5f));
        var butterflyMirrored = shot(BUTTERFLY, 0.2f, BLUE, 240,
                MotionSegment.setSpeed(ticks(120), 0.38f),
                MotionSegment.rotate(ticks(8), -0.5f));
        var companion = shot(BUTTERFLY, 0.2f, CYAN, 160,
                MotionSegment.rotate(ticks(8), 0.75f));
        var companionLeft = shot(BUTTERFLY, 0.2f, CYAN, 160,
                MotionSegment.rotate(ticks(8), 0.75f));
        var companionRight = shot(BUTTERFLY, 0.2f, CYAN, 160,
                MotionSegment.rotate(ticks(8), -0.75f));
        for (int volley = 0; volley < 4; volley++) {
            int at = volley * 40;
            emitters.add(fire(EmitShape.ring(count, 0f, true), butterfly, at, 0, 1));
            int side = at + 20;
            if (hard) {
                emitters.add(sweep(fire(EmitShape.ring(8, 0f, true), companionLeft, side, 0, 1), 0, -45f, -45f));
                emitters.add(sweep(fire(EmitShape.ring(8, 0f, true), companionRight, side, 0, 1), 0, 45f, 45f));
            } else {
                emitters.add(fire(EmitShape.ring(8, 0f, true), companion, side, 0, 1));
            }
            emitters.add(fire(EmitShape.ring(count, 0f, true), butterflyMirrored, at + 280, 0, 1));
            if (hard) {
                emitters.add(sweep(fire(EmitShape.ring(8, 0f, true), companionLeft, at + 300, 0, 1), 0, -45f, -45f));
                emitters.add(sweep(fire(EmitShape.ring(8, 0f, true), companionRight, at + 300, 0, 1), 0, 45f, 45f));
            } else {
                emitters.add(fire(EmitShape.ring(8, 0f, true), companion, at + 300, 0, 1));
            }
        }
        return loop(emitters, hard ? 2100 : 2400, 150, 580);
    }

    // st01bs BossCard2 (484-592): aimed 6/6/10/16 rings (speed 0.7, +-2 deg/f curve, accelerating
    // each volley, BossCard2_at 542-568) fire from the boss 90f apart (2 on E, 3 otherwise); then
    // BossCard2_at2 (570-592) drops 5/5/5/5-volley chains from player-relative offsets spanning
    // +-128 px horizontally (527-535) with rising acceleration (+0.6 per volley). Boss also
    // dashes and fires straight 192-fountain bursts mid-cycle (511-526) — kept as the dash-free
    // ring sequence.
    private static DanmakuPattern ghostSpot(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        int count = hard ? 10 : 6;
        var ghost = shot(BUTTERFLY, 0.07f, YELLOW, 260,
                MotionSegment.accelerate(ticks(4), 0.0022f),
                MotionSegment.rotate(ticks(8), 0.67f));
        var ghostMirrored = shot(BUTTERFLY, 0.07f, YELLOW, 260,
                MotionSegment.accelerate(ticks(4), 0.0022f),
                MotionSegment.rotate(ticks(8), -0.67f));
        for (int volley = 0; volley < 3; volley++) {
            emitters.add(fire(EmitShape.ring(count, 0f, true), ghost, 30 + volley * 90, 0, 1));
            emitters.add(fire(EmitShape.ring(count, 0f, true), ghostMirrored, 75 + volley * 90, 0, 1));
        }
        float[] dx = {-8f, -4f, 0f, 4f, 8f};
        for (int chain = 0; chain < 5; chain++) {
            var drop = shot(BUTTERFLY, 0.07f, CYAN, 240,
                    MotionSegment.accelerate(ticks(4), 0.005f + 0.01f * chain),
                    MotionSegment.rotate(ticks(8), chain % 2 == 0 ? 0.67f : -0.67f));
            var cue = fire(EmitShape.ring(count, 0f, true), drop, 340 + chain * 90, 0, 1);
            emitters.add(cue.withOffset(new Vec3(dx[chain], 0, -5.5)));
        }
        return loop(emitters, 2400, 60, 800);
    }

    // st01bs BossCard3 (594-668): one endless async loop (BossCard3_at) firing triplets of slow
    // petal clusters every 50/50/40/30f (diffI, shrinks while A > B). Clusters are 2 (E) / 4 (N/H/L)
    // petals in 2/2/3/4 ways (etCountD 639), speed 0.4 px/f, spin 10 deg/f, spawned at randomized
    // headings around the boss (-90..-30, -11..+19, -169..-139 deg) with randomized speed and angle
    // scatter (etEx flags 8192/16384). Ported as a 120f cycle of three randomized clusters.
    private static DanmakuPattern evernightSakura() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var petal = shot(SAKURA_PETAL, 0.04f, BLUE, 400,
                MotionSegment.accelerate(ticks(60), 0.0006f),
                MotionSegment.rotate(0, 0.33f));
        float[] headings = {-90f, -11f, -169f};
        float[] spread = {60f, 30f, 30f};
        for (int k = 0; k < 3; k++) {
            var cue = fire(EmitShape.fan(4, spread[k]), petal, k * 40, 0, 1).withJitter(18f);
            emitters.add(sweep(cue, 0, headings[k], headings[k]));
        }
        return loop(emitters, 3000, 90, 120);
    }

    // st01bs BossCard4 (671-783): two endless streams of 12-petal rings (sprite 8 = sakura petal,
    // speed 2.0 accelerating +0.0125 px/f^2 across frames 8-240, curve +-1 deg/f, BossCard4_at
    // 715-748 / at2 750-783). H fires both streams every 40f, L every 28f (at2 leading by 20/14f);
    // Overdrive tightens to 8f and rotates the first stream's aim -5 deg per volley (732-735).
    private static DanmakuPattern saigyouBlizzard(boolean overdrive) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var petal = shot(SAKURA_PETAL, 0.2f, CYAN, 260,
                MotionSegment.setSpeed(ticks(240), 0.5f),
                MotionSegment.rotate(ticks(8), 0.33f));
        var petalMirrored = shot(SAKURA_PETAL, 0.2f, BLUE, 260,
                MotionSegment.setSpeed(ticks(240), 0.5f),
                MotionSegment.rotate(ticks(8), -0.33f));
        int period = overdrive ? 3 : 13;
        int offset = overdrive ? 7 : 5;
        int volleys = overdrive ? 80 : 6;
        float yawStep = overdrive ? -5f : 0f;
        for (int v = 0; v < volleys; v++) {
            var cue = fire(EmitShape.ring(12, 0f, true), petal, v * period, 0, 1);
            emitters.add(sweep(cue, 0, yawStep * v, yawStep * v));
            emitters.add(fire(EmitShape.ring(12, 0f, true), petalMirrored, v * period + offset, 0, 1));
        }
        return loop(emitters, 3000, 90, overdrive ? 240 : 78);
    }
}
