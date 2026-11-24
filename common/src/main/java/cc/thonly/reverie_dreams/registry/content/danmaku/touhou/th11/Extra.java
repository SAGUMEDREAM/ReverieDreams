package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th11;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuLaser;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th11.Th11PatternSupport.*;

/**
 * TH11 stage07mboss.ecl (Kaenbyou Rin) and stage07boss.ecl (Komeiji Koishi),
 * Extra practice entries. Durations are the spellEx/setInterrupt values:
 * MBossCard1 3600 (MBossCard2), MBossCard2 3600 (MBossCard3), MBossCard3 3600
 * (MBossDead), BossCard1 5400 (Boss2), BossCard2 5400 (Boss3), BossCard3 5400
 * (Boss4), BossCard4 5400 (Boss5), BossCard5 3000 (Boss6), BossCard6 5400
 * (Boss7), BossCard7 6300 (Boss8), BossCard8 6300 (BossCard9), BossCard9 3540
 * (BossCard10), BossCard10 11400 (BossDead).
 */
final class Extra {
    private static final String RIN = "kaenbyou_rin";
    private static final String KOISHI = "komeiji_koishi";
    private static final Identifier STAR = ReverieDreams.id("star");
    private static final Identifier BUBBLE = ReverieDreams.id("bubble");
    private static final Identifier RICE = ReverieDreams.id("rice");
    private static final Identifier AMULET = ReverieDreams.id("amulet");
    private static final int RED = 0xE05260;
    private static final int BLUE = 0x5D8ADB;
    private static final int PURPLE = 0xA16DDD;
    private static final int GREEN = 0x62D958;
    private static final int GOLD = 0xDDBC61;
    private static final int WHITE = 0xF0EBF5;
    private static final int MAGENTA = 0xC357B8;

    private Extra() {}

    static List<TouhouSpell> spells() {
        return List.of(
                midboss(162, RIN, "秘法「九字刺」", 3600, Extra::kujiPierce),
                midboss(163, RIN, "奇迹「奇迹水果」", 3600, Extra::miracleFruit),
                midboss(164, RIN, "神德「五谷丰穰Rice Shower」", 3600, Extra::riceShower),
                card(165, KOISHI, "表象「梦枕上先祖总立」", 5400, Extra::ancestorPillow),
                card(166, KOISHI, "表象「弹幕妄想症」", 5400, Extra::danmakuParanoia),
                card(167, KOISHI, "本能「本我的解放」", 5400, Extra::idLiberation),
                card(168, KOISHI, "抑制「超自我」", 5400, Extra::superego),
                card(169, KOISHI, "反应「妖怪测谎仪」", 3000, Extra::youkaiPolygraph),
                card(170, KOISHI, "无意识「弹幕的罗夏」", 5400, Extra::rorschach),
                card(171, KOISHI, "复燃「恋之埋火」", 6300, Extra::embersOfLove),
                card(172, KOISHI, "深层「无意识的基因」", 6300, Extra::subconsciousGene),
                card(173, KOISHI, "「讨厌者的哲学」", 3540, Extra::hatedPhilosophy),
                card(174, KOISHI, "「Subterranean Rose」（地底蔷薇）", 11400, Extra::subterraneanRose));
    }

    private static TouhouSpell midboss(int number, String owner, String name, int frames,
                                       Supplier<DanmakuPattern> pattern) {
        return TouhouSpell.implemented(String.format(Locale.ROOT, "touhou/th11/s07_%03d_extra", number),
                name, frames, pattern).withCharacter("entity.reverie_dreams." + owner);
    }

    private static TouhouSpell card(int number, String owner, String name, int frames,
                                    Supplier<DanmakuPattern> pattern) {
        return midboss(number, owner, name, frames, pattern);
    }

    private static Vec3 seat(double x, double height, double depth) {
        return new Vec3(x, height, depth);
    }

    private static DanmakuPattern timelineWithLasers(List<DanmakuEmitter> emitters,
                                                     List<DanmakuLaser> lasers, int frames) {
        return new DanmakuPattern(emitters).withLasers(lasers)
                .withTiming(new SpellTiming(ticks(frames), 0, 0));
    }

