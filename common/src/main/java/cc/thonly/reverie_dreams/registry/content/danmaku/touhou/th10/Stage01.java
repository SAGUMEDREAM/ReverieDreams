package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th10;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuScatter;
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

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.catalogOnly;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.implemented;

final class Stage01 {
    private Stage01() {}

    /**
     * 原始减速区间 60 帧（60fps）→ 20 tick（20tps）。
     */
    private static final int DECEL_TICKS = 20;
    // 38 shots per slot in ECL; reduce density to preserve navigable gaps for MC hitboxes.
    private static final int RING_COUNT = 12;
    private static final float AUTUMN_SPEED = 0.5f;
    private static final float[][] FIRST_TURNS = {{90f, -90f, 90f, -90f}, {-90f, 90f, -90f, 90f}};
    // Both leaf waves use the same original bullet sprite family (ins_402 type 3).
    private static final Identifier LEAF_TYPE = ReverieDreams.id("leaf");

    /**
     * 一面：道中 秋静叶（Normal 无符卡，Hard 起有卡）、关底 秋穰子。
     */
    static List<TouhouSpell> spells() {
        return List.of(
                // stage01.ecl.raw MBossCard1 L1516：359 基号 0 掩码 FF，Normal 展开为 −1 不登记
                implemented("touhou/th10/s01_000_hard",
                        "叶符「纷乱的落叶」", 2160, () -> fallingLeaves()),
                implemented("touhou/th10/s01_003_normal",
                        "秋符「秋季的天空」", 2700, () -> autumnSkyNormal()),
                implemented("touhou/th10/s01_004_hard",
                        "秋符「无常秋日与少女的心」", 2700, () -> autumnSkyAndMaidenHeartHard()),
                catalogOnly("touhou/th10/s01_007_normal",
                        "丰符「大年收获者」", 2700),
                catalogOnly("touhou/th10/s01_008_hard",
                        "丰收「谷物神的允诺」", 2700)
        );
    }

    // MBossCard1_at: a slow blue leaf followed by alternating leaf colours and horizontal bends.
    // Source movement and the original random-angle samples are mapped to a bounded MC field.
    private static DanmakuPattern fallingLeaves() {
        List<DanmakuEmitter> emitters = new ArrayList<>();
        int[] colors = {0x88BBEE, 0xECC64A, 0xE37C28, 0xC34D35};
        for (int slot = 0; slot < colors.length; slot++) {
            float curl = slot % 2 == 0 ? 3.1f : -3.1f;
            var motion = DanmakuMotion.of(Vec3.ZERO, 0, 0, List.of(
                    MotionSegment.accelerate(0, 0.008f),
                    new MotionSegment(60, MotionSegment.ROTATE, curl,
                            new Vec3(0, 1, 0), Vec3.ZERO, MotionSegment.CENTER_SELF, 40),
                    MotionSegment.linear(100)
            ));
            emitters.add(DanmakuEmitter.of(EmitShape.aim(), DanmakuBulletSpec.of(LEAF_TYPE, 0.22f, motion)
                            .withColor(colors[slot]).withLifetime(130))
                    .withTiming(50 + slot, 639, 4)
                    .withAim(EmitAim.FIXED, 0, 0)
                    .withScatter(new DanmakuScatter(0, 0, 180, 0.27f)));
        }
        return new DanmakuPattern(emitters).withTiming(new SpellTiming(720, 0, 0));
    }

    private static DanmakuPattern autumnSkyNormal() {
        return autumnSky(false);
    }

    private static DanmakuPattern autumnSkyAndMaidenHeartHard() {
        return autumnSky(true);
    }

    // stage01 BossCard1 L451–680: first four fixed rings, opposing second group after
    // the difficulty-dependent wait. Each slot has three relative direction transforms.
    private static DanmakuPattern autumnSky(boolean hard) {
        List<DanmakuEmitter> emitters = new ArrayList<>();
        float[] finalTurns = {90f, -60f, (float) Math.toDegrees(0.8975979), -72f};
        int second = hard ? 110 : 150;
        for (int group = 0; group < 2; group++) {
            int at = SpellTiming.framesToTicks(group == 0 ? 150 : second * 3);
            for (int slot = 0; slot < finalTurns.length; slot++) {
                float first = FIRST_TURNS[group][slot];
                float third = finalTurns[slot] * (group == 0 ? 1 : -1);
                emitters.add(leafRingEmitter(autumnMotion(first, third), at));
            }
        }
        if (hard) {
            for (int cue : new int[]{210, 270, 330, 390, 450, 510}) {
                emitters.add(DanmakuEmitter.of(EmitShape.ring(10, 0, true),
                                DanmakuBulletSpec.of(LEAF_TYPE, 0.75f).withColor(0xF1B756).withLifetime(75))
                        .withTiming(SpellTiming.framesToTicks(cue), 3, 3)
                        .withAim(EmitAim.TARGET, 0, 0));
            }
        }
        return new DanmakuPattern(emitters).withTiming(new SpellTiming(900, 50, hard ? 121 : 161));
    }

    private static DanmakuEmitter leafRingEmitter(DanmakuMotion motion, int delay) {
        return DanmakuEmitter.of(EmitShape.ring(RING_COUNT, 0, true),
                        DanmakuBulletSpec.of(LEAF_TYPE, AUTUMN_SPEED, motion).withColor(0xE08038).withLifetime(115))
                .withTiming(delay, 0, 1).withAim(EmitAim.FIXED, 0, 0);
    }

    private static DanmakuMotion autumnMotion(float first, float third) {
        return DanmakuMotion.of(Vec3.ZERO, 0, 0, List.of(
                MotionSegment.accelerate(1, -AUTUMN_SPEED / DECEL_TICKS),
                MotionSegment.turn(DECEL_TICKS, first, new Vec3(0, 1, 0)),
                MotionSegment.setSpeed(DECEL_TICKS, AUTUMN_SPEED),
                MotionSegment.turn(2 * DECEL_TICKS, -first, new Vec3(0, 1, 0)),
                MotionSegment.setSpeed(2 * DECEL_TICKS, AUTUMN_SPEED),
                MotionSegment.turn(3 * DECEL_TICKS, third, new Vec3(0, 1, 0)),
                MotionSegment.setSpeed(3 * DECEL_TICKS, AUTUMN_SPEED * 0.6f),
                MotionSegment.linear(3 * DECEL_TICKS)));
    }
}
