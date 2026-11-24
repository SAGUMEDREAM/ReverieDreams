package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th10;

import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;

import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.catalogOnly;

final class Stage04 {
    private Stage04() {}

    /**
     * 四面：道中 犬走椛（无符卡），关底 射命丸文。「幻想風靡」为 N/H 共名生存卡，各给稳定 ID。
     */
    static List<TouhouSpell> spells() {
        return List.of(
                catalogOnly("touhou/th10/s04_043_normal",
                        "岐符「天之八衢」", 3600),
                catalogOnly("touhou/th10/s04_044_hard",
                        "岐符「猿田彦神之岔路」", 3600),
                catalogOnly("touhou/th10/s04_047_normal",
                        "风神「风神木叶隐身术」", 3600),
                catalogOnly("touhou/th10/s04_048_hard",
                        "风神「天狗颪」", 3600),
                catalogOnly("touhou/th10/s04_050_normal",
                        "「幻想风靡」", 1080),
                catalogOnly("touhou/th10/s04_051_hard",
                        "「幻想风靡」", 1080),
                catalogOnly("touhou/th10/s04_054_normal",
                        "塞符「山神渡御」", 4200),
                catalogOnly("touhou/th10/s04_055_hard",
                        "塞符「天孙降临」", 4200)
        );
    }
}
