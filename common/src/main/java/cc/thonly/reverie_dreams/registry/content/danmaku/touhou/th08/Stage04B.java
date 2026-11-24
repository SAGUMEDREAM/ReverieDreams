package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th08;

import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;

import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.catalogOnly;

final class Stage04B {
    private Stage04B() {}

    // Stage 4B · 雾雨魔理沙 & 爱丽丝（ecldata4b.ecl，路线路径 s04b）
    static List<TouhouSpell> spells() {
        return List.of(
                catalogOnly("touhou/th08/s04b_001_normal", "魔符「银河」", 2400),
                catalogOnly("touhou/th08/s04b_002_normal", "魔符「星尘幻想曲」", 3900),
                catalogOnly("touhou/th08/s04b_003_normal", "恋符「无方向激光」", 2700),
                catalogOnly("touhou/th08/s04b_004_normal", "恋符「极限火花」", 3000),
                catalogOnly("touhou/th08/s04b_005_normal", "光符「地光光线」", 3300),
                catalogOnly("touhou/th08/s04b_006_normal", "魔炮「最终火花」", 3900),


                catalogOnly("touhou/th08/s04b_001_hard", "魔空「小行星带」", 2400),
                catalogOnly("touhou/th08/s04b_002_hard", "黑魔「事件视界」", 3900),
                catalogOnly("touhou/th08/s04b_003_hard", "恋风「星光台风」", 2700),
                catalogOnly("touhou/th08/s04b_004_hard", "恋心「双重火花」", 3000),
                catalogOnly("touhou/th08/s04b_005_hard", "光击「射月」", 3300),
                catalogOnly("touhou/th08/s04b_006_hard", "魔炮「最终火花」", 3900)
        );
    }
}
