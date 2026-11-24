package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th08;

import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;

import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.catalogOnly;

final class Stage04A {
    private Stage04A() {}

    // Stage 4A · 博丽灵梦 & 八云紫（ecldata4a.ecl，路线路径 s04a）
    static List<TouhouSpell> spells() {
        return List.of(
                catalogOnly("touhou/th08/s04a_001_normal", "梦符「二重结界」", 3000),
                catalogOnly("touhou/th08/s04a_002_normal", "灵符「梦想封印　散」", 2700),
                catalogOnly("touhou/th08/s04a_003_normal", "梦符「封魔阵」", 2700),
                catalogOnly("touhou/th08/s04a_004_normal", "灵符「梦想封印　集」", 3000),
                catalogOnly("touhou/th08/s04a_005_normal", "境界「二重弹幕结界」", 3000),
                catalogOnly("touhou/th08/s04a_006_normal", "神灵「梦想封印　瞬」", 3900),



                catalogOnly("touhou/th08/s04a_001_hard", "梦境「二重大结界」", 3000),
                catalogOnly("touhou/th08/s04a_002_hard", "散灵「梦想封印　寂」", 2700),
                catalogOnly("touhou/th08/s04a_003_hard", "神技「八方鬼缚阵」", 2700),
                catalogOnly("touhou/th08/s04a_004_hard", "回灵「梦想封印　侘」", 3000),
                catalogOnly("touhou/th08/s04a_005_hard", "大结界「博丽弹幕结界」", 3000),
                catalogOnly("touhou/th08/s04a_006_hard", "神灵「梦想封印　瞬」", 3900)
        );
    }
}