    private static DanmakuLaser laser(int startTick, float pitch, float yaw, float length, float width,
                                      int warnTicks, int activeTicks, float sweepPerTick, int color) {
        return new DanmakuLaser(startTick, pitch, yaw, length, width, warnTicks, activeTicks,
                sweepPerTick, color);
    }

    // MBossCard1 (L37) cycles at3 (L156: fifteen left-edge lasers from
    // etOffsetAbs(-224, 0..448) stepping +32 px every 4 frames at heading 0
    // plus fifteen top-edge lasers at heading 90°, laserOnA2 380 length /
    // 32 width) and at3b (L179, mirrored from the right edge), then two
    // MBossCard1_at streams (L105): 219 bullets stepping 3.883222 rad (the
    // golden angle) on a circle whose radius grows 16 -> 180 px (0.75 px per
    // shot) with direction rotating by the same step, speed 0.1 accelerated
    // 0.016/90 frames (etEx 4) and sprite growth 4096/219; the second stream
    // flips both signs (L71/L85, colours 10 vs 6). The port keeps six lasers
    // per edge and samples every sixth spiral shot.
    private static DanmakuPattern kujiPierce() {
        final int deadline = 3600;
        var emitters = new ArrayList<DanmakuEmitter>();
        var lasers = new ArrayList<DanmakuLaser>();
        var spiral = shot(AMULET, 0.12f, PURPLE, 120, MotionSegment.accelerate(1, 0.002f));
        var spiralAlt = shot(AMULET, 0.12f, GREEN, 120, MotionSegment.accelerate(1, 0.002f));
        for (int cycle = 0, start = 120; start < deadline; cycle++, start += 590) {
            // at3: left and top edges; at3b: right edge (mirrored)
            for (int row = 0; row < 6; row++) {
                lasers.add(laser(ticks(start + row * 12), 0f, 90f, 12.7f, 1.07f, 2, 8, 0f, RED)
                        .withOffset(seat(-7.5, 0.2, -5.3 + row * 2.1)));
                lasers.add(laser(ticks(start + row * 12 + 4), 0f, 180f, 12.7f, 1.07f, 2, 8, 0f, RED)
                        .withOffset(seat(-6.9 + row * 2.3, 0.2, -5.5)));
                lasers.add(laser(ticks(start + row * 12 + 8), 0f, -90f, 12.7f, 1.07f, 2, 8, 0f, RED)
                        .withOffset(seat(7.5, 0.2, -5.3 + row * 2.1)));
            }
            // MBossCard1_at golden-angle spiral, sampled every sixth shot
            float base = cycle * 137f % 360f;
            emitters.add(sweep(fire(EmitShape.multiRing(3, 8, 0.53f, true, 222.49f, 0.5f, 0.02f),
                    spiral, start + 190, 0, 3), 0f, base, base));
            emitters.add(sweep(fire(EmitShape.multiRing(3, 8, 0.53f, true, 222.49f, 0.5f, 0.02f),
                    spiralAlt, start + 390, 0, 3), 0f, base + 111f, base + 111f));
        }
        return timelineWithLasers(emitters, lasers, deadline);
    }

    // MBossCard2_at (L255): every ~100 frame loop (L244-249) fires an 8-way
    // aimed fan (spread 0.3926991 rad) at speed 4 whose etEx 16/59 warp
    // teleports each bullet onto the player at frame 59 and re-launches it at
    // speed 1.0, then funcCall(6) rings. MBossCard2_at2 (L272): six 32-count
    // rings at random headings, speed 0 accelerated 0.0333 for 60 frames,
    // etDist 16 -> 136 px stepping 20 px every five frames. The port renders
    // the warp as a freeze-then-homing bullet and keeps one six-ring salvo
    // every 240 frames.
    private static DanmakuPattern miracleFruit() {
        final int deadline = 3600;
        var emitters = new ArrayList<DanmakuEmitter>();
        var warped = shot(BULLET, 0.4f, MAGENTA, 130, MotionSegment.freeze(19, 8),
                MotionSegment.homing(27, 90f, 20));
        var fruit = shot(BUBBLE, 0.02f, RED, 140, MotionSegment.accelerate(1, 0.02f));
        for (int volley = 0, start = 150; start < deadline; volley++, start += 45) {
            emitters.add(sweep(fire(EmitShape.fan(8, 22.5f), warped, start, 0, 3),
                    0f, volley * 13f % 360f, volley * 13f % 360f));
        }
        for (int salvo = 0, start = 240; start < deadline; salvo++, start += 240) {
            emitters.add(sweep(fire(EmitShape.multiRing(6, 12, 0.53f, true, 11.25f, 0.67f, 0.01f),
                    fruit, start, 0, 3), 0f, salvo * 47f % 360f, salvo * 47f % 360f));
        }
        return timeline(emitters, deadline);
    }

