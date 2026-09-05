package cc.thonly.reverie_dreams.world;

import cc.thonly.keine.tag.ConventionalBiomeTags;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.registry.SpawnPlacementsRegistry;
import cc.thonly.reverie_dreams.entity.UFO;
import cc.thonly.reverie_dreams.entity.elemental.IceElementalEntity;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.util.biome.BiomePredicateTool;
import cc.thonly.reverie_dreams.world.gen.RDBuiltinPlacedFeatures;
import cc.thonly.reverie_dreams.world.gen.RDBuiltinBiomes;
import lombok.extern.slf4j.Slf4j;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.world.level.levelgen.BalmWorldGen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.Random;

@Slf4j
public class BiomeModificationInit {
    public static void initialize() {
        addSpawnPlacements();
        addBlock();
        addFlower();
        addTree();
        addEntity();
        addStructure();
        ReverieDreams.COMMON_LATE_INIT.add(() -> {

        });
    }

    public static void addSpawnPlacements() {
        SpawnPlacementsRegistry.register(RDEntityTypes.YOUSEI, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (entityType, world, reason, pos, random) -> {
            if (!world.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK))
                return false;
            if (world.getRawBrightness(pos, 0) <= 8)
                return false;
            if (!world.getBlockState(pos).isAir())
                return false;

            int nearby = world.getEntitiesOfClass(
                    RDEntityTypes.YOUSEI.value().getBaseClass(),
                    new AABB(
                            pos.getX() - 8, pos.getY() - 4, pos.getZ() - 8,
                            pos.getX() + 8, pos.getY() + 4, pos.getZ() + 8
                    )
            ).size();

            if (nearby > 2)
                return false;
            return random.nextFloat() < 0.6f;
        });
        SpawnPlacementsRegistry.register(
                RDEntityTypes.MAID_YOUSEI,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, world, reason, pos, random) -> {
                    if (!world.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK))
                        return false;
                    if (world.getRawBrightness(pos, 0) <= 8)
                        return false;
                    if (!world.getBlockState(pos).isAir())
                        return false;

                    int nearby = world.getEntitiesOfClass(
                            RDEntityTypes.MAID_YOUSEI.value().getBaseClass(),
                            new AABB(
                                    pos.getX() - 16, pos.getY() - 16, pos.getZ() - 16,
                                    pos.getX() + 16, pos.getY() + 16, pos.getZ() + 16
                            )
                    ).size();

                    if (nearby > 2)
                        return false;

