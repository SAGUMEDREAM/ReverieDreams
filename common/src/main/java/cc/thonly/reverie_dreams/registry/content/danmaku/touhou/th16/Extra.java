package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th16;

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

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th16.Th16PatternSupport.*;

/** TH16 st07bs.ecl: Matara Okina's full-power Extra battle — the secret-god ritual set. */
final class Extra {
    private static final String OKINA = "matara_okina";
    private static final int CYAN = 0x00E3FF;
    private static final int GREY = 0x737373;
    private static final int PALE_VIOLET = 0x8697FF;
    private static final int GREEN = 0x2DFF00;
    private static final int PINK = 0xFD8BFF;
    private static final int LIME = 0x9DFF2A;
    private static final int ROSE = 0xFF8989;
    private static final int YELLOW = 0xFCFD26;
    private static final int RED = 0xFF0000;

    private Extra() {}

    static List<TouhouSpell> spells() {
        return List.of(
                extraCard(7, 109, OKINA, "秘仪「反向呼唤者」", 4200, Extra::reverseInvoker),
                extraCard(7, 110, OKINA, "秘仪「背叛的后方射击」", 4200, Extra::treacherousBackshot),
                extraCard(7, 111, OKINA, "秘仪「弹幕之玉茧」", 4200, Extra::silkCocoon),
                extraCard(7, 112, OKINA, "秘仪「秽那之火」", 4800, Extra::ketanaFire),
                extraCard(7, 113, OKINA, "秘仪「后户狂言」", 4800, Extra::backdoorKyogen),
                extraCard(7, 114, OKINA, "秘仪「摩多罗独钴」", 4800, Extra::mataraDokko),
                extraCard(7, 115, OKINA, "秘仪「七星之剑」", 4800, Extra::sevenStarSword),
                extraCard(7, 116, OKINA, "秘仪「无缘的艺能者」", 5400, Extra::unrelatedPerformer),
                extraCard(7, 117, OKINA, "「背面的暗黑猿乐」", 5400, Extra::darkSarugaku),
                extraCard(7, 118, OKINA, "「无政府弹幕地狱」", 9000, Extra::anarchyBulletHell));
    }