    // MBossCard3_at (L351): every $E frames (90 -> 50) the approach repeats —
    // five volleys (wait 3) of five speed-4 bullets each aimed around a base
    // rotating +1.256637 rad (72°) per bullet, with etEx 4/120 curvature
    // (0.016666668 -> 90°) and per-bullet scale cues (etEx 2048 sizes 6-14).
    // The port keeps three volleys per 150-frame cycle as 72°-spaced rings.
    private static DanmakuPattern riceShower() {
        final int deadline = 3600;
        var emitters = new ArrayList<DanmakuEmitter>();
        var grain = shot(RICE, 0.34f, GOLD, 115, MotionSegment.rotate(1, 0.5f));
        for (int cycle = 0, start = 150; start < deadline; cycle++, start += 150) {
            float base = cycle * 61f % 360f;
            for (int volley = 0; volley < 3; volley++) {
                float yaw = base + volley * 72f;
                var offset = seat(volley % 2 == 0 ? 0.67 : -0.67, 0.3,
                        volley == 1 ? 1.6 : -0.8);
                emitters.add(sweep(fire(EmitShape.ring(5, 0.5f, true), grain,
                        start + volley * 9, 0, 3).withOffset(offset), 0f, yaw, yaw));
            }
        }
        return timeline(emitters, deadline);
    }

    // BossCard1 (L765) cycle (L793-809): BossCard1At/At2 (L1104/L1126) cast
    // ten bottom-edge lasers each (etOffsetAbs(±192 -> ∓16 steps, y = 464),
    // laserOnA2 320 length / 32 width) whose headings rotate by %F1 per shot
    // — 0.005236 rad/frame on the slow pass, 0.03927 on the fast pass — then
    // BossCard1At3 (L1148) fires thirty bottom-edge columns (etAim 3, speed
    // 4.0, x stepping 16 px) where every third column also emits a 16-count
    // ring (speed 3.0). The port keeps six lasers per pass and ten columns
    // (every third as a ring) per cycle.
    private static DanmakuPattern ancestorPillow() {
        final int deadline = 5400;
        var emitters = new ArrayList<DanmakuEmitter>();
        var lasers = new ArrayList<DanmakuLaser>();
        var ancestor = shot(BUBBLE, 0.3f, PURPLE, 115);
        var ring = shot(STAR, 0.25f, WHITE, 110);
        for (int cycle = 0, start = 150; start < deadline; cycle++, start += 620) {
            float slowStep = 3f;
            float fastStep = 22.5f;
            for (int shaft = 0; shaft < 6; shaft++) {
                double x = -5.3 + shaft * 2.1;
                lasers.add(laser(ticks(start + shaft * 10), 0f, 180f + shaft * slowStep,
                        10.7f, 1.07f, 3, 9, 0f, MAGENTA).withOffset(seat(x, 0.2, 5.3)));
                lasers.add(laser(ticks(start + shaft * 10 + 4), 0f, 180f - shaft * slowStep,
                        10.7f, 1.07f, 3, 9, 0f, MAGENTA).withOffset(seat(-x, 0.2, 5.3)));
                lasers.add(laser(ticks(start + 520 + shaft * 8), 0f, 180f + shaft * fastStep,
                        10.7f, 1.07f, 3, 9, 0f, PURPLE).withOffset(seat(x, 0.2, 5.3)));
                lasers.add(laser(ticks(start + 520 + shaft * 8 + 4), 0f, 180f - shaft * fastStep,
                        10.7f, 1.07f, 3, 9, 0f, PURPLE).withOffset(seat(-x, 0.2, 5.3)));
            }
            // BossCard1At3: aimed columns with every third column as a ring
            for (int column = 0; column < 10; column++) {
                double x = -4.8 + column * 1.07;
                if (column % 3 == 2) {
                    emitters.add(sweep(fire(EmitShape.ring(16, 0.6f, true), ring,
                            start + 200 + column * 10, 0, 3).withOffset(seat(x, 0.2, 5.0)),
                            0f, column * 22.5f, column * 22.5f));
                } else {
                    emitters.add(fire(EmitShape.aim(), ancestor,
                            start + 200 + column * 10, 0, 3).withOffset(seat(x, 0.2, 5.0)));
                }
            }
        }
        return timelineWithLasers(emitters, lasers, deadline);
    }

