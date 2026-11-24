package cc.thonly.reverie_dreams.world.gen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.base.FruitLeavesBlock;
import cc.thonly.reverie_dreams.block.nature.PuffYoBushBlock;
import cc.thonly.reverie_dreams.registry.MCBuiltInRegistries;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDMagicForestBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.world.gen.feature.*;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoTreeConfiguration;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoTreeFeature;
import cc.thonly.reverie_dreams.world.gensokyo.understory.*;
import cc.thonly.reverie_dreams.world.gensokyo.rock.*;
import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeafLitterBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SegmentableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.PineFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.BendingTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.List;
import java.util.function.Function;

import cc.thonly.reverie_dreams.world.LargeBambooGeneration;
import cc.thonly.reverie_dreams.world.dream.DreamArchipelagoFeature;
import cc.thonly.reverie_dreams.world.dream.DreamWorldBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.bamboo.BambooForestBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.bamboo.BambooForestConfiguration;
import cc.thonly.reverie_dreams.world.gensokyo.bamboo.BambooForestFeature;
import cc.thonly.reverie_dreams.world.gensokyo.deadwood.GensokyoDeadwoodBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.deadwood.GensokyoDeadwoodConfiguration;
import cc.thonly.reverie_dreams.world.gensokyo.deadwood.GensokyoDeadwoodFeature;
import cc.thonly.reverie_dreams.world.gensokyo.landmark.GensokyoLandmarkBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.landmark.LandmarkTreeFeature;
import cc.thonly.reverie_dreams.world.gensokyo.landmark.LandmarkVegetationConfiguration;
import cc.thonly.reverie_dreams.world.gensokyo.landmark.LandmarkVegetationFeature;
import cc.thonly.reverie_dreams.world.gensokyo.landmark.MuenzukaGraveConfiguration;
import cc.thonly.reverie_dreams.world.gensokyo.landmark.MuenzukaGraveFeature;

