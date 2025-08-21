package cc.thonly.reverie_dreams.world;

import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.entity.elemental.IceElementalEntity;
import cc.thonly.reverie_dreams.world.gen.ModBiomeSources;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.GenerationStep;

import java.util.Random;

public class BiomeModificationInit {
    public static final int BASE_WEIGHT = 80;

    public static void init() {
        addBlock();
        addFlower();
        addTree();
        addEntity();
    }

    public static void addTree() {
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(BiomeKeys.BIRCH_FOREST, BiomeKeys.SAVANNA),
                GenerationStep.Feature.VEGETAL_DECORATION, PlacedFeaturesInit.SPIRITUAL_TREE_KEY);
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(BiomeKeys.FOREST),
                GenerationStep.Feature.VEGETAL_DECORATION, PlacedFeaturesInit.LEMON_TREE_KEY);
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(BiomeKeys.SAVANNA, BiomeKeys.JUNGLE),
                GenerationStep.Feature.VEGETAL_DECORATION, PlacedFeaturesInit.GINKGO_TREE_KEY);
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(BiomeKeys.FOREST, BiomeKeys.JUNGLE),
                GenerationStep.Feature.VEGETAL_DECORATION, PlacedFeaturesInit.PEACH_TREE_KEY);
    }

    public static void addBlock() {
        // 银矿石
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                PlacedFeaturesInit.OVERWORLD_SILVER_ORE_KEY
        );
        // 宝玉矿石
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                PlacedFeaturesInit.OVERWORLD_ORB_ORE_KEY
        );
    }

    public static void addFlower() {
        // 幻昙华花
        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(BiomeKeys.SNOWY_PLAINS, BiomeKeys.FLOWER_FOREST),
                GenerationStep.Feature.VEGETAL_DECORATION,
                PlacedFeaturesInit.UDUMBARA_FLOWER_KEY
        );
        // 银耳丛
        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(BiomeKeys.FOREST, BiomeKeys.DARK_FOREST, BiomeKeys.BIRCH_FOREST),
                GenerationStep.Feature.VEGETAL_DECORATION,
                PlacedFeaturesInit.TREMELLA_KEY
        );
    }

    public static void addEntity() {
        // 妖精
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_PLAINS),
                SpawnGroup.MONSTER,
                ModEntities.YOUSEI_ENTITY_TYPE, 10, 1, 2
        );
        SpawnRestriction.register(
                ModEntities.YOUSEI_ENTITY_TYPE,
                SpawnLocationTypes.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                (entity, world, reason, pos, random) ->
                        world.getBlockState(pos.down()).isOf(Blocks.GRASS_BLOCK) &&
                                world.getBaseLightLevel(pos, 0) > 8 &&
                                world.getBlockState(pos).isAir()
        );
        // 向日葵妖精
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_PLAINS),
                SpawnGroup.MONSTER,
                ModEntities.SUNFLOWER_YOUSEI_ENTITY_TYPE, 3, 1, 3
        );
        SpawnRestriction.register(
                ModEntities.SUNFLOWER_YOUSEI_ENTITY_TYPE,
                SpawnLocationTypes.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                (entity, world, reason, pos, random) ->
                        world.getBlockState(pos.down()).isOf(Blocks.GRASS_BLOCK) &&
                                world.getBaseLightLevel(pos, 0) > 8 &&
                                world.getBlockState(pos).isAir()
        );
        // 杀人蜂
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_BIRCH_FOREST),
                SpawnGroup.MONSTER,
                ModEntities.KILLER_BEE_ENTITY_TYPE, 7, 2, 3
        );
        // 毛玉
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_FOREST),
                SpawnGroup.MONSTER,
                ModEntities.HAIRBALL_ENTITY_TYPE, 10, 2, 4
        );
        // 哥布林
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_DESERT),
                SpawnGroup.MONSTER,
                ModEntities.GOBLIN_ENTITY_TYPE, 50 / 5, 1, 1
        );
        SpawnRestriction.register(
                ModEntities.GOBLIN_ENTITY_TYPE,
                SpawnLocationTypes.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                (entity, world, reason, pos, random) ->
                        world.getBlockState(pos.down()).isOf(Blocks.GRASS_BLOCK) &&
                                world.getBaseLightLevel(pos, 0) > 8 &&
                                world.getBlockState(pos).isAir()
        );
        // 蘑菇怪
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_MUSHROOM),
                SpawnGroup.MONSTER,
                ModEntities.MUSHROOM_MONSTER_ENTITY_TYPE, 8, 1, 2
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_DARK_FOREST),
                SpawnGroup.MONSTER,
                ModEntities.MUSHROOM_MONSTER_ENTITY_TYPE, 8, 1, 2
        );
        // 冰元素
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_SNOWY),
                SpawnGroup.MONSTER,
                ModEntities.ICE_ELEMENTAL_ENTITY_TYPE, 10, 1, 2
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_SNOWY_PLAINS),
                SpawnGroup.MONSTER,
                ModEntities.ICE_ELEMENTAL_ENTITY_TYPE, 10, 1, 2
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_COLD_END),
                SpawnGroup.MONSTER,
                ModEntities.ICE_ELEMENTAL_ENTITY_TYPE, 10, 1, 2
        );
        SpawnRestriction.register(
                ModEntities.ICE_ELEMENTAL_ENTITY_TYPE,
                SpawnLocationTypes.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                IceElementalEntity::canSpawn
        );
        // 月兔
        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(ModBiomeSources.THE_MOON),
                SpawnGroup.MONSTER,
                ModEntities.MOON_RABBIT, 20, 1, 3
        );
        BiomeModifications.create(Identifier.of("reverie_dreams", "moon_spawns"))
                .add(ModificationPhase.ADDITIONS,
                        BiomeSelectors.includeByKey(ModBiomeSources.THE_MOON),
                        ctx -> ctx.getSpawnSettings().addSpawn(
                                SpawnGroup.MONSTER,
                                new SpawnSettings.SpawnEntry(ModEntities.MOON_RABBIT, 1, 2),
                                10
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

    public static boolean canSpawn(EntityType<?> type, ServerWorld world, SpawnReason reason, BlockPos pos, Random random) {
        return world.getLightLevel(pos) <= 7 && world.getBlockState(pos.down()).isSolidBlock(world, pos.down());
    }

}
