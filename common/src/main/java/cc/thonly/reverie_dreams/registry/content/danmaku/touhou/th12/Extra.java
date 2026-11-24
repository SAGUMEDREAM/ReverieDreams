package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th12;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuLaser;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th12.Th12PatternSupport.*;

/**
 * TH12 stage07.ecl (Nazrin midboss) and stage07boss.ecl (Houjuu Nue), Extra
 * practice entries. Durations are the spellEx/setInterrupt values: MBossCard1
 * 3600 (MBossCard2), MBossCard2 3600 (MBossCard3), MBossCard3 3600
 * (MBossDead), BossCard1 3600 (Boss2), BossCard2 3600 (Boss3), BossCard3 3600
 * (Boss4), BossCard4 3600 (Boss5), BossCard5 3960 (Boss6), BossCard6 3960
 * (Boss7), BossCard7 4800 (Boss8), BossCard8 4200 (Boss9), BossCard9 4260
 * (Boss10), BossCard10 7200 (BossDead).
 */
final class Extra {
    private static final String NAZRIN = "entity.reverie_dreams.nazrin";
    private static final String NUE = "entity.reverie_dreams.houjuu_nue";
    private static final Identifier STAR = ReverieDreams.id("star");
    private static final Identifier BUBBLE = ReverieDreams.id("bubble");
    private static final Identifier KUNAI = ReverieDreams.id("kunai");
    private static final Identifier AMULET = ReverieDreams.id("amulet");
    private static final int PURPLE = 0x9A60D7;
    private static final int PINK = 0xE46E9F;
    private static final int RED = 0xD64550;
    private static final int BLUE = 0x5D8ADB;
    private static final int GREEN = 0x62D958;
    private static final int GOLD = 0xDDBC61;
    private static final int WHITE = 0xF0EBF5;
    private static final int CYAN = 0x63BCE6;
    private static final int MAGENTA = 0xC357B8;

    private Extra() {}

    static List<TouhouSpell> spells() {
        return List.of(
                midboss(100, NAZRIN, "伞符「大粒的泪雨」", 3600, Extra::tearRain),
                midboss(101, NAZRIN, "骤雨「游击台风」", 3600, Extra::guerrillaTyphoon),
                midboss(102, NAZRIN, "后光「唐伞惊异闪光」", 3600, Extra::umbrellaFlash),
                card(103, NUE, "正体不明「忿怒的红色UFO袭来」", 3600, Extra::redUfoInvasion),
                card(104, NUE, "鵺符「鵺的蛇形表演」", 3600, Extra::snakeShow),
                card(105, NUE, "正体不明「哀愁的蓝色UFO袭来」", 3600, Extra::blueUfoInvasion),
                card(106, NUE, "鵺符「弹幕奇美拉」", 3960, Extra::danmakuChimera),
                card(107, NUE, "正体不明「义心的绿色UFO袭来」", 3960, Extra::greenUfoInvasion),
                card(108, NUE, "鵺符「未定义黑暗」", 4800, Extra::unidentifiedDarkness),
                card(109, NUE, "正体不明「恐怖的彩虹UFO袭来」", 4200, Extra::rainbowUfoInvasion),
                card(110, NUE, "「平安京的恶梦」", 4260, Extra::heianNightmare),
                card(111, NUE, "恨弓「源三位赖政的弓」", 7200, Extra::yorimasaBow),
                card(112, NUE, "「Subterranean Rose」（地底蔷薇）", 11400, Extra::subterraneanRose));
    }

    private static TouhouSpell midboss(int number, String owner, String name, int frames,
                                       Supplier<DanmakuPattern> pattern) {
        return TouhouSpell.implemented(String.format(Locale.ROOT, "touhou/th12/s07_%03d_extra", number),
                name, frames, pattern).withCharacter(owner);
    }

    private static TouhouSpell card(int number, String owner, String name, int frames,
                                    Supplier<DanmakuPattern> pattern) {
        return midboss(number, owner, name, frames, pattern);
    }

