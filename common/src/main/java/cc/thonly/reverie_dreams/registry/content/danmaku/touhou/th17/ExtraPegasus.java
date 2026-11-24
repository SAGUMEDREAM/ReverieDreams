package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th17;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.EclBulletSprite;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.EclSpellTimeline;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.EclBulletSprite.*;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.EclSpellTimeline.*;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th17.Th17Spells.extra;

/** Saki, st07bs.ecl BossCard1–10. */
final class ExtraPegasus {
    private static final String SAKI = "kurokoma_saki";
    private ExtraPegasus() {}

    static List<TouhouSpell> spells() {
        return List.of(
                extra(91, SAKI, "劲疾技「惊险射击」", 4200, ExtraPegasus::thrillingShot),
                extra(92, SAKI, "劲疾技「闪电嘶鸣」", 4200, ExtraPegasus::lightningNeigh),
                extra(93, SAKI, "劲疾技「浓云」", 4200, ExtraPegasus::denseCloud),
                extra(94, SAKI, "劲疾技「兽性感染」", 4800, ExtraPegasus::epidemicity),
                extra(95, SAKI, "劲疾技「三角追击」", 4800, ExtraPegasus::triangleChase),
                extra(96, SAKI, "劲疾技「黑色天马流星弹」", 4800, ExtraPegasus::blackPegasus),
                extra(97, SAKI, "劲疾技「肌肉爆破」", 4800, ExtraPegasus::muscleExplosion),
                extra(98, SAKI, "「莫慌跟上我」", 5940, ExtraPegasus::followMe),
                extra(99, SAKI, "「鬼形的乌合之众」", 5880, ExtraPegasus::hoiPolloi),
                extra(100, SAKI, "「鬼畜生之所为」", 7200, ExtraPegasus::deeds));
    }

    private static DanmakuPattern thrillingShot() {
        var t = new EclSpellTimeline(4200, 90, 360);
        var blue = SCALE.shot(6, 9.3f, 88);
        var red = BALL.shot(2, 2f, 235);
        for (int i = 0; i < 67; i++) t.ring(90 + i * 3, red, 4, i * 137.508f);
        for (int column = -1; column <= 1; column++) {
            var offset = new Vec3(column * 32 / 30.0, 0.3, -Math.abs(column) * 32 / 30.0);
            for (int i = 0; i < 18; i++) {
                t.fan(350 + 3 * i, blue, 22, 189, (i - 8) * 9 + column * 7, offset);
            }
        }
        return t.build();
    }

    private static DanmakuPattern lightningNeigh() {
        var t = new EclSpellTimeline(4200, 90, 270);
        // Native beams retain a readable warning; one beam represents two adjacent 2f slices.
        for (int side : new int[]{-1, 1}) {
            for (int i = 0; i < 18; i++) {
                float yaw = side * (97 - 9 * i);
                var offset = new Vec3(side * (2.7 - i * 0.16), 0.5, 0.65 - i * 0.05);
                t.beam(90 + 4 * i, 27, 16, 320, 16, yaw, 0f, 0f, RICE.color(6), offset);
                t.beam(210 + 4 * i, 27, 16, 320, 16, -yaw, 0f, 0f, RICE.color(6), offset);
            }
        }
        return t.build();
    }

    private static DanmakuPattern denseCloud() {
        var t = new EclSpellTimeline(4200, 90, 270);
        var red = BALL.shot(2, 0.5f, 420, speed(32, 1.2f));
        for (int i = 0; i < 72; i++) {
            double angle = Math.toRadians(i * 137.508);
            var offset = new Vec3(3.1 * Math.sin(angle), 0.35, 3.1 * Math.cos(angle));
            t.rain(90 + i * 3, red, 3, 2, i * 137.508f, offset, 0.9f, 0.65f, 9f);
        }
        return t.build();
    }

    private static DanmakuPattern epidemicity() {
        var t = new EclSpellTimeline(4800, 90, 450);
        var flame = BLUE_FLAME.shot(6, 2.5f, 465, pause(70, 60), speed(130, 1.8f), curve(130, 0.16f));
        for (int i = 0; i < 3; i++) {
            var offset = new Vec3(i * 2.2 - 2.2, 0.4, 1.5 - i * 1.5);
            t.ring(90 + i * 150, flame, 16, i * 20, offset);
            t.ring(145 + i * 150, flame, 16, i * 20 + 11.25f, offset);
        }
        return t.build();
    }

    private static DanmakuPattern triangleChase() {
        var t = new EclSpellTimeline(4800, 120, 450);
        var scales = SCALE.shot(6, 0.01f, 840, pause(0, 600), speed(600, 1f));
        for (int side = 0; side < 3; side++) {
            double angle = Math.toRadians(side * 120);
            double next = angle + Math.PI * 2 / 3;
            var a = new Vec3(4 * Math.sin(angle), 0.4, 4 * Math.cos(angle));
            var b = new Vec3(4 * Math.sin(next), 0.4, 4 * Math.cos(next));
            for (int i = 0; i < 35; i++) {
                t.fan(120 + side * 120 + i * 3, scales, 1, 0, side * 120 + 60, a.lerp(b, i / 35.0));
            }
        }
        return t.build();
    }

