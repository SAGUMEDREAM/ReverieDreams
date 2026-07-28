package cc.thonly.reverie_dreams.world;

import cc.thonly.keine.tag.ConventionalBiomeTags;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.UFO;
import cc.thonly.reverie_dreams.entity.elemental.IceElementalEntity;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.util.PlatformContext;
import cc.thonly.reverie_dreams.world.gen.PlacedFeaturesInit;
import cc.thonly.reverie_dreams.world.gen.RDBiomes;
import dev.architectury.hooks.level.biome.GenerationProperties;
import dev.architectury.hooks.level.biome.SpawnProperties;
import dev.architectury.registry.level.biome.BiomeModifications;
import dev.architectury.registry.level.entity.SpawnPlacementsRegistry;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
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

import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@Slf4j
@SuppressWarnings("UnstableApiUsage")
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
                                    pos.getX() - 8, pos.getY() - 4, pos.getZ() - 8,
                                    pos.getX() + 8, pos.getY() + 4, pos.getZ() + 8
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
                                    pos.getX() - 8, pos.getY() - 4, pos.getZ() - 8,
                                    pos.getX() + 8, pos.getY() + 4, pos.getZ() + 8
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
                Monster::checkMonsterSpawnRules
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
        BiomeModifications.addProperties((context, mutable) -> {
            Optional<Identifier> keyOptional = context.getKey();
            if (keyOptional.isEmpty()) {
                return;
            }

            Identifier id = keyOptional.get();

            if (Objects.equals(Biomes.BIRCH_FOREST.identifier(), id)
                    || Objects.equals(Biomes.SAVANNA.identifier(), id)) {

                GenerationProperties.Mutable generationProperties =
                        mutable.getGenerationProperties();

                generationProperties.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        PlacedFeaturesInit.SPIRITUAL_TREE_KEY
                );
            }
        });
        BiomeModifications.addProperties((context, mutable) -> {
            Optional<Identifier> keyOptional = context.getKey();
            if (keyOptional.isEmpty()) {
                return;
            }

            Identifier id = keyOptional.get();

            if (Objects.equals(Biomes.FOREST.identifier(), id)) {

                GenerationProperties.Mutable generationProperties =
                        mutable.getGenerationProperties();

                generationProperties.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        PlacedFeaturesInit.LEMON_TREE_KEY
                );
            }
        });
        BiomeModifications.addProperties((context, mutable) -> {
            Optional<Identifier> keyOptional = context.getKey();
            if (keyOptional.isEmpty()) {
                return;
            }

            Identifier id = keyOptional.get();

            if (Objects.equals(Biomes.SAVANNA.identifier(), id)
                    || Objects.equals(Biomes.JUNGLE.identifier(), id)) {

                GenerationProperties.Mutable generationProperties =
                        mutable.getGenerationProperties();

                generationProperties.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        PlacedFeaturesInit.GINKGO_TREE_KEY
                );
            }
        });
        BiomeModifications.addProperties((context, mutable) -> {
            Optional<Identifier> keyOptional = context.getKey();
            if (keyOptional.isEmpty()) {
                return;
            }

            Identifier id = keyOptional.get();

            if (Objects.equals(Biomes.FOREST.identifier(), id)
                    || Objects.equals(Biomes.JUNGLE.identifier(), id)) {

                GenerationProperties.Mutable generationProperties =
                        mutable.getGenerationProperties();

                generationProperties.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        PlacedFeaturesInit.PEACH_TREE_KEY
                );
            }
        });
    }

    public static void addBlock() {
        BiomeModifications.addProperties((context, mutable) -> {

            if (!context.hasTag(BiomeTags.IS_OVERWORLD)) {
                return;
            }

            GenerationProperties.Mutable generationProperties =
                    mutable.getGenerationProperties();


            // 银矿石
            generationProperties.addFeature(
                    GenerationStep.Decoration.UNDERGROUND_ORES,
                    PlacedFeaturesInit.OVERWORLD_SILVER_ORE_KEY
            );


            // 宝玉矿石
            generationProperties.addFeature(
                    GenerationStep.Decoration.UNDERGROUND_ORES,
                    PlacedFeaturesInit.OVERWORLD_ORB_ORE_KEY
            );

        });
    }

    public static void addFlower() {
        BiomeModifications.addProperties((context, mutable) -> {

            Optional<Identifier> keyOptional = context.getKey();
            if (keyOptional.isEmpty()) {
                return;
            }

            Identifier id = keyOptional.get();

            GenerationProperties.Mutable generationProperties =
                    mutable.getGenerationProperties();


            // 幻昙华
            if (Objects.equals(Biomes.SNOWY_PLAINS.identifier(), id)
                    || Objects.equals(Biomes.FLOWER_FOREST.identifier(), id)) {

                generationProperties.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        PlacedFeaturesInit.UDUMBARA_FLOWER_KEY
                );
            }


            // 银耳丛
            if (Objects.equals(Biomes.FOREST.identifier(), id)
                    || Objects.equals(Biomes.DARK_FOREST.identifier(), id)
                    || Objects.equals(Biomes.BIRCH_FOREST.identifier(), id)) {

                generationProperties.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        PlacedFeaturesInit.TREMELLA_KEY
                );
            }

        });
    }

    public static void addEntity() {
        // 妖精大类
        BiomeModifications.addProperties((context, mutable) -> {
            // 配置关闭时不添加妖精生成
            if (!ReverieDreams.config().enableYouseiSpawn) {
                return;
            }

            SpawnProperties.Mutable spawnProperties =
                    mutable.getSpawnProperties();


            boolean isPlains = context.hasTag(ConventionalBiomeTags.IS_PLAINS);

            boolean isDream = context.getKey()
                                     .map(id -> Objects.equals(id, RDBiomes.DREAM.identifier()))
                                     .orElse(false);


            // 普通妖精
            if (isPlains) {
                spawnProperties.addSpawn(
                        MobCategory.MONSTER,
                        new MobSpawnSettings.SpawnerData(
                                RDEntityTypes.YOUSEI.value(),
                                1,
                                2
                        ),
                        10
                );

                // 向日葵妖精
                spawnProperties.addSpawn(
                        MobCategory.MONSTER,
                        new MobSpawnSettings.SpawnerData(
                                RDEntityTypes.SUNFLOWER_YOUSEI.value(),
                                1,
                                3
                        ),
                        1
                );

                // 女仆妖精
                spawnProperties.addSpawn(
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
                spawnProperties.addSpawn(
                        MobCategory.MONSTER,
                        new MobSpawnSettings.SpawnerData(
                                RDEntityTypes.YOUSEI.value(),
                                1,
                                2
                        ),
                        2
                );

                spawnProperties.addSpawn(
                        MobCategory.MONSTER,
                        new MobSpawnSettings.SpawnerData(
                                RDEntityTypes.SUNFLOWER_YOUSEI.value(),
                                1,
                                3
                        ),
                        3
                );
            }
        });

        BiomeModifications.addProperties((context, mutable) -> {
            SpawnProperties.Mutable spawnProperties =
                    mutable.getSpawnProperties();

            // 野猪
            if (context.hasTag(ConventionalBiomeTags.IS_FOREST)) {
                spawnProperties.addSpawn(
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
            if (context.hasTag(ConventionalBiomeTags.IS_BIRCH_FOREST)) {
                spawnProperties.addSpawn(
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
            if (context.hasTag(ConventionalBiomeTags.IS_FOREST)) {
                spawnProperties.addSpawn(
                        MobCategory.MONSTER,
                        new MobSpawnSettings.SpawnerData(
                                RDEntityTypes.HAIRBALL.value(),
                                2,
                                4
                        ),
                        10
                );
            }


            // 哥布林
            if (context.hasTag(ConventionalBiomeTags.IS_DESERT)) {
                spawnProperties.addSpawn(
                        MobCategory.MONSTER,
                        new MobSpawnSettings.SpawnerData(
                                RDEntityTypes.GOBLIN.value(),
                                1,
                                1
                        ),
                        50 / 5
                );


                // Oni 沙漠
                spawnProperties.addSpawn(
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
            if (context.hasTag(ConventionalBiomeTags.IS_DARK_FOREST) || context.hasTag(ConventionalBiomeTags.IS_MUSHROOM)) {
                spawnProperties.addSpawn(
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
            if (context.hasTag(ConventionalBiomeTags.IS_SNOWY)
                    || context.hasTag(ConventionalBiomeTags.IS_SNOWY_PLAINS)
                    || context.hasTag(ConventionalBiomeTags.IS_COLD_END)) {

                spawnProperties.addSpawn(
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
            context.getKey().ifPresent(id -> {
                if (Objects.equals(id, RDBiomes.THE_MOON.identifier())) {

                    spawnProperties.addSpawn(
                            MobCategory.MONSTER,
                            new MobSpawnSettings.SpawnerData(
                                    RDEntityTypes.MOON_RABBIT.value(),
                                    1,
                                    1
                            ),
                            10
                    );
                }
            });

            // UFO
            if (context.hasTag(ConventionalBiomeTags.IS_MOUNTAIN_PEAK)) {
                spawnProperties.addSpawn(
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
            if (context.hasTag(ConventionalBiomeTags.IS_DARK_FOREST)) {
                spawnProperties.addSpawn(
                        MobCategory.MONSTER,
                        new MobSpawnSettings.SpawnerData(
                                RDEntityTypes.ONI.value(),
                                1,
                                2
                        ),
                        2
                );
            }

        });
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
