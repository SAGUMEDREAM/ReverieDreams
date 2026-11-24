package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th14;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th14.Th14PatternSupport.*;

/**
 * TH14 st04bs2.ecl (Yatsuhashi Tsukumo). Numbers are the ECL spell ids.
 * Source: E:/Temp/rd-ecl-continuation-20261116/th14/ecl/st04bs2.ecl.txt.
 */
final class Stage04B {
    private static final String YATSU = "item.reverie_dreams.spellcard.character.tsukumo_yatsuhashi";
    private static final int WHITE = 0xE8E8F0;
    private static final int VIOLET = 0x9C6CC8;

    private Stage04B() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(4, 54, false, YATSU, "琴符「诸行无常的琴声」", 2400, () -> impermanentKoto(false)),
                card(4, 55, true, YATSU, "琴符「诸行无常的琴声」", 2400, () -> impermanentKoto(true)),
                card(4, 58, false, YATSU, "响符「平安的残响」", 2400, () -> echoChamber(false)),
                card(4, 59, true, YATSU, "响符「Echo Chamber」（回音之庭）", 2400, () -> echoChamber(true)),
                card(4, 62, false, YATSU, "筝曲「下克上送筝曲」", 3000, () -> gekokujou(false)),
                card(4, 63, true, YATSU, "筝曲「下克上安魂曲」", 3000, () -> gekokujou(true)));
    }

    /**
     * BossBCard1 (st04bs2 L345): spell 52, 2400f, one declaration for all difficulties.
     * BossBCard1_at loops forever from frame 90: every 12 frames an aimed fan of 6 (Normal) /
     * 8 (Hard) white notes with 30/20-degree spacing, speed 0.4 -> 1.0 px/frame over 40 frames,
     * plus four wandering curve terms of +/-0.008-0.02 rad/frame.
     */
    private static DanmakuPattern impermanentKoto(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var note = shot(NOTE, 0.075f, WHITE, 80,
                MotionSegment.accelerate(0, 0.009f), MotionSegment.linear(ticks(40)),
                MotionSegment.rotate(ticks(41), 1.2f));
        shots.add(sweep(fire(EmitShape.fan(hard ? 8 : 6, hard ? 140f : 150f), note, 90, 2310, 12),
                0f, 0f, 0f).withJitter(8f));
        return timeline(shots, 2400);
    }

    /**
     * BossBCard2 (st04bs2 L487): spell 56, 2400f. BossBCard2_at loops from frame 120: every 8
     * frames 2 (Normal) / 3 (Hard) white notes whose base yaw advances 4 degrees per shot while
     * the speed grows 2.0 -> 2.7 px/frame; etEx param 64 bounces them off the field walls.
     */
    private static DanmakuPattern echoChamber(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        int volleyCount = (2400 - 120) / 8;
        var note = shot(NOTE, 0.37f, WHITE, 130,
                MotionSegment.accelerate(0, 0.0006f), MotionSegment.bounce(0, 11f));
        shots.add(sweep(fire(EmitShape.fan(hard ? 3 : 2, hard ? 3.75f : 1.875f), note,
                120, 2280, 8), 0f, 0f, volleyCount * 4f)
                .withOffset(new Vec3(0, 1.2, 0)));
        return timeline(shots, 2400);
    }

    /**
     * BossBCard3 (st04bs2 L577): spell 60, 3000f. BossBCard3_at rains single white notes upward
     * (etAngle -90 degrees, speed 3.2 -> 1.0 px/frame) from the mirrored screen bottom, fifteen
     * per 96-frame cycle on Normal plus a second ten-note stream on Hard. After each 220-frame
     * dash BossBCard3_at2 answers with six aimed violet rounds on a 20-degree fan.
     */
    private static DanmakuPattern gekokujou(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var rain = shot(NOTE, 0.6f, WHITE, 60,
                MotionSegment.accelerate(0, -0.02f), MotionSegment.linear(ticks(20)));
        for (int cycle = 0, start = 300; start < 3000; cycle++, start += 96) {
            float x = (cycle & 1) == 0 ? -5.5f : 5.5f;
            int window = Math.min(90, 3000 - start);
            shots.add(sweep(fire(EmitShape.aim(), rain, start, window, 6), -80f, 0f, 0f)
                    .withOffset(new Vec3(x, 0.3, 3.5)).withJitter(10f));
            if (hard) {
                shots.add(sweep(fire(EmitShape.aim(), rain, start + 6, Math.min(60, 3000 - start - 6), 6), -80f, 0f, 0f)
                        .withOffset(new Vec3(-x, 0.3, 3.0)).withJitter(10f));
            }
        }
        var volley = shot(BALL, 0.22f, VIOLET, 80,
                MotionSegment.accelerate(0, -0.0015f), MotionSegment.linear(ticks(20)));
        for (int start = 640; start < 3000; start += 280) {
            shots.add(fire(EmitShape.fan(6, 100f), volley, start, 0, 3).withJitter(6f));
        }
        return timeline(shots, 3000);
    }
}
