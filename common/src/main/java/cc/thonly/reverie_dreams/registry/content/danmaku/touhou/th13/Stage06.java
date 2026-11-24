package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th13;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuFamiliar;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuLaser;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th13.Th13PatternSupport.*;

/** TH13 st06mbs/st06bs.ecl: Tojiko's mausoleum gate and Toyosatomimi no Miko's finale. */
final class Stage06 {
    private static final String MIKO = "toyosatomimi_no_miko";
    private static final String TOJIKO = "soga_no_toziko";
    // Sampled from th13 bullet.anm: spr32 = white, spr30 = 0xFFB900 gold, spr5 = 0x8B77FF violet,
    // spr20 = 0xF600E1 magenta, spr18 = red, spr24 = cyan, spr6 = 0x8697FF pale violet.
    private static final int WHITE = 0xFFFFFF;
    private static final int GOLD = 0xFFB900;
    private static final int VIOLET = 0x8B77FF;
    private static final int MAGENTA = 0xF600E1;
    private static final int RED = 0xFF0000;
    private static final int CYAN = 0x00FFFF;
    private static final int PALE_VIOLET = 0x8697FF;

    private Stage06() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(6, 60, false, TOJIKO, "雷矢「贺古治旋风」", 2700, Stage06::gateCyclone),
                card(6, 78, false, MIKO, "名誉「十二阶之色彩」", 3300, () -> twelveCapColors(4)),
                card(6, 78, true, MIKO, "名誉「十二阶之冠位」", 3300, () -> twelveCapColors(8)),
                card(6, 82, false, MIKO, "仙符「日出之处的道士」", 3600, () -> risingSun(false)),
                card(6, 82, true, MIKO, "仙符「日出之处的天子」", 3600, () -> risingSun(true)),
                card(6, 86, false, MIKO, "召唤「豪族乱舞」", 3900, Stage06::clanDance),
                card(6, 86, true, MIKO, "召唤「豪族乱舞」", 3900, Stage06::clanDance),
                card(6, 90, false, MIKO, "秘宝「斑鸠寺之天球仪」", 3600, () -> celestialSphere(false)),
                card(6, 90, true, MIKO, "秘宝「圣德太子的欧帕兹」", 3600, () -> celestialSphere(false)),
                extraCard(6, 125, MIKO, "「神灵大宇宙」", 3600, () -> celestialSphere(true)),
                card(6, 94, false, MIKO, "光符「救世观音之光照」", 3600, () -> guanyinLight(false)),
                card(6, 94, true, MIKO, "光符「癖性闪光」", 3600, () -> guanyinLight(true)),
                card(6, 98, false, MIKO, "眼光「十七条激光」", 4200, () -> seventeenLasers(false)),
                card(6, 98, true, MIKO, "神光「以不逆为宗」", 4200, () -> seventeenLasers(true)),
                card(6, 102, false, MIKO, "「星降神灵庙」", 7200, Stage06::fallingStars),
                card(6, 102, true, MIKO, "「新生的神灵」", 7200, Stage06::fallingStars));
    }

    // st06mbs MBossCard1 (86-177): 16/20-count aimed thunder fans (5.0 px/f, 1.875 deg/f curl,
    // arrows fork sideways every 16f) plus an ambient 16-ring each cycle (MBossCard1_at2 155-177).
    private static DanmakuPattern gateCyclone() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var arrow = shot(ARROWHEAD, 0.5f, PALE_VIOLET, 90);
        for (int v = 0; v < 10; v++) {
            var cue = fire(EmitShape.fan(16, 4f), arrow, v * 7, 0, 1).withAim(EmitAim.TARGET, 0, 0);
            emitters.add(sweep(cue, 0, 1.875f * v, 1.875f * v));
        }
        var rain = shot(ARROWHEAD, 0.1f, GOLD, 170);
        emitters.add(fire(EmitShape.ring(16, 0f, true), rain, 30, 0, 1).withAim(EmitAim.TARGET, 0, 0));
        return loop(emitters, 2700, 60, 70);
    }

    // st06bs BossCard1 (786-1018): pillar familiars rise at +-192/+-96 px (4 on E/N), 8 columns
    // on H, 6 on L (818-841); each pillar (BossCard1_EtAt 851-965) fires rising cascades — five
    // waves of 1-2 bullets at 0.5 -> 1.2 px/f with random +-16 px scatter and 30 deg/f curl —
    // while column tops drip aimed 1-bullets at 2.2 px/f accelerating +0.16 px/f^2 (at 996-1018).
    private static DanmakuPattern twelveCapColors(int columns) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var familiars = new ArrayList<DanmakuFamiliar>();
        float[] xs;
        if (columns == 8) {
            xs = new float[]{-12.4f, -9.6f, -6.9f, -4.1f, 4.1f, 6.9f, 9.6f, 12.4f};
        } else if (columns == 6) {
            xs = new float[]{-12f, -8.5f, -5f, 5f, 8.5f, 12f};
        } else {
            xs = new float[]{-12f, -6f, 6f, 12f};
        }
        var cascade = shot(PELLET, 0.05f, MAGENTA, 200, MotionSegment.setSpeed(ticks(120), 0.12f));
        var drip = shot(BALL, 0.22f, RED, 170, MotionSegment.setSpeed(ticks(40), 0.38f));
        for (int k = 0; k < xs.length; k++) {
            var pillar = fire(EmitShape.fan(1, 2f), cascade, 0, 260, 7).withAim(EmitAim.TARGET, 0, 0);
            var pillarPattern = DanmakuPattern.of(sweep(pillar, 0, 0f, 420f));
            familiars.add(DanmakuFamiliar.of(new Vec3(xs[k], 0.2, 2.6), pillarPattern)
                    .withDelay(ticks(170)).withDuration(ticks(2800)));
            for (int v = 0; v < 9; v++) {
                emitters.add(fire(EmitShape.aim(), drip, 170 + v * 21, 0, 1)
                        .withAim(EmitAim.TARGET, 0, 0).withOffset(new Vec3(xs[k], 0.2, 1.6)).withJitter(5f));
            }
        }
        return new DanmakuPattern(emitters).withFamiliars(familiars)
                .withTiming(new cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming(
                        ticks(3300), ticks(170), ticks(190)));
    }

    // st06bs BossCard2 (1020-1227): every ~360f cycle a burst of aimed 24/28/26/28-count rings
    // (1.0 -> 1.3/1.5 px/f speed tiers) whose bullets curve +-0.5 deg/f across frames 8-200
    // (1116, the +-0.0087 rad %A); N/H fire a second mirrored ring, L/OD add a third slow ring
    // and a 28-count 0.8 px/f ring (1166-1223).
    private static DanmakuPattern risingSun(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var sun = shot(STAR, 0.1f, WHITE, 240, MotionSegment.setSpeed(ticks(120), 0.15f),
                MotionSegment.rotate(ticks(8), 0.5f));
        var sunMirrored = shot(STAR, 0.1f, WHITE, 240, MotionSegment.setSpeed(ticks(120), 0.15f),
                MotionSegment.rotate(ticks(8), -0.5f));
        emitters.add(fire(EmitShape.ring(hard ? 26 : 24, 0f, true), sun, 120, 0, 1).withAim(EmitAim.TARGET, 0, 0));
        emitters.add(fire(EmitShape.ring(hard ? 26 : 24, 0f, true), sunMirrored, 121, 0, 1).withAim(EmitAim.TARGET, 0, 0));
        if (hard) {
            var halo = shot(STAR, 0.08f, WHITE, 260);
            emitters.add(fire(EmitShape.ring(28, 0f, true), halo, 122, 0, 1).withAim(EmitAim.TARGET, 0, 0));
        }
        return loop(emitters, 3600, 120, 120);
    }

    // st06bs BossCard3 (1229-1453): two clan phantoms (En1 +128 px / En2 -128 px) dash and each
    // fire 3 waves of 8-count x W-way aimed fans (W = 2 -> 10 growing per wave, En_at 1374-1390,
    // speed 1.0-3.0 px/f). Ported as mirrored familiars with growing-way fans.
    private static DanmakuPattern clanDance() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var familiars = new ArrayList<DanmakuFamiliar>();
        var fan = shot(BULLET, 0.1f, GOLD, 160, MotionSegment.setSpeed(ticks(60), 0.24f));
        for (int side = 0; side < 2; side++) {
            var ems = new ArrayList<DanmakuEmitter>();
            for (int wave = 0; wave < 3; wave++) {
                int ways = Math.min(2 + wave * 4, 10);
                var cue = fire(EmitShape.fan(ways, 30f), fan, wave * 60, 0, 1).withAim(EmitAim.TARGET, 0, 0);
                ems.add(sweep(cue, 0, 5f * wave, 5f * wave));
            }
            familiars.add(DanmakuFamiliar.of(new Vec3(side == 0 ? 8 : -8, 0.2, 1.4),
                    new DanmakuPattern(ems)).withDelay(ticks(180)).withDuration(ticks(3200)));
        }
        var cover = shot(PELLET, 0.12f, GOLD, 150);
        emitters.add(fire(EmitShape.ring(12, 0f, true), cover, 180, 0, 1));
        return new DanmakuPattern(emitters).withFamiliars(familiars)
                .withTiming(new cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming(
                        ticks(3900), ticks(180), ticks(200)));
    }

    // st06bs BossCard4 (1455-1637): the celestial sphere — armillary orbs (sprite 235) spawn
    // every frame for 50/55/75 volleys while their orbit radius grows 3-6 px per orb (at
    // 1569-1604), wrapping around the player's heading; Overdrive interlaces a second zig-zag
    // spiral (at2 1606-1637, +-222.5 deg steps) and opens with a 32-ring. Ported as a growing
    // spiral of slow bubbles with an OD zig-zag layer.
    private static DanmakuPattern celestialSphere(boolean overdrive) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var orbs = new ArrayList<DanmakuEmitter>();
        int volleys = overdrive ? 50 : 40;
        for (int v = 0; v < volleys; v++) {
            float radius = 1.2f + 0.2f * v;
            float yaw = 24f * v;
            var orb = shot(BUBBLE, 0.05f, VIOLET, 220);
            var cue = fire(EmitShape.ring(4, 0f, true), orb, v * 2, 0, 1);
            emitters.add(sweep(cue.withOffset(new Vec3(Math.cos(Math.toRadians(yaw)) * radius, 0.2,
                    Math.sin(Math.toRadians(yaw)) * radius)), 0, yaw, yaw));
        }
        orbs.addAll(emitters);
        if (overdrive) {
            var zig = shot(BALL, 0.02f, VIOLET, 240, MotionSegment.setSpeed(ticks(60), 0.14f));
            for (int v = 0; v < 24; v++) {
                float yaw = v % 2 == 0 ? 0f : 222.5f;
                var cue = fire(EmitShape.aim(), zig, 60 + v * 6, 0, 1).withAim(EmitAim.TARGET, 0, 0);
                orbs.add(sweep(cue, 0, yaw, yaw + 222.5f));
            }
        }
        return timeline(orbs, 3600);
    }

    // st06bs BossCard5 (1639-1768): the four-light spiral — endless single bullets cycling
    // sprites 32/6, 32/4, 32/5, 32/2 aimed with a 222.5 deg zig-zag per volley (1711-1753),
    // speed 0.3 px/f accelerating 0.025 (E) / 0.058 px/f^2, 2f cadence on E and 1f otherwise.
    private static DanmakuPattern guanyinLight(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var light = shot(BULLET, hard ? 0.03f : 0.03f, WHITE, 200,
                MotionSegment.setSpeed(ticks(120), hard ? 0.19f : 0.13f));
        for (int v = 0; v < 48; v++) {
            float yaw = 222.5f * v;
            var cue = fire(EmitShape.aim(), light, v * (hard ? 1 : 2), 0, 1).withAim(EmitAim.TARGET, 0, 0);
            emitters.add(sweep(cue, 0, yaw, yaw + 222.5f));
        }
        return loop(emitters, 3600, 120, hard ? 48 : 96);
    }

    // st06bs BossCard6 (1770-1983): seventeen full-field lasers (480 px long, 24 px wide, 30f
    // warning + 60/90f active, ins_700/701 with 17 fires every 2f each rotated +222.5 deg, at
    // 1901-1931) sweep in alternating groups; H/L add twin 8-ribbon laser clusters staggered
    // +-8 deg (at2 1933-1983) with cascade bullet sprays (EtAt 1833-1899).
    private static DanmakuPattern seventeenLasers(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var lasers = new ArrayList<DanmakuLaser>();
        int active = hard ? ticks(90) : ticks(60);
        for (int wave = 0; wave < 4; wave++) {
            int at = ticks(120) + wave * ticks(hard ? 75 : 120);
            for (int k = 0; k < 17; k++) {
                float yaw = 222.5f * (k + wave * 3);
                lasers.add(laser(at + k * 2, 0f, yaw, 16f, 0.8f, ticks(30), active, 0f, MAGENTA));
            }
            if (hard) {
                for (int k = 0; k < 17; k++) {
                    float yaw = 222.5f * (k + wave * 3);
                    lasers.add(laser(at + k * 2, 0f, yaw - 8f, 11f, 0.5f, ticks(40), ticks(30), 0.3f, WHITE));
                    lasers.add(laser(at + k * 2, 0f, yaw + 8f, 11f, 0.5f, ticks(40), ticks(30), -0.3f, WHITE));
                }
            }
            var spark = shot(PELLET, 0.02f, WHITE, 150, MotionSegment.setSpeed(ticks(120), 0.12f));
            for (int v = 0; v < 12; v++) {
                emitters.add(fire(EmitShape.ring(1, 0f, true), spark, at + v * 7, 0, 1)
                        .withAim(EmitAim.TARGET, 0, 0).withJitter(6f));
            }
        }
        return timeline(emitters, lasers, 4200);
    }

    // st06bs BossCard7 (1985-2168): the survival finale — an endless rain of 2.5/3.2 px/f stars
    // from random columns (skipping +-24 px around the player, 2091), aimed straight down with a
    // 30 deg/f curl (2094); spawn intervals tighten 30/15/12/8 -> 8/3/3/2 -> 4/2/2/2 frames over
    // the 7200f deadline (2065-2144). Star spirits drift alongside (ins_91, 2082).
    private static DanmakuPattern fallingStars() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var star = shot(STAR, 0.25f, WHITE, 220, MotionSegment.rotate(0, 1.05f));
        var starBright = shot(STAR, 0.25f, WHITE, 220, MotionSegment.rotate(0, 1.05f));
        int window = 240;
        for (int t = 0; t < window; t += 4) {
            var cue = fire(EmitShape.aim(), t % 8 == 0 ? starBright : star, t, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0).withJitter(10f);
            cue = cue.withOffset(new Vec3((t % 25 - 12) * 0.55f, 0.2, -9f));
            emitters.add(sweep(cue, 0, 0f, 0f));
        }
        var familiars = new ArrayList<DanmakuFamiliar>();
        for (int k = 0; k < 5; k++) {
            float x = -6f + 3f * k;
            familiars.add(DanmakuFamiliar.of(new Vec3(x, 0.2, -3f), DanmakuPattern.of())
                    .withDelay(ticks(120) + k * ticks(30)).withDuration(ticks(1000)));
        }
        return new DanmakuPattern(emitters).withFamiliars(familiars)
                .withTiming(new cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming(
                        ticks(7200), ticks(120), ticks(window)));
    }
}
