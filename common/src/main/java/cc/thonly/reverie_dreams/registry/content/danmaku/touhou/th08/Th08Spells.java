package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th08;

import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;

import java.util.ArrayList;
import java.util.List;

/** 永夜抄：按关卡与路线的出场顺序汇总。 */
public final class Th08Spells {
    private Th08Spells() {}

    public static List<TouhouSpell> spells() {
        var spells = new ArrayList<TouhouSpell>();
        spells.addAll(Stage01.spells());
        spells.addAll(Stage02.spells());
        spells.addAll(Stage03.spells());
        spells.addAll(Stage04A.spells());
        spells.addAll(Stage04B.spells());
        spells.addAll(Stage05.spells());
        spells.addAll(FinalA.spells());
        spells.addAll(FinalB.spells());
        spells.addAll(Extra.spells());
        return List.copyOf(spells);
    }
}
