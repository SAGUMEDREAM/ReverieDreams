package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th12;

import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeRange;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th12.Th12PatternSupport.*;

/** TH12 stage02.ecl: Kogasa's midboss and boss spell-practice entries. */
final class Stage02 {
    private static final String KOGASA = "tatara_kogasa";
    private static final int GREEN = 0x69CC58;
    private static final int PURPLE = 0x8B7EE8;
    private static final int BLUE = 0x6388DB;
    private static final int RED = 0xE65260;
    private static final int PALE = 0xE7DFF5;

    private Stage02() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(2, 12, false, KOGASA, "大轮「唐伞光晕」", 1800, () -> halo(false)),
                card(2, 13, true, KOGASA, "大轮「Hello, Forgotten World」（你好，被遗忘的世界）", 1800, () -> halo(true)),
                card(2, 16, false, KOGASA, "伞符「Parasol Star Symphony」（雨伞的星之交响）", 2400, () -> starSymphony(false)),
                card(2, 17, true, KOGASA, "伞符「Parasol Star Memories」（雨伞的星之追忆）", 2100, () -> starSymphony(true)),
                card(2, 20, false, KOGASA, "雨符「雨夜怪谈」", 2400, () -> rainNight(false)),
                card(2, 21, true, KOGASA, "雨伞「超防水干爽伞妖」", 2400, () -> rainNight(true)),
                card(2, 24, false, KOGASA, "化符「遗忘之伞的夜行列车」", 2400, () -> nightTrain(false)),
                card(2, 25, true, KOGASA, "化铁「备用伞特急夜晚狂欢号」", 2400, () -> nightTrain(true)));
    }

    // MBossCard1At: one shot every frame for 112 frames. etDist shrinks 128->16 px while the base angle
    // turns by 0.124174 rad/frame; the ECL count is 3 on Normal and 6 on Hard.
    private static DanmakuPattern halo(boolean hard) {
        var ring = shot(BALL, 0.30f, GREEN, 84, MotionSegment.accelerate(ticks(10), 0.010f));
        var shots = new ArrayList<DanmakuEmitter>();
        for (int frame = 130; frame < 1800; frame += 218) {
            shots.add(fire(EmitShape.ring(hard ? 6 : 3, 4.0f, true), ring, frame, 112, 1)
                    .withRanges(KeyframeRange.empty(0f, 0f), KeyframeRange.empty(0f, 796f))
                    .withJitter(6f));
        }
        return timeline(shots, 1800);
    }

    // BossCard1_at fires for 40 frames with count 3/4, shrinking dist 64->24 px and +0.146121 rad/frame.
    // BossCard1 schedules three bursts per visible cycle, then moveRand/wait gives the next cycle.
    private static DanmakuPattern starSymphony(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var ring = shot(BALL, hard ? 0.34f : 0.30f, PURPLE, 88, MotionSegment.accelerate(ticks(10), 0.010f));
        int[] burstLags = hard ? new int[]{10, 74, 138} : new int[]{10, 104, 198};
        for (int start = 120; start < (hard ? 2100 : 2400); start += hard ? 320 : 306) {
            for (int lag : burstLags) {
                int frame = start + lag;
                if (frame >= (hard ? 2100 : 2400)) {
                    break;
                }
                shots.add(fire(EmitShape.ring(hard ? 4 : 3, 2.4f, true), ring, frame, 40, 1)
                        .withRanges(KeyframeRange.empty(0f, 0f), KeyframeRange.empty(0f, 335f)));
            }
        }
        return timeline(shots, hard ? 2100 : 2400);
    }

    // BossCard2At_at: fixed downward shot with a random ring offset and radius 0->192 px then 192->16 px.
    // Hard fires six bullets in the same window; Normal fires one.
    private static DanmakuPattern rainNight(boolean hard) {
        var rain = shot(BULLET, 0.22f, BLUE, 100, MotionSegment.accelerate(ticks(20), 0.012f));
        var emitter = fire(EmitShape.fan(hard ? 6 : 1, 22.5f), rain, 90, 2310, hard ? 3 : 4)
                .withOffset(new Vec3(0, 0.5, 0));
        emitter = spread(emitter, 16f, 6f, 0f, 0.75f);
        return timeline(List.of(emitter), 2400);
    }

    // BossCard3_at/_at2/_atU create ParasolAt3 familiars; their internal et timing is approximated as
    // continuous two/three-shot side fans. BossCard3_at3 is the explicit 16 x 8 center multi-ring.
    private static DanmakuPattern nightTrain(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var side = shot(PELLET, 0.24f, GREEN, 90, MotionSegment.accelerate(ticks(20), 0.010f));
        var upper = shot(PELLET, 0.24f, BLUE, 90, MotionSegment.accelerate(ticks(20), 0.010f));
        int frame = 90;
        int cycle = hard ? 510 : 760;
        while (frame < 2400) {
            shots.add(fire(EmitShape.fan(hard ? 3 : 2, 48f), side, frame, 540, 8)
                    .withOffset(new Vec3(-8.0f, 0.25, 0)).withJitter(10f));
            shots.add(fire(EmitShape.fan(hard ? 3 : 2, 48f), side, frame, 540, 8)
                    .withOffset(new Vec3(8.0f, 0.25, 0)).withJitter(10f));
            shots.add(fire(EmitShape.fan(2, 48f), upper, frame + 160, 480, 8)
                    .withOffset(new Vec3(-8.0f, 1.5, 0)).withJitter(10f));
            shots.add(fire(EmitShape.fan(2, 48f), upper, frame + 160, 480, 8)
                    .withOffset(new Vec3(8.0f, 1.5, 0)).withJitter(10f));
            shots.add(fire(EmitShape.multiRing(8, 16, 0.2f, false, 22.5f, 0.2f, 0f),
                    shot(BULLET, 0.30f, RED, 100, MotionSegment.accelerate(ticks(40), 0.008f)),
                    frame, 0, 3));
            shots.add(fire(EmitShape.multiRing(8, 16, 0.2f, false, 22.5f, 0.2f, 0f),
                    shot(BULLET, 0.30f, RED, 100, MotionSegment.accelerate(ticks(40), 0.008f)),
                    frame + 160, 0, 3));
            frame += cycle;
        }
        return timeline(shots, 2400);
    }
}
