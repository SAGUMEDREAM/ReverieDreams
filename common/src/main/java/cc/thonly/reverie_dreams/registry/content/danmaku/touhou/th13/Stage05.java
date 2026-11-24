package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th13;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuFamiliar;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionPayload;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th13.Th13PatternSupport.*;

/**
 * TH13 st05mbs/st05bs.ecl: Soga no Tojiko's mausoleum-gate cameo and Mononobe no Futo's stage-5
 * boss cards. Futo's ritual boat (磐舟) spawns as an entity that keeps firing — ported as a
 * side-to-side familiar.
 */
final class Stage05 {
    private static final String TOJIKO = "soga_no_toziko";
    private static final String FUTO = "mononobe_no_futo";
    // Sampled from th13 bullet.anm: spr30 = 0xFFB900 gold, spr8 = 0x28FFFF cyan, spr18 = red,
    // spr24 = 0x00FFFF cyan fire, spr27 = 0x80FF08 green fire, spr11 = 0x9DFF2A lime,
    // spr6 = 0x8697FF pale violet (Tojiko arrows).
    private static final int GOLD = 0xFFB900;
    private static final int CYAN = 0x28FFFF;
    private static final int RED = 0xFF0000;
    private static final int FIRE_CYAN = 0x00FFFF;
    private static final int FIRE_GREEN = 0x80FF08;
    private static final int LIME = 0x9DFF2A;
    private static final int VIOLET_ARROW = 0x8697FF;
    private static final int WHITE = 0xFFFFFF;

