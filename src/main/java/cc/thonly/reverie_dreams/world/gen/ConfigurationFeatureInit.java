package cc.thonly.reverie_dreams.world.gen;

import cc.thonly.mystias_izakaya.block.MIBlocks;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.block.base.FruitLeavesBlock;
import cc.thonly.reverie_dreams.world.gen.feature.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.BendingTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import java.util.List;

public class ConfigurationFeatureInit {
    // 主世界
    public static final ResourceKey<ConfiguredFeature<?, ?>> SPIRITUAL_TREE_KEY = getOrCreateRegistryKey("spiritual_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> LEMON_TREE_KEY = getOrCreateRegistryKey("lemon_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GINKGO_TREE_KEY = getOrCreateRegistryKey("ginkgo_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PEACH_TREE_KEY = getOrCreateRegistryKey("peach_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_SILVER_ORE_KEY = getOrCreateRegistryKey("overworld_silver_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_ORB_ORE_KEY = getOrCreateRegistryKey("overworld_orb_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> NETHER_BLACK_SALT_ORE_KEY = getOrCreateRegistryKey("nether_black_salt");
    public static final ResourceKey<ConfiguredFeature<?, ?>> UDUMBARA_FLOWER_KEY = getOrCreateRegistryKey("udumbara_flower");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREMELLA_KEY = getOrCreateRegistryKey("tremella_flower");

    // 月球
    public static final ResourceKey<ConfiguredFeature<?, ?>> CRATER_KEY = getOrCreateRegistryKey("crater");
    public static final ResourceKey<ConfiguredFeature<?, ?>> DREAM_WORLD_GRID = getOrCreateRegistryKey("dream_world_grid");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CRATER_MEGA_KEY = getOrCreateRegistryKey("crater_mega");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CRATER_LARGE_KEY = getOrCreateRegistryKey("crater_large");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CRATER_SMALL_KEY = getOrCreateRegistryKey("crater_small");
    public static final ResourceKey<ConfiguredFeature<?, ?>> DREAM_GRID_KEY = getOrCreateRegistryKey("dream_world_grid");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FLOATING_DREAM_STONE_KEY = getOrCreateRegistryKey("floating_dream_stone");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FLOATING_DREAM_CRYSTAL_KEY = getOrCreateRegistryKey("floating_dream_crystal");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FLOATING_DREAM_TRIAL_ROOM_ZOMBIE_KEY = getOrCreateRegistryKey("float_dream_trial_room_zombie");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FLOATING_DREAM_TRIAL_ROOM_SKELETON_KEY = getOrCreateRegistryKey("float_dream_trial_room_skeleton");
    public static final ResourceKey<ConfiguredFeature<?, ?>> DREAM_FLOATING_ISLAND_KEY = getOrCreateRegistryKey("dream_floating_island");

    // 梦境世界
    public static final Feature<CraterFeatureConfig> CRATER = register("crater", new CraterFeature(CraterFeatureConfig.CODEC));
    public static final Feature<DreamGridFeatureConfig> DREAM_GRID = register("dream_world_grid", new DreamGridFeature(DreamGridFeatureConfig.CODEC));
    public static final Feature<DreamTrialRoomConfig> DREAM_TRIAL_ROOM = register("dream_trial_room", new DreamTrialRoom(DreamTrialRoomConfig.CODEC));
    public static final Feature<FloatingSphereFeatureConfig> FLOATING_SPHERE = register("floating_sphere", new FloatingSphereFeature(FloatingSphereFeatureConfig.CODEC));
    public static final Feature<NoneFeatureConfiguration> DREAM_FLOATING_ISLAND = register("dream_floating_island", new FloatingIslandFeature(NoneFeatureConfiguration.CODEC));

    // 主世界结构


    public static void init() {

    }

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        RuleTest stoneReplaceables = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateReplaceables = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        RuleTest netherOreReplaceables = new TagMatchTest(BlockTags.NETHER_CARVER_REPLACEABLES);
        RuleTest endOreReplaceables = new BlockMatchTest(Blocks.END_STONE);

        List<OreConfiguration.TargetBlockState> overworldSilverTargets = List.of(
                OreConfiguration.target(stoneReplaceables, ModBlocks.SILVER_ORE.defaultBlockState()),
                OreConfiguration.target(deepslateReplaceables, ModBlocks.DEEPSLATE_SILVER_ORE.defaultBlockState()));
        List<OreConfiguration.TargetBlockState> overworldOrbTargets = List.of(
                OreConfiguration.target(stoneReplaceables, ModBlocks.ORB_ORE.defaultBlockState()),
                OreConfiguration.target(deepslateReplaceables, ModBlocks.DEEPSLATE_ORB_ORE.defaultBlockState()));
        List<OreConfiguration.TargetBlockState> netherSaltTargets = List.of(
                OreConfiguration.target(netherOreReplaceables, MIBlocks.BLACK_SALT_BLOCK.defaultBlockState())
        );

        // 树木
        context.register(SPIRITUAL_TREE_KEY, new ConfiguredFeature<>(Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(ModBlocks.SPIRITUAL.log()),
                        new StraightTrunkPlacer(4, 2, 1),
                        BlockStateProvider.simple(ModBlocks.SPIRITUAL.leaves()),
                        new BlobFoliagePlacer(ConstantInt.of(4), ConstantInt.of(2), 2),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).build()
        ));

        context.register(LEMON_TREE_KEY, new ConfiguredFeature<>(Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(MIBlocks.LEMON.log()),
                        new BendingTrunkPlacer(2, 1, 2, 2, UniformInt.of(1, 1)),
                        new WeightedStateProvider(WeightedList.<BlockState>builder()
                                .add(MIBlocks.LEMON.leaves().defaultBlockState().setValue(LeavesBlock.PERSISTENT, true).setValue(LeavesBlock.WATERLOGGED, false), 3)
                                .add(MIBlocks.LEMON_FRUIT_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true).setValue(FruitLeavesBlock.AGE_PROPERTY, FruitLeavesBlock.MAX_AGE).setValue(LeavesBlock.WATERLOGGED, false), 1)),
                        new BlobFoliagePlacer(ConstantInt.of(3), ConstantInt.of(2), 2),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).build()
        ));

