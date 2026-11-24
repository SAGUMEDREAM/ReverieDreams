package cc.thonly.reverie_dreams.world;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.aquatic.AquaticSpecies;
import cc.thonly.reverie_dreams.entity.aquatic.CuisineAquaticEntity;
import cc.thonly.reverie_dreams.registry.content.entity.RDAquaticEntities;
import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import dev.architectury.registry.level.biome.BiomeModifications;
import dev.architectury.registry.level.entity.SpawnPlacementsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

/**
 * Additive spawn entries; all animals still share vanilla's WATER_AMBIENT population budget.
 */
public final class AquaticSpawning {
    public static final int POPULATION_RADIUS = 48;
    public static final int POPULATION_VERTICAL_RADIUS = 16;

    private AquaticSpawning() {
    }

    public static TagKey<Biome> biomeTag(AquaticSpecies species) {
        return TagKey.create(Registries.BIOME, ReverieDreams.id("spawns/" + species.key()));
    }

    public static int populationLimit(AquaticSpecies species) {
        return switch (species) {
            case SUPREME_TUNA, OCTOPUS -> 4;
            default -> 6;
        };
    }

    public static int chanceDenominator(AquaticSpecies species) {
        return switch (species) {
            case SUPREME_TUNA -> 32;
            case TUNA, OCTOPUS -> 10;
            default -> 6;
        };
    }

    public static int minimumWaterColumn(AquaticSpecies species) {
        return switch (species) {
            case SUPREME_TUNA -> 10;
            case TUNA -> 6;
            case OCTOPUS -> 4;
            case TROUT, SALMON -> 3;
            default -> 2;
        };
    }

    public static void initialize() {
        for (AquaticSpecies species : AquaticSpecies.values()) {
            SpawnPlacementsRegistry.register(RDAquaticEntities.type(species), SpawnPlacementTypes.IN_WATER,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    (type, world, reason, pos, random) -> canSpawn(species, world, reason, pos, random));
        }
        BiomeModifications.addProperties((context, mutable) -> {
            if (!ReverieDreams.config().enableAquaticSpawn)
                return;
            for (AquaticSpecies species : AquaticSpecies.values()) {
                if (context.hasTag(biomeTag(species))) {
                    mutable.getSpawnProperties().addSpawn(MobCategory.WATER_AMBIENT,
                            new MobSpawnSettings.SpawnerData(RDAquaticEntities.type(species).value(), 2, 6), 2);
                }
            }
        });
    }

    public static boolean isNatural(EntitySpawnReason reason) {
        return reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION;
    }

    public static boolean allowsDimension(ResourceKey<Level> dimension) {
        return dimension.equals(Level.OVERWORLD) || dimension.equals(RDBuiltinLevels.GENSOKYO);
    }

    public static boolean canSpawn(AquaticSpecies species, ServerLevelAccessor world, EntitySpawnReason reason,
                                   BlockPos pos, RandomSource random) {
        if (!isSourceWater(world, pos))
            return false;
        if (!isNatural(reason))
            return true;
        var config = ReverieDreams.config();
        if (!config.enableAquaticSpawn || config.aquaticLocalPopulationLimit <= 0
                || !allowsDimension(world.getLevel().dimension()) || !world.getBiome(pos).is(biomeTag(species)))
            return false;
        if (random.nextInt(chanceDenominator(species)) != 0 || !hasHabitat(species, world, pos))
            return false;
        return hasPopulationSpace(species, world, pos, Math.min(16, config.aquaticLocalPopulationLimit));
    }

    public static boolean hasHabitat(AquaticSpecies species, ServerLevelAccessor world, BlockPos pos) {
        if (!isSourceWater(world, pos) || !isSourceWater(world, pos.north()) || !isSourceWater(world, pos.south())
                || !isSourceWater(world, pos.east()) || !isSourceWater(world, pos.west()))
            return false;
        int depth = 1;
        for (int dy = 1; dy < 12 && isSourceWater(world, pos.above(dy)); dy++)
            depth++;
        for (int dy = 1; dy < 12 && isSourceWater(world, pos.below(dy)); dy++)
            depth++;
        if (depth < minimumWaterColumn(species))
            return false;
        if (species == AquaticSpecies.TROUT || species == AquaticSpecies.SALMON
                || species == AquaticSpecies.TUNA || species == AquaticSpecies.SUPREME_TUNA)
            return true;

        // Bottom dwellers need an actual supporting bed close below, never an endless water column.
        int bedRange = species == AquaticSpecies.OCTOPUS || species == AquaticSpecies.LAMPREY ? 3 : 1;
        for (int dy = 1; dy <= bedRange; dy++) {
            BlockPos bed = pos.below(dy);
            if (isSourceWater(world, bed))
                continue;
            var state = world.getBlockState(bed);
            if (!state.isFaceSturdy(world, bed, Direction.UP) || state.is(Blocks.MAGMA_BLOCK))
                return false;
            return species != AquaticSpecies.SEA_URCHIN || state.is(Blocks.STONE) || state.is(Blocks.GRAVEL)
                    || state.is(Blocks.SAND) || state.is(Blocks.ANDESITE) || state.is(Blocks.TUFF)
                    || state.is(Blocks.COBBLESTONE);
        }
        return false;
    }

    public static boolean hasPopulationSpace(AquaticSpecies species, ServerLevelAccessor world, BlockPos pos, int totalLimit) {
        if (totalLimit <= 0)
            return false;
        var nearby = world.getLevel().getEntitiesOfClass(CuisineAquaticEntity.class,
                new AABB(pos).inflate(POPULATION_RADIUS, POPULATION_VERTICAL_RADIUS, POPULATION_RADIUS),
                animal -> animal.isAlive() && !animal.isRemoved());
        if (nearby.size() >= totalLimit)
            return false;
        int sameSpecies = 0;
        for (CuisineAquaticEntity animal : nearby) {
            if (animal.species() == species && ++sameSpecies >= populationLimit(species))
                return false;
        }
        return true;
    }

    private static boolean isSourceWater(ServerLevelAccessor world, BlockPos pos) {
        if (!world.getLevel().hasChunkAt(pos))
            return false;
        var fluid = world.getFluidState(pos);
        return fluid.is(FluidTags.WATER) && fluid.isSource();
    }
}
