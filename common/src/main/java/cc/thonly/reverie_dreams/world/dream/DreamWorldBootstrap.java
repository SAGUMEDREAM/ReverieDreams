package cc.thonly.reverie_dreams.world.dream;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;

public final class DreamWorldBootstrap {
    public static final ResourceKey<ConfiguredFeature<?, ?>> ARCHIPELAGO = ResourceKey.create(
            Registries.CONFIGURED_FEATURE, ReverieDreams.id("dream_archipelago"));
    public static final ResourceKey<PlacedFeature> ARCHIPELAGO_PLACED = ResourceKey.create(
            Registries.PLACED_FEATURE, ReverieDreams.id("dream_archipelago"));

    public static void configured(BootstrapContext<ConfiguredFeature<?, ?>> context, Feature<NoneFeatureConfiguration> feature) {
        context.register(ARCHIPELAGO, new ConfiguredFeature<>(feature, NoneFeatureConfiguration.INSTANCE));
    }

    public static void placed(BootstrapContext<PlacedFeature> context) {
        context.register(ARCHIPELAGO_PLACED, new PlacedFeature(context.lookup(Registries.CONFIGURED_FEATURE)
                .getOrThrow(ARCHIPELAGO), List.of(BiomeFilter.biome())));
    }

    private DreamWorldBootstrap() {
    }
}
