package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th17;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
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

/** Keiki, st06bs.ecl BossCard1–7. The first three cards sculpt squares, circles and straight lines. */
final class Stage06 {
    private static final String KEIKI = "haniyasushin_keiki";
    private Stage06() {}

    static List<TouhouSpell> spells() {
        return Stream.of(
                variants(6, 60, KEIKI, "方形「方形造形术」", "方形「方形造物」", 3300, Stage06::squares),
                variants(6, 64, KEIKI, "圆形「正圆造形术」", "圆形「圆形造物」", 3300, Stage06::circles),
                variants(6, 68, KEIKI, "线形「线形造形术」", "线形「线形造物」", 3300, Stage06::lines),
                variants(6, 72, KEIKI, "埴轮「偶像人马造形术」", "埴轮「偶像造物」", 3600, Stage06::idol),
                variants(6, 76, KEIKI, "「鬼形造形术」", "「鬼形造形术」", 3600, Stage06::beastSculpting),
                variants(6, 80, KEIKI, "「几何造物」", "「几何造物」", 4800, Stage06::geometric),
                variants(6, 84, KEIKI, "「造形恶魔」", "「造形恶魔」", 7200, Stage06::idolaDiabolus))
                .flatMap(List::stream).toList();
    }

    private static DanmakuPattern squares(boolean hard) {
        var t = new EclSpellTimeline(3300, 90, 480);
        // st06bs:823–1029 passes needle color 6 on N and 13 on H.
        var needle = NEEDLE.shot(hard ? 13 : 6, 2f, 230);
        for (int i = 0; i < 6; i++) {
            polygon(t, 90 + i * (hard ? 56 : 64), hard ? 12 : 8, 4,
                    i % 3 == 2 ? 2.1 : 3.1, i * 13, needle);
        }
        return t.build();
    }

    private static DanmakuPattern circles(boolean hard) {
        var t = new EclSpellTimeline(3300, 90, 780);
        var orb = BALL.shot(2, 0.01f, 440, pause(0, 100), speed(100, 2.6f));
        for (int i = 0; i < 5; i++) {
            float radius = 1.7f + 0.5f * i;
            var center = new Vec3((i % 2 == 0 ? 1 : -1) * 1.7, 0, i * 0.45);
            t.emit(90 + 94 * i, EmitShape.ring(hard ? 46 : 40, radius, true), orb, 12 * i, center);
        }
        return t.build();
    }

    private static DanmakuPattern lines(boolean hard) {
        var t = new EclSpellTimeline(3300, 90, 450);
        // st06bs:1389–1465: N uses tint 13, H uses 6; the warning beam keeps the same tint.
        int shade = hard ? 6 : 13;
        int rows = hard ? 7 : 5;
        var needle = NEEDLE.shot(shade, hard ? 3f : 2f, 245);
        for (int wave = 0; wave < 2; wave++) {
            int at = 90 + wave * (hard ? 190 : 220);
            for (int i = 0; i < rows; i++) {
                double x = (i - (rows - 1) / 2.0) * (hard ? 1.05 : 1.3);
                float yaw = wave == 0 ? 12f : -12f;
                var offset = new Vec3(x, 0.25, 0);
                t.beam(at + 3 * i, 60, 60, 512, 16, yaw, 0, 0, RICE.color(shade), offset);
                t.fan(at + 18 + 3 * i, needle, 2, 4, yaw, offset);
            }
        }
        return t.build();
    }

