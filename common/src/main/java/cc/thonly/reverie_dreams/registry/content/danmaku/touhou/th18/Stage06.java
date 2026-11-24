package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th18;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeRange;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.DanmakuMotion;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/** Chimata's six stage-six attacks. Source: Japanese TH18 st06bs.ecl, BossCard1–6. */
final class Stage06 {
    private static final String CHIMATA = "entity.reverie_dreams.tenkyu_chimata";
    private static final Identifier AMULET = ReverieDreams.id("amulet");
    private static final Identifier NEEDLE = ReverieDreams.id("needle");
    private static final Identifier BALL = ReverieDreams.id("ball");
    private static final Identifier DROP = ReverieDreams.id("water_drop");
    private static final int[] RAINBOW = {0xC058D6, 0x6767E6, 0x44CFD1, 0x65BF62, 0xEAE16F, 0xF69356, 0xDF5560};

    private Stage06() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(62, false, "「献给无主的供物」", 4200, () -> offering(false)),
                card(63, true, "「献给无主的供物」", 4200, () -> offering(true)),
                card(66, false, "「弹幕收集狂的妄执」", 4200, () -> collector(false)),
                card(67, true, "「弹幕收集狂的妄执」", 4200, () -> collector(true)),
                card(70, false, "「弹幕市场」", 3300, () -> market(false)),
                card(71, true, "「高密度弹幕市场」", 3300, () -> market(true)),
                card(74, false, "「虹人环」", 3000, () -> rainbowRing(false)),
                card(75, true, "「虹人环」", 3000, () -> rainbowRing(true)),
                card(78, false, "「弹幕领土」", 3600, () -> dominion(false)),
                card(79, true, "「暴虐的弹幕领土」", 3600, () -> dominion(true)),
                card(82, false, "「弹幕的庇护所」", 7200, () -> asylum(false)),
                card(83, true, "「弹幕的庇护所」", 7200, () -> asylum(true)));
    }

    private static TouhouSpell card(int index, boolean hard, String name, int frames, Supplier<DanmakuPattern> pattern) {
        var path = String.format(Locale.ROOT, "touhou/th18/s06_%03d_%s", index, hard ? "hard" : "normal");
        return TouhouSpell.implemented(path, name, frames, pattern).withCharacter(CHIMATA);
    }

    // Boss1Card_at: five depth layers, twice as many talismans on the four rear layers.
    // Translate the player-centered playfield to a corridor ahead of the caster.
    private static DanmakuPattern offering(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        int frame = 90;
        for (int cycle = 0; frame < 4090; cycle++) {
            int gap = Math.max(hard ? 46 : 57, (hard ? 100 : 110) - (cycle % 8) * 10);
            for (int color = 0; color < 5 && frame < 4090; color++, frame += gap) {
                int count = Math.min(hard ? 8 : 6, (hard ? 4 : 3) + color);
                for (int depth = 0; depth < 5; depth++) {
                    float pitch = 5f - depth * 4f;
                    float yaw = (color & 1) == 0 ? 7f : -7f;
                    var bullet = bullet(AMULET, 0.10f + depth * 0.014f, RAINBOW[color], 142,
                            MotionSegment.setSpeed(20, 0.22f + depth * 0.02f));
                    emitters.add(volley(EmitShape.fan(depth == 0 ? count : count * 2, depth == 0 ? 54f : 78f),
                            bullet, frame, 0, 3, pitch, yaw).withOffset(new Vec3(0, 0.18 * depth, 0.92 * depth)));
                }
            }
        }
        return timeline(emitters, 4200);
    }

    // BossCard2_at: register five mirrored sources 180 frames apart. Each fires for 180 frames, then rests for 720.
    private static DanmakuPattern collector(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        emitters.add(volley(EmitShape.ring(hard ? 26 : 18, 2.5f, true),
                bullet(AMULET, 0.19f, RAINBOW[6], 180, MotionSegment.setSpeed(20, 0.28f)), 90, 0, 3, -3, 0));
        int cadence = hard ? 5 : 6;
        for (int wave = 0; wave < 5; wave++) {
            for (int side : new int[]{-1, 1}) {
                var needle = bullet(NEEDLE, 0.085f, RAINBOW[wave], 155, MotionSegment.freeze(10, 10),
                        MotionSegment.setSpeed(20, 0.34f));
                for (int start = 290 + wave * 180; start < 4200; start += 900) {
                    emitters.add(sweep(volley(EmitShape.aim(), needle, start, 180 - cadence, cadence, side * 6f, 0f),
                            side * -39f, side * 18f).withOffset(new Vec3(side * 3.2, 0.28 * (wave + 1), 1.6)).withJitter(9f));
                }
            }
        }
        return timeline(emitters, 4200);
    }

    // BossCard3_at and at2: drifting ring bullets in two overlapping layers, 16/24 bullets every 180/170 frames.
    private static DanmakuPattern market(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        int step = hard ? 170 : 180;
        for (int start = 120, wave = 0; start < 3300; start += step, wave++) {
            int count = hard ? 24 : 16;
            var ring = EmitShape.ring(count, 2.9f, true);
            var blue = bullet(BALL, 0.085f, RAINBOW[1], 145, MotionSegment.freeze(15, 14), MotionSegment.setSpeed(29, 0.25f));
            var red = bullet(BALL, 0.09f, RAINBOW[6], hard ? 190 : 154,
                    MotionSegment.rotate(20, hard ? 1.2f : 0.7f), MotionSegment.setSpeed(20, 0.21f));
            float phase = (wave * 17f) % 360;
            emitters.add(volley(ring, blue, start, 0, 3, 6, phase).withOffset(new Vec3(0, 0, 2.5)));
            emitters.add(volley(ring, red, start + 24, 0, 3, -5, phase + 180f / count).withOffset(new Vec3(0, 1, 3.5)));
        }
        return timeline(emitters, 3300);
    }

    // BossCard4_at: seven colors separated by 40 degrees, with decreasing gaps.
    private static DanmakuPattern rainbowRing(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        for (int color = 0; color < RAINBOW.length; color++) {
            int start = 120 + color * 2;
            float angle = 120f - color * 40f;
            var drop = bullet(DROP, 0.30f, RAINBOW[color], 105,
                    MotionSegment.freeze(17, 8), MotionSegment.setSpeed(25, 0.39f));
            for (int frame = start; frame < 3000; ) {
                float progress = Math.min(1f, (frame - start) / (hard ? 2400f : 1800f));
                double radians = Math.toRadians(angle + progress * 30f);
                Vec3 offset = new Vec3(4 * Math.sin(radians), 0.4 + 0.12 * color, 4 + 2 * Math.cos(radians));
                emitters.add(volley(EmitShape.aim(), drop, frame, 0, 1, 5, (float) Math.toDegrees(radians))
                        .withOffset(offset));
                frame += Math.round(hard ? (15 - 8 * progress) : (30 - 20 * progress));
            }
        }
        return timeline(emitters, 3000);
    }

    // BossCard5_at: seven threaded needle lines, 4/2-frame steps and rotating origins.
    private static DanmakuPattern dominion(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        for (int color = 0; color < RAINBOW.length; color++) {
            float angle = color * (360f / 7f);
            for (int frame = 120; frame < 3600; frame += 600) {
                var needle = bullet(NEEDLE, 0.25f, RAINBOW[color], 120,
                        MotionSegment.freeze(7, 7), MotionSegment.setSpeed(14, 0.38f));
                emitters.add(sweep(volley(EmitShape.aim(), needle, frame, 162, hard ? 2 : 4,
                        7f, angle).withOffset(new Vec3(3.2, (color % 3) * 0.42, 0)),
                        0, hard ? 245f : 148f).withRotation(KeyframeRange.empty(angle, angle - 135f)));
            }
        }
        return timeline(emitters, 3600);
    }

    // BossCard6 has HP gates at the boss. The item advances through four timed sections with the same deadline.
    private static DanmakuPattern asylum(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        for (int phase = 0; phase < 4; phase++) {
            int start = 120 + phase * 1760;
            int width = 1640;
            for (int color = 0; color < 5; color++) {
                float angle = color * 72f;
                var drop = bullet(DROP, (hard ? 0.31f : 0.27f) + 0.028f * color, RAINBOW[color], 145);
                emitters.add(sweep(volley(EmitShape.ring((hard ? 7 : 5) + phase, 1.4f, true),
                        drop, start + color * 19, width, hard ? 95 : 110, -3f + 4f * phase, angle),
                        0, 55f + 31f * phase));
            }
            if (phase > 0) {
                var circle = bullet(BALL, 0.11f, RAINBOW[phase + 2], 130,
                        MotionSegment.setSpeed(25, hard ? 0.34f : 0.28f));
                emitters.add(sweep(volley(EmitShape.ring(phase * 4 + (hard ? 18 : 13), 3.6f, true), circle,
                        start + 90, width - 90, 150 - 18 * phase, 1, 0), 0, -90f * phase));
            }
        }
        return timeline(emitters, 7200);
    }

    private static DanmakuEmitter volley(EmitShape shape, DanmakuBulletSpec bullet, int start, int window, int interval,
                                         float pitch, float yaw) {
        return DanmakuEmitter.of(shape, bullet).withTiming(ticks(start), ticks(window), Math.max(1, ticks(interval)))
                .withAim(EmitAim.FIXED, pitch, yaw);
    }

    private static DanmakuEmitter sweep(DanmakuEmitter emitter, float from, float to) {
        return emitter.withRanges(KeyframeRange.empty(0, 0), KeyframeRange.empty(from, to));
    }

    private static DanmakuBulletSpec bullet(Identifier type, float speed, int color, int lifetime, MotionSegment... motion) {
        return DanmakuBulletSpec.of(type, speed, DanmakuMotion.of(Vec3.ZERO, 0, 0, List.of(motion)))
                .withColor(color).withLifetime(lifetime);
    }

    private static int ticks(int frames) {
        return SpellTiming.framesToTicks(frames);
    }

    private static DanmakuPattern timeline(List<DanmakuEmitter> emitters, int frames) {
        return new DanmakuPattern(emitters).withTiming(new SpellTiming(ticks(frames), 0, 0));
    }
}
