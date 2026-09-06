package cc.thonly.reverie_dreams.world.gen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

public class RDBuiltinPlacedFeatures {
    public static final ResourceKey<PlacedFeature> DREAM_WORLD_GRID_KEY = getOrCreateRegistryKey("dream_world_grid");
    public static final ResourceKey<PlacedFeature> CRATER_MEGA_KEY = getOrCreateRegistryKey("crater_mega");
    public static final ResourceKey<PlacedFeature> CRATER_LARGE_KEY = getOrCreateRegistryKey("crater_large");
    public static final ResourceKey<PlacedFeature> CRATER_SMALL_KEY = getOrCreateRegistryKey("crater_small");
    public static final ResourceKey<PlacedFeature> MOON_IRON_ORE_KEY = getOrCreateRegistryKey("moon_iron_ore");
    public static final ResourceKey<PlacedFeature> MOON_GOLD_ORE_KEY = getOrCreateRegistryKey("moon_gold_ore");
    public static final ResourceKey<PlacedFeature> MOON_DIAMOND_ORE_KEY = getOrCreateRegistryKey("moon_diamond_ore");
    public static final ResourceKey<PlacedFeature> MOON_QUARTZ_ORE_KEY = getOrCreateRegistryKey("moon_quartz_ore");
    public static final ResourceKey<PlacedFeature> MOON_DIORITE_PLACED_KEY = getOrCreateRegistryKey("moon_diorite");
    public static final ResourceKey<PlacedFeature> MOON_WATER_LAKE_KEY = getOrCreateRegistryKey("moon_water_lake");
    public static final ResourceKey<PlacedFeature> MOON_GRAVEL_PLACED_KEY = getOrCreateRegistryKey("moon_gravel");

    public static final ResourceKey<PlacedFeature> FLOATING_DREAM_STONE_KEY = getOrCreateRegistryKey("floating_dream_stone_placed");
    public static final ResourceKey<PlacedFeature> FLOATING_DREAM_CRYSTAL_KEY = getOrCreateRegistryKey("floating_dream_crystal_placed");
    public static final ResourceKey<PlacedFeature> FLOATING_DREAM_TRIAL_ROOM_ZOMBIE_KEY = getOrCreateRegistryKey("floating_dream_trial_room_zombie_placed");
    public static final ResourceKey<PlacedFeature> FLOATING_DREAM_TRIAL_ROOM_SKELETON_KEY = getOrCreateRegistryKey("floating_dream_trial_room_skeleton_placed");
    public static final ResourceKey<PlacedFeature> DREAM_FLOATING_ISLAND_KEY = getOrCreateRegistryKey("dream_floating_island_placed");

    public static final ResourceKey<PlacedFeature> SPIRITUAL_TREE_KEY = getOrCreateRegistryKey("spiritual_tree_placed");
    public static final ResourceKey<PlacedFeature> LEMON_TREE_KEY = getOrCreateRegistryKey("lemon_tree_placed");
    public static final ResourceKey<PlacedFeature> GINKGO_TREE_KEY = getOrCreateRegistryKey("ginkgo_tree_placed");
    public static final ResourceKey<PlacedFeature> PEACH_TREE_KEY = getOrCreateRegistryKey("peach_tree_placed");
    public static final ResourceKey<PlacedFeature> OVERWORLD_SILVER_ORE_KEY = getOrCreateRegistryKey("overworld_silver_ore_placed");
    public static final ResourceKey<PlacedFeature> OVERWORLD_ORB_ORE_KEY = getOrCreateRegistryKey("overworld_orb_ore_placed");
    public static final ResourceKey<PlacedFeature> NETHER_BLACK_SALT_ORE_KEY = getOrCreateRegistryKey("nether_black_salt");
    public static final ResourceKey<PlacedFeature> UDUMBARA_FLOWER_KEY = getOrCreateRegistryKey("udumbara_flower_placed");
    public static final ResourceKey<PlacedFeature> TREMELLA_KEY = getOrCreateRegistryKey("tremella_placed");

    public static final ResourceKey<PlacedFeature> OUTER_SHRINE = getOrCreateRegistryKey("outer_shrine");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var registryLookup = context.lookup(Registries.CONFIGURED_FEATURE);

