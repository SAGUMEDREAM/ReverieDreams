package cc.thonly.reverie_dreams.world;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.registry.tag.ConventionalBiomeTags;
import cc.thonly.reverie_dreams.entity.UFO;
import cc.thonly.reverie_dreams.entity.Hairball;
import cc.thonly.reverie_dreams.entity.YoukaiRabbit;
import cc.thonly.reverie_dreams.entity.elemental.IceElementalEntity;
import cc.thonly.reverie_dreams.entity.npc.BaseYousei;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.world.gen.RDBuiltinConfigurationCarvers;
import cc.thonly.reverie_dreams.world.gen.RDBuiltinPlacedFeatures;
import cc.thonly.reverie_dreams.world.gen.RDBuiltinBiomes;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestType;
import cc.thonly.reverie_dreams.world.moon.MoonBiomes;
import dev.architectury.hooks.level.biome.GenerationProperties;
import dev.architectury.registry.level.biome.BiomeModifications;
import dev.architectury.registry.level.entity.SpawnPlacementsRegistry;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.biome.Biomes;
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
        AquaticSpawning.initialize();
        LargeBambooGeneration.initialize();
        addBlock();
        addFlower();
        addTree();
        addEntity();
        addStructure();
    }

    public static void addSpawnPlacements() {
        SpawnPlacementsRegistry.register(RDEntityTypes.YOUKAI_CAT,
                SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, LandEntitySpawnRules::forestYoukai);
        SpawnPlacementsRegistry.register(RDEntityTypes.YOUKAI_TANUKI,
                SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, LandEntitySpawnRules::forestYoukai);
        SpawnPlacementsRegistry.register(RDEntityTypes.YOUKAI_FOX,
                SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, LandEntitySpawnRules::forestYoukai);
        SpawnPlacementsRegistry.register(RDEntityTypes.GHOST,
                SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, LandEntitySpawnRules::ghost);
        SpawnPlacementsRegistry.register(RDEntityTypes.WHITE_WOLF_TENGU,
                SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, LandEntitySpawnRules::tengu);
        SpawnPlacementsRegistry.register(RDEntityTypes.CROW_TENGU,
                SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, LandEntitySpawnRules::tengu);
        SpawnPlacementsRegistry.register(RDEntityTypes.YOUSEI,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                BaseYousei::getSpawnPredicate
        );
        SpawnPlacementsRegistry.register(RDEntityTypes.MAID_YOUSEI,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                BaseYousei::getSpawnPredicate
        );
        SpawnPlacementsRegistry.register(RDEntityTypes.SUNFLOWER_YOUSEI,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                BaseYousei::getSpawnPredicate
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
                LandEntitySpawnRules::wildPig
        );
        SpawnPlacementsRegistry.register(
                RDEntityTypes.WAGYU,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules
        );
        SpawnPlacementsRegistry.register(
                RDEntityTypes.HAIRBALL,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Hairball::checkSpawnRules
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
                LandEntitySpawnRules::mushroomMonster
        );
        SpawnPlacementsRegistry.register(
                RDEntityTypes.YOUKAI_RABBIT,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                YoukaiRabbit::checkSpawnRules
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
                        RDBuiltinPlacedFeatures.SPIRITUAL_TREE_KEY
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
                        RDBuiltinPlacedFeatures.LEMON_TREE_KEY
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
                        RDBuiltinPlacedFeatures.GINKGO_TREE_KEY
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
                        RDBuiltinPlacedFeatures.PEACH_TREE_KEY
                );
            }
        });
        // 梅子树：森林
        BiomeModifications.addProperties((context, mutable) -> {
            Optional<Identifier> keyOptional = context.getKey();
            if (keyOptional.isEmpty()) {
                return;
            }

            Identifier id = keyOptional.get();

            if (Objects.equals(Biomes.FOREST.identifier(), id)
                    || Objects.equals(Biomes.FLOWER_FOREST.identifier(), id)) {

                GenerationProperties.Mutable generationProperties =
                        mutable.getGenerationProperties();

                generationProperties.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        RDBuiltinPlacedFeatures.PLUM_TREE_KEY
                );
            }
        });
        // 松树：针叶林
        BiomeModifications.addProperties((context, mutable) -> {
            Optional<Identifier> keyOptional = context.getKey();
            if (keyOptional.isEmpty()) {
                return;
            }

            Identifier id = keyOptional.get();

            if (Objects.equals(Biomes.TAIGA.identifier(), id)
                    || Objects.equals(Biomes.OLD_GROWTH_PINE_TAIGA.identifier(), id)
                    || Objects.equals(Biomes.SNOWY_TAIGA.identifier(), id)) {

                GenerationProperties.Mutable generationProperties =
                        mutable.getGenerationProperties();

                generationProperties.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        RDBuiltinPlacedFeatures.PINE_TREE_KEY
                );
            }
        });
        // 板栗树：深色森林
        BiomeModifications.addProperties((context, mutable) -> {
            Optional<Identifier> keyOptional = context.getKey();
            if (keyOptional.isEmpty()) {
                return;
            }

            Identifier id = keyOptional.get();

            if (Objects.equals(Biomes.DARK_FOREST.identifier(), id)) {

                GenerationProperties.Mutable generationProperties =
                        mutable.getGenerationProperties();

                generationProperties.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        RDBuiltinPlacedFeatures.CHESTNUT_TREE_KEY
                );
            }
        });
        // 枫树不再注入原版森林/桦木/花林/樱花林；只由 reverie_dreams:maple_forest 群系生成。
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
                    RDBuiltinPlacedFeatures.OVERWORLD_SILVER_ORE_KEY
            );
            generationProperties.addFeature(
                    GenerationStep.Decoration.UNDERGROUND_ORES,
                    RDBuiltinPlacedFeatures.OVERWORLD_SILVER_SMALL_KEY
            );


            // 宝玉矿石
            generationProperties.addFeature(
                    GenerationStep.Decoration.UNDERGROUND_ORES,
                    RDBuiltinPlacedFeatures.OVERWORLD_ORB_ORE_KEY
            );
            if (context.getKey().filter(Biomes.DRIPSTONE_CAVES.identifier()::equals).isPresent()) {
                generationProperties.addFeature(
                        GenerationStep.Decoration.UNDERGROUND_ORES,
                        RDBuiltinPlacedFeatures.OVERWORLD_ORB_DRIPSTONE_KEY
                );
            }

        });
        BiomeModifications.addProperties((context, mutable) -> {
            if (!context.hasTag(BiomeTags.IS_NETHER)) {
                return;
            }

            GenerationProperties.Mutable generationProperties =
                    mutable.getGenerationProperties();

            generationProperties.addFeature(
                    GenerationStep.Decoration.UNDERGROUND_ORES,
                    RDBuiltinPlacedFeatures.NETHER_BLACK_SALT_ORE_KEY
            );
        });
        BiomeModifications.addProperties((context, mutable) -> {
            Optional<Identifier> keyOptional = context.getKey();
            if (keyOptional.isEmpty()) {
                return;
            }
            if (!keyOptional.get().equals(RDBuiltinBiomes.THE_MOON.identifier())
                    && !keyOptional.get().equals(RDBuiltinBiomes.MOON_SEA.identifier())
                    && !keyOptional.get().equals(MoonBiomes.BASALT_MARE.identifier())
                    && !keyOptional.get().equals(MoonBiomes.SHADOW_ICE_CRATER.identifier())) {
                return;
            }
            GenerationProperties.Mutable generationProperties = mutable.getGenerationProperties();
            generationProperties.addFeature(
                    GenerationStep.Decoration.UNDERGROUND_ORES,
                    RDBuiltinPlacedFeatures.MOON_GOLD_ORE_KEY
            );
            // Water belongs to the continuous boundary basins, not random lakes in the dry lunar terrain.
            generationProperties.addFeature(
                    GenerationStep.Decoration.UNDERGROUND_ORES,
                    RDBuiltinPlacedFeatures.MOON_IRON_ORE_KEY
            );
            generationProperties.addFeature(
                    GenerationStep.Decoration.UNDERGROUND_ORES,
                    RDBuiltinPlacedFeatures.MOON_DIAMOND_ORE_KEY
            );
            generationProperties.addFeature(
                    GenerationStep.Decoration.UNDERGROUND_ORES,
                    RDBuiltinPlacedFeatures.MOON_QUARTZ_ORE_KEY
            );
            generationProperties.addFeature(
                    GenerationStep.Decoration.UNDERGROUND_ORES,
                    RDBuiltinPlacedFeatures.MOON_DIORITE_PLACED_KEY
            );
            generationProperties.addFeature(
                    GenerationStep.Decoration.UNDERGROUND_ORES,
                    RDBuiltinPlacedFeatures.MOON_GRAVEL_PLACED_KEY
            );
            generationProperties.addCarver(
                    RDBuiltinConfigurationCarvers.MOON_CAVE
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
                    || Objects.equals(Biomes.FLOWER_FOREST.identifier(), id)
                    || Objects.equals(GensokyoForestType.FOREST.biomeId(), id)
            ) {

                generationProperties.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        RDBuiltinPlacedFeatures.UDUMBARA_FLOWER_KEY
                );
            }


            // 银耳丛
            if (Objects.equals(Biomes.FOREST.identifier(), id)
                    || Objects.equals(Biomes.DARK_FOREST.identifier(), id)
                    || Objects.equals(Biomes.BIRCH_FOREST.identifier(), id)
                    || Objects.equals(GensokyoForestType.FOREST.biomeId(), id)
            ) {

                generationProperties.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        RDBuiltinPlacedFeatures.TREMELLA_KEY
                );
            }

        });
    }

    public static void addEntity() {
        EntityBiomeSpawns.initialize();
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
