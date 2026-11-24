package cc.thonly.reverie_dreams.world.gensokyo.tree;

import cc.thonly.reverie_dreams.ReverieDreams;

import net.minecraft.core.Holder;
import cc.thonly.reverie_dreams.world.gensokyo.understory.GensokyoUnderstoryBootstrap;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import cc.thonly.reverie_dreams.world.gen.RDBuiltinPlacedFeatures;
import cc.thonly.reverie_dreams.world.gen.biome.MapleForestBiomeCreator;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.NoiseBasedCountPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cc.thonly.reverie_dreams.world.gensokyo.rock.GensokyoRockBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.understory.MagicMushroomBootstrap;

/**
 * Standalone variants: registering these never adds them to a world or alters a vanilla biome.
 */
public final class GensokyoForestBootstrap {
    public static final int MAPLE_GRASS_COLOR = 0x879B55;
    public static final int MAPLE_FOLIAGE_COLOR = 0x668347;
    public static final int MAGIC_GRASS_COLOR = 0x4E8A72;
    public static final int MAGIC_FOLIAGE_COLOR = 0x4F743C;
    public static final int MAGIC_WATER_COLOR = 0x3F6875;
    public static final int JUNGLE_GRASS_COLOR = 0x709B50;
    public static final int JUNGLE_FOLIAGE_COLOR = 0x4D7E3B;
    private static final Set<ResourceKey<PlacedFeature>> TEMPLATE_TREES = Set.of(
            VegetationPlacements.TREES_BIRCH_AND_OAK_LEAF_LITTER, VegetationPlacements.TREES_BIRCH,
            VegetationPlacements.BIRCH_TALL, VegetationPlacements.TREES_TAIGA,
            VegetationPlacements.TREES_CHERRY, VegetationPlacements.DARK_FOREST_VEGETATION,
            placed("iroha_maple_tree_placed"), placed("itaya_maple_tree_placed"), placed("hauchiwa_maple_tree_placed"));

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("reverie_dreams", path);
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> configured(String path) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, id(path));
    }

    public static ResourceKey<PlacedFeature> placed(String path) {
        return ResourceKey.create(Registries.PLACED_FEATURE, id(path));
    }

    public static ResourceKey<Biome> biome(GensokyoForestType type) {
        return ResourceKey.create(Registries.BIOME, id(type.biomePath()));
    }

    public static void configured(BootstrapContext<ConfiguredFeature<?, ?>> context, Feature<GensokyoTreeConfiguration> feature) {
        for (GensokyoTreeProfile profile : GensokyoTreeProfile.values()) {
            context.register(configured(profile.path()), new ConfiguredFeature<>(feature, configuration(profile)));
        }
        var lookup = context.lookup(Registries.CONFIGURED_FEATURE);
        for (GensokyoForestType type : GensokyoForestType.values()) {
            List<WeightedPlacedFeature> choices = new ArrayList<>();
            float remaining = 1;
            for (int index = 0; index < type.mix().size() - 1; index++) {
                var entry = type.mix().get(index);
                choices.add(new WeightedPlacedFeature(Holder.direct(new PlacedFeature(
                        lookup.getOrThrow(configured(entry.profile().path())), List.of())), entry.weight() / remaining));
                remaining -= entry.weight();
            }
            var fallback = type.mix().getLast().profile();
            context.register(configured(type.treesPath()), new ConfiguredFeature<>(Feature.RANDOM_SELECTOR,
                    new RandomFeatureConfiguration(choices, Holder.direct(new PlacedFeature(
                            lookup.getOrThrow(configured(fallback.path())), List.of())))));
        }
    }

    public static GensokyoTreeConfiguration configuration(GensokyoTreeProfile profile) {
        String material = switch (profile.settings().species()) {
            case OAK -> "minecraft:oak";
            case DARK_OAK -> "minecraft:dark_oak";
            case BIRCH -> "minecraft:birch";
            case SPRUCE -> "minecraft:spruce";
            case CHERRY -> "minecraft:cherry";
            case JUNGLE -> "minecraft:jungle";
            case IROHA_MAPLE -> ReverieDreams.MOD_ID + ":iroha_maple";
            case ITAYA_MAPLE -> ReverieDreams.MOD_ID + ":itaya_maple";
            case HAUCHIWA_MAPLE -> ReverieDreams.MOD_ID + ":hauchiwa_maple";
        };
        Block log = material(material + "_log");
        Block leaves = material(material + "_leaves");
        var settings = profile.settings();
        return new GensokyoTreeConfiguration(log.defaultBlockState(), leaves.defaultBlockState(), settings.species(),
                settings.minHeight(), settings.maxHeight(), settings.trunkWidth(), settings.crownRadius(), settings.crownStyle());
    }

    private static Block material(String id) {
        return BuiltInRegistries.BLOCK.getOptional(Identifier.parse(id))
                .orElseThrow(() -> new IllegalStateException("Missing tree material: " + id));
    }

    public static void placed(BootstrapContext<PlacedFeature> context) {
        var lookup = context.lookup(Registries.CONFIGURED_FEATURE);
        for (GensokyoForestType type : GensokyoForestType.values()) {
            context.register(placed(type.treesPath()), new PlacedFeature(lookup.getOrThrow(configured(type.treesPath())), List.of(
                    NoiseBasedCountPlacement.of(type.densityScale(), 96.0,
                            type == GensokyoForestType.MAGIC_FOREST ? 0.85 : 0.70), InSquarePlacement.spread(),
                    PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome())));
        }
    }

    public static void biomes(BootstrapContext<Biome> context) {
        var features = context.lookup(Registries.PLACED_FEATURE);
        var carvers = context.lookup(Registries.CONFIGURED_CARVER);
        for (GensokyoForestType type : GensokyoForestType.values()) {
            Biome template = switch (type) {
                // Temperate evergreen understory, without vanilla bamboo, melon or tropical animal layers.
                case FOREST, MAGIC_FOREST, JUNGLE -> OverworldBiomes.forest(features, carvers, false, false, false);
                case BIRCH_FOREST -> OverworldBiomes.forest(features, carvers, true, false, false);
                case TAIGA -> OverworldBiomes.taiga(features, carvers, false);
                case CHERRY_GROVE -> OverworldBiomes.meadowOrCherryGrove(features, carvers, true);
                case DARK_FOREST -> OverworldBiomes.darkForest(features, carvers, false);
                case MAPLE_FOREST -> MapleForestBiomeCreator.createMapleForest(features, carvers);
            };
            var generation = new BiomeGenerationSettings.PlainBuilder();
            template.getGenerationSettings().getCarvers().forEach(generation::addCarver);
            var steps = template.getGenerationSettings().features();
            int replaced = 0;
            for (int step = 0; step < steps.size(); step++) {
                for (var entry : steps.get(step)) {
                    if (entry.unwrapKey().map(TEMPLATE_TREES::contains).orElse(false)) {
                        if (replaced == 0)
                            generation.addFeature(step, features.getOrThrow(placed(type.treesPath())));
                        replaced++;
                    } else
                        generation.addFeature(step, entry);
                }
            }
            int expected = type == GensokyoForestType.MAPLE_FOREST ? 3 : 1;
            if (replaced != expected)
                throw new IllegalStateException("Expected " + expected + " tree layers for " + type + ", got " + replaced);
            for (var profile : GensokyoRockBootstrap.PROFILES) {
                if (profile.theme() == type)
                    generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION.ordinal(), features.getOrThrow(profile.placed()));
            }
            for (var profile : GensokyoUnderstoryBootstrap.PROFILES) {
                if (profile.theme() == type)
                    generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION.ordinal(), features.getOrThrow(profile.placed()));
            }
            if (type == GensokyoForestType.MAGIC_FOREST) {
                generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION.ordinal(),
                        features.getOrThrow(MagicMushroomBootstrap.GIANTS));
                generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION.ordinal(),
                        features.getOrThrow(RDBuiltinPlacedFeatures.FICUS_MICROCARPA_KEY));
                generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION.ordinal(),
                        features.getOrThrow(RDBuiltinPlacedFeatures.PUFF_YO_BUSH_KEY));
            }
            if (type == GensokyoForestType.JUNGLE) {
                generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION.ordinal(),
                        features.getOrThrow(cc.thonly.reverie_dreams.world.gensokyo.jungle.GensokyoJungleDecoration.VINES));
                generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION.ordinal(),
                        features.getOrThrow(cc.thonly.reverie_dreams.world.gensokyo.jungle.GensokyoJungleDecoration.FALLEN_LOG));
            }
            BiomeSpecialEffects effects = switch (type) {
                case JUNGLE -> new BiomeSpecialEffects.Builder().waterColor(0x426E78)
                        .grassColorOverride(JUNGLE_GRASS_COLOR).foliageColorOverride(JUNGLE_FOLIAGE_COLOR).build();
                case MAGIC_FOREST -> new BiomeSpecialEffects.Builder().waterColor(MAGIC_WATER_COLOR)
                        .grassColorOverride(MAGIC_GRASS_COLOR).foliageColorOverride(MAGIC_FOLIAGE_COLOR).build();
                case MAPLE_FOREST ->
                        new BiomeSpecialEffects.Builder().waterColor(template.getSpecialEffects().waterColor())
                                .grassColorOverride(MAPLE_GRASS_COLOR).foliageColorOverride(MAPLE_FOLIAGE_COLOR).build();
                default -> template.getSpecialEffects();
            };
            // This conifer forest is temperate. Vanilla taiga's 0.25 makes the
            // authored mountain foothills snowy after normal altitude cooling.
            float temperature = type == GensokyoForestType.TAIGA ? 0.6F
                    : type == GensokyoForestType.JUNGLE ? 0.85F : template.getBaseTemperature();
            context.register(biome(type), new Biome.BiomeBuilder().hasPrecipitation(template.hasPrecipitation())
                    .temperature(temperature).downfall(type.downfall()).putAttributes(template.getAttributes())
                    .specialEffects(effects).mobSpawnSettings(template.getMobSettings()).generationSettings(generation.build()).build());
        }
    }

    private GensokyoForestBootstrap() {
    }
}
