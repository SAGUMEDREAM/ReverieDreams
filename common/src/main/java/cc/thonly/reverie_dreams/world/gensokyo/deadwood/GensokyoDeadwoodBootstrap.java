package cc.thonly.reverie_dreams.world.gensokyo.deadwood;

import cc.thonly.reverie_dreams.ReverieDreams;

import cc.thonly.reverie_dreams.registry.content.block.RDDeadwoodBlocks;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Preview registration only; deliberately never appended to a biome's generation settings.
 */
public final class GensokyoDeadwoodBootstrap {
    public record Profile(GensokyoForestType theme, GensokyoDeadwoodConfiguration.Kind kind) {
        public String path() {
            return "gensokyo/deadwood/" + theme.name().toLowerCase(Locale.ROOT) + "/" + kind.name().toLowerCase(Locale.ROOT);
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
            for (var kind : GensokyoDeadwoodConfiguration.Kind.values())
                profiles.add(new Profile(theme, kind));
        PROFILES = List.copyOf(profiles);
    }

    public static List<String> woods(GensokyoForestType theme) {
        return switch (theme) {
            case FOREST -> List.of("oak", "oak", "birch");
            case BIRCH_FOREST -> List.of("birch");
            case TAIGA -> List.of("spruce");
            case CHERRY_GROVE -> List.of("cherry");
            case DARK_FOREST -> List.of("dark_oak");
            case MAGIC_FOREST -> List.of("oak", "dark_oak");
            // Fallen understory trees use existing split-log assets; no new wood registry is needed.
            case JUNGLE -> List.of("oak");
            case MAPLE_FOREST -> List.of("iroha_maple", "itaya_maple", "hauchiwa_maple");
        };
    }

    public static GensokyoDeadwoodConfiguration configuration(Profile profile) {
        var logs = new ArrayList<BlockState>();
        var caps = new ArrayList<BlockState>();
        for (String wood : woods(profile.theme)) {
            logs.add(state((wood.endsWith("maple") ? ReverieDreams.MOD_ID + ":" : "minecraft:") + wood + "_log"));
            caps.add(RDDeadwoodBlocks.SPLINTERED_LOGS.get(wood).defaultBlockState());
        }
        var litter = switch (profile.theme) {
            case CHERRY_GROVE -> states("pink_petals");
            case MAPLE_FOREST ->
                    states(ReverieDreams.MOD_ID + ":iroha_maple_leaf_litter", ReverieDreams.MOD_ID + ":itaya_maple_leaf_litter", ReverieDreams.MOD_ID + ":hauchiwa_maple_leaf_litter");
            default -> states("leaf_litter");
        };
        var accents = switch (profile.theme) {
            case TAIGA -> states("moss_carpet", "fern");
            case DARK_FOREST -> states("moss_carpet", "brown_mushroom", "red_mushroom");
            case MAGIC_FOREST ->
                    states("moss_carpet", ReverieDreams.MOD_ID + ":blue_mushroom", ReverieDreams.MOD_ID + ":purple_mushroom");
            case JUNGLE -> states("moss_carpet", "fern", "brown_mushroom");
            default -> List.<BlockState>of();
        };
        return new GensokyoDeadwoodConfiguration(profile.theme, profile.kind, List.copyOf(logs), List.copyOf(caps), litter, accents);
    }

    public static void configured(BootstrapContext<ConfiguredFeature<?, ?>> context, Feature<GensokyoDeadwoodConfiguration> feature) {
        for (var profile : PROFILES)
            context.register(profile.configured(), new ConfiguredFeature<>(feature, configuration(profile)));
    }

    public static void placed(BootstrapContext<PlacedFeature> context) {
        var lookup = context.lookup(Registries.CONFIGURED_FEATURE);
        for (var profile : PROFILES)
            context.register(profile.placed(), new PlacedFeature(lookup.getOrThrow(profile.configured()), List.of()));
    }

    private static List<BlockState> states(String... ids) {
        return Arrays.stream(ids).map(GensokyoDeadwoodBootstrap::state).toList();
    }

    private static BlockState state(String id) {
        return BuiltInRegistries.BLOCK.getOptional(Identifier.parse(id)).orElseThrow(() -> new IllegalStateException("Missing deadwood material: " + id)).defaultBlockState();
    }

    private GensokyoDeadwoodBootstrap() {
    }
}
