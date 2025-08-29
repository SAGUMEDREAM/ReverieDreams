package cc.thonly.reverie_dreams.world.gen;

import cc.thonly.mystias_izakaya.block.MIBlocks;
import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.block.base.FruitLeavesBlock;
import cc.thonly.reverie_dreams.world.gen.feature.*;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.structure.rule.BlockMatchRuleTest;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.structure.rule.TagMatchRuleTest;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.foliage.BlobFoliagePlacer;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.WeightedBlockStateProvider;
import net.minecraft.world.gen.trunk.BendingTrunkPlacer;
import net.minecraft.world.gen.trunk.StraightTrunkPlacer;

import java.util.List;

public class ConfigurationFeatureInit {
    public static final RegistryKey<ConfiguredFeature<?, ?>> SPIRITUAL_TREE_KEY = getOrCreateRegistryKey("spiritual_tree");
    public static final RegistryKey<ConfiguredFeature<?, ?>> LEMON_TREE_KEY = getOrCreateRegistryKey("lemon_tree");
    public static final RegistryKey<ConfiguredFeature<?, ?>> GINKGO_TREE_KEY = getOrCreateRegistryKey("ginkgo_tree");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PEACH_TREE_KEY = getOrCreateRegistryKey("peach_tree");
    public static final RegistryKey<ConfiguredFeature<?, ?>> OVERWORLD_SILVER_ORE_KEY = getOrCreateRegistryKey("overworld_silver_ore");
    public static final RegistryKey<ConfiguredFeature<?, ?>> OVERWORLD_ORB_ORE_KEY = getOrCreateRegistryKey("overworld_orb_ore");
    public static final RegistryKey<ConfiguredFeature<?, ?>> NETHER_BLACK_SALT_ORE_KEY = getOrCreateRegistryKey("nether_black_salt");
    public static final RegistryKey<ConfiguredFeature<?, ?>> UDUMBARA_FLOWER_KEY = getOrCreateRegistryKey("udumbara_flower");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREMELLA_KEY = getOrCreateRegistryKey("tremella_flower");

    public static final RegistryKey<ConfiguredFeature<?, ?>> CRATER_KEY = getOrCreateRegistryKey("crater");
    public static final RegistryKey<ConfiguredFeature<?, ?>> DREAM_WORLD_GRID = getOrCreateRegistryKey("dream_world_grid");
    public static final RegistryKey<ConfiguredFeature<?, ?>> CRATER_MEGA_KEY = getOrCreateRegistryKey("crater_mega");
    public static final RegistryKey<ConfiguredFeature<?, ?>> CRATER_LARGE_KEY = getOrCreateRegistryKey("crater_large");
    public static final RegistryKey<ConfiguredFeature<?, ?>> CRATER_SMALL_KEY = getOrCreateRegistryKey("crater_small");
    public static final RegistryKey<ConfiguredFeature<?, ?>> DREAM_GRID_KEY = getOrCreateRegistryKey("dream_world_grid");
    public static final RegistryKey<ConfiguredFeature<?, ?>> FLOATING_DREAM_STONE_KEY = getOrCreateRegistryKey("floating_dream_stone");
    public static final RegistryKey<ConfiguredFeature<?, ?>> FLOATING_DREAM_CRYSTAL_KEY = getOrCreateRegistryKey("floating_dream_crystal");
    public static final RegistryKey<ConfiguredFeature<?, ?>> FLOATING_DREAM_TRIAL_ROOM_ZOMBIE_KEY = getOrCreateRegistryKey("float_dream_trial_room_zombie");
    public static final RegistryKey<ConfiguredFeature<?, ?>> FLOATING_DREAM_TRIAL_ROOM_SKELETON_KEY = getOrCreateRegistryKey("float_dream_trial_room_skeleton");

    public static final Feature<CraterFeatureConfig> CRATER = register("crater", new CraterFeature(CraterFeatureConfig.CODEC));
    public static final Feature<DreamGridFeatureConfig> DREAM_GRID = register("dream_world_grid", new DreamGridFeature(DreamGridFeatureConfig.CODEC));
    public static final Feature<DreamTrialRoomConfig> DREAM_TRIAL_ROOM = register("dream_trial_room", new DreamTrialRoom(DreamTrialRoomConfig.CODEC));
    public static final Feature<FloatingSphereFeatureConfig> FLOATING_SPHERE = register("floating_sphere", new FloatingSphereFeature(FloatingSphereFeatureConfig.CODEC));


    public static void init() {

    }

