package cc.thonly.reverie_dreams.world.gen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.block.RDMagicForestBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.world.gensokyo.rock.GensokyoRockBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.understory.GensokyoUnderstoryBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.understory.MagicMushroomBootstrap;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.TrapezoidInt;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

import cc.thonly.reverie_dreams.world.LargeBambooGeneration;
import cc.thonly.reverie_dreams.world.dream.DreamWorldBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.bamboo.BambooForestBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.deadwood.GensokyoDeadwoodBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.landmark.GensokyoLandmarkBootstrap;

public class RDBuiltinPlacedFeatures {
    public static final List<ResourceKey<PlacedFeature>> GENSOKYO_DEADWOOD_KEYS =
            GensokyoDeadwoodBootstrap.PROFILES.stream()
                    .map(GensokyoDeadwoodBootstrap.Profile::placed).toList();
    public static final List<ResourceKey<PlacedFeature>> GENSOKYO_ROCK_KEYS = GensokyoRockBootstrap.PROFILES.stream().map(GensokyoRockBootstrap.Profile::placed).toList();
    public static final ResourceKey<PlacedFeature> MAGIC_MUSHROOM_GIANTS = MagicMushroomBootstrap.GIANTS;
    public static final List<ResourceKey<PlacedFeature>> GENSOKYO_UNDERSTORY_KEYS = GensokyoUnderstoryBootstrap.PROFILES.stream().map(GensokyoUnderstoryBootstrap.Profile::placed).toList();
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
    public static final ResourceKey<PlacedFeature> PLUM_TREE_KEY = getOrCreateRegistryKey("plum_tree_placed");
    public static final ResourceKey<PlacedFeature> PINE_TREE_KEY = getOrCreateRegistryKey("pine_tree_placed");
    public static final ResourceKey<PlacedFeature> CHESTNUT_TREE_KEY = getOrCreateRegistryKey("chestnut_tree_placed");
    public static final ResourceKey<PlacedFeature> IROHA_MAPLE_TREE_KEY = getOrCreateRegistryKey("iroha_maple_tree_placed");
    public static final ResourceKey<PlacedFeature> ITAYA_MAPLE_TREE_KEY = getOrCreateRegistryKey("itaya_maple_tree_placed");
    public static final ResourceKey<PlacedFeature> HAUCHIWA_MAPLE_TREE_KEY = getOrCreateRegistryKey("hauchiwa_maple_tree_placed");
    public static final ResourceKey<PlacedFeature> OVERWORLD_SILVER_ORE_KEY = getOrCreateRegistryKey("overworld_silver_ore_placed");
    public static final ResourceKey<PlacedFeature> OVERWORLD_ORB_ORE_KEY = getOrCreateRegistryKey("overworld_orb_ore_placed");
    public static final ResourceKey<PlacedFeature> OVERWORLD_SILVER_SMALL_KEY = getOrCreateRegistryKey("overworld_silver_ore_small_placed");
    public static final ResourceKey<PlacedFeature> OVERWORLD_ORB_DRIPSTONE_KEY = getOrCreateRegistryKey("overworld_orb_ore_dripstone_placed");
    public static final ResourceKey<PlacedFeature> NETHER_BLACK_SALT_ORE_KEY = getOrCreateRegistryKey("nether_black_salt");
    public static final ResourceKey<PlacedFeature> UDUMBARA_FLOWER_KEY = getOrCreateRegistryKey("udumbara_flower_placed");
    public static final ResourceKey<PlacedFeature> TREMELLA_KEY = getOrCreateRegistryKey("tremella_placed");
    public static final ResourceKey<PlacedFeature> MAPLE_LEAF_LITTER_KEY = getOrCreateRegistryKey("maple_leaf_litter_placed");
    public static final ResourceKey<PlacedFeature> FICUS_MICROCARPA_KEY = getOrCreateRegistryKey("ficus_microcarpa_placed");
    public static final ResourceKey<PlacedFeature> PUFF_YO_BUSH_KEY = getOrCreateRegistryKey("puff_yo_bush_placed");

