package cc.thonly.reverie_dreams.registry.content.danmaku.soku;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuStrike;
import cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming;
import net.minecraft.world.phys.Vec3;

import java.util.List;

final class ReimuMelee {
    private ReimuMelee() {
    }

    // Final PAT action 300 replaces an earlier declaration. Both versions use these two boxes.
    // Action 320 uses the same attack, one frame earlier and one frame shorter overall.
    // Evidence: docs/research/soku/mechanisms.md, final PAT offsets 0x28e9b / 0x32e09.
    static DanmakuPattern nearA(boolean chained) {
        int start = chained ? 6 : 7;
        return new DanmakuPattern(List.of())
                .withTiming(new SpellTiming(SpellTiming.framesToTicks(chained ? 17 : 18), 0, 0))
                .withStrikes(List.of(box(start, start + 2, 20, -105, 99, -51),
                        box(start, start + 2, 42, -133, 91, -98)));
    }

    private static DanmakuStrike box(int from, int until, int left, int top, int right, int bottom) {
        // Adaptation: 100 source pixels = 1.8 blocks. Side-view X becomes forward Z;
        // side-view Y points down, so invert it. A 0.75-block lateral depth covers an MC body.
        double unit = 1.8D / 100.0D;
        return new DanmakuStrike(from, until, 0, 200 / 100.0f,
                new Vec3(-0.375, -bottom * unit - 0.99D, left * unit),
                new Vec3(0.375, -top * unit - 0.99D, right * unit));
    }
}
