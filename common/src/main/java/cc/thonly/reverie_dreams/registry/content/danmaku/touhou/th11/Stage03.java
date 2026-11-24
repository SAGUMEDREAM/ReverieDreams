package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th11;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuScatter;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th11.Th11PatternSupport.*;

/**
 * TH11 stage03.ecl. Numbers are the one-based spell practice entries.
 * Source: E:/Temp/rd-ecl-continuation-20261116/th11/ecl/stage03.ecl.txt.
 */
final class Stage03 {
    private static final String YUGI = "hoshiguma_yugi";
    private static final int RED = 0xE05260;
    private static final int PURPLE = 0xB75BD0;
    private static final int BLUE = 0x5D8ADB;
    private static final int GREEN = 0x62D958;
    private static final int WHITE = 0xF0EBF5;

    private Stage03() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(3, 28, false, YUGI, "鬼符「怪力乱神」", 2040, () -> monstrousStrength(false)),
                card(3, 29, true, YUGI, "鬼符「怪力乱神」", 2040, () -> monstrousStrength(true)),
                card(3, 32, false, YUGI, "怪轮「地狱之苦轮」", 2580, () -> wheelOfSuffering(false)),
                card(3, 33, true, YUGI, "枷符「罪人不释之枷」", 2100, () -> wheelOfSuffering(true)),
                card(3, 36, false, YUGI, "力业「大江山岚」", 2400, () -> mountainStorm(false)),
                card(3, 37, true, YUGI, "力业「大江山颪」", 2400, () -> mountainStorm(true)),
                card(3, 40, false, YUGI, "四天王奥义「三步必杀」", 3000, () -> threeStepSureKill(false)),
                card(3, 41, true, YUGI, "四天王奥义「三步必杀」", 3000, () -> threeStepSureKill(true)));
    }

    /**
     * MBossCard1: eight Normal or ten Hard satellite sources at a 186-frame cadence.
     * Each source fires a short spiral, then expires. The original source motion is
     * represented by its final ring position.
     */
    private static DanmakuPattern monstrousStrength(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        int sources = hard ? 10 : 8;
        float step = hard ? 36f : 45f;
        for (int source = 0; source < sources; source++) {
            int start = 186 + source * 186;
            float sourceAngle = (hard ? -18f : -22.5f) + source * step;
            double radians = Math.toRadians(sourceAngle);
            var offset = new Vec3(Math.cos(radians) * 2.5, 0.65, Math.sin(radians) * 2.5);
            var spiral = shot(BALL, 0.20f, RED, 110,
                    MotionSegment.rotate(ticks(40), 27f));
            var emitter = fire(EmitShape.aim(), spiral, start, 156, 2)
                    .withOffset(offset);
            shots.add(sweep(emitter, 0f, sourceAngle - 180f, sourceAngle + 180f));
        }
        return timeline(shots, 2040);
    }

    /**
     * BossCard1: six wheel satellites, followed by the central 20-way spray.
     * Normal lasts 2580 frames and fires every 50; Hard lasts 2100 and fires every 5.
     * The ECL angular step is 15 degrees and its base advances by 2 degrees per wave.
     */
    private static DanmakuPattern wheelOfSuffering(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        int frames = hard ? 2100 : 2580;
        for (int source = 0; source < 6; source++) {
            float sourceAngle = 150f + source * 60f;
            double radians = Math.toRadians(sourceAngle);
            var offset = new Vec3(Math.cos(radians) * 3.4, 0.55, Math.sin(radians) * 3.4);
            var satelliteShot = shot(BALL, hard ? 0.34f : 0.28f, PURPLE, 105);
            var shape = hard ? EmitShape.fan(4, 45f) : EmitShape.aim();
            shots.add(sweep(fire(shape, satelliteShot, 60, 0, 3).withOffset(offset),
                    0f, sourceAngle, sourceAngle));
        }

        int interval = hard ? 5 : 50;
        var centralShot = shot(BULLET, hard ? 0.36f : 0.32f, RED, 92);
        for (int start = 280; start < frames; start += interval) {
            int wave = (start - 280) / interval;
            float phase = wave * 2f;
            shots.add(sweep(fire(EmitShape.fan(20, 285f), centralShot, start, 0, 3),
                    2f, phase - 142.5f, phase - 142.5f));
        }
        return timeline(shots, frames);
    }

    /**
     * BossCard2: one 240-frame side barrage in Normal. Hard swaps the active side
     * every 400 frames (240 firing plus 160 transition). The original has three
     * speed tiers at 8/4.5/3 px per frame from distinct height lanes.
     */
    private static DanmakuPattern mountainStorm(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        int fastStep = 3;
        int middleStep = hard ? 10 : 24;
        int slowStep = hard ? 18 : 40;
        for (int start = 120; start < 2400; start += fastStep) {
            if (!hard && start >= 360) {
                break;
            }
            if (start >= 360 && (start - 120) % 400 != 0 && (start - 120) % 400 < 240) {
                continue;
            }
            int side = hard ? ((start - 120) / 400) % 2 == 0 ? 1 : -1 : 1;
            if ((start - 120) % fastStep == 0) {
                addStormLane(shots, start, side, 0.25f, 0.34f, 100, 78.75f);
            }
            if (start >= 180 && (start - 180) % middleStep == 0) {
                addStormLane(shots, start, side, 1.00f, 0.22f, 110, 67.5f);
                addStormLane(shots, start, side, 1.70f, 0.22f, 110, 67.5f);
            }
            if (start >= 210 && (start - 210) % slowStep == 0) {
                addStormLane(shots, start, side, 2.45f, 0.16f, 125, 67.5f);
                addStormLane(shots, start, side, 3.15f, 0.16f, 125, 67.5f);
            }
        }
        return timeline(shots, 2400);
    }

    private static void addStormLane(List<DanmakuEmitter> shots, int start, int side,
                                      float height, float speed, int lifetime, float eclAngle) {
        float yaw = side > 0 ? eclAngle : -eclAngle;
        var bullet = shot(BALL, speed, RED, lifetime);
        shots.add(sweep(fire(EmitShape.aim(), bullet, start, 0, 3)
                .withOffset(new Vec3(side * 5.2f, height, 0)), 0f, yaw, yaw));
    }

    /**
     * BossCard3: the ECL's 770-frame six-part cycle. All six parts use sixteen-way
     * outward rings; Normal steps 45 degrees, Hard steps 22.5 degrees and adds a
     * bounded radial offset. Original one- and two-frame cadences are subsampled
     * to three- and six-frame emitters for the 20 TPS runtime.
     */
    private static DanmakuPattern threeStepSureKill(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        addRingBursts(shots, hard, 0, 20, 0.60f, 0.08f, 6, 0.5f, PURPLE);
        addRingBursts(shots, hard, 120, 20, 2.50f, 0.14f, 6, -0.5f, GREEN);
        addRingBursts(shots, hard, 240, 60, 5.30f, 0.07f, 3, 22.5f, BLUE);
        addRingBursts(shots, hard, 500, 20, 0.60f, 0.08f, 6, -1f, RED);
        addRingBursts(shots, hard, 590, 20, 2.50f, 0.14f, 6, 1f, WHITE);
        addRingBursts(shots, hard, 710, 60, 5.30f, 0.07f, 3, 22.5f, RED);
        return loop(shots, 3000, 60, 770);
    }

    private static void addRingBursts(List<DanmakuEmitter> shots, boolean hard, int start,
                                       int waves, float radius, float radiusStep,
                                       int interval, float phaseStep, int color) {
        float angleStep = hard ? 22.5f : 45f;
        for (int wave = 0; wave < waves; wave++) {
            int frame = start + wave * interval;
            float phase = wave * phaseStep;
            var bullet = shot(BALL, 0.28f, color, 115);
            var emitter = fire(EmitShape.multiRing(16, 1, radius + radiusStep * wave, true,
                            angleStep, 0f, 0f),
                    bullet, frame, 0, 3);
            if (hard) {
                emitter = emitter.withScatter(new DanmakuScatter(0.18f, 0.18f, 0f, 1f));
            }
            shots.add(sweep(emitter, 0f, phase, phase));
        }
    }
}
