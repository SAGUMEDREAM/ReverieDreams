package cc.thonly.reverie_dreams.registry.content.danmaku.touhou;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.DanmakuMotion;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionPayload;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Sprite families used by TH15/TH17. Color indices are local to each row of bullet.anm. */
public enum EclBulletSprite {
    BALL("ball", 0.65f, Palette.SIXTEEN),                 // 4, 5, 7
    RICE("rice", 0.68f, Palette.SIXTEEN),                 // 8
    KUNAI("kunai", 0.68f, Palette.SIXTEEN),               // 9
    NEEDLE("needle", 0.58f, Palette.SIXTEEN),             // 10
    SCALE("arrowhead", 0.65f, Palette.SIXTEEN),           // 12
    STAR("star", 0.58f, Palette.SIXTEEN),                 // 16
    ORB("medium_orb", 0.95f, Palette.EIGHT),              // 18
    HIGHLIGHT_ORB("highlight_orb", 0.95f, Palette.EIGHT), // 19
    OVAL("oval", 0.76f, Palette.EIGHT),                  // 20
    BLADE("throwing_knife", 0.85f, Palette.EIGHT),         // 21
    BUTTERFLY("butterfly", 0.85f, Palette.EIGHT),           // 22
    LARGE_STAR("star", 1.1f, Palette.EIGHT),               // 23
    RED_FLAME("fireball", 0.78f, 0xF54C48),               // 25
    PURPLE_FLAME("fireball", 0.78f, 0xCB53F8),            // 26
    BLUE_FLAME("fireball", 0.78f, 0x726BFF),              // 27
    HEART("heart", 0.76f, Palette.EIGHT),                 // 29
    ARROW("arrow", 0.85f, Palette.EIGHT),                 // 31
    GLOW_ORB("light_orb", 0.88f, Palette.EIGHT),          // 33
    DROP("water_drop", 0.77f, Palette.SIXTEEN),           // 34
    LASER_FRAGMENT("short_laser", 1f, Palette.SIXTEEN),   // laser1.anm palette
    MOON("lunatic_moon", 2.55f, -1);                     // TH15 Extra auxiliary moon, fixed original colors

    private final Identifier type;
    private final float scale;
    private final int[] colors;

    EclBulletSprite(String type, float scale, Palette palette) {
        this(type, scale, palette.colors);
    }

    EclBulletSprite(String type, float scale, int... colors) {
        this.type = ReverieDreams.id(type);
        this.scale = scale;
        this.colors = colors;
    }

    /** RGB tints matched to the atlas hue; the Mod supplies its own highlight and shading. */
    public int color(int index) {
        return colors.length == 1 ? colors[0] : colors[index];
    }

    public DanmakuBulletSpec shot(int index, float pixelsPerFrame, int lifetimeFrames, MotionSegment... segments) {
        var motion = DanmakuMotion.of(Vec3.ZERO, 0f, 0f, List.of(segments));
        return new DanmakuBulletSpec(type, color(index), false, speed(pixelsPerFrame), scale, 0f,
                SpellTiming.framesToTicks(lifetimeFrames), motion);
    }

    public MotionSegment transform(int frame, int index) {
        return MotionSegment.transform(SpellTiming.framesToTicks(frame), MotionPayload.transform(type, color(index), scale));
    }

    public MotionSegment split(int frame, int index, int count, float spread, float pixelsPerFrame, int lifetimeFrames) {
        return MotionSegment.split(SpellTiming.framesToTicks(frame),
                splitPayload(index, count, spread, pixelsPerFrame, lifetimeFrames));
    }

    public MotionPayload splitPayload(int index, int count, float spread, float pixelsPerFrame, int lifetimeFrames) {
        return MotionPayload.split(type, color(index), scale, count, spread, speed(pixelsPerFrame),
                SpellTiming.framesToTicks(lifetimeFrames));
    }

    private static float speed(float pixelsPerFrame) {
        return pixelsPerFrame / 10f; // 30 pixels/block, 3 original frames/server tick
    }

    private enum Palette {
        SIXTEEN(0x949AAB, 0x9E2635, 0xFF4747, 0x8E36AA, 0xFF6CE8, 0x4143C6, 0x597CFF, 0x197DC3,
                0x47E9FF, 0x138356, 0x46E065, 0x648E2D, 0xB7EB4A, 0xC3A22B, 0xFFCB50, 0xFFFFFF),
        EIGHT(0xA5A7B6, 0xFF5356, 0xEB5DEB, 0x645CFF, 0x40D9F2, 0x50D956, 0xF6D743, 0xFFFFFF);

        private final int[] colors;

        Palette(int... colors) {
            this.colors = colors;
        }
    }
}
