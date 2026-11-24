package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th18;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuLaser;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionPayload;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th18.Th18PatternSupport.*;

/** Japanese TH18 st05bs.ecl, BossCard1–4. */
final class Stage05 {
    private static final String OWNER = "entity.reverie_dreams.iizunamaru_megumu";
    private static final int[] COLORS = {0xEC5B64, 0xDE9664, 0xE8CC67, 0x73B976, 0x668BDF, 0xAB72DA};

    private Stage05() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card("046_normal", "祸星「星火燎原之舞」", 3000, () -> wildfire(false)),
                card("047_hard", "祸星「星火燎原乱舞」", 3000, () -> wildfire(true)),
                card("050_normal", "星风「虹彩陆离之舞」", 2700, () -> iridescentDance(false)),
                card("051_hard", "星风「虹彩陆离乱舞」", 2700, () -> iridescentDance(true)),
                card("054_normal", "光马「天马行空之舞」", 2700, () -> pegasus(false)),
                card("055_hard", "光马「天马行空乱舞」", 2700, () -> pegasus(true)),
                card("058_normal", "虹光「光风霁月」", 3600, () -> clearMoon(false)),
                card("059_hard", "虹光「光风霁月」", 3600, () -> clearMoon(true)));
    }

    private static TouhouSpell card(String id, String name, int frames, Supplier<DanmakuPattern> pattern) {
        return TouhouSpell.implemented("touhou/th18/s05_" + id, name, frames, pattern).withCharacter(OWNER);
    }

    // BossCard1_Et places the slow stars first; the callback releases them
    // towards the front after a long, varying delay.
    private static DanmakuPattern wildfire(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        for (int phase = 0; phase < 2; phase++) {
            for (int index = 0; index < 5; index++) {
                float angle = (phase == 0 ? 1 : -1) * index * 72f;
                double rad = Math.toRadians(angle);
                var star = shot("star", 0.026f, COLORS[(index + phase) % COLORS.length], 153,
                        MotionSegment.turn(68 + index * 6, phase == 0 ? 28 : -28, UP),
                        MotionSegment.setSpeed(68 + index * 6, 0.27f));
                emitters.add(fire(EmitShape.aim(), star, 120 + phase * 270 + index * 22,
                        angle, -3f).withTiming(ticks(120 + phase * 270 + index * 22), ticks(180), ticks(5))
                        .withOffset(new Vec3(3.3 * Math.sin(rad), 0.6 + index * 0.11, 3.3 * Math.cos(rad)))
                        .withJitter(hard ? 13f : 7f));
            }
        }
        return loop(emitters, 3000, 120, 570);
    }

    // BossCard2_at: two interleaved laser sweeps, with 60/50f warning and 9/8f cadence.
    private static DanmakuPattern iridescentDance(boolean hard) {
        int duration = 2700;
        return loop(List.of(), duration, 0, 0)
                .withLasers(rainbowSweeps(duration, 120, hard ? 8 : 9, hard ? 50 : 60, false));
    }

    // BossCard3_at: five parent orbs, with delayed 3/4-way detachments.
    private static DanmakuPattern pegasus(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        for (int side : new int[]{-1, 1}) {
            int start = side < 0 ? 120 : 450;
            var orb = shot("ball", 0.36f, COLORS[4], 133,
                    MotionSegment.split(9, pegasusChildren(hard, COLORS[1])),
                    MotionSegment.turn(12, side * 28, UP),
                    MotionSegment.split(19, pegasusChildren(hard, COLORS[4])),
                    MotionSegment.setSpeed(24, 0.13f),
                    MotionSegment.split(31, pegasusChildren(hard, COLORS[5])),
                    MotionSegment.setSpeed(36, 0.28f));
            emitters.add(fire(EmitShape.fan(5, 80f), orb, start, side * 46f, -9f).withJitter(8f));
        }
        return loop(emitters, 2700, 120, 660);
    }

    private static MotionPayload pegasusChildren(boolean hard, int color) {
        return MotionPayload.split(ReverieDreams.id("star"), color, 0.73f, hard ? 4 : 3, 45f, 0.22f, 95);
    }

    // BossCard4_at2 distributes six colors by one sixth of the ring step.
    // The ring fires next to the returning laser sweep.
    private static DanmakuPattern clearMoon(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        int count = hard ? 5 : 3;
        for (int frame = 120, wave = 0; frame < 3600; frame += 260, wave++) {
            for (int color = 0; color < COLORS.length; color++) {
                var orb = shot("ball", 0.12f, COLORS[color], 145, MotionSegment.setSpeed(60, 0.25f));
                emitters.add(fire(EmitShape.ring(count, 1.7f, true), orb, frame,
                        wave * 23f + color * 60f / count, -5f));
            }
        }
        return loop(emitters, 3600, 0, 0).withLasers(rainbowSweeps(3600, 150, hard ? 7 : 8, 60, true));
    }

    private static List<DanmakuLaser> rainbowSweeps(int duration, int start, int cadence, int warn, boolean returning) {
        var lasers = new ArrayList<DanmakuLaser>();
        for (int phaseStart = start, wave = 0; phaseStart < duration; phaseStart += 260, wave++) {
            int sign = wave % 2 == 0 ? 1 : -1;
            for (int beam = 0; beam * cadence < 130 && phaseStart + beam * cadence < duration; beam++) {
                float yaw = sign * (68f - beam * 7f) + (returning ? 180f : 0f);
                lasers.add(new DanmakuLaser(ticks(phaseStart + beam * cadence),
                        beam % 3 * 5f - 5f, yaw, 19f, 0.54f, ticks(warn), ticks(60),
                        0f, COLORS[(wave + beam) % COLORS.length]));
            }
        }
        return lasers;
    }
}
