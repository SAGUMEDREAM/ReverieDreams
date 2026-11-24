package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th16;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
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

/**
 * TH16 st05bs.ecl: Nishida Satono and Teireida Mai — Okina's two servants. Their cards pair a
 * front dancer (Mai) with back-line support bullets (Satono). The id62 Lunatic branch reuses
 * Aunn's 狗符「山狗の散歩」 text (ZUN copy-paste quirk) — both rows use the official 舞符 name.
 */
final class Stage05 {
    private static final String MAI = "teireida_mai";
    private static final int CYAN = 0x28FFFF;
    private static final int LIME = 0x9DFF2A;
    private static final int GREY = 0x737373;
    private static final int YELLOW = 0xFCFD26;
    private static final int RED = 0xFF0000;

    private Stage05() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(5, 46, false, MAI, "竹符「竹枪之舞」", 1800, () -> bambooDance(false)),
                card(5, 46, true, MAI, "竹符「竹枪狂舞」", 1800, () -> bambooDance(true)),
                card(5, 50, false, MAI, "茗荷「忘却汝名」", 1800, Stage05::forgetYourName),
                card(5, 50, true, MAI, "茗荷「忘却汝名」", 1800, Stage05::forgetYourName),
                card(5, 54, false, MAI, "笹符「七夕星节」", 2400, Stage05::tanabataStar),
                card(5, 54, true, MAI, "笹符「七夕星节」", 2400, Stage05::tanabataStar),
                card(5, 58, false, MAI, "冥加「在你身后」", 2400, () -> behindYou(false)),
                card(5, 58, true, MAI, "冥加「在你身后」", 2400, () -> behindYou(true)),
                card(5, 62, false, MAI, "舞符「身后祭典」", 3000, () -> behindFestival(false)),
                card(5, 62, true, MAI, "舞符「身后祭典」", 3000, () -> behindFestival(true)),
                card(5, 66, false, MAI, "狂舞「吓唬天狗」", 3000, () -> tenguOdo(false)),
                card(5, 66, true, MAI, "狂舞「狂乱天狗怖」", 3000, () -> tenguOdo(true)));
    }

    // st05bs BossCard1 (1374-1415): bamboo spear volleys alternating left-right dance steps.
    private static DanmakuPattern bambooDance(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var spear = shot(KUNAI, hard ? 0.3f : 0.2f, CYAN, 170, MotionSegment.rotate(0, hard ? -0.5f : 0.3f));
        for (int v = 0; v < 14; v++) {
            var cue = fire(EmitShape.fan(hard ? 3 : 1, 6f), spear, 120 + v * (hard ? 8 : 12), 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0);
            emitters.add(sweep(cue, 0, (v % 2 == 0 ? 40f : -40f), (v % 2 == 0 ? 40f : -40f)));
        }
        return loop(emitters, 1800, 120, ticks(170));
    }

    // st05bs BossCard1B (1459-1527): forget-your-name lasers — 20/40 volleys of 22 px lasers
    // with 96f life sweeping (%C) and a creeping spawn distance (%E, 1526).
    private static DanmakuPattern forgetYourName() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var lasers = new ArrayList<DanmakuLaser>();
        for (int v = 0; v < 20; v++) {
            int at = ticks(120) + v * ticks(40);
            float yaw = 30f * v;
            lasers.add(laser(at, 0f, yaw, 13f, 1.4f, ticks(30), ticks(96), 1.5f, GREY));
            lasers.add(laser(at + ticks(8), 0f, yaw + 120f, 13f, 1.4f, ticks(30), ticks(96), -1.5f, GREY));
            var needle = shot(PELLET, 0.14f, LIME, 140);
            emitters.add(fire(EmitShape.aim(), needle, at + ticks(4), 0, 1).withAim(EmitAim.TARGET, 0, 0));
        }
        return timeline(emitters, lasers, 1800);
    }

    // st05bs BossCard2 (1811-1900): tanabata star festival — vertical star columns.
    private static DanmakuPattern tanabataStar() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var star = shot(STAR, 0.2f, YELLOW, 190, MotionSegment.rotate(0, 0.25f));
        for (int v = 0; v < 18; v++) {
            float x = -7.5f + (v % 7) * 2.5f;
            emitters.add(fire(EmitShape.aim(), star, 120 + v * 12, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0).withOffset(new Vec3(x, 0.2, 1.5)));
        }
        return loop(emitters, 2400, 120, ticks(220));
    }

    // st05bs BossCard2B (1878-1950): behind-you — teleports (ins_800 BossCard1Hide) plus
    // 1×[-1]-way bullets with 0.01 px/f^2 accel, 64-flag twins and 16f splitters (1938-1950).
    private static DanmakuPattern behindYou(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var sting = shot(BULLET, 0.16f, GREY, 170,
                MotionSegment.split(ticks(16), new cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionPayload(
                        BULLET, GREY, 0.85f, 0.14f, hard ? 2 : 1, 40f, 110)));
        for (int v = 0; v < 12; v++) {
            emitters.add(fire(EmitShape.aim(), sting, 120 + v * 16, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0).withJitter(8f));
        }
        return loop(emitters, 2400, 120, ticks(190));
    }

    // st05bs BossCard3/B (2110-2140): behind-festival — like 冥加 but from player-relative
    // columns (ins_628 [-9987]*192, 2185) with mirrored spawns.
    private static DanmakuPattern behindFestival(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var sting = shot(BULLET, 0.16f, GREY, 170,
                MotionSegment.split(ticks(16), new cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionPayload(
                        BULLET, GREY, 0.85f, 0.14f, hard ? 2 : 1, 40f, 110)));
        for (int v = 0; v < 14; v++) {
            float x = (v % 6 - 3) * 3.2f;
            emitters.add(fire(EmitShape.aim(), sting, 120 + v * 14, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0).withOffset(new Vec3(x, 0.2, 0.5f)));
        }
        return loop(emitters, 3000, 120, ticks(200));
    }

    // st05bs BossCard4 (2446+): the tengu-scare finale — heavy dance volleys with sweeping aim.
    private static DanmakuPattern tenguOdo(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var drum = shot(BALL, hard ? 0.24f : 0.18f, CYAN, 190);
        for (int v = 0; v < 12; v++) {
            var cue = fire(EmitShape.ring(hard ? 10 : 6, 0f, true), drum, 120 + v * 16, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0);
            emitters.add(sweep(cue, 0, (v % 2 == 0 ? 0f : 30f), (v % 2 == 0 ? 0f : 30f)));
        }
        return loop(emitters, 3000, 120, ticks(190));
    }
}
