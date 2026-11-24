package cc.thonly.reverie_dreams.world.gen.biome;

import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestBootstrap;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Open ordinary grassland; Phantom Meadow keeps its separate authored flowers and identity.
 */
public final class GensokyoPlainsBiomeCreator {
    public static final int GRASS_COLOR = 0x83AD58;
    public static final int FOLIAGE_COLOR = 0x709D4D;

    public static Biome create(HolderGetter<PlacedFeature> features, HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        Biome template = OverworldBiomes.plains(features, carvers, false, false, false);
        var generation = new BiomeGenerationSettings.PlainBuilder();
        template.getGenerationSettings().getCarvers().forEach(generation::addCarver);
        var steps = template.getGenerationSettings().features();
        for (int step = 0; step < steps.size(); step++) {
            for (var feature : steps.get(step)) {
                if (feature.unwrapKey().filter(VegetationPlacements.TREES_PLAINS::equals).isPresent()) {
                    generation.addFeature(step, features.getOrThrow(GensokyoForestBootstrap.placed("gensokyo/landmarks/meadow_oak")));
                } else
                    generation.addFeature(step, feature);
            }
        }
        return new Biome.BiomeBuilder().hasPrecipitation(true).temperature(.85F).downfall(.6F)
                .putAttributes(template.getAttributes())
                .specialEffects(new BiomeSpecialEffects.Builder().waterColor(0x448C9D)
                        .grassColorOverride(GRASS_COLOR).foliageColorOverride(FOLIAGE_COLOR).build())
                .mobSpawnSettings(template.getMobSettings()).generationSettings(generation.build()).build();
    }

    private GensokyoPlainsBiomeCreator() {
    }
}