    // BossCard2 (L1185): BossCard2at (L1226) spawns one bullet every frame at
    // a circlePos seat 64-80 px around the player (circlePos around
    // %PLAYER_X2/%PLAYER_Y2) flying at a random heading + 135° ± 22.5°
    // (3π/4 + %RANDRAD/8), speed 1.0-2.5; BossCard2at2 (L1255) adds aimed
    // 9/8-way rings every $F frames (80 -> 20, -4 per pair). The port seats
    // two six-count rings around a fixed forward seat and keeps one ring
    // emitter; cadence trims keep the entity budget near Stage05 levels.
    private static DanmakuPattern danmakuParanoia() {
        final int deadline = 5400;
        var emitters = new ArrayList<DanmakuEmitter>();
        var whisper = shot(BULLET, 0.25f, BLUE, 120);
        var ring = shot(BUBBLE, 0.24f, PURPLE, 115);
        emitters.add(sweep(fire(EmitShape.ring(6, 2.4f, true), whisper, 150, deadline - 210, 30)
                .withOffset(seat(0, 0.25, 3.2)), 0f, 137.5f, 137.5f));
        emitters.add(sweep(fire(EmitShape.ring(6, 1.6f, true), whisper, 210, deadline - 270, 30)
                .withOffset(seat(0, 0.25, 3.2)), 0f, 275f, 275f));
        emitters.add(fire(EmitShape.ring(9, 0.5f, true), ring, 180, deadline - 240, 40));
        return timeline(emitters, deadline);
    }

    // BossCard3 (L1286): two permanent BossCard3At streams (L1319/L1320) —
    // 12-count rings of etSprite 27 speed 2.5 every 18 frames, heading stepping
    // +0.09817477 rad (5.625°) with etEx 8 curvature easing from -0.008461539
    // to ∓0.007853982 rad over 260 frames; stream one colour 2 curls negative,
    // stream two colour 1 curls positive. The port keeps 8-count rings every
    // 36 frames, split into four sweep segments per stream.
    private static DanmakuPattern idLiberation() {
        final int deadline = 5400;
        var emitters = new ArrayList<DanmakuEmitter>();
        var idA = shot(BUBBLE, 0.28f, RED, 120, MotionSegment.rotate(1, -1.35f));
        var idB = shot(BUBBLE, 0.28f, BLUE, 120, MotionSegment.rotate(1, 1.35f));
        for (int segment = 0; segment < 4; segment++) {
            int start = 120 + segment * 1320;
            int window = Math.min(1320, deadline - start);
            if (window <= 0) {
                break;
            }
            float from = segment * 207f;
            emitters.add(sweep(fire(EmitShape.ring(8, 0.4f, true), idA, start, window, 36),
                    0f, from, from + 207f));
            emitters.add(sweep(fire(EmitShape.ring(8, 0.4f, true), idB, start, window, 36),
                    0f, -from, -from - 207f));
        }
        return timeline(emitters, deadline);
    }

