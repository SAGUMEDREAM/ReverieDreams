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

/** Japanese TH18 st03bs.ecl, BossCard1–3. */
final class Stage03 {
    private static final String OWNER = "entity.reverie_dreams.komakusa_sannyo";
    private static final int RED = 0xD65A65;
    private static final int BLUE = 0x698FE2;
    private static final int GREEN = 0x70BF72;

    private Stage03() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card("022_normal", "山符「动天的云间草」", 2400, () -> saxifrage(false)),
                card("023_hard", "山怪「惊愕的云间草」", 2400, () -> saxifrage(true)),
                card("026_normal", "山符「妖光闪耀的薄雪草」", 2400, () -> edelweiss(false)),
                card("027_hard", "山怪「群妖骚动的薄雪草」", 2400, () -> edelweiss(true)),
                card("030_normal", "山花「杀戮的驹草」", 2700, () -> mountainQueen(false)),
                card("031_hard", "山花「杀戮的山之女王」", 2700, () -> mountainQueen(true)));
    }

    private static TouhouSpell card(String id, String name, int frames, Supplier<DanmakuPattern> pattern) {
        return TouhouSpell.implemented("touhou/th18/s03_" + id, name, frames, pattern).withCharacter(OWNER);
    }

    // BossCard1_at turns each cluster across seven spokes. Retain every third
    // cluster in the spatial port, so the returning arms have readable gaps.
    private static DanmakuPattern saxifrage(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        int clusters = (hard ? 45 : 60) / 3;
        for (int phase = 0; phase < 2; phase++) {
            int turn = phase == 0 ? 1 : -1;
            var seed = shot("ball", 0.16f, RED, 105, MotionSegment.freeze(12, 6),
                    MotionSegment.turn(18, turn * 88f, UP), MotionSegment.setSpeed(18, 0.28f),
                    MotionSegment.turn(40, -turn * 60f, UP));
            for (int group = 0; group < clusters; group++) {
                var shape = EmitShape.multiRing(hard ? 2 : 1, 7, 1.2f, true,
                        7f, 0.24f, 0.12f);
                emitters.add(fire(shape, seed, 90 + phase * 240 + group * 2, group * 360f / clusters, 3f));
            }
        }
        return loop(emitters, 2400, 90, 480);
    }

    // BossCard2_at2b uses five beams 72 degrees apart. The laser's detached
    // fragments are represented by a delayed pellet wave on the same headings.
    private static DanmakuPattern edelweiss(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var lasers = new ArrayList<DanmakuLaser>();
        var pellet = shot("ball", 0.25f, BLUE, 92, MotionSegment.setSpeed(30, 0.09f));
        for (int start = 90; start < 2400; start += 470) {
            for (int volley = 0; volley < 3; volley++) {
                int frame = start + volley * 90;
                if (frame >= 2400) break;
                for (int beam = 0; beam < 5; beam++) {
                    float yaw = beam * 72f + 18f * volley;
                    lasers.add(new DanmakuLaser(ticks(frame), -4f, yaw, 16f, 0.75f,
                            10, 20, 0f, BLUE));
                    emitters.add(fire(EmitShape.fan(6, 58f), pellet, frame + 32, yaw, -4f)
                            .withOffset(new Vec3(0, 0.4, 2f)));
                }
            }
        }
        // The background ring callback is installed only on Hard and Lunatic.
        if (hard) {
            var ring = shot("ball", 0.22f, GREEN, 100, MotionSegment.setSpeed(32, 0.08f));
            for (int frame = 290; frame < 2400; frame += 120) {
                emitters.add(fire(EmitShape.ring(40, 1.2f, true), ring, frame, frame * 0.21f, 3f));
            }
        }
        return loop(emitters, 2400, 0, 0).withLasers(lasers);
    }

    // BossCard3_at: 6/9 parent flowers, alternating ±36 degrees between
    // successive launches. Each flower arrests, sheds small bullets, and reforms.
    private static DanmakuPattern mountainQueen(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        int count = hard ? 9 : 6;
        int period = count * 30 + 160;
        for (int phase = 0; phase < 2; phase++) {
            int turn = phase == 0 ? 1 : -1;
            for (int flower = 0; flower < count; flower++) {
                var seed = shot("ball", 0.29f, phase == 0 ? RED : GREEN, 114,
                        MotionSegment.freeze(28, 18),
                        MotionSegment.split(36, MotionPayload.split(ReverieDreams.id("pellet"), RED, 0.7f, 9, 124f, 0.23f, 68)),
                        MotionSegment.transform(39, MotionPayload.transform(ReverieDreams.id("sakura_petal"), RED, 0.89f)),
                        MotionSegment.turn(47, turn * 34f, UP), MotionSegment.setSpeed(47, 0.21f));
                emitters.add(fire(EmitShape.aim(), seed, 120 + phase * period + flower * 30,
                        phase * 180f + turn * flower * 36f, -9f));
            }
        }
        return loop(emitters, 2700, 120, period * 2);
    }
}
