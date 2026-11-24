package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th13;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th13.Th13PatternSupport.*;

/**
 * TH13 st07mbs.ecl: Nue Houjuu, the Extra midboss. Her three "Unknown" cards fire wobbling
 * unidentified bullets whose trails keep changing (ins_612/etEx2 splits, etExSet waver modes).
 * Source: E:/Temp/rd-ecl-continuation-20261116/th13/ecl/st07mbs.ecl.txt.
 */
final class ExtraMidboss {
    private static final String NUE = "houjuu_nue";
    // Sampled from th13 bullet.anm: spr24 = 0x00FFFF cyan, spr6 = 0x8697FF pale violet,
    // spr30 = 0xFFB900 gold, spr18 = 0xFF0000 red.
    private static final int CYAN = 0x00FFFF;
    private static final int PALE_VIOLET = 0x8697FF;
    private static final int GOLD = 0xFFB900;
    private static final int RED = 0xFF0000;

    private ExtraMidboss() {}

    static List<TouhouSpell> spells() {
        return List.of(
                extraCard(7, 106, NUE, "未知「轨道不明之鬼火」", 4200, ExtraMidboss::unknownOrb),
                extraCard(7, 107, NUE, "未知「形态不明之空鱼」", 4200, ExtraMidboss::unknownFish),
                extraCard(7, 108, NUE, "未知「原理不明之妖怪玉」", 4200, ExtraMidboss::unknownSphere));
    }

    // MBossCard1 (137-226): every 210f cycle 20 volleys of 12-count aimed wisps 10f apart
    // (1.3 px/f, 1.875 deg/f curl) whose heading wavers (etExSet modes 2-5 with 8f/60f windows,
    // 193-196); an ambient 16-ring rains every 60f (at2 205-226).
    private static DanmakuPattern unknownOrb() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var wisp = shot(BALL, 0.13f, CYAN, 200, MotionSegment.rotate(ticks(8), 0.63f));
        for (int v = 0; v < 10; v++) {
            emitters.add(fire(EmitShape.ring(12, 0f, true), wisp, v * 10, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0).withJitter(30f));
        }
        var rain = shot(BALL, 0.1f, PALE_VIOLET, 170);
        emitters.add(fire(EmitShape.ring(16, 0f, true), rain, 60, 0, 1).withAim(EmitAim.TARGET, 0, 0));
        return loop(emitters, 4200, 60, 70);
    }

    // MBossCard2 (228-294): 20 volleys of 13-count sky-fish fans (2.0 px/f) whose heading
    // zig-zags 15 deg per volley (288, 0.2618 rad); each fish re-emits three times at 16f
    // intervals with rising speed 3.0/3.5/4.0 px/f (etEx2 279-283).
    private static DanmakuPattern unknownFish() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var fish = shot(BULLET, 0.2f, GOLD, 190, splitRise(0.3f, 1), splitRise(0.35f, 2), splitRise(0.4f, 3));
        for (int v = 0; v < 10; v++) {
            float yaw = 15f * v;
            var cue = fire(EmitShape.fan(13, 10f), fish, v * 10, 0, 1).withAim(EmitAim.TARGET, 0, 0);
            emitters.add(sweep(cue, 0, yaw, yaw + 60f));
        }
        return loop(emitters, 4200, 60, 100);
    }

    private static MotionSegment splitRise(float speed, int at) {
        return MotionSegment.split(ticks(16) * at, new cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionPayload(
                BULLET, GOLD, 0.85f, speed, 1, 12f, 110));
    }

    // MBossCard3 (296-403): 24 volleys of single orbs (2.2 px/f) marching 15 deg per volley from
    // -180 deg (361-369); each orb spawns ring sub-bullets every 16f at 2.2+dist*0.1 px/f
    // (etExSet2 modes 6/10, 365-366). at2 adds 2-count fans from the player heading (375-403).
    private static DanmakuPattern unknownSphere() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var orb = shot(BALL, 0.22f, RED, 220,
                MotionSegment.split(ticks(16), new cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionPayload(
                        PELLET, PALE_VIOLET, 0.7f, 0.14f, 6, 360f, 90)));
        for (int v = 0; v < 24; v++) {
            float yaw = -180f + 15f * v;
            emitters.add(sweep(fire(EmitShape.aim(), orb, v * 6, 0, 1).withAim(EmitAim.TARGET, 0, 0), 0, yaw, yaw));
        }
        var fan = shot(PELLET, 0.18f, PALE_VIOLET, 160);
        emitters.add(fire(EmitShape.fan(2, 6f), fan, 120, 0, 1).withAim(EmitAim.TARGET, 0, 0));
        return loop(emitters, 4200, 60, 150);
    }
}
