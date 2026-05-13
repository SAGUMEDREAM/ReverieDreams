package cc.thonly.reverie_dreams.world;

import cc.thonly.keine.tag.ConventionalBiomeTags;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.Hairball;
import cc.thonly.reverie_dreams.entity.MaidYousei;
import cc.thonly.reverie_dreams.entity.SunflowerYousei;
import cc.thonly.reverie_dreams.entity.UFO;
import cc.thonly.reverie_dreams.entity.elemental.IceElementalEntity;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.util.biome.BiomePredicateTool;
import cc.thonly.reverie_dreams.world.gen.BiomeInit;
import cc.thonly.reverie_dreams.world.gen.PlacedFeaturesInit;
import net.blay09.mods.balm.Balm;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.Random;
import java.util.function.Supplier;

public class BiomeModificationInit {
    public static void initialize() {
        addSpawnPlacements();
        ReverieDreams.LATE_INIT.add(() -> {
            addBlock();
            addFlower();
            addTree();
            addEntity();
            addStructure();
        });
    }

    public static void addSpawnPlacements() {
        RDEntityTypes.YOUSEI.withSpawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, () -> (entityType, world, reason, pos, random) -> {
            if (!world.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)) return false;
            if (world.getRawBrightness(pos, 0) <= 8) return false;
            if (!world.getBlockState(pos).isAir()) return false;

            int nearby = world.getEntitiesOfClass(
                    RDEntityTypes.YOUSEI.asHolder().value().getBaseClass(),
                    new AABB(
                            pos.getX() - 8, pos.getY() - 4, pos.getZ() - 8,
                            pos.getX() + 8, pos.getY() + 4, pos.getZ() + 8
                    )
            ).size();

            if (nearby > 2) return false;
            return random.nextFloat() < 0.6f;
        });
        RDEntityTypes.SUNFLOWER_YOUSEI.withSpawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new Supplier<SpawnPlacements.SpawnPredicate<SunflowerYousei>>() {
            @Override
            public SpawnPlacements.SpawnPredicate<SunflowerYousei> get() {
                return (entityType, world, reason, pos, random) -> {
                    // 原本条件
                    if (!world.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)) return false;
                    if (world.getRawBrightness(pos, 0) <= 8) return false;
                    if (!world.getBlockState(pos).isAir()) return false;

                    // 检测周围是否已有太多该实体
                    int nearbyCount = world.getEntitiesOfClass(
                            RDEntityTypes.SUNFLOWER_YOUSEI.asHolder().value().getBaseClass(),
                            new AABB(
                                    pos.getX() - 8, pos.getY() - 4, pos.getZ() - 8,
                                    pos.getX() + 8, pos.getY() + 4, pos.getZ() + 8
                            )
                    ).size();

                    return nearbyCount < 3; // 附近 16x8x16 范围内少于 3 个才允许生成
                };
            }
        });
        RDEntityTypes.MAID_YOUSEI.withSpawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new Supplier<SpawnPlacements.SpawnPredicate<MaidYousei>>() {
            @Override
            public SpawnPlacements.SpawnPredicate<MaidYousei> get() {
                return (entityType, world, reason, pos, random) -> {
                    // 原本条件
                    if (!world.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)) return false;
                    if (world.getRawBrightness(pos, 0) <= 8) return false;
                    if (!world.getBlockState(pos).isAir()) return false;

                    // 检测周围是否已有太多该实体
                    int nearbyCount = world.getEntitiesOfClass(
                            RDEntityTypes.SUNFLOWER_YOUSEI.asHolder().value().getBaseClass(),
                            new AABB(
                                    pos.getX() - 8, pos.getY() - 4, pos.getZ() - 8,
                                    pos.getX() + 8, pos.getY() + 4, pos.getZ() + 8
                            )
                    ).size();

                    return nearbyCount < 3; // 附近 16x8x16 范围内少于 3 个才允许生成
                };
            }
        });
        RDEntityTypes.HAIRBALL.withSpawnPlacement(SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, () -> Hairball::checkSpawnRules
        );
        RDEntityTypes.GOBLIN.withSpawnPlacement(SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, () -> (entity, world, reason, pos, random) ->
                        world.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK) &&
                                world.getRawBrightness(pos, 0) > 8 &&
                                world.getBlockState(pos).isAir()
        );
        RDEntityTypes.ICE_ELEMENTAL.withSpawnPlacement(
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                () -> IceElementalEntity::canSpawn
        );
        RDEntityTypes.UFO.withSpawnPlacement(SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                () -> UFO::checkSpawnRules
        );
        RDEntityTypes.MOON_RABBIT.withSpawnPlacement(
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                () -> (entity, world, reason, pos, random) -> {
                    return world.getBlockState(pos.above()).isAir();
                }
        );
    }

    public static void addTree() {
        Balm.biomeModifications().modifyBiome(
                ReverieDreams.id("spiritual_tree_spawn"),
                BiomePredicateTool.includeByKey(Biomes.BIRCH_FOREST, Biomes.SAVANNA),
                (biome, builder) -> builder.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        PlacedFeaturesInit.SPIRITUAL_TREE_KEY
                )
        );
        Balm.biomeModifications().modifyBiome(
                ReverieDreams.id("lemon_tree_spawn"),
                BiomePredicateTool.includeByKey(Biomes.FOREST),
                (biome, builder) -> builder.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        PlacedFeaturesInit.LEMON_TREE_KEY
                )
        );
        Balm.biomeModifications().modifyBiome(
                ReverieDreams.id("ginkgo_tree_spawn"),
                BiomePredicateTool.includeByKey(Biomes.SAVANNA, Biomes.JUNGLE),
                (biome, builder) -> builder.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        PlacedFeaturesInit.GINKGO_TREE_KEY
                )
        );
        Balm.biomeModifications().modifyBiome(
                ReverieDreams.id("peach_tree_spawn"),
                BiomePredicateTool.includeByKey(Biomes.FOREST, Biomes.JUNGLE),
                (biome, builder) -> builder.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        PlacedFeaturesInit.PEACH_TREE_KEY
                )
        );
    }

    public static void addBlock() {
        // 银矿石
        Balm.biomeModifications().modifyBiome(
                ReverieDreams.id("overworld_silver_ore_spawn"),
                BiomePredicateTool.includeByKey(BiomeTags.IS_OVERWORLD),
                (biome, builder) -> builder.addFeature(
                        GenerationStep.Decoration.UNDERGROUND_ORES,
                        PlacedFeaturesInit.OVERWORLD_SILVER_ORE_KEY
                )
        );
        // 宝玉矿石
        Balm.biomeModifications().modifyBiome(
                ReverieDreams.id("overworld_orb_ore_spawn"),
                BiomePredicateTool.includeByKey(BiomeTags.IS_OVERWORLD),
                (biome, builder) -> builder.addFeature(
                        GenerationStep.Decoration.UNDERGROUND_ORES,
                        PlacedFeaturesInit.OVERWORLD_ORB_ORE_KEY
                )
        );
    }

    public static void addFlower() {
        // 幻昙华
        Balm.biomeModifications().modifyBiome(
                ReverieDreams.id("udumbara_flower_spawn"),
                BiomePredicateTool.includeByKey(Biomes.SNOWY_PLAINS, Biomes.FLOWER_FOREST),
                (biome, builder) -> builder.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        PlacedFeaturesInit.UDUMBARA_FLOWER_KEY
                )
        );
        // 银耳丛
        Balm.biomeModifications().modifyBiome(
                ReverieDreams.id("tremella_spawn"),
                BiomePredicateTool.includeByKey(Biomes.FOREST, Biomes.DARK_FOREST, Biomes.BIRCH_FOREST),
                (biome, builder) -> builder.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        PlacedFeaturesInit.TREMELLA_KEY
                )
        );
    }

    public static void addEntity() {
        // 野猪
        Balm.biomeModifications().modifyBiome(ReverieDreams.id("wild_pig_spawn"),
                BiomePredicateTool.tag(ConventionalBiomeTags.IS_FOREST),
                (biome, builder) -> builder.addSpawn(MobCategory.MONSTER,
                        new MobSpawnSettings.SpawnerData(
                                RDEntityTypes.WILD_PIG.asHolder().value(),
                                1,
                                3
                        ), 1)
        );
        // 妖精大类
        if (ReverieDreams.config().enableYouseiSpawn) {
            // 普通妖精
            Balm.biomeModifications().modifyBiome(ReverieDreams.id("yousei_spawn_plains"),
                    BiomePredicateTool.tag(ConventionalBiomeTags.IS_PLAINS),
                    (biome, builder) -> builder.addSpawn(MobCategory.MONSTER,
                            new MobSpawnSettings.SpawnerData(RDEntityTypes.YOUSEI.asHolder().value(), 1, 2),
                            10
                    )
            );
            Balm.biomeModifications().modifyBiome(ReverieDreams.id("yousei_spawn_dream"),
                    BiomePredicateTool.includeByKey(ConventionalBiomeTags.IS_PLAINS),
                    (biome, builder) -> builder.addSpawn(MobCategory.MONSTER,
                            new MobSpawnSettings.SpawnerData(RDEntityTypes.YOUSEI.asHolder().value(), 1, 2),
                            2
                    )
            );
            // 向日葵妖精
            Balm.biomeModifications().modifyBiome(ReverieDreams.id("sunflower_yousei_spawn_dream"),
                    BiomePredicateTool.includeByKey(ConventionalBiomeTags.IS_PLAINS),
                    (biome, builder) -> builder.addSpawn(MobCategory.MONSTER,
                            new MobSpawnSettings.SpawnerData(RDEntityTypes.SUNFLOWER_YOUSEI.asHolder().value(), 1, 3),
                            3
                    )
            );
            Balm.biomeModifications().modifyBiome(ReverieDreams.id("sunflower_yousei_spawn_dream"),
                    BiomePredicateTool.includeByKey(ConventionalBiomeTags.IS_PLAINS),
                    (biome, builder) -> builder.addSpawn(MobCategory.MONSTER,
                            new MobSpawnSettings.SpawnerData(RDEntityTypes.SUNFLOWER_YOUSEI.asHolder().value(), 1, 1),
                            1
                    )
            );
            // 女仆妖精
            Balm.biomeModifications().modifyBiome(ReverieDreams.id("maid_yousei_spawn_dark_forest"),
                    BiomePredicateTool.includeByKey(ConventionalBiomeTags.IS_PLAINS),
                    (biome, builder) -> builder.addSpawn(MobCategory.MONSTER,
                            new MobSpawnSettings.SpawnerData(RDEntityTypes.MAID_YOUSEI.asHolder().value(), 1, 2),
                            10
                    )
            );
        }
        // 杀人蜂
        Balm.biomeModifications().modifyBiome(ReverieDreams.id("killer_bee_spawn_birch_forest"),
                BiomePredicateTool.includeByKey(ConventionalBiomeTags.IS_BIRCH_FOREST),
                (biome, builder) -> builder.addSpawn(MobCategory.MONSTER,
                        new MobSpawnSettings.SpawnerData(RDEntityTypes.KILLER_BEE.asHolder().value(), 2, 3),
                        7
                )
        );
        // 毛玉
        Balm.biomeModifications().modifyBiome(ReverieDreams.id("hailball_spawn_forest"),
                BiomePredicateTool.includeByKey(ConventionalBiomeTags.IS_FOREST),
                (biome, builder) -> builder.addSpawn(MobCategory.MONSTER,
                        new MobSpawnSettings.SpawnerData(RDEntityTypes.HAIRBALL.asHolder().value(), 2, 4),
                        10
                )
        );

        // 哥布林
        Balm.biomeModifications().modifyBiome(ReverieDreams.id("goblin_spawn_desert"),
                BiomePredicateTool.tag(ConventionalBiomeTags.IS_DESERT),
                (biome, builder) -> builder.addSpawn(
                        MobCategory.MONSTER,
                        new MobSpawnSettings.SpawnerData(RDEntityTypes.GOBLIN.asHolder().value(), 1, 1),
                        50 / 5
                )
        );
        // 蘑菇怪
        Balm.biomeModifications().modifyBiome(ReverieDreams.id("mushroom_spawn_mushroom"),
                BiomePredicateTool.tag(ConventionalBiomeTags.IS_DESERT),
                (biome, builder) -> builder.addSpawn(
                        MobCategory.MONSTER,
                        new MobSpawnSettings.SpawnerData(RDEntityTypes.MUSHROOM_MONSTER.asHolder().value(), 1, 1),
                        8
                )
        );
        Balm.biomeModifications().modifyBiome(ReverieDreams.id("mushroom_spawn_dark_forest"),
                BiomePredicateTool.tag(ConventionalBiomeTags.IS_DESERT),
                (biome, builder) -> builder.addSpawn(
                        MobCategory.MONSTER,
                        new MobSpawnSettings.SpawnerData(RDEntityTypes.MUSHROOM_MONSTER.asHolder().value(), 1, 1),
                        8
                )
        );
        // 冰元素
        Balm.biomeModifications().modifyBiome(
                ReverieDreams.id("ice_elemental_snowy"),
                BiomePredicateTool.tag(ConventionalBiomeTags.IS_SNOWY),
                (biome, builder) -> builder.addSpawn(
                        MobCategory.MONSTER,
                        new MobSpawnSettings.SpawnerData(RDEntityTypes.ICE_ELEMENTAL.asHolder().value(), 1, 2),
                        10 // weight 最后面
                )
        );

        Balm.biomeModifications().modifyBiome(
                ReverieDreams.id("ice_elemental_snowy_plains"),
                BiomePredicateTool.tag(ConventionalBiomeTags.IS_SNOWY_PLAINS),
                (biome, builder) -> builder.addSpawn(
                        MobCategory.MONSTER,
                        new MobSpawnSettings.SpawnerData(RDEntityTypes.ICE_ELEMENTAL.asHolder().value(), 1, 2),
                        10
                )
        );

        Balm.biomeModifications().modifyBiome(
                ReverieDreams.id("ice_elemental_cold_end"),
                BiomePredicateTool.tag(ConventionalBiomeTags.IS_COLD_END),
                (biome, builder) -> builder.addSpawn(
                        MobCategory.MONSTER,
                        new MobSpawnSettings.SpawnerData(RDEntityTypes.ICE_ELEMENTAL.asHolder().value(), 1, 2),
                        10
                )
        );
        // 月兔
        Balm.biomeModifications().modifyBiome(
                ReverieDreams.id("moon_rabbit_spawn"),
                BiomePredicateTool.includeByKey(BiomeInit.THE_MOON),
                (biome, builder) -> builder.addSpawn(
                        MobCategory.MONSTER,
                        new MobSpawnSettings.SpawnerData(RDEntityTypes.MOON_RABBIT.asHolder().value(), 1, 1),
                        10
                )
        );
//        RDEntityTypes.MOON_RABBIT.withSpawnPlacement(
//                SpawnPlacementTypes.ON_GROUND,
//                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
//                () -> (entity, world, reason, pos, random) -> {
//                    return world.getBlockState(pos.below()).is(RDBlocks.MOON_STONE.block()) &&
//                            world.getBlockState(pos).isAir();
//                }
//        );
        // UFO
        Balm.biomeModifications().modifyBiome(
                ReverieDreams.id("ufo_spawn"),
                BiomePredicateTool.tag(ConventionalBiomeTags.IS_MOUNTAIN_PEAK),
                (biome, builder) -> builder.addSpawn(
                        MobCategory.MONSTER,
                        new MobSpawnSettings.SpawnerData(RDEntityTypes.UFO.asHolder().value(), 1, 2),
                        3
                )
        );
    }

    public static void addStructure() {
//        BiomeModifications.addFeature(
//                biome -> biome.getBiomeKey() == BiomeKeys.FOREST,
//                GenerationStep.Feature.SURFACE_STRUCTURES,
//
//                );
    }

    public static boolean canSpawn(EntityType<?> type, ServerLevel world, EntitySpawnReason reason, BlockPos pos, Random random) {
        return world.getMaxLocalRawBrightness(pos) <= 7 && world.getBlockState(pos.below()).isRedstoneConductor(world, pos.below());
    }

}
