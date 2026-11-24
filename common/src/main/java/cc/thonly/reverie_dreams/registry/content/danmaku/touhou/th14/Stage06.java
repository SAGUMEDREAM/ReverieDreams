package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th14;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th14.Th14PatternSupport.*;

/**
 * TH14 st06bs.ecl (Raiko Horikawa). Numbers are the ECL spell ids; the last card runs 7200f.
 * Source: E:/Temp/rd-ecl-continuation-20261116/th14/ecl/st06bs.ecl.txt.
 */
final class Stage06 {
    private static final String RAIKO = "horikawa_raiko";
    private static final int BLUE = 0x6464E8;
    private static final int GREEN = 0x58D868;
    private static final int TEAL = 0x4CB4B8;
    private static final int VIOLET = 0x9C6CC8;
    private static final int RED_BRIGHT = 0xE85C5C;
    private static final int RED = 0xC03C4C;
    // BossCard6_at cycles bid 9 through colors 2/14/13/10/8/6/4 - the seven rainbow bands.
    private static final int[] RAINBOW = {0xE85C5C, 0xE0B070, 0xE8E060, 0x58D868, 0x6CE8E8, 0x6464E8, 0xB85CD0};

    private Stage06() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(6, 86, false, RAIKO, "小弹「小人的道路」", 2700, () -> littleOnesRoad(false)),
                card(6, 87, true, RAIKO, "小弹「小人的荆棘路」", 2700, () -> littleOnesRoad(true)),
                card(6, 90, false, RAIKO, "小槌「变大吧」", 2700, () -> growBig(false)),
                card(6, 91, true, RAIKO, "小槌「变得更大吧」", 2700, () -> growBig(true)),
                card(6, 94, false, RAIKO, "妖剑「辉针剑」", 2700, () -> shiningSword(false)),
                card(6, 95, true, RAIKO, "妖剑「辉针剑」", 2700, () -> shiningSword(true)),
                card(6, 98, false, RAIKO, "小槌「你给我变大吧」", 2700, () -> growBigYou(false)),
                card(6, 99, true, RAIKO, "小槌「你给我变大吧」", 2700, () -> growBigYou(true)),
                card(6, 102, false, RAIKO, "「进击的小人」", 3600, () -> advancingOnes(false)),
                card(6, 103, true, RAIKO, "「Wall of Issun」（一寸之壁）", 3600, () -> advancingOnes(true)),
                card(6, 106, false, RAIKO, "「Hop-o'-My-Thumb Seven」（七个小拇指）", 7200, () -> thumbSeven(false)),
                card(6, 107, true, RAIKO, "「七个一寸法师」", 7200, () -> thumbSeven(true)));
    }

    /**
     * BossCard1 (st06bs L743): spell 84, 2700f. BossCard1_at loops 128-way rings of tiny blue
     * rings (2.8-degree spacing, 1.5 px/frame) whose interval $F shrinks 76 -> 30 by three per
     * ring while the base yaw wanders within +/-8 degrees; each iteration also fires one green
     * aimed bullet that brakes to 0 and restarts after 40 frames. Ring density is reduced to
     * 24 ways for the Minecraft field.
     */
    private static DanmakuPattern littleOnesRoad(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var ring = shot(PELLET, 0.28f, BLUE, 80);
        var chaser = shot(PELLET, 0.28f, GREEN, 70,
                MotionSegment.setSpeed(ticks(13), 0.02f),
                MotionSegment.accelerate(ticks(13), 0.01f), MotionSegment.linear(ticks(41)));
        int interval = 76;
        int floor = 30;
        float yaw = 0f;
        for (int t = 150; t < 2700; ) {
            shots.add(sweep(fire(EmitShape.ring(24, 0.8f, true), ring, t, 0, 3), 0f, yaw, yaw)
                    .withOffset(new Vec3(0, 1.2, 0)));
            shots.add(sweep(fire(EmitShape.aim(), chaser, t, 0, 3), 0f, yaw, yaw)
                    .withOffset(new Vec3(0, 1.2, 0)).withJitter(5f));
            t += interval;
            if (interval > floor) {
                interval -= 3;
            }
            yaw = (yaw + 14f) % 360f;
        }
        return timeline(shots, 2700);
    }

    /**
     * BossCard2 (st06bs L884): spell 88, 2700f. Two BossCard2_at streams spiral outward (base
     * yaw stepping -/+20 degrees per 20-frame volley) firing paired red and blue rounds that
     * grow to 6x/4x scale and accelerate 0.5 -> 6.6/4.4 px/frame over 120 frames; BossCard2_at2
     * adds aimed violet growers (scale 0.3 -> 2.0) whose interval shrinks 60 -> 30 frames.
     * Growth is ported as bubble-sized bullets with the original acceleration ramp.
     */
    private static DanmakuPattern growBig(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var red = shot(BUBBLE, 0.09f, RED_BRIGHT, 110, MotionSegment.accelerate(0, 0.021f));
        var blue = shot(BUBBLE, 0.09f, BLUE, 110, MotionSegment.accelerate(0, 0.017f));
        shots.add(sweep(fire(EmitShape.fan(2, 40f), red, 150, 2550, 20), 0f, 180f, 180f - 2550f)
                .withOffset(new Vec3(0, 1.2, 0)).withJitter(6f));
        shots.add(sweep(fire(EmitShape.fan(2, 40f), blue, 150, 2550, 20), 0f, 0f, 2550f)
                .withOffset(new Vec3(0, 1.2, 0)).withJitter(6f));
        var aimed = shot(BUBBLE, 0.09f, VIOLET, 100,
                MotionSegment.accelerate(0, hard ? 0.016f : 0.013f));
        for (int start = 630; start < 2700; start += 50) {
            shots.add(fire(hard ? EmitShape.fan(3, 30f) : EmitShape.aim(), aimed, start, 0, 3)
                    .withJitter(5f));
        }
        return timeline(shots, 2700);
    }

    /**
     * BossCard3 (st06bs L1079): spell 92, 2700f, one declaration per difficulty pair.
     * BossCard3_at sweeps 32-bullet 60-degree fans of violet rounds (speed 5.0 braking to 0.5
     * px/frame) every 16 frames while the base yaw steps -10 degrees per shot. BossCard3_at2
     * (every 600 frames) fires growing teal volleys whose volley count climbs 3 -> 16.
     * Fan density is reduced to 12 bullets (28 degrees) per sweep.
     */
    private static DanmakuPattern shiningSword(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var slash = shot(BALL, 0.94f, VIOLET, 60,
                MotionSegment.accelerate(0, -0.042f), MotionSegment.linear(ticks(20)));
        shots.add(sweep(fire(EmitShape.fan(12, 28f), slash, 220, 2480, 16), 0f, 180f, 180f - 1550f)
                .withOffset(new Vec3(0, 1.2, 0)).withJitter(4f));
        var grower = shot(BALL, 0.19f, TEAL, 80,
                MotionSegment.accelerate(ticks(30), 0.02f), MotionSegment.linear(ticks(31)));
        for (int call = 0, start = 120; start < 2700; call++, start += 630) {
            int volleys = Math.min(3 + call, 8);
            shots.add(fire(EmitShape.fan(6, 50f), grower, start, Math.min(volleys * 10, 2700 - start), 10).withJitter(6f));
        }
        return timeline(shots, 2700);
    }

    /**
     * BossCard4 (st06bs L1232): spell 96, 2700f. BossCard4_at fires sixteen-bullet 310-degree
     * sheets of blue ovals (speed 1.0 -> 0.5 px/frame) every 60 frames with a zigzag base
     * (four shots down-walk, eight up-walk, four down-walk, 10 degrees each). BossCard4_at2
     * loops growing teal fans from three mirrored et slots (0/±32 degrees) whose volley count
     * grows 3 -> 8/10 and acceleration rises 4.8 -> 6.5 px/frame.
     */
    private static DanmakuPattern growBigYou(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var sheet = shot(BALL, 0.19f, BLUE, 70,
                MotionSegment.accelerate(0, -0.005f), MotionSegment.linear(ticks(20)));
        float yaw = 270f;
        for (int t = 720; t < 2700; t += 60) {
            shots.add(sweep(fire(EmitShape.ring(16, 1.2f, true), sheet, t, 0, 3), 0f, yaw, yaw)
                    .withOffset(new Vec3(0, 1.2, 0)));
            int phase = ((t - 720) / 60) % 16;
            boolean up = phase >= 4 && phase < 12;
            yaw += (up ? 1 : -1) * 10f;
        }
        var teal = shot(BALL, 0.28f, TEAL, 90,
                MotionSegment.accelerate(ticks(20), 0.014f), MotionSegment.linear(ticks(21)));
        int volleyCap = hard ? 10 : 8;
        for (int cycle = 0, start = 210; start < 2700; cycle++, start += 60) {
            int volleys = Math.min(3 + cycle, volleyCap);
            shots.add(fire(EmitShape.fan(6, 74f), teal, start, Math.min(volleys * 10, 2700 - start), 10).withJitter(6f));
        }
        return timeline(shots, 2700);
    }

    /**
     * BossCard5 (st06bs L1436): spell 100, 3600f. BossCard5_at answers every $I2 frames with a
     * dense wall (4x48 bullets at 5.14-degree spacing, 1.0 px/frame) plus four advance bullets;
     * $I2 shrinks 160 -> 30 (Normal one per ring, Hard two). Walls are reduced to 24 ways and
     * the advance wave to four aimed violet rounds.
     */
    private static DanmakuPattern advancingOnes(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var wall = shot(BALL, 0.19f, RED_BRIGHT, 90);
        var advance = shot(BALL, 0.19f, VIOLET, 80);
        int interval = hard ? 140 : 160;
        float yaw = -45f;
        for (int t = 150; t < 3600; ) {
            shots.add(sweep(fire(EmitShape.ring(24, 1.0f, true), wall, t, 0, 3), 0f, yaw, yaw)
                    .withOffset(new Vec3(0, 1.2, 0)));
            shots.add(fire(EmitShape.aim(), advance, t, 0, 3).withJitter(20f));
            t += Math.max(30, interval);
            interval -= hard ? 2 : 1;
            yaw = (yaw - 45f + 360f) % 360f;
        }
        return timeline(shots, 3600);
    }

    /**
     * BossCard6 (st06bs L1597): spell 104, 7200f. BossCard6_at fires all seven rainbow bands
     * (11/12 bullets, 5.14-degree spacing) back to back and waits $I2 (100 frames, 50 after the
     * 5400-frame mark) while the base yaw advances -308.6 degrees per cycle. Six familiar sprites
     * join in three pairs (life-gated in the original; ported at fixed times) running aimed
     * 7-bullet streams every 90/50/120 frames with speed 1.0 -> 2.5/3.5 px/frame ramps.
     */
    private static DanmakuPattern thumbSeven(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        int count = hard ? 12 : 11;
        for (int band = 0; band < 7; band++) {
            var band1 = shot(BALL, 0.19f, RAINBOW[band], 90);
            shots.add(sweep(fire(EmitShape.fan(count, (count - 1) * 5.14f), band1, 180, 7020, 100),
                    0f, band * 51.4f, band * 51.4f - 308.6f * 70f).withOffset(new Vec3(0, 1.4, 0)));
        }
        Vec3[][] pairs = {{new Vec3(-4, 1.2, 0), new Vec3(4, 1.2, 0)},
                {new Vec3(-8, 1.4, 0), new Vec3(8, 1.4, 0)},
                {new Vec3(-10, 1.6, 0), new Vec3(10, 1.6, 0)}};
        int[] pairStart = {180, 2400, 4800};
        int[] cadence = {90, 50, 120};
        for (int pair = 0; pair < pairs.length; pair++) {
            float speed = pair == 0 ? 0.19f : pair == 1 ? 0.19f : 0.28f;
            float boost = pair == 0 ? 0.014f : pair == 1 ? 0.024f : 0.006f;
            var stream = shot(BALL, speed, RAINBOW[(pair + 2) % 7], 80,
                    MotionSegment.accelerate(ticks(40), boost), MotionSegment.linear(ticks(41)));
            for (Vec3 spot : pairs[pair]) {
                shots.add(fire(EmitShape.fan(7, 31f), stream, pairStart[pair], 7200 - pairStart[pair],
                        cadence[pair]).withOffset(spot).withJitter(pair == 0 ? 10f : 2f));
            }
        }
        return timeline(shots, 7200);
    }
}