        context.register(GINKGO_TREE_KEY, new ConfiguredFeature<>(Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(MIBlocks.LEMON.log()),
                        new StraightTrunkPlacer(3, 1, 0),
                        new WeightedStateProvider(WeightedList.<BlockState>builder()
                                .add(MIBlocks.GINKGO.leaves().defaultBlockState().setValue(LeavesBlock.PERSISTENT, true).setValue(LeavesBlock.WATERLOGGED, false), 3)
                                .add(MIBlocks.GINKGO_FRUIT_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true).setValue(FruitLeavesBlock.AGE_PROPERTY, FruitLeavesBlock.MAX_AGE).setValue(LeavesBlock.WATERLOGGED, false), 1)),
                        new BlobFoliagePlacer(ConstantInt.of(3), ConstantInt.of(2), 2),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).build()
        ));

        context.register(PEACH_TREE_KEY, new ConfiguredFeature<>(Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(MIBlocks.PEACH.log()),
                        new StraightTrunkPlacer(2, 1, 1),
                        new WeightedStateProvider(WeightedList.<BlockState>builder()
                                .add(MIBlocks.PEACH.leaves().defaultBlockState().setValue(LeavesBlock.PERSISTENT, true).setValue(LeavesBlock.WATERLOGGED, false), 3)
                                .add(MIBlocks.PEACH_FRUIT_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true).setValue(FruitLeavesBlock.AGE_PROPERTY, FruitLeavesBlock.MAX_AGE).setValue(LeavesBlock.WATERLOGGED, false), 1)),
                        new BlobFoliagePlacer(ConstantInt.of(3), ConstantInt.of(2), 2),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).build()
        ));

        // 矿物
        context.register(OVERWORLD_SILVER_ORE_KEY, new ConfiguredFeature<>(Feature.ORE,
                new OreConfiguration(overworldSilverTargets, 15, 0.3F)
        ));

        context.register(OVERWORLD_ORB_ORE_KEY, new ConfiguredFeature<>(Feature.ORE,
                new OreConfiguration(overworldOrbTargets, 7, 0.32F)
        ));

        context.register(NETHER_BLACK_SALT_ORE_KEY, new ConfiguredFeature<>(Feature.ORE,
                new OreConfiguration(netherSaltTargets, 7, 0.22F)
        ));

        // 花
        context.register(UDUMBARA_FLOWER_KEY, new ConfiguredFeature<>(Feature.FLOWER,
                new RandomPatchConfiguration(32, 4, 1,
                        PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK,
                                new SimpleBlockConfiguration(BlockStateProvider.simple(MIBlocks.UDUMBARA_FLOWER)))
                )
        ));
        context.register(TREMELLA_KEY, new ConfiguredFeature<>(Feature.FLOWER,
                new RandomPatchConfiguration(32, 5, 2,
                        PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK,
                                new SimpleBlockConfiguration(BlockStateProvider.simple(MIBlocks.TREMELLA)))
                )
        ));

        // 世界生成
        context.register(CRATER_SMALL_KEY, new ConfiguredFeature<>(
                CRATER,
                new CraterFeatureConfig(
                        UniformInt.of(2, 3),
                        UniformInt.of(4, 7)
                )
        ));
        context.register(CRATER_LARGE_KEY, new ConfiguredFeature<>(
                CRATER,
                new CraterFeatureConfig(
                        UniformInt.of(3, 5),
                        UniformInt.of(12, 15)
                )
        ));
        context.register(CRATER_MEGA_KEY, new ConfiguredFeature<>(
                CRATER,
                new CraterFeatureConfig(
                        UniformInt.of(8, 16),
                        UniformInt.of(32, 48)
                )
        ));

        context.register(DREAM_GRID_KEY, new ConfiguredFeature<>(DREAM_GRID,
                new DreamGridFeatureConfig(BuiltInRegistries.BLOCK.getKey(Blocks.OBSIDIAN))
        ));

        context.register(FLOATING_DREAM_STONE_KEY, new ConfiguredFeature<>(FLOATING_SPHERE,
                new FloatingSphereFeatureConfig(BuiltInRegistries.BLOCK.getKey(ModBlocks.DREAM_STONE.block()), 2, 7, 25, 120, 32)
        ));

        context.register(FLOATING_DREAM_CRYSTAL_KEY, new ConfiguredFeature<>(FLOATING_SPHERE,
                new FloatingSphereFeatureConfig(BuiltInRegistries.BLOCK.getKey(ModBlocks.DREAM_CRYSTAL_ORE), 0, 1, 60, 128, 32)
        ));

        context.register(FLOATING_DREAM_TRIAL_ROOM_ZOMBIE_KEY, new ConfiguredFeature<>(DREAM_TRIAL_ROOM,
                new DreamTrialRoomConfig(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ZOMBIE))
        ));
        context.register(FLOATING_DREAM_TRIAL_ROOM_SKELETON_KEY, new ConfiguredFeature<>(DREAM_TRIAL_ROOM,
                new DreamTrialRoomConfig(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.SKELETON))
        ));

        context.register(DREAM_FLOATING_ISLAND_KEY, new ConfiguredFeature<>(DREAM_FLOATING_ISLAND,
                new NoneFeatureConfiguration()
        ));

    }

    private static ResourceKey<ConfiguredFeature<?, ?>> getOrCreateRegistryKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ReverieDreams.id(name));
    }

    private static <C extends FeatureConfiguration, F extends Feature<C>> F registerForVanilla(String name, F feature) {
        return (F) Registry.register(BuiltInRegistries.FEATURE, name, feature);
    }

    private static <C extends FeatureConfiguration, F extends Feature<C>> F register(String name, F feature) {
        return (F) Registry.register(BuiltInRegistries.FEATURE, ReverieDreams.id(name), feature);
    }
}
