package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th10;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
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

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.catalogOnly;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.implemented;

final class Extra {
    private Extra() {}

    /**
     * 落叶弹用米粒弹型近似（原始 ins_402 弹种 3）。
     */
    private static final Identifier LEAF_TYPE = ReverieDreams.id("rice");

    /**
     * Extra（s07）：道中 八坂神奈子（#97–99）在前，关底 洩矢诹访子（#100–109）在后，
     * 全部为 342 固定卡号，仅 Extra 一个难度。
     */
    static List<TouhouSpell> spells() {
        return List.of(
                implemented("touhou/th10/s07_097_extra",
                        "神符「如水眼之美丽源泉」", 4200, () -> beautifulSpring()),
                catalogOnly("touhou/th10/s07_098_extra",
                        "神符「结于杉木之古缘」", 4200),
                catalogOnly("touhou/th10/s07_099_extra",
                        "神符「神所踏足之御神渡」", 4200),
                catalogOnly("touhou/th10/s07_100_extra",
                        "开宴「二拜二拍一拜」", 6300),
                catalogOnly("touhou/th10/s07_101_extra",
                        "土著神「手长足长大人」", 6300),
                catalogOnly("touhou/th10/s07_102_extra",
                        "神具「洩矢的铁轮」", 6300),
                catalogOnly("touhou/th10/s07_103_extra",
                        "源符「厌川的翡翠」", 6300),
                catalogOnly("touhou/th10/s07_104_extra",
                        "蛙狩「蛙以口鸣，方致蛇祸」", 6300),
                catalogOnly("touhou/th10/s07_105_extra",
                        "土著神「七石七木」", 6300),
                catalogOnly("touhou/th10/s07_106_extra",
                        "土著神「小小青蛙不输风雨」", 6300),
                catalogOnly("touhou/th10/s07_107_extra",
                        "土著神「宝永四年的赤蛙」", 2400),
                catalogOnly("touhou/th10/s07_108_extra",
                        "「诹访大战 ～ 土著神话 vs 中央神话」", 5040),
                catalogOnly("touhou/th10/s07_109_extra",
                        "祟符「御社宫司大人」", 9600)
        );
    }

    // stage07.ecl.raw L3566–3594 / L3945–3974. A fixed one-direction stack is
    // released each ECL frame; its angle advances by 3.883222 radians. Use every
    // third frame and every second speed layer so the wake remains navigable.
    private static DanmakuPattern beautifulSpring() {
        int intro = SpellTiming.framesToTicks(90);
        int wave = SpellTiming.framesToTicks(160);
        int loop = SpellTiming.framesToTicks(220);
        List<DanmakuEmitter> emitters = new ArrayList<>();
        var motion = DanmakuMotion.of(Vec3.ZERO, 0, 0, List.of(
                MotionSegment.accelerate(0, 0.009f),
                MotionSegment.linear(SpellTiming.framesToTicks(90))));
        var drops = DanmakuBulletSpec.of(LEAF_TYPE, 0.62f, motion)
                .withColor(0xC2DEFF).withLifetime(80);
        for (int t = 0; t < wave; t++) {
            float yaw = (float) (Math.toDegrees(Math.PI / 2 + t * 3 * 3.883222) % 360);
            emitters.add(DanmakuEmitter.of(EmitShape.multiRing(4, 1, 0, true, 0, 0, -5f / 24f), drops)
                    .withAim(EmitAim.FIXED, 0, yaw).withAttached(false).withTiming(intro + t, 0, 1));
        }
        return new DanmakuPattern(emitters)
                .withTiming(new SpellTiming(SpellTiming.framesToTicks(4200), intro, loop));
    }
}
