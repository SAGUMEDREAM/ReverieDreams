package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th16;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeRange;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuLaser;
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

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/** Shared helpers for the TH16 (Hidden Star in Four Seasons) spell ports. */
final class Th16PatternSupport {
    static final Identifier PELLET = ReverieDreams.id("pellet");
    static final Identifier BALL = ReverieDreams.id("ball");
    static final Identifier BULLET = ReverieDreams.id("bullet");
    static final Identifier STAR = ReverieDreams.id("star");
    static final Identifier RICE = ReverieDreams.id("rice");
    static final Identifier KUNAI = ReverieDreams.id("kunai");
    static final Identifier AMULET = ReverieDreams.id("amulet");
    static final Identifier BUBBLE = ReverieDreams.id("bubble");
    static final Identifier BUTTERFLY = ReverieDreams.id("butterfly");
    static final Identifier ARROWHEAD = ReverieDreams.id("arrowhead");
    static final Identifier FIREBALL = ReverieDreams.id("fireball");
    static final Vec3 UP = new Vec3(0, 1, 0);

    private Th16PatternSupport() {}

    static TouhouSpell card(int stage, int number, boolean hard, String owner, String chineseName,
                            int frames, Supplier<DanmakuPattern> pattern) {
        String path = String.format(Locale.ROOT, "touhou/th16/s%02d_%03d_%s",
                stage, number, hard ? "hard" : "normal");
        return TouhouSpell.implemented(path, chineseName, frames, pattern)
                .withCharacter(owner.startsWith("item.") ? owner : "entity.reverie_dreams." + owner);
    }

    static TouhouSpell extraCard(int stage, int number, String owner, String chineseName,
                                 int frames, Supplier<DanmakuPattern> pattern) {
        String path = String.format(Locale.ROOT, "touhou/th16/s%02d_%03d_extra", stage, number);
        return TouhouSpell.implemented(path, chineseName, frames, pattern)
                .withCharacter(owner.startsWith("item.") ? owner : "entity.reverie_dreams." + owner);
    }

    static int ticks(int frames) {
        return SpellTiming.framesToTicks(frames);
    }

    static DanmakuPattern timeline(List<DanmakuEmitter> emitters, List<DanmakuLaser> lasers, int frames) {
        return new DanmakuPattern(emitters).withLasers(lasers)
                .withTiming(new SpellTiming(ticks(frames), 0, 0));
    }

    static DanmakuPattern loop(List<DanmakuEmitter> emitters, int frames, int intro, int interval) {
        return new DanmakuPattern(emitters).withTiming(new SpellTiming(ticks(frames), ticks(intro), ticks(interval)));
    }

    static DanmakuBulletSpec shot(Identifier type, float speed, int color, int lifetime, MotionSegment... motion) {
        return DanmakuBulletSpec.of(type, speed, DanmakuMotion.of(Vec3.ZERO, 0f, 0f, List.of(motion)))
                .withColor(color).withLifetime(lifetime);
    }

    static DanmakuEmitter fire(EmitShape shape, DanmakuBulletSpec bullet, int startFrame, int windowFrames, int stepFrames) {
        return DanmakuEmitter.of(shape, bullet).withTiming(ticks(startFrame), ticks(windowFrames), Math.max(1, ticks(stepFrames)))
                .withAim(EmitAim.SOURCE_FACING, 0, 0);
    }

    static DanmakuEmitter sweep(DanmakuEmitter emitter, float pitch, float fromYaw, float toYaw) {
        return emitter.withRanges(KeyframeRange.empty(pitch, pitch), KeyframeRange.empty(fromYaw, toYaw));
    }

    static DanmakuLaser laser(int startTick, float pitch, float yaw, float length, float width,
                              int warnTicks, int activeTicks, float sweepPerTick, int color) {
        return new DanmakuLaser(startTick, pitch, yaw, length, width, warnTicks, activeTicks, sweepPerTick, color);
    }
}
