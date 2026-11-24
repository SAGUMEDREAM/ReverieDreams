package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th14;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
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
 * TH14 st04bs.ecl (Benben Tsukumo). Numbers are the ECL spell ids.
 * Source: E:/Temp/rd-ecl-continuation-20261116/th14/ecl/st04bs.ecl.txt.
 */
final class Stage04 {
    private static final String BENBEN = "item.reverie_dreams.spellcard.character.tsukumo_benben";
    // bid 38/39/40 are the note sprites (bullet5.png); bid 42 is its violet twin.
    private static final int WHITE = 0xE8E8F0;
    private static final int VIOLET = 0xB557CB;
    private static final int PALE = 0xE7DFF5;
    private static final int LASER_N = 0xE0B070;
    private static final int LASER_H = 0xE85C5C;

    private Stage04() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(4, 42, false, BENBEN, "平曲「祇园精舍的钟声」", 2400, () -> gionBell(false)),
                card(4, 43, true, BENBEN, "平曲「祇园精舍的钟声」", 2400, () -> gionBell(true)),
                card(4, 46, false, BENBEN, "怨灵「无耳芳一」", 2400, () -> homelessGhost(false)),
                card(4, 47, true, BENBEN, "怨灵「平家的大怨灵」", 2400, () -> homelessGhost(true)),
                card(4, 50, false, BENBEN, "乐符「邪恶的五线谱」", 3000, () -> evilScore(false)),
                card(4, 51, true, BENBEN, "乐符「凶恶的五线谱」", 3000, () -> evilScore(true)));
    }

    /** NOTE bullet that flies out, brakes to a stop, then swirls away (the hanging-note motif). */
    private static DanmakuBulletSpec hangingNote(int color) {
        return shot(NOTE, 0.37f, color, 110,
                MotionSegment.accelerate(0, -0.0185f), MotionSegment.linear(ticks(60)),
                MotionSegment.setSpeed(ticks(60), 0.15f),
                MotionSegment.rotate(ticks(61), 6f));
    }

    /**
     * BossCard1 (st04bs L879): spell 40, 2400f. Every 80 (Normal) / 60 (Hard) frames after the
     * charge, BossCard1_at fires 16 staggered note volleys: each volley is $I0 = 28 (Normal) /
     * 34 (Hard) notes on a 30-degree random-base ring at 2.0 px/frame braking to 0 in 60 frames,
     * then re-accelerating 0.2 + i x 0.22 toward an angle that walks 10.5 degrees per volley.
     * The two note sprites (bid 39/40) alternate via $I1.
     */
    private static DanmakuPattern gionBell(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        int period = hard ? 240 : 260;
        int count = hard ? 34 : 28;
        for (int call = 0, start = 120; start < 2400; call++, start += period) {
            var note = hangingNote((call & 1) == 0 ? WHITE : VIOLET);
            shots.add(fire(EmitShape.ring(count, 1.0f, true), note, start, 0, 3)
                    .withOffset(new Vec3(0, 1.0, 0)).withJitter(10f));
        }
        return timeline(shots, 2400);
    }

    /**
     * BossCard2 (st04bs L966): spell 44, 2400f. Phase one spawns fifteen expanding phantom rings
     * (6 per ring on Normal, radius 32 + 28k px) whose members take turns dashing at the player;
     * ported as homing pale ghosts launched radially every 10 frames. From frame 210 the boss
     * also loops five aimed note shots (Normal one, Hard three at 20 degrees) every 180 frames,
     * speed 3.0 px/frame braking and restarting.
     */
    private static DanmakuPattern homelessGhost(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var ghost = shot(BALL, 0.06f, PALE, 90,
                MotionSegment.accelerate(0, 0.007f), MotionSegment.linear(ticks(30)),
                MotionSegment.homing(ticks(30), 1.5f, 30));
        for (int wave = 0; wave < 15; wave++) {
            int start = 120 + wave * 10;
            shots.add(fire(EmitShape.ring(hard ? 6 : 6, 2.0f + wave * 1.75f, true), ghost, start, 0, 3)
                    .withOffset(new Vec3(0, 1.0, 0)));
        }
        var note = shot(NOTE, 0.56f, WHITE, 80,
                MotionSegment.accelerate(0, -0.0065f), MotionSegment.linear(ticks(30)),
                MotionSegment.setSpeed(ticks(30), 0.03f),
                MotionSegment.accelerate(ticks(31), 0.012f));
        for (int start = 210; start < 2400; start += 180) {
            int window = Math.min(42, 2400 - start);
            var shape = hard ? EmitShape.fan(3, 20f) : EmitShape.aim();
            shots.add(fire(shape, note, start, window, 10).withJitter(4f));
        }
        return timeline(shots, 2400);
    }

    /**
     * BossCard3 (st04bs L1118): spell 48, 3000f. Every 270-frame cycle two interleaved note
     * streams run for 30 (Normal) / 40 (Hard) iterations at a 2-frame cadence: a white note
     * (1.5-3.0 px/frame) and a violet note, both orbiting a point above their spawn
     * (radius 1.2 / 0.6 blocks in the etExSet2 orbit parameters). BossCard3_at2 adds five
     * curving lasers per cycle (Hard mirrors them), speed 9 px/frame hooking -8.4 deg/frame
     * before recovering; laserTiming delays the beam 120 frames.
     */
    private static DanmakuPattern evilScore(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var beams = new ArrayList<DanmakuLaser>();
        for (int cycle = 0, start = 150; start < 3000; cycle++, start += 270) {
            int iterations = hard ? 40 : 30;
            var white = shot(NOTE, 0.42f, WHITE, 100,
                    MotionSegment.orbit(ticks(20), (cycle & 1) == 0 ? 45f : -45f, new Vec3(0, 1.2, 0)));
            var violet = shot(NOTE, 0.3f, VIOLET, 100,
                    MotionSegment.orbit(ticks(20), (cycle & 1) == 0 ? -45f : 45f, new Vec3(0, 0.6, 0)));
            shots.add(fire(EmitShape.aim(), white, start + 60, iterations * 2, 2).withJitter(14f));
            shots.add(fire(EmitShape.aim(), violet, start + 61, iterations * 2, 2).withJitter(14f));
            float base = -80f;
            for (int i = 0; i < 5; i++) {
                beams.add(laser(ticks(start + i * 2), 6f, base - i * 20f, 20f, 0.9f, 40, 50, -2.5f,
                        hard ? LASER_H : LASER_N));
                if (hard) {
                    beams.add(laser(ticks(start + i * 2), 6f, -base + i * 20f, 20f, 0.9f, 40, 50, 2.5f,
                            LASER_H));
                }
            }
        }
        return timeline(shots, beams, 3000);
    }
}
