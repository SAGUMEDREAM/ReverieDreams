package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06;

import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeRange;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuMath;
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

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.implemented;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.ARROWHEAD;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.BALL;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.bullet;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.sp;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.stream;

final class Stage03 {
    private Stage03() {}

    private static final int[] RAIN_COLORS = {0x6686FF, 0x62D67D, 0xB4E862, 0x65D9E8};
    private static final int[] SIDE_COLORS = {0xE76563, 0xAC63E3, 0xF0D059, 0xEA9C4C};
    private static final double[] SIDE_ACCEL_ANGLES = {180, 0, 135, 45};

    // 3 面 · 红美铃
    static List<TouhouSpell> spells() {
        return List.of(
                // 道中华符：Sub13 L334-373。两组走相固定环（N 4 路、速度 2.0、±6.5°/±7.5° 每 6 帧）+ 每 80 帧 42 路狙环（L368）。
                implemented("touhou/th06/s03_007_normal", "华符「芳华绚烂」", 1200, () -> flowerSplendour(4, 6.5f, -7.5f, 42, 27)),
                // 虹符：Sub29(d3) L803-905。多色小环序列（2/3/4 路，异色异路数，相位 +6.5°/环）；
                // 多色用随机染色近似，基础相行走动简化为慢扫。
                implemented("touhou/th06/s03_009_normal", "虹符「彩虹的风铃」", 1800, () -> rainbowWindChime(3)),
                // 彩符三连：d3 Sub31–33，分色固定向量加速，保留各段发射次序。
                implemented("touhou/th06/s03_011_normal", "彩符「彩雨」", 2160, () -> colourfulRain()),
                implemented("touhou/th06/s03_013_normal", "彩符「极彩台风」", 2160, () -> prismaticTyphoon(false)),
                // 道中华符（Hard）：Sub14 L377-423。固定环 H 5 路、速度 4.0，走相 ±8.5°/±9.5°；每 120 帧 48 路狙环（L416）。
                implemented("touhou/th06/s03_008_hard", "华符「Selaginella 9」", 1200, () -> flowerSplendour(5, 8.5f, -9.5f, 48, 40)),
                implemented("touhou/th06/s03_009_hard", "虹符「彩虹的风铃」", 1800, () -> rainbowWindChime(4)),
                implemented("touhou/th06/s03_010_hard", "幻符「华想梦葛」", 2400, Stage03::dreamVine),
                implemented("touhou/th06/s03_012_hard", "彩符「彩光乱舞」", 2160, () -> prismaticLightDance()),
                implemented("touhou/th06/s03_013_hard", "彩符「极彩台风」", 2160, () -> prismaticTyphoon(true))
        );
    }

