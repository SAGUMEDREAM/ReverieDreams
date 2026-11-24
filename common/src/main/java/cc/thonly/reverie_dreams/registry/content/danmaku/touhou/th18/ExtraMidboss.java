package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th18;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuLaser;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionPayload;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th18.Th18PatternSupport.*;

/** Tsukasa's three Extra attacks in Japanese TH18 st07mbs.ecl. */
final class ExtraMidboss {
    private static final int DEADLINE = 3600;
    private static final int RED = 0xDB6870;
    private static final int PURPLE = 0x9973D6;
    private static final int CYAN = 0x69B9DE;

    private ExtraMidboss() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(85, "狐符「狐之螺旋」", ExtraMidboss::foxWinder),
                card(86, "管狐「圆管之狐」", ExtraMidboss::cylinderFox),
                card(87, "星狐「天狐龙星之舞」", ExtraMidboss::dragonStar));
    }

    private static TouhouSpell card(int number, String name, Supplier<DanmakuPattern> pattern) {
        return TouhouSpell.implemented("touhou/th18/s07_0" + number + "_extra", name, DEADLINE, pattern)
                .withCharacter("entity.reverie_dreams.kudamaki_tsukasa");
    }

    // MBoss1_at uses two emitters on opposing 70px offsets, six directions and
    // three layers. Each pass gradually bends through the top of the pattern.
    private static DanmakuPattern foxWinder() {
        var emitters = new ArrayList<DanmakuEmitter>();
        for (int wave = 0; wave < 30; wave++) {
            float yaw = 10 * wave * (wave + 1) / 72f;
            for (int side : new int[]{-1, 1}) {
                double theta = Math.toRadians(yaw * side);
                Vec3 offset = new Vec3(side * 2.6 * Math.cos(theta), 0.6, side * 2.6 * Math.sin(theta));
                var bead = shot("ball", 0.26f, side < 0 ? CYAN : PURPLE, 115);
                for (int layer = 0; layer < 3; layer++) {
                    var fan = EmitShape.fan(6, 46f);
                    emitters.add(fire(fan, bead, 90 + wave * 10, side * yaw, (layer - 1) * 9f).withOffset(offset));
                }
            }
        }
        return loop(emitters, DEADLINE, 90, 330);
    }

    // MBossCard2_at2 emits twelve accelerating beams at 30-degree intervals.
    private static DanmakuPattern cylinderFox() {
        var lasers = new ArrayList<DanmakuLaser>();
        for (int frame = 90; frame < DEADLINE; frame += 420) {
            for (int ray = 0; ray < 12; ray++) {
                lasers.add(new DanmakuLaser(ticks(frame), -3f, 10f + ray * 30f,
                        15f, 0.53f, 18, 49, 0f, PURPLE));
            }
        }
        return loop(List.of(), DEADLINE, 0, 0).withLasers(lasers);
    }

    // MBossCard3 alternates the source across seven x coordinates and waits 160f.
    // The parent emits two twelve-point child waves separated by 30f.
    private static DanmakuPattern dragonStar() {
        var emitters = new ArrayList<DanmakuEmitter>();
        int[] columns = {-160, 160, 0, 64, -64, 128, -128};
        var star = shot("star", 0.20f, RED, 124, MotionSegment.freeze(14, 20),
                MotionSegment.split(17, fan(CYAN)),
                MotionSegment.turn(22, 68f, UP), MotionSegment.split(27, fan(PURPLE)),
                MotionSegment.setSpeed(34, 0.32f));
        for (int i = 0; i < columns.length; i++) {
            emitters.add(fire(EmitShape.aim(), star, 150 + i * 160, 0, 18f)
                    .withOffset(new Vec3(columns[i] / 27f, 2.8, 0.65)).withJitter(6f));
        }
        return loop(emitters, DEADLINE, 150, columns.length * 160);
    }

    private static MotionPayload fan(int color) {
        return MotionPayload.split(ReverieDreams.id("star"), color, 0.71f, 12, 165f, 0.23f, 98);
    }
}