    private static Vec3 seat(double x, double height, double depth) {
        return new Vec3(x, height, depth);
    }

    // MBossCard1At (stage07.ecl L762): sixty 2-frame volleys of a nine-way fan
    // (etCount 9,1, spread 0.7662421 rad) whose heading steps by the golden
    // angle 3.883222 rad and whose spawn distance shrinks 128 -> 48 px (etDist,
    // -2 px per volley); speed 3.5 with per-volley deceleration (etEx 4/30).
    // The card repeats every wait($C) with $C dropping 190 -> 130 (L744-750).
    // The port keeps five of the sixty volleys per cycle and repeats every 150
    // frames, stepping each sampled volley by the full golden angle.
    private static DanmakuPattern tearRain() {
        final int deadline = 3600;
        var emitters = new ArrayList<DanmakuEmitter>();
        var bullet = shot(BULLET, 0.3f, PURPLE, 110, MotionSegment.accelerate(1, -0.004f));
        var fan = EmitShape.fan(9, 43.9f);
        for (int cycle = 0, start = 210; start < deadline; cycle++, start += 150) {
            float base = cycle * 37f % 360f;
            for (int volley = 0; volley < 5; volley++) {
                double radius = 4.27 - volley * 0.66;
                float yaw = base + volley * 222.49f;
                double radians = Math.toRadians(yaw);
                var offset = seat(Math.cos(radians) * radius, 0.35, Math.sin(radians) * radius);
                emitters.add(sweep(fire(fan, bullet, start + volley * 12, 0, 3).withOffset(offset),
                        0f, yaw, yaw));
            }
        }
        return timeline(emitters, deadline);
    }

    // MBossCard2At2/_l2/_l (L896/L927/L918): the card repeats every ~190 -> 170
    // frames (L877-885) with three rain fronts 58 frames apart. Each front
    // drops 35 columns five frames apart from the left screen edge
    // (etOffsetAbs x = -224) with y stepping +18 px; bullets leave at speed
    // 1-3 toward 20-30 degrees + 0.7662421 rad spread and decelerate for 90
    // frames (etEx 4, rate 0.055556). The port keeps three fronts of twelve
    // columns ten frames apart and repeats every 330 frames.
    private static DanmakuPattern guerrillaTyphoon() {
        final int deadline = 3600;
        var emitters = new ArrayList<DanmakuEmitter>();
        var rain = shot(BULLET, 0.2f, BLUE, 120, MotionSegment.accelerate(1, -0.003f));
        for (int wave = 0, start = 210; start < deadline; wave++, start += 330) {
            for (int front = 0; front < 3; front++) {
                var offset = seat(-7.4, 0.3, -3.3 + front * 3.3);
                emitters.add(sweep(fire(EmitShape.fan(6, 43.9f), rain,
                        start + front * 58, 110, 10).withOffset(offset),
                        0f, 35f + wave * 23f, 35f + wave * 23f));
            }
        }
        return timeline(emitters, deadline);
    }

    // MBossCard3At/Loser (L1029/L1117): every $E frames (90 -> 50, -5 per
    // cycle) $F umbrellas (32 -> +2 per cycle) seat evenly on a 64 px circle;
    // each casts a striking laser (laserNew 512/512/0/14, laserTiming
    // 120/15/40/15) whose heading swings radial -> radial ± 2.8274333 rad
    // (162°) with ease-out over 120 frames (laserStAngle, L1132-1141). The
    // port seats sixteen umbrellas and repeats the flash every 300 frames;
    // the swing becomes a 4.05 deg/tick sweep reflected at ±162 degrees.
    private static DanmakuPattern umbrellaFlash() {
        final int deadline = 3600;
        var lasers = new ArrayList<DanmakuLaser>();
        for (int flash = 0, start = 240; start < deadline; flash++, start += 300) {
            float twist = flash % 2 == 0 ? 4.05f : -4.05f;
            for (int blade = 0; blade < 16; blade++) {
                float yaw = blade * 22.5f + flash * 11f;
                double radians = Math.toRadians(yaw);
                var offset = seat(Math.cos(radians) * 2.13, 0.3, Math.sin(radians) * 2.13);
                lasers.add(laser(ticks(start), 0, yaw, 17f, 0.47f, 40, 10, twist, PINK)
                        .withOffset(offset).withSweepLimit(162f));
            }
        }
        return timeline(new ArrayList<>(), lasers, deadline);
    }