public class RDBuiltinConfigurationFeatures {
    private static final RegistrySupplier<Feature<NoneFeatureConfiguration>> JUNGLE_VINES = registerFeature(
            "gensokyo_jungle_vines", codec -> new cc.thonly.reverie_dreams.world.gensokyo.jungle.GensokyoJungleDecoration(codec, false),
            NoneFeatureConfiguration.CODEC);
    private static final RegistrySupplier<Feature<NoneFeatureConfiguration>> JUNGLE_FALLEN_LOG = registerFeature(
            "gensokyo_jungle_fallen_log", codec -> new cc.thonly.reverie_dreams.world.gensokyo.jungle.GensokyoJungleDecoration(codec, true),
            NoneFeatureConfiguration.CODEC);
    private static final RegistrySupplier<Feature<SimpleBlockConfiguration>> TREMELLA =
            registerFeature("tremella", TremellaFeature::new, SimpleBlockConfiguration.CODEC);
    private static final RegistrySupplier<Feature<NoneFeatureConfiguration>> FICUS_GROWTH =
            registerFeature("ficus_growth", FicusGrowthFeature::new, NoneFeatureConfiguration.CODEC);
    private static final RegistrySupplier<Feature<NoneFeatureConfiguration>> DREAM_ARCHIPELAGO =
            registerFeature("dream_archipelago", DreamArchipelagoFeature::new,
                    NoneFeatureConfiguration.CODEC);
    private static final RegistrySupplier<Feature<GensokyoDeadwoodConfiguration>> GENSOKYO_DEADWOOD =
            registerFeature("gensokyo_deadwood", GensokyoDeadwoodFeature::new,
                    GensokyoDeadwoodConfiguration.CODEC);
    public static final List<ResourceKey<ConfiguredFeature<?, ?>>> GENSOKYO_DEADWOOD_KEYS =
            GensokyoDeadwoodBootstrap.PROFILES.stream()
                    .map(GensokyoDeadwoodBootstrap.Profile::configured).toList();
    private static final RegistrySupplier<Feature<LandmarkVegetationConfiguration>> LANDMARK_VEGETATION =
            registerFeature("gensokyo_landmark_vegetation", LandmarkVegetationFeature::new,
                    LandmarkVegetationConfiguration.CODEC);
    private static final RegistrySupplier<Feature<MuenzukaGraveConfiguration>> MUENZUKA_GRAVE =
            registerFeature("muenzuka_grave", MuenzukaGraveFeature::new,
                    MuenzukaGraveConfiguration.CODEC);
    private static final RegistrySupplier<Feature<GensokyoTreeConfiguration>> LANDMARK_TREE =
            registerFeature("gensokyo_landmark_tree", LandmarkTreeFeature::new, GensokyoTreeConfiguration.CODEC);
    private static final RegistrySupplier<Feature<BambooForestConfiguration>> BAMBOO_FOREST =
            registerFeature("gensokyo_bamboo_forest", BambooForestFeature::new,
                    BambooForestConfiguration.CODEC);
    private static final RegistrySupplier<Feature<NoneFeatureConfiguration>> LARGE_BAMBOO_SHOOTS =
            registerFeature("large_bamboo_shoots", LargeBambooShootFeature::new, NoneFeatureConfiguration.CODEC);
    private static final RegistrySupplier<Feature<GensokyoRockConfiguration>> GENSOKYO_ROCK =
            registerFeature("gensokyo_rock", GensokyoRockFeature::new, GensokyoRockConfiguration.CODEC);
    public static final List<ResourceKey<ConfiguredFeature<?, ?>>> GENSOKYO_ROCK_KEYS =
            GensokyoRockBootstrap.PROFILES.stream().map(GensokyoRockBootstrap.Profile::configured).toList();
    private static final RegistrySupplier<Feature<GensokyoTreeConfiguration>> GENSOKYO_TREE =
            registerFeature("gensokyo_tree", GensokyoTreeFeature::new, GensokyoTreeConfiguration.CODEC);
    private static final RegistrySupplier<Feature<GensokyoUnderstoryConfiguration>> GENSOKYO_UNDERSTORY =
            registerFeature("gensokyo_understory", GensokyoUnderstoryFeature::new, GensokyoUnderstoryConfiguration.CODEC);
    private static final RegistrySupplier<Feature<HugeMushroomFeatureConfiguration>> MAGIC_HUGE_MUSHROOM =
            registerFeature("magic_huge_mushroom", MagicHugeMushroomFeature::new, HugeMushroomFeatureConfiguration.CODEC);
    public static final ResourceKey<ConfiguredFeature<?, ?>> HUGE_BLUE_MUSHROOM = MagicMushroomBootstrap.HUGE_BLUE;
    public static final ResourceKey<ConfiguredFeature<?, ?>> HUGE_PURPLE_MUSHROOM = MagicMushroomBootstrap.HUGE_PURPLE;
    public static final List<ResourceKey<ConfiguredFeature<?, ?>>> GENSOKYO_UNDERSTORY_KEYS =
            GensokyoUnderstoryBootstrap.PROFILES.stream().map(GensokyoUnderstoryBootstrap.Profile::configured).toList();
    // 主世界
    public static final ResourceKey<ConfiguredFeature<?, ?>> SPIRITUAL_TREE_KEY = getOrCreateRegistryKey("spiritual_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> LEMON_TREE_KEY = getOrCreateRegistryKey("lemon_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GINKGO_TREE_KEY = getOrCreateRegistryKey("ginkgo_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PEACH_TREE_KEY = getOrCreateRegistryKey("peach_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PLUM_TREE_KEY = getOrCreateRegistryKey("plum_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PINE_TREE_KEY = getOrCreateRegistryKey("pine_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CHESTNUT_TREE_KEY = getOrCreateRegistryKey("chestnut_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> IROHA_MAPLE_TREE_KEY = getOrCreateRegistryKey("iroha_maple_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ITAYA_MAPLE_TREE_KEY = getOrCreateRegistryKey("itaya_maple_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> HAUCHIWA_MAPLE_TREE_KEY = getOrCreateRegistryKey("hauchiwa_maple_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_SILVER_ORE_KEY = getOrCreateRegistryKey("overworld_silver_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_ORB_ORE_KEY = getOrCreateRegistryKey("overworld_orb_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_SILVER_SMALL_KEY = getOrCreateRegistryKey("overworld_silver_ore_small");
    public static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_ORB_BURIED_KEY = getOrCreateRegistryKey("overworld_orb_ore_buried");
    public static final ResourceKey<ConfiguredFeature<?, ?>> NETHER_BLACK_SALT_ORE_KEY = getOrCreateRegistryKey("nether_black_salt");
    public static final ResourceKey<ConfiguredFeature<?, ?>> UDUMBARA_FLOWER_KEY = getOrCreateRegistryKey("udumbara_flower");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREMELLA_KEY = getOrCreateRegistryKey("tremella_flower");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAPLE_LEAF_LITTER_KEY = getOrCreateRegistryKey("maple_leaf_litter");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FICUS_MICROCARPA_KEY = getOrCreateRegistryKey("ficus_microcarpa");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PUFF_YO_BUSH_KEY = getOrCreateRegistryKey("puff_yo_bush");

    // 月球
    public static final ResourceKey<ConfiguredFeature<?, ?>> CRATER_KEY = getOrCreateRegistryKey("crater");
    public static final ResourceKey<ConfiguredFeature<?, ?>> DREAM_WORLD_GRID = getOrCreateRegistryKey("dream_world_grid");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CRATER_MEGA_KEY = getOrCreateRegistryKey("crater_mega");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CRATER_LARGE_KEY = getOrCreateRegistryKey("crater_large");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CRATER_SMALL_KEY = getOrCreateRegistryKey("crater_small");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MOON_IRON_ORE_KEY = getOrCreateRegistryKey("moon_iron_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MOON_GOLD_ORE_KEY = getOrCreateRegistryKey("moon_gold_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MOON_DIAMOND_ORE_KEY = getOrCreateRegistryKey("moon_diamond_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MOON_QUARTZ_ORE_KEY = getOrCreateRegistryKey("moon_quartz_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MOON_DIORITE_KEY = getOrCreateRegistryKey("moon_diorite");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MOON_GRAVEL_PLACED_KEY = getOrCreateRegistryKey("moon_gravel");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MOON_WATER_LAKE_KEY = getOrCreateRegistryKey("moon_water_lake");

    // 梦境
    public static final ResourceKey<ConfiguredFeature<?, ?>> DREAM_GRID_KEY = getOrCreateRegistryKey("dream_world_grid");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FLOATING_DREAM_STONE_KEY = getOrCreateRegistryKey("floating_dream_stone");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FLOATING_DREAM_CRYSTAL_KEY = getOrCreateRegistryKey("floating_dream_crystal");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FLOATING_DREAM_TRIAL_ROOM_ZOMBIE_KEY = getOrCreateRegistryKey("float_dream_trial_room_zombie");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FLOATING_DREAM_TRIAL_ROOM_SKELETON_KEY = getOrCreateRegistryKey("float_dream_trial_room_skeleton");
    public static final ResourceKey<ConfiguredFeature<?, ?>> DREAM_FLOATING_ISLAND_KEY = getOrCreateRegistryKey("dream_floating_island");

    // 梦境世界
    public static final RegistrySupplier<Feature<CraterFeatureConfig>> CRATER =
            registerFeature(
                    "crater",
                    CraterFeature::new,
                    CraterFeatureConfig.CODEC
            );
    public static final RegistrySupplier<Feature<LakeFeature.Configuration>> LAKE =
            registerFeature(
                    "lake",
                    LakeFeature::new,
                    LakeFeature.Configuration.CODEC
            );
    public static final RegistrySupplier<Feature<DreamGridFeatureConfig>> DREAM_GRID =
            registerFeature(
                    "dream_world_grid",
                    DreamGridFeature::new,
                    DreamGridFeatureConfig.CODEC
            );
    public static final RegistrySupplier<Feature<DreamTrialRoomConfig>> DREAM_TRIAL_ROOM =
            registerFeature(
                    "dream_trial_room",
                    DreamTrialRoom::new,
                    DreamTrialRoomConfig.CODEC
            );
    public static final RegistrySupplier<Feature<FloatingSphereFeatureConfig>> FLOATING_SPHERE =
            registerFeature(
                    "floating_sphere",
                    FloatingSphereFeature::new,
                    FloatingSphereFeatureConfig.CODEC
            );
    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> DREAM_FLOATING_ISLAND =
            registerFeature(
                    "dream_floating_island",
                    FloatingIslandFeature::new,
                    NoneFeatureConfiguration.CODEC
            );

    // 主世界结构
    public static void init() {
    }

    public static <C extends FeatureConfiguration, F extends Feature<C>>
    RegistrySupplier<Feature<C>> registerFeature(
            String id,
            Function<Codec<C>, F> factory,
            Codec<C> codec
    ) {
        return MCBuiltInRegistries.FEATURE.register(
                id,
                () -> factory.apply(codec)
        );
    }

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        cc.thonly.reverie_dreams.world.gensokyo.jungle.GensokyoJungleDecoration.configured(context, JUNGLE_VINES.value(), JUNGLE_FALLEN_LOG.value());
        DreamWorldBootstrap.configured(context, DREAM_ARCHIPELAGO.value());
        BambooForestBootstrap.configured(context, BAMBOO_FOREST.value());
        LargeBambooGeneration.configured(context, LARGE_BAMBOO_SHOOTS.value());
        GensokyoRockBootstrap.configured(context, GENSOKYO_ROCK.value());
        GensokyoLandmarkBootstrap.configured(context, LANDMARK_VEGETATION.value(), LANDMARK_TREE.value(), MUENZUKA_GRAVE.value());
        GensokyoDeadwoodBootstrap.configured(context, GENSOKYO_DEADWOOD.value());
        GensokyoForestBootstrap.configured(context, GENSOKYO_TREE.value());
        GensokyoUnderstoryBootstrap.configured(context, GENSOKYO_UNDERSTORY.value());
        MagicMushroomBootstrap.configured(context, MAGIC_HUGE_MUSHROOM.value());
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
                        BlockStateProvider.simple(RDWoodBlocks.SPIRITUAL_BUNDLE.leaves().asBlock().defaultBlockState().setValue(LeavesBlock.DISTANCE, 7).setValue(LeavesBlock.WATERLOGGED, false)),
                        new BlobFoliagePlacer(ConstantInt.of(4), ConstantInt.of(2), 2),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).build()
        ));

        context.register(LEMON_TREE_KEY, new ConfiguredFeature<>(Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(RDWoodBlocks.LEMON_BUNDLE.log().asBlock()),
                        new BendingTrunkPlacer(2, 1, 2, 2, UniformInt.of(1, 1)),
                        new WeightedStateProvider(WeightedList.<BlockState>builder()
                                .add(RDWoodBlocks.LEMON_BUNDLE.leaves().defaultBlockState().setValue(LeavesBlock.WATERLOGGED, false), 3)
                                .add(RDWoodBlocks.LEMON_FRUIT_LEAVES.defaultBlockState().setValue(FruitLeavesBlock.AGE_PROPERTY, FruitLeavesBlock.MAX_AGE).setValue(LeavesBlock.WATERLOGGED, false), 1)),
                        new BlobFoliagePlacer(ConstantInt.of(3), ConstantInt.of(2), 2),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).build()
        ));

        context.register(GINKGO_TREE_KEY, new ConfiguredFeature<>(Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(RDWoodBlocks.GINKGO_BUNDLE.log().asBlock()),
                        new StraightTrunkPlacer(3, 1, 0),
                        new WeightedStateProvider(WeightedList.<BlockState>builder()
                                .add(RDWoodBlocks.GINKGO_BUNDLE.leaves().defaultBlockState().setValue(LeavesBlock.WATERLOGGED, false), 3)
                                .add(RDWoodBlocks.GINKGO_FRUIT_LEAVES.defaultBlockState().setValue(FruitLeavesBlock.AGE_PROPERTY, FruitLeavesBlock.MAX_AGE).setValue(LeavesBlock.WATERLOGGED, false), 1)),
                        new BlobFoliagePlacer(ConstantInt.of(3), ConstantInt.of(2), 2),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).build()
        ));

        context.register(PEACH_TREE_KEY, new ConfiguredFeature<>(Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(RDWoodBlocks.PEACH_BUNDLE.log().asBlock()),
                        new StraightTrunkPlacer(2, 1, 1),
                        new WeightedStateProvider(WeightedList.<BlockState>builder()
                                .add(RDWoodBlocks.PEACH_BUNDLE.leaves().defaultBlockState().setValue(LeavesBlock.WATERLOGGED, false), 3)
                                .add(RDWoodBlocks.PEACH_FRUIT_LEAVES.defaultBlockState().setValue(FruitLeavesBlock.AGE_PROPERTY, FruitLeavesBlock.MAX_AGE).setValue(LeavesBlock.WATERLOGGED, false), 1)),
                        new BlobFoliagePlacer(ConstantInt.of(3), ConstantInt.of(2), 2),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).build()
        ));

        context.register(PLUM_TREE_KEY, new ConfiguredFeature<>(Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(Blocks.CHERRY_LOG),
                        new BendingTrunkPlacer(2, 1, 2, 2, UniformInt.of(1, 1)),
                        new WeightedStateProvider(WeightedList.<BlockState>builder()
                                .add(Blocks.CHERRY_LEAVES.defaultBlockState().setValue(LeavesBlock.WATERLOGGED, false), 3)
                                .add(RDWoodBlocks.PLUM_FRUIT_LEAVES.defaultBlockState().setValue(FruitLeavesBlock.AGE_PROPERTY, FruitLeavesBlock.MAX_AGE).setValue(LeavesBlock.WATERLOGGED, false), 1)),
                        new BlobFoliagePlacer(ConstantInt.of(3), ConstantInt.of(2), 2),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).build()
        ));

        context.register(PINE_TREE_KEY, new ConfiguredFeature<>(Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(Blocks.SPRUCE_LOG),
                        new StraightTrunkPlacer(6, 3, 0),
                        new WeightedStateProvider(WeightedList.<BlockState>builder()
                                .add(Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.WATERLOGGED, false), 3)
                                .add(RDWoodBlocks.PINE_FRUIT_LEAVES.defaultBlockState().setValue(FruitLeavesBlock.AGE_PROPERTY, FruitLeavesBlock.MAX_AGE).setValue(LeavesBlock.WATERLOGGED, false), 1)),
                        new PineFoliagePlacer(ConstantInt.of(1), ConstantInt.of(0), UniformInt.of(3, 4)),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).build()
        ));

        context.register(CHESTNUT_TREE_KEY, new ConfiguredFeature<>(Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(Blocks.DARK_OAK_LOG),
                        new StraightTrunkPlacer(4, 2, 0),
                        new WeightedStateProvider(WeightedList.<BlockState>builder()
                                .add(Blocks.DARK_OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.WATERLOGGED, false), 3)
                                .add(RDWoodBlocks.CHESTNUT_FRUIT_LEAVES.defaultBlockState().setValue(FruitLeavesBlock.AGE_PROPERTY, FruitLeavesBlock.MAX_AGE).setValue(LeavesBlock.WATERLOGGED, false), 1)),
                        new BlobFoliagePlacer(ConstantInt.of(3), ConstantInt.of(2), 3),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).build()
        ));

        context.register(IROHA_MAPLE_TREE_KEY, new ConfiguredFeature<>(Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(RDWoodBlocks.IROHA_MAPLE_BUNDLE.log().asBlock()),
                        new BendingTrunkPlacer(3, 1, 1, 2, UniformInt.of(1, 2)),
                        BlockStateProvider.simple(RDWoodBlocks.IROHA_MAPLE_BUNDLE.leaves().asBlock().defaultBlockState().setValue(LeavesBlock.WATERLOGGED, false)),
                        new BlobFoliagePlacer(ConstantInt.of(3), ConstantInt.of(2), 2),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).build()
        ));

        context.register(ITAYA_MAPLE_TREE_KEY, new ConfiguredFeature<>(Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(RDWoodBlocks.ITAYA_MAPLE_BUNDLE.log().asBlock()),
                        new StraightTrunkPlacer(4, 2, 0),
                        BlockStateProvider.simple(RDWoodBlocks.ITAYA_MAPLE_BUNDLE.leaves().asBlock().defaultBlockState().setValue(LeavesBlock.WATERLOGGED, false)),
                        // Radius 4 produces upper-crown leaves beyond vanilla's six-step log support.
                        new BlobFoliagePlacer(ConstantInt.of(3), ConstantInt.of(2), 3),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).build()
        ));

        context.register(HAUCHIWA_MAPLE_TREE_KEY, new ConfiguredFeature<>(Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(RDWoodBlocks.HAUCHIWA_MAPLE_BUNDLE.log().asBlock()),
                        new StraightTrunkPlacer(3, 2, 0),
                        BlockStateProvider.simple(RDWoodBlocks.HAUCHIWA_MAPLE_BUNDLE.leaves().asBlock().defaultBlockState().setValue(LeavesBlock.WATERLOGGED, false)),
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

        context.register(OVERWORLD_SILVER_SMALL_KEY, new ConfiguredFeature<>(Feature.ORE,
                new OreConfiguration(overworldSilverTargets, 7, 0.2F)
        ));

        context.register(OVERWORLD_ORB_BURIED_KEY, new ConfiguredFeature<>(Feature.ORE,
                new OreConfiguration(overworldOrbTargets, 5, 0.65F)
        ));

        context.register(NETHER_BLACK_SALT_ORE_KEY, new ConfiguredFeature<>(Feature.ORE,
                new OreConfiguration(netherSaltTargets, 7, 0.22F)
        ));

        context.register(
                MOON_DIORITE_KEY,
                new ConfiguredFeature<>(
                        Feature.ORE,
                        new OreConfiguration(
                                List.of(
                                        OreConfiguration.target(
                                                new BlockMatchTest(
                                                        RDBlocks.MOON_STONE.block().asBlock()
                                                ),
                                                Blocks.DIORITE.defaultBlockState()
                                        )
                                ),
                                14,
                                0.85F
                        )
                )
        );
        context.register(
                MOON_GRAVEL_PLACED_KEY,
                new ConfiguredFeature<>(
                        Feature.ORE,
                        new OreConfiguration(
                                List.of(
                                        OreConfiguration.target(
                                                new BlockMatchTest(
                                                        RDBlocks.MOON_STONE.block().asBlock()
                                                ),
                                                Blocks.GRAVEL.defaultBlockState()
                                        )
                                ),
                                33
                        )
                )
        );
        context.register(
                MOON_WATER_LAKE_KEY,
                new ConfiguredFeature<>(
                        LAKE.get(),
                        new LakeFeature.Configuration(
                                BlockStateProvider.simple(Blocks.WATER),
                                BlockStateProvider.simple(RDBlocks.MOON_STONE.block().asBlock())
                        )
                )
        );


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
                        TREMELLA.value(),
                        new SimpleBlockConfiguration(
                                BlockStateProvider.simple(
                                        RDWoodBlocks.TREMELLA.defaultBlockState()
                                )
                        )
                )
        );

        // 枫树落叶层：三种枫树的落叶随机混铺（含朝向与 1~3 段）
        WeightedList.Builder<BlockState> mapleLeafLitterStates = WeightedList.builder();
        for (Block litter : List.of(
                RDWoodBlocks.IROHA_MAPLE_LEAF_LITTER.asBlock(),
                RDWoodBlocks.ITAYA_MAPLE_LEAF_LITTER.asBlock(),
                RDWoodBlocks.HAUCHIWA_MAPLE_LEAF_LITTER.asBlock())) {
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                for (int amount = 1; amount <= 3; amount++) {
                    mapleLeafLitterStates.add(
                            litter.defaultBlockState()
                                    .setValue(LeafLitterBlock.FACING, facing)
                                    .setValue(SegmentableBlock.AMOUNT, amount),
                            1
                    );
                }
            }
        }
        context.register(
                MAPLE_LEAF_LITTER_KEY,
                new ConfiguredFeature<>(
                        Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(
                                new WeightedStateProvider(mapleLeafLitterStates.build())
                        )
                )
        );

        // 魔法森林附生植物
        context.register(
                FICUS_MICROCARPA_KEY,
                new ConfiguredFeature<>(FICUS_GROWTH.value(), NoneFeatureConfiguration.INSTANCE)
        );
        WeightedList.Builder<BlockState> puffYoBushStates = WeightedList.builder();
        puffYoBushStates.add(RDMagicForestBlocks.PUFF_YO_BUSH.defaultBlockState().setValue(PuffYoBushBlock.AGE, 0), 6);
        puffYoBushStates.add(RDMagicForestBlocks.PUFF_YO_BUSH.defaultBlockState().setValue(PuffYoBushBlock.AGE, 1), 3);
        puffYoBushStates.add(RDMagicForestBlocks.PUFF_YO_BUSH.defaultBlockState().setValue(PuffYoBushBlock.AGE, 2), 1);
        context.register(
                PUFF_YO_BUSH_KEY,
                new ConfiguredFeature<>(
                        Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(new WeightedStateProvider(puffYoBushStates.build()))
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
        context.register(MOON_IRON_ORE_KEY, new ConfiguredFeature<>(
                Feature.ORE,
                new OreConfiguration(
                        List.of(
                                OreConfiguration.target(
                                        new BlockMatchTest(RDBlocks.MOON_STONE.block().asBlock()),
                                        RDBlocks.MOON_IRON_ORE.defaultBlockState()
                                )
                        ),
                        7
                )
        ));
        context.register(MOON_GOLD_ORE_KEY, new ConfiguredFeature<>(
                Feature.ORE,
                new OreConfiguration(
                        List.of(
                                OreConfiguration.target(
                                        new BlockMatchTest(RDBlocks.MOON_STONE.block().asBlock()),
                                        RDBlocks.MOON_GOLD_ORE.defaultBlockState()
                                )
                        ),
                        6
                )
        ));
        context.register(MOON_DIAMOND_ORE_KEY, new ConfiguredFeature<>(
                Feature.ORE,
                new OreConfiguration(
                        List.of(
                                OreConfiguration.target(
                                        new BlockMatchTest(RDBlocks.MOON_STONE.block().asBlock()),
                                        RDBlocks.MOON_DIAMOND_ORE.defaultBlockState()
                                )
                        ),
                        5
                )
        ));
        context.register(MOON_QUARTZ_ORE_KEY, new ConfiguredFeature<>(
                Feature.ORE,
                new OreConfiguration(
                        List.of(
                                OreConfiguration.target(
                                        new BlockMatchTest(RDBlocks.MOON_STONE.block().asBlock()),
                                        RDBlocks.MOON_QUARTZ_ORE.defaultBlockState()
                                )
                        ),
                        7
                )
        ));

        // 梦境
        context.register(DREAM_GRID_KEY, new ConfiguredFeature<>(DREAM_GRID.value(),
                new DreamGridFeatureConfig(BuiltInRegistries.BLOCK.getKey(Blocks.OBSIDIAN))
        ));

        context.register(FLOATING_DREAM_STONE_KEY, new ConfiguredFeature<>(FLOATING_SPHERE.value(),
                new FloatingSphereFeatureConfig(BuiltInRegistries.BLOCK.getKey(RDBlocks.DREAM_STONE.block().asBlock()), 2, 4, 36, 104, 16)
        ));

        context.register(FLOATING_DREAM_CRYSTAL_KEY, new ConfiguredFeature<>(FLOATING_SPHERE.value(),
                new FloatingSphereFeatureConfig(BuiltInRegistries.BLOCK.getKey(RDBlocks.DREAM_CRYSTAL_ORE.asBlock()), 1, 2, 64, 116, 16)
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
