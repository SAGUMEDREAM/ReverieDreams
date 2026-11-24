package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07;

import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;

import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.catalogOnly;

final class Stage03 {
    private Stage03() {}

    // 三面（爱丽丝道中 Hard 乙女文乐；关底四卡）
    static List<TouhouSpell> spells() {
        return List.of(
                catalogOnly("touhou/th07/s03_001_normal", "蒼符「博爱的法兰西人偶」", 3000),
                catalogOnly("touhou/th07/s03_002_normal", "紅符「红毛的荷兰人偶」", 3600),
                catalogOnly("touhou/th07/s03_003_normal", "闇符「雾之伦敦人偶」", 2700),
                catalogOnly("touhou/th07/s03_004_normal", "咒詛「魔彩光的上海人偶」", 3300),
                catalogOnly("touhou/th07/s03_001_hard", "操符「少女文乐」", 3000),
                catalogOnly("touhou/th07/s03_002_hard", "蒼符「博爱的法兰西人偶 -Hard-」", 3000),
                catalogOnly("touhou/th07/s03_003_hard", "白符「白垩的俄罗斯人偶」", 3600),
                catalogOnly("touhou/th07/s03_004_hard", "廻符「轮回的西藏人偶」", 2700),
                catalogOnly("touhou/th07/s03_005_hard", "咒詛「魔彩光的上海人偶 -Hard-」", 3300)
        );
    }
}
