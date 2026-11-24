package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th16;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th16.Th16PatternSupport.*;

/**
 * TH16 st06bs.ecl: Matara Okina's finale. The four 裏-season cards and the four 7200f survival
 * finales reuse one attack skeleton per season; the source spawns bullets both from Okina and
 * from mirrored player-relative columns (ins_628).
 */
final class Stage06 {
    private static final String OKINA = "matara_okina";
    // Sampled from th16 bullet.anm: spr5 = 0x8B77FF violet (halo), spr8 = 0x28FFFF cyan,
    // spr16 = grey (famine), spr19 = 0xFF00FF magenta, base green 0x2DFF00 for summer fire,
    // white 0xFFFFFF, gold 0xFFB900, red 0xFF0000.
    private static final int WHITE = 0xFFFFFF;
    private static final int GOLD = 0xFFB900;
    private static final int VIOLET = 0x8B77FF;
    private static final int GREEN = 0x2DFF00;
    private static final int MAGENTA = 0xFF00FF;
    private static final int CYAN = 0x28FFFF;
    private static final int PURPLE = 0x8B77FF;
    private static final int RED = 0xFF0000;

    private Stage06() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(6, 70, false, OKINA, "后符「秘神之圣光」", 2700, () -> secretHalo(false)),
                // The Lunatic branch carries its own name; it takes the hard catalog slot.
                card(6, 70, true, OKINA, "后符「绝对秘神之圣光」", 2700, Stage06::secretHalo),
                card(6, 74, false, OKINA, "裏夏「灼热盛夏」", 3000, () -> scorchedSummer(false)),
                card(6, 74, true, OKINA, "裏夏「异常酷暑的焦土」", 3000, () -> scorchedSummer(true)),
                card(6, 78, false, OKINA, "裏秋「饥馑之秋」", 3000, () -> famineAutumn(false)),
                card(6, 78, true, OKINA, "裏秋「异常枯死的饿鬼」", 3000, () -> famineAutumn(true)),
                card(6, 82, false, OKINA, "裏冬「黑色雪人」", 3000, Stage06::blackSnowman),
                card(6, 82, true, OKINA, "裏冬「异常降雪的雪人」", 3000, Stage06::blackSnowman),
                card(6, 86, false, OKINA, "裏春「四月愚者」", 3600, () -> aprilWizard(false)),
                card(6, 86, true, OKINA, "裏春「异常落花的魔术师」", 3600, () -> aprilWizard(true)),
                card(6, 90, false, OKINA, "「裏·微风樱花」", 7200, () -> seasonFinale(CYAN, 0.35f)),
                card(6, 94, false, OKINA, "「裏·完美夏日冰」", 7200, () -> seasonFinale(VIOLET, -0.35f)),
                card(6, 98, false, OKINA, "「裏·疯狂秋风」", 7200, () -> seasonFinale(MAGENTA, 0.35f)),
                card(6, 102, false, OKINA, "「裏·极限之冬」", 7200, () -> seasonFinale(RED, -0.35f)));
    }

    // st06bs BossCard1 (788-870): halo rings (sprite 5, 1.5 px/f) that repeatedly spawn halos
    // of their own (etExSet2 modes 3/4 sub-fires, 853-855) — ported as ring splitters.
    private static DanmakuPattern secretHalo(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var halo = shot(STAR, 0.15f, VIOLET, 200,
                MotionSegment.split(ticks(16), new cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionPayload(
                        STAR, WHITE, 0.8f, 0.12f, 6, 360f, 100)));
        for (int v = 0; v < 10; v++) {
            emitters.add(fire(EmitShape.ring(hard ? 3 : 2, 0f, true), halo, 120 + v * 24, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0));
        }
        return loop(emitters, 2700, 120, ticks(240));
    }

    private static DanmakuPattern secretHalo() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var halo = shot(STAR, 0.18f, VIOLET, 210,
                MotionSegment.split(ticks(14), new cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionPayload(
                        STAR, WHITE, 0.8f, 0.14f, 8, 360f, 100)));
        for (int v = 0; v < 12; v++) {
            emitters.add(fire(EmitShape.ring(4, 0f, true), halo, 120 + v * 20, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0));
        }
        return loop(emitters, 2700, 120, ticks(240));
    }

    // st06bs BossCard2 (947-1015): scorched summer — symmetric mirrored spawns (ins_628 +-%A)
    // of 0.1-0.6 px/f flames aimed at the player with 30 deg/f curl, 9/19/12/16 volleys.
    private static DanmakuPattern scorchedSummer(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var flame = shot(FIREBALL, 0.01f, GREEN, 260,
                MotionSegment.setSpeed(ticks(120), hard ? 0.25f : 0.15f), MotionSegment.rotate(ticks(8), 1f));
        for (int v = 0; v < (hard ? 10 : 5); v++) {
            float x = (v % 2 == 0 ? 1 : -1) * (1.5f + v);
            emitters.add(fire(EmitShape.aim(), flame, 120 + v * 20, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0).withOffset(new Vec3(x, 0.2, 1.2)));
            emitters.add(fire(EmitShape.aim(), flame, 130 + v * 20, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0).withOffset(new Vec3(-x, 0.2, 1.2)));
        }
        return loop(emitters, 3000, 120, ticks(220));
    }

    // st06bs BossCard3 (1118-1170): famine autumn — leaping gaki bullets from the boss plus
    // wide waves.
    private static DanmakuPattern famineAutumn(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var gaki = shot(BULLET, 0.12f, RED, 220, MotionSegment.rotate(0, hard ? -0.7f : 0.5f));
        for (int v = 0; v < 12; v++) {
            emitters.add(fire(EmitShape.fan(hard ? 5 : 3, 10f), gaki, 120 + v * 16, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0));
        }
        return loop(emitters, 3000, 120, ticks(200));
    }

    // st06bs BossCard4 (1309-1380): black snowman — heavy bullets with exotic motion flags
    // (1048576/2, 32768, 536870912, 128/1000) and etExSet modes 8-12; ported as slow big
    // spheres that accelerate late.
    private static DanmakuPattern blackSnowman() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var snow = shot(BUBBLE, 0.02f, MAGENTA, 280, MotionSegment.setSpeed(ticks(200), 0.22f));
        for (int v = 0; v < 12; v++) {
            emitters.add(fire(EmitShape.ring(3, 0f, true), snow, 120 + v * 22, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0).withJitter(10f));
        }
        var flurry = shot(PELLET, 0.12f, CYAN, 160, MotionSegment.rotate(0, 0.5f));
        for (int v = 0; v < 16; v++) {
            emitters.add(fire(EmitShape.aim(), flurry, 140 + v * 12, 0, 1).withAim(EmitAim.TARGET, 0, 0));
        }
        return loop(emitters, 3000, 120, ticks(260));
    }

    // st06bs BossCard5 (1446-1530): april wizard — 1×[-1] petal bullets (1.0 px/f, +-20 deg/f
    // aim drift, 3.0 cap, 64-twins and 16f splitters) from mirrored columns (1517-1518).
    private static DanmakuPattern aprilWizard(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var petal = shot(BULLET, 0.1f, CYAN, 240,
                MotionSegment.setSpeed(ticks(120), 0.3f),
                MotionSegment.split(ticks(16), new cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionPayload(
                        BULLET, CYAN, 0.85f, 0.12f, hard ? 2 : 1, 30f, 120)));
        for (int v = 0; v < (hard ? 12 : 8); v++) {
            float x = (v % 4 - 1.5f) * 3.5f;
            emitters.add(fire(EmitShape.aim(), petal, 120 + v * 18, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0).withOffset(new Vec3(x, 0.2, 0.8f)));
        }
        return loop(emitters, 3600, 120, ticks(220));
    }

    // st06bs BossCard6-9 (1610-2700): the four 7200f season finales — 1f-cadence streams with
    // 330f cycles; ported as dense rotating rain in the season palette.
    private static DanmakuPattern seasonFinale(int color, float yawPerVolley) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var rain = shot(BALL, 0.2f, color, 240);
        var halo = shot(STAR, 0.14f, PURPLE, 200,
                MotionSegment.split(ticks(16), new cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionPayload(
                        STAR, color, 0.8f, 0.12f, 4, 360f, 100)));
        for (int v = 0; v < 30; v++) {
            var cue = fire(EmitShape.ring(4, 0f, true), rain, v * 6, 0, 1).withAim(EmitAim.TARGET, 0, 0);
            emitters.add(sweep(cue, 0, yawPerVolley * v, yawPerVolley * v));
            if (v % 3 == 0) {
                emitters.add(fire(EmitShape.aim(), halo, v * 6 + 2, 0, 1).withAim(EmitAim.TARGET, 0, 0));
            }
        }
        return loop(emitters, 7200, 120, ticks(180));
    }
}
