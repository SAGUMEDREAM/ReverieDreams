package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th14;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuLaser;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th14.Th14PatternSupport.*;

/**
 * TH14 st07bs.ecl (Mamizou Futatsuiwa, Extra boss). Numbers are the ECL spellEx ids; the
 * deadline variable $A resolves to 50 frames x 60 for cards 111-117, 75 x 60 for card 118 and
 * 120 x 60 for card 119 (st07bs L1347/L2132/L1000).
 * Source: E:/Temp/rd-ecl-continuation-20261116/th14/ecl/st07bs.ecl.txt.
 */
final class Extra {
    private static final String MAMIZOU = "hutatsuiwa_mamizou";
    private static final int WHITE = 0xE8E8F0;
    private static final int RED = 0xE85C5C;
    private static final int MAGENTA = 0xB85CD0;
    private static final int YELLOW = 0xE8E060;
    private static final int BLUE = 0x6464E8;
    private static final int VIOLET = 0x9C6CC8;
    private static final int CYAN = 0x6CE8E8;

    private Extra() {}

    static List<TouhouSpell> spells() {
        return List.of(
                extraCard(7, 111, MAMIZOU, "一鼓「暴乱宫太鼓」", 3000, Extra::ragingTaiko),
                extraCard(7, 112, MAMIZOU, "二鼓「怨灵绫鼓」", 3000, Extra::ayanoDrum),
                extraCard(7, 113, MAMIZOU, "三鼓「午夜零时的三振」", 3000, Extra::midnightStrike),
                extraCard(7, 114, MAMIZOU, "死鼓「Land Percuss」（轻敲大地）", 3000, Extra::landPercuss),
                extraCard(7, 115, MAMIZOU, "五鼓「雷电拨浪鼓」", 3000, Extra::dendenDrum),
                extraCard(7, 116, MAMIZOU, "六鼓「Alternate Sticking」（交替打击法）", 3000, Extra::alternateSticking),
                extraCard(7, 117, MAMIZOU, "七鼓「高速和太鼓火箭」", 3000, Extra::taikoRocket),
                extraCard(7, 118, MAMIZOU, "八鼓「雷神之怒」", 4500, Extra::raijinWrath),
                extraCard(7, 119, MAMIZOU, "「Blue Lady Show」（蓝色佳人秀）", 4500, Extra::blueLadyShow),
                extraCard(7, 120, MAMIZOU, "「Pristine Beat」（原始的节拍）", 7200, Extra::pristineBeat));
    }

    /**
     * BossCard1 (st07bs L971): spellEx 110, 3000f. BossCard1_at spawns nine TaikoA01 drums from
     * x = -192 walking right (48 px, 5 frames apart) and nine from +192 walking left; the drums
     * fall at 1 -> 3 px/frame and despawn near the bottom. Ported as falling drum bullets from
     * sweeping columns on a 310-frame cycle.
     */
    private static DanmakuPattern ragingTaiko() {
        var shots = new ArrayList<DanmakuEmitter>();
        var drum = shot(BUBBLE, 0.19f, RED, 90,
                MotionSegment.accelerate(0, 0.006f), MotionSegment.linear(ticks(60)));
        for (int cycle = 0, start = 150; start < 3000; cycle++, start += 310) {
            for (int k = 0; k < 9; k++) {
                float x = -9f + k * 2.0f;
                shots.add(sweep(fire(EmitShape.aim(), drum, start + k * 5, 0, 5), 65f, 0f, 0f)
                        .withOffset(new Vec3(x, 2.6, 0)).withJitter(5f));
                float back = 9f - k * 2.0f;
                shots.add(sweep(fire(EmitShape.aim(), drum, start + 90 + k * 5, 0, 5), 65f, 0f, 0f)
                        .withOffset(new Vec3(back, 2.6, 0)).withJitter(5f));
            }
        }
        return timeline(shots, 3000);
    }

