package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07;

import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;

import java.util.ArrayList;
import java.util.List;

/** 妖妖梦：按关卡与路线的出场顺序汇总。 */
public final class Th07Spells {
    private Th07Spells() {}

    public static List<TouhouSpell> spells() {
        var spells = new ArrayList<TouhouSpell>();
        spells.addAll(Stage01.spells());
        spells.addAll(Stage02.spells());
        spells.addAll(Stage03.spells());
        spells.addAll(Stage04.spells());
        spells.addAll(Stage05.spells());
        spells.addAll(Stage06.spells());
        spells.addAll(Extra.spells());
        spells.addAll(Phantasm.spells());
        return List.copyOf(spells);
    }
}
