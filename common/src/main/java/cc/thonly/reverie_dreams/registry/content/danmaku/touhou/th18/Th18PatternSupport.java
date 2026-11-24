package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th18;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.DanmakuMotion;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import net.minecraft.world.phys.Vec3;

import java.util.List;

final class Th18PatternSupport {
    static final Vec3 UP = new Vec3(0, 1, 0);

    private Th18PatternSupport() {}

    static DanmakuBulletSpec shot(String type, float speed, int color, int lifetime, MotionSegment... motion) {
        return shot(type, speed, color, lifetime, 0f, motion);
    }

    static DanmakuBulletSpec shot(String type, float speed, int color, int lifetime, float acceleration, MotionSegment... motion) {
        return DanmakuBulletSpec.of(ReverieDreams.id(type), speed, DanmakuMotion.of(Vec3.ZERO, acceleration, 0, List.of(motion)))
                .withColor(color).withLifetime(lifetime);
    }

    static DanmakuEmitter fire(EmitShape shape, DanmakuBulletSpec shot, int frame, float yaw, float pitch) {
        return DanmakuEmitter.of(shape, shot).withTiming(ticks(frame), 0, 1).withAim(EmitAim.SOURCE_FACING, pitch, yaw);
    }

    static DanmakuPattern loop(List<DanmakuEmitter> emitters, int deadline, int intro, int interval) {
        return new DanmakuPattern(emitters).withTiming(new SpellTiming(ticks(deadline), ticks(intro), ticks(interval)));
    }

    static int ticks(int frames) {
        return SpellTiming.framesToTicks(frames);
    }
}