    private static DanmakuPattern idol(boolean hard) {
        var t = new EclSpellTimeline(3600, 90, 540);
        var pellet = BALL.shot(4, hard ? 3f : 2.2f, 240);
        var aquamarine = ARROW.shot(4, 2f, 250, speed(20, 1f), speed(50, 2.3f));
        var gold = ARROW.shot(6, 2.3f, 285);
        for (int i = 0; i < 3; i++) {
            int at = 90 + i * 170;
            t.ring(at, pellet, hard ? 24 : 16, i * 17);
            for (int lane = -2; lane <= 2; lane++) {
                var offset = new Vec3(lane * 1.2, 0.4, -1.7 + Math.abs(lane) * 0.45);
                t.fan(at + 45, aquamarine, hard ? 5 : 4, 55, 0, offset);
                t.fan(at + 70 + 3 * lane, gold, hard ? 4 : 2, 50, -lane * 8, offset);
            }
        }
        return t.build();
    }

    private static DanmakuPattern beastSculpting(boolean hard) {
        var t = new EclSpellTimeline(3600, 90, 660);
        var red = NEEDLE.shot(2, hard ? 3.1f : 2.7f, 240);
        var blue = NEEDLE.shot(6, 1f, 305);
        for (int limb = 0; limb < 4; limb++) {
            int start = 90 + limb * 5;
            t.sweep(start, start + 69, hard ? 3 : 4, EmitShape.aim(), red,
                    -72 + limb * 38, -12 + limb * 38, new Vec3((limb - 1.5) * 1.4, 0.5, 0));
        }
        for (int wave = 0; wave < 3; wave++) t.ring(505 + 62 * wave, blue, 8, wave * 15);
        return t.build();
    }

    private static DanmakuPattern geometric(boolean hard) {
        var t = new EclSpellTimeline(4800, 120, 690);
        int[] colors = hard ? new int[]{13, 2, 13} : new int[]{6, 6, 6};
        for (int wave = 0; wave < colors.length; wave++) {
            int at = 120 + wave * 210;
            var needle = NEEDLE.shot(colors[wave], 1.85f, 365, pause(65, 60), speed(125, 2.1f));
            polygon(t, at, hard && wave == 1 ? 8 : 6, wave == 1 ? 6 : 4,
                    2.8 + wave * 0.5, wave * 19, needle);
            polygon(t, at + 62, 6, wave == 1 ? 4 : 6, 2.3, -wave * 19, needle);
        }
        return t.build();
    }

    private static DanmakuPattern idolaDiabolus(boolean hard) {
        var t = new EclSpellTimeline(7200, 90, 0);
        int[] colors = {2, 6, 13, 4};
        float[] speeds = hard ? new float[]{1.1f, 1.4f, 1.6f, 1.8f} : new float[]{1f, 1.4f, 1.6f, 1.8f};
        for (int phase = 0; phase < 4; phase++) {
            int start = 90 + phase * 1750;
            int end = start + 1728;
            for (int arm = 0; arm < colors.length; arm++) {
                float angle = (arm + 1) * 60f;
                float rate = (arm % 2 == 0 ? 1 : -1) * (0.72f + phase * 0.11f);
                var shot = KUNAI.shot(colors[arm], speeds[arm], 405);
                t.sweep(start + arm * 3, end, hard ? 6 : 9, EmitShape.aim(), shot,
                        angle, angle + rate * (end - start), new Vec3((arm - 1.5) * 0.6, 0.4, 0));
            }
        }
        return t.build();
    }

    /** Sample each side of the original mirrored lattice in the caster's local frame. */
    private static void polygon(EclSpellTimeline t, int at, int perSide, int sides, double radius,
                                 float yaw, DanmakuBulletSpec shot) {
        for (int side = 0; side < sides; side++) {
            double a = Math.toRadians(yaw + 360.0 * side / sides);
            double b = a + 2 * Math.PI / sides;
            var left = new Vec3(radius * Math.sin(a), 0.2, radius * Math.cos(a));
            var right = new Vec3(radius * Math.sin(b), 0.2, radius * Math.cos(b));
            float direction = yaw + (side + 0.5f) * 360f / sides;
            for (int i = 0; i < perSide; i++) {
                t.emit(at + 3 * i, EmitShape.aim(), shot, direction, left.lerp(right, i / (double) perSide));
            }
        }
    }
}