    // BossCard2At2 (L1761): per cycle two eight-cloud rings rotate ±0.015708
    // rad/frame with wait($A) 400 -> 320 (-40) between halves. Each
    // BossCard2UfoRed (L1810) orbits for 330 frames; its At (L1827) fires one
    // aimed big bullet (etSprite 24) every 20 frames from frame 60, speed 0.2,
    // growth (etEx 4096/60) and deceleration (etEx 4/120, 0.025). A destroyed
    // UFO bursts BossCard2At3 (L1796): a nine-way fan, speed 1.7, spread 15
    // degrees. The port keeps one eight-seat ring per cycle; the orbit is not
    // representable, so seats are fixed on the ring and their aim sweeps.
    private static DanmakuPattern redUfoInvasion() {
        final int deadline = 3600;
        var emitters = new ArrayList<DanmakuEmitter>();
        var bigShot = shot(BUBBLE, 0.12f, RED, 130, MotionSegment.accelerate(1, -0.001f));
        var burst = shot(AMULET, 0.4f, GOLD, 105);
        int[] waves = {120, 840, 1400, 1840, 2280, 2720, 3160};
        for (int wave = 0; wave < waves.length; wave++) {
            int start = waves[wave];
            for (int seatIdx = 0; seatIdx < 8; seatIdx++) {
                float yaw = seatIdx * 45f + wave * 17f;
                double radians = Math.toRadians(yaw);
                var offset = seat(Math.cos(radians) * 3.2, 0.35, Math.sin(radians) * 3.2);
                emitters.add(sweep(fire(EmitShape.aim(), bigShot, start + 60, 240, 20)
                        .withOffset(offset), 0f, yaw + wave * 40f, yaw + wave * 40f + 90f));
                emitters.add(sweep(fire(EmitShape.fan(9, 15f), burst, start + 330, 0, 3)
                        .withOffset(offset), 0f, yaw + 180f, yaw + 180f));
            }
        }
        return timeline(emitters, deadline);
    }

    // BossCard3At/At2 (L1905/L1957): the card alternates a 32-laser curving
    // wave and its mirror roughly every 220 frames (L1887-1899). Each laser
    // starts on a random heading stepping 0.19635 rad (11.25°) and wobbles
    // ±0.07853982 rad (4.5°) over 20-frame segments (etEx 8); the second wave
    // adds a 100-frame counter-segment (L1939). The port keeps eight lasers
    // per wave, both waves every 440 frames; the wobble becomes a ±0.675
    // deg/tick sweep limited to ±4.5 (wave one) and ±9 (wave two) degrees.
    private static DanmakuPattern snakeShow() {
        final int deadline = 3600;
        var lasers = new ArrayList<DanmakuLaser>();
        for (int cycle = 0, start = 180; start < deadline; cycle++, start += 440) {
            float base = cycle * 47f % 360f;
            for (int blade = 0; blade < 8; blade++) {
                lasers.add(laser(ticks(start), 0, base + blade * 11.25f, 14f, 0.44f,
                        10, 12, 0.675f, MAGENTA).withSweepLimit(4.5f));
                lasers.add(laser(ticks(start + 190), 0, base + 5.6f + blade * 11.25f, 14f, 0.44f,
                        10, 12, 0.675f, CYAN).withSweepLimit(9f));
            }
        }
        return timeline(new ArrayList<>(), lasers, deadline);
    }

