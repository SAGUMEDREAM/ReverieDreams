package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuMath;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuScatter;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.DanmakuMotion;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.ARROWHEAD;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.bullet;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.sp;

final class Stage04Wood {
    private static final int INTRO_FRAMES = 120;
    private static final int DEADLINE_FRAMES = 2100;
    private static final int GREEN = 0x60D16D;
    private static final int LIME = 0xAFE569;
    private static final float FIELD_WIDTH = 18;

    private Stage04Wood() {}

    // ecldata4.ecl.raw Sub47/48: main spray every sixth cycle and two edge
    // shots (0/2 frames) in each six-frame cycle. The second edge has two
    // alternating source ranges; Upper holds its edge acceleration for 60 frames.
    static DanmakuPattern horn(boolean upper, boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        double accelerationAngle = upper ? 45 : 135;
        // Sub48's H-only 20 shots are additional to the unmasked 15.
        int mainCount = upper ? (hard ? 18 : 8) : 5;
        int mainColor = upper ? 0xE4CD5B : LIME;
        emitters.add(spray(0, mainCount, 2f, 0.3f, -180, 180, accelerationAngle, 0.012f, -1,
                mainColor, Vec3.ZERO, 0, 0));
        double start = upper ? 29 : 119;
        double end = upper ? 61 : 151;
        int edgeAccelerationFrames = upper ? 60 : -1;
        for (int cycle = 0; cycle < 6; cycle++) {
            int frame = cycle * 6;
            emitters.add(spray(frame, 1, 1.2f, 0.3f, start, end, accelerationAngle, 0.012f,
                    edgeAccelerationFrames, GREEN, new Vec3(0, 0, -2.5), FIELD_WIDTH, 0));
            double side = upper ? -9.5 : 9.5;
            boolean upperHalf = cycle % 2 == 0;
            emitters.add(spray(frame + 2, 1, 1.2f, 0.3f, start, end, accelerationAngle, 0.012f,
                    edgeAccelerationFrames, LIME, new Vec3(side, 0, upperHalf ? 2.25 : 6.75), 0, 4.5f));
        }
        return new DanmakuPattern(emitters).withTiming(timing(36));
    }

    // Sub49 (Hard): main spray every eighth five-frame cycle. Side sources
    // alternate, with mirrored travel and acceleration angles.
    static DanmakuPattern greenStorm() {
        var emitters = new ArrayList<DanmakuEmitter>();
        // The 120-frame supercycle preserves the five-frame cadence through 20 TPS rounding.
        for (int cycle = 0; cycle < 24; cycle++) {
            int frame = cycle * 5;
            if (cycle % 8 == 0) {
                emitters.add(spray(frame, 15, 2f, 0.3f, -180, 180, 90, 0.008f, -1,
                        LIME, Vec3.ZERO, 0, 0));
            }
            boolean right = cycle % 2 == 0;
            emitters.add(spray(frame, 1, 1.4f, 0.3f, right ? 119 : 29, right ? 151 : 61,
                    right ? 135 : 45, 0.008f, -1, GREEN, new Vec3(right ? 9.5 : -9.5, 0, 4.5), 0, 9));
        }
        return new DanmakuPattern(emitters).withTiming(timing(120));
    }

    private static SpellTiming timing(int loopFrames) {
        return new SpellTiming(SpellTiming.framesToTicks(DEADLINE_FRAMES), SpellTiming.framesToTicks(INTRO_FRAMES),
                SpellTiming.framesToTicks(loopFrames));
    }

    private static DanmakuEmitter spray(int frame, int count, float speed, float minimum,
                                       double angleA, double angleB, double accelerationAngle,
                                       float acceleration, int accelerationFrames, int color,
                                       Vec3 offset, float width, float depth) {
        // Mod playfield: local XZ, mirror screen edge offsets around the cast origin.
        var vector = DanmakuMath.direction(0, (float) accelerationAngle - 90).scale(sp(acceleration) * 3);
        int accelerationTicks = accelerationFrames < 0 ? -1 : SpellTiming.framesToTicks(accelerationFrames);
        var motion = DanmakuMotion.of(Vec3.ZERO, 0, 0, List.of(
                MotionSegment.accelerateVector(0, vector, accelerationTicks)));
        return DanmakuEmitter.of(EmitShape.ring(count, 0, false), bullet(ARROWHEAD, speed, 60).withColor(color).withMotion(motion))
                .withAim(EmitAim.FIXED, 0, (float) ((angleA + angleB) / 2 - 90))
                .withScatter(new DanmakuScatter(width, depth, (float) (Math.abs(angleA - angleB) / 2), minimum / speed))
                .withOffset(offset)
                .withTiming(SpellTiming.framesToTicks(INTRO_FRAMES + frame), 0, 1);
    }
}
