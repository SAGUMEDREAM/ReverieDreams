package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeRange;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.DanmakuMotion;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/** Reusable pattern primitives for TH07 only. */
final class Th07PatternSupport {
    private Th07PatternSupport() {}

    static final Identifier ARROWHEAD = ReverieDreams.id("arrowhead");
    static final Identifier BALL = ReverieDreams.id("ball");
    static final Identifier BUBBLE = ReverieDreams.id("bubble");
    static final Identifier RICE = ReverieDreams.id("rice");
    static final int RED = 0xFF4050;
    static final int BLUE = 0x5468FF;
    static final int CYAN = 0x54E0FF;
    static final int DARK_BLUE = 0x4050C8;
    static final int PURPLE = 0xB060F0;
    static final int PINK = 0xFF70C8;
    static final int WHITE = 0xEDEDF5;
    static final int YELLOW = 0xF2D24A;
    static final int GREEN = 0x58D06A;
    static final int MAGENTA = 0xF060C8;

    static DanmakuBulletSpec shot(Identifier type, float speed, int color) {
        return DanmakuBulletSpec.of(type, speed).withColor(color);
    }

    static DanmakuBulletSpec shot(Identifier type, float speed, int color, DanmakuMotion motion) {
        return DanmakuBulletSpec.of(type, speed, motion).withColor(color);
    }

    static DanmakuEmitter fire(EmitShape shape, DanmakuBulletSpec bullet, int delay, int duration, int interval) {
        return DanmakuEmitter.of(shape, bullet).withTiming(delay, duration, interval);
    }

    static DanmakuEmitter fire(EmitShape shape, DanmakuBulletSpec bullet, int delay, int duration, int interval,
                                       EmitAim aim, float pitch, float yaw) {
        return DanmakuEmitter.of(shape, bullet).withTiming(delay, duration, interval).withAim(aim, pitch, yaw);
    }

    static DanmakuEmitter fire(EmitShape shape, DanmakuBulletSpec bullet, int delay, int duration, int interval,
                                       EmitAim aim, float pitch, float yaw, Vec3 offset, KeyframeRange rotation) {
        return DanmakuEmitter.of(shape, bullet).withTiming(delay, duration, interval)
                .withAim(aim, pitch, yaw).withOffset(offset).withRotation(rotation);
    }
}