    // BossCard4At2 (L2051): per cycle sixteen BossCard4UfoBlue spawn at the
    // boss (eight clockwise, eight counter, 60 frames apart, wait($A) 400 ->
    // 320). Each UFO orbits its seat at 0.007854 rad/frame and
    // BossCard4UfoBlueAt (L2108) casts a short laser (laserNew 0/128/512/14)
    // from 32 px every 10 frames, heading stepping -0.194161 rad (11.13°).
    // The port keeps eight seats and three headings per seat, five waves; the
    // orbit itself is not representable.
    private static DanmakuPattern blueUfoInvasion() {
        final int deadline = 3600;
        var lasers = new ArrayList<DanmakuLaser>();
        int[] waves = {120, 840, 1400, 1840, 2280};
        for (int wave = 0; wave < waves.length; wave++) {
            int start = waves[wave];
            for (int seatIdx = 0; seatIdx < 8; seatIdx++) {
                float yaw = seatIdx * 45f + wave * 23f;
                double radians = Math.toRadians(yaw);
                var offset = seat(Math.cos(radians) * 3.2, 0.3, Math.sin(radians) * 3.2);
                for (int heading = 0; heading < 3; heading++) {
                    lasers.add(laser(ticks(start + heading * 10), 0, yaw - heading * 11.13f,
                            4.3f, 0.44f, 2, 8, 0f, BLUE).withOffset(offset));
                }
            }
        }
        return timeline(new ArrayList<>(), lasers, deadline);
    }

    // BossCard5At2/At1 (L2228/L2200): every 160+60 frame cycle casts sixteen
    // striking lasers (laserNew 0/128/512/14) whose headings step -0.392699
    // rad (22.5°) from 1.3744467 rad — a full-circle spoke, re-aimed the same
    // way each cycle; etEx 512/1200 keeps the beams alive; funcCall(3)/(4)
    // (L2232/L2234) are chime effects. The port repeats every 440 frames and
    // sweeps each spoke ±90 degrees at 3 deg/tick to suggest the travelling
    // beam (laserNew's 512 speed has no moving-beam equivalent here).
    private static DanmakuPattern danmakuChimera() {
        final int deadline = 3960;
        var lasers = new ArrayList<DanmakuLaser>();
        for (int cycle = 0, start = 210; start < deadline; cycle++, start += 440) {
            for (int spoke = 0; spoke < 16; spoke++) {
                lasers.add(laser(ticks(start), 0, 78.75f - spoke * 22.5f, 4.3f, 0.44f,
                        8, 22, cycle % 2 == 0 ? 3f : -3f, GOLD).withSweepLimit(90f));
            }
        }
        return timeline(new ArrayList<>(), lasers, deadline);
    }

    // BossCard6At2 (L2280): per half-cycle four pairs of BossCard6UfoGreen
    // enter from x = ±300 px (enmCreateA, 15 frames apart, life $I0 120 ->
    // 60) flying at speed 4, then halt (L2499) and cast two wobbling curving
    // lasers (BossCard6LaserAt2, L2418: laserCuOn, laserTiming 42, ±0.15707964
    // rad alternating over 10-frame segments) plus BossCard6LaserAt's
    // (L2379) two-way big-bullet spiral (etSprite 24/23, angle stepping
    // ±0.194161 rad from a circling offset). The port keeps three halting
    // seats per side per half-cycle; the fly-in is not representable.
    private static DanmakuPattern greenUfoInvasion() {
        final int deadline = 3960;
        var emitters = new ArrayList<DanmakuEmitter>();
        var lasers = new ArrayList<DanmakuLaser>();
        var spiral = shot(BUBBLE, 0.16f, GREEN, 110);
        for (int half = 0, start = 150; start < deadline; half++, start += 380) {
            int side = half % 2 == 0 ? -1 : 1;
            for (int lane = 0; lane < 3; lane++) {
                var offset = seat(side * (5.2 - lane * 1.3), 0.3, -2.1 + lane * 2.1);
                float baseYaw = side < 0 ? 90f : -90f;
                lasers.add(laser(ticks(start + 120), 0, baseYaw, 4.3f, 0.48f,
                        14, 20, 2.7f, GREEN).withOffset(offset).withSweepLimit(9f));
                lasers.add(laser(ticks(start + 130), 0, baseYaw + 22.5f, 4.3f, 0.48f,
                        14, 20, -2.7f, GREEN).withOffset(offset).withSweepLimit(9f));
                emitters.add(sweep(fire(EmitShape.multiRing(2, 4, 0.5f, true, 90f, 0.3f, 0.03f),
                        spiral, start + 130, 30, 10).withOffset(offset), 0f, baseYaw, baseYaw));
            }
        }
        return timeline(emitters, lasers, deadline);
    }

