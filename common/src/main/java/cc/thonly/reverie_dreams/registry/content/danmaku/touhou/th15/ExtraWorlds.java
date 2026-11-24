package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th15;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.MovingLaserSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.EclBulletSprite;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.EclSpellTimeline;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.EclBulletSprite.*;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.EclSpellTimeline.*;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th15.Th15PatternSupport.extraCard;

/** st07bs.ecl BossCard1–10 and the companion's last-card streams in st07bs2.ecl. */
final class ExtraWorlds {
    private static final String HECATIA = "hecatia_lapislazuli";
    private static final String JUNKO = "junko";
    private ExtraWorlds() {}

    static List<TouhouSpell> spells() {
        return List.of(
                extraCard(109, HECATIA, "异界「逢魔之刻」", 4200, () -> otherworld(false)),
                extraCard(110, HECATIA, "地球「邪秽在身」", 4200, () -> earthRain(false)),
                extraCard(111, HECATIA, "月「阿波罗反射镜」", 4200, ExtraWorlds::apolloMirror),
                extraCard(112, JUNKO, "「用于逼死瓮中鼠的单纯弹幕」", 4800, ExtraWorlds::corneredRat),
                extraCard(113, HECATIA, "异界「地狱的非理想弹幕」", 4800, () -> otherworld(true)),
                extraCard(114, HECATIA, "地球「落向地狱的雨」", 4800, () -> earthRain(true)),
                extraCard(115, HECATIA, "月「月狂冲击」", 4800, ExtraWorlds::lunaticImpact),
                extraCard(116, JUNKO, "「用于杀人的纯粹弹幕」", 4800, ExtraWorlds::pureDanmaku),
                extraCard(117, HECATIA, "「三位一体论狂想曲」", 5400, ExtraWorlds::trinitarianRhapsody),
                extraCard(118, HECATIA, "「最初也是最后的无名弹幕」", 7200, ExtraWorlds::namelessDanmaku));
    }

    private static DanmakuPattern otherworld(boolean nonIdeal) {
        var timeline = new EclSpellTimeline(nonIdeal ? 4800 : 4200, 60, 240);
        var star = STAR.shot(6, 1.5f, 300);
        for (int side : new int[]{-1, 1}) {
            var flame = nonIdeal
                    ? RED_FLAME.shot(0, 2f, 370, curve(0, side * 0.65f), curve(60, -side * 0.65f))
                    : BLUE_FLAME.shot(0, 2f, 370, curve(60, side * 0.2f), speed(100, 1.35f));
            for (int i = 0; i < 24; i++) {
                timeline.fan(60 + (side < 0 ? 0 : 120) + i * 2, flame,
                        6, 225, side * i * 7.5f, Vec3.ZERO);
            }
        }
        if (nonIdeal) {
            for (int i = 0; i < 6; i++) timeline.ring(60 + 40 * i, star, 32, i * 17);
        }
        return timeline.build();
    }

    // The lifted outer sources map the two screen edges. Opcodes 134217728 and 16384
    // consume the flame and launch one blue flying laser (color index 6).
    private static DanmakuPattern earthRain(boolean hell) {
        var timeline = new EclSpellTimeline(hell ? 4800 : 4200, 60, 240);
        var star = STAR.shot(6, 1.5f, 320);
        for (int i = 0; i < 6; i++) {
            timeline.ring(60 + 40 * i, star, 32, hell ? 0 : i * 23);
        }
        for (int i = 0; i < 16; i++) {
            int phase = 120 + (i % 5) * 24;
            var beam = blueBeam(hell ? 4f : 1f, 360 - phase);
            var flame = hell
                    ? BLUE_FLAME.shot(0, 1.6f, 360,
                            MotionSegment.turn(ticks(phase), 180f, new Vec3(0, 1, 0)), MotionSegment.fireBeam(ticks(phase), beam))
                    : BLUE_FLAME.shot(0, 1.6f, 360, MotionSegment.fireBeam(ticks(phase), beam));
            for (int side : new int[]{-1, 1}) {
                timeline.rain(75 + 12 * i, flame, 2, 12, side * 14,
                        new Vec3(side * (2 + i % 4), 2, -3), 0.8f, 0.3f, hell ? 5 : 2);
            }
        }
        return timeline.build();
    }

    private static MovingLaserSpec blueBeam(float speedPx, int lifetimeFrames) {
        return new MovingLaserSpec(speedPx / 10f, 32f / 30f, 96f / 30f, 16f / 30f, ticks(lifetimeFrames),
                LASER_FRAGMENT.color(6), true);
    }

    private static DanmakuPattern apolloMirror() {
        var timeline = new EclSpellTimeline(4200, 60, 240);
        // st07bs:1808–1810: fire from the actual reflected edge; inherit the reflection's direction.
        var seed = STAR.shot(6, 1.5f, 450, MotionSegment.bounceWithBeam(0, 9.6f, blueBeam(3.5f, 315)));
        for (int i = 0; i < 3; i++) {
            timeline.ring(60 + i * 80, seed, 48, i * 17);
        }
        return timeline.build();
    }

