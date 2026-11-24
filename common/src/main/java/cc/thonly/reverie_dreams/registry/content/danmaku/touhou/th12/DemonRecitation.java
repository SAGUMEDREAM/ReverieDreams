package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th12;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuLaser;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th12.Th12PatternSupport.*;

/** The layered BossCard4 callbacks of TH12 stage06.ecl. */
final class DemonRecitation {
    private static final int DEADLINE = 4200;
    private static final int VIOLET = 0x9A63E0;
    private static final int BLUE = 0x678DE7;
    private static final int RUST = 0xDE6D54;

    private DemonRecitation() {}

    static DanmakuPattern pattern(boolean hard, List<Vec3> corners) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var lasers = new ArrayList<DanmakuLaser>();
        addBeams(lasers, corners);
        addCornerFire(emitters, corners);
        addSideRain(emitters);
        addLateLayers(emitters, hard);
        return timeline(emitters, lasers, DEADLINE);
    }

    private static void addBeams(List<DanmakuLaser> lasers, List<Vec3> corners) {
        for (int i = 0; i < corners.size(); i++) {
            Vec3 offset = corners.get(i);
            // Initial BossCard4Laser waits 120f, then fires for 60f.
            lasers.add(laser(ticks(120), 0, 0, 22f, 0.82f, 40, 20, 0f, VIOLET).withOffset(offset));
            // BossCard4Laser2 reverses at ±π/48 around its initial heading.
            float rate = (i == 0 || i == 3) ? -0.2f : 0.2f;
            lasers.add(laser(ticks(2640), 0, 0, 22f, 0.73f, 40, ticks(DEADLINE - 2760), rate, BLUE)
                    .withOffset(offset).withSweepLimit(3.75f));
        }
    }

    private static void addCornerFire(List<DanmakuEmitter> emitters, List<Vec3> corners) {
        var bolt = shot(BULLET, 0.44f, VIOLET, 87, MotionSegment.setSpeed(30, 0.28f));
        for (int frame = 240, wave = 0; frame < DEADLINE; frame += frame < 390 ? 3 : 12, wave++) {
            boolean opening = frame < 390;
            float width = opening ? Math.max(24f, 115f - wave * 2f) : 120f;
            for (int i = 0; i < corners.size(); i++) {
                float yaw = opening ? 0 : (i == 0 || i == 3 ? 1 : -1)
                        * (float) (14 * Math.sin((frame - 390) / 115.0));
                var cue = sweep(fire(EmitShape.fan(opening ? 2 : 3, width), bolt, frame, 0, 3),
                        0, yaw, yaw).withOffset(corners.get(i));
                emitters.add(cue);
            }
        }
    }

    private static void addSideRain(List<DanmakuEmitter> emitters) {
        var bolt = shot(BALL, 0.17f, RUST, 114);
        for (int side : new int[]{-1, 1}) {
            var rain = fire(EmitShape.aim(), bolt, side == -1 ? 440 : 450, DEADLINE - 451, 20)
                    .withOffset(new Vec3(side * 5.6, 0.65, 0.3)).withJitter(8f);
            emitters.add(spread(sweep(rain, 10f, side * 24f, side * 24f), 1.7f, 3.2f, 0, 0.86f));
        }
    }

    private static void addLateLayers(List<DanmakuEmitter> emitters, boolean hard) {
        var star = shot(ReverieDreams.id("star"), 0.24f, BLUE, 121);
        var small = shot(BALL, 0.20f, VIOLET, 116, MotionSegment.accelerate(36, hard ? 0.008f : 0.006f));
        emitters.add(fire(EmitShape.fan(hard ? 3 : 1, 90f), star, 2640, DEADLINE - 2641, hard ? 100 : 120));
        for (int start = 2760; start < DEADLINE; start += 180) {
            for (int side : new int[]{-1, 1}) {
                for (int index = 0; index < (hard ? 7 : 6); index++) {
                    float yaw = side * (-45f + index * (hard ? 30f : 36f));
                    emitters.add(sweep(fire(EmitShape.aim(), small, start + 5 * index + (side < 0 ? 80 : 0),
                            0, 3), -3, yaw, yaw).withJitter(9f));
                }
            }
        }
        var ring = shot(PELLET, 0.24f, RUST, 89);
        emitters.add(fire(EmitShape.ring(16, 0.6f, true), ring, 3240, DEADLINE - 3241, 20));
        var blade = shot(BULLET, 0.26f, BLUE, 117);
        int step = hard ? 3 : 4;
        for (int frame = 2640, index = 0; frame < DEADLINE; index++, frame += step) {
            int sweepSteps = hard ? 20 : 10;
            int wave = index % (sweepSteps * 2);
            float yaw = -90 + (180f / sweepSteps) * (wave < sweepSteps ? wave : 2 * sweepSteps - wave);
            emitters.add(sweep(fire(EmitShape.aim(), blade, frame, 0, 3), 8, yaw, yaw));
        }
    }
}