    // BossCard4 (L1354): two BossCard4At streams (L1387/L1388) of 12-count
    // rings speed 0.3 every 20 frames — the first six volleys materialise a
    // line toward the player (etDist growing 0 -> 350 px in 32 px steps), then
    // headings spiral at ±0.09817477 rad with etEx 8 curvature growing
    // ($H 40 -> 260, rate 0.0076923077) and sprite growth 4096/25. The port
    // renders the line as four staggered forward seats and the spiral as two
    // sweep segments per stream.
    private static DanmakuPattern superego() {
        final int deadline = 5400;
        var emitters = new ArrayList<DanmakuEmitter>();
        var egoA = shot(BUBBLE, 0.14f, GREEN, 130, MotionSegment.accelerate(1, 0.004f));
        var egoB = shot(BUBBLE, 0.14f, WHITE, 130, MotionSegment.accelerate(1, 0.004f));
        for (int side = 0; side < 2; side++) {
            var spec = side == 0 ? egoA : egoB;
            float sign = side == 0 ? 1f : -1f;
            for (int step = 0; step < 4; step++) {
                emitters.add(sweep(fire(EmitShape.ring(6, 0.35f, true), spec,
                        150 + step * 60, 0, 3).withOffset(seat(0, 0.3, 0.9 + step * 1.0)),
                        0f, sign * step * 23f, sign * step * 23f));
            }
            for (int segment = 0; segment < 2; segment++) {
                int start = 600 + segment * 1500;
                int window = Math.min(1500, deadline - start);
                if (window <= 0) {
                    break;
                }
                float from = sign * segment * 180f;
                emitters.add(sweep(fire(EmitShape.ring(6, 0.35f, true), spec, start, window, 30),
                        0f, from, from + sign * 180f));
            }
        }
        return timeline(emitters, deadline);
    }

    // BossCard5 (L1439) is a timed survival (spellTimeout, 3000). Four
    // BossCard5Shadow (L1544) and four Shadow2 (L1565) electrodes seat around
    // the boss at %F0 = 3.534292 + k*90° and 4.31969 + k*90° (a 45° lattice);
    // each aims at the player with a needle accelerating 0.001745 +
    // 0.000002 rad/frame² (Shadow2At, L1586) and fires through the funcSet(7)
    // animation hook whose cadence is not in the ECL. The port seats eight
    // electrodes and fires one aimed shot per electrode every 60 frames with
    // a slowly opening yaw sweep for the needle drift.
    private static DanmakuPattern youkaiPolygraph() {
        final int deadline = 3000;
        var emitters = new ArrayList<DanmakuEmitter>();
        var needle = shot(AMULET, 0.2f, RED, 120);
        for (int electrode = 0; electrode < 8; electrode++) {
            float yaw = 22.5f + electrode * 45f;
            double radians = Math.toRadians(yaw);
            var offset = seat(Math.cos(radians) * 2.13, 0.3, Math.sin(radians) * 2.13);
            emitters.add(sweep(fire(EmitShape.aim(), needle, 180, deadline - 240, 60)
                    .withOffset(offset), 0f, yaw * 0.1f, yaw * 0.1f + 30f));
        }
        return timeline(emitters, deadline);
    }

    // BossCard6 (L1616): three phases (L1652/L1676/L1699) each spawn a
    // mirrored pair of BossCard6Et ink blots (moveEllipse ±0.05236 rad/frame,
    // aspect %F3/%F4 = 135°/0.3, 90°/0.2, 45°/0.1; 120 frames moving + 400
    // stationary). BossCard6EtAt (L1741) fires one etSprite 2 bullet every
    // two frames at the player, speed 0 with 0.015 acceleration over 120
    // frames and growth 4096/120, colours $I0 = 6/10/4. The port keeps six
    // fixed mid-ellipse seats (the sweep is not representable) firing every
    // eight frames, re-seated each cycle.
    private static DanmakuPattern rorschach() {
        final int deadline = 5400;
        var emitters = new ArrayList<DanmakuEmitter>();
        var blotA = shot(BUBBLE, 0.03f, BLUE, 140, MotionSegment.accelerate(1, 0.01f));
        var blotB = shot(BUBBLE, 0.03f, MAGENTA, 140, MotionSegment.accelerate(1, 0.01f));
        var blotC = shot(BUBBLE, 0.03f, GOLD, 140, MotionSegment.accelerate(1, 0.01f));
        var specs = new DanmakuBulletSpec[]{blotA, blotB, blotC};
        double[][] seats = {{3.5, -1.5, -3.5, -1.5}, {3.9, -2.3, -3.9, -2.3}, {4.2, -3.1, -4.2, -3.1}};
        for (int cycle = 0; cycle < 4; cycle++) {
            for (int phase = 0; phase < 3; phase++) {
                int start = 120 + cycle * 1520 + phase * 500;
                if (start + 520 >= deadline) {
                    break;
                }
                var spec = specs[phase];
                for (int mirror = 0; mirror < 2; mirror++) {
                    emitters.add(fire(EmitShape.aim(), spec, start, 500, 8)
                            .withOffset(seat(seats[phase][mirror * 2], 0.3,
                                    seats[phase][mirror * 2 + 1])));
                }
            }
        }
        return timeline(emitters, deadline);
    }