    // BossCard7 (L2561): each cycle (wait $D 170 -> 40, -20) seats sixteen
    // BossCard7Cloud (L2627) on a ring stepping 0.392699 rad that drift
    // outward at 0.5-0.9. After 80 frames each cloud's At2 (L2665) fires
    // twelve bullets (etSprite 23,6) starting behind the player
    // (%ANGLE_PLAYER + π) stepping -0.554746 rad (31.76°) every five frames
    // at speeds 0.2-2.2. The port keeps eight seats and fires each seat's
    // backwards fan three times per cycle.
    private static DanmakuPattern unidentifiedDarkness() {
        final int deadline = 4800;
        var emitters = new ArrayList<DanmakuEmitter>();
        var dark = shot(BULLET, 0.15f, PURPLE, 120);
        var darkFast = shot(BULLET, 1.1f, WHITE, 100);
        for (int cycle = 0, start = 150; start < deadline; cycle++, start += 280) {
            for (int seatIdx = 0; seatIdx < 8; seatIdx++) {
                float yaw = seatIdx * 45f + cycle * 29f;
                double radians = Math.toRadians(yaw);
                var offset = seat(Math.cos(radians) * 4.0, 0.35, Math.sin(radians) * 4.0);
                var backwards = sweep(fire(EmitShape.fan(4, 95.3f), dark,
                        start + 80, 55, 20).withOffset(offset), 0f, yaw + 180f, yaw + 180f);
                emitters.add(backwards);
                emitters.add(sweep(fire(EmitShape.fan(4, 95.3f), darkFast,
                        start + 95, 55, 20).withOffset(offset), 0f, yaw + 180f, yaw + 180f));
            }
        }
        return timeline(emitters, deadline);
    }

    // BossCard8At2 (L2736): each half-cycle seats two big crossers (x = ±224,
    // life 8000) and three small UFOs (±48-±144, life 900), thirty frames
    // apart, then moveRand(120) + wait(90). BossCard8UfoRainbow (L2801)
    // crosses at speed ramping to 3.0 and its At (L2823) drops paired
    // near-static seeds (speed 0.1, etDist 32) at %A and 180° - %A stepping
    // -0.194161 rad every five frames. The port keeps the two crosser lanes
    // and four small seats per cycle; the crossing motion is not
    // representable, so seeds drop from fixed lane positions.
    private static DanmakuPattern rainbowUfoInvasion() {
        final int deadline = 4200;
        var emitters = new ArrayList<DanmakuEmitter>();
        var seed = shot(PELLET, 0.05f, MAGENTA, 140);
        var seedFast = shot(PELLET, 0.14f, CYAN, 130);
        for (int cycle = 0, start = 150; start < deadline; cycle++, start += 780) {
            for (int lane = 0; lane < 2; lane++) {
                var offset = seat(0, 0.3, -2.2 + lane * 4.4);
                emitters.add(sweep(fire(EmitShape.fan(2, 150f), seed,
                        start + 60, 300, 15).withOffset(offset),
                        0f, 90f + cycle * 37f, 90f + cycle * 37f));
            }
            for (int small = 0; small < 4; small++) {
                var offset = seat(small % 2 == 0 ? -1.6 : 1.6, 0.3, -3.2 + small * 2.1);
                emitters.add(sweep(fire(EmitShape.fan(2, 120f), seedFast,
                        start + 120 + small * 30, 240, 24).withOffset(offset),
                        0f, 60f + small * 45f + cycle * 19f, 60f + small * 45f + cycle * 19f));
            }
        }
        return timeline(emitters, deadline);
    }

