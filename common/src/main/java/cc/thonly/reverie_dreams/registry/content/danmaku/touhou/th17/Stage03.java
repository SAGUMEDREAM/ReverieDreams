package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th17;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.EclSpellTimeline;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.stream.Stream;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.EclBulletSprite.*;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.EclSpellTimeline.*;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th17.Th17Spells.variants;

/** Kutaka, st03bs.ecl BossCard1–3. Extra has its own patterns. */
final class Stage03 {
    static final String KUTAKA = "niwatari_kutaka";
    private Stage03() {}

    static List<TouhouSpell> spells() {
        return Stream.of(
                variants(3, 20, KUTAKA, "水符「分水的试练」", "水符「分水的上级试炼」", 2100, Stage03::waterTrial),
                variants(3, 24, KUTAKA, "光符「瞭望的试练」", "光符「瞭望的上级试炼」", 2100, Stage03::lightTrial),
                variants(3, 28, KUTAKA, "鬼符「鬼渡的试练」", "鬼符「鬼渡的上级试炼」", 2400, Stage03::crossingTrial))
                .flatMap(List::stream).toList();
    }

    private static DanmakuPattern waterTrial(boolean hard) {
        var t = new EclSpellTimeline(2100, 90, 270);
        int[] colors = {8, 13, 6};
        for (int wave = 0; wave < colors.length; wave++) {
            var drops = DROP.shot(colors[wave], 1f, 360, speed(90, 2.7f), curve(90, (wave - 1) * 0.14f));
            for (int pass = 0; pass < 3; pass++) {
                int count = hard ? 12 : 10;
                t.fan(90 + wave * 90 + pass * 18, drops, count, (count - 1) * 30,
                        -20 + pass * 20, new Vec3((wave - 1) * 2.25, 0, 0));
            }
        }
        return t.build();
    }

    private static DanmakuPattern lightTrial(boolean hard) {
        var t = new EclSpellTimeline(2100, 90, 300);
        var pellets = BALL.shot(10, 2f, 210);
        for (int frame = 90; frame < 390; frame += 5) {
            t.rain(frame, pellets, 2, 10, 0, new Vec3(0, 1.25, -3), 4.8f, 3f, 5f);
        }
        for (int i = 0; i < 5; i++) {
            int at = 90 + i * 60;
            float angle = (i - 2) * 16;
            var offset = new Vec3((i - 2) * 1.1, 0.25, 0);
            t.beam(at, 28, 45, 256, 16, angle, 0.25f, 9, RICE.color(6), offset);
            if (hard) t.beam(at + 16, 28, 45, 256, 16, -angle - 18, -0.25f, 9, RICE.color(6), offset);
        }
        return t.build();
    }

    private static DanmakuPattern crossingTrial(boolean hard) {
        var t = new EclSpellTimeline(2400, 90, 480);
        var phantom = BALL.shot(4, 0.2f, 330, pause(120, 80), speed(200, hard ? 2.4f : 1.75f));
        var flame = RED_FLAME.shot(0, 1.6f, 260);
        for (int i = 0; i < 3; i++) {
            int at = 90 + i * 160;
            for (int side : new int[]{-1, 1}) {
                var offset = new Vec3(side * (2.5 + i * 0.5), 0.4, -2);
                t.fan(at, phantom, hard ? 13 : 9, 80, -side * 34, offset);
                t.fan(at + 100, flame, 10, 180, -side * 20, offset);
            }
        }
        return t.build();
    }
}