    // BossCard7 (L1764) loops every ~300 frames (L1803-1813): BossCard7EtAt
    // (L1833) fires two etSprite 27 bullets speed 2.0 from the eye offsets
    // (±32, -48) that curl via etEx 256 over 10x15 frames; BossCard7EtAt3
    // (L1880) repeats them at headings +0.392699 and 2.7488935 rad;
    // BossCard7EtAt2 (L1851) sprinkles two etSprite 18/25 embers every two
    // frames at random headings and speeds 0-0.4 growing 4096/50. The port
    // keeps the four eye bullets per cycle and one ember per 20 frames.
    private static DanmakuPattern embersOfLove() {
        final int deadline = 6300;
        var emitters = new ArrayList<DanmakuEmitter>();
        var eye = shot(BUBBLE, 0.22f, RED, 120, MotionSegment.rotate(1, 0.8f));
        var eyeAngled = shot(BUBBLE, 0.22f, PURPLE, 120, MotionSegment.rotate(1, 0.8f));
        var ember = shot(PELLET, 0.08f, GOLD, 130);
        for (int cycle = 0, start = 180; start < deadline; cycle++, start += 300) {
            emitters.add(sweep(fire(EmitShape.fan(2, 180f), eye, start, 0, 3)
                    .withOffset(seat(-1.07, 1.4, 0)), 0f, 0f, 0f));
            emitters.add(sweep(fire(EmitShape.fan(2, 180f), eyeAngled, start + 150, 0, 3)
                    .withOffset(seat(1.07, 1.4, 0)), 0f, 22.5f, 22.5f));
        }
        emitters.add(fire(EmitShape.aim(), ember, 240, deadline - 300, 20).withJitter(40f));
        return timeline(emitters, deadline);
    }

    // BossCard8 (L1900): two BossCard8Et blobs orbit the boss with
    // moveEllipseRel at ±0.07854 rad/frame (a full orbit every 80 frames,
    // aspect 135°/0.3) for 100 frames then hold; BossCard8EtAt (L2002) fires
    // one etSprite 1 bullet every two frames aimed at the player, speed 0
    // accelerated 0.025 over 120 frames, heading spiralling +0.019635 rad per
    // shot, colours 6/10. The boss dashes along moveBezier paths (L1955-1978).
    // The port keeps eight orbit seats around the boss, one aimed shot each
    // every 60 frames with a ±45 degree yaw sweep for the spiral.
    private static DanmakuPattern subconsciousGene() {
        final int deadline = 6300;
        var emitters = new ArrayList<DanmakuEmitter>();
        var geneA = shot(STAR, 0.03f, BLUE, 140, MotionSegment.accelerate(1, 0.015f));
        var geneB = shot(STAR, 0.03f, MAGENTA, 140, MotionSegment.accelerate(1, 0.015f));
        for (int orbit = 0; orbit < 8; orbit++) {
            float yaw = orbit * 45f;
            double radians = Math.toRadians(yaw);
            var offset = seat(Math.cos(radians) * 2.8, 0.3, Math.sin(radians) * 2.8);
            var spec = orbit % 2 == 0 ? geneA : geneB;
            float from = orbit * 45f;
            emitters.add(sweep(fire(EmitShape.aim(), spec, 150 + orbit * 5, deadline - 210, 60)
                    .withOffset(offset), 0f, from, from + 45f));
        }
        return timeline(emitters, deadline);
    }

