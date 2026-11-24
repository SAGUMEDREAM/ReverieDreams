package cc.thonly.reverie_dreams.world.gensokyo.landmark;

import cc.thonly.reverie_dreams.registry.content.block.RDLandmarkBlocks;
import cc.thonly.reverie_dreams.world.gensokyo.understory.GensokyoUnderstoryFeature;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/**
 * One chunk-sized patch: no height edits, no flower walls, no writes into neighbouring biomes.
 */
public final class LandmarkVegetationFeature extends Feature<LandmarkVegetationConfiguration> {
    public LandmarkVegetationFeature(Codec<LandmarkVegetationConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<LandmarkVegetationConfiguration> context) {
        return placePatch(context.level(), context.origin(), context.config().theme(), context.random());
    }

    public static boolean placePatch(WorldGenLevel level, BlockPos origin, LandmarkVegetationConfiguration.Theme theme, RandomSource random) {
        boolean natural = level instanceof WorldGenRegion;
        int startX = natural ? Math.floorDiv(origin.getX(), 16) * 16 : origin.getX() - 8;
        int startZ = natural ? Math.floorDiv(origin.getZ(), 16) * 16 : origin.getZ() - 8;
        int placed = 0;
        long seed = level.getSeed();
        for (int dz = 0; dz < 16; dz++)
            for (int dx = 0; dx < 16; dx++) {
                int x = startX + dx, z = startZ + dz;
                double density = LandmarkDistribution.density(theme, seed, x, z);
                if (random.nextDouble() > density)
                    continue;
                BlockPos pos = ground(level, x, z);
                if (pos == null || (natural && !level.getBiome(pos).is(GensokyoLandmarkBootstrap.biome(theme)))
                        || GensokyoUnderstoryFeature.isNearProtectedGround(level, pos) || !gentle(level, pos))
                    continue;
                BlockState plant = choose(theme, seed, x, z, random);
                if (plant.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                    Direction heading = theme == LandmarkVegetationConfiguration.Theme.GARDEN_OF_THE_SUN && random.nextInt(5) != 0
                            ? Direction.SOUTH : Direction.Plane.HORIZONTAL.getRandomDirection(random);
                    plant = plant.setValue(BlockStateProperties.HORIZONTAL_FACING, heading);
                }
                if (plant.hasProperty(BlockStateProperties.FLOWER_AMOUNT))
                    plant = plant.setValue(BlockStateProperties.FLOWER_AMOUNT, 1 + random.nextInt(3));
                if (!plant.canSurvive(level, pos) || !clear(level, pos))
                    continue;
                if (plant.getBlock() instanceof DoublePlantBlock) {
                    BlockPos upper = pos.above();
                    if (!clear(level, upper))
                        continue;
                    // Preflight both halves before the first write; preserve static direction on both.
                    level.setBlock(pos, plant.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER), Block.UPDATE_CLIENTS);
                    level.setBlock(upper, plant.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_CLIENTS);
                } else
                    level.setBlock(pos, plant, Block.UPDATE_CLIENTS);
                placed++;
            }
        return placed > 0;
    }

    public static BlockState choose(LandmarkVegetationConfiguration.Theme theme, long seed, int x, int z, RandomSource random) {
        double colour = LandmarkDistribution.field(seed ^ 0x434f4c4f5552L, x, z, 22);
        return switch (theme) {
            case PHANTOM_MEADOW -> {
                int roll = random.nextInt(10);
                if (roll < 4)
                    yield RDLandmarkBlocks.CLOVER_PATCH.defaultBlockState();
                if (roll == 4)
                    yield Blocks.SHORT_GRASS.defaultBlockState();
                yield (colour < .36 ? Blocks.POPPY : colour > .64 ? Blocks.ORANGE_TULIP : Blocks.DANDELION).defaultBlockState();
            }
            case MUENZUKA -> random.nextInt(7) < 5 ? RDLandmarkBlocks.RED_SPIDER_LILY.defaultBlockState()
                    : RDLandmarkBlocks.PURPLE_CHERRY_PETALS.defaultBlockState();
            case GARDEN_OF_THE_SUN -> random.nextInt(5) == 0 ? RDLandmarkBlocks.SHORT_SUNFLOWER.defaultBlockState()
                    : RDLandmarkBlocks.GARDEN_SUNFLOWER.defaultBlockState();
        };
    }

    private static boolean gentle(WorldGenLevel level, BlockPos centre) {
        for (Direction d : Direction.Plane.HORIZONTAL) {
            BlockPos soil = ground(level, centre.getX() + d.getStepX() * 2, centre.getZ() + d.getStepZ() * 2);
            if (soil == null || Math.abs(soil.getY() - centre.getY()) > 2)
                return false;
        }
        return true;
    }

    private static boolean writable(WorldGenLevel level, BlockPos pos) {
        return !level.isOutsideBuildHeight(pos) && level.ensureCanWrite(pos);
    }

    private static boolean clear(WorldGenLevel level, BlockPos pos) {
        if (!writable(level, pos) || !level.getFluidState(pos).isEmpty() || level.getBlockEntity(pos) != null)
            return false;
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN);
    }

    private static BlockPos ground(WorldGenLevel level, int x, int z) {
        BlockPos probe = new BlockPos(x, level.getMinY() + 1, z);
        if (!writable(level, probe))
            return null;
        int top = level.getHeight(level instanceof WorldGenRegion ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.OCEAN_FLOOR, x, z);
        for (int y = top; y >= Math.max(level.getMinY() + 1, top - 48); y--) {
            BlockPos p = new BlockPos(x, y, z);
            if (!writable(level, p) || !writable(level, p.below()))
                return null;
            BlockState below = level.getBlockState(p.below());
            if (!level.getFluidState(p.below()).isEmpty())
                return null;
            if (below.is(BlockTags.SUBSTRATE_OVERWORLD))
                return p;
            if (!below.isAir() && !below.is(BlockTags.LOGS) && !below.is(BlockTags.LEAVES)
                    && !below.canBeReplaced())
                return null;
        }
        return null;
    }
}
