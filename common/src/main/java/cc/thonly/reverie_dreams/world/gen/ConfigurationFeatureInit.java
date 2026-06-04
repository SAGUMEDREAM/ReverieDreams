package cc.thonly.reverie_dreams.world.gen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.base.FruitLeavesBlock;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.world.gen.feature.*;
import net.blay09.mods.balm.core.BalmRegistrar;
import net.blay09.mods.balm.core.BalmRegistrars;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
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
import net.minecraft.world.level.levelgen.feature.configurations.*;
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
    public static Holder<Feature<CraterFeatureConfig>> CRATER;
    public static Holder<Feature<DreamGridFeatureConfig>> DREAM_GRID;
    public static Holder<Feature<DreamTrialRoomConfig>> DREAM_TRIAL_ROOM;
    public static Holder<Feature<FloatingSphereFeatureConfig>> FLOATING_SPHERE;
    public static Holder<Feature<NoneFeatureConfiguration>> DREAM_FLOATING_ISLAND;

    // 主世界结构
    @SuppressWarnings("unchecked")
    public static void init(BalmRegistrars registrars) {
        BalmRegistrar.Scoped<Feature<?>> scoped = registrars.registrar(Registries.FEATURE);
        CRATER = (Holder<Feature<CraterFeatureConfig>>) (Object) scoped.register("crater", key -> new CraterFeature(CraterFeatureConfig.CODEC));
        DREAM_GRID = (Holder<Feature<DreamGridFeatureConfig>>) (Object) scoped.register("dream_world_grid", key -> new DreamGridFeature(DreamGridFeatureConfig.CODEC));
        DREAM_TRIAL_ROOM = (Holder<Feature<DreamTrialRoomConfig>>) (Object) scoped.register("dream_trial_room", key -> new DreamTrialRoom(DreamTrialRoomConfig.CODEC));
        FLOATING_SPHERE = (Holder<Feature<FloatingSphereFeatureConfig>>) (Object) scoped.register("floating_sphere", key -> new FloatingSphereFeature(FloatingSphereFeatureConfig.CODEC));
        DREAM_FLOATING_ISLAND = (Holder<Feature<NoneFeatureConfiguration>>) (Object) scoped.register("dream_floating_island", key -> new FloatingIslandFeature(NoneFeatureConfiguration.CODEC));
    }

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        RuleTest stoneReplaceables = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateReplaceables = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        RuleTest netherOreReplaceables = new TagMatchTest(BlockTags.NETHER_CARVER_REPLACEABLES);
        RuleTest endOreReplaceables = new BlockMatchTest(Blocks.END_STONE);

        List<OreConfiguration.TargetBlockState> overworldSilverTargets = List.of(
                OreConfiguration.target(stoneReplaceables, RDBlocks.SILVER_ORE.defaultBlockState()),
                OreConfiguration.target(deepslateReplaceables, RDBlocks.DEEPSLATE_SILVER_ORE.defaultBlockState()));
        List<OreConfiguration.TargetBlockState> overworldOrbTargets = List.of(
                OreConfiguration.target(stoneReplaceables, RDBlocks.ORB_ORE.defaultBlockState()),
                OreConfiguration.target(deepslateReplaceables, RDBlocks.DEEPSLATE_ORB_ORE.defaultBlockState()));
        List<OreConfiguration.TargetBlockState> netherSaltTargets = List.of(
                OreConfiguration.target(netherOreReplaceables, RDBlocks.BLACK_SALT_BLOCK.defaultBlockState())
        );

        // 树木
        context.register(SPIRITUAL_TREE_KEY, new ConfiguredFeature<>(Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(RDWoodBlocks.SPIRITUAL_BUNDLE.log().asBlock()),
                        new StraightTrunkPlacer(5, 2, 1),
                        BlockStateProvider.simple(RDWoodBlocks.SPIRITUAL_BUNDLE.leaves().asBlock().defaultBlockState().setValue(LeavesBlock.DISTANCE, 7).setValue(LeavesBlock.PERSISTENT, true).setValue(LeavesBlock.WATERLOGGED, false)),
                        new BlobFoliagePlacer(ConstantInt.of(4), ConstantInt.of(2), 2),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).build()
        ));

        context.register(LEMON_TREE_KEY, new ConfiguredFeature<>(Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(RDWoodBlocks.LEMON_BUNDLE.log().asBlock()),
                        new BendingTrunkPlacer(2, 1, 2, 2, UniformInt.of(1, 1)),
                        new WeightedStateProvider(WeightedList.<BlockState>builder()
                                .add(RDWoodBlocks.LEMON_BUNDLE.leaves().defaultBlockState().setValue(LeavesBlock.PERSISTENT, true).setValue(LeavesBlock.WATERLOGGED, false), 3)
                                .add(RDWoodBlocks.LEMON_FRUIT_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true).setValue(FruitLeavesBlock.AGE_PROPERTY, FruitLeavesBlock.MAX_AGE).setValue(LeavesBlock.WATERLOGGED, false), 1)),
                        new BlobFoliagePlacer(ConstantInt.of(3), ConstantInt.of(2), 2),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).build()
        ));

        context.register(GINKGO_TREE_KEY, new ConfiguredFeature<>(Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(RDWoodBlocks.LEMON_BUNDLE.log().asBlock()),
                        new StraightTrunkPlacer(3, 1, 0),
                        new WeightedStateProvider(WeightedList.<BlockState>builder()
                                .add(RDWoodBlocks.GINKGO_BUNDLE.leaves().defaultBlockState().setValue(LeavesBlock.PERSISTENT, true).setValue(LeavesBlock.WATERLOGGED, false), 3)
                                .add(RDWoodBlocks.GINKGO_FRUIT_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true).setValue(FruitLeavesBlock.AGE_PROPERTY, FruitLeavesBlock.MAX_AGE).setValue(LeavesBlock.WATERLOGGED, false), 1)),
                        new BlobFoliagePlacer(ConstantInt.of(3), ConstantInt.of(2), 2),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).build()
        ));

        context.register(PEACH_TREE_KEY, new ConfiguredFeature<>(Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(RDWoodBlocks.PEACH_BUNDLE.log().asBlock()),
                        new StraightTrunkPlacer(2, 1, 1),
                        new WeightedStateProvider(WeightedList.<BlockState>builder()
                                .add(RDWoodBlocks.PEACH_BUNDLE.leaves().defaultBlockState().setValue(LeavesBlock.PERSISTENT, true).setValue(LeavesBlock.WATERLOGGED, false), 3)
                                .add(RDWoodBlocks.PEACH_FRUIT_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true).setValue(FruitLeavesBlock.AGE_PROPERTY, FruitLeavesBlock.MAX_AGE).setValue(LeavesBlock.WATERLOGGED, false), 1)),
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
        context.register(
                UDUMBARA_FLOWER_KEY,
                new ConfiguredFeature<>(
                        Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(
                                BlockStateProvider.simple(
                                        RDWoodBlocks.UDUMBARA_FLOWER.defaultBlockState()
                                )
                        )
                )
        );
        context.register(
                TREMELLA_KEY,
                new ConfiguredFeature<>(
                        Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(
                                BlockStateProvider.simple(
                                        RDWoodBlocks.TREMELLA.defaultBlockState()
                                )
                        )
                )
        );

        // 世界生成
        context.register(CRATER_SMALL_KEY, new ConfiguredFeature<>(
                CRATER.value(),
                new CraterFeatureConfig(
                        UniformInt.of(2, 3),
                        UniformInt.of(4, 7)
                )
        ));
        context.register(CRATER_LARGE_KEY, new ConfiguredFeature<>(
                CRATER.value(),
                new CraterFeatureConfig(
                        UniformInt.of(3, 5),
                        UniformInt.of(12, 15)
                )
        ));
        context.register(CRATER_MEGA_KEY, new ConfiguredFeature<>(
                CRATER.value(),
                new CraterFeatureConfig(
                        UniformInt.of(8, 16),
                        UniformInt.of(32, 48)
                )
        ));

        context.register(DREAM_GRID_KEY, new ConfiguredFeature<>(DREAM_GRID.value(),
                new DreamGridFeatureConfig(BuiltInRegistries.BLOCK.getKey(Blocks.OBSIDIAN))
        ));

        context.register(FLOATING_DREAM_STONE_KEY, new ConfiguredFeature<>(FLOATING_SPHERE.value(),
                new FloatingSphereFeatureConfig(BuiltInRegistries.BLOCK.getKey(RDBlocks.DREAM_STONE.block().asBlock()), 2, 7, 25, 120, 32)
        ));

        context.register(FLOATING_DREAM_CRYSTAL_KEY, new ConfiguredFeature<>(FLOATING_SPHERE.value(),
                new FloatingSphereFeatureConfig(BuiltInRegistries.BLOCK.getKey(RDBlocks.DREAM_CRYSTAL_ORE.asBlock()), 0, 1, 60, 128, 32)
        ));

        context.register(FLOATING_DREAM_TRIAL_ROOM_ZOMBIE_KEY, new ConfiguredFeature<>(DREAM_TRIAL_ROOM.value(),
                new DreamTrialRoomConfig(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ZOMBIE))
        ));
        context.register(FLOATING_DREAM_TRIAL_ROOM_SKELETON_KEY, new ConfiguredFeature<>(DREAM_TRIAL_ROOM.value(),
                new DreamTrialRoomConfig(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.SKELETON))
        ));

        context.register(DREAM_FLOATING_ISLAND_KEY, new ConfiguredFeature<>(DREAM_FLOATING_ISLAND.value(),
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