                    return random.nextFloat() < 0.6f;
                }
        );
        SpawnPlacementsRegistry.register(
                RDEntityTypes.SUNFLOWER_YOUSEI,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, world, reason, pos, random) -> {
                    if (!world.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK))
                        return false;
                    if (world.getRawBrightness(pos, 0) <= 8)
                        return false;
                    if (!world.getBlockState(pos).isAir())
                        return false;

                    int nearbyCount = world.getEntitiesOfClass(
                            RDEntityTypes.SUNFLOWER_YOUSEI.value().getBaseClass(),
                            new AABB(
                                    pos.getX() - 16, pos.getY() - 16, pos.getZ() - 16,
                                    pos.getX() + 16, pos.getY() + 16, pos.getZ() + 16
                            )
                    ).size();

                    return nearbyCount < 3;
                }
        );
        SpawnPlacementsRegistry.register(
                RDEntityTypes.GOBLIN,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entity, world, reason, pos, random) ->
                        world.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)
                                && world.getRawBrightness(pos, 0) > 8
                                && world.getBlockState(pos).isAir()
        );
        SpawnPlacementsRegistry.register(
                RDEntityTypes.ICE_ELEMENTAL,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                IceElementalEntity::canSpawn
        );
        SpawnPlacementsRegistry.register(
                RDEntityTypes.UFO,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                UFO::checkSpawnRules
        );
        SpawnPlacementsRegistry.register(
                RDEntityTypes.WILD_PIG,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules
        );
        SpawnPlacementsRegistry.register(
                RDEntityTypes.HAIRBALL,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mob::checkMobSpawnRules
        );
        SpawnPlacementsRegistry.register(
                RDEntityTypes.KILLER_BEE,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules
        );
        SpawnPlacementsRegistry.register(
                RDEntityTypes.MUSHROOM_MONSTER,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules
        );
        SpawnPlacementsRegistry.register(
                RDEntityTypes.MOON_RABBIT,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, spawnReason, pos, randomSource) -> {
                    boolean setup = Monster.checkMonsterSpawnRules(type, level, spawnReason, pos, randomSource);
                    int nearby = level.getEntitiesOfClass(
                            RDEntityTypes.ONI.value().getBaseClass(),
                            new AABB(
                                    pos.getX() - 32,
                                    pos.getY() - 10,
                                    pos.getZ() - 32,
                                    pos.getX() + 32,
                                    pos.getY() + 10,
                                    pos.getZ() + 32
                            )
                    ).size();

                    if (nearby > 2)
                        return false;
                    return setup;
                }
        );
        SpawnPlacementsRegistry.register(
                RDEntityTypes.ONI,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, world, reason, pos, random) -> {
                    if (world.getBlockState(pos.below()).is(Blocks.AIR))
                        return false;

                    if (world.getRawBrightness(pos, 0) <= 8)
                        return false;

                    if (!world.getBlockState(pos).isAir())
                        return false;

                    int nearby = world.getEntitiesOfClass(
                            RDEntityTypes.ONI.value().getBaseClass(),
                            new AABB(
                                    pos.getX() - 16,
                                    pos.getY() - 8,
                                    pos.getZ() - 16,
                                    pos.getX() + 16,
                                    pos.getY() + 8,
                                    pos.getZ() + 16
                            )
                    ).size();

                    if (nearby > 2)
                        return false;

                    return random.nextFloat() < 0.6f;
                }
        );
    }

    public static void addTree() {
        BalmWorldGen worldGen = Balm.biomeModifications();

        worldGen.modifyBiome(
                ReverieDreams.id("spiritual_tree_spawn"),
                BiomePredicateTool.includeByKey(Biomes.BIRCH_FOREST, Biomes.SAVANNA),
                (biome, builder) -> builder.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        RDBuiltinPlacedFeatures.SPIRITUAL_TREE_KEY
                )
        );

        worldGen.modifyBiome(
                ReverieDreams.id("lemon_tree_spawn"),
                BiomePredicateTool.includeByKey(Biomes.FOREST),
                (biome, builder) -> builder.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        RDBuiltinPlacedFeatures.LEMON_TREE_KEY
                )
        );

        worldGen.modifyBiome(
                ReverieDreams.id("ginkgo_tree_spawn"),
                BiomePredicateTool.includeByKey(Biomes.SAVANNA, Biomes.JUNGLE),
                (biome, builder) -> builder.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        RDBuiltinPlacedFeatures.GINKGO_TREE_KEY
                )
        );

        worldGen.modifyBiome(
                ReverieDreams.id("peach_tree_spawn"),
                BiomePredicateTool.includeByKey(Biomes.FOREST, Biomes.JUNGLE),
                (biome, builder) -> builder.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        RDBuiltinPlacedFeatures.PEACH_TREE_KEY
                )
        );
    }

    public static void addBlock() {
        BalmWorldGen worldGen = Balm.biomeModifications();

        // 银矿石 + 宝玉矿石
        worldGen.modifyBiome(
                ReverieDreams.id("overworld_ores"),
                BiomePredicateTool.tag(BiomeTags.IS_OVERWORLD),
                (biome, builder) -> {
                    builder.addFeature(
                            GenerationStep.Decoration.UNDERGROUND_ORES,
                            RDBuiltinPlacedFeatures.OVERWORLD_SILVER_ORE_KEY
                    );

                    builder.addFeature(
                            GenerationStep.Decoration.UNDERGROUND_ORES,
                            RDBuiltinPlacedFeatures.OVERWORLD_ORB_ORE_KEY
                    );
                }
        );

        // 月球
        worldGen.modifyBiome(
                ReverieDreams.id("moon_generation"),
                BiomePredicateTool.includeByKey(
                        RDBuiltinBiomes.THE_MOON
                ),
                (biome, builder) -> {
                    // 月球金矿
                    builder.addFeature(
                            GenerationStep.Decoration.UNDERGROUND_ORES,
                            RDBuiltinPlacedFeatures.MOON_GOLD_ORE_KEY
                    );

                    // 月球水湖
                    builder.addFeature(
                            GenerationStep.Decoration.LAKES,
                            RDBuiltinPlacedFeatures.MOON_WATER_LAKE_KEY
                    );

                    // 月球铁矿
                    builder.addFeature(
                            GenerationStep.Decoration.UNDERGROUND_ORES,
                            RDBuiltinPlacedFeatures.MOON_IRON_ORE_KEY
                    );

                    // 月球钻石矿
                    builder.addFeature(
                            GenerationStep.Decoration.UNDERGROUND_ORES,
                            RDBuiltinPlacedFeatures.MOON_DIAMOND_ORE_KEY
                    );

                    // 月球石英矿
                    builder.addFeature(
                            GenerationStep.Decoration.UNDERGROUND_ORES,
                            RDBuiltinPlacedFeatures.MOON_QUARTZ_ORE_KEY
                    );

                    // 月球闪长岩
                    builder.addFeature(
                            GenerationStep.Decoration.UNDERGROUND_ORES,
                            RDBuiltinPlacedFeatures.MOON_DIORITE_PLACED_KEY
                    );

                    // 月球沙砾
                    builder.addFeature(
                            GenerationStep.Decoration.UNDERGROUND_ORES,
                            RDBuiltinPlacedFeatures.MOON_GRAVEL_PLACED_KEY
                    );

                }
        );
    }

    public static void addFlower() {
        BalmWorldGen worldGen = Balm.biomeModifications();

        // 幻昙华
        worldGen.modifyBiome(
                ReverieDreams.id("udumbara_flower"),
                BiomePredicateTool.includeByKey(
                        Biomes.SNOWY_PLAINS,
                        Biomes.FLOWER_FOREST
                ),
                (biome, builder) -> builder.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        RDBuiltinPlacedFeatures.UDUMBARA_FLOWER_KEY
                )
        );

        // 银耳丛
        worldGen.modifyBiome(
                ReverieDreams.id("tremella"),
                BiomePredicateTool.includeByKey(
                        Biomes.FOREST,
                        Biomes.DARK_FOREST,
                        Biomes.BIRCH_FOREST
                ),
                (biome, builder) -> builder.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        RDBuiltinPlacedFeatures.TREMELLA_KEY
                )
        );
    }

    public static void addEntity() {
        BalmWorldGen worldGen = Balm.biomeModifications();

        // =========================
        // 妖精大类
        // =========================
        if (ReverieDreams.config().enableYouseiSpawn) {
            worldGen.modifyBiome(
                    ReverieDreams.id("yousei_spawn"),
                    biome -> BiomePredicateTool.tag(ConventionalBiomeTags.IS_PLAINS).test(biome)
                            || BiomePredicateTool.includeByKey(RDBuiltinBiomes.DREAM).test(biome),
                    (biome, builder) -> {
                        boolean isPlains = BiomePredicateTool
                                .tag(ConventionalBiomeTags.IS_PLAINS)
                                .test(biome);

                        boolean isDream = BiomePredicateTool
                                .includeByKey(RDBuiltinBiomes.DREAM)
                                .test(biome);

                        // 普通妖精
                        if (isPlains) {
                            builder.addSpawn(
                                    MobCategory.MONSTER,
                                    new MobSpawnSettings.SpawnerData(
                                            RDEntityTypes.YOUSEI.value(),
                                            1,
                                            2
                                    ),
                                    10
                            );

                            // 向日葵妖精
                            builder.addSpawn(
                                    MobCategory.MONSTER,
                                    new MobSpawnSettings.SpawnerData(
                                            RDEntityTypes.SUNFLOWER_YOUSEI.value(),
                                            1,
                                            3
                                    ),
                                    1
                            );

                            // 女仆妖精
                            builder.addSpawn(
                                    MobCategory.MONSTER,
                                    new MobSpawnSettings.SpawnerData(
                                            RDEntityTypes.MAID_YOUSEI.value(),
                                            1,
                                            2
                                    ),
                                    10
                            );
                        }

                        // 梦境世界额外生成
                        if (isDream) {
                            builder.addSpawn(
                                    MobCategory.MONSTER,
                                    new MobSpawnSettings.SpawnerData(
                                            RDEntityTypes.YOUSEI.value(),
                                            1,
                                            2
                                    ),
                                    2
                            );

                            builder.addSpawn(
                                    MobCategory.MONSTER,
                                    new MobSpawnSettings.SpawnerData(
                                            RDEntityTypes.SUNFLOWER_YOUSEI.value(),
                                            1,
                                            3
                                    ),
                                    3
                            );
                        }
                    }
            );
        }

        // =========================
        // 其他实体
        // =========================
        worldGen.modifyBiome(
                ReverieDreams.id("entity_spawn"),
                BiomePredicateTool.all(),
                (biome, builder) -> {

                    // 野猪
                    if (BiomePredicateTool
                            .tag(ConventionalBiomeTags.IS_FOREST)
                            .test(biome)) {

                        builder.addSpawn(
                                MobCategory.MONSTER,
                                new MobSpawnSettings.SpawnerData(
                                        RDEntityTypes.WILD_PIG.value(),
                                        1,
                                        3
                                ),
                                1
                        );
                    }

                    // 杀人蜂
                    if (BiomePredicateTool
                            .tag(ConventionalBiomeTags.IS_BIRCH_FOREST)
                            .test(biome)) {

                        builder.addSpawn(
                                MobCategory.MONSTER,
                                new MobSpawnSettings.SpawnerData(
                                        RDEntityTypes.KILLER_BEE.value(),
                                        2,
                                        3
                                ),
                                7
                        );
                    }

                    // 毛玉
                    if (BiomePredicateTool
                            .tag(ConventionalBiomeTags.IS_FOREST)
                            .test(biome)) {

                        builder.addSpawn(
                                MobCategory.MONSTER,
                                new MobSpawnSettings.SpawnerData(
                                        RDEntityTypes.HAIRBALL.value(),
                                        2,
                                        4
                                ),
                                10
                        );
                    }

                    // 哥布林 + Oni 沙漠
                    if (BiomePredicateTool
                            .tag(ConventionalBiomeTags.IS_DESERT)
                            .test(biome)) {

                        builder.addSpawn(
                                MobCategory.MONSTER,
                                new MobSpawnSettings.SpawnerData(
                                        RDEntityTypes.GOBLIN.value(),
                                        1,
                                        1
                                ),
                                50 / 5
                        );

                        builder.addSpawn(
                                MobCategory.MONSTER,
                                new MobSpawnSettings.SpawnerData(
                                        RDEntityTypes.ONI.value(),
                                        1,
                                        2
                                ),
                                2
                        );
                    }

                    // 蘑菇怪
                    if (BiomePredicateTool
                            .tag(ConventionalBiomeTags.IS_DARK_FOREST)
                            .test(biome)
                            || BiomePredicateTool
                            .tag(ConventionalBiomeTags.IS_MUSHROOM)
                            .test(biome)) {

                        builder.addSpawn(
                                MobCategory.MONSTER,
                                new MobSpawnSettings.SpawnerData(
                                        RDEntityTypes.MUSHROOM_MONSTER.value(),
                                        1,
                                        1
                                ),
                                8
                        );
                    }

                    // 冰元素
                    if (BiomePredicateTool
                            .tag(ConventionalBiomeTags.IS_SNOWY)
                            .test(biome)
                            || BiomePredicateTool
                            .tag(ConventionalBiomeTags.IS_SNOWY_PLAINS)
                            .test(biome)
                            || BiomePredicateTool
                            .tag(ConventionalBiomeTags.IS_COLD_END)
                            .test(biome)) {

                        builder.addSpawn(
                                MobCategory.MONSTER,
                                new MobSpawnSettings.SpawnerData(
                                        RDEntityTypes.ICE_ELEMENTAL.value(),
                                        1,
                                        2
                                ),
                                10
                        );
                    }

                    // 月兔
                    if (BiomePredicateTool
                            .includeByKey(RDBuiltinBiomes.THE_MOON)
                            .test(biome)) {

                        builder.addSpawn(
                                MobCategory.MONSTER,
                                new MobSpawnSettings.SpawnerData(
                                        RDEntityTypes.MOON_RABBIT.value(),
                                        1,
                                        1
                                ),
                                10
                        );
                    }

                    // UFO
                    if (BiomePredicateTool
                            .tag(ConventionalBiomeTags.IS_MOUNTAIN_PEAK)
                            .test(biome)) {

                        builder.addSpawn(
                                MobCategory.MONSTER,
                                new MobSpawnSettings.SpawnerData(
                                        RDEntityTypes.UFO.value(),
                                        1,
                                        2
                                ),
                                3
                        );
                    }

                    // Oni
                    if (BiomePredicateTool
                            .tag(ConventionalBiomeTags.IS_DARK_FOREST)
                            .test(biome)) {

                        builder.addSpawn(
                                MobCategory.MONSTER,
                                new MobSpawnSettings.SpawnerData(
                                        RDEntityTypes.ONI.value(),
                                        1,
                                        2
                                ),
                                2
                        );
                    }
                }
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
