package cc.thonly.reverie_dreams.item.danmaku;

import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpells;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class SpellcardArt {
    private SpellcardArt() {
    }

    private static final class Models {
        private static final Map<Identifier, Identifier> BY_PATTERN = TouhouSpells.catalog().stream()
                .map(TouhouSpell::id)
                .collect(Collectors.toUnmodifiableMap(Function.identity(), id -> id.withPrefix("spellcard/")));
    }

    /**
     * The standalone item definition for a card with dedicated artwork.
     */
    public static @Nullable Identifier itemModel(@Nullable Identifier pattern) {
        return pattern == null ? null : Models.BY_PATTERN.get(pattern);
    }
}
