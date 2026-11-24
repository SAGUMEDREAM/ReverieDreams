package cc.thonly.reverie_dreams.world.gensokyo.tree;

import net.minecraft.core.registries.Registries;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class GensokyoJungleTest {
    @Test
    void jungleMixContainsSixDistinctSilhouettesAndGiantTreesAreAMinority() {
        var mix = GensokyoForestType.JUNGLE.mix();
        assertEquals(6, mix.size());
        assertEquals(1.0, mix.stream().mapToDouble(GensokyoForestType.Mix::weight).sum(), 0.00001);
        var forms = new HashSet<GensokyoTreeShape.Form>();
        for (var entry : mix) {
            forms.add(GensokyoTreeShape.create(entry.profile().settings(), new Random(42)).form());
            assertTrue(entry.weight() > 0);
        }
        assertEquals(6, forms.size());
        assertTrue(mix.stream().filter(e -> e.profile() == GensokyoTreeProfile.JUNGLE_CANOPY)
                .mapToDouble(GensokyoForestType.Mix::weight).sum() < 0.15);
    }

    @Test
    void jungleBiomeUsesCustomTreesAndDecorationsWithoutVanillaBambooOrJungleLayers() {
        var lookup = GensokyoForestPreviewExporter.registries();
        var biome = lookup.lookupOrThrow(Registries.BIOME).getOrThrow(GensokyoForestType.JUNGLE.biomeKey()).value();
        var features = biome.getGenerationSettings().features().stream().flatMap(set -> set.stream())
                .map(h -> h.unwrapKey().orElseThrow().identifier().toString()).toList();
        for (String needed : new String[]{"gensokyo/jungle_trees", "gensokyo/jungle/vines", "gensokyo/jungle/fallen_log",
                "gensokyo/understory/jungle/leaf_patch", "gensokyo/understory/jungle/shrub", "gensokyo/understory/jungle/thicket",
                "gensokyo/understory/jungle/floor_patch", "gensokyo/rocks/jungle/low_outcrop"}) {
            assertTrue(features.contains("reverie_dreams:" + needed), needed);
        }
        assertTrue(features.stream().noneMatch(id -> id.startsWith("minecraft:") && (id.contains("bamboo") || id.contains("jungle"))));
        assertEquals(GensokyoForestBootstrap.JUNGLE_GRASS_COLOR, biome.getSpecialEffects().grassColorOverride().orElseThrow() & 0xFFFFFF);
    }
}
