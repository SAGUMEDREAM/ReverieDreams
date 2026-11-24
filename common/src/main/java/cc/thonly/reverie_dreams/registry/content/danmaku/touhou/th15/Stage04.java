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

/** TH15 st04bs.ecl: Kishin Sagume's Normal and Hard spell-practice entries. */
final class Stage04 {
    private static final String SAGUME = "kisin_sagume";
    private static final Identifier RICE = ReverieDreams.id("rice");
    private static final Identifier BALL = ReverieDreams.id("ball");
    private static final Identifier BULLET = ReverieDreams.id("bullet");
    private static final int WHITE = 0xEDEDF2;
    private static final int CRIMSON = 0xC23B4E;
    private static final int NAVY = 0x3949AB;
    private static final int GOLD = 0xDDBC61;

    private Stage04() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(4, 42, false, SAGUME, "玉符「乌合之咒」", 2400, () -> mobCurse(false)),
                card(4, 42, true, SAGUME, "玉符「乌合之逆咒」", 2400, () -> mobCurse(true)),
                card(4, 46, false, SAGUME, "玉符「秽身探知型机雷」", 3000, () -> detectingMines(false)),
                card(4, 46, true, SAGUME, "玉符「秽身探知型机雷·改」", 3000, () -> detectingMines(true)),
                card(4, 50, false, SAGUME, "玉符「众神的弹冠」", 3000, () -> divineCrown(false)),
                card(4, 50, true, SAGUME, "玉符「众神的光辉弹冠」", 3000, () -> divineCrown(true)),
                card(4, 54, false, SAGUME, "「单翼的白鹭」", 3600, () -> singleWingedHeron(false)),
                card(4, 54, true, SAGUME, "「单翼的白鹭」", 3600, () -> singleWingedHeron(true)));
    }

    // st04bs.ecl BossCard1_at/_at2 (1322-1362): crows (BCirC01, 468-540) spawn on
    // a 32px-spaced line above the field and dive while spraying rice sideways;
    // BossCard1 (1230-1312) alternates the two lines every 120f and Hard adds an
    // 8-way star rain every 120f (BossCard1_at3, 1364-1400).
    private static DanmakuPattern mobCurse(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var spray = shot(RICE, 0.2f, CRIMSON, 60);
        int period = hard ? 120 : 240;
        for (int cycle = 0; 63 + cycle * period + 144 < 2370; cycle++) {
            for (int line = 0; line < (hard ? 2 : 3); line++) {
                boolean downMarch = (cycle + line) % 2 == 0;
                int start = 63 + cycle * period + line * 48;
                for (int i = 0; i < 13; i++) {
                    float x = (downMarch ? -1 : 1) * (6.4f - i * 0.53f);
                    Vec3 offset = new Vec3(x, 0.25, -1.07);
                    var cue = fire(EmitShape.fan(hard ? 2 : 1, 12f), spray, start + i * 3, 60, 30);
                    emitters.add(cue.withOffset(offset));
                }
            }
            if (hard) {
                var rain = shot(BULLET, 0.2f, NAVY, 130, MotionSegment.accelerate(20, 0.01f));
                for (int i = 0; i < 8; i++) {
                    emitters.add(fire(EmitShape.fan(8, 45f), rain, 63 + cycle * period + 96, 0, 1));
                }
            }
        }
        return timeline(emitters, 2400);
    }

    // st04bs.ecl BossCard2_at (1470-1501): proximity mines (BCirC02, 542-606)
    // march around a 160px circle 10° per spawn every 10f, creep toward the
    // player and burst into a 3-way rice fan plus creeping balls; killing one
    // releases its own fan (581-606). BossCard2_at2 adds a crow line every 240f.
    private static DanmakuPattern detectingMines(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        for (int spawn = 0; spawn * 18 + 63 < 2970; spawn++) {
            float yaw = -10f * spawn;
            Vec3 offset = DanmakuMath.direction(0, yaw).scale(5.3).add(0, 0.3, 0);
            var burst = shot(RICE, 0.02f, CRIMSON, 110, MotionSegment.setSpeed(30, 0.1f));
            var cue = sweep(fire(EmitShape.fan(hard ? 5 : 3, hard ? 40f : 24f), burst, 63 + spawn * 18, 0, 1), 0, 0, 0);
            emitters.add(cue.withOffset(offset));
            if (spawn % 4 == 0) {
                var orb = shot(BALL, 0.01f, NAVY, 110, MotionSegment.setSpeed(40, 0.1f));
                var orbCue = sweep(fire(EmitShape.fan(3, 40f), orb, 63 + spawn * 18, 0, 1), 0, 0, 0);
                emitters.add(orbCue.withOffset(offset));
            }
        }
        var spray = shot(RICE, 0.2f, GOLD, 60);
        for (int cycle = 0; 63 + cycle * 240 + 39 < 2970; cycle++) {
            boolean downMarch = cycle % 2 == 0;
            for (int i = 0; i < 13; i++) {
                float x = (downMarch ? -1 : 1) * (6.4f - i * 0.53f);
                Vec3 offset = new Vec3(x, 0.25, -1.07);
                var cue = fire(EmitShape.fan(1, 12f), spray, 63 + cycle * 240 + i * 3, 60, 30);
                emitters.add(cue.withOffset(offset));
            }
        }
        return timeline(emitters, 3000);
    }

    // st04bs.ecl BossCard3_at (1609-1645): diving birds (BCirC03, 645-712) burst
    // from the boss every 3f in a heading marching 222.5°, accelerating 2→10;
    // their kill-released fans (681-712) cannot be expressed with invulnerable
    // familiars, so the mod adds periodic 5-way (H) drop volleys instead.
    private static DanmakuPattern divineCrown(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var bird = shot(RICE, 0.2f, WHITE, 150, MotionSegment.accelerate(40, 0.02f));
        for (int spawn = 0; spawn * 12 + 63 < 2970; spawn++) {
            float yaw = 37f * spawn;
            var cue = sweep(fire(EmitShape.aim(), bird, 63 + spawn * 12, 0, 1), 0, yaw, yaw);
            emitters.add(cue);
        }
        if (hard) {
            var drop = shot(RICE, 0.2f, GOLD, 110);
            for (int wave = 0; 180 + wave * 180 < 2880; wave++) {
                emitters.add(fire(EmitShape.fan(5, 100f), drop, 180 + wave * 180, 0, 1));
            }
        }
        return timeline(emitters, 3000);
    }

    // st04bs.ecl BossCard4 (1702-1753) + BossCard4_at (1762-1812): every cycle
    // (N ~280f / H ~200f) the boss dashes aside and sweeps 40 pivot shots 4f
    // apart; each shot fires laser pairs on both flanks (offsets at yaw±90°,
    // headings yaw and yaw±40°) and the pivot marches %B/30 per shot, mirrored
    // next cycle. Sampled to 10 shots per cycle with one flank laser per shot
    // (the second heading is folded into the 2°/t curl); laser length 640px.
    private static DanmakuPattern singleWingedHeron(boolean hard) {
        var lasers = new ArrayList<DanmakuLaser>();
        int period = hard ? 67 : 93;
        for (int cycle = 0; 32 + cycle * period + 40 < 1200; cycle++) {
            float mirror = cycle % 2 == 0 ? 1f : -1f;
            for (int i = 0; i < 10; i++) {
                float yaw = 90 + mirror * i * 2f;
                float side = i % 2 == 0 ? 1f : -1f;
                Vec3 offset = DanmakuMath.direction(0, yaw + side * 90f).scale(0.67).add(0, 0.2, 0);
                lasers.add(laser(32 + cycle * period + i * 4, 0, yaw, 21.3f, 0.53f, 8, 16, mirror * 2f, WHITE)
                        .withOffset(offset));
            }
        }
        return timeline(List.of(), lasers, 3600);
    }
}
