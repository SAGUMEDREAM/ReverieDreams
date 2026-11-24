package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th17;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.EclSpellTimeline;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.EclBulletSprite.*;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.EclSpellTimeline.*;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th17.Th17Spells.variants;

/** Eika, st01bs.ecl BossCard1/2. */
final class Stage01 {
    private static final String EIKA = "ebisu_eika";
    private Stage01() {}

    static List<TouhouSpell> spells() {
        return Stream.of(
                variants(1, 0, EIKA, "石符「石林」", "石符「石头针叶林」", 1800, Stage01::stoneWoods),
                variants(1, 4, EIKA, "石符「孩子们的灵薄狱」", "石符「大孩子们的灵薄狱」", 1800, Stage01::childrensLimbo))
                .flatMap(List::stream).toList();
    }

    // 387/405: N red orb -> light red offspring; H green orb -> cyan offspring.
    private static DanmakuPattern stoneWoods(boolean hard) {
        var t = new EclSpellTimeline(1800, 90, 0);
        int frame = 90;
        int amount = hard ? 50 : 10;
        for (int volley = 0; frame < 1690; volley++, frame += 90) {
            var motion = new ArrayList<MotionSegment>();
            motion.add(speed(60, 3f));
            for (int shot = 0; shot < amount; shot += 3) {
                motion.add(BALL.split(65 + shot * (hard ? 2 : 3), hard ? 8 : 2,
                        Math.min(3, amount - shot), 118f, hard ? 1.45f : 1.25f, 240));
            }
            var stone = ORB.shot(hard ? 5 : 1, 1f, 350, motion.toArray(MotionSegment[]::new));
            int side = volley % 4 < 2 ? 1 : -1;
            var offset = new Vec3(side * (volley % 2 == 0 ? 100 : 180) / 30.0, 0.6, -2.5);
            t.emit(frame, EmitShape.aim(), stone, side * 42, offset);
            amount = Math.min(hard ? 80 : 70, amount + (volley % 4 == 3 ? 10 : 0));
        }
        return t.build();
    }

    private static DanmakuPattern childrensLimbo(boolean hard) {
        var t = new EclSpellTimeline(1800, 90, 300);
        float speed = hard ? 3.4f : 2.1f;
        int interval = hard ? 40 : 50;
        int pauseAge = hard ? 160 : 120;
        var arrested = HIGHLIGHT_ORB.shot(1, speed, 360, pause(pauseAge, 60), speed(pauseAge + 60, hard ? 1.9f : 2.4f));
        var mobile = HIGHLIGHT_ORB.shot(1, speed, 245);
        for (int wave = 0; wave < 4; wave++) {
            t.ring(90 + wave * interval, arrested, 20, -20 + 12 * wave, new Vec3(-1.0, 0, 0));
            t.ring(90 + wave * interval, mobile, 20, -20 + 12 * wave, new Vec3(1.0, 0, 0));
        }
        return t.build();
    }
}