    private static DanmakuPattern corneredRat() {
        var timeline = new EclSpellTimeline(4800, 90, 240);
        var wall = OVAL.shot(3, 0.5f, 480);
        var cluster = GLOW_ORB.shot(1, 0.5f, 480);
        for (int side : new int[]{-1, 1}) {
            for (int i = 0; i < 10; i++) {
                var edge = new Vec3(side * 5.5, 0, -4 + i * 0.8);
                timeline.fan(90 + i * 9, wall, 1, 0, -90 * side, edge);
            }
            timeline.ring(190, cluster, 8, side * 20, new Vec3(side * 4, 0, 4));
        }
        return timeline.build();
    }

    private static DanmakuPattern lunaticImpact() {
        var timeline = new EclSpellTimeline(4800, 0, 0);
        var star = STAR.shot(6, 1.5f, 300);
        for (int at : new int[]{94, 604, 1234}) {
            // BossTrase and BossCard7_Moon_at: blue stars come from the edge struck by the moon.
            var impact = STAR.splitPayload(6, 24, 108, 1.4f, 315);
            var movingMoon = MOON.shot(0, 1.35f, 4800 - at,
                    MotionSegment.bounceWithSplit(0, 8.5f, impact), speed(120, 3f));
            timeline.emit(at, EmitShape.aim(), movingMoon, 0, new Vec3(0, 1.4, -2));
            for (int i = 0; i < 10; i++) {
                timeline.fan(at + 60 + i * 40, star, 20, 133, (i % 2 == 0 ? 1 : -1) * 12, Vec3.ZERO);
            }
        }
        return timeline.build();
    }

    private static DanmakuPattern pureDanmaku() {
        var timeline = new EclSpellTimeline(4800, 94, 360);
        var aimed = GLOW_ORB.shot(1, 1.5f, 320);
        var arrested = GLOW_ORB.shot(1, 1.5f, 380, pause(90, 57), speed(147, 1.5f));
        for (int i = 0; i < 4; i++) {
            timeline.ring(94 + i * 90, i % 2 == 0 ? aimed : arrested, 61, i * 6);
        }
        return timeline.build();
    }

    // Three successive worlds use blue, green and red rings with matching heart layers.
    private static DanmakuPattern trinitarianRhapsody() {
        var timeline = new EclSpellTimeline(5400, 0, 0);
        trinitarianWaves(timeline, 204, 2020, 180, STAR, 6, 3, 32, 40);
        trinitarianWaves(timeline, 2220, 3390, 84, STAR, 10, 5, 48, 20);
        trinitarianWaves(timeline, 3620, 5330, 96, BALL, 2, 1, 15, 20);
        return timeline.build();
    }

    private static void trinitarianWaves(EclSpellTimeline timeline, int start, int end, int interval,
                                         EclBulletSprite sprite, int color, int heart, int ways, int stopAt) {
        var core = sprite.shot(color, 2.5f, 300, pause(stopAt, 40), speed(stopAt + 40, 1.5f));
        var halo = HEART.shot(heart, 2.5f, 220, speed(stopAt, 0.25f));
        for (int frame = start, i = 0; frame < end; frame += interval, i++) {
            double angle = Math.toRadians(i * 120 + (frame - start) * 0.15);
            var origin = new Vec3(3.5 * Math.sin(angle), 0, 3.5 * Math.cos(angle));
            timeline.ring(frame, core, ways, i * 11, origin);
            timeline.ring(frame + 9, halo, 64, i * 11 + 6, origin);
        }
    }

    private static DanmakuPattern namelessDanmaku() {
        var timeline = new EclSpellTimeline(7200, 0, 0);
        var pink = KUNAI.shot(4, 2.5f, 310);
        var red = KUNAI.shot(2, 2f, 350);
        var companion = BALL.shot(6, 2f, 285); // st07bs2:162,196,230, all three companion phases
        for (int stage = 0; stage < 3; stage++) {
            // The shared-health HP gates become time gates for an item cast; both sources stay visible.
            int start = 94 + 2360 * stage;
            int end = start + 2259;
            float turn = (end - start) * (stage == 0 ? 1.3f : 1f);
            var shot = stage == 2 ? red : pink;
            timeline.sweep(start, end, 3, EmitShape.fan(stage == 0 ? 5 : 7, 9), shot,
                    0, turn, new Vec3(-2.2, 0, 0));
            int cadence = new int[]{100, 80, 60}[stage];
            for (int at = start; at < end; at += cadence) {
                timeline.ring(at, companion, 90, -(at - start) / 5f, new Vec3(2.2, 0, 0));
            }
        }
        return timeline.build();
    }
}
