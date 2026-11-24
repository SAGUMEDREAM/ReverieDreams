package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th15;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuLaser;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuMath;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th15.Th15PatternSupport.*;

/** TH15 st05bs.ecl: Clownpiece's Normal and Hard spell-practice entries. */
final class Stage05 {
    private static final String CLOWNPIECE = "clownpiece";
    private static final Identifier STAR = ReverieDreams.id("star");
    private static final Identifier BULLET = ReverieDreams.id("bullet");
    private static final int RED = 0xD03A50;
    private static final int WHITE = 0xEDEDF2;
    private static final int BLUE = 0x3949AB;
    private static final int MOON_GOLD = 0xE8D48B;

    private Stage05() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(5, 58, false, CLOWNPIECE, "狱符「地狱月食」", 1800, () -> hellEclipse(false)),
                card(5, 58, true, CLOWNPIECE, "狱符「地狱之蚀」", 1800, () -> hellEclipse(true)),
                card(5, 62, false, CLOWNPIECE, "狱符「闪光与条纹」", 2400, () -> flashAndStripe(false)),
                card(5, 62, true, CLOWNPIECE, "狱符「星与条纹」", 2400, () -> flashAndStripe(true)),
                card(5, 66, false, CLOWNPIECE, "狱炎「擦弹狱火」", 3600, () -> grazeInferno(false)),
                card(5, 66, true, CLOWNPIECE, "狱炎「擦弹狱火」", 3600, () -> grazeInferno(true)),
                card(5, 70, false, CLOWNPIECE, "地狱「条纹深渊」", 3000, () -> stripedAbyss(false)),
                card(5, 70, true, CLOWNPIECE, "地狱「条纹深渊」", 3000, () -> stripedAbyss(true)),
                card(5, 74, false, CLOWNPIECE, "「伪阿波罗」", 2220, () -> fakeApollo(false)),
                card(5, 74, true, CLOWNPIECE, "「阿波罗捏造说」", 2220, () -> fakeApollo(true)));
    }

    // st05bs.ecl BossCard1_at (691-739): a source orbits outward (96px cap) while
    // spraying 8-way (N) / 10-way (H) stars whose heading drops 0.8° per shot and
    // whose speed jumps 4→7 after 30f. N rains from a marching edge line
    // (BossCard1_at2, 741-787); H sprays 32 random directions every 120f
    // (BossCard1_at2h, 789-816). The moon deco fires 5-way rings every 60f.
    private static DanmakuPattern hellEclipse(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var star = shot(STAR, 0.13f, RED, 100, MotionSegment.setSpeed(10, 0.23f));
        for (int i = 0; i < 150; i++) {
            float yaw = 90 - 0.8f * i;
            Vec3 offset = DanmakuMath.direction(0, -210 - 2f * i).scale(Math.min(i * 0.7, 3.2)).add(0, 0.25, 0);
            var cue = sweep(fire(EmitShape.fan(hard ? 10 : 8, 10f), star, 63 + i * 6, 0, 1), 0, yaw, yaw);
            emitters.add(cue.withOffset(offset));
        }
        var ring = shot(STAR, 0.2f, MOON_GOLD, 90);
        emitters.add(fire(EmitShape.fan(5, 50f), ring, 63, 1500, 60).withOffset(new Vec3(0, 0.3, -4.7)));
        var rain = shot(BULLET, 0.15f, WHITE, 110);
        if (hard) {
            var spray = shot(BULLET, 0.1f, BLUE, 130, MotionSegment.setSpeed(30, 0.2f));
            emitters.add(fire(EmitShape.ring(16, 0.6f, true), spray, 102, 1650, 12));
        } else {
            for (int i = 0; i * 12 + 102 < 1770; i++) {
                float x = pingPong(i, 6.4, 0.8);
                for (int side = 0; side < 2; side++) {
                    var cue = sweep(fire(EmitShape.aim(), rain, 102 + i * 12, 0, 1), 0, 0, 0);
                    emitters.add(cue.withOffset(new Vec3(side == 0 ? x : -x, 0.3, -1.07)));
                }
            }
        }
        return timeline(emitters, 1800);
    }

    // st05bs.ecl BossCard2_at (903-926): thirteen 1600px horizontal lasers march
    // up the field 64px apart every 10f; BossCard2_at2 (928-968) adds mirrored
    // zigzag kunai streams (2 rounds N / 3 H). Hard rains from the edges
    // continuously (BossCard2_at3, 970-1016).
    private static DanmakuPattern flashAndStripe(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var lasers = new ArrayList<DanmakuLaser>();
        for (int cycle = 0; 21 + cycle * 43 + 40 < 790; cycle++) {
            for (int i = 0; i < 13; i++) {
                lasers.add(laser(21 + cycle * 43 + i * 3, 0, 90, 20f, 0.53f, 4, 12, 0f, RED)
                        .withOffset(new Vec3(-6.4, 0.2, 7.0 - i * 1.07)));
            }
            int rounds = hard ? 3 : 2;
            for (int round = 0; round < rounds; round++) {
                for (int side = 0; side < 2; side++) {
                    for (int i = 0; i < 20; i++) {
                        float yaw = round * 23 + (side == 0 ? 1 : -1) * (64.5f - i * 7f);
                        var kunai = shot(BULLET, 0.15f, BLUE, 90, MotionSegment.accelerate(10, 0.02f));
                        emitters.add(sweep(fire(EmitShape.fan(1, 0f), kunai,
                                93 + cycle * 129 + round * 60 + i * 3, 0, 1), 0, yaw, yaw));
                    }
                }
            }
        }
        if (hard) {
            var rain = shot(BULLET, 0.15f, WHITE, 110);
            for (int i = 0; i * 18 + 63 < 2340; i++) {
                float x = pingPong(i, 6.4, 0.8);
                for (int side = 0; side < 2; side++) {
                    var cue = sweep(fire(EmitShape.aim(), rain, 63 + i * 18, 0, 1), 0, 0, 0);
                    emitters.add(cue.withOffset(new Vec3(side == 0 ? x : -x, 0.3, -1.07)));
                }
            }
        }
        return timeline(emitters, lasers, 2400);
    }

    // st05bs.ecl BossCard3_at/_at2 (1126-1196): huge star walls — 144 (N/H) ways
    // on the main volley and 64 (N) / 32 (H) on the follow-up, both widening at
    // 2° spacing from a random heading and accelerating 1.5→2.0 after 120f.
    private static DanmakuPattern grazeInferno(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        for (int volley = 0; 63 + volley * 129 < 3540; volley++) {
            float base = volley * 137f;
            var wall = shot(STAR, 0.15f, RED, 130, MotionSegment.accelerate(40, 0.0125f));
            var wallCue = sweep(fire(EmitShape.fan(48, 200f), wall, 63 + volley * 129, 0, 1), 0, base, base);
            emitters.add(wallCue);
            var follow = shot(STAR, 0.15f, BLUE, 130, MotionSegment.accelerate(40, 0.0125f));
            var followCue = sweep(fire(EmitShape.fan(hard ? 10 : 21, hard ? 70f : 120f), follow,
                    93 + volley * 129, 0, 1), 0, -base, -base);
            emitters.add(followCue);
        }
        return timeline(emitters, 3600);
    }

    // st05bs.ecl BossCard4_at/_at2 (1266-1355): criss-crossing 600px (N) / 650px
    // (H) horizontal lasers sweep down from both edges 64px apart every 10f and
    // creep across the field at 6px/f — the mod renders the sweep as successive
    // stripes. Volleys repeat every ~150f (BossCard4 1238-1263).
    private static DanmakuPattern stripedAbyss(boolean hard) {
        var lasers = new ArrayList<DanmakuLaser>();
        int length = hard ? 22 : 20;
        for (int cycle = 0; 21 + cycle * 50 + 40 < 990; cycle++) {
            for (int i = 0; i < 10; i++) {
                float down = 6.0f - i * 1.07f;
                float up = -6.0f + i * 1.07f;
                lasers.add(laser(21 + cycle * 50 + i * 2, 0, 90, length, 0.53f, 2, 14, 0f, RED)
                        .withOffset(new Vec3(-6.4, 0.2, down)));
                lasers.add(laser(31 + cycle * 50 + i * 2, 0, 270, length, 0.53f, 2, 14, 0f, BLUE)
                        .withOffset(new Vec3(6.4, 0.2, up)));
            }
        }
        return timeline(List.of(), lasers, 3000);
    }

    // st05bs.ecl BossCard5_Moon_at2 (1469-1530): three moons orbit at 140px and
    // each fires 23-way (N/H) 10°-spaced arcs at the player every 25-30f with
    // speed 0.8 (N) / 1.2 (H). The boss repeats the Hell Eclipse spiral and edge
    // rain (BossCard5_at/_at2, 1532-1619, identical to BossCard1_at/_at2).
    private static DanmakuPattern fakeApollo(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        float[] moonAngles = {-90f, 30f, 150f};
        for (int moon = 0; moon < 3; moon++) {
            Vec3 offset = DanmakuMath.direction(0, moonAngles[moon]).scale(4.7).add(0, 0.3, 0);
            var arc = shot(STAR, hard ? 0.12f : 0.08f, MOON_GOLD, 130);
            emitters.add(fire(EmitShape.fan(16, 110f), arc, 72 + moon * 12, 2070, hard ? 24 : 30)
                    .withOffset(offset));
        }
        var star = shot(STAR, 0.13f, RED, 100, MotionSegment.setSpeed(10, 0.23f));
        for (int i = 0; i < 110; i++) {
            float yaw = 90 - 0.8f * i;
            Vec3 offset = DanmakuMath.direction(0, -210 - 2f * i).scale(Math.min(i * 0.7, 3.2)).add(0, 0.25, 0);
            var cue = sweep(fire(EmitShape.fan(8, 10f), star, 63 + i * 6, 0, 1), 0, yaw, yaw);
            emitters.add(cue.withOffset(offset));
        }
        var rain = shot(BULLET, 0.15f, WHITE, 110);
        if (hard) {
            var spray = shot(BULLET, 0.1f, BLUE, 130, MotionSegment.setSpeed(30, 0.2f));
            emitters.add(fire(EmitShape.ring(16, 0.6f, true), spray, 102, 2070, 12));
        } else {
            for (int i = 0; i * 12 + 102 < 2160; i++) {
                float x = pingPong(i, 6.4, 0.8);
                for (int side = 0; side < 2; side++) {
                    var cue = sweep(fire(EmitShape.aim(), rain, 102 + i * 12, 0, 1), 0, 0, 0);
                    emitters.add(cue.withOffset(new Vec3(side == 0 ? x : -x, 0.3, -1.07)));
                }
            }
        }
        return timeline(emitters, 2220);
    }

    private static float pingPong(int i, double limit, double step) {
        double span = limit * 2;
        double cycle = span / step;
        double position = (i % (int) cycle) * step;
        if (i % (int) (cycle * 2) >= cycle) {
            position = span - position;
        }
        return (float) (position - limit);
    }
}