        context.register(SPIRITUAL_TREE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.SPIRITUAL_TREE_KEY),
                VegetationPlacements.treePlacement(
                        PlacementUtils.countExtra(0, 0.1f, 1),
                        RDWoodBlocks.SPIRITUAL_BUNDLE.sapling().asBlock()
                )
        ));

        context.register(LEMON_TREE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.LEMON_TREE_KEY),
                VegetationPlacements.treePlacement(
                        PlacementUtils.countExtra(0, 0.2f, 1),
                        RDWoodBlocks.LEMON_BUNDLE.sapling().asBlock()
                )
        ));

        context.register(GINKGO_TREE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.GINKGO_TREE_KEY),
                VegetationPlacements.treePlacement(
                        PlacementUtils.countExtra(0, 0.1f, 1),
                        RDWoodBlocks.GINKGO_BUNDLE.sapling().asBlock()
                )
        ));

        context.register(PEACH_TREE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.PEACH_TREE_KEY),
                VegetationPlacements.treePlacement(
                        PlacementUtils.countExtra(0, 0.1f, 1),
                        RDWoodBlocks.PEACH_BUNDLE.sapling().asBlock()
                )
        ));

        context.register(OVERWORLD_SILVER_ORE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.OVERWORLD_SILVER_ORE_KEY),
                Modifiers.modifiersCount(9, HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(10), VerticalAnchor.belowTop(10)))
        ));

        context.register(OVERWORLD_ORB_ORE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.OVERWORLD_ORB_ORE_KEY),
                Modifiers.modifiersCount(7, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80)))
        ));

        context.register(NETHER_BLACK_SALT_ORE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.NETHER_BLACK_SALT_ORE_KEY),
                Modifiers.modifiersCount(6, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80)))
        ));

        context.register(UDUMBARA_FLOWER_KEY,
                new PlacedFeature(
                        registryLookup.getOrThrow(
                                RDBuiltinConfigurationFeatures.UDUMBARA_FLOWER_KEY
                        ),
                        List.of(
                                RarityFilter.onAverageOnceEvery(5),
                                CountPlacement.of(8),
                                InSquarePlacement.spread(),
                                RandomOffsetPlacement.vertical(
                                        ConstantInt.of(2)
                                ),
                                PlacementUtils.HEIGHTMAP,
                                BiomeFilter.biome()
                        )
                )
        );
        context.register(TREMELLA_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.TREMELLA_KEY),
                List.of(
                        RarityFilter.onAverageOnceEvery(6),
                        CountPlacement.of(16),
                        InSquarePlacement.spread(),
                        RandomOffsetPlacement.vertical(
                                ConstantInt.of(2)
                        ),
                        PlacementUtils.HEIGHTMAP,
                        BiomeFilter.biome()
                )
        ));

        // 世界生成
        context.register(CRATER_MEGA_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.CRATER_MEGA_KEY),
                List.of(
                        RarityFilter.onAverageOnceEvery(256),
                        InSquarePlacement.spread(),
                        PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT,
                        BiomeFilter.biome()
                )
        ));
        context.register(CRATER_LARGE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.CRATER_LARGE_KEY),
                List.of(
                        RarityFilter.onAverageOnceEvery(12),
                        InSquarePlacement.spread(),
                        PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT,
                        BiomeFilter.biome()
                )
        ));
        context.register(CRATER_SMALL_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.CRATER_SMALL_KEY),
                List.of(
                        RarityFilter.onAverageOnceEvery(4),
                        InSquarePlacement.spread(),
                        PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT,
                        BiomeFilter.biome()
                )
        ));
        context.register(DREAM_WORLD_GRID_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.DREAM_GRID_KEY),
                List.of(
                        BiomeFilter.biome(),
                        HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE),
                        CountPlacement.of(1)
                )
        ));
        context.register(
                MOON_IRON_ORE_KEY,
                new PlacedFeature(
                        registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.MOON_IRON_ORE_KEY),
                        List.of(
                                CountPlacement.of(22),
                                InSquarePlacement.spread(),

                                HeightRangePlacement.uniform(
                                        VerticalAnchor.absolute(5),
                                        VerticalAnchor.absolute(35)
                                ),

                                BiomeFilter.biome()
                        )
                )
        );
        context.register(
                MOON_GOLD_ORE_KEY,
                new PlacedFeature(
                        registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.MOON_GOLD_ORE_KEY),
                        List.of(
                                CountPlacement.of(6),
                                InSquarePlacement.spread(),

                                HeightRangePlacement.uniform(
                                        VerticalAnchor.absolute(-20),
                                        VerticalAnchor.absolute(30)
                                ),

                                BiomeFilter.biome()
                        )
                )
        );
        context.register(
                MOON_DIAMOND_ORE_KEY,
                new PlacedFeature(
                        registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.MOON_DIAMOND_ORE_KEY),
                        List.of(
                                CountPlacement.of(6),
                                InSquarePlacement.spread(),

                                HeightRangePlacement.uniform(
                                        VerticalAnchor.absolute(-32),
                                        VerticalAnchor.absolute(5)
                                ),

                                BiomeFilter.biome()
                        )
                )
        );
        context.register(
                MOON_QUARTZ_ORE_KEY,
                new PlacedFeature(
                        registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.MOON_QUARTZ_ORE_KEY),
                        List.of(
                                CountPlacement.of(6),
                                InSquarePlacement.spread(),

                                HeightRangePlacement.uniform(
                                        VerticalAnchor.absolute(-32),
                                        VerticalAnchor.absolute(30)
                                ),

                                BiomeFilter.biome()
                        )
                )
        );
        context.register(
                MOON_DIORITE_PLACED_KEY,
                new PlacedFeature(
                        registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.MOON_DIORITE_KEY),
                        List.of(
                                CountPlacement.of(15),

                                InSquarePlacement.spread(),

                                HeightRangePlacement.uniform(
                                        VerticalAnchor.absolute(-20),
                                        VerticalAnchor.absolute(35)
                                ),

                                BiomeFilter.biome()
                        )
                )
        );
        context.register(
                MOON_GRAVEL_PLACED_KEY,
                new PlacedFeature(
                        registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.MOON_GRAVEL_PLACED_KEY),
                        List.of(
                                CountPlacement.of(18),

                                InSquarePlacement.spread(),

                                HeightRangePlacement.uniform(
                                        VerticalAnchor.absolute(-25),
                                        VerticalAnchor.absolute(38)
                                ),

                                BiomeFilter.biome()
                        )
                )
        );
        context.register(
                MOON_WATER_LAKE_KEY,
                new PlacedFeature(
                        registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.MOON_WATER_LAKE_KEY),
                        List.of(
                                RarityFilter.onAverageOnceEvery(80),
                                InSquarePlacement.spread(),
                                HeightRangePlacement.uniform(
                                        VerticalAnchor.absolute(20),
                                        VerticalAnchor.absolute(40)
                                ),
                                BiomeFilter.biome()
                        )
                )
        );


        context.register(FLOATING_DREAM_STONE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.FLOATING_DREAM_STONE_KEY),
                List.of(
                        RarityFilter.onAverageOnceEvery(11),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(10), VerticalAnchor.absolute(120)),
                        BiomeFilter.biome()
                )
        ));

        context.register(FLOATING_DREAM_CRYSTAL_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.FLOATING_DREAM_CRYSTAL_KEY),
                List.of(
                        RarityFilter.onAverageOnceEvery(6),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(20), VerticalAnchor.absolute(128)),
                        BiomeFilter.biome()
                )
        ));

        context.register(FLOATING_DREAM_TRIAL_ROOM_ZOMBIE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.FLOATING_DREAM_TRIAL_ROOM_ZOMBIE_KEY),
                List.of(
                        RarityFilter.onAverageOnceEvery(110),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(50), VerticalAnchor.absolute(200)),
                        BiomeFilter.biome()
                )
        ));

        context.register(FLOATING_DREAM_TRIAL_ROOM_SKELETON_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.FLOATING_DREAM_TRIAL_ROOM_SKELETON_KEY),
                List.of(
                        RarityFilter.onAverageOnceEvery(110),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(50), VerticalAnchor.absolute(200)),
                        BiomeFilter.biome()
                )
        ));

        context.register(DREAM_FLOATING_ISLAND_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.DREAM_FLOATING_ISLAND_KEY),
                List.of(
                        RarityFilter.onAverageOnceEvery(150),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(40), VerticalAnchor.absolute(180)),
                        BiomeFilter.biome()
                )
        ));
    }

    public static void init() {

    }

    public static ResourceKey<PlacedFeature> getOrCreateRegistryKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(ReverieDreams.MOD_ID, name));
    }


    public static class Modifiers {
        public static List<PlacementModifier> modifiers(PlacementModifier countModifier, PlacementModifier heightModifier) {
            return List.of(countModifier, InSquarePlacement.spread(), heightModifier, BiomeFilter.biome());
        }

        public static List<PlacementModifier> modifiersCount(int count, PlacementModifier heightModifier) {
            return modifiers(CountPlacement.of(count), heightModifier);
        }

        public static List<PlacementModifier> modifiersRarity(int chance, PlacementModifier heightModifier) {
            return modifiers(RarityFilter.onAverageOnceEvery(chance), heightModifier);
        }
    }
}
