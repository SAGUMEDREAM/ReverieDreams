package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th11;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuScatter;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th11.Th11PatternSupport.*;

/**
 * TH11 stage05mboss.ecl and stage05boss.ecl. Numbers are one-based practice entries.
 * Source: E:/Temp/rd-ecl-continuation-20261116/th11/ecl/stage05*.ecl.txt.
 */
final class Stage05 {
    private static final String RIN = "kaenbyou_rin";
    private static final int RED = 0xE05260;
    private static final int BLUE = 0x5D8ADB;
    private static final int PURPLE = 0xB75BD0;
    private static final int GREEN = 0x62D958;
    private static final int WHITE = 0xF0EBF5;

    private Stage05() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(5, 120, false, RIN, "猫符「Cat's Walk」（猫的步伐）", 2220, () -> catsWalk(false)),
                card(5, 121, true, RIN, "猫符「怨灵猫乱步」", 2220, () -> catsWalk(true)),
                card(5, 124, false, RIN, "咒精「Zombie Fairy」（僵尸妖精）", 3600, () -> zombieFairy(false)),
                card(5, 125, true, RIN, "咒精「怨灵凭依妖精」", 3600, () -> zombieFairy(true)),
                card(5, 128, false, RIN, "恨灵「Spleen Eater」（脾脏蛀食者）", 3000, () -> spleenEater(false)),
                card(5, 129, true, RIN, "尸灵「食人怨灵」", 3000, () -> spleenEater(true)),
                card(5, 132, false, RIN, "赎罪「旧地狱的针山」", 3000, () -> needleMountain(false)),
                card(5, 133, true, RIN, "赎罪「古时之针与痛楚的怨灵」", 3000, () -> needleMountain(true)),
                card(5, 136, false, RIN, "「死灰复燃」", 3600, () -> deadAsh(false)),
                card(5, 137, true, RIN, "「小恶灵复活」", 3600, () -> deadAsh(true)));
    }

    /**
     * MBossCard1/MBossCard2_at: Normal has 16-way rings, two waves per source, and a
     * 60-frame source cadence. Hard has 24-way rings, four waves, a 40-frame cadence,
     * and a faster speed ramp. Original radii are 64/80/96/112 px.
     */
    private static DanmakuPattern catsWalk(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        int sourceInterval = hard ? 40 : 60;
        int waves = hard ? 4 : 2;
        int count = hard ? 24 : 16;
        for (int source = 0, start = 90; start < 2220; source++, start += sourceInterval) {
            int lane = switch (source % 3) {
                case 0 -> 0;
                case 1 -> hard ? 1 : -1;
                default -> hard ? -1 : 1;
            };
            float height = 1.20f + (source % 8) * 0.24f;
            for (int wave = 0; wave < waves; wave++) {
                float radius = 3.00f + wave * 0.65f;
                float phase = (source * 47f + wave * 30f) % 360f;
                var bullet = shot(BALL, hard ? 0.24f : 0.20f, RED, 110);
                shots.add(sweep(fire(EmitShape.ring(count, radius, true), bullet,
                        start + wave * 8, 0, 3)
                        .withOffset(new Vec3(lane * 2.8f, height, 0)), 0f, phase, phase));
            }
        }
        return timeline(shots, 2220);
    }

    /**
     * BossCard1At: one Normal or two Hard bullets per cue. The base angle advances
     * 7.5 degrees each shot; Normal fires every 6 frames and Hard every 3 frames.
     * Original speed samples 1.2 to 1.7 px/frame.
     */
    private static DanmakuPattern zombieFairy(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        int interval = hard ? 3 : 6;
        for (int start = 270, wave = 0; start < 3600; start += interval, wave++) {
            float phase = wave * 7.5f;
            var bullet = shot(BULLET, 0.24f, RED, 100);
            var shape = hard ? EmitShape.fan(2, 18f) : EmitShape.aim();
            var emitter = fire(shape, bullet, start, 0, 3)
                    .withOffset(new Vec3(1.7f, 0.35f, 0))
                    .withScatter(new DanmakuScatter(0, 0, 0, 0.71f));
            shots.add(sweep(emitter, 0f, phase, phase));
        }
        return timeline(shots, 3600);
    }

    /**
     * BossCard2: eight phantom sources around the player per cycle. Each emits forty
     * shots every two frames. Normal repeats every 520 frames; Hard every 420 and
     * uses the faster ECL acceleration branch.
     */
    private static DanmakuPattern spleenEater(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        int interval = hard ? 480 : 580;
        for (int cycleStart = 120; cycleStart < 3000; cycleStart += interval) {
            for (int phantom = 0; phantom < 8; phantom++) {
                float angle = phantom * 45f;
                double radians = Math.toRadians(angle);
                var offset = new Vec3(Math.cos(radians) * 2.6, 0.55, Math.sin(radians) * 2.6);
                var bullet = shot(BULLET, hard ? 0.26f : 0.20f,
                        hard ? PURPLE : BLUE, 112);
                var emitter = fire(EmitShape.aim(), bullet, cycleStart + 12, 80, 2)
                        .withOffset(offset);
                shots.add(sweep(emitter, 0f, angle, angle + 180f));
            }
        }
        return timeline(shots, 3000);
    }

    /**
     * BossCard3: seven arm cues, then two opposite needle spirals. Normal has 20
     * needles per wave and Hard 24. Each spiral has fourteen three-frame waves and
     * a 5.625-degree phase step; the arm helpers contribute five 16-way bursts each.
     */
    private static DanmakuPattern needleMountain(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        int cycle = hard ? 660 : 700;
        for (int cycleStart = 60; cycleStart < 3000; cycleStart += cycle) {
            for (int arm = 0; arm < 7; arm++) {
                int armStart = cycleStart + arm * 60;
                for (int helper = 0; helper < 5; helper++) {
                    float direction = arm * 30f - 90f;
                    double radians = Math.toRadians(direction + helper * 12f);
                    var offset = new Vec3(Math.cos(radians) * (0.9f + helper * 0.35f),
                            0.45f, Math.sin(radians) * (0.9f + helper * 0.35f));
                    var bullet = shot(BALL, 0.30f, GREEN, 108);
                    shots.add(sweep(fire(EmitShape.ring(16, 0.75f, true), bullet,
                            armStart + 240, 0, 3).withOffset(offset), 0f,
                            direction + helper * 12f, direction + helper * 12f));
                }
            }

            int wait = hard ? 80 : 100;
            int first = cycleStart + 420 + wait;
            addNeedleSpiral(shots, hard, first, 1f, PURPLE);
            addNeedleSpiral(shots, hard, first + wait, -1f, BLUE);
        }
        return timeline(shots, 3000);
    }

    private static void addNeedleSpiral(List<DanmakuEmitter> shots, boolean hard,
                                         int start, float sign, int color) {
        int count = hard ? 24 : 20;
        for (int wave = 0; wave < 14; wave++) {
            float radius = 2.70f + wave * 1.30f;
            float phase = wave * 5.625f * sign;
            var bullet = shot(BULLET, hard ? 0.26f : 0.22f, color, 115);
            shots.add(sweep(fire(EmitShape.ring(count, radius, true), bullet,
                    start + wave * 3, 0, 3), 0f, phase, phase));
        }
    }

    /**
     * BossCard4: sixteen side spirit sources. Their firing interval decreases from
     * 90 to 40 frames; Normal emits one pellet and Hard two. The shared stream begins
     * at frame 240, fires every ten frames, and advances 6.405 degrees per shot.
     */
    private static DanmakuPattern deadAsh(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        for (int source = 0; source < 16; source++) {
            int pair = source / 2;
            int side = (source & 1) == 0 ? -1 : 1;
            float x = side * (4.0f - (source % 3) * 0.25f);
            float y = 3.80f - pair * 0.52f;
            int interval = 90;
            for (int frame = 300 + pair * 8; frame < 3600; frame += interval) {
                if (interval > 40) {
                    interval--;
                }
                float pitch = y > 1.8f ? 12f : -12f;
                var bullet = shot(PELLET, hard ? 0.20f : 0.16f, WHITE, 105);
                var shape = hard ? EmitShape.fan(2, 16f) : EmitShape.aim();
                shots.add(sweep(fire(shape, bullet, frame, 0, 3)
                        .withOffset(new Vec3(x, y, 0)), pitch, 0f, 0f));
            }
        }

        for (int frame = 240; frame < 3600; frame += 10) {
            int wave = (frame - 240) / 10;
            float phase = wave * 6.405f;
            var bullet = shot(BULLET, 0.20f, RED, 110);
            var emitter = fire(EmitShape.fan(2, 14f), bullet, frame, 0, 3)
                    .withOffset(new Vec3(1.6f, 0.45f, 0))
                    .withScatter(new DanmakuScatter(0, 0, 0, 0.60f));
            shots.add(sweep(emitter, 0f, phase, phase));
        }
        return timeline(shots, 3600);
    }
}
