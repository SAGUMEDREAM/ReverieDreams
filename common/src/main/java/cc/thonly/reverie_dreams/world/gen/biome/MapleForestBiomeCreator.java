package cc.thonly.reverie_dreams.world.gen.biome;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.world.gen.RDBuiltinPlacedFeatures;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.CavePlacements;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Datagen bootstrap for the TerraBlender maple forest region.
 *
 * <p>The placed features are intentionally the same set that the hand-written
 * biome used before it was moved into the registry bootstrap. Keeping the
 * bootstrap here means {@code runData} owns the biome JSON as well as the
 * generated dimension data.</p>
 */
@SuppressWarnings("unchecked")
public final class MapleForestBiomeCreator {
    public static Biome createMapleForest(
            HolderGetter<PlacedFeature> featureLookup,
            HolderGetter<ConfiguredWorldCarver<?>> carverLookup
    ) {
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(featureLookup, carverLookup);
        var features = new ResourceKey[]{
                CavePlacements.GLOW_LICHEN,
                VegetationPlacements.PATCH_TALL_GRASS_2,
                VegetationPlacements.FOREST_FLOWERS,
                RDBuiltinPlacedFeatures.IROHA_MAPLE_TREE_KEY,
                RDBuiltinPlacedFeatures.ITAYA_MAPLE_TREE_KEY,
                RDBuiltinPlacedFeatures.HAUCHIWA_MAPLE_TREE_KEY,
                VegetationPlacements.PATCH_BUSH,
                VegetationPlacements.FLOWER_DEFAULT,
                VegetationPlacements.PATCH_GRASS_FOREST,
                VegetationPlacements.BROWN_MUSHROOM_NORMAL,
                RDBuiltinPlacedFeatures.MAPLE_LEAF_LITTER_KEY,
                VegetationPlacements.PATCH_PUMPKIN,
                VegetationPlacements.PATCH_SUGAR_CANE,
                VegetationPlacements.PATCH_FIREFLY_BUSH_NEAR_WATER,
        };
        for (ResourceKey<PlacedFeature> feature : features) {
            generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, feature);
        }

        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        spawns.creatureGenerationProbability(0.1F);

        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(0.7F)
                .downfall(0.6F)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(0x3F6F9A)
                        .foliageColorOverride(0xD4692E)
                        .grassColorOverride(0xC78A3A)
                        .build())
                .mobSpawnSettings(spawns.build())
                .generationSettings(generation.build())
                .build();
    }

}
