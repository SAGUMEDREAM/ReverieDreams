package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th15;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuLaser;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuMath;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th15.Th15PatternSupport.*;

/** TH15 st03mbs/st03bs.ecl: Doremy Sweet's midboss and boss Normal/Hard cards. */
final class Stage03 {
    private static final String DOREMY = "doremy_sweet";
    private static final Identifier BALL = ReverieDreams.id("ball");
    private static final Identifier STAR = ReverieDreams.id("star");
    private static final Identifier BULLET = ReverieDreams.id("bullet");
    private static final int INDIGO = 0x5B6EE1;
    private static final int NAVY = 0x3949AB;
    private static final int VIOLET = 0x9A8BD8;
    private static final int CRIMSON = 0xC23B4E;

    private Stage03() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(3, 22, false, DOREMY, "梦符「绯色的噩梦」", 1320, () -> crimsonNightmare(false)),
                card(3, 22, true, DOREMY, "梦符「绯色的压迫噩梦」", 1320, () -> crimsonNightmare(true)),
                card(3, 26, false, DOREMY, "梦符「蓝色的愁梦」", 2220, () -> melancholyDream(false)),
                card(3, 26, true, DOREMY, "梦符「蓝色的愁三重梦」", 2220, () -> melancholyDream(true)),
                card(3, 30, false, DOREMY, "梦符「刈安色的迷梦」", 2400, () -> bewilderingDream(false)),
                card(3, 30, true, DOREMY, "梦符「刈安色的错综迷梦」", 2400, () -> bewilderingDream(true)),
                card(3, 34, false, DOREMY, "梦符「捕梦网」", 3000, () -> dreamCatcher(false)),
                card(3, 34, true, DOREMY, "梦符「苍色的捕梦网」", 3000, () -> dreamCatcher(true)),
                card(3, 38, false, DOREMY, "月符「藏青色的狂梦」", 3000, () -> navyMadnessDream(false)),
                card(3, 38, true, DOREMY, "月符「藏青色的狂梦」", 3000, () -> navyMadnessDream(true)));
    }

    // st03mbs.ecl MBossCard1_at (266-346): sources march around a 128px circle
    // near the player (11.25° per shot), each firing a fast 8-way (N) / 10-way
    // (H) converge volley. Cycles alternate direction every 240f.
    private static DanmakuPattern crimsonNightmare(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var nightmare = shot(BALL, 0.8f, CRIMSON, 55);
        int shots = hard ? 24 : 16;
        for (int cycle = 0; 63 + cycle * 240 < 1290; cycle++) {
            float turn = cycle % 2 == 0 ? 11.25f : -11.25f;
            for (int i = 0; i < shots; i++) {
                float yaw = 90 + turn * i;
                Vec3 offset = DanmakuMath.direction(0, yaw).scale(3.2).add(0, 0.25, 0);
                var cue = sweep(fire(EmitShape.fan(hard ? 10 : 8, 60f), nightmare, 63 + cycle * 240 + i * 3, 0, 1), 0, 0, 0);
                emitters.add(cue.withOffset(offset));
            }
        }
        return timeline(emitters, 1320);
    }

    // st03bs.ecl BossCard1_at (761-859): two spiral arms grow outward from
    // around the player (128px + 8px/shot), marching 11.25° apart per shot; big
    // balls creep at 0.3 then dash at 2.0 after 60f, followed by a narrow arrow
    // pair. Hard (BossCard1_at2, 861-963) adds two fixed-angle arm volleys.
    private static DanmakuPattern melancholyDream(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var orb = shot(BALL, 0.03f, INDIGO, 120, MotionSegment.setSpeed(20, 0.2f));
        var arrow = shot(BULLET, 0.15f, VIOLET, 110);
        for (int cycle = 0; 96 + cycle * 168 < 2190; cycle++) {
            float base = cycle * 37f;
            for (int i = 0; i < 16; i++) {
                for (int side = 0; side < 2; side++) {
                    float yaw = base + (side == 0 ? -1 : 1) * (90 + i * 11.25f);
                    double radius = 2.7 + i * 0.17;
                    Vec3 offset = DanmakuMath.direction(0, yaw).scale(radius).add(0, 0.2, 0);
                    var cue = sweep(fire(EmitShape.fan(4, 30f), orb, 96 + cycle * 168 + i * 6, 0, 1), 0, yaw, yaw);
                    emitters.add(cue.withOffset(offset));
                    var tail = sweep(fire(EmitShape.fan(2, 20f), arrow, 96 + cycle * 168 + i * 6, 0, 1), 0, yaw, yaw);
                    emitters.add(tail.withOffset(offset));
                }
            }
            if (hard) {
                for (int extra = 0; extra < 2; extra++) {
                    float yaw = base + 90 + (extra == 0 ? 5 : -5);
                    Vec3 offset = DanmakuMath.direction(0, yaw).scale(2.7).add(0, 0.2, 0);
                    emitters.add(sweep(fire(EmitShape.fan(4, 30f), orb, 96 + cycle * 168 + 120 + extra * 12, 0, 1),
                            0, yaw, yaw).withOffset(offset));
                }
            }
        }
        return timeline(emitters, 2220);
    }

    // st03bs.ecl BossCard2_at (1044-1178): from points on a fixed 128px circle
    // marching 11.25° per shot, big balls fly toward the opposite marching angle
    // so the streams cross over the player. Big balls creep at 0.3 and dash at
    // 1.0 (N) / 1.8 (H) after 60f; a slower color follows. Cycles flip the march
    // sign every 200f (N) / 100f (H).
    private static DanmakuPattern bewilderingDream(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        int period = hard ? 198 : 99;
        int shots = hard ? 30 : 25;
        int step = hard ? 3 : 6;
        for (int cycle = 0; 96 + cycle * period < 2340; cycle++) {
            float turn = (cycle % 2 == 0 ? 1 : -1) * 11.25f;
            for (int i = 0; i < shots; i++) {
                float yaw = turn * i;
                Vec3 offset = DanmakuMath.direction(0, yaw).scale(3.2).add(0, 0.2, 0);
                var main = shot(BALL, 0.03f, NAVY, 130, MotionSegment.setSpeed(20, hard ? 0.18f : 0.1f));
                var cue = sweep(fire(EmitShape.fan(hard ? 8 : 4, 30f), main, 96 + cycle * period + i * step, 0, 1),
                        0, yaw + 180, yaw + 180);
                emitters.add(cue.withOffset(offset));
                if (i % 3 == 0) {
                    var slow = shot(BALL, 0.03f, INDIGO, 130, MotionSegment.setSpeed(20, hard ? 0.15f : 0.1f));
                    var second = sweep(fire(EmitShape.fan(hard ? 5 : 4, 24f), slow,
                            96 + cycle * period + i * step, 0, 1), 0, yaw, yaw);
                    emitters.add(second.withOffset(offset));
                }
            }
        }
        return timeline(emitters, 2400);
    }

    // st03bs.ecl BossCard3 (1230-1334) + BossCard3_at (1336-1450): every 420f the
    // boss fires a wheel of 50 short lasers (wait 2f between shots) around a 64px
    // circle centred on the player, marching 3.883° per shot and reversing each
    // wheel; the lasers self-rotate in the original, adapted to a constant 2°/t
    // curl. The star companion (et1) fires on H/Lunatic only, one per laser.
    private static DanmakuPattern dreamCatcher(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var lasers = new ArrayList<DanmakuLaser>();
        var star = shot(STAR, 0.12f, INDIGO, 90);
        for (int volley = 0; 21 + volley * 140 + 50 < 1000; volley++) {
            float march = volley % 2 == 0 ? -3.883f : 3.883f;
            for (int i = 0; i < 50; i++) {
                float yaw = 90 + march * i;
                Vec3 offset = DanmakuMath.direction(0, yaw).scale(2.13).add(0, 0.25, 0);
                int at = 21 + volley * 140 + i;
                lasers.add(laser(at, 0, yaw, 4.3f, 0.53f, 10, 20, volley % 2 == 0 ? -2f : 2f, VIOLET)
                        .withOffset(offset));
                if (hard) {
                    emitters.add(fire(EmitShape.aim(), star, at * 3, 0, 1).withOffset(offset));
                }
            }
        }
        return timeline(emitters, lasers, 3000);
    }

    // st03bs.ecl BossCard4_at (1573-1655): two creep-then-dash spiral arms around
    // the player — a fine arm marching 3° (E: 6°) per shot and a coarse arm
    // marching 20°, both growing 0.7px per shot. Cycles repeat every ~480f and
    // Hard fires the fine arm twice (BossCard4 1537-1565).
    private static DanmakuPattern navyMadnessDream(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        for (int cycle = 0; 72 + cycle * 480 < 2940; cycle++) {
            int armA = hard ? 2 : 1;
            for (int a = 0; a < armA; a++) {
                for (int i = 0; i < 60; i++) {
                    float yaw = 90 + cycle * 53 + (a == 0 ? 1 : -1) * -(i * 3f);
                    double radius = 2.1 + i * 0.023;
                    Vec3 offset = DanmakuMath.direction(0, yaw).scale(radius).add(0, 0.2, 0);
                    var fine = shot(BULLET, 0.03f, NAVY, 130, MotionSegment.setSpeed(20, 0.15f));
                    var cue = sweep(fire(EmitShape.aim(), fine, 72 + cycle * 480 + i * 3, 0, 1), 0, yaw, yaw);
                    emitters.add(cue.withOffset(offset));
                }
            }
            for (int i = 0; i < 40; i++) {
                float yaw = 90 + cycle * 53 + i * 20f;
                double radius = 2.1 + i * 0.023;
                Vec3 offset = DanmakuMath.direction(0, yaw).scale(radius).add(0, 0.2, 0);
                var coarse = shot(BALL, 0.03f, INDIGO, 130, MotionSegment.setSpeed(20, 0.15f));
                var cue = sweep(fire(EmitShape.aim(), coarse, 72 + cycle * 480 + 480 + i * 3, 0, 1), 0, yaw, yaw);
                emitters.add(cue.withOffset(offset));
            }
        }
        return timeline(emitters, 3000);
    }
}
