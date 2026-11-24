package cc.thonly.reverie_dreams.world.gen.biome;

import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestType;
import cc.thonly.reverie_dreams.world.gensokyo.understory.GensokyoUnderstoryBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.understory.MagicMushroomBootstrap;
import cc.thonly.reverie_dreams.world.gen.RDBuiltinPlacedFeatures;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class MagicForestBiomeCreator {
    private MagicForestBiomeCreator() {
    }

    public static Biome create(HolderGetter<PlacedFeature> features, HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        Biome template = OverworldBiomes.darkForest(features, carvers, false);
        var generation = new BiomeGenerationSettings.PlainBuilder();
        template.getGenerationSettings().getCarvers().forEach(generation::addCarver);
        var steps = template.getGenerationSettings().features();
        for (int step = 0; step < steps.size(); step++) {
            for (var feature : steps.get(step)) {
                generation.addFeature(step, feature);
            }
        }

        int vegetation = GenerationStep.Decoration.VEGETAL_DECORATION.ordinal();
        for (var profile : GensokyoUnderstoryBootstrap.PROFILES) {
            if (profile.theme() == GensokyoForestType.MAGIC_FOREST) {
                generation.addFeature(vegetation, features.getOrThrow(profile.placed()));
            }
        }
        generation.addFeature(vegetation, features.getOrThrow(MagicMushroomBootstrap.GIANTS));
        generation.addFeature(vegetation, features.getOrThrow(RDBuiltinPlacedFeatures.FICUS_MICROCARPA_KEY));
        generation.addFeature(vegetation, features.getOrThrow(RDBuiltinPlacedFeatures.PUFF_YO_BUSH_KEY));

        return new Biome.BiomeBuilder()
                .hasPrecipitation(template.hasPrecipitation())
                .temperature(template.getBaseTemperature())
                .downfall(GensokyoForestType.MAGIC_FOREST.downfall())
                .putAttributes(template.getAttributes())
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(GensokyoForestBootstrap.MAGIC_WATER_COLOR)
                        .grassColorOverride(GensokyoForestBootstrap.MAGIC_GRASS_COLOR)
                        .foliageColorOverride(GensokyoForestBootstrap.MAGIC_FOLIAGE_COLOR)
                        .build())
                .mobSpawnSettings(template.getMobSettings())
                .generationSettings(generation.build())
                .build();
    }
}
