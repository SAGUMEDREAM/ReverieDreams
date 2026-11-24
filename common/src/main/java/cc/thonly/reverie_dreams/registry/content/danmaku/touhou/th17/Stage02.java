package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th17;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.EclSpellTimeline;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.stream.Stream;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.EclBulletSprite.*;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.EclSpellTimeline.*;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th17.Th17Spells.variants;

/** Urumi, st02bs.ecl BossCard1–3. */
final class Stage02 {
    private static final String URUMI = "ushizaki_urumi";
    private Stage02() {}

    static List<TouhouSpell> spells() {
        return Stream.of(
                variants(2, 8, URUMI, "石符「石之婴儿」", "石符「沉重的石之婴儿」", 1800, hard -> stoneBaby(hard, false)),
                variants(2, 12, URUMI, "溺符「三途的沦溺」", "溺符「三途的沦溺」", 2400, hard -> stoneBaby(hard, true)),
                variants(2, 16, URUMI, "鬼符「魔鬼围城」", "鬼符「饿鬼围城」", 2400, Stage02::demonSiege))
                .flatMap(List::stream).toList();
    }

    private static DanmakuPattern stoneBaby(boolean hard, boolean sanzu) {
        var t = new EclSpellTimeline(sanzu ? 2400 : 1800, 90, 260);
        var orb = GLOW_ORB.shot(2, 3f, 300, speed(60, 1f), speed(120, sanzu ? 0.45f : 1.5f));
        var stone = HIGHLIGHT_ORB.shot(1, 3f, 330, speed(60, 1f), speed(120, sanzu ? 0.45f : 1.5f));
        var water = RICE.shot(6, 2f, 210);
        int count = sanzu ? (hard ? 3 : 1) : (hard ? 14 : 10);
        for (int i = 0; i < 5; i++) {
            int at = 90 + i * 30;
            float yaw = (i - 2) * 15;
            t.fan(at, orb, count, Math.min(330, (count - 1) * 30), yaw, Vec3.ZERO);
            t.fan(at + 3, stone, count, Math.min(330, (count - 1) * 30), yaw, Vec3.ZERO);
        }
        if (sanzu) {
            for (int i = 0; i < 24; i++) {
                t.rain(90 + i * 9, water, 2, 6, i % 2 == 0 ? -22 : 22,
                        new Vec3(i % 2 == 0 ? -2 : 2, 0.6, -1), 2.6f, 2f, 10f);
            }
        }
        return t.build();
    }

    private static DanmakuPattern demonSiege(boolean hard) {
        var t = new EclSpellTimeline(2400, 90, 450);
        var rice = RICE.shot(2, 1.5f, 340, MotionSegment.bounce(0, 11f), speed(90, hard ? 1.9f : 1.8f));
        for (int side : new int[]{-1, 1}) {
            int start = side < 0 ? 90 : 310;
            for (int i = 0; i < (hard ? 9 : 7); i++) {
                t.fan(start + i * (hard ? 8 : 10), rice, hard ? 7 : 6,
                        hard ? 150 : 125, side * (42 - i * 5), Vec3.ZERO);
            }
        }
        return t.build();
    }
}
