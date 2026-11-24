package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th11;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuLaser;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th11.Th11PatternSupport.*;

/** TH11 stage01.ecl: midboss, then Yamame. Numbers are the one-based spell-practice entries. */
final class Stage01 {
    private static final String YAMAME = "kurodani_yamame";
    private static final int RED = 0xE65C60;
    private static final int BLUE = 0x597EDB;
    private static final int PURPLE = 0xB557CB;
    private static final int GREEN = 0x74D854;

    private Stage01() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(1, 1, true, "kisume", "怪奇「钓瓶落之怪」", 2160, Stage01::bucketDropper),
                card(1, 4, false, YAMAME, "罠符「捕捉之网」", 2100, () -> web(false)),
                card(1, 5, true, YAMAME, "蜘蛛「石窟的蜘蛛巢」", 2100, () -> web(true)),
                card(1, 8, false, YAMAME, "瘴符「瘴气四溢」", 2400, () -> miasma(false)),
                card(1, 9, true, YAMAME, "瘴气「原因不明的热病」", 2400, () -> miasma(true)));
    }

    // MBossCard1_at/_at2: after frame 150, a five-frame pellet cadence and a separate 60-frame laser cadence.
    // Convert the original downward fall to a shallow descending sheet and give the beam a visible warning.
    private static DanmakuPattern bucketDropper() {
        var falling = shot(BULLET, 0.11f, BLUE, 110,
                MotionSegment.freeze(ticks(180), ticks(30)),
                MotionSegment.setSpeed(ticks(210), 0.32f));
        var shots = new ArrayList<DanmakuEmitter>();
        var beams = new ArrayList<DanmakuLaser>();
        for (int frame = 150, wave = 0; frame < 2160; frame += 60, wave++) {
            float side = (wave & 1) == 0 ? -1f : 1f;
            shots.add(sweep(fire(EmitShape.aim(), falling, frame, 57, 6)
                    .withOffset(new Vec3(side * 1.6, 2.1, 0)), 13f, -14f, 14f));
            beams.add(new DanmakuLaser(ticks(frame), 8f, side * 16f, 16f, 0.32f, 10, 9, 0f, BLUE));
        }
        return timeline(shots, 2160).withLasers(beams);
    }

    // BossCard1_at: 46 staggered launches, alternating turn sign and sprite color every 240 frames.
    private static DanmakuPattern web(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        for (int side : new int[]{-1, 1}) {
            for (int lane = 0; lane < 2; lane++) {
                int start = 150 + (side > 0 ? 240 : 0) + lane * 9;
                var bead = shot(BALL, 0.14f, side < 0 ? BLUE : GREEN, 94,
                        MotionSegment.freeze(ticks(120) - lane * 5, ticks(20)),
                        MotionSegment.turn(ticks(140) - lane * 5, side * (hard ? 78f : 66f), UP),
                        MotionSegment.setSpeed(ticks(140) - lane * 5, hard ? 0.37f : 0.29f));
                shots.add(sweep(fire(EmitShape.fan(hard ? 4 : 2, hard ? 16f : 9f),
                                bead, start, 42, 3).withOffset(new Vec3(side * 0.45, lane * 0.65, 0)),
                        lane == 0 ? 1f : 9f, side * 5f, side * 53f));
            }
        }
        return loop(shots, 2100, 150, 480);
    }

    // BossCard2_at: alternating purple/red pairs. Hard also invokes BossCard2At from both sides.
    private static DanmakuPattern miasma(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        for (int wave = 0; wave < 4; wave++) {
            int start = 150 + wave * 380;
            int side = (wave == 0 || wave == 3) ? 1 : -1;
            int color = (wave & 1) == 0 ? PURPLE : RED;
            addMiasmaPair(shots, start, side, color, Vec3.ZERO);
            if (hard) {
                int otherColor = color == PURPLE ? RED : PURPLE;
                addMiasmaPair(shots, start, -side, otherColor, new Vec3(-2.8, 0.55, 0));
                addMiasmaPair(shots, start, -side, otherColor, new Vec3(2.8, 0.55, 0));
            }
        }
        return loop(shots, 2400, 150, 1520);
    }

    private static void addMiasmaPair(List<DanmakuEmitter> shots, int start, int side, int color, Vec3 offset) {
        var outgoing = shot(BALL, 0.17f, color, 92, MotionSegment.accelerate(ticks(30), 0.008f),
                MotionSegment.linear(ticks(90)));
        var returning = shot(BALL, 0.17f, color, 116, MotionSegment.freeze(ticks(60), ticks(60)),
                MotionSegment.turn(ticks(120), 168f * side, UP), MotionSegment.setSpeed(ticks(120), 0.36f));
        shots.add(sweep(fire(EmitShape.aim(), outgoing, start, 132, 3).withOffset(offset),
                4f, side * 5f, side * 46f));
        shots.add(sweep(fire(EmitShape.aim(), returning, start + 6, 126, 6).withOffset(offset),
                9f, side * 5f, side * 46f));
    }
}
