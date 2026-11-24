package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th14;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
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
 * TH14 st03mbs.ecl (Kagerou midboss) and st03bs.ecl. Numbers are the ECL spell ids.
 * Source: E:/Temp/rd-ecl-continuation-20261116/th14/ecl/st03mbs.ecl.txt, st03bs.ecl.txt.
 */
final class Stage03 {
    private static final String KAGEROU = "imaizumi_kagerou";
    // bid 18 (14x14 round) color 3, bid 19 (teardrop) color 1, bid 25 (30x30 ball) color 0.
    private static final int VIOLET = 0x9C6CC8;
    private static final int RED = 0xC03C4C;
    private static final int WHITE = 0xE8E8F0;

    private Stage03() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(3, 27, true, KAGEROU, "牙符「月下的犬齿」", 2040, Stage03::moonFang),
                card(3, 30, false, KAGEROU, "变身「Triangle Fang」（三角齿）", 2400, () -> fangDash(false)),
                card(3, 31, true, KAGEROU, "变身「Star Fang」（星形齿）", 2400, () -> fangDash(true)),
                card(3, 34, false, KAGEROU, "咆哮「Strange Roar」（陌生的咆哮）", 2400, () -> strangeRoar(false)),
                card(3, 35, true, KAGEROU, "咆哮「满月的远吠」", 2400, () -> strangeRoar(true)),
                card(3, 38, false, KAGEROU, "狼符「Star Ring Pounce」（星环猛扑）", 3000, () -> pounce(false)),
                card(3, 39, true, KAGEROU, "天狼「High-Speed Pounce」（高速猛扑）", 3000, () -> pounce(true)));
    }

    /** Violet round bullet that spawns still and reaches 1.0 px/frame in about 50 frames. */
    private static DanmakuBulletSpec fangBullet(float curvePerTick) {
        var segments = new ArrayList<MotionSegment>();
        segments.add(MotionSegment.accelerate(0, 0.011f));
        segments.add(MotionSegment.linear(ticks(50)));
        if (curvePerTick != 0f) {
            // one tick after linear so the continuous segments do not collide on the same tick
            segments.add(MotionSegment.rotate(ticks(51), curvePerTick));
        }
        return shot(BALL, 0.0f, VIOLET, 70, segments.toArray(new MotionSegment[0]));
    }

    /**
     * MBossCard1 (st03mbs L245): spell3 26, 2040f, Hard/Lunatic only (st03mbs L171 gates the card
     * behind !HL). Every 150 frames two wolves launch at the player angle -/+ 7 degrees (speed
     * 1.7 -> 4.0 px/frame) and each drops $I0 bullets on a 3-frame cadence from a 32 px random
     * offset, accelerating 0 -> 3.0 px/frame in 50 frames. $I0 starts 20 and grows to 24.
     */
    private static DanmakuPattern moonFang() {
        var shots = new ArrayList<DanmakuEmitter>();
        int count = 20;
        for (int cycle = 150; cycle < 2040; cycle += 150) {
            var bullet = shot(BALL, 0.0f, VIOLET, Math.max(20, (2040 - cycle) / 3),
                    MotionSegment.accelerate(0, 0.033f), MotionSegment.linear(ticks(50)));
            for (float side : new float[]{-7f, 7f}) {
                shots.add(sweep(fire(EmitShape.aim(), bullet, cycle, 60, 3), 0f, side, side)
                        .withOffset(new Vec3(0, 1.4, 0)).withJitter(14f));
            }
            if (count < 24) {
                count++;
            }
        }
        return timeline(shots, 2040);
    }

    /**
     * BossCard1 (st03bs L342): spell 28, 2400f. The boss dashes along fixed waypoints (three per
     * phase on Normal, five on Hard) and fires violet bullets on a 3-frame cadence while dashing:
     * Normal three bullets 30 degrees apart, Hard three bullets 45 degrees apart, both curving
     * 0.02 rad/frame with a sign that flips each phase. Bullet speed ramps 0 -> 1.4 px/frame.
     */
    private static DanmakuPattern fangDash(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        float spread = hard ? 45f : 30f;
        int[] starts = hard
                ? new int[]{150, 180, 220, 260, 300, 480, 510, 550, 590, 630}
                : new int[]{150, 190, 250, 440, 480, 540};
        int cycle = hard ? 660 : 580;
        int[] windows = hard
                ? new int[]{27, 36, 36, 36, 27, 27, 36, 36, 36, 27}
                : new int[]{36, 57, 36, 36, 57, 36};
        for (int phase = 0, base = 150; base < 2400; phase++, base += cycle) {
            float curve = (phase & 1) == 0 ? 3.4f : -3.4f;
            var bullet = fangBullet(curve);
            for (int i = 0; i < starts.length; i++) {
                int start = base + (starts[i] - 150);
                if (start >= 2400) {
                    continue;
                }
                int window = Math.min(windows[i], 2400 - start);
                float x = (i % 3 == 0) ? -7f : (i % 3 == 1 ? 7f : 0f);
                shots.add(sweep(fire(EmitShape.fan(3, spread), bullet, start, window, 3),
                        0f, 0f, 0f).withOffset(new Vec3(x, 1.2, 0)).withJitter(8f));
            }
        }
        return timeline(shots, 2400);
    }

    /**
     * BossCard2 (st03bs L596): spell 32, 2400f. Every 320 frames 18 (Normal) / 20 (Hard) wolves
     * fan out in 20.0/18.4 degree steps and each drops $I0 bullets (16 -> 18 Normal, 20 -> 24
     * Hard) on a 4/3-frame cadence from a 32 px random offset. Bullets leave backward from the
     * wolf facing, accelerate 0 -> 1.0 px/frame and curve 0.025-0.03 rad/frame, flipping sign
     * per cycle.
     */
    private static DanmakuPattern strangeRoar(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        int wolves = hard ? 20 : 18;
        float wolfSpacing = hard ? 18.4f : 20f;
        int count = hard ? 20 : 16;
        int cap = hard ? 24 : 18;
        int step = hard ? 3 : 4;
        for (int phase = 0, cycle = 150; cycle < 2400; phase++, cycle += 320) {
            float curve = (phase & 1) == 0 ? 5.2f : -5.2f;
            var bullet = fangBullet(curve).withLifetime(Math.max(20, (2400 - cycle) / 3));
            for (int wolf = 0; wolf < wolves; wolf++) {
                float yaw = wolf * wolfSpacing + 180f;
                shots.add(sweep(fire(EmitShape.aim(), bullet, cycle, 80, step), 0f, yaw, yaw)
                        .withOffset(new Vec3(0, 1.4, 0)).withJitter(10f));
            }
            if (count < cap) {
                count++;
            }
        }
        return timeline(shots, 2400);
    }

    /**
     * BossCard3 (st03bs L762): spell 36, 3000f. During roughly eight dashes per 1150-frame cycle
     * the boss bursts eight red teardrops (30 degrees apart, random base, 0.4 -> 1.0 px/frame,
     * strong hook) and twice fires 8/12-way white big-ball fans braking 2.6 -> 0.5-1.5 px/frame.
     * Four bottom dashes answer with 20 (Normal) / 40 (Hard) white rings braking to 0.5/0.8.
     */
    private static DanmakuPattern pounce(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        Vec3[] spots = {new Vec3(-6.9, -1.0, 0), new Vec3(6.9, -0.9, 0), new Vec3(-11.2, -0.9, 0),
                new Vec3(11.2, -0.9, 0), new Vec3(0, -0.8, 0), new Vec3(10.0, -0.6, 0),
                new Vec3(-10.0, -0.5, 0), new Vec3(11.2, -0.9, 0)};
        int[] burstOffsets = {0, 120, 300, 420, 540, 780, 900, 1080};
        int[] ringOffsets = {330, 480, 1080, 1230};
        int[] fanOffsets = {600, 1750};
        for (int phase = 0, base = 150; base < 3000; phase++, base += 1150) {
            float hook = (phase & 1) == 0 ? 2.0f : -2.0f;
            var red = shot(RICE, 0.19f, RED, 60, MotionSegment.rotate(0, hook));
            for (int i = 0; i < burstOffsets.length; i++) {
                int start = base + burstOffsets[i];
                if (start >= 3000) {
                    continue;
                }
                shots.add(sweep(fire(EmitShape.fan(8, 210f), red, start, 0, 3),
                        0f, i * 43f, i * 43f).withOffset(spots[i]).withJitter(10f));
            }
            var ring = shot(BUBBLE, 0.49f, WHITE, 60,
                    MotionSegment.accelerate(0, hard ? -0.017f : -0.02f), MotionSegment.linear(ticks(20)));
            for (int offset : ringOffsets) {
                int start = base + offset;
                if (start >= 3000) {
                    continue;
                }
                shots.add(fire(EmitShape.ring(hard ? 40 : 20, 1.0f, true), ring, start, 0, 3)
                        .withOffset(new Vec3(0, -0.6, 0)));
            }
            var fan = shot(BUBBLE, 0.49f, WHITE, 60,
                    MotionSegment.accelerate(0, -0.02f), MotionSegment.linear(ticks(20)));
            for (int offset : fanOffsets) {
                int start = base + offset;
                if (start >= 3000) {
                    continue;
                }
                shots.add(sweep(fire(EmitShape.fan(hard ? 12 : 8, hard ? 330f : 210f), fan, start, 0, 3),
                        0f, offset * 0.031f, offset * 0.031f).withOffset(new Vec3(0, -0.6, 0)));
            }
        }
        return timeline(shots, 3000);
    }
}