    public static final ResourceKey<PlacedFeature> OUTER_SHRINE = getOrCreateRegistryKey("outer_shrine");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        cc.thonly.reverie_dreams.world.gensokyo.jungle.GensokyoJungleDecoration.placed(context);
        DreamWorldBootstrap.placed(context);
        LargeBambooGeneration.placed(context);
        BambooForestBootstrap.placed(context);
        GensokyoRockBootstrap.placed(context);
        GensokyoForestBootstrap.placed(context);
        GensokyoLandmarkBootstrap.placed(context);
        GensokyoDeadwoodBootstrap.placed(context);
        MagicMushroomBootstrap.placed(context);
        GensokyoUnderstoryBootstrap.placed(context);
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

        context.register(PLUM_TREE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.PLUM_TREE_KEY),
                VegetationPlacements.treePlacement(
                        PlacementUtils.countExtra(0, 0.125f, 1),
                        RDWoodBlocks.PLUM_SAPLING.asBlock()
                )
        ));

        context.register(PINE_TREE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.PINE_TREE_KEY),
                VegetationPlacements.treePlacement(
                        PlacementUtils.countExtra(0, 0.2f, 1),
                        RDWoodBlocks.PINE_SAPLING.asBlock()
                )
        ));

        context.register(CHESTNUT_TREE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.CHESTNUT_TREE_KEY),
                VegetationPlacements.treePlacement(
                        PlacementUtils.countExtra(0, 0.125f, 1),
                        RDWoodBlocks.CHESTNUT_SAPLING.asBlock()
                )
        ));

        context.register(IROHA_MAPLE_TREE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.IROHA_MAPLE_TREE_KEY),
                VegetationPlacements.treePlacement(
                        PlacementUtils.countExtra(4, 0.5f, 1),
                        RDWoodBlocks.IROHA_MAPLE_BUNDLE.sapling().asBlock()
                )
        ));

        context.register(ITAYA_MAPLE_TREE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.ITAYA_MAPLE_TREE_KEY),
                VegetationPlacements.treePlacement(
                        PlacementUtils.countExtra(4, 0.5f, 1),
                        RDWoodBlocks.ITAYA_MAPLE_BUNDLE.sapling().asBlock()
                )
        ));

        context.register(HAUCHIWA_MAPLE_TREE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.HAUCHIWA_MAPLE_TREE_KEY),
                VegetationPlacements.treePlacement(
                        PlacementUtils.countExtra(4, 0.5f, 1),
                        RDWoodBlocks.HAUCHIWA_MAPLE_BUNDLE.sapling().asBlock()
                )
        ));

        context.register(OVERWORLD_SILVER_ORE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.OVERWORLD_SILVER_ORE_KEY),
                Modifiers.modifiersCount(6, HeightRangePlacement.triangle(VerticalAnchor.absolute(-40), VerticalAnchor.absolute(88)))
        ));

        context.register(OVERWORLD_SILVER_SMALL_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.OVERWORLD_SILVER_SMALL_KEY),
                Modifiers.modifiersCount(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(-56), VerticalAnchor.absolute(112)))
        ));

        context.register(OVERWORLD_ORB_ORE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.OVERWORLD_ORB_BURIED_KEY),
                Modifiers.modifiersCount(1, HeightRangePlacement.triangle(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(12)))
        ));

        context.register(OVERWORLD_ORB_DRIPSTONE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.OVERWORLD_ORB_BURIED_KEY),
                Modifiers.modifiersCount(2, HeightRangePlacement.triangle(VerticalAnchor.absolute(-48), VerticalAnchor.absolute(32)))
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
                                RarityFilter.onAverageOnceEvery(24),
                                CountPlacement.of(1),
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
                        RarityFilter.onAverageOnceEvery(12),
                        CountPlacement.of(2),
                        InSquarePlacement.spread(),
                        RandomOffsetPlacement.vertical(
                                ConstantInt.of(2)
                        ),
                        HeightmapPlacement.onHeightmap(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES),
                        BiomeFilter.biome()
                )
        ));

        // 枫树落叶层（沿地表散铺，2×24 次）
        context.register(MAPLE_LEAF_LITTER_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.MAPLE_LEAF_LITTER_KEY),
                List.of(
                        CountPlacement.of(2),
                        InSquarePlacement.spread(),
                        HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG),
                        BiomeFilter.biome(),
                        CountPlacement.of(24),
                        RandomOffsetPlacement.of(
                                TrapezoidInt.of(-7, 7, 0),
                                TrapezoidInt.of(-3, 3, 0)
                        ),
                        BlockPredicateFilter.forPredicate(BlockPredicate.allOf(
                                BlockPredicate.replaceable(),
                                BlockPredicate.wouldSurvive(
                                        RDWoodBlocks.IROHA_MAPLE_LEAF_LITTER.defaultBlockState(),
                                        Vec3i.ZERO
                                )
                        ))
                )
        ));

        // 魔法森林附生植物
        context.register(FICUS_MICROCARPA_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.FICUS_MICROCARPA_KEY),
                List.of(
                        CountPlacement.of(8),
                        InSquarePlacement.spread(),
                        HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG),
                        BiomeFilter.biome()
                )
        ));
        context.register(PUFF_YO_BUSH_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.PUFF_YO_BUSH_KEY),
                List.of(
                        RarityFilter.onAverageOnceEvery(24),
                        CountPlacement.of(2),
                        InSquarePlacement.spread(),
                        HeightmapPlacement.onHeightmap(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES),
                        RandomOffsetPlacement.of(
                                TrapezoidInt.of(-4, 4, 0),
                                TrapezoidInt.of(-1, 1, 0)
                        ),
                        BlockPredicateFilter.forPredicate(BlockPredicate.allOf(
                                BlockPredicate.replaceable(),
                                BlockPredicate.wouldSurvive(
                                        RDMagicForestBlocks.PUFF_YO_BUSH.defaultBlockState(),
                                        Vec3i.ZERO
                                )
                        )),
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
                                CountPlacement.of(48),
                                InSquarePlacement.spread(),

                                HeightRangePlacement.uniform(
                                        VerticalAnchor.absolute(-116),
                                        VerticalAnchor.absolute(40)
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
                                CountPlacement.of(18),
                                InSquarePlacement.spread(),

                                HeightRangePlacement.uniform(
                                        VerticalAnchor.absolute(-116),
                                        VerticalAnchor.absolute(16)
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
                                CountPlacement.of(14),
                                InSquarePlacement.spread(),

                                HeightRangePlacement.uniform(
                                        VerticalAnchor.absolute(-120),
                                        VerticalAnchor.absolute(-36)
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
                                CountPlacement.of(28),
                                InSquarePlacement.spread(),

                                HeightRangePlacement.uniform(
                                        VerticalAnchor.absolute(-112),
                                        VerticalAnchor.absolute(24)
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
                                CountPlacement.of(2),

                                InSquarePlacement.spread(),

                                HeightRangePlacement.uniform(
                                        VerticalAnchor.absolute(-112),
                                        VerticalAnchor.absolute(20)
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
                        RarityFilter.onAverageOnceEvery(40),
                        InSquarePlacement.spread(),
                        BiomeFilter.biome()
                )
        ));

        context.register(FLOATING_DREAM_CRYSTAL_KEY, new PlacedFeature(
                registryLookup.getOrThrow(RDBuiltinConfigurationFeatures.FLOATING_DREAM_CRYSTAL_KEY),
                List.of(
                        RarityFilter.onAverageOnceEvery(24),
                        InSquarePlacement.spread(),
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
