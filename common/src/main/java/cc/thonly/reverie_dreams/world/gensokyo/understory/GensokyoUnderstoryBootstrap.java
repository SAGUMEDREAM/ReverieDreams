package cc.thonly.reverie_dreams.world.gensokyo.understory;

import cc.thonly.reverie_dreams.ReverieDreams;

import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Arrays;

/**
 * Invoked by the existing RDBuiltin* bootstraps; no independent registry lifecycle.
 */
public final class GensokyoUnderstoryBootstrap {
    public record Profile(GensokyoForestType theme, GensokyoUnderstoryConfiguration.Kind kind) {
        public String path() {
            return "gensokyo/understory/" + theme.name().toLowerCase(Locale.ROOT) + "/" + kind.name().toLowerCase(Locale.ROOT);
        }

        public ResourceKey<ConfiguredFeature<?, ?>> configured() {
            return GensokyoForestBootstrap.configured(path());
        }

        public ResourceKey<PlacedFeature> placed() {
            return GensokyoForestBootstrap.placed(path());
        }
    }

    public static final List<Profile> PROFILES;

    static {
        var profiles = new ArrayList<Profile>();
        for (var theme : GensokyoForestType.values())
            for (var kind : GensokyoUnderstoryConfiguration.Kind.values())
                profiles.add(new Profile(theme, kind));
        PROFILES = List.copyOf(profiles);
    }

    public static void configured(BootstrapContext<ConfiguredFeature<?, ?>> context, Feature<GensokyoUnderstoryConfiguration> feature) {
        for (var profile : PROFILES)
            context.register(profile.configured(), new ConfiguredFeature<>(feature, configuration(profile)));
    }

    public static void placed(BootstrapContext<PlacedFeature> context) {
        var lookup = context.lookup(Registries.CONFIGURED_FEATURE);
        for (var profile : PROFILES) {
            var density = GensokyoUnderstoryDistribution.density(profile.theme());
            var modifiers = new ArrayList<PlacementModifier>();
            switch (profile.kind()) {
                case LEAF_PATCH -> modifiers.add(CountPlacement.of(density.leafAttempts()));
                case SHRUB -> modifiers.add(RarityFilter.onAverageOnceEvery(density.shrubRarity()));
                case THICKET -> modifiers.add(RarityFilter.onAverageOnceEvery(density.thicketRarity()));
                case FLOOR_PATCH -> modifiers.add(CountPlacement.of(density.floorAttempts()));
            }
            modifiers.add(InSquarePlacement.spread());
            modifiers.add(PlacementUtils.HEIGHTMAP_OCEAN_FLOOR);
            modifiers.add(BiomeFilter.biome());
            context.register(profile.placed(), new PlacedFeature(lookup.getOrThrow(profile.configured()), modifiers));
        }
    }

    public static GensokyoUnderstoryConfiguration configuration(Profile profile) {
        String[] woods = switch (profile.theme()) {
            case FOREST -> new String[]{"oak", "oak", "birch"};
            case BIRCH_FOREST -> new String[]{"birch"};
            case TAIGA -> new String[]{"spruce"};
            case CHERRY_GROVE -> new String[]{"oak", "oak", "oak", "cherry"};
            case DARK_FOREST -> new String[]{"dark_oak", "dark_oak", "oak"};
            case MAGIC_FOREST -> new String[]{"dark_oak", "oak"};
            case JUNGLE -> new String[]{"jungle", "jungle", "oak"};
            case MAPLE_FOREST ->
                    new String[]{ReverieDreams.MOD_ID + ":iroha_maple", ReverieDreams.MOD_ID + ":itaya_maple", ReverieDreams.MOD_ID + ":hauchiwa_maple", "oak"};
        };
        List<BlockState> logs = new ArrayList<>(), leaves = new ArrayList<>();
        for (String wood : woods) {
            logs.add(state(wood + "_log"));
            leaves.add(state(wood + "_leaves"));
        }
        var flowers = switch (profile.theme()) {
            case FOREST -> states("dandelion", "poppy", "oxeye_daisy");
            case BIRCH_FOREST -> states("azure_bluet", "oxeye_daisy", "lily_of_the_valley");
            case TAIGA -> states("fern", "fern", "brown_mushroom");
            case CHERRY_GROVE -> states("pink_petals", "pink_petals", "pink_tulip");
            case DARK_FOREST -> states("brown_mushroom", "red_mushroom");
            case MAGIC_FOREST ->
                    states("brown_mushroom", "red_mushroom", ReverieDreams.MOD_ID + ":blue_mushroom", ReverieDreams.MOD_ID + ":purple_mushroom", "allium", "cornflower");
            case MAPLE_FOREST -> states("dandelion", "poppy", "brown_mushroom");
            case JUNGLE -> states("fern", "fern", "lily_of_the_valley", "brown_mushroom");
        };
        var litter = switch (profile.theme()) {
            case CHERRY_GROVE -> states("pink_petals");
            case MAPLE_FOREST ->
                    states(ReverieDreams.MOD_ID + ":iroha_maple_leaf_litter", ReverieDreams.MOD_ID + ":itaya_maple_leaf_litter", ReverieDreams.MOD_ID + ":hauchiwa_maple_leaf_litter");
            case TAIGA, JUNGLE -> states("moss_carpet", "leaf_litter");
            default -> states("leaf_litter");
        };
        var accents = switch (profile.theme()) {
            case TAIGA -> states("fern", "fern", "moss_carpet", "moss_carpet", "mossy_cobblestone");
            case DARK_FOREST -> states("fern", "brown_mushroom", "red_mushroom");
            case MAGIC_FOREST ->
                    states("fern", "brown_mushroom", "red_mushroom", ReverieDreams.MOD_ID + ":blue_mushroom", ReverieDreams.MOD_ID + ":purple_mushroom");
            case JUNGLE -> states("fern", "fern", "moss_carpet", "short_grass", "brown_mushroom");
            default -> states("short_grass", "short_grass", "fern");
        };
        return new GensokyoUnderstoryConfiguration(profile.theme(), profile.kind(), List.copyOf(logs), List.copyOf(leaves), flowers, litter, accents);
    }

    private static List<BlockState> states(String... ids) {
        return Arrays.stream(ids).map(GensokyoUnderstoryBootstrap::state).toList();
    }

    private static BlockState state(String id) {
        return BuiltInRegistries.BLOCK.getOptional(Identifier.parse(id)).orElseThrow(() -> new IllegalStateException("Missing understory material: " + id)).defaultBlockState();
    }

    private GensokyoUnderstoryBootstrap() {
    }
}
