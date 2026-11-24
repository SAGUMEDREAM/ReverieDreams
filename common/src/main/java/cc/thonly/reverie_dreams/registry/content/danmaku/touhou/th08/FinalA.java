package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th08;

import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;

import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.catalogOnly;

final class FinalA {
    private FinalA() {}

    // Final A · 八意永琳（ecldata6.ecl，路线路径 s06a）
    static List<TouhouSpell> spells() {
        return List.of(
                catalogOnly("touhou/th08/s06a_001_normal", "天丸「壶中天地」", 3600),
                catalogOnly("touhou/th08/s06a_002_normal", "觉神「神代的记忆」", 2400),
                catalogOnly("touhou/th08/s06a_003_normal", "苏活「生命游戏　-生命游戏-」", 4620),
                catalogOnly("touhou/th08/s06a_004_normal", "操神「思兼神器」", 3720),
                catalogOnly("touhou/th08/s06a_005_normal", "天咒「阿波罗13」", 4200),
                catalogOnly("touhou/th08/s06a_006_normal", "秘术「天文密葬法」", 7200),
                catalogOnly("touhou/th08/s06a_007_normal", "禁药「蓬莱之药」", 5940),

                catalogOnly("touhou/th08/s06a_001_hard", "天丸「壶中天地」", 3600),
                catalogOnly("touhou/th08/s06a_002_hard", "神符「天人的系谱」", 2400),
                catalogOnly("touhou/th08/s06a_003_hard", "苏生「再起游戏」", 4620),
                catalogOnly("touhou/th08/s06a_004_hard", "神脑「思兼之脑」", 3720),
                catalogOnly("touhou/th08/s06a_005_hard", "天咒「阿波罗13」", 4200),
                catalogOnly("touhou/th08/s06a_006_hard", "秘术「天文密葬法」", 7200),
                catalogOnly("touhou/th08/s06a_007_hard", "禁药「蓬莱之药」", 5940)
        );
    }
}
