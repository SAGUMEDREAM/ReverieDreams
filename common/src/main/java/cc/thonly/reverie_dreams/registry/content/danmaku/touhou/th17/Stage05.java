package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th17;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.EclSpellTimeline;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.stream.Stream;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.EclBulletSprite.*;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.EclSpellTimeline.*;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th17.Th17Spells.variants;

/** Mayumi, st05bs.ecl BossCard1–4; the last card reuses the three formations. */
final class Stage05 {
    private static final String MAYUMI = "joutougu_mayumi";

    private enum Formation { ARCHERS, SWORDSMEN, CAVALRY }

    private Stage05() {}

    static List<TouhouSpell> spells() {
        return Stream.of(
                variants(5, 44, MAYUMI, "埴轮「弓兵埴轮」", "埴轮「熟练弓兵埴轮」", 2700, hard -> regiment(hard, Formation.ARCHERS)),
                variants(5, 48, MAYUMI, "埴轮「剑士埴轮」", "埴轮「熟练剑士埴轮」", 2700, hard -> regiment(hard, Formation.SWORDSMEN)),
                variants(5, 52, MAYUMI, "埴轮「骑马兵埴轮」", "埴轮「熟练骑马兵埴轮」", 2700, hard -> regiment(hard, Formation.CAVALRY)),
                variants(5, 56, MAYUMI, "埴轮「空洞的无尽兵团」", "埴轮「不败的无尽兵团」", 3600, Stage05::endlessArmy))
                .flatMap(List::stream).toList();
    }

    private static DanmakuPattern regiment(boolean hard, Formation unit) {
        var t = new EclSpellTimeline(2700, 90, 540);
        var backingArrows = ARROW.shot(2, 3f, 270, speed(20, 1.25f), speed(50, 2.4f));
        for (int i = 0; i < 3; i++) {
            t.ring(90 + 170 * i, backingArrows, hard ? 15 : 11, i * 23);
            formation(t, 160 + 155 * i, hard, unit, i % 2 == 0 ? -1 : 1);
        }
        return t.build();
    }

    private static DanmakuPattern endlessArmy(boolean hard) {
        var t = new EclSpellTimeline(3600, 90, 690);
        for (int i = 0; i < Formation.values().length; i++) {
            int frame = 150 + 190 * i;
            formation(t, frame, hard, Formation.values()[i], -1);
            formation(t, frame + 50, hard, Formation.values()[i], 1);
        }
        return t.build();
    }

    /** Rebuild the soldier spawn lines as view-relative offsets while keeping each unit's shot. */
    private static void formation(EclSpellTimeline t, int at, boolean hard, Formation unit, int side) {
        int lanes = hard ? 6 : 4;
        for (int lane = 0; lane < lanes; lane++) {
            int frame = at + lane * 6;
            var origin = new Vec3(side * (2 + lane * 0.68), 0.35, -2.25 + lane * 0.38);
            switch (unit) {
                case ARCHERS -> {
                    float speed = hard ? 3.2f : 2.5f;
                    var aimed = ARROW.shot(3, speed, 260, pause(35, 12), speed(47, speed));
                    var sweep = ARROW.shot(1, speed, 240, speed(35, 1.65f));
                    t.fan(frame, aimed, hard ? 6 : 4, hard ? 50 : 30, 0, origin);
                    t.fan(frame + 36, sweep, hard ? 6 : 4, hard ? 50 : 30, -30 * side, origin);
                }
                case SWORDSMEN -> {
                    var blade = BLADE.shot(3, hard ? 4f : 3f, 250, speed(35, 1.5f));
                    var returning = BLADE.shot(1, hard ? 2f : 1.5f, 260, curve(35, 0.12f * side));
                    t.sweep(frame, frame + 24, 3, EmitShape.aim(), blade, -35 * side, 35 * side, origin);
                    t.ring(frame + 42, returning, hard ? 25 : 20, side * 15, origin);
                }
                case CAVALRY -> {
                    var scale = SCALE.shot(13, hard ? 2.85f : 2.3f, 275, curve(0, 0.13f * side));
                    t.fan(frame, scale, hard ? 5 : 10, 180, -24 * side, origin);
                    if (hard) t.fan(frame + 18, scale, 5, 180, 24 * side, origin);
                }
            }
        }
    }
}