    // BossCard9 (L2880) is a timed survival (spellTimeout, 4260). Phase one
    // (BossCard9At1b, L3022) runs eight columns 76 px apart (x -266..+266);
    // each BossCard9At1 (L2931) raises a striking wall laser (laserNew
    // 512/512/0/14, laserTiming 30/10/600) then five waves ($D 200 -> 130) of
    // 14x4 grid bullets whose etEx 33554432 slide slides each bullet ±76/±48
    // px over $D frames, plus BossCard9At1c's (L3040) eight aimed kunai per
    // column. Phase two (BossCard9At2b, L3178) mirrors the walls horizontally
    // from x = -224 with BossCard9At2c side streams (L3196); phase three
    // (BossCard9At3b, L3440) runs both walls with a 24 px grid pitch
    // shrinking 3 px per wave. The port keeps three waves per phase and
    // renders the slide as a yaw sweep across each curtain window.
    private static DanmakuPattern heianNightmare() {
        final int deadline = 4260;
        var emitters = new ArrayList<DanmakuEmitter>();
        var lasers = new ArrayList<DanmakuLaser>();
        var wall = shot(BULLET, 0.05f, WHITE, 160);
        var kunai = shot(KUNAI, 0.26f, GOLD, 110, MotionSegment.accelerate(1, -0.001f));
        // phase one: vertical curtain columns from the far edge
        for (int column = 0; column < 8; column++) {
            double x = -6.3 + column * 1.8;
            var offset = seat(x, 0.3, -4.4);
            for (int wave = 0; wave < 3; wave++) {
                int start = 240 + wave * 180;
                emitters.add(sweep(fire(EmitShape.aim(), wall, start, 60, 10).withOffset(offset),
                        0f, -30f, 30f));
                emitters.add(sweep(fire(EmitShape.fan(8, 15f), kunai, start, 0, 3)
                        .withOffset(offset), 0f, 0f, 0f));
                lasers.add(laser(ticks(start), 0, 180f, 17f, 0.47f, 10, 20, 0f, PURPLE)
                        .withOffset(offset));
            }
        }
        // phase two: horizontal curtain rows from the left edge
        for (int row = 0; row < 8; row++) {
            var offset = seat(-7.3, 0.3, -4.4 + row * 1.45);
            for (int wave = 0; wave < 3; wave++) {
                int start = 1380 + wave * 180;
                emitters.add(sweep(fire(EmitShape.aim(), wall, start, 60, 10).withOffset(offset),
                        0f, 60f, 120f));
                emitters.add(sweep(fire(EmitShape.fan(8, 15f), kunai, start, 0, 3)
                        .withOffset(offset), 0f, 90f, 90f));
                lasers.add(laser(ticks(start), 0, 90f, 17f, 0.47f, 10, 20, 0f, MAGENTA)
                        .withOffset(offset));
            }
        }
        // phase three: both curtains at once, tighter pitch
        for (int column = 0; column < 6; column++) {
            var offset = seat(-4.5 + column * 1.8, 0.3, -4.4);
            for (int wave = 0; wave < 3; wave++) {
                int start = 2760 + wave * 180;
                emitters.add(sweep(fire(EmitShape.aim(), wall, start, 60, 10).withOffset(offset),
                        0f, -22f, 22f));
                lasers.add(laser(ticks(start), 0, 180f, 17f, 0.47f, 10, 20, 0f, BLUE)
                        .withOffset(offset));
            }
        }
        for (int row = 0; row < 6; row++) {
            var offset = seat(-7.3, 0.3, -3.6 + row * 1.6);
            for (int wave = 0; wave < 3; wave++) {
                int start = 2820 + wave * 180;
                emitters.add(sweep(fire(EmitShape.aim(), wall, start, 60, 10).withOffset(offset),
                        0f, 60f, 120f));
                lasers.add(laser(ticks(start), 0, 90f, 17f, 0.47f, 10, 20, 0f, RED)
                        .withOffset(offset));
            }
        }
        return timeline(emitters, lasers, deadline);
    }

