package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeRange;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.catalogOnly;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.implemented;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.ARROWHEAD;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.WHITE;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.fire;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.shot;

final class Stage04 {
    private Stage04() {}

    private static final Identifier NOTE = ReverieDreams.id("note");
    private static final int BROWN = 0xB08048;

    // 四面（莉莉白道中无符卡；三骚灵共用卡名、独奏卡随路线）
    static List<TouhouSpell> spells() {
        return List.of(
                implemented("touhou/th07/s04_001_normal", "騒符「幻灵盛宴」", 3602, () -> phantomDinning()),
                catalogOnly("touhou/th07/s04_002_normal", "弦奏「瓜奈里·德尔·杰苏」", 3602),
                catalogOnly("touhou/th07/s04_003_normal", "管霊「日野幻灵」", 3602),
                catalogOnly("touhou/th07/s04_004_normal", "冥鍵「法吉奥里冥奏」", 3602),
                catalogOnly("touhou/th07/s04_005_normal", "合葬「棱镜协奏曲」", 3602),
                catalogOnly("touhou/th07/s04_006_normal", "大合葬「灵车大协奏曲」", 3602),
                catalogOnly("touhou/th07/s04_001_hard", "騒符「现场骚灵现象」", 3602),
                catalogOnly("touhou/th07/s04_002_hard", "神弦「斯特拉迪瓦里」", 3962),
                catalogOnly("touhou/th07/s04_003_hard", "冥管「幽灵克利福德」", 3602),
                catalogOnly("touhou/th07/s04_004_hard", "鍵霊「贝森朵夫神奏」", 3602),
                catalogOnly("touhou/th07/s04_005_hard", "騒葬「斯提吉斯河岸」", 3602),
                catalogOnly("touhou/th07/s04_006_hard", "大合葬「灵车大协奏曲·改」", 3602)
        );
    }

    /**
     * 四面三骚灵卡一 #45（Sub121，raw L4318-4429）：中枢旋转音符螺旋 +
     * 两侧使魔圈的瞄准箭弹。三姐妹实体圈以单环瞄准近似，实体未还原已标注。
     */
    private static DanmakuPattern phantomDinning() {
        DanmakuEmitter spiral = fire(EmitShape.fan(1, 0f), shot(NOTE, 0.45f, WHITE),
                0, 380, 3, EmitAim.OUTWARD, 0f, 0f,
                new Vec3(0.0D, 0.8D, 0.0D), KeyframeRange.empty(0f, 760f));
        DanmakuEmitter aimed = fire(EmitShape.ring(3, 1.9f, true), shot(ARROWHEAD, 0.5f, BROWN),
                20, 360, 30, EmitAim.TARGET, 0f, 0f);
        return DanmakuPattern.of(spiral, aimed);
    }
}
