package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th10;

import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;

import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.catalogOnly;

final class Stage05 {
    private Stage05() {}

    /**
     * 五面：道中 + 关底 东风谷早苗。BossCard1/BossCard2 内 359 同基号分支：
     * Hard 落「明るい夜」/「モーゼの奇跡」（基号 63/67），Lunatic 才落「明るすぎる夜」/另一张
     * 「モーゼの奇跡」（基号 63/68 的 L 分支），N/H 只登记 Hard 分支。
     */
    static List<TouhouSpell> spells() {
        return List.of(
                catalogOnly("touhou/th10/s05_058_normal",
                        "秘术「灰色奇术」", 2400),
                catalogOnly("touhou/th10/s05_059_hard",
                        "秘术「遗忘之祭仪」", 2400),
                catalogOnly("touhou/th10/s05_062_normal",
                        "奇迹「白昼的客星」", 4200),
                catalogOnly("touhou/th10/s05_063_hard",
                        "奇迹「客星璀璨之夜」", 4200),
                catalogOnly("touhou/th10/s05_066_normal",
                        "开海「海水分开之日」", 4200),
                catalogOnly("touhou/th10/s05_067_hard",
                        "开海「摩西之奇迹」", 4200),
                catalogOnly("touhou/th10/s05_070_normal",
                        "准备「呼唤神风的星之仪式」", 4200),
                catalogOnly("touhou/th10/s05_071_hard",
                        "准备「召请建御名方神」", 4200),
                catalogOnly("touhou/th10/s05_074_normal",
                        "奇迹「神之风」", 4200),
                catalogOnly("touhou/th10/s05_075_hard",
                        "大奇迹「八坂之神风」", 4200)
        );
    }
}