    // BossCard10 (L984) phases on $LIFE (<8400/5600/2800 of 11200) or
    // $TIME >= 5400; the port maps the life thresholds to equal time quarters
    // because a held card has no encounter HP (th18 princess precedent).
    // BossCard10At1 (L1169): ninety arrows from the boss every 2 frames
    // (etDist 50, speed 8) with jitter growing 0.174533 -> 2.06 rad.
    // BossCard10At2 family (L1192-1581): twenty top-edge arrows per wave (x
    // stepping 19 px from ±192, y = -32) at 90° stepping ∓0.05236 rad every
    // 2-3 frames as short travelling lasers (laserNew 0/128/1024/14 or
    // 0/130/1024/14); At3* (L1583-1652) are background arrow showers
    // (funcCall(5)) and are not ported. Arrows become steep beams dropped
    // from above the arena.
    private static DanmakuPattern yorimasaBow() {
        final int deadline = 7200;
        var lasers = new ArrayList<DanmakuLaser>();
        int[] phaseStarts = {120, 1470, 2820, 4170, 5460};
        int[] iterations = {3, 3, 3, 3, 4};
        for (int phase = 0; phase < phaseStarts.length; phase++) {
            int gap = 480;
            for (int iter = 0; iter < iterations[phase]; iter++) {
                int start = phaseStarts[phase] + iter * gap;
                if (start + 60 >= deadline) {
                    break;
                }
                // BossCard10At1 volley: bow shots fanning around the player
                for (int arrow = 0; arrow < 12; arrow++) {
                    float yaw = (arrow - 6) * 10f * (1f + arrow * 0.02f);
                    lasers.add(laser(ticks(start + arrow * 2), 70f, 180f + yaw, 4.3f, 0.3f,
                            1, 2, 0f, GOLD).withOffset(seat(yaw / 12.0, 4.5, 0)));
                }
                // BossCard10At2 family: top-edge arrows converging ±3° per shaft
                float converge = phase % 2 == 0 ? -3f : 3f;
                float xStep = phase % 2 == 0 ? -0.63f : 0.63f;
                double x = phase % 2 == 0 ? 6.3 : -6.3;
                for (int shaft = 0; shaft < 16; shaft++) {
                    lasers.add(laser(ticks(start + 120 + shaft * 3), 75f,
                            180f + shaft * converge, 4.3f, 0.3f, 1, 2, 0f, WHITE)
                            .withOffset(seat(x, 4.5, 0)));
                    x += xStep;
                }
            }
        }
        return timeline(new ArrayList<>(), lasers, deadline);
    }

    // BossCard10EtAt/AtB/AtC/AtD (L908/L957/L1006/L1055): alternating 64-count
    // rose rings (etSprite 1, colours 6/2, speed 1.0-2.5) at random headings —
    // every 100/55/100/40 frames by phase; the four At2-style streams
    // (L940-1101) only drive the rotating boss graphic (funcSet 12). Life
    // thresholds 8400/5600/2800 of 11200 map to time quarters 2850/5700/8550.
    private static DanmakuPattern subterraneanRose() {
        final int deadline = 11400;
        var emitters = new ArrayList<DanmakuEmitter>();
        var roseA = shot(STAR, 0.28f, RED, 130);
        var roseB = shot(STAR, 0.22f, WHITE, 130);
        int[][] phases = {{90, 2850, 200}, {2850, 5700, 150}, {5700, 8550, 200}, {8550, 11400, 120}};
        for (int[] phase : phases) {
            int count = phase[1] >= 8550 ? 32 : phase[1] >= 5700 ? 24 : 32;
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
