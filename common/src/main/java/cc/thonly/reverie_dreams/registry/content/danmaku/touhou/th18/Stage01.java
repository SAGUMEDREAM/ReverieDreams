package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th18;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th18.Th18PatternSupport.*;

/** Japanese TH18 st01bs.ecl, BossCard1 and BossCard2. */
final class Stage01 {
    private static final int DEADLINE = 2100;
    private static final String MIKE = "entity.reverie_dreams.goutokuzi_mike";
    private static final String PATH = "touhou/th18/";
    private static final int[] COLORS = {0xA367D8, 0x657EE2, 0x63D4EA, 0x76C67A, 0xE9D360, 0xEB9A50, 0xDE5268};

    private Stage01() {}

    static List<TouhouSpell> spells() {
        return List.of(
                TouhouSpell.implemented(PATH + "s01_002_normal", "招符「弹幕万来」", DEADLINE, () -> welcomingBarrage(false)).withCharacter(MIKE),
                TouhouSpell.implemented(PATH + "s01_003_hard", "招符「弹幕万来」", DEADLINE, () -> welcomingBarrage(true)).withCharacter(MIKE),
                TouhouSpell.implemented(PATH + "s01_006_normal", "招符「弹灾招福」", DEADLINE, () -> invitingFortune(false)).withCharacter(MIKE),
                TouhouSpell.implemented(PATH + "s01_007_hard", "招符「弹灾招福」", DEADLINE, () -> invitingFortune(true)).withCharacter(MIKE));
    }

    // BossCard1_at sends three fans for each of seven colors, spaced five ECL frames apart.
    // The shells first travel away from the target, then their lateral acceleration curves them back.
    private static DanmakuPattern welcomingBarrage(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        int count = hard ? 10 : 5;
        for (int color = 0; color < COLORS.length; color++) {
            for (int volley = 0; volley < 3; volley++) {
                int side = volley == 1 ? -1 : 1;
                var ball = shot("ball", 0.30f, COLORS[color], 112,
                        MotionSegment.setSpeed(18, 0.13f), MotionSegment.turn(20, side * 116f, UP),
                        MotionSegment.accelerate(20, 0.006f));
                var fan = EmitShape.fan(count, (count - 1) * 10f);
                emitters.add(fire(fan, ball, 90 + color * 15 + volley * 5, side * 54f, 0)
                        .withJitter(6f));
            }
        }
        return loop(emitters, DEADLINE, 90, 165);
    }

    // BossCard2_at runs from three separated sources on Normal and six on Hard.
    // Each source emits seven sixteen-way rings with a 3-frame gap and increasing acceleration.
    private static DanmakuPattern invitingFortune(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        int sources = hard ? 6 : 3;
        for (int source = 0; source < sources; source++) {
            Vec3 offset = switch (source) {
                case 0 -> new Vec3(-2.4, 0, 0.7);
                case 1 -> new Vec3(2.4, 0, 0.7);
                default -> new Vec3((source - 3.5) * 1.15, 0.25, 2.6);
            };
            for (int color = 0; color < COLORS.length; color++) {
                var ring = shot("ball", 0.18f, COLORS[color], 115,
                        MotionSegment.accelerate(10, 0.003f + 0.0015f * color));
                emitters.add(fire(EmitShape.ring(16, 1.4f, true), ring, 150 + color * 3
                        + source * 18, source * 43f + color * 5f, 0).withOffset(offset));
            }
        }
        var drop = shot("water_drop", 0.21f, 0xD8AD56, 95);
        int count = hard ? 5 : 3;
        emitters.add(fire(EmitShape.fan(count, (count - 1) * 30f), drop, 150 + 54, 0, 8));
        // The calling ECL subroutines consume 18 frames each before the movement wait.
        return loop(emitters, DEADLINE, 90, 60 + sources * 18 + (hard ? 100 : 160));
    }
}
