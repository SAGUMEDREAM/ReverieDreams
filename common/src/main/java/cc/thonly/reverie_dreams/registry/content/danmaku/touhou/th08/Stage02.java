package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th08;

import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;

import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.catalogOnly;

final class Stage02 {
    private Stage02() {}

    // Stage 2 · 米斯蒂娅·萝蕾拉（ecldata2.ecl）
    static List<TouhouSpell> spells() {
        return List.of(
                catalogOnly("touhou/th08/s02_001_normal", "声符「枭的夜鸣」", 1800),
                catalogOnly("touhou/th08/s02_002_normal", "蛾符「天蛾的蛊道」", 2160),
                catalogOnly("touhou/th08/s02_003_normal", "鹰符「伊尔斯塔德俯冲」", 2220),
                catalogOnly("touhou/th08/s02_004_normal", "夜盲「夜雀之歌」", 2220),
                catalogOnly("touhou/th08/s02_005_normal", "夜雀「深夜的合唱主唱」", 2400),

                // Hard：#19 在 Sub33(L1031) 与 Sub38(L1181) 两处二次开卡，同一卡号只登记一次
                catalogOnly("touhou/th08/s02_001_hard", "声符「角鸮咆哮」", 1800),
                catalogOnly("touhou/th08/s02_002_hard", "毒符「毒蛾的鳞粉」", 2280),
                catalogOnly("touhou/th08/s02_003_hard", "鹰符「伊尔斯塔德俯冲」", 2220),
                catalogOnly("touhou/th08/s02_004_hard", "夜盲「夜雀之歌」", 2220),
                catalogOnly("touhou/th08/s02_005_hard", "夜雀「深夜的合唱主唱」", 2400)
        );
    }
}