    private static DanmakuPattern blackPegasus() {
        var t = new EclSpellTimeline(4800, 90, 360);
        var marker = LARGE_STAR.shot(1, 0.01f, 225, pause(0, 180), speed(180, 2f));
        var sparks = STAR.shot(13, 3f, 225);
        for (int i = 0; i < 12; i++) {
            int frame = 90 + 20 * i;
            var origin = new Vec3((i % 6 - 2.5) * 1.2, 1 + (i % 3) * 0.5, -1 - i * 0.21);
            t.fan(frame, marker, 1, 0, i * 12, origin);
            t.ring(frame + 12, sparks, 30, i * 29, origin);
            t.beam(frame + 24, 30, 18, 220, 16, (i % 7 - 3) * 24, 0f, 0f, RICE.color(2), origin);
        }
        return t.build();
    }

    private static DanmakuPattern muscleExplosion() {
        var t = new EclSpellTimeline(4800, 90, 570);
        for (int wave = 0; wave < 3; wave++) {
            int frame = 90 + wave * 175;
            for (int i = 0; i < 18; i++) {
                int delay = 70 + 4 * (i % 6);
                var orb = BALL.shot(13, 0.04f, 330, pause(0, delay), speed(delay, 2.5f - (i % 4) * 0.4f),
                        speed(delay + 60, 0.7f), speed(delay + 100, 1.7f));
                double angle = Math.toRadians(i * 20 + wave * 17);
                t.ring(frame + 3 * i, orb, 4, i * 20, new Vec3(Math.sin(angle) * 3.1, 0.45, Math.cos(angle) * 3.1));
            }
        }
        return t.build();
    }

    private static DanmakuPattern followMe() {
        var t = new EclSpellTimeline(5940, 90, 430);
        var trail = BALL.shot(6, 2f, 265, speed(120, 8f));
        var red = GLOW_ORB.shot(1, 1f, 330, speed(120, 3f));
        for (int side : new int[]{-1, 1}) {
            for (int i = 0; i < 50; i++) {
                int at = 90 + 8 * i;
                float yaw = side * (12 + 19 * (float) Math.sin(i * 0.27));
                t.fan(at, trail, 1, 0, yaw, new Vec3(side * (2.0 + i % 12 * 0.09), 0.45, -0.7));
            }
        }
        t.sweep(190, 504, 21, EmitShape.aim(), red, -18, 18, Vec3.ZERO);
        return t.build();
    }

    private static DanmakuPattern hoiPolloi() {
        var t = new EclSpellTimeline(5880, 0, 0);
        int[] counts = {10, 15, 20, 25, 30, 30, 30, 30, 45};
        int[] stages = {90, 750, 1410, 2070, 2730, 3390, 3990, 4510, 5210};
        var scout = BALL.shot(13, 2f, 340, speed(60, 0.5f));
        var red = RED_FLAME.shot(0, 0.4f, 270, speed(60, 1.4f));
        var purple = PURPLE_FLAME.shot(0, 2f, 340, speed(60, 0.5f), speed(120, 2.2f));
        for (int wave = 0; wave < stages.length; wave++) {
            for (int origin = 0; origin < 3; origin++) {
                double angle = Math.toRadians(wave * 28 + origin * 120);
                var offset = new Vec3(3.9 * Math.sin(angle), 0.6, 3.9 * Math.cos(angle));
                int at = stages[wave] + origin * 50;
                t.fan(at, scout, 6, 100, origin * 120, offset);
                t.fan(at + 60, red, 3, 24, origin * 120, offset);
                t.ring(at + 115, purple, counts[wave], wave * 17, offset);
            }
        }
        return t.build();
    }

    // Preserve the ordered HP/time-gate colors for item casts: pink, blue, red, ochre, pink.
    private static DanmakuPattern deeds() {
        var t = new EclSpellTimeline(7200, 0, 0);
        var sprites = new EclBulletSprite[]{NEEDLE, SCALE, SCALE, RICE, SCALE};
        int[] shades = {4, 6, 2, 13, 4};
        int[] times = {90, 1800, 3600, 5400, 6000, 7188};
        float[] speeds = {1, 1.8f, 2, 1.3f, 2.5f};
        for (int i = 0; i < sprites.length; i++) {
            int start = times[i];
            int end = times[i + 1] - 3;
            var shot = sprites[i].shot(shades[i], speeds[i], 390);
            for (int side : new int[]{-1, 1}) {
                int spread = i == 2 ? 12 : 8;
                t.sweep(start, end, i == 0 || i == 3 ? 3 : 6, EmitShape.fan(i == 2 ? 3 : 1, spread), shot,
                        side * (40 + i * 18), side * (end - start) * (i == 2 ? 0.5f : 1.12f),
                        new Vec3(side * (i % 3 + 1) * 0.65, 0.45, 0));
            }
        }
        return t.build();
    }
}
