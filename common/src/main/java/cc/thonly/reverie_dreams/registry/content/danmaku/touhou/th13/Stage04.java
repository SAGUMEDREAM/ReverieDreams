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

/**
 * TH13 st04mbs/st04bs.ecl: Yoshika's underground-gate cameo and Kaku Seiga's stage-4 boss cards.
 * Seiga summons Yoshika mid-card (ins_800 BossMiyako*) — her assist fire is ported as familiars.
 */
final class Stage04 {
    private static final String SEIGA = "kaku_seiga";
    private static final String YOSHIKA = "miyako_yoshika";
    // Sampled from th13 bullet.anm: spr18 = 0xFF0000 red, spr21 = 0x3C00FF blue-violet,
    // spr9 = 0x36FF36 green (Yoshika assist), spr10 = 0x85FF9C green (evil balls / midboss).
    private static final int RED = 0xFF0000;
    private static final int BLUE_VIOLET = 0x3C00FF;
    private static final int YOSHIKA_GREEN = 0x36FF36;
    private static final int EVIL_GREEN = 0x85FF9C;
    private static final int WHITE = 0xFFFFFF;

    private Stage04() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(4, 44, false, YOSHIKA, "邪符「殃殃鬼」", 2700, () -> evilGateStream(3, 0.2f)),
                card(4, 45, true, YOSHIKA, "邪符「孤魂野鬼」", 2700, () -> evilGateStream(2, 0.33f)),
                card(4, 47, false, SEIGA, "入魔「走火入魔」", 3300, () -> possession(false)),
                card(4, 47, true, SEIGA, "入魔「走火入魔」", 3300, () -> possession(true)),
                card(4, 51, false, SEIGA, "降灵「死人弹起」", 3300, () -> channelYoshika(0.2f, 0.3f)),
                card(4, 51, true, SEIGA, "通灵「通灵芳香」", 3300, () -> channelYoshika(0.3f, 0.3f)),
                card(4, 55, false, SEIGA, "道符「道胎动」", 2520, () -> taoFetus(false)),
                card(4, 55, true, SEIGA, "道符「道胎动」", 2520, () -> taoFetus(false)),
                extraCard(4, 122, SEIGA, "道符「TAO胎动 ～道～」", 2520, () -> taoFetus(true)));
    }

    // st04mbs MBossCard1 (285-392): a 100-shot curling stream of 19/19/25/35 bullets aimed at the
    // player, one per frame, with an accelerating heading (0.29-0.57 deg/f^2 by distance, 379-388),
    // speed 1.0 -> 5 px/f over frames 4-60 (377). Two to four EvilBall00 minions drift above
    // (318-325). Ported as aimed fans every 6t with a 24 deg/volley curl plus orb familiars.
    private static DanmakuPattern evilGateStream(int volleys, float curlPerVolley) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var stream = shot(ARROWHEAD, 0.1f, EVIL_GREEN, 130,
                MotionSegment.setSpeed(ticks(60), 0.5f));
        for (int v = 0; v < volleys; v++) {
            var cue = fire(EmitShape.fan(v < 2 ? 19 : 35, 30f), stream, v * 6, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0);
            emitters.add(sweep(cue, 0, curlPerVolley * v * 72f, curlPerVolley * v * 72f));
        }
        var familiars = new ArrayList<DanmakuFamiliar>();
        int orbs = volleys >= 2 ? 4 : 2;
        for (int k = 0; k < orbs; k++) {
            float angle = 90f + 360f * k / orbs;
            var orb = DanmakuFamiliar.of(polar(angle, 1.9), DanmakuPattern.of())
                    .withOrbit(1.9f, 1.2f, angle).withDuration(ticks(2400));
            familiars.add(orb);
        }
        return new DanmakuPattern(emitters).withFamiliars(familiars)
                .withTiming(new cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming(
                        ticks(2700), ticks(210), ticks(120)));
    }

    // st04bs BossCard1 (344-529): Seiga raises Yoshika (BossMiyakoCard1: 2-bullet 20 deg/f
    // spirals every 10/5/3/2f, speed 0.5-1.5, 1149-1194 — ported as her familiar) and layers:
    // an ambient 11-ring every 60/45/30f (at2 493-529, speed 2.4-3.8, spin 18 deg/f), two
    // 8/10-bullet arm streams curling +-2 deg/f that leak player-aimed sub-shots every 16f
    // (at 448-491, etExSet2 475), and a 32-ring at 1.4-1.9 px/f each cycle (406-440).
    private static DanmakuPattern possession(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var ambient = shot(BALL, hard ? 0.34f : 0.24f, RED, 220,
                MotionSegment.rotate(0, 0.6f));
        for (int v = 0; v < 8; v++) {
            emitters.add(fire(EmitShape.ring(11, 0f, true), ambient, 210 + v * (hard ? 15 : 20), 0, 1));
        }
        var armLeft = shot(BULLET, 0.04f, BLUE_VIOLET, 170,
                MotionSegment.setSpeed(ticks(120), 0.2f),
                MotionSegment.rotate(ticks(8), 0.67f));
        var armRight = shot(BULLET, 0.04f, BLUE_VIOLET, 170,
                MotionSegment.setSpeed(ticks(120), 0.2f),
                MotionSegment.rotate(ticks(8), -0.67f));
        for (int v = 0; v < 30; v++) {
            int at = v * 2;
            emitters.add(sweep(fire(EmitShape.fan(8, 0f), armLeft, at, 0, 1), 0, 30f * v, 30f * v + 60f));
            emitters.add(sweep(fire(EmitShape.fan(10, 0f), armRight, at, 0, 1), 0, -30f * v, -30f * v - 60f));
        }
        var burst = shot(STAR, hard ? 0.19f : 0.14f, BLUE_VIOLET, 200,
                MotionSegment.rotate(0, 1.05f));
        emitters.add(fire(EmitShape.ring(32, 0f, true), burst, 420, 0, 1));
        var yoshikaFire = shot(PELLET, hard ? 0.1f : 0.05f, YOSHIKA_GREEN, 160);
        var yoshikaPattern = new DanmakuPattern(List.of(
                sweep(fire(EmitShape.fan(2, 6f), yoshikaFire, 0, 60, 2), 0, 0f, 720f)));
        var familiars = List.of(DanmakuFamiliar.of(new Vec3(0, 0.2, 1.6), yoshikaPattern)
                .withDelay(ticks(210)).withDuration(ticks(2800)));
        return new DanmakuPattern(emitters).withFamiliars(familiars)
                .withTiming(new cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming(
                        ticks(3300), ticks(210), ticks(450)));
    }

    // st04bs BossCard2 (531-630): Seiga channels the standing corpse — endless 5-bullet fans at
    // the player (BossCard2_at 605-630, aim = heading toward the player scaled 10 deg, spin
    // 20 deg/f, speed 2.0/3.0/3.5 px/f) whose cadence tightens 120 -> 60 (N) or 60 -> 30 (H/L)
    // frames. Yoshika returns with orbiting EvilBall01 familiars (BossMiyakoCard2 1207-1251).
    private static DanmakuPattern channelYoshika(float speedN, float speedH) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var fan = shot(KUNAI, speedN, BLUE_VIOLET, 190);
        for (int v = 0; v < 16; v++) {
            var cue = fire(EmitShape.fan(5, 12f), fan, 210 + v * 10, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0);
            emitters.add(sweep(cue, 0, 20f * v, 20f * v));
        }
        var familiars = new ArrayList<DanmakuFamiliar>();
        for (int k = 0; k < 8; k++) {
            float angle = 45f * k;
            familiars.add(DanmakuFamiliar.of(polar(angle, 1.2), DanmakuPattern.of())
                    .withOrbit(1.2f, 1.5f, angle).withDuration(ticks(2800)));
        }
        var assist = shot(PELLET, speedH, YOSHIKA_GREEN, 150);
        familiars.add(DanmakuFamiliar.of(new Vec3(0, 0.2, 2), new DanmakuPattern(List.of(
                sweep(fire(EmitShape.fan(3, 8f), assist, 0, 80, 6), 0, 0f, 480f))))
                .withDelay(ticks(210)).withDuration(ticks(2800)));
        return new DanmakuPattern(emitters).withFamiliars(familiars)
                .withTiming(new cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming(
                        ticks(3300), ticks(210), ticks(160)));
    }

    // st04bs BossCard3 (632-724): every 200f (140 on Overdrive) Yoshika sweeps 4/8/8/10 short
    // lasers (16 px wide, 40f, +-1.6 deg/f, BossMiyakoCard3_at 1422-1468, alternating sweep
    // direction per wave) and Seiga releases 4 -> 8+ EvilBall02 spirits in an expanding ring
    // (BossCard3_at 693-724, +1 count every other cycle). Overdrive uses 10 lasers throughout.
    private static DanmakuPattern taoFetus(boolean overdrive) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var lasers = new ArrayList<DanmakuLaser>();
        var familiars = new ArrayList<DanmakuFamiliar>();
        int cycles = overdrive ? 8 : 7;
        int period = overdrive ? 140 : 200;
        for (int c = 0; c < cycles; c++) {
            int count = overdrive ? 10 : Math.min(4 + c * 2, 10);
            int at = ticks(210) + c * ticks(period);
            float sweepDeg = (c % 2 == 0 ? 1.6f : -1.6f);
            for (int k = 0; k < count; k++) {
                float yaw = 360f * k / count;
                lasers.add(laser(at, 0f, yaw, 11f, 1.0f, ticks(20), ticks(40), sweepDeg, BLUE_VIOLET));
                lasers.add(laser(at + ticks(20), 0f, yaw, 11f, 1.0f, ticks(20), ticks(40), -sweepDeg, WHITE));
            }
            int orbs = Math.min(4 + c, 8);
            for (int k = 0; k < orbs; k++) {
                float angle = 360f * k / orbs;
                familiars.add(DanmakuFamiliar.of(polar(angle, 1.5), DanmakuPattern.of())
                        .withDelay(at).withDuration(ticks(120)));
            }
        }
        var spark = shot(PELLET, 0.15f, EVIL_GREEN, 140);
        emitters.add(fire(EmitShape.ring(16, 0f, true), spark, 210, 0, 1));
        return new DanmakuPattern(emitters).withFamiliars(familiars).withLasers(lasers)
                .withTiming(new cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming(
                        ticks(2520), ticks(210), ticks(overdrive ? 47 : 67)));
    }
}
