package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th16;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuFamiliar;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuLaser;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th16.Th16PatternSupport.*;

/** TH16 st03mbs/st03bs.ecl: Larva's spring cameo and Komano Aunn's stage-3 cards. */
final class Stage03 {
    private static final String AUNN = "komano_aunn";
    private static final String LARVA = "eternity_larva";
    private static final int CYAN = 0x28FFFF;
    private static final int MAGENTA = 0xF600E1;
    private static final int WHITE = 0xFFFFFF;
    private static final int RED = 0xFF0000;

    private Stage03() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(3, 20, true, LARVA, "春符「惊喜之春」", 1320, Stage03::surpriseSpring),
                card(3, 22, false, AUNN, "犬符「野狗散步」", 1920, () -> strayDogWalk(1)),
                card(3, 22, true, AUNN, "狗符「山狗散步」", 1920, () -> strayDogWalk(2)),
                card(3, 26, false, AUNN, "独乐「coma犬回旋」", 1980, () -> komaSpin(false)),
                // ECL shows the L branch with a distinct name; it takes the hard catalog slot.
                card(3, 26, true, AUNN, "独乐「卷起与死亡」", 1980, Stage03::curlUpAndDie),
                card(3, 30, false, AUNN, "狛符「一人阿吽之呼吸」", 2400, Stage03::aunBreath),
                card(3, 30, true, AUNN, "狛符「一人阿吽之呼吸」", 2400, Stage03::aunBreath));
    }

    // st03mbs MBossCard1 (164-313): four interleaved rotating streams (counts 3/10/8/8, speeds
    // 1.0/1.5/2.0/2.5, dists 80/64/16/48 px) at 3f cadence with headings -10/+25/-20/+30 deg per
    // volley (286-309, the fourth stream only on L).
    private static DanmakuPattern surpriseSpring() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var s1 = shot(BALL, 0.1f, CYAN, 200, MotionSegment.rotate(0, -0.58f));
        var s2 = shot(BALL, 0.15f, MAGENTA, 200, MotionSegment.rotate(0, 0.87f));
        var s3 = shot(BALL, 0.2f, CYAN, 200, MotionSegment.rotate(0, -1.17f));
        for (int v = 0; v < 40; v++) {
            emitters.add(sweep(fire(EmitShape.aim(), s1, v * 3, 0, 1).withAim(EmitAim.TARGET, 0, 0),
                    0, -10f * v, -10f * v));
            emitters.add(sweep(fire(EmitShape.aim(), s2, v * 3 + 1, 0, 1).withAim(EmitAim.TARGET, 0, 0),
                    0, 25f * v, 25f * v));
            emitters.add(sweep(fire(EmitShape.aim(), s3, v * 3 + 2, 0, 1).withAim(EmitAim.TARGET, 0, 0),
                    0, -20f * v, -20f * v));
        }
        return loop(emitters, 1320, 60, 120);
    }

    // st03bs BossCard1 (472-604): aimed stray dogs (sprite 20/6, 1.5 px/f) that split sideways
    // twice every 16f (etEx2 567-568) while a dog familiar roams (ins_91 840f, 591).
    private static DanmakuPattern strayDogWalk(int forks) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var dog = shot(BULLET, 0.15f, MAGENTA, 200,
                MotionSegment.split(ticks(16), new cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionPayload(
                        BULLET, MAGENTA, 0.85f, 0.15f, forks, 30f, 110)),
                MotionSegment.split(ticks(32), new cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionPayload(
                        BULLET, WHITE, 0.85f, 0.17f, forks, 30f, 110)));
        for (int v = 0; v < 20; v++) {
            emitters.add(fire(EmitShape.aim(), dog, 90 + v * 10, 0, 1).withAim(EmitAim.TARGET, 0, 0));
        }
        var rover = DanmakuFamiliar.of(new Vec3(0, 0.2, -3), DanmakuPattern.of())
                .withOrbit(3f, 1.2f, 0f).withDelay(ticks(90)).withDuration(ticks(840));
        return new DanmakuPattern(emitters).withFamiliars(List.of(rover))
                .withTiming(new cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming(
                        ticks(1920), ticks(90), ticks(100)));
    }

    // st03bs BossCard2 (605-854): sweeping koma lasers — 12/16 volleys of 22 px-wide lasers
    // with 64f life curving +-2 deg/f (697-717), paired with aimed +-2 deg/f bullets (at2).
    private static DanmakuPattern komaSpin(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var lasers = new ArrayList<DanmakuLaser>();
        int volleys = hard ? 16 : 12;
        for (int v = 0; v < volleys; v++) {
            int at = ticks(90) + v * ticks(30);
            float yaw = 25f * v;
            lasers.add(laser(at, 0f, yaw, 12f, 1.4f, ticks(30), ticks(64), 2f, RED));
            lasers.add(laser(at + ticks(10), 0f, yaw + 180f / volleys, 12f, 1.4f, ticks(30), ticks(64), -2f, RED));
            var shard = shot(BULLET, 0.16f, WHITE, 150, MotionSegment.rotate(0, 2f));
            emitters.add(fire(EmitShape.aim(), shard, at + ticks(5), 0, 1).withAim(EmitAim.TARGET, 0, 0));
        }
        return timeline(emitters, lasers, 1980);
    }

    // st03bs BossCard3 L-variant (BossCard3_at2 1047-1085): dense rolling laser pairs with
    // reversed sweeps.
    private static DanmakuPattern curlUpAndDie() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var lasers = new ArrayList<DanmakuLaser>();
        for (int v = 0; v < 14; v++) {
            int at = ticks(90) + v * ticks(26);
            float yaw = 30f * v;
            lasers.add(laser(at, 0f, yaw, 10f, 1.2f, ticks(26), ticks(50), 3f, RED));
            lasers.add(laser(at, 0f, yaw + 90f, 10f, 1.2f, ticks(26), ticks(50), -3f, RED));
        }
        var shard = shot(BULLET, 0.18f, RED, 140);
        for (int v = 0; v < 16; v++) {
            emitters.add(fire(EmitShape.aim(), shard, ticks(90) + v * ticks(18), 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0));
        }
        return timeline(emitters, lasers, 1980);
    }

    // st03bs BossCard3 (855-1140): the lion-dance spin — two counter-rotating streams (sprite
    // 8/4 at 1.1 px/f and 18/2 at 1.5 px/f with 16f splitters), 60/60/85/120 volleys (1119).
    private static DanmakuPattern aunBreath() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var inner = shot(BALL, 0.11f, CYAN, 210);
        var outer = shot(BULLET, 0.15f, RED, 210,
                MotionSegment.split(ticks(16), new cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionPayload(
                        BULLET, RED, 0.8f, 0.15f, 1, 25f, 110)));
        int volleys = 40;
        for (int v = 0; v < volleys; v++) {
            emitters.add(sweep(fire(EmitShape.aim(), inner, v * 6, 0, 1).withAim(EmitAim.TARGET, 0, 0),
                    0, 8f * v, 8f * v));
            emitters.add(sweep(fire(EmitShape.aim(), outer, v * 6 + 3, 0, 1).withAim(EmitAim.TARGET, 0, 0),
                    0, -8f * v, -8f * v));
        }
        return loop(emitters, 2400, 90, ticks(240));
    }
}
