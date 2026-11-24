package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.catalogOnly;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.implemented;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.bullet;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.layerStep;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.sp;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.stream;

final class Stage05 {
    private Stage05() {}

    // 5 面 · 十六夜咲夜
    static List<TouhouSpell> spells() {
        return List.of(
                // 道中奇术：Sub18–20。先放苦无环，随后从对侧放实际的飞刀。
                implemented("touhou/th06/s05_032_normal", "奇术「Misdirection」", 1800, () -> misdirection(24)),
                // 幻在/幻象/幻幽/幻世与女仆秘技：EX4/EX5 战斗时停 + EX4(2) 重定向（engine.md §4）——时停缺失，全部阻塞。
                catalogOnly("touhou/th06/s05_034_normal", "幻在「Clock Corpse」", 1800),
                catalogOnly("touhou/th06/s05_035_normal", "幻象「Luna Clock」", 1800),
                catalogOnly("touhou/th06/s05_036_normal", "女仆秘技「操弄玩偶」", 1800),
                // 道中幻惑（H/L）：Sub19 同源，H/L 加边界反弹（flags 2052）——反弹缺失，阻塞。
                catalogOnly("touhou/th06/s05_033_hard", "奇术「幻惑Misdirection」", 1800),
                catalogOnly("touhou/th06/s05_037_hard", "幻幽「Jack the Ludo Bile」", 1800),
                catalogOnly("touhou/th06/s05_038_hard", "幻世「The World」", 1800),
                catalogOnly("touhou/th06/s05_039_hard", "女仆秘技「杀人玩偶」", 1800)
        );
    }

    /**
     * 奇術「ミスディレクション」：Sub19/20 先放 type 4 苦无；80/98/116 帧从对侧
     * 发射 type 8 飞刀。飞刀为四层、每层 11 路，角距 12°。
     */
    private static DanmakuPattern misdirection(int count) {
        float step = layerStep(3f, 1.2f, 3);
        DanmakuBulletSpec spec = bullet(ReverieDreams.id("kunai"), 3f, 70).withColor(0xF55256);
        List<DanmakuEmitter> emitters = new ArrayList<>(List.of(
                stream(EmitShape.multiRing(3, count, 0f, true, 0f, 0f, step),
                        spec, 0, 240, 80, EmitAim.TARGET, 0f, 0f, null)
                        .withOffset(new Vec3(-6, 0, 0)),
                stream(EmitShape.multiRing(3, count, 0f, true, 0f, 0f, step),
                        spec, 40, 240, 80, EmitAim.TARGET, 0f, 0f, null)
                        .withOffset(new Vec3(6, 0, 0))
        ));
        var knife = ReverieDreams.id("throwing_knife");
        var fan = EmitShape.fan(11, 120f);
        for (int layer = 0; layer < 4; layer++) {
            float speed = 4.5f - (4.5f - 1.2f) * layer / 4f;
            var blade = bullet(knife, speed, 70).withColor(0xAA75E7);
            for (int pulse = 0; pulse < 3; pulse++) {
                int delay = SpellTiming.framesToTicks(80 + pulse * 18);
                emitters.add(stream(fan, blade, delay, 240, 80, EmitAim.TARGET, 0f, 0f, null)
                        .withOffset(new Vec3(6, 0, 0)));
                emitters.add(stream(fan, blade, 40 + delay, 240, 80, EmitAim.TARGET, 0f, 0f, null)
                        .withOffset(new Vec3(-6, 0, 0)));
            }
        }
        return DanmakuPattern.of(emitters.toArray(DanmakuEmitter[]::new));
    }
}
