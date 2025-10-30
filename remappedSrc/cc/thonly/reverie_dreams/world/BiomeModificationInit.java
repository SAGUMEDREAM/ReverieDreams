package cc.thonly.reverie_dreams.world;

import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.entity.elemental.IceElementalEntity;
import cc.thonly.reverie_dreams.world.gen.BiomeInit;
import cc.thonly.reverie_dreams.world.gen.PlacedFeaturesInit;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
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
        // 妖精
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_PLAINS),
                MobCategory.MONSTER,
                ModEntities.YOUSEI_ENTITY_TYPE, 10, 1, 2
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(BiomeInit.DREAM),
                MobCategory.MONSTER,
                ModEntities.YOUSEI_ENTITY_TYPE, 2, 1, 2
        );
        SpawnPlacements.register(
                ModEntities.YOUSEI_ENTITY_TYPE,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entity, world, reason, pos, random) ->
                        world.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK) &&
                                world.getRawBrightness(pos, 0) > 8 &&
                                world.getBlockState(pos).isAir()
        );
        // 向日葵妖精
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_PLAINS),
                MobCategory.MONSTER,
                ModEntities.SUNFLOWER_YOUSEI_ENTITY_TYPE, 3, 1, 3
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(BiomeInit.DREAM),
                MobCategory.MONSTER,
                ModEntities.SUNFLOWER_YOUSEI_ENTITY_TYPE, 1, 1, 1
        );
        SpawnPlacements.register(
                ModEntities.SUNFLOWER_YOUSEI_ENTITY_TYPE,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entity, world, reason, pos, random) ->
                        world.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK) &&
                                world.getRawBrightness(pos, 0) > 8 &&
                                world.getBlockState(pos).isAir()
        );
        // 杀人蜂
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_BIRCH_FOREST),
                MobCategory.MONSTER,
                ModEntities.KILLER_BEE_ENTITY_TYPE, 7, 2, 3
        );
        // 毛玉
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_FOREST),
                MobCategory.MONSTER,
                ModEntities.HAIRBALL_ENTITY_TYPE, 10, 2, 4
        );
        // 哥布林
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_DESERT),
                MobCategory.MONSTER,
                ModEntities.GOBLIN_ENTITY_TYPE, 50 / 5, 1, 1
        );
        SpawnPlacements.register(
                ModEntities.GOBLIN_ENTITY_TYPE,
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
                ModEntities.MUSHROOM_MONSTER_ENTITY_TYPE, 8, 1, 2
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_DARK_FOREST),
                MobCategory.MONSTER,
                ModEntities.MUSHROOM_MONSTER_ENTITY_TYPE, 8, 1, 2
        );
        // 冰元素
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_SNOWY),
                MobCategory.MONSTER,
                ModEntities.ICE_ELEMENTAL_ENTITY_TYPE, 10, 1, 2
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_SNOWY_PLAINS),
                MobCategory.MONSTER,
                ModEntities.ICE_ELEMENTAL_ENTITY_TYPE, 10, 1, 2
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_COLD_END),
                MobCategory.MONSTER,
                ModEntities.ICE_ELEMENTAL_ENTITY_TYPE, 10, 1, 2
        );
        SpawnPlacements.register(
                ModEntities.ICE_ELEMENTAL_ENTITY_TYPE,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                IceElementalEntity::canSpawn
        );
        // 月兔
        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(BiomeInit.THE_MOON),
                MobCategory.MONSTER,
                ModEntities.MOON_RABBIT_ENTITY_TYPE, 10, 1, 1
        );
        BiomeModifications.create(ResourceLocation.fromNamespaceAndPath("reverie_dreams", "moon_spawns"))
                .add(ModificationPhase.ADDITIONS,
                        BiomeSelectors.includeByKey(BiomeInit.THE_MOON),
                        ctx -> ctx.getSpawnSettings().addSpawn(
                                MobCategory.MONSTER,
                                new MobSpawnSettings.SpawnerData(ModEntities.MOON_RABBIT_ENTITY_TYPE, 1, 1),
                                5
                        )
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
