package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th18;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th18.Th18PatternSupport.*;

/** Japanese TH18 st02bs.ecl, BossCard1–3. */
final class Stage02 {
    private static final String TAKANE = "entity.reverie_dreams.yamashiro_takane";
    private static final int GREEN = 0x6AB840;
    private static final int JADE = 0x4CADA0;
    private static final int MOSS = 0x99CF68;

    private Stage02() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card("010_normal", "森符「木隐的技术」", 2100, () -> camouflage(false)),
                card("011_hard", "森符「极·木隐的技术」", 2100, () -> camouflage(true)),
                card("014_normal", "森符「最深的森域」", 2400, () -> deepForest(false)),
                card("015_hard", "森符「极·最深的森域」", 2400, () -> deepForest(true)),
                card("018_normal", "叶技「绿色螺旋」", 2400, () -> cyclone(false)),
                card("019_hard", "叶技「绿色旋风」", 2400, () -> cyclone(true)));
    }

    private static TouhouSpell card(String id, String name, int frames, Supplier<DanmakuPattern> pattern) {
        return TouhouSpell.implemented("touhou/th18/s02_" + id, name, frames, pattern).withCharacter(TAKANE);
    }

    // BossCard1_at: 15 fans per arm at 22-degree angle increments. Mirror the two
    // arms and interpolate their density over the 2100-frame card.
    private static DanmakuPattern camouflage(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        int interval = hard ? 4 : 5;
        int pairFrames = 30 + 15 * interval;
        for (int frame = 90, pair = 0; frame < 2100; frame += pairFrames, pair++) {
            for (int arm = 0; arm < 2; arm++) {
                int side = ((pair + arm) % 2 == 0) ? 1 : -1;
                var blade = shot("water_drop", 0.2f, GREEN, 118, 0.0018f,
                        MotionSegment.rotate(1, side * 2.7f, UP));
                for (int wave = 0; wave < 15; wave++) {
                    int time = frame + arm * 30 + wave * interval;
                    if (time >= 2100) break;
                    int count = (hard ? 5 : 3) + (int) ((hard ? 2 : 3) * time / 2100f);
                    emitters.add(fire(EmitShape.fan(count, (count - 1) * 20f), blade,
                            time, side * (15f + wave * 22f), -4)
                            .withOffset(new Vec3(side * 0.75, 0.38, 0)));
                }
            }
        }
        return new DanmakuPattern(emitters).withTiming(new SpellTiming(ticks(2100), 0, 0));
    }

    // BossCard2_at: paired 16-way spirals, seven waves each. Hard adds five waves
    // of the second sprite and reduces the repeat step from 15 to 10 frames.
    private static DanmakuPattern deepForest(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        int waveCount = hard ? 12 : 7;
        int cadence = hard ? 10 : 15;
        int period = waveCount * cadence + 190;
        for (int wave = 0; wave < waveCount; wave++) {
            for (int side : new int[]{-1, 1}) {
                var bullet = shot(wave < 7 ? "water_drop" : "bacillus", 0.17f, wave < 7 ? GREEN : MOSS, 124, 0.0012f,
                        MotionSegment.rotate(1, side * 2.3f),
                        MotionSegment.setSpeed(68, 0.13f));
                emitters.add(fire(EmitShape.ring(16, 0.8f, true), bullet, 90 + wave * cadence,
                        side * (20 + wave * 5), 0).withOffset(new Vec3(side * 0.55, 0.38, 0)));
            }
        }
        var cue = shot("ball", 0.3f, JADE, 85, MotionSegment.setSpeed(20, 0.24f));
        for (int frame = 120; frame < 90 + period; frame += 30) {
            emitters.add(fire(EmitShape.fan(10, 130f), cue, frame, 0f, 5f));
        }
        return loop(emitters, 2400, 90, period);
    }

    // BossCard3: two runs of eight sources, incrementing by ±45 degrees. Each
    // source fires seven curved fans, followed by the distinct trailing ball.
    private static DanmakuPattern cyclone(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        int count = hard ? 4 : 3;
        for (int phase = 0; phase < 2; phase++) {
            int rotation = phase == 0 ? 1 : -1;
            for (int wave = 0; wave < 8; wave++) {
                float yaw = rotation * 45f * wave;
                int start = 120 + phase * 160 + wave * 20;
                var blades = shot("water_drop", 0.29f, GREEN, 115,
                        MotionSegment.rotate(1, (wave % 2 == 0 ? 1 : -1) * 11f),
                        MotionSegment.setSpeed(20, 0.19f));
                for (int burst = 0; burst < 7; burst++) {
                    emitters.add(fire(EmitShape.fan(count, (count - 1) * 30f), blades,
                            start + burst * 5, yaw + 90f, phase == 0 ? -4f : 4f));
                }
                var orb = shot("ball", 0.16f, MOSS, 108);
                emitters.add(fire(EmitShape.aim(), orb, start + 55, yaw, 0f));
            }
        }
        // Materialize every repeat so the finishing bullets can overlap the next pass.
        var waves = new ArrayList<DanmakuEmitter>();
        for (int frame = 0; frame < 2400; frame += 320) {
            for (var emitter : emitters) {
                int tick = ticks(frame) + emitter.delay();
                if (tick < ticks(2400)) waves.add(emitter.withTiming(tick, 0, 1));
            }
        }
        return new DanmakuPattern(waves).withTiming(new SpellTiming(ticks(2400), 0, 0));
    }
}
