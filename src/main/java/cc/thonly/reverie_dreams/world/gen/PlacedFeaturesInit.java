package cc.thonly.reverie_dreams.world.gen;

import cc.thonly.mystias_izakaya.block.MIBlocks;
import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.block.ModBlocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;
import net.minecraft.world.gen.feature.VegetationPlacedFeatures;
import net.minecraft.world.gen.placementmodifier.*;

import java.util.List;

public class PlacedFeaturesInit {
    public static final RegistryKey<PlacedFeature> DREAM_WORLD_GRID_KEY = getOrCreateRegistryKey("dream_world_grid");
    public static final RegistryKey<PlacedFeature> CRATER_MEGA_KEY = getOrCreateRegistryKey("crater_mega");
    public static final RegistryKey<PlacedFeature> CRATER_LARGE_KEY = getOrCreateRegistryKey("crater_large");
    public static final RegistryKey<PlacedFeature> CRATER_SMALL_KEY = getOrCreateRegistryKey("crater_small");
    public static final RegistryKey<PlacedFeature> FLOATING_DREAM_STONE_KEY = getOrCreateRegistryKey("floating_dream_stone_placed");
    public static final RegistryKey<PlacedFeature> FLOATING_DREAM_CRYSTAL_KEY = getOrCreateRegistryKey("floating_dream_crystal_placed");
    public static final RegistryKey<PlacedFeature> FLOATING_DREAM_TRIAL_ROOM_ZOMBIE_KEY = getOrCreateRegistryKey("floating_dream_trial_room_zombie_placed");
    public static final RegistryKey<PlacedFeature> FLOATING_DREAM_TRIAL_ROOM_SKELETON_KEY = getOrCreateRegistryKey("floating_dream_trial_room_skeleton_placed");
    public static final RegistryKey<PlacedFeature> DREAM_FLOATING_ISLAND_KEY = getOrCreateRegistryKey("dream_floating_island_placed");

    public static final RegistryKey<PlacedFeature> SPIRITUAL_TREE_KEY = getOrCreateRegistryKey("spiritual_tree_placed");
    public static final RegistryKey<PlacedFeature> LEMON_TREE_KEY = getOrCreateRegistryKey("lemon_tree_placed");
    public static final RegistryKey<PlacedFeature> GINKGO_TREE_KEY = getOrCreateRegistryKey("ginkgo_tree_placed");
    public static final RegistryKey<PlacedFeature> PEACH_TREE_KEY = getOrCreateRegistryKey("peach_tree_placed");
    public static final RegistryKey<PlacedFeature> OVERWORLD_SILVER_ORE_KEY = getOrCreateRegistryKey("overworld_silver_ore_placed");
    public static final RegistryKey<PlacedFeature> OVERWORLD_ORB_ORE_KEY = getOrCreateRegistryKey("overworld_orb_ore_placed");
    public static final RegistryKey<PlacedFeature> UDUMBARA_FLOWER_KEY = getOrCreateRegistryKey("udumbara_flower_placed");
    public static final RegistryKey<PlacedFeature> TREMELLA_KEY = getOrCreateRegistryKey("tremella_placed");

    public static void bootstrap(Registerable<PlacedFeature> context) {
        var registryLookup = context.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);

