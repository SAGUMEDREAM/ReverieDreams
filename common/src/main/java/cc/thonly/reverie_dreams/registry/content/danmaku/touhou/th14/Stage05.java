package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th14;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionPayload;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th14.Th14PatternSupport.*;

/**
 * TH14 st05mbs.ecl and st05bs.ecl (Seija Kijin). Numbers are the ECL spell ids.
 * Source: E:/Temp/rd-ecl-continuation-20261116/th14/ecl/st05mbs.ecl.txt, st05bs.ecl.txt.
 */
final class Stage05 {
    private static final String SEIJA = "kijin_seijia";
    // bid 20 color 1 = golden ring; bid 25/26 color 3 and bid 30 color 1 are the big balls.
    private static final int GOLD = 0xD8B050;
    private static final int VIOLET = 0x9C6CC8;
    private static final int RED = 0xC03C4C;
    private static final int RED_BRIGHT = 0xE85C5C;

    private Stage05() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(5, 66, false, SEIJA, "欺符「逆针击」", 1500, () -> reverseNeedle(false)),
                card(5, 67, true, SEIJA, "欺符「逆针击」", 1500, () -> reverseNeedle(true)),
                card(5, 70, false, SEIJA, "逆符「镜之国的弹幕」", 2100, () -> mirrorCountry(false)),
                card(5, 71, true, SEIJA, "逆符「Evil in the Mirror」（镜中的邪恶）", 2100, () -> mirrorCountry(true)),
                card(5, 74, false, SEIJA, "逆符「天地有用」", 2100, () -> heavenEarth(false)),
                card(5, 75, true, SEIJA, "逆符「天下翻覆」", 2100, () -> heavenEarth(true)),
                card(5, 78, false, SEIJA, "逆弓「天壤梦弓」", 2100, () -> dreamBow(false)),
                card(5, 79, true, SEIJA, "逆弓「天壤梦弓的诏敕」", 2100, () -> dreamBow(true)),
                card(5, 82, false, SEIJA, "逆转「Reverse Hierarchy」（阶级反转）", 3000, () -> reverseHierarchy(false)),
                card(5, 83, true, SEIJA, "逆转「Change Air Brave」（变革空勇士）", 3000, () -> reverseHierarchy(true)));
    }

    /** Golden coin that decelerates 3.0 -> 1.0 px/frame and splits into needles after 15 frames. */
    private static DanmakuBulletSpec coin(int splitCount) {
        return shot(BALL, 0.56f, GOLD, 70,
                MotionSegment.accelerate(0, -0.012f), MotionSegment.linear(ticks(30)),
                MotionSegment.split(ticks(15), MotionPayload.split(NEEDLE, GOLD, 0.7f,
                        splitCount, 60f, 0.19f, 40)));
    }

    /**
     * MBossCard1 (st05mbs L155): spell 64, 1500f, both difficulties. Each synchronous call fires
     * thirteen coin pairs on a 60-degree spread, nine pairs whose spread narrows 60 -> 8.6
     * degrees (6-frame cadence, aimed upward), then a 60 (Normal) / 40 (Hard) coin shower at
     * -90 +/- 30 degrees; every coin splits into 2 (Normal) / 3 (Hard) needles. The call repeats
     * every ~312 frames.
     */
    private static DanmakuPattern reverseNeedle(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        int callLength = hard ? 212 : 192;
        int split = hard ? 3 : 2;
        var bullet = coin(split);
        for (int start = 150; start + callLength <= 1500; start += callLength + 120) {
            addNeedleCall(shots, bullet, start, hard);
        }
        return timeline(shots, 1500);
    }

    private static void addNeedleCall(List<DanmakuEmitter> shots, DanmakuBulletSpec bullet,
                                      int start, boolean hard) {
        shots.add(sweep(fire(EmitShape.fan(2, 60f), bullet, start, 78, 6), -70f, 0f, 0f));
        for (int k = 0; k < 9; k++) {
            float spread = 60f - k * 8.57f;
            shots.add(sweep(fire(EmitShape.fan(2, Math.max(2f, spread)), bullet, start + 78 + k * 6, 0, 6),
                    -70f, 0f, 0f));
        }
        int shower = start + 132;
        int window = Math.min(hard ? 80 : 60, 1500 - shower);
        if (window > 0) {
            shots.add(sweep(fire(EmitShape.aim(), bullet, shower, window, hard ? 2 : 1),
                    -70f, 0f, 0f).withJitter(30f));
        }
    }

    /**
     * BossCard1 (st05bs L501): spell 68, 2100f. BossCard1_at rains single big violet balls from
     * thirteen top-screen columns (x = -192..128 px stepping 32) aimed downward on a 3-frame
     * cadence, speed 1.4 px/frame growing toward 2.4 as %C climbs. Hard adds BossCard1_at_l:
     * twin diagonal streams from x = +/-192 down-left/down-right every 16 frames.
     */
    private static DanmakuPattern mirrorCountry(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var rain = shot(BUBBLE, 0.26f, VIOLET, 70, MotionSegment.accelerate(0, 0.0005f));
        for (int column = 0; column < 13; column++) {
            float x = -9f + column * 1.55f;
            shots.add(sweep(fire(EmitShape.aim(), rain, 300 + column * 3, Math.min(1800, 2100 - 300 - column * 3), 39), 62f, 0f, 0f)
                    .withOffset(new Vec3(x, 2.6, 0)).withJitter(6f));
        }
        if (hard) {
            var diagonal = shot(BUBBLE, 0.26f, VIOLET, 80, MotionSegment.accelerate(0, 0.0005f));
            for (float side : new float[]{-9f, 9f}) {
                shots.add(sweep(fire(EmitShape.aim(), diagonal, 300, 1800, 16), 55f, side < 0 ? -22f : 22f,
                        side < 0 ? -22f : 22f).withOffset(new Vec3(side, 2.2, 0)).withJitter(12f));
            }
        }
        return timeline(shots, 2100);
    }

    /**
     * BossCard2 (st05bs L644): spell 72, 2100f. BossCard2_at sweeps three-ball violet fans
     * across the same thirteen top columns (7-frame cadence Normal, 10 Hard) with a slow
     * 0.6 deg/frame random drift; Hard mirrors it with BossCard2_at_l rising from the screen
     * bottom (y = 448 px) at -90 degrees.
     */
    private static DanmakuPattern heavenEarth(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        int step = hard ? 10 : 7;
        var fall = shot(BUBBLE, 0.26f, VIOLET, 80,
                MotionSegment.accelerate(0, 0.0005f), MotionSegment.rotate(0, 0.6f));
        for (int column = 0; column < 13; column++) {
            float x = -9f + column * 1.55f;
            int columnStart = 300 + column * step;
            shots.add(sweep(fire(EmitShape.fan(3, 30f), fall, columnStart, Math.min(1800, 2100 - columnStart), step * 2),
                    62f, 0f, 0f).withOffset(new Vec3(x, 2.6, 0)).withJitter(5f));
        }
        if (hard) {
            var rise = shot(BUBBLE, 0.26f, VIOLET, 80,
                    MotionSegment.accelerate(0, 0.0005f), MotionSegment.rotate(0, -0.6f));
            for (int column = 0; column < 13; column++) {
                float x = -9f + column * 1.55f;
                int columnStart = 300 + column * step;
                shots.add(sweep(fire(EmitShape.fan(3, 30f), rise, columnStart, Math.min(1800, 2100 - columnStart), step * 2),
                        -62f, 0f, 0f).withOffset(new Vec3(x, 0.3, 3.4)).withJitter(5f));
            }
        }
        return timeline(shots, 2100);
    }

    /**
     * BossCard3 (st05bs L838): spell 76, 2100f. MBossCard3_at fires twenty sweeps (Normal) of
     * six red 32 px arrows each: the column x walks -192 -> -80 and 192 -> 80 in 40 px steps on
     * a 2-frame cadence, arrows leave upward (-90 +/- 10-15 degrees, 1.4 px/frame) and every
     * arrow carries six chained 30-way split triggers. Ported as one 10-way ring split per arrow.
     */
    private static DanmakuPattern dreamBow(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var arrow = shot(BALL, 0.26f, RED, 60,
                MotionSegment.accelerate(0, -0.01f), MotionSegment.linear(ticks(30)),
                MotionSegment.split(ticks(15), MotionPayload.split(BALL, RED, 0.7f,
                        10, 360f, 0.15f, 45)));
        int sweeps = hard ? 22 : 20;
        for (int column = 0; column < 12; column++) {
            float x = column < 6 ? -9f + column * 2.5f : 9f - (column - 6) * 2.5f;
            shots.add(sweep(fire(EmitShape.aim(), arrow, 150 + column * 2, sweeps * 24, 24),
                    -75f, 0f, 0f).withOffset(new Vec3(x, 1.6, 0)).withJitter(hard ? 15f : 10f));
        }
        return timeline(shots, 2100);
    }

    /**
     * BossCard4 (st05bs L910): spell 80, 3000f. BossCard4_at bursts sixty 12 (Normal) / 14 (Hard)
     * bullet sheets of bright red balls on a 2-frame cadence (base yaw walking 1 degree per shot,
     * direction re-rolled per 240-frame burst) with the 2.0 -> 1.0 -> 3.0 -> 2.0 speed dance.
     * BossCard4_at2 answers with aimed violet volleys every 180 (Normal) / 100 (Hard) frames,
     * four shots at 20-frame spacing, Hard three-wide on a 45-degree fan.
     */
    private static DanmakuPattern reverseHierarchy(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var red = shot(BALL, 0.37f, RED_BRIGHT, 80,
                MotionSegment.setSpeed(ticks(30), 0.19f),
                MotionSegment.accelerate(ticks(30), 0.018f), MotionSegment.linear(ticks(81)));
        for (int burst = 0, start = 150; start < 3000; burst++, start += 240) {
            float sign = (burst & 1) == 0 ? 1f : -1f;
            shots.add(sweep(fire(EmitShape.fan(hard ? 14 : 12, (hard ? 13 : 11) * 1.875f), red,
                    start, 120, 2), 0f, 0f, sign * 60f).withJitter(4f));
        }
        var violet = shot(BALL, 0.47f, VIOLET, 70,
                MotionSegment.accelerate(0, -0.007f), MotionSegment.linear(ticks(20)));
        int volleyGap = hard ? 100 : 180;
        for (int start = 240; start < 3000; start += volleyGap) {
            int window = Math.min(62, 3000 - start);
            var shape = hard ? EmitShape.fan(3, 90f) : EmitShape.aim();
            shots.add(fire(shape, violet, start, window, 20).withJitter(6f));
        }
        return timeline(shots, 3000);
    }
}
