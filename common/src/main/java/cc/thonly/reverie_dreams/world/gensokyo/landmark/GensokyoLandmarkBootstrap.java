package cc.thonly.reverie_dreams.world.gensokyo.landmark;

import cc.thonly.reverie_dreams.registry.content.block.RDLandmarkBlocks;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoTreeConfiguration;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoTreeShape;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

import cc.thonly.reverie_dreams.ReverieDreams;

/**
 * All lifecycle calls originate in the existing RDBuiltin* bootstrap methods.
 */
public final class GensokyoLandmarkBootstrap {
    public static final ResourceKey<ConfiguredFeature<?, ?>> PURPLE_CHERRY = GensokyoForestBootstrap.configured("gensokyo/landmarks/purple_cherry");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MEADOW_OAK = GensokyoForestBootstrap.configured("gensokyo/landmarks/meadow_oak");

    public static ResourceKey<Biome> biome(LandmarkVegetationConfiguration.Theme theme) {
        return ResourceKey.create(Registries.BIOME, ReverieDreams.id("gensokyo_" + theme.getSerializedName()));
    }

    public static String path(LandmarkVegetationConfiguration.Theme theme) {
        return "gensokyo/landmarks/" + theme.getSerializedName() + "/vegetation";
    }

    public static GensokyoTreeConfiguration purpleCherry() {
        return new GensokyoTreeConfiguration(Blocks.CHERRY_LOG.defaultBlockState(), RDLandmarkBlocks.PURPLE_CHERRY_LEAVES.defaultBlockState(),
                GensokyoTreeShape.Species.CHERRY, 10, 17, 1, 4);
    }

    public static void configured(BootstrapContext<ConfiguredFeature<?, ?>> context, Feature<LandmarkVegetationConfiguration> vegetation,
                                  Feature<GensokyoTreeConfiguration> trees, Feature<MuenzukaGraveConfiguration> graves) {
        for (var theme : LandmarkVegetationConfiguration.Theme.values())
            context.register(GensokyoForestBootstrap.configured(path(theme)),
                    new ConfiguredFeature<>(vegetation, new LandmarkVegetationConfiguration(theme)));
        for (var kind : MuenzukaGraveConfiguration.Kind.values())
            context.register(GensokyoForestBootstrap.configured(
                    MuenzukaGraveFeature.PATH + (kind == MuenzukaGraveConfiguration.Kind.RANDOM ? "" : "_" + kind.getSerializedName())), new ConfiguredFeature<>(graves, new MuenzukaGraveConfiguration(kind)));
        context.register(PURPLE_CHERRY, new ConfiguredFeature<>(trees, purpleCherry()));
        context.register(MEADOW_OAK, new ConfiguredFeature<>(trees, new GensokyoTreeConfiguration(Blocks.OAK_LOG.defaultBlockState(),
                Blocks.OAK_LEAVES.defaultBlockState(), GensokyoTreeShape.Species.OAK, 10, 16, 1, 3)));
    }

    public static void placed(BootstrapContext<PlacedFeature> context) {
        var lookup = context.lookup(Registries.CONFIGURED_FEATURE);
        context.register(GensokyoForestBootstrap.placed(MuenzukaGraveFeature.PATH), new PlacedFeature(
                lookup.getOrThrow(GensokyoForestBootstrap.configured(MuenzukaGraveFeature.PATH)), List.of(InSquarePlacement.spread(), BiomeFilter.biome())));
        for (var theme : LandmarkVegetationConfiguration.Theme.values())
            context.register(GensokyoForestBootstrap.placed(path(theme)),
                    new PlacedFeature(lookup.getOrThrow(GensokyoForestBootstrap.configured(path(theme))), List.of(InSquarePlacement.spread(), BiomeFilter.biome())));
        context.register(GensokyoForestBootstrap.placed("gensokyo/landmarks/purple_cherry"), new PlacedFeature(lookup.getOrThrow(PURPLE_CHERRY), List.of(
                RarityFilter.onAverageOnceEvery(3), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome())));
        context.register(GensokyoForestBootstrap.placed("gensokyo/landmarks/meadow_oak"), new PlacedFeature(lookup.getOrThrow(MEADOW_OAK), List.of(
                RarityFilter.onAverageOnceEvery(12), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome())));
    }

    public static void biomes(BootstrapContext<Biome> context) {
        var features = context.lookup(Registries.PLACED_FEATURE);
        var carvers = context.lookup(Registries.CONFIGURED_CARVER);
        Biome template = OverworldBiomes.plains(features, carvers, false, false, false);
        for (var theme : LandmarkVegetationConfiguration.Theme.values()) {
            var generation = new BiomeGenerationSettings.PlainBuilder();
            template.getGenerationSettings().getCarvers().forEach(generation::addCarver);
            var steps = template.getGenerationSettings().features();
            for (int step = 0; step < steps.size(); step++)
                if (step != GenerationStep.Decoration.VEGETAL_DECORATION.ordinal())
                    for (var feature : steps.get(step))
                        generation.addFeature(step, feature);
            if (theme != LandmarkVegetationConfiguration.Theme.GARDEN_OF_THE_SUN)
                generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION.ordinal(),
                        features.getOrThrow(GensokyoForestBootstrap.placed("gensokyo/landmarks/" + (theme == LandmarkVegetationConfiguration.Theme.MUENZUKA ? "purple_cherry" : "meadow_oak"))));
            if (theme == LandmarkVegetationConfiguration.Theme.MUENZUKA)
                generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION.ordinal(),
                        features.getOrThrow(GensokyoForestBootstrap.placed(MuenzukaGraveFeature.PATH)));
            generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION.ordinal(), features.getOrThrow(GensokyoForestBootstrap.placed(path(theme))));
            int grass = switch (theme) {
                case PHANTOM_MEADOW -> 0x86AA58;
                case MUENZUKA -> 0x68765D;
                case GARDEN_OF_THE_SUN -> 0x8DA34D;
            };
            var spawns = new MobSpawnSettings.Builder();
            MobSpawnSettings mobs = theme == LandmarkVegetationConfiguration.Theme.PHANTOM_MEADOW ? template.getMobSettings() : spawns.build();
            context.register(biome(theme), new Biome.BiomeBuilder().hasPrecipitation(true).temperature(theme == LandmarkVegetationConfiguration.Theme.MUENZUKA ? .6F : .85F)
                    .downfall(.65F).putAttributes(template.getAttributes()).specialEffects(new BiomeSpecialEffects.Builder()
                            .waterColor(theme == LandmarkVegetationConfiguration.Theme.MUENZUKA ? 0x687385 : 0x448C9D).grassColorOverride(grass)
                            .foliageColorOverride(theme == LandmarkVegetationConfiguration.Theme.MUENZUKA ? 0xA183B4 : grass).build())
                    .mobSpawnSettings(mobs).generationSettings(generation.build()).build());
        }
    }

    private GensokyoLandmarkBootstrap() {
    }
}
