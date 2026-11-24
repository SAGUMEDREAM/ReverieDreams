package cc.thonly.reverie_dreams.world;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestBootstrap;
import dev.architectury.registry.level.biome.BiomeModifications;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

import net.minecraft.data.worldgen.placement.PlacementUtils;

public final class LargeBambooGeneration {
    public static final TagKey<Biome> HAS_SHOOTS = TagKey.create(Registries.BIOME, ReverieDreams.id("has_large_bamboo_shoots"));
    public static final TagKey<Biome> ABUNDANT_SHOOTS = TagKey.create(Registries.BIOME, ReverieDreams.id("abundant_large_bamboo_shoots"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH = GensokyoForestBootstrap.configured("large_bamboo/shoot_patch");
    public static final ResourceKey<PlacedFeature> SPARSE = GensokyoForestBootstrap.placed("large_bamboo/shoot_patch");
    public static final ResourceKey<PlacedFeature> ABUNDANT = GensokyoForestBootstrap.placed("large_bamboo/shoot_patch_abundant");

    public static void configured(BootstrapContext<ConfiguredFeature<?, ?>> context, Feature<NoneFeatureConfiguration> feature) {
        context.register(PATCH, new ConfiguredFeature<>(feature, NoneFeatureConfiguration.INSTANCE));
    }

    public static void placed(BootstrapContext<PlacedFeature> context) {
        var feature = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(PATCH);
        context.register(SPARSE, new PlacedFeature(feature, List.of(RarityFilter.onAverageOnceEvery(20), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome())));
        context.register(ABUNDANT, new PlacedFeature(feature, List.of(RarityFilter.onAverageOnceEvery(3), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome())));
    }

    public static void initialize() {
        BiomeModifications.addProperties((context, mutable) -> {
            if (context.hasTag(HAS_SHOOTS))
                mutable.getGenerationProperties().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION,
                        context.hasTag(ABUNDANT_SHOOTS) ? ABUNDANT : SPARSE);
        });
    }

    private LargeBambooGeneration() {
    }
}