    public static void bootstrap(Registerable<ConfiguredFeature<?, ?>> context) {
        RuleTest stoneReplaceables = new TagMatchRuleTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateReplaceables = new TagMatchRuleTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        RuleTest netherOreReplaceables = new TagMatchRuleTest(BlockTags.NETHER_CARVER_REPLACEABLES);
        RuleTest endOreReplaceables = new BlockMatchRuleTest(Blocks.END_STONE);

        List<OreFeatureConfig.Target> overworldSilverTargets = List.of(
                OreFeatureConfig.createTarget(stoneReplaceables, ModBlocks.SILVER_ORE.getDefaultState()),
                OreFeatureConfig.createTarget(deepslateReplaceables, ModBlocks.DEEPSLATE_SILVER_ORE.getDefaultState()));
        List<OreFeatureConfig.Target> overworldOrbTargets = List.of(
                OreFeatureConfig.createTarget(stoneReplaceables, ModBlocks.ORB_ORE.getDefaultState()),
                OreFeatureConfig.createTarget(deepslateReplaceables, ModBlocks.DEEPSLATE_ORB_ORE.getDefaultState()));
        List<OreFeatureConfig.Target> netherSaltTargets = List.of(
                OreFeatureConfig.createTarget(netherOreReplaceables, MIBlocks.BLACK_SALT_BLOCK.getDefaultState())
        );

        // 树木
        context.register(SPIRITUAL_TREE_KEY, new ConfiguredFeature<>(Feature.TREE,
                new TreeFeatureConfig.Builder(
                        BlockStateProvider.of(ModBlocks.SPIRITUAL.log()),
                        new StraightTrunkPlacer(4, 2, 1),
                        BlockStateProvider.of(ModBlocks.SPIRITUAL.leaves()),
                        new BlobFoliagePlacer(ConstantIntProvider.create(4), ConstantIntProvider.create(2), 2),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).build()
        ));

        context.register(LEMON_TREE_KEY, new ConfiguredFeature<>(Feature.TREE,
                new TreeFeatureConfig.Builder(
                        BlockStateProvider.of(MIBlocks.LEMON.log()),
                        new BendingTrunkPlacer(2, 1, 2, 2, UniformIntProvider.create(1, 1)),
                        new WeightedBlockStateProvider(Pool.<BlockState>builder()
                                .add(MIBlocks.LEMON.leaves().getDefaultState().with(LeavesBlock.PERSISTENT, true).with(LeavesBlock.WATERLOGGED, false), 3)
                                .add(MIBlocks.LEMON_FRUIT_LEAVES.getDefaultState().with(LeavesBlock.PERSISTENT, true).with(FruitLeavesBlock.AGE_PROPERTY, FruitLeavesBlock.MAX_AGE).with(LeavesBlock.WATERLOGGED, false), 1)),
                        new BlobFoliagePlacer(ConstantIntProvider.create(3), ConstantIntProvider.create(2), 2),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).build()
        ));

        context.register(GINKGO_TREE_KEY, new ConfiguredFeature<>(Feature.TREE,
                new TreeFeatureConfig.Builder(
                        BlockStateProvider.of(MIBlocks.LEMON.log()),
                        new StraightTrunkPlacer(3, 1, 0),
                        new WeightedBlockStateProvider(Pool.<BlockState>builder()
                                .add(MIBlocks.GINKGO.leaves().getDefaultState().with(LeavesBlock.PERSISTENT, true).with(LeavesBlock.WATERLOGGED, false), 3)
                                .add(MIBlocks.GINKGO_FRUIT_LEAVES.getDefaultState().with(LeavesBlock.PERSISTENT, true).with(FruitLeavesBlock.AGE_PROPERTY, FruitLeavesBlock.MAX_AGE).with(LeavesBlock.WATERLOGGED, false), 1)),
                        new BlobFoliagePlacer(ConstantIntProvider.create(3), ConstantIntProvider.create(2), 2),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).build()
        ));

        context.register(PEACH_TREE_KEY, new ConfiguredFeature<>(Feature.TREE,
                new TreeFeatureConfig.Builder(
                        BlockStateProvider.of(MIBlocks.PEACH.log()),
                        new StraightTrunkPlacer(2, 1, 1),
                        new WeightedBlockStateProvider(Pool.<BlockState>builder()
                                .add(MIBlocks.PEACH.leaves().getDefaultState().with(LeavesBlock.PERSISTENT, true).with(LeavesBlock.WATERLOGGED, false), 3)
                                .add(MIBlocks.PEACH_FRUIT_LEAVES.getDefaultState().with(LeavesBlock.PERSISTENT, true).with(FruitLeavesBlock.AGE_PROPERTY, FruitLeavesBlock.MAX_AGE).with(LeavesBlock.WATERLOGGED, false), 1)),
                        new BlobFoliagePlacer(ConstantIntProvider.create(3), ConstantIntProvider.create(2), 2),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).build()
        ));

        // 矿物
        context.register(OVERWORLD_SILVER_ORE_KEY, new ConfiguredFeature<>(Feature.ORE,
                new OreFeatureConfig(overworldSilverTargets, 15, 0.3F)
        ));

        context.register(OVERWORLD_ORB_ORE_KEY, new ConfiguredFeature<>(Feature.ORE,
                new OreFeatureConfig(overworldOrbTargets, 7, 0.32F)
        ));

        context.register(NETHER_BLACK_SALT_ORE_KEY, new ConfiguredFeature<>(Feature.ORE,
                new OreFeatureConfig(netherSaltTargets, 7, 0.22F)
        ));

        // 花
        context.register(UDUMBARA_FLOWER_KEY, new ConfiguredFeature<>(Feature.FLOWER,
                new RandomPatchFeatureConfig(32, 4, 1,
                        PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK,
                                new SimpleBlockFeatureConfig(BlockStateProvider.of(MIBlocks.UDUMBARA_FLOWER)))
                )
        ));
        context.register(TREMELLA_KEY, new ConfiguredFeature<>(Feature.FLOWER,
                new RandomPatchFeatureConfig(32, 5, 2,
                        PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK,
                                new SimpleBlockFeatureConfig(BlockStateProvider.of(MIBlocks.TREMELLA)))
                )
        ));

        // 世界生成
        context.register(CRATER_SMALL_KEY, new ConfiguredFeature<>(
                CRATER,
                new CraterFeatureConfig(
                        UniformIntProvider.create(2, 3),
                        UniformIntProvider.create(4, 7)
                )
        ));
        context.register(CRATER_LARGE_KEY, new ConfiguredFeature<>(
                CRATER,
                new CraterFeatureConfig(
                        UniformIntProvider.create(3, 5),
                        UniformIntProvider.create(12, 15)
                )
        ));
        context.register(CRATER_MEGA_KEY, new ConfiguredFeature<>(
                CRATER,
                new CraterFeatureConfig(
                        UniformIntProvider.create(8, 16),
                        UniformIntProvider.create(32, 48)
                )
        ));

        context.register(DREAM_GRID_KEY, new ConfiguredFeature<>(DREAM_GRID,
                new DreamGridFeatureConfig(Registries.BLOCK.getId(Blocks.OBSIDIAN))
        ));

        context.register(FLOATING_DREAM_STONE_KEY, new ConfiguredFeature<>(FLOATING_SPHERE,
                new FloatingSphereFeatureConfig(Registries.BLOCK.getId(ModBlocks.DREAM_STONE.block()), 2, 7, 25, 120, 32)
        ));

        context.register(FLOATING_DREAM_CRYSTAL_KEY, new ConfiguredFeature<>(FLOATING_SPHERE,
                new FloatingSphereFeatureConfig(Registries.BLOCK.getId(ModBlocks.DREAM_CRYSTAL_ORE), 0, 1, 60, 128, 32)
        ));

        context.register(FLOATING_DREAM_TRIAL_ROOM_ZOMBIE_KEY, new ConfiguredFeature<>(DREAM_TRIAL_ROOM,
                new DreamTrialRoomConfig(Registries.ENTITY_TYPE.getId(EntityType.ZOMBIE))
        ));
        context.register(FLOATING_DREAM_TRIAL_ROOM_SKELETON_KEY, new ConfiguredFeature<>(DREAM_TRIAL_ROOM,
                new DreamTrialRoomConfig(Registries.ENTITY_TYPE.getId(EntityType.SKELETON))
        ));

    }

    private static RegistryKey<ConfiguredFeature<?, ?>> getOrCreateRegistryKey(String name) {
        return RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Touhou.id(name));
    }

    private static <C extends FeatureConfig, F extends Feature<C>> F registerForVanilla(String name, F feature) {
        return (F) Registry.register(Registries.FEATURE, name, feature);
    }

    private static <C extends FeatureConfig, F extends Feature<C>> F register(String name, F feature) {
        return (F) Registry.register(Registries.FEATURE, Touhou.id(name), feature);
    }
}
