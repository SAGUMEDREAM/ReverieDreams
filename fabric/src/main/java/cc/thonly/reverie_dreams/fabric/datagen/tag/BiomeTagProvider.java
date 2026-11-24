package cc.thonly.reverie_dreams.fabric.datagen.tag;

import cc.thonly.reverie_dreams.api.registry.tag.ConventionalBiomeTags;
import cc.thonly.reverie_dreams.entity.aquatic.AquaticSpecies;
import cc.thonly.reverie_dreams.registry.tag.RDBiomeTags;
import cc.thonly.reverie_dreams.world.AquaticSpawning;
import cc.thonly.reverie_dreams.world.LargeBambooGeneration;
import cc.thonly.reverie_dreams.world.gen.RDBuiltinBiomes;
import cc.thonly.reverie_dreams.world.gensokyo.bamboo.BambooForestBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.landmark.GensokyoLandmarkBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.landmark.LandmarkVegetationConfiguration;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestType;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unchecked")
public final class BiomeTagProvider extends FabricTagsProvider<Biome> {
    public BiomeTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.BIOME, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        addBiomeClassificationTags();
        addLargeBambooTags();
        addEntitySpawnTags();
        addAquaticSpawnTags();
    }

    private void addAquaticSpawnTags() {
        builder(AquaticSpawning.biomeTag(AquaticSpecies.TROUT)).add(Biomes.RIVER, Biomes.FROZEN_RIVER);
        builder(AquaticSpawning.biomeTag(AquaticSpecies.LAMPREY)).add(Biomes.SWAMP, Biomes.MANGROVE_SWAMP);
        builder(AquaticSpawning.biomeTag(AquaticSpecies.SALMON)).add(Biomes.FROZEN_RIVER);
        builder(AquaticSpawning.biomeTag(AquaticSpecies.TUNA)).add(Biomes.OCEAN, Biomes.DEEP_OCEAN, Biomes.RIVER);
        builder(AquaticSpawning.biomeTag(AquaticSpecies.SUPREME_TUNA))
                .add(Biomes.DEEP_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.RIVER);
        builder(AquaticSpawning.biomeTag(AquaticSpecies.SHRIMP)).add(Biomes.SWAMP, Biomes.MANGROVE_SWAMP, Biomes.RIVER);
        builder(AquaticSpawning.biomeTag(AquaticSpecies.CRAB))
                .add(Biomes.BEACH, Biomes.STONY_SHORE, Biomes.MANGROVE_SWAMP, Biomes.RIVER);
        builder(AquaticSpawning.biomeTag(AquaticSpecies.OCTOPUS))
                .add(Biomes.BEACH, Biomes.LUKEWARM_OCEAN, Biomes.WARM_OCEAN);
        builder(AquaticSpawning.biomeTag(AquaticSpecies.SEA_URCHIN))
                .add(Biomes.BEACH, Biomes.LUKEWARM_OCEAN, Biomes.WARM_OCEAN);
    }

    private void addBiomeClassificationTags() {
        var overworld = builder(BiomeTags.IS_OVERWORLD);
        var forest = builder(ConventionalBiomeTags.IS_FOREST);
        var deciduous = builder(ConventionalBiomeTags.IS_DECIDUOUS_TREE);
        var coniferous = builder(ConventionalBiomeTags.IS_CONIFEROUS_TREE);
        var gensokyo = builder(RDBiomeTags.GENSOKYO);
        var plains = RDBuiltinBiomes.GENSOKYO_PLAINS;
        var overworldMagicForest = RDBuiltinBiomes.MAGIC_FOREST;
        overworld.add(overworldMagicForest);
        forest.add(overworldMagicForest);
        deciduous.add(overworldMagicForest);
        builder(RDBiomeTags.MAGIC_FOREST)
                .add(overworldMagicForest, GensokyoForestBootstrap.biome(GensokyoForestType.MAGIC_FOREST));
        overworld.add(plains);
        gensokyo.add(plains);
        builder(ConventionalBiomeTags.IS_PLAINS).add(plains);
        gensokyo.addOptional(BambooForestBootstrap.BIOME);
        forest.add(BambooForestBootstrap.BIOME);
        for (var theme : LandmarkVegetationConfiguration.Theme.values()) {
            var key = GensokyoLandmarkBootstrap.biome(theme);
            overworld.add(key);
            gensokyo.addOptional(key);
        }
        for (GensokyoForestType type : GensokyoForestType.values()) {
            var key = GensokyoForestBootstrap.biome(type);
            overworld.add(key);
            forest.add(key);
            if (type == GensokyoForestType.TAIGA) {
                coniferous.add(key);
            } else if (type != GensokyoForestType.JUNGLE) {
                deciduous.add(key);
            }
            gensokyo.addOptional(key);
        }
    }

    private void addLargeBambooTags() {
        builder(LargeBambooGeneration.ABUNDANT_SHOOTS).add(Biomes.BAMBOO_JUNGLE);
        builder(LargeBambooGeneration.HAS_SHOOTS)
                .addTag(LargeBambooGeneration.ABUNDANT_SHOOTS)
                .add(Biomes.JUNGLE)
                .add(Biomes.SPARSE_JUNGLE)
                .add(Biomes.FOREST)
                .add(Biomes.BIRCH_FOREST);
    }

    private void addEntitySpawnTags() {
        builder(RDBiomeTags.FOREST_YOUKAI_SPAWN_AREA).addOptionalTag(ConventionalBiomeTags.IS_FOREST);
        var forest = GensokyoForestBootstrap.biome(GensokyoForestType.FOREST);
        var birchForest = GensokyoForestBootstrap.biome(GensokyoForestType.BIRCH_FOREST);
        var taiga = GensokyoForestBootstrap.biome(GensokyoForestType.TAIGA);
        var cherryGrove = GensokyoForestBootstrap.biome(GensokyoForestType.CHERRY_GROVE);
        var darkForest = GensokyoForestBootstrap.biome(GensokyoForestType.DARK_FOREST);
        var magicForest = GensokyoForestBootstrap.biome(GensokyoForestType.MAGIC_FOREST);
        var mapleForest = GensokyoForestBootstrap.biome(GensokyoForestType.MAPLE_FOREST);

        builder(RDBiomeTags.NORMAL_YOUSEI_SPAWN_AREA)
                .addOptionalTag(ConventionalBiomeTags.IS_PLAINS)
                .addOptionalTag(ConventionalBiomeTags.IS_FOREST)
                .addTag(RDBiomeTags.NORMAL_YOUSEI_PRIMARY_HABITAT)
                .add(RDBuiltinBiomes.DREAM);
        builder(RDBiomeTags.NORMAL_YOUSEI_PRIMARY_HABITAT)
                .add(RDBuiltinBiomes.GENSOKYO_PLAINS, RDBuiltinBiomes.GENSOKYO_PHANTOM_MEADOW)
                .add(forest, birchForest, taiga, cherryGrove, mapleForest, magicForest, BambooForestBootstrap.BIOME);

        builder(RDBiomeTags.SUNFLOWER_YOUSEI_SPAWN_AREA)
                .add(Biomes.SUNFLOWER_PLAINS, Biomes.FLOWER_FOREST, RDBuiltinBiomes.GENSOKYO_PHANTOM_MEADOW)
                .addTag(RDBiomeTags.SUNFLOWER_YOUSEI_PRIMARY_HABITAT)
                .add(RDBuiltinBiomes.DREAM);
        builder(RDBiomeTags.SUNFLOWER_YOUSEI_PRIMARY_HABITAT)
                .add(RDBuiltinBiomes.GENSOKYO_GARDEN_OF_THE_SUN);

        builder(RDBiomeTags.MAID_YOUSEI_SPAWN_AREA)
                .add(Biomes.DARK_FOREST, forest, darkForest)
                .addTag(RDBiomeTags.MAID_YOUSEI_PRIMARY_HABITAT);
        // A datapack can opt an estate biome into the primary habitat.
        builder(RDBiomeTags.MAID_YOUSEI_PRIMARY_HABITAT);

        builder(RDBiomeTags.HAIR_BALL_SPAWN_AREA)
                .addOptionalTag(ConventionalBiomeTags.IS_FOREST)
                .addTag(RDBiomeTags.HAIR_BALL_PRIMARY_HABITAT);
        builder(RDBiomeTags.HAIR_BALL_PRIMARY_HABITAT)
                .add(Biomes.FOREST, Biomes.BIRCH_FOREST, Biomes.DARK_FOREST)
                .add(forest, birchForest, magicForest, darkForest, mapleForest);

        builder(RDBiomeTags.TENGU_SPAWN_AREA)
                .add(RDBuiltinBiomes.MAPLE_FOREST)
                .addTag(RDBiomeTags.TENGU_PRIMARY_HABITAT);
        builder(RDBiomeTags.TENGU_PRIMARY_HABITAT).add(mapleForest);
        builder(RDBiomeTags.YOUSEI_LOW_DENSITY_SPAWN_AREA).add(mapleForest, taiga);

        builder(RDBiomeTags.YOUKAI_RABBIT_SPAWN_AREA)
                .add(Biomes.BAMBOO_JUNGLE)
                .addTag(RDBiomeTags.YOUKAI_RABBIT_PRIMARY_HABITAT);
        builder(RDBiomeTags.YOUKAI_RABBIT_PRIMARY_HABITAT).add(BambooForestBootstrap.BIOME);

        builder(RDBiomeTags.WILD_PIG_SPAWN_AREA).addOptionalTag(ConventionalBiomeTags.IS_FOREST);
        builder(RDBiomeTags.KILLER_BEE_SPAWN_AREA)
                .addOptionalTag(ConventionalBiomeTags.IS_BIRCH_FOREST)
                .add(birchForest);
        builder(RDBiomeTags.GOBLIN_SPAWN_AREA).addOptionalTag(ConventionalBiomeTags.IS_DESERT);
        builder(RDBiomeTags.ONI_SPAWN_AREA)
                .addOptionalTag(ConventionalBiomeTags.IS_DESERT)
                .addOptionalTag(ConventionalBiomeTags.IS_DARK_FOREST)
                .add(darkForest);
        builder(RDBiomeTags.MUSHROOM_MONSTER_SPAWN_AREA)
                .addOptionalTag(ConventionalBiomeTags.IS_DARK_FOREST)
                .addOptionalTag(ConventionalBiomeTags.IS_MUSHROOM)
                .add(darkForest)
                .addTag(RDBiomeTags.MAGIC_FOREST);
        builder(RDBiomeTags.ICE_ELEMENTAL_SPAWN_AREA)
                .addOptionalTag(ConventionalBiomeTags.IS_SNOWY)
                .addOptionalTag(ConventionalBiomeTags.IS_SNOWY_PLAINS)
                .addOptionalTag(ConventionalBiomeTags.IS_COLD_END);
        builder(RDBiomeTags.UFO_SPAWN_AREA).addOptionalTag(ConventionalBiomeTags.IS_MOUNTAIN_PEAK);
        builder(RDBiomeTags.GHOST_SPAWN_AREA).add(RDBuiltinBiomes.GENSOKYO_MUENZUKA);
    }
}
