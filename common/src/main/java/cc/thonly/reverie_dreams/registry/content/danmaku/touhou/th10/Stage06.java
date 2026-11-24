package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th10;

import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;

import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.catalogOnly;

final class Stage06 {
    private Stage06() {}

    /**
     * 六面：八坂神奈子。第三张卡 N 走 BossCard3（贄符 #86），H 走 BossCard3H（神秘「葛井の清水」 #87）。
     */
    static List<TouhouSpell> spells() {
        return List.of(
                catalogOnly("touhou/th10/s06_078_normal",
                        "神祭「扩大御柱」", 4200),
                catalogOnly("touhou/th10/s06_079_hard",
                        "奇祭「目处梃子乱舞」", 4200),
                catalogOnly("touhou/th10/s06_082_normal",
                        "筒粥「神之粥」", 4200),
                catalogOnly("touhou/th10/s06_083_hard",
                        "忘谷「遗忘之谷」", 4200),
                catalogOnly("touhou/th10/s06_086_normal",
                        "贽符「御射山御狩神事」", 4200),
                catalogOnly("touhou/th10/s06_087_hard",
                        "神秘「葛井之清水」", 3600),
                catalogOnly("touhou/th10/s06_090_normal",
                        "天流「天水奇迹」", 6000),
                catalogOnly("touhou/th10/s06_091_hard",
                        "天龙「雨之源泉」", 6000),
                catalogOnly("touhou/th10/s06_094_normal",
                        "「信仰之山」", 9000),
                catalogOnly("touhou/th10/s06_095_hard",
                        "「风神之神德」", 9000)
        );
    }
}