    // BossCard9 (L2041) is a timed survival (spellTimeout, 3540) with a
    // 32-count opening ring (L2088-2095, etSprite 17 speed 2.0). Phase one
    // (EtAtA, L2190) drops 7-count vertical lines every 13 frames (skip 1 in
    // 5) from the top edge (y = -16) through a sliding 64 px window (x -8
    // px/frame, five 64 px columns), colour 6. Phase two (EtAtB1/B2,
    // L2227/L2264) mirrors them from the bottom edge (y = 464, heading -90°)
    // and the right edge (x = 208, heading 180°), colour 13, skip 2 in 6.
    // Phase three (EtAtC1/C2, L2301/L2333) fires dense 15/17-count lines every
    // 100 frames from the same edges, colour 2. The port keeps three sliding
    // columns and both mirrored edges with trimmed cadences.
    private static DanmakuPattern hatedPhilosophy() {
        final int deadline = 3540;
        var emitters = new ArrayList<DanmakuEmitter>();
        var rain = shot(STAR, 0.2f, RED, 120);
        var rainUp = shot(STAR, 0.2f, MAGENTA, 120);
        var wall = shot(STAR, 0.22f, BLUE, 125);
        emitters.add(fire(EmitShape.ring(32, 0.6f, true), rain, 120, 0, 3));
        // phase one: top-edge sliding columns
        for (int column = 0; column < 3; column++) {
            emitters.add(sweep(fire(EmitShape.multiRing(7, 1, 0.3f, true, 0f, 0.3f, 0f),
                    rain, 150 + column * 40, 1050, 40).withOffset(seat(-3.6 + column * 3.6, 0.2, -4.4)),
                    0f, 0f, 0f));
        }
        // phase two: bottom and right edges
        emitters.add(sweep(fire(EmitShape.multiRing(7, 1, 0.3f, true, 0f, 0.3f, 0f),
                rainUp, 1500, 1400, 40).withOffset(seat(0, 0.2, 4.8)), 0f, 180f, 180f));
        emitters.add(sweep(fire(EmitShape.multiRing(7, 1, 0.3f, true, 0f, 0.3f, 0f),
                rainUp, 1560, 1400, 40).withOffset(seat(6.4, 0.2, 0)), 0f, -90f, -90f));
        // phase three: dense curtains
        emitters.add(sweep(fire(EmitShape.multiRing(10, 1, 0.35f, true, 0f, 0.35f, 0f),
                wall, 2940, 600, 100).withOffset(seat(0, 0.2, 4.8)), 0f, 180f, 180f));
        emitters.add(sweep(fire(EmitShape.multiRing(10, 1, 0.35f, true, 0f, 0.35f, 0f),
                wall, 3000, 480, 100).withOffset(seat(6.4, 0.2, 0)), 0f, -90f, -90f));
        return timeline(emitters, deadline);
    }

    // BossCard10 (L820) phases on $LIFE (<8400/5600/2800 of 11200); the port
    // maps the life thresholds to time quarters at 2850/5700/8550 (th18
    // princess precedent). BossCard10EtAt (L908): alternating 64-count rose
    // rings (etSprite 1, colours 6/2, speed 1.0-2.5) every 100 frames;
    // EtAtB (L957) 48-count every 55; EtAtC (L1006) 64-count every 100;
    // EtAtD (L1055) 64-count every 40. The At2 streams (L940-1101) only drive
    // the rotating boss graphic (funcSet 12) and are not ported.
    private static DanmakuPattern subterraneanRose() {
        final int deadline = 11400;
        var emitters = new ArrayList<DanmakuEmitter>();
        var roseA = shot(STAR, 0.28f, RED, 130);
        var roseB = shot(STAR, 0.22f, WHITE, 130);
        int[][] phases = {{90, 2850, 200}, {2850, 5700, 150}, {5700, 8550, 200}, {8550, 11400, 120}};
        for (int[] phase : phases) {
            int count = phase[1] >= 5700 && phase[1] < 8550 ? 24 : 32;
            for (int start = phase[0]; start < phase[1]; start += phase[2]) {
                emitters.add(sweep(fire(EmitShape.ring(count, 1.9f, true), roseA, start, 0, 3),
                        0f, start * 0.11f % 360f, start * 0.11f % 360f));
                emitters.add(sweep(fire(EmitShape.ring(count, 1.3f, true), roseB,
                        start + phase[2] / 2, 0, 3), 0f, -start * 0.13f % 360f,
                        -start * 0.13f % 360f));
            }
        }
        return timeline(emitters, deadline);
    }
}
