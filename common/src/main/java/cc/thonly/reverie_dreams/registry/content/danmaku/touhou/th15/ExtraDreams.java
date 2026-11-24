package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th15;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.EclSpellTimeline;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.EclBulletSprite.*;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.EclSpellTimeline.*;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th15.Th15PatternSupport.extraCard;

/** Doremy's Extra midboss, st07mbs.ecl MBossCard1–3. */
final class ExtraDreams {
    private static final String DOREMY = "doremy_sweet";
    private ExtraDreams() {}

    static List<TouhouSpell> spells() {
        return List.of(
                extraCard(106, DOREMY, "蝴蝶「取而代之的蝴蝶」", 3000, ExtraDreams::butterflySupplantation),
                extraCard(107, DOREMY, "超特急「梦幻快车」", 3000, ExtraDreams::dreamExpress),
                extraCard(108, DOREMY, "爬梦「爬行的子弹」", 3000, ExtraDreams::creepingBullet));
    }

    // MBossCard1_at: fast sideways red butterflies, then paired sweeps through the aimed direction.
    private static DanmakuPattern butterflySupplantation() {
        var timeline = new EclSpellTimeline(3000, 60, 240);
        var crossing = BUTTERFLY.shot(1, 8f, 105);
        var trail = BUTTERFLY.shot(1, 2.2f, 220);
        for (int sign : new int[]{-1, 1}) {
            timeline.fan(60, crossing, 1, 0, 90 * sign, Vec3.ZERO);
            for (int i = 0; i < 60; i++) {
                timeline.fan(66 + 2 * i, trail, 1, 0, sign * (120 - i * 4), new Vec3(sign * 1.4, 0, 0));
            }
        }
        return timeline.build();
    }

    // MBossCard2_at/at2: nine parallel scale lanes with explicit colors, plus the red radial orbs.
    private static DanmakuPattern dreamExpress() {
        var timeline = new EclSpellTimeline(3000, 60, 180);
        int[] colors = {10, 11, 8, 13, 6, 14, 4, 2, 3};
        float[] offsets = {0, 16, -16, 32, -32, 48, -48, 64, -64};
        var red = ORB.shot(1, 4f, 160);
        for (int lane = 0; lane < colors.length; lane++) {
            var rail = SCALE.shot(colors[lane], 1f, 180, speed(60, 8f));
            for (int frame = 60; frame < 180; frame += 6) {
                timeline.fan(frame, rail, 1, 0, 0, new Vec3(offsets[lane] / 30, 0, 0));
            }
        }
        timeline.ring(60, red, 32, 0);
        timeline.ring(120, red, 32, 12);
        timeline.ring(180, red, 32, 24);
        return timeline.build();
    }

    private static DanmakuPattern creepingBullet() {
        var timeline = new EclSpellTimeline(3000, 60, 240);
        var rice = RICE.shot(1, 0.5f, 360);
        for (int i = 0; i < 120; i++) {
            // Stagger the original randomized triplets without orienting all volleys to world north.
            timeline.ring(60 + i * 2, rice, 3, i * 137.508f);
        }
        return timeline.build();
    }
}
