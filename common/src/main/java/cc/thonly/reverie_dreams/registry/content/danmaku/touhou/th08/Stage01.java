package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th08;

import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;

import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.catalogOnly;

final class Stage01 {
    private Stage01() {}

    // Stage 1 · 莉格露·奈特巴格（ecldata1.ecl）
    static List<TouhouSpell> spells() {
        return List.of(
                // Normal: no midboss spell; #10 is the genuine story Last Spell.
                catalogOnly("touhou/th08/s01_001_normal", "灯符「萤火虫现象」", 1920),
                catalogOnly("touhou/th08/s01_002_normal", "蠢符「小虫风暴」", 2220),
                catalogOnly("touhou/th08/s01_003_normal", "隐虫「永夜蛰居」", 2400),


                // Hard：#0/#4/#8/#11 为 F4 掩码入口，萤符仅在 H/L 存在
                catalogOnly("touhou/th08/s01_001_hard", "萤符「地上的流星」", 2100),
                catalogOnly("touhou/th08/s01_002_hard", "灯符「萤火虫现象」", 1920),
                catalogOnly("touhou/th08/s01_003_hard", "蠢符「夜虫风暴」", 2220),
                catalogOnly("touhou/th08/s01_004_hard", "隐虫「永夜蛰居」", 2400)
        );
    }
}