    /**
     * BossCard2 (st07bs L1364): spellEx 111, 3000f. BossCard2_at launches 4+4+24 phantoms whose
     * wall-exit reaction fires five tight accelerating bullets (speed 1 -> 9 px/frame over 120
     * frames each) plus a 90-degree pair. Ported as phantom-position fans on the three volleys.
     */
    private static DanmakuPattern ayanoDrum() {
        var shots = new ArrayList<DanmakuEmitter>();
        var cluster = shot(BALL, 0.09f, RED, 110, MotionSegment.accelerate(0, 0.045f));
        var pair = shot(BALL, 0.19f, RED, 80);
        for (int volley = 0; volley < 3; volley++) {
            int count = volley == 2 ? 24 : 4;
            float base = volley == 0 ? -45f : volley == 1 ? -135f : 0f;
            float step = volley == 2 ? 11.25f : 30f;
            int start = 150 + volley * 300;
            for (int k = 0; k < count; k++) {
                float yaw = base + k * step;
                shots.add(sweep(fire(EmitShape.fan(5, 4f), cluster, start + k * 10, 0, 5),
                        0f, yaw, yaw).withJitter(6f));
                shots.add(sweep(fire(EmitShape.fan(2, 90f), pair, start + k * 10 + 5, 0, 5),
                        0f, yaw, yaw).withJitter(6f));
            }
        }
        return timeline(shots, 3000);
    }

    /**
     * BossCard3 (st07bs L1529): spellEx 112, 3000f. Three TaikoA02 drums (radii 16/16/160 px)
     * orbit while TaikoA02_at answers every 60 frames with a 4.0 px/frame pair plus symmetric
     * 6-bullet fans at 10/20/30-degree spacing running 3.0/2.0/1.0 px/frame in both directions.
     */
    private static DanmakuPattern midnightStrike() {
        var shots = new ArrayList<DanmakuEmitter>();
        Vec3[] spots = {new Vec3(-6.7, 1.0, 0), new Vec3(6.7, 1.0, 0), new Vec3(0, 1.5, 0)};
        var fast = shot(BALL, 0.75f, BLUE, 70);
        var mid = shot(BALL, 0.56f, VIOLET, 70);
        var slow = shot(BALL, 0.37f, CYAN, 70);
        for (int cycle = 0, start = 270; start < 3000; cycle++, start += 60) {
            float yaw = cycle * 3f;
            for (Vec3 spot : spots) {
                shots.add(sweep(fire(EmitShape.fan(2, 180f), fast, start, 0, 3), 0f, yaw, yaw)
                        .withOffset(spot));
                shots.add(sweep(fire(EmitShape.fan(6, 50f), mid, start + 2, 0, 3), 0f, yaw, yaw)
                        .withOffset(spot));
                shots.add(sweep(fire(EmitShape.fan(6, 100f), slow, start + 4, 0, 3), 0f, yaw, yaw)
                        .withOffset(spot));
            }
        }
        return timeline(shots, 3000);
    }

    /**
     * BossCard4 (st07bs L1599): spellEx 113, 3000f. BossCard4_at fans 140 yellow amulets over a
     * 140-degree arc (1.0 -> 0.7 px/frame) every 90 frames from a +/-32 px random offset; the
     * card body answers each volley with a screen shake. Fan density reduced to 24 amulets.
     */
    private static DanmakuPattern landPercuss() {
        var shots = new ArrayList<DanmakuEmitter>();
        var amulet = shot(AMULET, 0.19f, YELLOW, 80,
                MotionSegment.accelerate(0, -0.0015f), MotionSegment.linear(ticks(20)));
        for (int start = 150; start < 3000; start += 90) {
            shots.add(fire(EmitShape.fan(24, 24f), amulet, start, 0, 3).withJitter(14f));
        }
        return timeline(shots, 3000);
    }

    /**
     * BossCard5 (st07bs L1669): spellEx 114, 3000f. BossCard5_at fires thirteen curving stub
     * lasers stepping 27.7 degrees, then thirteen more stepping the opposite way (S-curve
     * -/+1.3 deg/frame over 300 frames, 28 px width); BossCard2_at2 adds bouncing pairs of blue
     * rounds whose interval shrinks with $GI0 (46 -> 10). Laser sweep uses the initial rate.
     */
    private static DanmakuPattern dendenDrum() {
        var shots = new ArrayList<DanmakuEmitter>();
        var beams = new ArrayList<DanmakuLaser>();
        for (int call = 0, start = 150; start < 3000; call++, start += 240) {
            float sign = (call & 1) == 0 ? 1.3f : -1.3f;
            for (int k = 0; k < 13; k++) {
                float yaw = k * 27.7f;
                beams.add(laser(ticks(start + k), 0f, yaw, 12f, 1.75f, 20, 30, sign, WHITE));
                beams.add(laser(ticks(start + 50 + k), 0f, yaw, 12f, 1.75f, 20, 30, -sign, WHITE));
            }
        }
        var bounce = shot(BALL, 0.30f, BLUE, 100,
                MotionSegment.accelerate(0, -0.004f), MotionSegment.linear(ticks(20)));
        for (int start = 60; start < 3000; start += 9) {
            shots.add(fire(EmitShape.aim(), bounce, start, 0, 3)
                    .withOffset(new Vec3(0, 1.6, 0)).withJitter(10f));
        }
        return timeline(shots, beams, 3000);
    }

