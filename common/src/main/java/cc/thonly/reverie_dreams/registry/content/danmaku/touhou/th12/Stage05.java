package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th12;

import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeRange;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuLaser;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th12.Th12PatternSupport.*;

/** TH12 stage05.ecl: Nazrin's midboss and Shou Toramaru's boss entries, Normal and Hard. */
final class Stage05 {
    private static final String NAZRIN = "nazrin";
    private static final String SHOU = "item.reverie_dreams.spellcard.character.toramaru_shou";
    private static final int GOLD = 0xE8B55C;
    private static final int BLUE = 0x6388DB;
    private static final int PURPLE = 0x8B7EE8;
    private static final int WHITE = 0xF0EBF5;

    private Stage05() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(5, 59, false, NAZRIN, "宝塔「Greatest Treasure」（无上财宝）", 2400, () -> greatestTreasure(false)),
                card(5, 60, true, NAZRIN, "宝塔「Greatest Treasure」（无上财宝）", 2400, () -> greatestTreasure(true)),
                card(5, 63, false, SHOU, "宝塔「Radiant Treasure」（闪亮财宝）", 2700, () -> radiantTreasure(false)),
                card(5, 64, true, SHOU, "宝塔「Radiant Treasure Gun」（闪亮宝枪）", 2700, () -> radiantTreasure(true)),
                card(5, 67, false, SHOU, "光符「Absolute Justice」（绝对正义）", 2700, () -> absoluteJustice(false)),
                card(5, 68, true, SHOU, "光符「正义之威光」", 2700, () -> absoluteJustice(true)),
                card(5, 71, false, SHOU, "法力「至宝之独钴杵」", 2880, () -> dharmaVajra(false)),
                card(5, 72, true, SHOU, "法灯「无瑕佛法之独钴杵」", 2880, () -> dharmaVajra(true)),
                card(5, 75, false, SHOU, "光符「净化之魔」", 3000, () -> purificationMagic(false)),
                card(5, 76, true, SHOU, "光符「净化之魔」", 3000, () -> purificationMagic(true)));
    }

    // MBossCard1 uses eight Normal or twelve Hard moving laser objects; MBossCard1LaserAt fires two
    // bullets per step at speed 0.4. Hard uses the same 16 px radius growth but more laser objects.
    private static DanmakuPattern greatestTreasure(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var beams = new ArrayList<DanmakuLaser>();
        var trail = shot(BULLET, 0.28f, GOLD, 110, MotionSegment.accelerate(ticks(60), 0.006f));
        for (int cycle = 0, start = 120; start < 2200; cycle++, start += 430) {
            float side = (cycle & 1) == 0 ? -1f : 1f;
            shots.add(fire(EmitShape.fan(hard ? 12 : 8, 360f), trail, start, 120, 3)
                    .withOffset(new Vec3(side * 1.2f, 0.35, 0))
                    .withRotation(KeyframeRange.empty(0f, hard ? 540f : 360f)));
            beams.add(laser(ticks(start), 0f, side * 45f, 18f, 0.9f, 10, 16, 0f, GOLD));
        }
        return timeline(shots, beams, 2400);
    }

    // BossCard1 places 8 Normal or 10 Hard lasers; BossCard1LaserAt3 adds Hard-only center sweeps.
    private static DanmakuPattern radiantTreasure(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var beams = new ArrayList<DanmakuLaser>();
        var trail = shot(BULLET, 0.30f, GOLD, 100, MotionSegment.accelerate(ticks(60), 0.007f));
        int count = hard ? 10 : 8;
        for (int half = 0; half < 2; half++) {
            int start = 150 + half * 650;
            float side = half == 0 ? -1f : 1f;
            for (int i = 0; i < count; i++) {
                float yaw = side * (i * (360f / count));
                beams.add(laser(ticks(start + i * 9), 0f, yaw, 16f, 0.8f, 8, 16, 0f, GOLD));
            }
            shots.add(fire(EmitShape.fan(count, 300f), trail, start + 60, 120, 3)
                    .withOffset(new Vec3(side * 1.0f, 0.4, 0)).withJitter(5f));
        }
        if (hard) {
            for (int i = 0; i < 64; i++) {
                beams.add(laser(ticks(360 + i * 3), 0f, i * 11.25f, 12f, 0.8f, 8, 12, 0f, PURPLE));
            }
        }
        return timeline(shots, beams, 2700);
    }

    // BossCard2At3 is a nine-bullet 15-degree fan every 40 frames; BossCard2 adds six visible laser
    // objects on both sides. Normal and Hard share the bullet count and cadence.
    private static DanmakuPattern absoluteJustice(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var beams = new ArrayList<DanmakuLaser>();
        var fan = shot(BULLET, 0.55f, WHITE, 120, MotionSegment.accelerate(ticks(80), 0.008f));
        for (int cycle = 0, start = 150; start < 2500; cycle++, start += 420) {
            shots.add(fire(EmitShape.fan(9, 15f), fan, start, 120, 120)
                    .withOffset(new Vec3((cycle & 1) == 0 ? -2.5f : 2.5f, 0.5, 0)).withJitter(4f));
            for (int i = 0; i < 3; i++) {
                int frame = start + i * 20;
                beams.add(laser(ticks(frame), 0f, -180f + i * 72f, 20f, 1.0f, 10, 20, 0f, PURPLE));
                beams.add(laser(ticks(frame), 0f, 180f - i * 72f, 20f, 1.0f, 10, 20, 0f, PURPLE));
            }
        }
        return timeline(shots, beams, 2700);
    }

    // BossCard3: Normal uses 12 random-angle bullets per 60-frame wave. Hard uses a 0.242701 rad
    // spiral every 6 frames. Both use rotating, player-tracking laser objects.
    private static DanmakuPattern dharmaVajra(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var beams = new ArrayList<DanmakuLaser>();
        var scatter = shot(BULLET, 0.34f, GOLD, 120, MotionSegment.accelerate(ticks(120), 0.008f));
        var spiral = shot(BULLET, 0.34f, GOLD, 120, MotionSegment.accelerate(ticks(120), 0.008f));
        for (int cycle = 0, start = 240; start < 2700; cycle++, start += 600) {
            if (hard) {
                shots.add(fire(EmitShape.aim(), spiral, start, 240, 18)
                        .withRanges(KeyframeRange.empty(0f, 0f), KeyframeRange.empty(0f, 2322f)));
            } else {
                shots.add(fire(EmitShape.fan(12, 360f), scatter, start, 240, 180).withJitter(12f));
            }
            beams.add(laser(ticks(start), 0f, (cycle & 1) == 0 ? 45f : -45f, 26f, 0.9f, 12, 30, 0f, GOLD));
            beams.add(laser(ticks(start), 0f, (cycle & 1) == 0 ? 135f : 225f, 26f, 0.9f, 12, 30, 0f, GOLD));
        }
        return timeline(shots, beams, 2880);
    }

    // BossCard4 creates laser columns from the right and left. Normal starts the second half lower;
    // Hard keeps the same origin. Bullet output is one accelerating bullet per laser, speed 0.2.
    private static DanmakuPattern purificationMagic(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var beams = new ArrayList<DanmakuLaser>();
        var purge = shot(BULLET, 0.20f, WHITE, 130, MotionSegment.accelerate(ticks(60), 0.009f));
        int start = 150;
        for (int half = 0; half < 4; half++) {
            int count = half % 2 == 0 ? (hard ? 9 : 8) : (hard ? 10 : 9);
            float side = half % 2 == 0 ? 1f : -1f;
            for (int i = 0; i < count; i++) {
                int frame = start + i * 30;
                float y = hard ? 2.0f + i * 0.6f : 2.0f + i * 0.7f;
                shots.add(fire(EmitShape.aim(), purge, frame, 0, 3)
                        .withOffset(new Vec3(side * 12.0f, y, 0)).withJitter(4f));
                beams.add(laser(ticks(frame), 0f, side * 90f, 20f, 0.9f, 8, 14, 0f, BLUE));
            }
            start += 700;
        }
        return timeline(shots, beams, 3000);
    }
}