    // BossCard1 (930-1030): reverse invoker — aimed pairs fired from behind (teleport slots,
    // main loop 939-948).
    private static DanmakuPattern reverseInvoker() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var pair = shot(BULLET, 0.15f, CYAN, 170);
        for (int v = 0; v < 18; v++) {
            emitters.add(fire(EmitShape.aim(), pair, 120 + v * 14, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0).withJitter(8f));
        }
        return loop(emitters, 4200, 120, ticks(240));
    }

    // BossCard2 (1224-1330): the betrayal — six 100f waits then a 60/40/30f squeeze, firing
    // 1-count aimed bullets (4/6, 1.5 px/f, 1266-1276).
    private static DanmakuPattern treacherousBackshot() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var shot1 = shot(BULLET, 0.15f, GREY, 160);
        for (int v = 0; v < 24; v++) {
            int gap = v < 6 ? 33 : (v < 12 ? 20 : 10);
            emitters.add(fire(EmitShape.aim(), shot1, 120 + v * gap, 0, 1).withAim(EmitAim.TARGET, 0, 0));
        }
        return loop(emitters, 4200, 120, ticks(300));
    }

    // BossCard3 (1396-1470): the cocoon — slow twin spirals curving +-1.35 deg/f for 240f
    // (1440-1444) plus a roving silk familiar (ins_91 3600f, 1467).
    private static DanmakuPattern silkCocoon() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var silk = shot(BALL, 0.01f, PALE_VIOLET, 260,
                MotionSegment.setSpeed(ticks(240), 0.16f));
        for (int v = 0; v < 20; v++) {
            float yaw = 30f * v;
            emitters.add(sweep(fire(EmitShape.aim(), silk, 120 + v * 6, 0, 1).withAim(EmitAim.TARGET, 0, 0),
                    0, yaw, yaw));
        }
        var cocoon = DanmakuFamiliar.of(new Vec3(0, 0.2, -4), DanmakuPattern.of())
                .withOrbit(4f, 0.8f, 0f).withDelay(ticks(120)).withDuration(ticks(3600));
        return new DanmakuPattern(emitters).withFamiliars(List.of(cocoon))
                .withTiming(new cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming(
                        ticks(4200), ticks(120), ticks(120)));
    }

    // BossCard4 (1508-1570): ketana fire — aimed flames (25/0, 1.5 px/f) every 3f alternating
    // with a splitter def every 4f, both curving to the player (1557-1563).
    private static DanmakuPattern ketanaFire() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var flame = shot(FIREBALL, 0.15f, GREEN, 170, MotionSegment.rotate(ticks(4), -1f));
        var ember = shot(PELLET, 0.15f, LIME, 170, MotionSegment.rotate(ticks(4), 1f));
        for (int v = 0; v < 40; v++) {
            emitters.add(fire(EmitShape.aim(), flame, 120 + v * 3, 0, 1).withAim(EmitAim.TARGET, 0, 0));
            if (v % 2 == 1) {
                emitters.add(fire(EmitShape.aim(), ember, 120 + v * 3 + 2, 0, 1).withAim(EmitAim.TARGET, 0, 0));
            }
        }
        return loop(emitters, 4800, 120, ticks(120));
    }

    // BossCard5 (1599-1672): backdoor kyogen — wobbling bullets with three stacked decel/curve
    // pairs (1639-1642) every 2f, then 5-count (4/2, 2.5 px/f) fan bursts (1663-1670).
    private static DanmakuPattern backdoorKyogen() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var wobble = shot(BALL, 0.1f, CYAN, 200,
                MotionSegment.rotate(ticks(8), 0.87f), MotionSegment.rotate(ticks(33), -0.87f));
        for (int v = 0; v < 45; v++) {
            emitters.add(fire(EmitShape.aim(), wobble, 120 + v * 2, 0, 1).withAim(EmitAim.TARGET, 0, 0));
        }
        var fan = shot(BULLET, 0.25f, PINK, 160);
        for (int v = 0; v < 6; v++) {
            emitters.add(fire(EmitShape.fan(5, 10f), fan, 260 + v * 40, 0, 1).withAim(EmitAim.TARGET, 0, 0));
        }
        return loop(emitters, 4800, 120, ticks(210));
    }

    // BossCard6 (1707-1760): matara dokko — bullets from behind at 1.0+dist*0.1 px/f spawning
    // angled sub-shots every 16f (etExSet2 1753).
    private static DanmakuPattern mataraDokko() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var bolt = shot(BULLET, 0.15f, PALE_VIOLET, 180,
                MotionSegment.split(ticks(16), new cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionPayload(
                        BULLET, PINK, 0.85f, 0.16f, 1, 15f, 120)));
        for (int v = 0; v < 24; v++) {
            emitters.add(fire(EmitShape.aim(), bolt, 120 + v * 8, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0).withOffset(new Vec3(0, 0.2, 3.5)).withJitter(6f));
        }
        return loop(emitters, 4800, 120, ticks(190));
    }

    // BossCard7 (1788-1870): seven-star sword — 32-count rings (16/6, 1.0 px/f) every 100f plus
    // a 512 px-long laser (32->512, 16 px wide, 60f/30f) at the player (1853-1858).
    private static DanmakuPattern sevenStarSword() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var lasers = new ArrayList<DanmakuLaser>();
        for (int v = 0; v < 7; v++) {
            int at = ticks(120) + v * ticks(55);
            float yaw = 360f * v / 7f;
            lasers.add(laser(at, 0f, yaw, 16f, 0.55f, ticks(30), ticks(60), 0f, PALE_VIOLET));
            var ring = shot(BALL, 0.1f, GREY, 200);
            emitters.add(fire(EmitShape.ring(32, 0f, true), ring, at, 0, 1).withAim(EmitAim.TARGET, 0, 0));
        }
        return timeline(emitters, lasers, 4800);
    }

    // BossCard8 (1893-1958): unrelated performer — endless 1.5 px/f aimed shots (4/6).
    private static DanmakuPattern unrelatedPerformer() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var shot1 = shot(BULLET, 0.15f, ROSE, 170);
        for (int v = 0; v < 30; v++) {
            emitters.add(fire(EmitShape.aim(), shot1, 120 + v * 10, 0, 1).withAim(EmitAim.TARGET, 0, 0));
        }
        return loop(emitters, 5400, 120, ticks(300));
    }

    // BossCard9 (2065-2137): dark sarugaku — rotating multi-column streams ($A defs x $B count,
    // 2.3 px/f, +-0.67 deg/f curl, offsets %E*%D, 28f cadence) — the noh stage barrage.
    private static DanmakuPattern darkSarugaku() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var stream = shot(BALL, 0.23f, PALE_VIOLET, 200, MotionSegment.rotate(ticks(8), -0.45f));
        for (int v = 0; v < 16; v++) {
            float yaw = 45f * v;
            var cue = fire(EmitShape.ring(8, 0f, true), stream, 120 + v * 28, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0);
            emitters.add(sweep(cue.withOffset(new Vec3((v % 4 - 1.5f) * 4f, 0.2, 1f)), 0, yaw, yaw));
        }
        return loop(emitters, 5400, 120, ticks(450));
    }

    // BossCard10 (989-1060): anarchy bullet hell — the 9000f survival: endless upward 1x3-way
    // shots (12/4, 3.0/2.0 px/f, 24 px dist) rotating +-5 deg per volley every frame plus an
    // aim-mode-10 second def (1060-1061).
    private static DanmakuPattern anarchyBulletHell() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var hell = shot(BULLET, 0.3f, YELLOW, 160, MotionSegment.setSpeed(ticks(40), 0.2f));
        for (int v = 0; v < 40; v++) {
            float yaw = 5f * v;
            var cue = fire(EmitShape.fan(3, 6f), hell, v * 2, 0, 1).withAim(EmitAim.TARGET, 0, 0);
            emitters.add(sweep(cue, 0, yaw, yaw));
            if (v % 4 == 0) {
                emitters.add(fire(EmitShape.ring(1, 0f, true),
                        shot(PELLET, 0.16f, RED, 150), v * 2 + 1, 0, 1)
                        .withAim(EmitAim.TARGET, 0, 0).withJitter(14f));
            }
        }
        return loop(emitters, 9000, 120, ticks(80));
    }
}
