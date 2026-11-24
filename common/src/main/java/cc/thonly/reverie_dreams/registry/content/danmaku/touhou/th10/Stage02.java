package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th10;

import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;

import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.catalogOnly;

final class Stage02 {
    private Stage02() {}

    /**
     * 二面：道中 + 关底 键山雏。弹幕主体在 {@code Boss1CardAtEnemy} 等 effect 脚本内，暂缺实现。
     */
    static List<TouhouSpell> spells() {
        return List.of(
                catalogOnly("touhou/th10/s02_011_normal",
                        "厄符「厄运」", 2400),
                catalogOnly("touhou/th10/s02_012_hard",
                        "厄符「厄神大人的生理节律」", 2400),
                catalogOnly("touhou/th10/s02_015_normal",
                        "疵符「破裂的护符」", 2700),
                catalogOnly("touhou/th10/s02_016_hard",
                        "疵痕「损坏的护符」", 2700),
                catalogOnly("touhou/th10/s02_019_normal",
                        "恶灵「厄运之轮」", 2700),
                catalogOnly("touhou/th10/s02_020_hard",
                        "悲运「大钟婆之火」", 2700),
                catalogOnly("touhou/th10/s02_023_normal",
                        "创符「痛苦之流」", 2700),
                catalogOnly("touhou/th10/s02_024_hard",
                        "创符「流放人偶」", 2700)
        );
    }
}