    private Stage05() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(5, 59, false, TOJIKO, "雷矢「贺古治旋风」", 2700, () -> thunderArrow(false)),
                card(5, 59, true, TOJIKO, "雷矢「贺古治龙卷」", 2700, () -> thunderArrow(true)),
                extraCard(5, 123, TOJIKO, "怨灵「入鹿之雷」", 2700, () -> thunderArrow(true)),
                card(5, 62, false, FUTO, "天符「雨之磐舟」", 3300, () -> heavenlyRockBoat(false)),
                card(5, 62, true, FUTO, "天符「天磐舟升天」", 3300, () -> heavenlyRockBoat(true)),
                card(5, 66, false, FUTO, "投皿「物部的八十平瓮」", 3000, () -> plateThrow(19, 9)),
                card(5, 66, true, FUTO, "投皿「物部的八十平瓮」", 3000, () -> plateThrow(33, 24)),
                card(5, 70, false, FUTO, "炎符「废佛之炎风」", 3000, Stage05::flameCurtain),
                card(5, 70, true, FUTO, "炎符「樱井寺炎上」", 3000, Stage05::flameCurtain),
                card(5, 74, false, FUTO, "圣童女「大物忌正餐」", 3600, () -> sacredMeal(false)),
                card(5, 74, true, FUTO, "圣童女「大物忌正餐」", 3600, () -> sacredMeal(false)),
                extraCard(5, 124, FUTO, "圣童女「太阳神的祭品」", 3600, () -> sacredMeal(true)));
    }

    // st05mbs MBossCard1 (134-339): Tojiko hurls 20-volley bursts of 16/20 lightning arrows every
    // ~210f — 5.0 px/f aimed fans spaced 7f with a 1.875 deg/f curl (at 190-229), each arrow
    // forking sideways every 16f (etEx2 +-90 deg, 210-211). L/OD add a second arrow stream with
    // +-45/60 deg forks (at3 266-302); Overdrive zig-zags the aim ([-9981] flip, 172-173). An
    // ambient 16-ring rains every 60f the whole card (at2 231-264).
    private static DanmakuPattern thunderArrow(boolean lunatic) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var arrow = shot(ARROWHEAD, 0.5f, VIOLET_ARROW, 90, fork(0.28f), fork(-0.28f));
        var arrowTornado = shot(ARROWHEAD, 0.5f, GOLD, 90, fork(0.28f), fork(-0.42f));
        for (int v = 0; v < 10; v++) {
            int at = v * 7;
            emitters.add(sweep(fire(EmitShape.fan(lunatic ? 20 : 16, 4f), lunatic ? arrowTornado : arrow, at, 0, 1),
                    0, 1.875f * v, 1.875f * v));
        }
        var rain = shot(ARROWHEAD, 0.1f, GOLD, 170);
        emitters.add(fire(EmitShape.ring(16, 0f, true), rain, 30, 0, 1).withAim(EmitAim.TARGET, 0, 0));
        return loop(emitters, 2700, 60, 70);
    }

    private static MotionSegment fork(float degrees) {
        return MotionSegment.split(ticks(16), new MotionPayload(ARROWHEAD, 0x8697FF, 0.8f, 0.3f, 2, 90f, 60));
    }

    // st05bs BossCard1 (397-617): Futo shuttles the boat across the top (240f runs to +-256 px,
    // 438-487) while both hulls fire. Boss: 18-count rings at 2.2/3.2 px/f with 18 deg/f spin,
    // aimed at the player every 80f (x1 on E, x3 on N/H/L with 10f gaps, 604-613). Ship: a
    // 1-per-frame rain of slow big bullets (0.4 px/f, accelerating 0.0167 px/f^2 over 4-120f)
    // from alternating +-70 px offsets, aimed with a 30 deg/f spin (Ship_at 490-528; the H card
    // doubles the L shots, 555-557).
    private static DanmakuPattern heavenlyRockBoat(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var ring = shot(BALL, hard ? 0.32f : 0.22f, GOLD, 200, MotionSegment.rotate(0, 0.6f));
        for (int v = 0; v < 10; v++) {
            int at = 120 + v * (hard ? 27 : 40);
            int waves = hard ? 3 : 1;
            for (int w = 0; w < waves; w++) {
                emitters.add(fire(EmitShape.ring(18, 0f, true), ring, at + w * 3, 0, 1)
                        .withAim(EmitAim.TARGET, 0, 0));
            }
        }
        var boatShot = shot(BALL, 0.04f, CYAN, 240,
                MotionSegment.setSpeed(ticks(120), 0.2f));
        var boatPattern = DanmakuPattern.of(sweep(
                fire(EmitShape.aim(), boatShot, 0, ticks(2600), hard ? 2 : 3), 0, -1080f, 1080f));
        var ship = DanmakuFamiliar.of(new Vec3(0, 0.2, 4.5), boatPattern)
                .withDelay(ticks(90)).withDuration(ticks(2700));
        var drift = DanmakuFamiliar.of(new Vec3(-8.5, 0.2, 2), DanmakuPattern.of())
                .withOrbit(8.5f, 0.4f, 0f).withDelay(ticks(90)).withDuration(ticks(2700));
        return new DanmakuPattern(emitters).withFamiliars(List.of(ship, drift))
                .withTiming(new cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming(
                        ticks(3300), ticks(120), ticks(120)));
    }

    // st05bs BossCard2 (619-699): plate-throwing cascade — $E = 9/19/24/33 plates fired 1f apart
    // per cycle ([-9985] grows +2 until $D), each a single bullet at 1.0 -> 7+ px/f (+0.2 per
    // plate, 688-689) with random speed scale 0.5-2.0 (flag 8192, 677), aimed at the player with
    // a 20 deg/f curl and +-12 deg lateral drift per throw (692-693); cycles alternate sides.
    private static DanmakuPattern plateThrow(int throws_, int normalThrows) {
        var emitters = new ArrayList<DanmakuEmitter>();
        int count = throws_;
        for (int k = 0; k < count; k++) {
            float speed = 0.1f + 0.02f * k;
            var plate = shot(RICE, speed, RED, 200,
                    MotionSegment.rotate(ticks(4), 0.67f));
            var cue = fire(EmitShape.aim(), plate, 120 + k * 2, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0)
                    .withJitter(25f);
            float drift = (k % 2 == 0 ? 1 : -1) * 12f;
            emitters.add(sweep(cue, 0, drift * (k / 2), drift * (k / 2) + 20f * k));
        }
        return loop(emitters, 3000, 120, 400);
    }

    // st05bs BossCard3 (701-828): the fire-spirit entity (ins_301 BossCard3_at) sweeps 259 rows
    // of flame bullets across the field — 18/24/26 per row spread over 500 px (784), aim flips
    // 222.5 deg between bullets (782, the zig-zag curtain), speed 0.1 accelerating +0.0083
    // px/f^2 (frames 4-120, 767), tall flame sprites near the player column (786-806). Rows
    // tighten 8-($C/4) f apart as the count drops (819-820).
    private static DanmakuPattern flameCurtain() {
        var emitters = new ArrayList<DanmakuEmitter>();
        for (int row = 0; row < 12; row++) {
            int count = Math.max(26 - row, 18);
            int at = 120 + row * (8 + row / 2) * 3;
            var flame = shot(FIREBALL, 0.01f, FIRE_CYAN, 260,
                    MotionSegment.setSpeed(ticks(120), 0.13f));
            var cue = fire(EmitShape.fan(count, 180f), flame, at, 0, 1).withJitter(6f);
            emitters.add(sweep(cue, 0, row % 2 == 0 ? 0f : 180f, row % 2 == 0 ? 180f : 360f));
        }
        return loop(emitters, 3000, 120, ticks(300));
    }

    // st05bs BossCard4 (830-1011): the offering ship rains a slow 3-count spiral every 2f from
    // 0.2 px/f bullets (angle starts down, drifts +-1.2/1.8/2.1 deg per volley by difficulty,
    // 936-945) while every 5th volley (OD) or every 40/20th volley re-aims a 222.5 deg zig-zag
    // spray at 0.1 px/f accelerating (913-927, 976-991). OD swaps the rain sprite to 8/13 with
    // 5 bullets (893-895). Ported as a ship familiar with two emitters.
    private static DanmakuPattern sacredMeal(boolean overdrive) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var rain = shot(BALL, 0.02f, CYAN, 300, MotionSegment.rotate(0, overdrive ? -0.3f : 0.2f));
        for (int v = 0; v < 30; v++) {
            emitters.add(fire(EmitShape.ring(overdrive ? 5 : 3, 0f, true), rain, v * 6, 0, 1));
        }
        var zig = shot(BULLET, 0.01f, LIME, 220, MotionSegment.setSpeed(ticks(60), 0.12f));
        for (int v = 0; v < 9; v++) {
            float yaw = v % 2 == 0 ? 0f : 222.5f;
            emitters.add(sweep(fire(EmitShape.fan(overdrive ? 3 : 1, 4f), zig, 180 + v * 90, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0), 0, yaw, yaw + 222.5f));
        }
        var ship = DanmakuFamiliar.of(new Vec3(0, 0.2, 3.4),
                DanmakuPattern.of(fire(EmitShape.ring(overdrive ? 5 : 3, 0f, true), rain, 0, 20, 6)))
                .withOrbit(3.4f, 0.5f, 0f).withDelay(ticks(120)).withDuration(ticks(3200));
        return new DanmakuPattern(emitters).withFamiliars(List.of(ship))
                .withTiming(new cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming(
                        ticks(3600), ticks(120), ticks(180)));
    }
}
