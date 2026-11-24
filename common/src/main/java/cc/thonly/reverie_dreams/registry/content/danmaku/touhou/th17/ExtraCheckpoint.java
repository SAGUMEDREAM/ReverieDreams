package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th17;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.EclSpellTimeline;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.EclBulletSprite.*;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.EclSpellTimeline.*;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th17.Th17Spells.extra;

/** Kutaka, st07mbs.ecl MBossCard1–3. */
final class ExtraCheckpoint {
    private ExtraCheckpoint() {}

    static List<TouhouSpell> spells() {
        return List.of(
                extra(88, Stage03.KUTAKA, "血战「血之分水岭」", 2400, ExtraCheckpoint::watershed),
                extra(89, Stage03.KUTAKA, "血战「狱界视线」", 2400, ExtraCheckpoint::underworldGaze),
                extra(90, Stage03.KUTAKA, "血战「全灵鬼渡」", 2400, ExtraCheckpoint::spiritCrossing));
    }

    private static DanmakuPattern watershed() {
        var t = new EclSpellTimeline(2400, 90, 120);
        var red = DROP.shot(2, 1f, 360, speed(70, 2.4f));
        var darkRed = DROP.shot(1, 1f, 360, speed(70, 2.4f));
        for (int i = 0; i < 60; i += 2) {
            double angle = Math.toRadians(111.25 * i);
            var offset = new Vec3(Math.sin(angle), 0.25, Math.cos(angle));
            t.fan(90 + i, i % 4 == 0 ? red : darkRed, 10, 117, i * 9, offset);
            t.fan(150 + i, i % 4 == 0 ? red : darkRed, 10, 117, -i * 9, offset.scale(-1));
        }
        return t.build();
    }

    private static DanmakuPattern underworldGaze() {
        var t = new EclSpellTimeline(2400, 90, 240);
        var scale = SCALE.shot(2, 0.5f, 360, pause(0, 80), speed(80, 1.7f));
        t.ring(90, scale, 40, 5);
        t.ring(210, scale, 40, 17);
        for (int i = 0; i < 8; i++) {
            float yaw = (i % 4 - 1.5f) * 26;
            t.beam(90 + i * 28, 35, 34, 256, 16, yaw, 0.24f, 8, RICE.color(2),
                    new Vec3((i % 4 - 1.5) * 1.4, 0.3, -0.5));
        }
        return t.build();
    }

    private static DanmakuPattern spiritCrossing() {
        var t = new EclSpellTimeline(2400, 90, 480);
        var phantomRice = RICE.shot(4, 0.2f, 400, pause(0, 60), speed(60, 2f));
        for (int group = 0; group < 3; group++) {
            for (int i = 0; i < 10; i++) {
                for (int side : new int[]{-1, 1}) {
                    var offset = new Vec3(side * (5 - i * 0.42), 0.45, -2.1 + i * 0.6);
                    t.fan(90 + group * 160 + i * 10, phantomRice, 3, 20, -side * 50, offset);
                }
            }
        }
        return t.build();
    }
}
