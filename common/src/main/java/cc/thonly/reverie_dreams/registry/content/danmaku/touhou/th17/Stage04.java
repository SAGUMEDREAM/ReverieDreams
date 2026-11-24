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

/** Yachie, st04bs.ecl BossCard1–3. */
final class Stage04 {
    private static final String YACHIE = "kitcho_yachie";
    private Stage04() {}

    static List<TouhouSpell> spells() {
        return Stream.of(
                variants(4, 32, YACHIE, "龟符「龟甲地狱」", "龟符「龟甲地狱」", 2400, Stage04::tortoiseShell),
                variants(4, 36, YACHIE, "鬼符「邪道之畜生」", "鬼符「邪道之狗畜生」", 2400, Stage04::sneakAttack),
                variants(4, 40, YACHIE, "龙符「龙纹弹」", "龙符「龙纹弹」", 3300, Stage04::dragonCrest))
                .flatMap(List::stream).toList();
    }

    private static DanmakuPattern tortoiseShell(boolean hard) {
        var t = new EclSpellTimeline(2400, 90, 300);
        var needle = NEEDLE.shot(10, 0.04f, 360, speed(100, 1.55f));
        for (int arm = 0; arm < 6; arm++) {
            for (int edge = 0; edge < 10; edge++) {
                double angle = Math.toRadians(arm * 60);
                double next = Math.toRadians((arm + 1) * 60);
                double u = edge / 10.0;
                var a = new Vec3(Math.sin(angle) * 4, 0.25, Math.cos(angle) * 4);
                var b = new Vec3(Math.sin(next) * 4, 0.25, Math.cos(next) * 4);
                t.emit(90 + 3 * edge, EmitShape.aim(), needle, arm * 60 + 30, a.lerp(b, u));
            }
        }
        flankFlames(t, 126, 378, hard ? 12 : 18, 1f, 1, hard ? 0.6f : 0.45f);
        return t.build();
    }

    private static DanmakuPattern sneakAttack(boolean hard) {
        var t = new EclSpellTimeline(2400, 90, 320);
        flankFlames(t, 90, 345, hard ? 15 : 20, 2.5f, hard ? 3 : 2, hard ? 0.95f : 0.68f);
        return t.build();
    }

    // The original side arcs are aimed once per cast; no projectile constantly steers toward a player.
    private static void flankFlames(EclSpellTimeline t, int first, int end, int step, float velocity, int count, float arc) {
        for (int side : new int[]{-1, 1}) {
            var flame = RED_FLAME.shot(6, velocity, 400, speed(60, 1.1f), curve(60, arc * side));
            for (int frame = first + (side > 0 ? step / 2 : 0); frame < end; frame += step) {
                t.fan(frame, flame, count, 14, side * 45, new Vec3(side * 2.75, 0.65, -1));
            }
        }
    }

    private static DanmakuPattern dragonCrest(boolean hard) {
        var t = new EclSpellTimeline(3300, 90, 540);
        int[][] colors = {{10, 2}, {6, 13}, {4, 8}};
        for (int phase = 0; phase < colors.length; phase++) {
            int waveAt = 90 + phase * 180;
            var starter = SCALE.shot(10, 1.03f, 255);
            t.ring(waveAt, starter, 12, phase * 30);
            for (int stream = 0; stream < 2; stream++) {
                var scale = SCALE.shot(colors[phase][stream], hard ? 1.8f : 1.5f, 270);
                for (int wing = 0; wing < 8; wing++) {
                    float yaw = phase * 55 + (stream == 0 ? 1 : -1) * (wing * 15 + 12);
                    t.fan(waveAt + 30 + wing * (hard ? 9 : 12), scale, 5, 100, yaw,
                            new Vec3(stream == 0 ? -1.7 : 1.7, 0.45, 0));
                }
            }
        }
        return t.build();
    }
}
