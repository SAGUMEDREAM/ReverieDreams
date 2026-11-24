package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th18;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
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

/** Japanese TH18 st04bs.ecl, BossCard1–3. */
final class Stage04 {
    private static final String OWNER = "entity.reverie_dreams.tamatsukuri_misumaru";
    private static final int[] COLORS = {0xA66DDD, 0x6795E6, 0x55C4C3, 0x74BB67, 0xE5C957, 0xDB864E, 0xDC5965};

    private Stage04() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card("034_normal", "玉符「虹龙阴阳玉」", 2700, () -> rainbowOrbs(false)),
                card("035_hard", "玉符「虹龙阴阳玉」", 2700, () -> rainbowOrbs(true)),
                card("038_normal", "玉将「阴阳球女王」", 2700, () -> rainbowGate(false)),
                card("039_hard", "女王珠「彩虹门的另一侧」", 2700, () -> rainbowGate(true)),
                card("042_normal", "「阴阳窒息」", 3300, () -> suffocation(false)),
                card("043_hard", "「阴阳窒息」", 3300, () -> suffocation(true)));
    }

    private static TouhouSpell card(String id, String name, int frames, Supplier<DanmakuPattern> pattern) {
        return TouhouSpell.implemented("touhou/th18/s04_" + id, name, frames, pattern).withCharacter(OWNER);
    }

    // BossCard1_at: two seven-color groups with opposite curvature and distinct second-stage speeds.
    private static DanmakuPattern rainbowOrbs(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        for (int side : new int[]{-1, 1}) {
            for (int index = 0; index < COLORS.length; index++) {
                var orb = shot("yin_yang", 0.16f, COLORS[index], 146, 0.0012f,
                        MotionSegment.rotate(2, side * 6f), MotionSegment.linear(40),
                        MotionSegment.setSpeed(41, side > 0 ? 0.26f : 0.17f),
                        MotionSegment.turn(65, side * 60f, UP),
                        MotionSegment.transform(83, MotionPayload.transform(ReverieDreams.id("yin_yang"), COLORS[index], 0.67f)));
                emitters.add(fire(EmitShape.fan(3, 28f), orb, 150,
                        index * (360f / COLORS.length) + side * 15f, -4f)
                        .withOffset(new Vec3(side * 1.1, 0.18, 0)));
            }
        }
        return loop(emitters, 2700, 90, hard ? 210 : 240);
    }

    // BossCard2_at: 14/20 parents each detach four times, changing color between
    // releases, then pause and begin moving again.
    private static DanmakuPattern rainbowGate(boolean hard) {
        var orb = shot("yin_yang", 0.24f, COLORS[0], 154, gateMotion());
        var emitters = new ArrayList<DanmakuEmitter>();
        for (int frame = 90, wave = 0; frame < 2700; frame += 430, wave++) {
            emitters.add(fire(EmitShape.ring(hard ? 20 : 14, 1.1f, true),
                    orb, frame, wave * 31f, -2f));
        }
        return loop(emitters, 2700, 0, 0);
    }

    private static MotionSegment[] gateMotion() {
        var motion = new ArrayList<MotionSegment>();
        for (int split = 0; split < 4; split++) {
            int at = 8 + split * 7;
            motion.add(MotionSegment.split(at, MotionPayload.split(
                    ReverieDreams.id("ball"), COLORS[split + 1], 0.8f, 1, 0, 0.19f, 87)));
            motion.add(MotionSegment.transform(at + 1, MotionPayload.transform(
                    ReverieDreams.id("yin_yang"), COLORS[split + 1], 1f)));
            motion.add(MotionSegment.turn(at + 2, 45f, UP));
        }
        motion.add(MotionSegment.freeze(36, 40));
        motion.add(MotionSegment.setSpeed(76, 0.28f));
        return motion.toArray(MotionSegment[]::new);
    }

    // BossCard3_at wraps horizontal positions by 237.32504 px over a 384 px field.
    // BossCard3_at2 alternates the two curved orb groups while their counts grow.
    private static DanmakuPattern suffocation(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var rain = shot("yin_yang", 0.2f, COLORS[1], 118);
        double x = 0;
        for (int frame = 120, index = 0; frame < 3300; frame += hard ? 24 : 30, index++) {
            x = (x + 237.32504) % 384.0;
            emitters.add(fire(EmitShape.aim(), rain, frame, 0, 25f).withJitter(5f)
                    .withOffset(new Vec3((x - 192) / 28, 3.5 + (index % 4) * 0.22, 0.65)));
        }
        for (int frame = 180, wave = 0; frame < 3300; frame += 290, wave++) {
            int direction = wave % 2 == 0 ? 1 : -1;
            int count = Math.min(hard ? 6 : 5, (hard ? 4 : 3) + wave / 2);
            for (int arm = 0; arm < 7; arm++) {
                var orb = shot("yin_yang", 0.13f, COLORS[arm], 135, 0.0015f,
                        MotionSegment.rotate(1, direction * 6f), MotionSegment.linear(34),
                        MotionSegment.setSpeed(35, 0.23f));
                emitters.add(fire(EmitShape.fan(count, 22f), orb, frame,
                        direction * arm * 360f / 7, -2f));
            }
        }
        return loop(emitters, 3300, 0, 0);
    }
}
