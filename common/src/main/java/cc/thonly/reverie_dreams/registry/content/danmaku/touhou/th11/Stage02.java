package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th11;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuScatter;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th11.Th11PatternSupport.*;

/** TH11 stage02.ecl: Parsee's midboss card, then her three boss cards. */
final class Stage02 {
    private static final String PARSEE = "mizuhashi_parsee";
    private static final int GREEN = 0x69CC58;
    private static final int PURPLE = 0x9672DB;
    private static final int BLUE = 0x6388DB;
    private static final int RED = 0xE65260;
    private static final int WHITE = 0xF0EBF5;

    private Stage02() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(2, 12, false, PARSEE, "妒符「绿眼怪兽」", 2040, () -> greenEyed(false)),
                card(2, 13, true, PARSEE, "嫉妒「看不见的绿眼怪兽」", 2040, () -> greenEyed(true)),
                card(2, 16, false, PARSEE, "开花爷爷「对华丽的仁者之嫉妒」", 2400, () -> bloomingAsh(false)),
                card(2, 17, true, PARSEE, "开花爷爷「小白的灰烬」", 2100, () -> bloomingAsh(true)),
                card(2, 20, false, PARSEE, "剪舌麻雀「对谦虚的富者之记恨」", 2400, () -> baskets(false)),
                card(2, 21, true, PARSEE, "剪舌麻雀「大葛笼与小葛笼」", 2400, () -> baskets(true)),
                card(2, 24, false, PARSEE, "恨符「丑时参拜」", 2580, () -> oxHour(false)),
                card(2, 25, true, PARSEE, "恨符「丑时参拜第七日」", 2580, () -> oxHour(true)));
    }

    // MBossCard1At–At4: four sides alternate at 150 + n*(440+25+25).
    // The scripted paths become sloping ribbons from offset sources. Their release is synchronized per ribbon.
    private static DanmakuPattern greenEyed(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        for (int wave = 0; wave < 4; wave++) {
            int start = 150 + wave * 490;
            int side = ((wave & 1) == 0) == hard ? 1 : -1;
            for (int part = 0; part < 4; part++) {
                int drawOffset = part * 27;
                for (int stream = 0; stream < 2; stream++) {
                    int lag = drawOffset + stream * 3;
                    var seed = shot(stream == 0 ? PELLET : BULLET, 0.135f, stream == 0 ? GREEN : BLUE, 178,
                            MotionSegment.freeze(ticks(30), ticks(440 - lag) - ticks(30)),
                            MotionSegment.turn(ticks(440 - lag), -side * (hard ? 31f : 21f), UP),
                            MotionSegment.setSpeed(ticks(440 - lag), 0.36f));
                    var emitter = fire(EmitShape.aim(), seed, start + lag, 0, 3)
                            .withOffset(new Vec3(side * (1.4 + part * 0.58), 0.35 + part * 0.35, 0));
                    shots.add(sweep(emitter, 9f - part * 3f, side * -11f, side * -11f));
                }
            }
        }
        return timeline(shots, 2040);
    }

    // BossCard1_at / BossCard1At_at: blossom clusters and a staggered ash crossfire from BossCard1_at2.
    private static DanmakuPattern bloomingAsh(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        int frames = hard ? 2100 : 2400;
        for (int wave = 0, start = 150; start < frames; start += 360, wave++) {
            for (int branch = -1; branch <= 1; branch++) {
                int draw = start + (branch + 1) * 18;
                var bud = shot(PELLET, 0.19f, GREEN, 96, MotionSegment.freeze(ticks(36), ticks(54)),
                        MotionSegment.setSpeed(ticks(90), 0.34f));
                var blossom = fire(EmitShape.fan(hard ? 7 : 5, hard ? 62f : 44f), bud, draw, 0, 3)
                        .withOffset(new Vec3(branch * 2.1, 0.7, 0));
                shots.add(sweep(blossom, 8f, branch * 12f, branch * 12f));
            }
            if (start + 290 < frames) {
                float angle = (wave & 1) == 0 ? -10f : 10f;
                var ash = fire(EmitShape.fan(hard ? 16 : 8, hard ? 38f : 27f),
                        shot(BULLET, hard ? 0.36f : 0.28f, WHITE, 70), start + 290, 0, 3);
                shots.add(sweep(ash, 2f, angle, angle));
            }
        }
        return timeline(shots, frames);
    }

    // BossCard2 and BossCard2Dummy: offset copy, followed by six larger shots from the copy and a small shot
    // from the other side after the swap. The damageable dummy is represented by a second firing origin.
    private static DanmakuPattern baskets(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        int interval = hard ? 381 : 231;
        for (int wave = 0, start = 60; start < 2400; start += interval, wave++) {
            int side = (wave & 1) == 0 ? 1 : -1;
            Vec3 copy = new Vec3(side * 2.5, 0.55, 0);
            var larger = shot(BALL, hard ? 0.41f : 0.32f, PURPLE, 68);
            var small = shot(PELLET, 0.43f, GREEN, 62);
            shots.add(fire(EmitShape.aim(), larger, start + 30, 36, hard ? 4 : 6)
                    .withOffset(copy).withScatter(new DanmakuScatter(0.8f, 0.3f, 45f, 0.35f)));
            shots.add(sweep(fire(EmitShape.fan(6, 64f), larger, start + 80, 0, 3)
                    .withOffset(copy), 4f, side * -24f, side * -24f));
            shots.add(sweep(fire(EmitShape.aim(), small, start + 81, 0, 3)
                    .withOffset(copy.scale(-1).add(0, 0.9, 0)), 7f, side * 15f, side * 15f));
        }
        return timeline(shots, 2400);
    }

    // BossCard3_at: leave outward trails, then return across them. Hard repeats the lane every 2π/7.
    // The lane group is narrowed into a forward sector so a handheld cast has a readable front.
    private static DanmakuPattern oxHour(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var nail = shot(BALL, 0.17f, RED, 86, MotionSegment.accelerate(ticks(12), 0.013f),
                MotionSegment.freeze(ticks(60), ticks(15)), MotionSegment.turn(ticks(75), 155f, UP),
                MotionSegment.setSpeed(ticks(75), 0.36f), MotionSegment.linear(ticks(75)));
        int lanes = hard ? 7 : 1;
        for (int lane = 0; lane < lanes; lane++) {
            float angle = hard ? (lane - 3) * (360f / 7f) * 0.4f : 0f;
            var trail = fire(EmitShape.aim(), nail, 120, hard ? 126 : 192, hard ? 9 : 6)
                    .withScatter(new DanmakuScatter(0.8f, 0.8f, 0f, 0.65f));
            shots.add(sweep(trail, 6f, angle - 7f, angle + 7f));
        }
        return loop(shots, 2580, 120, 201);
    }
}
