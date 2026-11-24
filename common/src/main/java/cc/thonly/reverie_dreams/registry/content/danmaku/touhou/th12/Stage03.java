package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th12;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuLaser;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th12.Th12PatternSupport.*;

/** TH12 stage03.ecl: Ichirin and Unzan's spell-practice entries. */
final class Stage03 {
    private static final String UNZAN = "item.reverie_dreams.spellcard.character.ichirin_unzan";
    private static final int RED = 0xE65260;
    private static final int PURPLE = 0x8B7EE8;
    private static final int BLUE = 0x6388DB;
    private static final int PALE = 0xE7DFF5;

    private Stage03() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(3, 28, false, UNZAN, "铁拳「问答无用妖怪拳」", 3000, () -> monsterFist(false)),
                card(3, 29, true, UNZAN, "神拳「凌云地狱冲」", 3000, () -> monsterFist(true)),
                card(3, 32, false, UNZAN, "拳符「天网沙袋」", 2700, () -> sandbag(false)),
                card(3, 33, true, UNZAN, "连打「云界海妖来袭」", 2100, () -> sandbag(true)),
                card(3, 36, false, UNZAN, "拳打「重拳碎击」", 2400, () -> hookStorm(false)),
                card(3, 37, true, UNZAN, "溃灭「天上天下连续勾拳」", 2400, () -> hookStorm(true)),
                card(3, 40, false, UNZAN, "大喝「守旧尊老之怒眼」", 2400, () -> angryEye(false)),
                card(3, 41, true, UNZAN, "忿怒「天变巨眼焚身」", 2400, () -> angryEye(true)));
    }

    // MBossCard1ArmL/R spawn moving arms and MBossCard1ArmAt fires one sprite 3/4 bullet every two frames.
    // Normal has two arms; Hard adds two player-directed arms on frame 170.
    private static DanmakuPattern monsterFist(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var armShot = shot(BULLET, 0.18f, RED, 100, MotionSegment.accelerate(ticks(40), 0.006f));
        int cycle = hard ? 360 : 301;
        for (int wave = 0, frame = 150; frame < 2500; wave++, frame += cycle) {
            for (int side : new int[]{-1, 1}) {
                shots.add(fire(EmitShape.aim(), armShot, frame, 600, 2)
                        .withOffset(new Vec3(side * 7.0f, 0.5, 0)).withJitter(18f));
            }
            if (hard) {
                shots.add(fire(EmitShape.fan(3, 52f), armShot, frame + 20, 500, 2)
                        .withOffset(new Vec3(-4.0f, 0.5, 0)).withJitter(14f));
                shots.add(fire(EmitShape.fan(3, 52f), armShot, frame + 20, 500, 2)
                        .withOffset(new Vec3(4.0f, 0.5, 0)).withJitter(14f));
            }
        }
        return timeline(shots, 3000);
    }

    // BossCard1_at/_atH create three or five arms per side. Each arm lasts about 30 frames and
    // BossCard1ArmAt sweeps its muzzle 12 px per frame while firing every frame.
    private static DanmakuPattern sandbag(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        int arms = hard ? 5 : 3;
        for (int side : new int[]{-1, 1}) {
            for (int i = 0; i < arms; i++) {
                float y = 1.8f + i * 0.8f;
                shots.add(fire(EmitShape.aim(), shot(BULLET, 0.26f, PALE, 92), 150, 30, 3)
                        .withOffset(new Vec3(side * 10.0f, y, 0)).withJitter(8f));
                shots.add(fire(EmitShape.aim(), shot(BULLET, 0.26f, PALE, 92), 411, 30, 3)
                        .withOffset(new Vec3(-side * 10.0f, y, 0)).withJitter(8f));
            }
        }
        var seal = shot(BALL, 0.28f, PURPLE, 90);
        shots.add(fire(EmitShape.ring(8, 2.5f, true), seal, 660, 0, 3));
        shots.add(fire(EmitShape.ring(8, 2.5f, true), seal, 990, 0, 3));
        return timeline(shots, hard ? 2100 : 2700);
    }

    // BossCard2_at/_atH create four arm pairs per wave. BossCard2ArmAt is one 10/14-bullet radial burst.
    private static DanmakuPattern hookStorm(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        int count = hard ? 14 : 10;
        float radius = hard ? 3.0f : 4.0f;
        int waveCount = hard ? 4 : 2;
        int start = 150;
        for (int wave = 0; wave < waveCount; wave++) {
            for (int arm = 0; arm < 4; arm++) {
                float y = 2.0f + arm * 8.0f;
                int frame = start + arm * (hard ? 30 : 80);
                for (int side : new int[]{-1, 1}) {
                    shots.add(fire(EmitShape.ring(count, radius, true), shot(BULLET, 0.20f, BLUE, 100), frame, 0, 3)
                            .withOffset(new Vec3(side * 6.0f, y, 0)).withJitter(10f));
                }
            }
            start += hard ? 420 : 870;
        }
        return timeline(shots, 2400);
    }

    // BossCard3Head/R each perform three 16-laser groups. BossCard3ArmAt adds slower center/side rain.
    private static DanmakuPattern angryEye(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var beams = new ArrayList<DanmakuLaser>();
        for (int cycle = 0; cycle < 6; cycle++) {
            int base = 150 + cycle * 384;
            for (int group = 0; group < 3; group++) {
                int frame = base + group * 132;
                for (int i = 0; i < 16; i++) {
                    float yaw = i * 22.5f;
                    beams.add(laser(ticks(frame), 0f, yaw, 8f, 0.9f, 8, 12, 0f, PURPLE));
                }
            }
            if (base < 1900) {
                for (int side : new int[]{-1, 1}) {
                    shots.add(fire(EmitShape.aim(), shot(BULLET, 0.18f, RED, 100), base + 360, 90, hard ? 16 : 8)
                            .withOffset(new Vec3(side * 8.0f, 0.5, 0)).withJitter(16f));
                }
            }
        }
        return timeline(shots, beams, 2400);
    }
}
