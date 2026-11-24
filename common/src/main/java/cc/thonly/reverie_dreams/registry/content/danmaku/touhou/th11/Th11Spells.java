package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th11;

import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;

import java.util.List;
import java.util.stream.Stream;

public final class Th11Spells {
    private Th11Spells() {}

    public static List<TouhouSpell> spells() {
        return Stream.of(Stage01.spells(), Stage02.spells(), Stage03.spells(), Stage04.spells(),
                        Stage05.spells(), Stage06.spells(), Extra.spells())
                .flatMap(List::stream).toList();
    }
}