    /**
     * BossCard6 (st07bs L1794): spellEx 115, 3000f. The boss dashes to +/-170 px and calls
     * BossCard6_at (base angle alternating 0/pi, deadline $D shrinking 120 -> 30): sixteen
     * 3-frame iterations of mirrored stop-go blue rounds (brake to 0 in 60 frames, re-accelerate
     * 0.2-5.2 px/frame over 320) at vertical offsets growing 14 px per shot, plus yellow big-ball
     * showers that grow with $GI0.
     */
    private static DanmakuPattern alternateSticking() {
        var shots = new ArrayList<DanmakuEmitter>();
        var round = shot(BUBBLE, 0.19f, BLUE, 110,
                MotionSegment.setSpeed(ticks(20), 0.02f),
                MotionSegment.accelerate(ticks(20), 0.008f), MotionSegment.linear(ticks(61)));
        var shower = shot(BALL, 0.02f, YELLOW, 110,
                MotionSegment.accelerate(ticks(20), 0.01f), MotionSegment.linear(ticks(21)));
        int deadline = 120;
        for (int call = 0, start = 180; start < 3000; call++, start += 210 + deadline) {
            float yaw = (call & 1) == 0 ? 0f : 180f;
            float x = (call & 1) == 0 ? 6.5f : -6.5f;
            int window = Math.min(48, 3000 - start);
            for (float side : new float[]{-1f, 1f}) {
                shots.add(sweep(fire(EmitShape.aim(), round, start, window, 3), 0f, yaw, yaw)
                        .withOffset(new Vec3(x, 1.2 + side * 1.1, 0)).withJitter(6f));
            }
            shots.add(fire(EmitShape.fan(6, 150f), shower, start + 10, Math.max(0, window - 10), 3)
                    .withOffset(new Vec3(x, 1.2, 0)).withJitter(20f));
            deadline = Math.max(30, deadline - 10);
        }
        return timeline(shots, 3000);
    }

    /**
     * BossCard7 (st07bs L1907): spellEx 116, 3000f. BossCard7_at launches ten TaikoA07 rockets
     * (10 frames apart, yaw stepping +20 degrees) then two TaikoA07b rockets; each rocket fires
     * a 600 px x 48 px travelling laser streak and later a 60-bullet trail. Ported as ten laser
     * streaks per 560-frame cycle with yaw stepping 20 degrees plus a trailing bullet fan.
     */
    private static DanmakuPattern taikoRocket() {
        var shots = new ArrayList<DanmakuEmitter>();
        var beams = new ArrayList<DanmakuLaser>();
        var trail = shot(BALL, 0.02f, RED, 100,
                MotionSegment.accelerate(0, 0.012f), MotionSegment.linear(ticks(40)));
        for (int cycle = 0, start = 150; start < 3000; cycle++, start += 560) {
            float base = 180f + cycle * 13f;
            for (int k = 0; k < 10; k++) {
                float yaw = base + k * 20f;
                beams.add(laser(ticks(start + k * 10), 0f, yaw, 20f, 1.75f, 8, 12, 0f, CYAN));
                shots.add(sweep(fire(EmitShape.fan(6, 50f), trail, start + k * 10 + 40, 0, 3),
                        0f, yaw + 180f, yaw + 180f).withJitter(10f));
            }
        }
        return timeline(shots, beams, 3000);
    }

    /**
     * BossCard8 (st07bs L2020): spellEx 117, 3000f. BossCard8_at scatters $I0 (25 -> 65) curving
     * stub lasers stepping 222.5 degrees with S-curve rates flipping per cycle; BossCard8_at3
     * grows 60 blue rounds from an expanding ring (speed 0 -> 3.0 px/frame over 320 frames).
     */
    private static DanmakuPattern raijinWrath() {
        var shots = new ArrayList<DanmakuEmitter>();
        var beams = new ArrayList<DanmakuLaser>();
        for (int cycle = 0, start = 150; start < 4500; cycle++, start += 300) {
            int lasers = Math.min(25 + cycle * 5, 65);
            float sign = (cycle & 1) == 0 ? 1.7f : -1.7f;
            for (int k = 0; k < lasers; k++) {
                float yaw = k * 222.5f % 360f;
                beams.add(laser(ticks(start + k * 2), 0f, yaw, 10f, 1.75f, 20, 30, sign, BLUE));
            }
            var ring = shot(BALL, 0.02f, BLUE, 110, MotionSegment.accelerate(0, 0.0052f));
            shots.add(fire(EmitShape.ring(24, 1.5f + cycle * 0.12f, true), ring, start + 120, 0, 3)
                    .withOffset(new Vec3(0, 1.4, 0)));
        }
        return timeline(shots, beams, 4500);
    }