        context.register(SPIRITUAL_TREE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(ConfigurationFeatureInit.SPIRITUAL_TREE_KEY),
                VegetationPlacedFeatures.treeModifiersWithWouldSurvive(
                        PlacedFeatures.createCountExtraModifier(0, 0.1f, 1),
                        ModBlocks.SPIRITUAL.sapling()
                )
        ));

        context.register(LEMON_TREE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(ConfigurationFeatureInit.LEMON_TREE_KEY),
                VegetationPlacedFeatures.treeModifiersWithWouldSurvive(
                        PlacedFeatures.createCountExtraModifier(0, 0.2f, 1),
                        MIBlocks.LEMON.sapling()
                )
        ));

        context.register(GINKGO_TREE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(ConfigurationFeatureInit.GINKGO_TREE_KEY),
                VegetationPlacedFeatures.treeModifiersWithWouldSurvive(
                        PlacedFeatures.createCountExtraModifier(0, 0.1f, 1),
                        MIBlocks.GINKGO.sapling()
                )
        ));

        context.register(PEACH_TREE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(ConfigurationFeatureInit.PEACH_TREE_KEY),
                VegetationPlacedFeatures.treeModifiersWithWouldSurvive(
                        PlacedFeatures.createCountExtraModifier(0, 0.1f, 1),
                        MIBlocks.PEACH.sapling()
                )
        ));

        context.register(OVERWORLD_SILVER_ORE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(ConfigurationFeatureInit.OVERWORLD_SILVER_ORE_KEY),
                Modifiers.modifiersCount(9, HeightRangePlacementModifier.uniform(YOffset.aboveBottom(10), YOffset.belowTop(10)))
        ));

        context.register(OVERWORLD_ORB_ORE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(ConfigurationFeatureInit.OVERWORLD_ORB_ORE_KEY),
                Modifiers.modifiersCount(7, HeightRangePlacementModifier.trapezoid(YOffset.aboveBottom(-80), YOffset.aboveBottom(80)))
        ));

        context.register(UDUMBARA_FLOWER_KEY, new PlacedFeature(
                registryLookup.getOrThrow(ConfigurationFeatureInit.UDUMBARA_FLOWER_KEY),
                List.of(
                        RarityFilterPlacementModifier.of(5),
                        SquarePlacementModifier.of(),
                        PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP,
                        BiomePlacementModifier.of()
                )
        ));

        context.register(TREMELLA_KEY, new PlacedFeature(
                registryLookup.getOrThrow(ConfigurationFeatureInit.TREMELLA_KEY),
                List.of(
                        RarityFilterPlacementModifier.of(6),
                        SquarePlacementModifier.of(),
                        PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP,
                        BiomePlacementModifier.of()
                )
        ));

        // 世界生成
        context.register(CRATER_MEGA_KEY, new PlacedFeature(
                registryLookup.getOrThrow(ConfigurationFeatureInit.CRATER_MEGA_KEY),
                List.of(
                        RarityFilterPlacementModifier.of(256),
                        SquarePlacementModifier.of(),
                        PlacedFeatures.BOTTOM_TO_120_RANGE,
                        BiomePlacementModifier.of()
                )
        ));
        context.register(CRATER_LARGE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(ConfigurationFeatureInit.CRATER_LARGE_KEY),
                List.of(
                        RarityFilterPlacementModifier.of(12),
                        SquarePlacementModifier.of(),
                        PlacedFeatures.BOTTOM_TO_120_RANGE,
                        BiomePlacementModifier.of()
                )
        ));
        context.register(CRATER_SMALL_KEY, new PlacedFeature(
                registryLookup.getOrThrow(ConfigurationFeatureInit.CRATER_SMALL_KEY),
                List.of(
                        RarityFilterPlacementModifier.of(4),
                        SquarePlacementModifier.of(),
                        PlacedFeatures.BOTTOM_TO_120_RANGE,
                        BiomePlacementModifier.of()
                )
        ));
        context.register(DREAM_WORLD_GRID_KEY, new PlacedFeature(
                registryLookup.getOrThrow(ConfigurationFeatureInit.DREAM_GRID_KEY),
                List.of(
                        BiomePlacementModifier.of(),
                        HeightmapPlacementModifier.of(Heightmap.Type.WORLD_SURFACE),
                        CountPlacementModifier.of(1)
                )
        ));

        context.register(FLOATING_DREAM_STONE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(ConfigurationFeatureInit.FLOATING_DREAM_STONE_KEY),
                List.of(
                        RarityFilterPlacementModifier.of(11),
                        SquarePlacementModifier.of(),
                        HeightRangePlacementModifier.uniform(YOffset.fixed(10), YOffset.fixed(120)),
                        BiomePlacementModifier.of()
                )
        ));

        context.register(FLOATING_DREAM_CRYSTAL_KEY, new PlacedFeature(
                registryLookup.getOrThrow(ConfigurationFeatureInit.FLOATING_DREAM_CRYSTAL_KEY),
                List.of(
                        RarityFilterPlacementModifier.of(6),
                        SquarePlacementModifier.of(),
                        HeightRangePlacementModifier.uniform(YOffset.fixed(20), YOffset.fixed(128)),
                        BiomePlacementModifier.of()
                )
        ));

        context.register(FLOATING_DREAM_TRIAL_ROOM_ZOMBIE_KEY, new PlacedFeature(
                registryLookup.getOrThrow(ConfigurationFeatureInit.FLOATING_DREAM_TRIAL_ROOM_ZOMBIE_KEY),
                List.of(
                        RarityFilterPlacementModifier.of(110),
                        SquarePlacementModifier.of(),
                        HeightRangePlacementModifier.uniform(YOffset.fixed(50), YOffset.fixed(200)),
                        BiomePlacementModifier.of()
                )
        ));

        context.register(FLOATING_DREAM_TRIAL_ROOM_SKELETON_KEY, new PlacedFeature(
                registryLookup.getOrThrow(ConfigurationFeatureInit.FLOATING_DREAM_TRIAL_ROOM_SKELETON_KEY),
                List.of(
                        RarityFilterPlacementModifier.of(110),
                        SquarePlacementModifier.of(),
                        HeightRangePlacementModifier.uniform(YOffset.fixed(50), YOffset.fixed(200)),
                        BiomePlacementModifier.of()
                )
        ));

        context.register(DREAM_FLOATING_ISLAND_KEY, new PlacedFeature(
                registryLookup.getOrThrow(ConfigurationFeatureInit.DREAM_FLOATING_ISLAND_KEY),
                List.of(
                        RarityFilterPlacementModifier.of(150),
                        HeightRangePlacementModifier.uniform(YOffset.fixed(40), YOffset.fixed(180)),
                        BiomePlacementModifier.of()
                )
        ));
    }

    public static void init() {

    }

    public static RegistryKey<PlacedFeature> getOrCreateRegistryKey(String name) {
        return RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(Touhou.MOD_ID, name));
    }


    public static class Modifiers {
        public static List<PlacementModifier> modifiers(PlacementModifier countModifier, PlacementModifier heightModifier) {
            return List.of(countModifier, SquarePlacementModifier.of(), heightModifier, BiomePlacementModifier.of());
        }

        public static List<PlacementModifier> modifiersCount(int count, PlacementModifier heightModifier) {
            return modifiers(CountPlacementModifier.of(count), heightModifier);
        }

        public static List<PlacementModifier> modifiersRarity(int chance, PlacementModifier heightModifier) {
            return modifiers(RarityFilterPlacementModifier.of(chance), heightModifier);
        }
    }
}
