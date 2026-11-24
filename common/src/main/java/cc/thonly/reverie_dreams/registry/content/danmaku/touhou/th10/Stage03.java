package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th10;

import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;

import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.catalogOnly;

final class Stage03 {
    private Stage03() {}

    /**
     * 三面：道中 + 关底 河城荷取。ECL 41 为 Lunatic 特例（BossCard3L），N/H 不登记。
     */
    static List<TouhouSpell> spells() {
        return List.of(
                catalogOnly("touhou/th10/s03_027_normal",
                        "光学「光学迷彩」", 2700),
                catalogOnly("touhou/th10/s03_028_hard",
                        "光学「水迷彩」", 2700),
                catalogOnly("touhou/th10/s03_031_normal",
                        "洪水「泥浆泛滥」", 3600),
                catalogOnly("touhou/th10/s03_032_hard",
                        "洪水「冲积梦魇」", 3600),
                catalogOnly("touhou/th10/s03_035_normal",
                        "水符「河童之河口浪潮」", 3600),
                catalogOnly("touhou/th10/s03_036_hard",
                        "水符「河童之山洪暴发」", 3600),
                catalogOnly("touhou/th10/s03_039_normal",
                        "河童「妖怪黄瓜」", 3600),
                catalogOnly("touhou/th10/s03_040_hard",
                        "河童「延展手臂」", 3600)
        );
    }
}