    /**
     * BossCard9 (st07bs L2148): spellEx 118, 4500f. BossCard9_at/_at2/_at3 run rotating
     * 8-bullet white-note sheets aimed straight down or up (four, six and eight streams; the
     * base angle drifts 0.0015-0.0023 rad/frame) with the beat rhythm staggering them 0/207/402/
     * 563 ticks; _at3 adds a 20-bullet violet note fan.
     */
    private static DanmakuPattern blueLadyShow() {
        var shots = new ArrayList<DanmakuEmitter>();
        int[][] phases = {{4, 1200, 90, 150}, {6, 1200, 90, 1470}, {8, 1200, 90, 2790}};
        for (int phase = 0; phase < phases.length; phase++) {
            int streams = phases[phase][0];
            int start = phases[phase][3];
            float drift = (phase & 1) == 0 ? -0.26f : 0.34f;
            for (int s = 0; s < streams; s++) {
                var note = shot(NOTE, 0.38f, WHITE, 90);
                float base = (s % 2 == 0) ? 90f : -90f;
                shots.add(sweep(fire(EmitShape.fan(8, 13.1f), note, start + s * 20,
                        phases[phase][1], phases[phase][2]), 0f, base, base + drift * 400)
                        .withJitter(3f));
            }
        }
        var violet = shot(NOTE, 0.23f, MAGENTA, 80);
        shots.add(sweep(fire(EmitShape.fan(20, 37.5f), violet, 4110, 390, 20), 0f, 90f, 60f));
        return timeline(shots, 4500);
    }

    /**
     * BossCard10 (st07bs L1017): spellEx 119, 7200f. Four life-gated phases loop the drum
     * rhythm: beat1 fans 52 cyan notes (97.5 degrees, 4.5 -> 2.5 px/frame) and beat2 fires
     * 96-bullet 0.5-degree clusters from +/-32 px offsets (colors 6/4/2/13 per phase); the
     * final section answers every 45 frames with 36 white notes. Cluster density reduced to
     * 24 bullets over 24 degrees.
     */
    private static DanmakuPattern pristineBeat() {
        var shots = new ArrayList<DanmakuEmitter>();
        int[] phaseColors = {CYAN, MAGENTA, RED, YELLOW};
        int[] phaseEnd = {1500, 3000, 4500, 5400};
        for (int phase = 0; phase < 4; phase++) {
            int start = phase == 0 ? 60 : phaseEnd[phase - 1];
            int end = phaseEnd[phase];
            int count = phase == 0 ? 32 : phase == 1 ? 32 : phase == 2 ? 19 : 25;
            for (int t = start + 90; t < end; t += 270) {
                var beat1 = shot(NOTE, 0.85f, phaseColors[phase], 70,
                        MotionSegment.accelerate(0, -0.02f), MotionSegment.linear(ticks(20)));
                shots.add(sweep(fire(EmitShape.fan(16, 30f), beat1, t, 0, 3), 0f, 0f, 0f)
                        .withJitter(8f));
                var beat2 = shot(BALL, 0.38f, phaseColors[phase], 80);
                float[][] offsets = {{2, 0}, {-2, 0}, {0, 1}, {0, -1}};
                int pairs = Math.min(phase + 1, 4);
                for (int k = 0; k < pairs; k++) {
                    shots.add(sweep(fire(EmitShape.fan(24, 24f), beat2, t + 135 + k * 45, 0, 3),
                            0f, 0f, 0f).withOffset(new Vec3(offsets[k][0], 1.2 + offsets[k][1], 0))
                            .withJitter(3f));
                }
            }
        }
        var finale = shot(NOTE, 0.56f, WHITE, 80,
                MotionSegment.accelerate(0, -0.006f), MotionSegment.linear(ticks(20)));
        for (int t = 5460; t < 7200; t += 45) {
            shots.add(sweep(fire(EmitShape.fan(18, 33.75f), finale, t, 0, 3), 0f, 0f, 0f)
                    .withJitter(10f));
        }
        return timeline(shots, 7200);
    }
}