    // Sub30 uses EX2 for 160 frames. Every sixth frame all five edges are
    // sampled at t mod 30 divided by 30. Source-space offsets turn with the cast.
    private static DanmakuPattern dreamVine() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var leaf = bullet(BALL, 2f, 105).withColor(0x76C971);
        for (int frame = 0; frame < 160; frame += 6) {
            int turn = frame / 30;
            float edgeProgress = (frame % 30) / 30f;
            float progress = frame / 160f;
            for (int edge = 0; edge < 5; edge++) {
                double angle = (turn + edge) * 4 * Math.PI / 5;
                Vec3 vertex = starPoint(angle);
                Vec3 next = starPoint(angle + 4 * Math.PI / 5);
                Vec3 offset = vertex.lerp(next, edgeProgress).add(0, 0.65, progress * 0.85);
                float yaw = (60f - 30f * edge) * (progress + 0.5f);
                emitters.add(stream(EmitShape.fan(2, 12f), leaf, SpellTiming.framesToTicks(frame), 0,
                        1, EmitAim.TARGET, 0, 0, KeyframeRange.empty(yaw, yaw))
                        .withOffset(offset).withScatter(new DanmakuScatter(0, 0, 0, 0.5f)));
            }
        }
        // The first sequence starts immediately; Sub30 restarts the sampler
        // every 180 frames after its first reposition.
        return new DanmakuPattern(emitters).withTiming(new SpellTiming(800, 0, 60));
    }

    private static Vec3 starPoint(double angle) {
        return new Vec3(Math.cos(angle) * 3.7, 0, Math.sin(angle) * 3.7);
    }

    private static DanmakuBulletSpec bulletRandomColor(Identifier type, float pxPerFrame, int lifetime) {
        return new DanmakuBulletSpec(type, DanmakuBulletSpec.DEFAULT_COLOR, true,
                sp(pxPerFrame), 1f, 0f, lifetime, DanmakuMotion.STRAIGHT);
    }

    // ecldata3.ecl.raw L901–950: four independently randomized shots every four frames for 20
    // samples, 20-frame gap, then 20 samples every three frames with lateral bias.
    private static DanmakuPattern colourfulRain() {
        var emitters = new ArrayList<DanmakuEmitter>();
        int intro = 120;
        for (int wave = 0; wave < 20; wave++) {
            for (int color = 0; color < 4; color++) {
                emitters.add(prismaticShot(intro + 4 * wave, 1f, 0.3f, -180, 180,
                        90, 0.027f, RAIN_COLORS[color], 1));
                emitters.add(prismaticShot(intro + 100 + 3 * wave, 1f, 0.3f, -180, 180,
                        SIDE_ACCEL_ANGLES[color], 0.024f, SIDE_COLORS[color], 1));
            }
        }
        return new DanmakuPattern(emitters).withTiming(prismaticTiming(intro, 160));
    }

    // ecldata3.ecl.raw L951–1019: in Hard each frame
    // samples four fixed angles 90° apart; keep every third wave and the 10°/frame
    // phase advance. After the gap, keep the second section's 30° reverse steps
    // and its bounded angle/speed scatter. Use fixed opening phases for MC.
    private static DanmakuPattern prismaticLightDance() {
        var emitters = new ArrayList<DanmakuEmitter>();
        int intro = 120;
        // Three 100-frame cycles avoid accumulating ceil(100/3) rounding error.
        for (int pass = 0; pass < 3; pass++) {
            int start = intro + 100 * pass;
            double phase = -120 + pass * 120;
            for (int wave = 0; wave < 40; wave += 3) {
                for (int color = 0; color < 4; color++) {
                    double angle = phase + 10 * wave + 90 * color;
                    emitters.add(prismaticShot(start + wave, 1.5f, 1.5f, angle, angle,
                            90, 0.025f, RAIN_COLORS[color], 1));
                }
            }
            for (int wave = 0; wave < 20; wave++) {
                for (int color = 0; color < 4; color++) {
                    // The ECL shooter reduces angle1 after the colour offset, before random sampling.
                    double angle = Math.IEEEremainder(phase + 400 - 30 * wave + 90 * color, 360);
                    emitters.add(prismaticShot(start + 60 + 2 * wave, 1.2f, 0.3f, angle, 5.625,
                            SIDE_ACCEL_ANGLES[color], 0.024f, SIDE_COLORS[color], 1));
                }
            }
        }
        return new DanmakuPattern(emitters).withTiming(prismaticTiming(intro, 300));
    }

    // ecldata3.ecl.raw L1020–1130: EX_ACCELERATION rotates two sets of axes
    // by +6°/-6° every six frames. Convert substeps before tick rounding,
    // and replay each of the 60 angular slots every 360 frames.
    private static DanmakuPattern prismaticTyphoon(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        int intro = 180; // 60-frame stage transition + 120-frame opening motion.
        int step = 6;
        // At the end of the first wave with a timer strictly above 1200,
        // Sub33 increments the count, so the following wave uses it.
        int startDenser = intro + ((1200 - intro) / step + 2) * step;
        // The script uses a second angular accumulator and adds a shot as the
        // enemy timer crosses 1200. Keep the opening phase explicit in MC.
        for (int frame = intro; frame < intro + 360; frame += step) {
            int wave = (frame - intro) / step;
            double first = 90 + 6 * wave;
            double second = -90 - 6 * wave;
            int count = 2;
            addTyphoonShot(emitters, frame, first, 0.016f, RAIN_COLORS[0], count, startDenser);
            addTyphoonShot(emitters, frame + 1, first + 90, 0.018f, RAIN_COLORS[1], 1);
            if (hard) {
                addTyphoonShot(emitters, frame + 1, first + 180, 0.016f, RAIN_COLORS[2], 1);
            }
            addTyphoonShot(emitters, frame + 2, first - 90, 0.018f, RAIN_COLORS[3], 1);
            addTyphoonShot(emitters, frame + 3, second, 0.016f, SIDE_COLORS[0], count, startDenser + 3);
            if (hard) {
                addTyphoonShot(emitters, frame + 3, second + 90, 0.018f, SIDE_COLORS[0], 1);
            }
            addTyphoonShot(emitters, frame + 4, second + 180, 0.018f, SIDE_COLORS[0], 1);
            addTyphoonShot(emitters, frame + 5, second - 90, 0.016f, SIDE_COLORS[0], 1);
        }
        return new DanmakuPattern(emitters).withTiming(new SpellTiming(720, 0, 0));
    }

    private static SpellTiming prismaticTiming(int introFrames, int loopFrames) {
        return new SpellTiming(SpellTiming.framesToTicks(2160), SpellTiming.framesToTicks(introFrames),
                SpellTiming.framesToTicks(loopFrames));
    }

    private static void addTyphoonShot(List<DanmakuEmitter> emitters, int frame, double angle, float acceleration,
                                       int color, int count) {
        addTyphoonShot(emitters, frame, angle, acceleration, color, count, -1);
    }

    private static void addTyphoonShot(List<DanmakuEmitter> emitters, int frame, double angle, float acceleration,
                                       int color, int count, int startDenser) {
        int tick = SpellTiming.framesToTicks(frame);
        int period = SpellTiming.framesToTicks(360);
        int deadline = SpellTiming.framesToTicks(2160);
        var emitter = prismaticShot(frame, 1f, 0.3f, -180, 180, angle, acceleration, color, count);
        emitters.add(emitter.withTiming(tick, deadline - tick - 1, period));
        if (startDenser >= 0) {
            // Sub33 updates the multiplicity after the wave, for subsequent waves.
            int lateWave = frame + Math.max(0, Math.ceilDiv(startDenser - frame, 360)) * 360;
            int lateTick = SpellTiming.framesToTicks(lateWave);
            if (lateTick < deadline) {
                var extra = prismaticShot(lateWave, 1f, 0.3f, -180, 180, angle, acceleration, color, 1);
                emitters.add(extra.withTiming(lateTick, deadline - lateTick - 1, period));
            }
        }
    }

    private static DanmakuEmitter prismaticShot(int frame, float speed, float minSpeed, double angleA, double angleB,
                                               double accelerationAngle, float acceleration, int color, int count) {
        // ECL +x is angle 0, +y is 90°. Map +y to world +Z with the same scale
        // on both screen axes. Three source frames pass per MC tick.
        double yaw = (angleA + angleB) / 2 - 90;
        float halfRange = (float) (Math.abs(angleA - angleB) / 2);
        var vector = DanmakuMath.direction(0, (float) (accelerationAngle - 90)).scale(sp(acceleration) * 3);
        var motion = DanmakuMotion.of(Vec3.ZERO, 0, 0, List.of(MotionSegment.accelerateVector(0, vector, -1)));
        return DanmakuEmitter.of(EmitShape.ring(count, 0, false), bullet(ARROWHEAD, speed, 65).withColor(color).withMotion(motion))
                .withAim(EmitAim.FIXED, 0, (float) yaw)
                .withScatter(new DanmakuScatter(0, 0, halfRange, minSpeed / speed))
                .withTiming(SpellTiming.framesToTicks(frame), 0, 1);
    }

    /**
     * 華符「芳華絢爛」／「セラギネラ９」：两组走相固定环（每 2 tick 一波，相位 ±角速行走）+ 周期狙环。
     * ECL Sub13 L334-373（N 4 路/2.0 px/帧/42 路）与 Sub14 L377-423（H 5 路/4.0 px/帧/48 路）。
     */
    private static DanmakuPattern flowerSplendour(int count, float walkA, float walkB, int aimCount, int aimInterval) {
        int duration = 160;
        float base = count == 4 ? 2f : 4f;
        int volleys = duration / 2;
        return DanmakuPattern.of(
                stream(EmitShape.ring(count, 0f, true), bullet(BALL, base, 45),
                        0, duration, 2, EmitAim.FIXED, 0f, 0f,
                        KeyframeRange.empty(0f, walkA * volleys)),
                stream(EmitShape.ring(count, 0f, true), bullet(BALL, base, 45),
                        0, duration, 2, EmitAim.FIXED, 0f, 0f,
                        KeyframeRange.empty(0f, walkB * volleys)),
                stream(EmitShape.ring(aimCount, 0f, true), bullet(BALL, 1.2f, 80),
                        0, duration, aimInterval, EmitAim.TARGET, 0f, 0f, null)
        );
    }

    /**
     * 虹符「彩虹の風鈴」：七联异色小环序列（2/3/4 路，相位逐环 +6.5°），每 5 tick 一序列；
     * 多色用随机染色近似，基础相位行走简化为整体慢扫。ECL Sub29(d3) L803-905。
     */
    private static DanmakuPattern rainbowWindChime(int midCount) {
        int[] counts = {2, midCount, midCount + 1, 2, midCount + 1, midCount, 2};
        List<DanmakuEmitter> emitters = new java.util.ArrayList<>();
        for (int i = 0; i < counts.length; i++) {
            emitters.add(stream(EmitShape.ring(counts[i], 0f, true),
                    bulletRandomColor(BALL, 2.6f, 40), 0, 150, 5,
                    EmitAim.FIXED, 0f, 0f, KeyframeRange.empty(i * 6.5f, i * 6.5f + 75f)));
        }
        return DanmakuPattern.of(emitters.toArray(new DanmakuEmitter[0]));
    }
}
