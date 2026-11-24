package cc.thonly.reverie_dreams.world.gensokyo.tree;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biomes;

import static org.junit.jupiter.api.Assertions.*;

class GensokyoForestRegistryTest {
    @Test
    void allStandaloneBiomesResolveAndReplaceTheirTemplateTreesWithOneMixedLayer() {
        var registries = GensokyoForestPreviewExporter.registries();
        var ops = registries.createSerializationContext(JsonOps.INSTANCE);
        for (var type : GensokyoForestType.values()) {
            var biome = registries.lookupOrThrow(Registries.BIOME).getOrThrow(GensokyoForestBootstrap.biome(type)).value();
            long ours = biome.getGenerationSettings().features().stream().flatMap(set -> set.stream())
                    .filter(holder -> holder.is(GensokyoForestBootstrap.placed(type.treesPath()))).count();
            assertEquals(1, ours);
            assertDoesNotThrow(() -> Biome.DIRECT_CODEC.parse(ops, Biome.DIRECT_CODEC.encodeStart(ops, biome).getOrThrow()).getOrThrow());
        }
        // The vanilla biome and its feature list remain independent.
        var vanillaForest = registries.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.FOREST).value();
        assertFalse(vanillaForest.getGenerationSettings().features().stream().flatMap(set -> set.stream())
                .anyMatch(holder -> holder.unwrapKey().map(key -> key.identifier().getNamespace().equals("reverie_dreams")).orElse(false)));
    }

    @Test
    void mapleForestPreservesTheOriginalUnderstoryAndUsesActualMapleMaterials() {
        var registries = GensokyoForestPreviewExporter.registries();
        var biomes = registries.lookupOrThrow(Registries.BIOME);
        var original = biomes.getOrThrow(ResourceKey.create(Registries.BIOME,
                GensokyoForestBootstrap.id("maple_forest"))).value();
        var variant = biomes.getOrThrow(GensokyoForestBootstrap.biome(GensokyoForestType.MAPLE_FOREST)).value();
        var originalIds = original.getGenerationSettings().features().stream().flatMap(set -> set.stream())
                .map(holder -> holder.unwrapKey().orElseThrow().identifier().toString()).toList();
        var variantIds = variant.getGenerationSettings().features().stream().flatMap(set -> set.stream())
                .map(holder -> holder.unwrapKey().orElseThrow().identifier().toString()).toList();
        var originalTrees = Set.of("reverie_dreams:iroha_maple_tree_placed", "reverie_dreams:itaya_maple_tree_placed",
                "reverie_dreams:hauchiwa_maple_tree_placed");
        assertEquals(3, originalIds.stream().filter(originalTrees::contains).count());
        assertTrue(variantIds.stream().noneMatch(originalTrees::contains));
        assertEquals(originalIds.stream().filter(id -> !originalTrees.contains(id)).toList(),
                variantIds.stream().filter(id -> !id.equals("reverie_dreams:gensokyo/maple_forest_trees")
                        && !id.startsWith("reverie_dreams:gensokyo/understory/") && !id.startsWith("reverie_dreams:gensokyo/rocks/")).toList());
        assertTrue(variantIds.contains("reverie_dreams:maple_leaf_litter_placed"));
        var ops = registries.createSerializationContext(JsonOps.INSTANCE);
        var before = Biome.DIRECT_CODEC.encodeStart(ops, original).getOrThrow().getAsJsonObject();
        var after = Biome.DIRECT_CODEC.encodeStart(ops, variant).getOrThrow().getAsJsonObject();
        for (String field : List.of("temperature", "downfall", "attributes", "spawners", "spawn_costs")) {
            assertEquals(before.get(field), after.get(field), field);
        }
        assertEquals(before.getAsJsonObject("effects").get("water_color"), after.getAsJsonObject("effects").get("water_color"));
        assertEquals("#879b55", after.getAsJsonObject("effects").get("grass_color").getAsString());
        assertEquals("#668347", after.getAsJsonObject("effects").get("foliage_color").getAsString());
        assertEquals("#c78a3a", before.getAsJsonObject("effects").get("grass_color").getAsString(), "Overworld template stays independent");
        for (var entry : GensokyoForestType.MAPLE_FOREST.mix()) {
            if (!entry.profile().settings().species().isMaple()) continue;
            var config = GensokyoForestBootstrap.configuration(entry.profile());
            String species = config.shape().name().toLowerCase(Locale.ROOT);
            assertEquals("reverie_dreams:" + species + "_log", BuiltInRegistries.BLOCK.getKey(config.log().getBlock()).toString());
            assertEquals("reverie_dreams:" + species + "_leaves", BuiltInRegistries.BLOCK.getKey(config.leaves().getBlock()).toString());
        }
        assertEquals(0.14, GensokyoForestType.MAPLE_FOREST.mix().stream()
                .filter(entry -> !entry.profile().settings().species().isMaple()).mapToDouble(GensokyoForestType.Mix::weight).sum(), 0.00001);
    }

    @Test
    void mapleForestContainsBothOriginalAndAddedFamiliesAndOldJsonDefaultsToOriginal() {
        var mix = GensokyoForestType.MAPLE_FOREST.mix();
        assertEquals(0.43, mix.stream().filter(e -> e.profile().settings().species().isMaple() && !e.profile().isAutumnMaple())
                .mapToDouble(GensokyoForestType.Mix::weight).sum(), 0.00001);
        assertEquals(0.43, mix.stream().filter(e -> e.profile().isAutumnMaple())
                .mapToDouble(GensokyoForestType.Mix::weight).sum(), 0.00001);
        var registries = GensokyoForestPreviewExporter.registries();
        var ops = registries.createSerializationContext(JsonOps.INSTANCE);
        for (var profile : GensokyoTreeProfile.values()) {
            if (!profile.settings().species().isMaple()) continue;
            assertTrue(mix.stream().anyMatch(e -> e.profile() == profile && e.weight() > 0), () -> "Tree missing from forest: " + profile);
            var feature = registries.lookupOrThrow(Registries.CONFIGURED_FEATURE).getOrThrow(GensokyoForestBootstrap.configured(profile.path())).value();
            var json = ConfiguredFeature.DIRECT_CODEC.encodeStart(ops, feature).getOrThrow().getAsJsonObject();
            assertEquals(profile.isAutumnMaple(), json.getAsJsonObject("config").has("crown_style"));
            var decoded = (GensokyoTreeConfiguration) ConfiguredFeature.DIRECT_CODEC.parse(ops, json).getOrThrow().config();
            assertEquals(profile.settings(), decoded.settings());
        }
    }

    @Test
    void configuredTreesRoundTripAndBadHeightBoundsFailDecode() {
        var registries = GensokyoForestPreviewExporter.registries();
        var ops = registries.createSerializationContext(JsonOps.INSTANCE);
        for (var profile : GensokyoTreeProfile.values()) {
            var tree = registries.lookupOrThrow(Registries.CONFIGURED_FEATURE).getOrThrow(GensokyoForestBootstrap.configured(profile.path())).value();
            var json = ConfiguredFeature.DIRECT_CODEC.encodeStart(ops, tree).getOrThrow();
            assertDoesNotThrow(() -> ConfiguredFeature.DIRECT_CODEC.parse(ops, json).getOrThrow());
            var invalid = JsonParser.parseString(json.toString()).getAsJsonObject();
            invalid.getAsJsonObject("config").addProperty("min_height", 39);
            invalid.getAsJsonObject("config").addProperty("max_height", 8);
            assertTrue(ConfiguredFeature.DIRECT_CODEC.parse(ops, invalid).error().isPresent());
        }
    }
}
