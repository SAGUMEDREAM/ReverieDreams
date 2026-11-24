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

/** TH15 st06bs.ecl: Junko's Normal and Hard spell-practice entries. */
final class Stage06 {
    private static final String JUNKO = "junko";
    private static final Identifier STAR = ReverieDreams.id("star");
    private static final Identifier BALL = ReverieDreams.id("ball");
    private static final Identifier RICE = ReverieDreams.id("rice");
    private static final int WHITE = 0xEDEDF2;
    private static final int GOLD = 0xE8D48B;
    private static final int ICE = 0xA8D8F0;
    private static final int RED = 0xD03A50;
    private static final int BLUE = 0x3949AB;

    private Stage06() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(6, 78, false, JUNKO, "「掌中纯光」", 3300, () -> purePalmLight(false)),
                card(6, 78, true, JUNKO, "「掌中纯光」", 3300, () -> purePalmLight(true)),
                card(6, 82, false, JUNKO, "「杀意之百合」", 3300, () -> lilyOfMurder(false)),
                card(6, 82, true, JUNKO, "「杀意之百合」", 3300, () -> lilyOfMurder(true)),
                card(6, 86, false, JUNKO, "「原始的神灵界」", 3600, () -> divineSpiritWorld(false)),
                card(6, 86, true, JUNKO, "「现代的神灵界」", 3600, () -> divineSpiritWorld(true)),
                card(6, 90, false, JUNKO, "「颤抖冻结之星」", 3000, () -> tremblingFrozenStars(false)),
                card(6, 90, true, JUNKO, "「颤抖冻结之星」", 3000, () -> tremblingFrozenStars(true)),
                card(6, 94, false, JUNKO, "「纯粹的狂气」", 3600, () -> pureMadness(false)),
                card(6, 94, true, JUNKO, "「纯粹的狂气」", 3600, () -> pureMadness(true)),
                card(6, 98, false, JUNKO, "「满溢的瑕秽」", 1800, () -> overflowingImpurity(false)),
                card(6, 98, true, JUNKO, "「满溢的瑕秽」", 1800, () -> overflowingImpurity(true)),
                card(6, 102, false, JUNKO, "纯符「纯粹弹幕地狱」", 7200, () -> pureBulletHell(false)),
                card(6, 102, true, JUNKO, "纯符「纯粹的弹幕地狱」", 7200, () -> pureBulletHell(true)));
    }

    // st06bs.ecl BossCard1 (738-907): three dense light rings every 100f — 96 (N) /
    // 108 (H) bullets at a random heading plus a companion echo ring (BossCard1_at,
    // 835-871) — then three 33-bullet rings marching 3.9° per shot every 10f
    // (BossCard1_at2, 873-907). Speed 0.5 px/f; the whole cycle repeats every 640f.
    private static DanmakuPattern purePalmLight(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var lightRing = shot(STAR, 0.05f, WHITE, 110);
        var lightEcho = shot(STAR, 0.05f, GOLD, 110);
        for (int volley = 0; volley < 3; volley++) {
            int at = 156 + volley * 102;
            var ring = fire(EmitShape.ring(hard ? 108 : 96, 0f, true), lightRing, at, 0, 1);
            emitters.add(sweep(ring, 0, volley * 137f, volley * 137f));
            emitters.add(fire(EmitShape.ring(hard ? 108 : 96, 0f, true), lightEcho, at + 3, 0, 1));
        }
        var marchRing = shot(STAR, 0.05f, WHITE, 110);
        for (int i = 0; i < 3; i++) {
            var cue = fire(EmitShape.ring(33, 0f, true), marchRing, 420 + i * 12, 0, 1);
            emitters.add(sweep(cue, 0, i * 3.9f, i * 3.9f));
        }
        return loop(emitters, 3300, 156, 642);
    }

    // st06bs.ecl BossCard2 (909-1205): curving single "lily" seeds fly at the player
    // every 130f (N) / 110f (H) (BossCard2_at, 1016-1040); on reaching a field edge a
    // seed reflects and detonates into a 512px boundary laser, four 28px curving
    // lasers at ±30°/±45° and a ~100-bullet rice cone around the inward heading
    // (BossCard2_at2/_at3, 1042-1138), with a closing 3-way seed fan every cycle
    // (BossCard2_atb, 1140-1205). Bounce-on-contact cannot be expressed, so each
    // cycle detonates on a rotating edge instead.
    private static DanmakuPattern lilyOfMurder(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var lasers = new ArrayList<DanmakuLaser>();
        int cycle = hard ? 246 : 230;
        int volleys = hard ? 4 : 3;
        float[] edgeYaw = {90f, 270f, 0f, 180f};
        Vec3[] edgeOrigin = {new Vec3(0, 0.2, -6.9), new Vec3(0, 0.2, 6.9),
                new Vec3(-6.4, 0.2, 0), new Vec3(6.4, 0.2, 0)};
        for (int c = 0; 32 + c * cycle + 170 < 1100; c++) {
            int start = 32 + c * cycle;
            float inward = edgeYaw[c % 4];
            Vec3 origin = edgeOrigin[c % 4];
            for (int v = 0; v < volleys; v++) {
                var seed = shot(BALL, 0.6f, RED, 50,
                        MotionSegment.rotate(0, v % 2 == 0 ? 1.2f : -1.2f));
                emitters.add(fire(EmitShape.fan(1, 0f), seed, start * 3 + v * (hard ? 111 : 132), 0, 1));
            }
            int burst = start + (hard ? 146 : 150);
            lasers.add(laser(burst, 0, inward, 17f, 1.6f, 7, 10, 0f, RED).withOffset(origin));
            for (int side = 0; side < 4; side++) {
                float offset = (side < 2 ? -1 : 1) * (side % 2 == 0 ? 30f : 45f);
                lasers.add(laser(burst + 2, 0, inward + offset, 14f, 0.93f, 8, 12,
                        side % 2 == 0 ? -2f : 2f, BLUE)
                        .withOffset(origin.add(DanmakuMath.direction(0, inward + offset).scale(0.5))));
            }
            var cone = shot(RICE, 0.2f, WHITE, 34);
            var cue = sweep(fire(EmitShape.fan(12, 90f), cone, burst * 3 + 12, 0, 1),
                    0, inward + c * 37f, inward + c * 37f);
            emitters.add(cue.withOffset(origin));
        }
        return timeline(emitters, lasers, 3300);
    }

    // st06bs.ecl BossCard3 (1207-1441). Normal 原始の神霊界: slow spirit bullets rake
    // inward from all four edges in 16 marching waves per 400f cycle (at/_at2/_at3/_at4,
    // 1291-1441), mirrored left-right each cycle via %F0. Hard 現代の神霊界 replaces
    // them with four corner fountains (BossCard3_at5, 1443-1483) that step 15px
    // outward per wave and spray 8 random-direction bullets per wave. Speed ramps
    // 0.2 -> 1.4 px/f after 120f; lifetime 200f.
    private static DanmakuPattern divineSpiritWorld(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var spirit = shot(BALL, 0.02f, WHITE, 67, MotionSegment.setSpeed(40, 0.14f));
        if (!hard) {
            for (int cycle = 0; cycle < 2; cycle++) {
                float f0 = cycle == 0 ? 1f : -1f;
                int base = cycle * 402 + 120;
                for (int w = 0; w < 16; w++) {
                    emitters.add(sweep(fire(EmitShape.fan(5, 12f), spirit, base + w * 3, 0, 1), 0, 90f, 90f)
                            .withOffset(new Vec3(f0 * (0.4f * w - 1.07f), 0.2, -7.47f + 0.53f * w)));
                    emitters.add(sweep(fire(EmitShape.fan(5, 12f), spirit, base + w * 3, 0, 1), 0, 270f, 270f)
                            .withOffset(new Vec3(f0 * (1.07f - 0.4f * w), 0.2, 7.47f - 0.6f * w)));
                    emitters.add(sweep(fire(EmitShape.fan(8, 20f), spirit, base + w * 3, 0, 1), 0, 0f, 0f)
                            .withOffset(new Vec3(-6.4f + 0.6f * w, 0.2, f0 * -1.6f + (w % 3 - 1) * 2.13f)));
                    emitters.add(sweep(fire(EmitShape.fan(8, 20f), spirit, base + w * 3, 0, 1), 0, 180f, 180f)
                            .withOffset(new Vec3(6.4f - 0.6f * w, 0.2, f0 * 1.6f + (w % 3 - 1) * 2.13f)));
                }
            }
            return loop(emitters, 3600, 0, 804);
        }
        float[] cornerX = {-6.4f, 6.4f, -6.4f, 6.4f};
        float[] cornerZ = {7.47f, 7.47f, -7.47f, -7.47f};
        float[] cornerYaw = {0f, 270f, 90f, 180f};
        for (int corner = 0; corner < 4; corner++) {
            Vec3 anchor = new Vec3(cornerX[corner], 0.2, cornerZ[corner]);
            for (int w = 0; w < 16; w++) {
                Vec3 dir = DanmakuMath.direction(0, cornerYaw[corner]);
                var cue = fire(EmitShape.ring(8, 0f, true), spirit, 120 + w * 3, 0, 1);
                emitters.add(cue.withOffset(anchor.add(dir.scale(0.5f * w))));
            }
        }
        return loop(emitters, 3600, 0, 402);
    }

    // st06bs.ecl BossCard4 (1485-1577): every 91f (N) / 117f (H) a random-heading
    // star ring — 42->80 ways (N) / 66->88 (H), +6 per volley — fires five (N) /
    // seven (H) times 13f apart, each repeat rotated by half the ring spacing so
    // successive rings interlock (BossCard4_at, 1553-1578). Speed 1.5 px/f.
    private static DanmakuPattern tremblingFrozenStars(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        int period = hard ? 117 : 93;
        int repeats = hard ? 7 : 5;
        int ways = hard ? 66 : 42;
        int cap = hard ? 88 : 80;
        var star = shot(STAR, 0.15f, ICE, 90);
        for (int volley = 0; 96 + volley * period + 39 < 2990; volley++) {
            int count = Math.min(ways + 6 * volley, cap);
            int at = 96 + volley * period;
            float step = 180f / count;
            for (int k = 0; k < repeats; k++) {
                float rot = volley * 47f + k * step;
                var cue = fire(EmitShape.ring(count, 0f, true), star, at + k * 13, 0, 1);
                emitters.add(sweep(cue, 0, rot, rot));
            }
        }
        return timeline(emitters, 3000);
    }

    // st06bs.ecl BossCard5 (1580-1687): every 660f cycle fires a wide and a thin
    // curving-laser storm 110f apart (BossCard5_at, 1656-1687) — 32 lasers spaced
    // 11.25° around a random heading, 16px wide, speed 6 px/f, each S-curving at
    // 8°/f before reversing; the storm mirrors its curl every cycle via %F0. The
    // mod renders each laser as a static beam with a constant average curl.
    private static DanmakuPattern pureMadness(boolean hard) {
        var lasers = new ArrayList<DanmakuLaser>();
        for (int cycle = 0; 32 + cycle * 220 + 130 < 1200; cycle++) {
            for (int half = 0; half < 2; half++) {
                int index = cycle * 2 + half;
                float curl = index % 2 == 0 ? 2f : -2f;
                float base = index * 137f;
                int at = 32 + cycle * 220 + half * 110;
                for (int i = 0; i < 32; i++) {
                    lasers.add(laser(at, 0, base + i * 11.25f, 20f, 0.53f, 8, 20, curl,
                            half == 0 ? RED : BLUE));
                }
            }
        }
        return timeline(List.of(), lasers, 3600);
    }

    // st06bs.ecl BossCard6 (1689-1818): stars bloom on a 48px circle around the
    // player every 120f, tightening to 50f by the end (floatTime, 1745); 22 (N) /
    // 26 (H) bullets at a random heading, 1.5 px/f decaying after 120f. The
    // player-centred origin is approximated with a point 3 blocks ahead of the
    // caster (BossCard6_at, 1766-1791; ath is Lunatic-only and dropped).
    private static DanmakuPattern overflowingImpurity(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var bloom = shot(STAR, 0.15f, WHITE, 67, MotionSegment.setSpeed(30, 0.06f));
        int at = 32;
        for (int k = 0; at < 590; k++) {
            float base = k * 137f;
            var cue = fire(EmitShape.ring(hard ? 26 : 22, 1.6f, true), bloom, at * 3, 0, 1);
            emitters.add(sweep(cue, 0, base, base).withOffset(new Vec3(0, 0.2, -3.0f)));
            at += Math.round(40 - 23f * k / 20f);
        }
        return timeline(emitters, 1800);
    }

    // st06bs.ecl BossCard7 (1821-2185): the finale cycles four phases gated by
    // survival time (1800f/3600f/5400f caps in the main loop) — every 30f a 22 (N) /
    // 24 (H) ring drifts down-screen from a random point around the boss (at,
    // 1916-1955); every 40f twin towers at ±96px spray 10-bullet random rings
    // (at2/_at2b, 1957-2059); every 110f/100f twin towers at ±160px throw 52-bullet
    // rings (at3/_at3b, 2061-2163); and the last third fires a 24-bullet ring on a
    // 64px radius every 10f (at4, 2165-2185).
    private static DanmakuPattern pureBulletHell(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var drift = shot(STAR, 0.2f, WHITE, 60);
        for (int at = 32; at < 600; at += 10) {
            Vec3 dir = DanmakuMath.direction(0, at * 137f);
            emitters.add(fire(EmitShape.ring(hard ? 24 : 22, 0f, true), drift, at * 3, 0, 1)
                    .withOffset(dir.scale(1.07f).add(0, 0.2, 0)));
        }
        var towerRain = shot(STAR, hard ? 0.14f : 0.1f, GOLD, 70);
        for (int at = 600; at < 1200; at += 13) {
            Vec3 dir = DanmakuMath.direction(0, at * 173f);
            emitters.add(fire(EmitShape.ring(10, 0f, true), towerRain, at * 3, 0, 1)
                    .withOffset(dir.scale(1.07f).add(new Vec3(3.2f, 0.2, -2.67f))));
            emitters.add(fire(EmitShape.ring(10, 0f, true), towerRain, at * 3 + 3, 0, 1)
                    .withOffset(dir.scale(-1.07f).add(new Vec3(-3.2f, 0.2, -2.67f))));
        }
        var towerWall = shot(STAR, hard ? 0.24f : 0.2f, ICE, 80);
        int wallStep = hard ? 34 : 37;
        for (int at = 1200; at < 1800; at += wallStep) {
            Vec3 dir = DanmakuMath.direction(0, at * 61f);
            emitters.add(fire(EmitShape.ring(52, 0f, true), towerWall, at * 3, 0, 1)
                    .withOffset(dir.scale(1.07f).add(new Vec3(5.33f, 0.2, -2.13f))));
            emitters.add(fire(EmitShape.ring(52, 0f, true), towerWall, at * 3 + 3, 0, 1)
                    .withOffset(dir.scale(-1.07f).add(new Vec3(-5.33f, 0.2, -2.13f))));
        }
        var hellRing = shot(STAR, 0.4f, RED, 50);
        for (int at = 1800; at < 2400; at += 3) {
            var cue = fire(EmitShape.ring(24, 2.13f, true), hellRing, at * 3, 0, 1);
            emitters.add(sweep(cue, 0, at * 97f, at * 97f));
        }
        return timeline(emitters, 7200);
    }
}
