package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th08;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuFamiliar;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.catalogOnly;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.implemented;

final class Stage03 {
    private Stage03() {}

    private static final Identifier TYPE_BALL = ReverieDreams.id("ball");
    private static final Identifier TYPE_RICE = ReverieDreams.id("rice");

    // Stage 3 · 上白泽慧音（ecldata3.ecl）
    static List<TouhouSpell> spells() {
        return List.of(
                implemented("touhou/th08/s03_001_normal", "产灵「第一金字塔」", 2280, () -> firstPyramid(2)),
                catalogOnly("touhou/th08/s03_002_normal", "始符「蜉蝣137」", 2040),
                catalogOnly("touhou/th08/s03_003_normal", "野符「将门危机」", 2040),
                // #43–46 的开卡指令共用掩码；难度分支分别调用其脚本。
                catalogOnly("touhou/th08/s03_005_normal", "国符「三种神器　玉」", 2400),
                catalogOnly("touhou/th08/s03_008_normal", "终符「幻想天皇」", 1920),
                catalogOnly("touhou/th08/s03_009_normal", "未来「高天原」", 2100),

                implemented("touhou/th08/s03_001_hard", "产灵「第一金字塔」", 2280, () -> firstPyramid(3)),
                catalogOnly("touhou/th08/s03_002_hard", "始符「蜉蝣137」", 2040),
                catalogOnly("touhou/th08/s03_003_hard", "野符「义满危机」", 2040),
                catalogOnly("touhou/th08/s03_006_hard", "国符「三种神器　镜」", 2400),
                catalogOnly("touhou/th08/s03_008_hard", "虚史「幻想乡传说」", 1920),
                catalogOnly("touhou/th08/s03_009_hard", "未来「高天原」", 2100)
        );
    }

    // ECL Sub30–33 (Normal) and Sub35–38 (Hard). The triangles are firing sources. Their scripted
    // translation is adapted to two/three 120-degree formations moving around the cast.
    private static DanmakuPattern firstPyramid(int groups) {
        boolean hard = groups == 3;
        List<DanmakuFamiliar> familiars = new java.util.ArrayList<>();
        float[] phase = {-90, 90, 180};
        float[] radii = {2.5f, 4, 5.5f};
        float[] rotations = {-3f, 3f, -3f};
        DanmakuEmitter sprays = DanmakuEmitter.of(EmitShape.fan(2, 60),
                        DanmakuBulletSpec.of(TYPE_RICE, hard ? 0.5f : 0.45f)
                                .withColor(0xB8E3FF).withLifetime(90))
                .withAim(EmitAim.TARGET_OR_SOURCE_FACING, 0, 0)
                .withTiming(14, 350, hard ? 4 : 5);
        for (int g = 0; g < groups; g++) {
            for (int corner = 0; corner < 3; corner++) {
                familiars.add(DanmakuFamiliar.of(new Vec3(0, 1.4, 0), DanmakuPattern.of(sprays))
                        .withDelay(30).withDuration(365)
                        .withOrbit(radii[g], rotations[g], phase[g] + corner * 120));
            }
        }
        var support = DanmakuEmitter.of(EmitShape.multiRing(3, 3, 0, true, 0, 0, -1f / 6),
                        DanmakuBulletSpec.of(TYPE_BALL, 0.75f).withColor(0xE3E3FF).withLifetime(70))
                .withAim(EmitAim.TARGET, 0, 0).withTiming(64, 333, 67);
        return DanmakuPattern.of(support).withFamiliars(familiars)
                .withTiming(new SpellTiming(760, 30, 367));
    }
}
