package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th17;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class Th17Spells {
    private Th17Spells() {}

    public static List<TouhouSpell> spells() {
        return Stream.of(Stage01.spells(), Stage02.spells(), Stage03.spells(), Stage04.spells(),
                        Stage05.spells(), Stage06.spells(), ExtraCheckpoint.spells(), ExtraPegasus.spells())
                .flatMap(List::stream).toList();
    }

    static List<TouhouSpell> variants(int stage, int number, String owner, String normal, String hard,
                                      int frames, Function<Boolean, DanmakuPattern> pattern) {
        return List.of(card(stage, number, owner, normal, frames, "normal", () -> pattern.apply(false)),
                card(stage, number, owner, hard, frames, "hard", () -> pattern.apply(true)));
    }

    static TouhouSpell extra(int number, String owner, String title, int frames, Supplier<DanmakuPattern> pattern) {
        return card(7, number, owner, title, frames, "extra", pattern);
    }

    private static TouhouSpell card(int stage, int number, String owner, String title, int frames, String difficulty,
                                    Supplier<DanmakuPattern> pattern) {
        String path = String.format(Locale.ROOT, "touhou/th17/s%02d_%03d_%s", stage, number, difficulty);
        return TouhouSpell.implemented(path, title, frames, pattern).withCharacter("entity.reverie_dreams." + owner);
    }
}
