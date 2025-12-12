package cc.thonly.reverie_dreams.world;

import cc.thonly.reverie_dreams.config.ReverieDreamsConfiguration;
import cc.thonly.reverie_dreams.entity.HairballEntity;
import cc.thonly.reverie_dreams.entity.UfoEntity;
import cc.thonly.reverie_dreams.entity.elemental.IceElementalEntity;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.world.gen.BiomeInit;
import cc.thonly.reverie_dreams.world.gen.PlacedFeaturesInit;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.Random;

public class BiomeModificationInit {
    public static final int BASE_WEIGHT = 80;

    public static void init() {
        addBlock();
        addFlower();
        addTree();
        addEntity();
        addStructure();
    }

    public static void addTree() {
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.BIRCH_FOREST, Biomes.SAVANNA),
                GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeaturesInit.SPIRITUAL_TREE_KEY);
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.FOREST),
                GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeaturesInit.LEMON_TREE_KEY);
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.SAVANNA, Biomes.JUNGLE),
                GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeaturesInit.GINKGO_TREE_KEY);
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.FOREST, Biomes.JUNGLE),
                GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeaturesInit.PEACH_TREE_KEY);
    }

    public static void addBlock() {
        // 银矿石
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                PlacedFeaturesInit.OVERWORLD_SILVER_ORE_KEY
        );
        // 宝玉矿石
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                PlacedFeaturesInit.OVERWORLD_ORB_ORE_KEY
        );
    }

    public static void addFlower() {
        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(Biomes.SNOWY_PLAINS, Biomes.FLOWER_FOREST),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                PlacedFeaturesInit.UDUMBARA_FLOWER_KEY
        );
        // 银耳丛
        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(Biomes.FOREST, Biomes.DARK_FOREST, Biomes.BIRCH_FOREST),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                PlacedFeaturesInit.TREMELLA_KEY
        );
    }

    public static void addEntity() {
        // 妖精大类
        if (ReverieDreamsConfiguration.ENABLE_YOUSEI_SPAWN) {
            // 普通妖精
            BiomeModifications.addSpawn(
                    BiomeSelectors.tag(ConventionalBiomeTags.IS_PLAINS),
                    MobCategory.MONSTER,
                    RDEntityTypes.YOUSEI, 10, 1, 2
            );
            BiomeModifications.addSpawn(
                    BiomeSelectors.includeByKey(BiomeInit.DREAM),
                    MobCategory.MONSTER,
                    RDEntityTypes.YOUSEI, 2, 1, 2
            );
            SpawnPlacements.register(
                    RDEntityTypes.YOUSEI,
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    (entityType, world, reason, pos, random) -> {
                        if (!world.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)) return false;
                        if (world.getRawBrightness(pos, 0) <= 8) return false;
                        if (!world.getBlockState(pos).isAir()) return false;

                        int nearby = world.getEntitiesOfClass(
                                RDEntityTypes.YOUSEI.getBaseClass(),
                                new AABB(
                                        pos.getX() - 8, pos.getY() - 4, pos.getZ() - 8,
                                        pos.getX() + 8, pos.getY() + 4, pos.getZ() + 8
                                )
                        ).size();

                        if (nearby > 2) return false;
                        return random.nextFloat() < 0.6f;
                    }
            );
            // 向日葵妖精
            BiomeModifications.addSpawn(
                    BiomeSelectors.tag(ConventionalBiomeTags.IS_PLAINS),
                    MobCategory.MONSTER,
                    RDEntityTypes.SUNFLOWER_YOUSEI, 3, 1, 3
            );
            BiomeModifications.addSpawn(
                    BiomeSelectors.includeByKey(BiomeInit.DREAM),
                    MobCategory.MONSTER,
                    RDEntityTypes.SUNFLOWER_YOUSEI, 1, 1, 1
            );
            SpawnPlacements.register(
                    RDEntityTypes.SUNFLOWER_YOUSEI,
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    (entityType, world, reason, pos, random) -> {
                        // 原本条件
                        if (!world.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)) return false;
                        if (world.getRawBrightness(pos, 0) <= 8) return false;
                        if (!world.getBlockState(pos).isAir()) return false;

                        // 检测周围是否已有太多该实体
                        int nearbyCount = world.getEntitiesOfClass(
                                RDEntityTypes.SUNFLOWER_YOUSEI.getBaseClass(),
                                new AABB(
                                        pos.getX() - 8, pos.getY() - 4, pos.getZ() - 8,
                                        pos.getX() + 8, pos.getY() + 4, pos.getZ() + 8
                                )
                        ).size();

                        return nearbyCount < 3; // 附近 16x8x16 范围内少于 3 个才允许生成
                    }
            );
            // 女仆妖精
            BiomeModifications.addSpawn(
                    BiomeSelectors.tag(ConventionalBiomeTags.IS_DARK_FOREST),
                    MobCategory.MONSTER,
                    RDEntityTypes.MAID_YOUSEI, 10, 1, 2
            );
        }

        // 杀人蜂
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_BIRCH_FOREST),
                MobCategory.MONSTER,
                RDEntityTypes.KILLER_BEE, 7, 2, 3
        );
        // 毛玉
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_FOREST),
                MobCategory.MONSTER,
                RDEntityTypes.HAIRBALL, 10, 2, 4
        );
        SpawnPlacements.register(
                RDEntityTypes.HAIRBALL,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                HairballEntity::checkSpawnRules
        );

        // 哥布林
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_DESERT),
                MobCategory.MONSTER,
                RDEntityTypes.GOBLIN, 50 / 5, 1, 1
        );
        SpawnPlacements.register(
                RDEntityTypes.GOBLIN,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entity, world, reason, pos, random) ->
                        world.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK) &&
                                world.getRawBrightness(pos, 0) > 8 &&
                                world.getBlockState(pos).isAir()
        );
        // 蘑菇怪
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_MUSHROOM),
                MobCategory.MONSTER,
                RDEntityTypes.MUSHROOM_MONSTER, 8, 1, 2
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_DARK_FOREST),
                MobCategory.MONSTER,
                RDEntityTypes.MUSHROOM_MONSTER, 8, 1, 2
        );
        // 冰元素
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_SNOWY),
                MobCategory.MONSTER,
                RDEntityTypes.ICE_ELEMENTAL, 10, 1, 2
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_SNOWY_PLAINS),
                MobCategory.MONSTER,
                RDEntityTypes.ICE_ELEMENTAL, 10, 1, 2
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_COLD_END),
                MobCategory.MONSTER,
                RDEntityTypes.ICE_ELEMENTAL, 10, 1, 2
        );
        SpawnPlacements.register(
                RDEntityTypes.ICE_ELEMENTAL,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                IceElementalEntity::canSpawn
        );
        // 月兔
        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(BiomeInit.THE_MOON),
                MobCategory.MONSTER,
                RDEntityTypes.MOON_RABBIT, 10, 1, 1
        );
        BiomeModifications.create(ResourceLocation.fromNamespaceAndPath("reverie_dreams", "moon_spawns"))
                .add(ModificationPhase.ADDITIONS,
                        BiomeSelectors.includeByKey(BiomeInit.THE_MOON),
                        ctx -> ctx.getSpawnSettings().addSpawn(
                                MobCategory.MONSTER,
                                new MobSpawnSettings.SpawnerData(RDEntityTypes.MOON_RABBIT, 1, 1),
                                5
                        )
                );
        // UFO
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_MOUNTAIN_PEAK),
                MobCategory.MONSTER,
                RDEntityTypes.UFO, 3, 1, 2
        );
        SpawnPlacements.register(
                RDEntityTypes.UFO,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                UfoEntity::checkSpawnRules
        );

//        SpawnRestriction.register(
//                ModEntities.MOON_RABBIT,
//                SpawnLocationTypes.ON_GROUND,
//                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
//                (entity, world, reason, pos, random) -> {
//                    return world.getBlockState(pos.up()).isAir();
//                }
//        );
//        SpawnRestriction.register(
//                ModEntities.MOON_RABBIT,
//                SpawnLocationTypes.ON_GROUND,
//                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
//                (entity, world, reason, pos, random) ->
//                        world.getBlockState(pos.down()).isOf(ModBlocks.MOON_STONE.block()) &&
//                                world.getBlockState(pos).isAir()
//        );
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
