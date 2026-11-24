package cc.thonly.reverie_dreams.world.gensokyo.rock;

import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestType;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Registration stays behind the existing RDBuiltin* entry points.
 */
public final class GensokyoRockBootstrap {
    public record Profile(GensokyoForestType theme, GensokyoRockConfiguration.Kind kind) {
        public String path() {
            return "gensokyo/rocks/" + theme.name().toLowerCase(Locale.ROOT) + "/" + kind.name().toLowerCase(Locale.ROOT);
        }

        public ResourceKey<ConfiguredFeature<?, ?>> configured() {
            return GensokyoForestBootstrap.configured(path());
        }

        public ResourceKey<PlacedFeature> placed() {
            return GensokyoForestBootstrap.placed(path());
        }

        public int rarity() {
            int[] rates = switch (theme) {
                case FOREST -> new int[]{8, 40, 384};
                case BIRCH_FOREST -> new int[]{16, 72, 640};
                case TAIGA -> new int[]{6, 28, 256};
                case CHERRY_GROVE -> new int[]{20, 96, 768};
                case DARK_FOREST -> new int[]{8, 36, 384};
                case MAGIC_FOREST -> new int[]{10, 48, 384};
                case MAPLE_FOREST -> new int[]{12, 56, 512};
                case JUNGLE -> new int[]{12, 64, 768};
            };
            return rates[kind.ordinal()];
        }
    }

    public static final List<Profile> PROFILES;

    static {
        var list = new ArrayList<Profile>();
        for (var theme : GensokyoForestType.values())
            for (var kind : GensokyoRockConfiguration.Kind.values())
                list.add(new Profile(theme, kind));
        PROFILES = List.copyOf(list);
    }

    public static GensokyoRockConfiguration configuration(Profile profile) {
        float moss = switch (profile.theme()) {
            case FOREST -> .20F;
            case BIRCH_FOREST -> .07F;
            case TAIGA -> .35F;
            case CHERRY_GROVE -> .06F;
            case DARK_FOREST -> .38F;
            case MAGIC_FOREST -> .42F;
            case MAPLE_FOREST -> .14F;
            case JUNGLE -> .58F;
        };
        boolean dark = profile.theme() == GensokyoForestType.TAIGA || profile.theme() == GensokyoForestType.DARK_FOREST;
        return new GensokyoRockConfiguration(profile.theme(), profile.kind(),
                (dark ? Blocks.ANDESITE : Blocks.STONE).defaultBlockState(), (dark ? Blocks.STONE : Blocks.ANDESITE).defaultBlockState(),
                Blocks.MOSSY_COBBLESTONE.defaultBlockState(), moss);
    }

    public static void configured(BootstrapContext<ConfiguredFeature<?, ?>> context, Feature<GensokyoRockConfiguration> feature) {
        for (var profile : PROFILES)
            context.register(profile.configured(), new ConfiguredFeature<>(feature, configuration(profile)));
    }

    public static void placed(BootstrapContext<PlacedFeature> context) {
        var lookup = context.lookup(Registries.CONFIGURED_FEATURE);
        for (var profile : PROFILES)
            context.register(profile.placed(), new PlacedFeature(lookup.getOrThrow(profile.configured()), List.of(
                    RarityFilter.onAverageOnceEvery(profile.rarity()), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome())));
    }

    private GensokyoRockBootstrap() {
    }
}
