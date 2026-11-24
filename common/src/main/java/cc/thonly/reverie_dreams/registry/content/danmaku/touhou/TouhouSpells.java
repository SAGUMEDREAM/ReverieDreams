package cc.thonly.reverie_dreams.registry.content.danmaku.touhou;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuDefaults;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06Spells;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07Spells;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th08.Th08Spells;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th09.Th09Spells;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th10.Th10Spells;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th11.Th11Spells;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th12.Th12Spells;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th13.Th13Spells;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th14.Th14Spells;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th15.Th15Spells;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th16.Th16Spells;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th17.Th17Spells;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th18.Th18Spells;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public final class TouhouSpells {
    private TouhouSpells() {
    }

    private static final class Catalog {
        private static final List<TouhouSpell> ORDERED = build();
        private static final Map<Identifier, String> CHARACTERS = buildCharacters();

        private static List<TouhouSpell> build() {
            List<TouhouSpell> spells = Stream.of(Th06Spells.spells(), Th07Spells.spells(), Th08Spells.spells(),
                            Th09Spells.spells(), Th10Spells.spells(), Th11Spells.spells(), Th12Spells.spells(),
                            Th13Spells.spells(), Th14Spells.spells(), Th15Spells.spells(), Th16Spells.spells(),
                            Th17Spells.spells(), Th18Spells.spells())
                    .flatMap(List::stream).toList();
            Set<Identifier> ids = new HashSet<>();
            for (TouhouSpell spell : spells) {
                if (!ids.add(spell.id())) {
                    throw new IllegalStateException("Duplicate Touhou spell: " + spell.id());
                }
            }
            return spells;
        }

        private static Map<Identifier, String> buildCharacters() {
            Map<Identifier, String> characters = new LinkedHashMap<>();
            for (TouhouSpell spell : ORDERED) {
                if (spell.characterKey() != null) {
                    characters.put(spell.id(), spell.characterKey());
                }
            }
            return Map.copyOf(characters);
        }
    }

    public static void register(BiConsumer<Identifier, DanmakuPattern> registrar) {
        Patterns.ORDERED.forEach(registrar);
    }

    /** 全部已声明的符卡（含 catalog-only 条目），按作品与关卡顺序。 */
    public static List<TouhouSpell> catalog() {
        return Catalog.ORDERED;
    }

    @Nullable
    static String characterKey(Identifier id) {
        return Catalog.CHARACTERS.get(id);
    }

    /** 符卡归属角色的注册键（如 entity.reverie_dreams.yuyuko）；无归属时为空。 */
    @Nullable
    public static String characterKeyOf(Identifier id) {
        return Catalog.CHARACTERS.get(id);
    }

    private static final class Patterns {
        private static final Map<Identifier, DanmakuPattern> ORDERED = build();

        private static Map<Identifier, DanmakuPattern> build() {
            Map<Identifier, DanmakuPattern> patterns = new LinkedHashMap<>();
            for (TouhouSpell spell : Catalog.ORDERED) {
                var factory = spell.patternFactory();
                if (factory == null) {
                    continue;
                }
                DanmakuPattern pattern = Objects.requireNonNull(factory.get(), () -> "Missing pattern: " + spell.id());
                if (pattern.emitters().isEmpty() && pattern.lasers().isEmpty() && pattern.familiars().isEmpty()) {
                    throw new IllegalStateException("Empty Touhou spell: " + spell.id());
                }
                int ticks = spell.durationTicks();
                // Existing ports are finite motifs. Repeat their attack timeline within the ECL deadline,
                // unless a port provides its own intro and verified repeating section.
                if (!pattern.timing().isTimed()) {
                    int loop = Math.min(ticks, pattern.maxDuration() + 1);
                    pattern = pattern.withTiming(new SpellTiming(ticks, 0, loop));
                } else if (pattern.timing().duration() != ticks) {
                    throw new IllegalStateException("Spell timeline does not match ECL duration: " + spell.id());
                }
                patterns.put(spell.id(), pattern);
            }
            return Collections.unmodifiableMap(patterns);
        }
    }

    public static void addChineseSpellNames(BiConsumer<String, String> builder) {
        for (TouhouSpell spell : Catalog.ORDERED) {
            builder.accept(DanmakuDefaults.translationKey(spell.id()), spell.chineseName());
        }
    }
}
