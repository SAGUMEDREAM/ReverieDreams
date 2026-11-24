package cc.thonly.reverie_dreams.world.gensokyo.understory;

import cc.thonly.reverie_dreams.ReverieDreams;

import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestBootstrap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;

public final class MagicMushroomBootstrap {
    public static final ResourceKey<ConfiguredFeature<?, ?>> HUGE_BLUE = GensokyoForestBootstrap.configured("gensokyo/huge_blue_mushroom");
    public static final ResourceKey<ConfiguredFeature<?, ?>> HUGE_PURPLE = GensokyoForestBootstrap.configured("gensokyo/huge_purple_mushroom");
    public static final ResourceKey<PlacedFeature> GIANTS = GensokyoForestBootstrap.placed("gensokyo/magic_mushroom_giants");

    public static void configured(BootstrapContext<ConfiguredFeature<?, ?>> context, Feature<HugeMushroomFeatureConfiguration> feature) {
        var stem = BuiltInRegistries.BLOCK.getValue(Identifier.parse(ReverieDreams.MOD_ID + ":magic_mushroom_stem"));
        for (boolean blue : new boolean[]{true, false}) {
            var cap = BuiltInRegistries.BLOCK.getValue(Identifier.parse(ReverieDreams.MOD_ID + ":" + (blue ? "blue" : "purple") + "_mushroom_block"));
            context.register(blue ? HUGE_BLUE : HUGE_PURPLE, new ConfiguredFeature<>(feature, new HugeMushroomFeatureConfiguration(
                    BlockStateProvider.simple(cap), BlockStateProvider.simple(stem), blue ? 3 : 2,
                    BlockPredicate.matchesTag(BlockTags.SUBSTRATE_OVERWORLD))));
        }
        var lookup = context.lookup(Registries.CONFIGURED_FEATURE);
        context.register(GensokyoForestBootstrap.configured("gensokyo/magic_mushroom_giants"), new ConfiguredFeature<>(Feature.RANDOM_BOOLEAN_SELECTOR,
                new RandomBooleanFeatureConfiguration(
                        Holder.direct(new PlacedFeature(lookup.getOrThrow(HUGE_BLUE), List.of())),
                        Holder.direct(new PlacedFeature(lookup.getOrThrow(HUGE_PURPLE), List.of())))));
    }

    public static void placed(BootstrapContext<PlacedFeature> context) {
        context.register(GIANTS, new PlacedFeature(context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(
                GensokyoForestBootstrap.configured("gensokyo/magic_mushroom_giants")), List.of(
                RarityFilter.onAverageOnceEvery(8), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_NO_LEAVES, BiomeFilter.biome())));
    }

    private MagicMushroomBootstrap() {
    }
}
