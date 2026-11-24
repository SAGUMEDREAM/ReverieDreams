package cc.thonly.reverie_dreams.world.gensokyo.deadwood;

import cc.thonly.reverie_dreams.block.nature.SplinteredLogBlock;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestType;
import cc.thonly.reverie_dreams.world.gensokyo.understory.GensokyoUnderstoryFeature;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import cc.thonly.reverie_dreams.world.gensokyo.road.GensokyoRoadProtection;
import net.minecraft.util.RandomSource;

/**
 * Manual specimens only. An obstructed/unsupported specimen performs zero writes.
 */
public final class GensokyoDeadwoodFeature extends Feature<GensokyoDeadwoodConfiguration> {
    public GensokyoDeadwoodFeature(Codec<GensokyoDeadwoodConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<GensokyoDeadwoodConfiguration> context) {
        // Even accidental biome references cannot turn this preview batch into natural decoration.
        if (context.level() instanceof WorldGenRegion)
            return false;
        return placeDeadwood(context.level(), context.origin(), context.config(), context.random().nextLong());
    }

    public static boolean placeDeadwood(WorldGenLevel level, BlockPos origin, GensokyoDeadwoodConfiguration config, long seed) {
        if (config.logs().isEmpty() || config.logs().size() != config.splinters().size())
            return false;
        var random = new Random(seed ^ 0x44454144574f4f44L);
        Direction forward = Direction.Plane.HORIZONTAL.getRandomDirection(RandomSource.create(seed));
        Direction sideways = forward.getClockWise();
        int palette = random.nextInt(config.logs().size());
        var shape = GensokyoDeadwoodShape.create(config.theme(), config.kind(), seed);
        BlockPos centre = ground(level, origin.getX(), origin.getZ(), origin.getY());
        if (centre == null)
            return false;
        var plan = new LinkedHashMap<BlockPos, BlockState>();
        var feet = new HashMap<BlockPos, Boolean>();
        for (var cell : shape.cells()) {
            var p = cell.point();
            BlockPos pos = centre.offset(forward.getStepX() * p.x() + sideways.getStepX() * p.z(), p.y(),
                    forward.getStepZ() * p.x() + sideways.getStepZ() * p.z());
            if (!clear(level, pos))
                return false;
            if (p.y() == 0) {
                BlockPos soil = ground(level, pos.getX(), pos.getZ(), centre.getY());
                if (soil == null || soil.getY() > centre.getY() || soil.getY() < centre.getY() - 1
                        || GensokyoUnderstoryFeature.isNearProtectedGround(level, soil))
                    return false;
                boolean supported = soil.getY() == centre.getY();
                if (config.kind() == GensokyoDeadwoodConfiguration.Kind.CUT_STUMP && !supported)
                    return false;
                feet.put(pos, supported);
                // Keep a one-block gap from live trees, existing woody decorations, and water.
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockPos neighbour = pos.relative(direction);
                    if (!writable(level, neighbour) || !level.getFluidState(neighbour).isEmpty())
                        return false;
                    BlockState state = level.getBlockState(neighbour);
                    if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || state.getBlock() instanceof SplinteredLogBlock)
                        return false;
                }
            }
            BlockState state;
            if (cell.splinter()) {
                state = config.splinters().get(palette).setValue(SplinteredLogBlock.FACING, forward)
                        .setValue(SplinteredLogBlock.VARIANT, cell.variant());
            } else {
                Direction.Axis axis = cell.axis() == 'Y' ? Direction.Axis.Y : cell.axis() == 'X' ? forward.getAxis() : sideways.getAxis();
                state = config.logs().get(palette).setValue(RotatedPillarBlock.AXIS, axis);
            }
            plan.put(pos, state);
        }
        // At most isolated one-block gaps under a rigid fallen log, never a floating trunk or a bridge.
        long supported = feet.values().stream().filter(Boolean::booleanValue).count();
        if (feet.isEmpty() || supported * 5 < feet.size() * 4L)
            return false;
        for (var entry : feet.entrySet())
            if (!entry.getValue()) {
                boolean adjacentSupport = false;
                for (Direction direction : Direction.Plane.HORIZONTAL)
                    adjacentSupport |= Boolean.TRUE.equals(feet.get(entry.getKey().relative(direction)));
                if (!adjacentSupport)
                    return false;
            }
        // All mandatory wood, splinters and their support have passed before the first mutation.
        for (var entry : plan.entrySet())
            level.setBlock(entry.getKey(), entry.getValue(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        decorate(level, centre, config, palette, plan, random);
        return true;
    }

    private static void decorate(WorldGenLevel level, BlockPos centre, GensokyoDeadwoodConfiguration config, int palette,
                                 Map<BlockPos, BlockState> wood, Random random) {
        // Sparse accents are optional, preserve existing plants, and obey native survival/light rules.
        for (var pos : wood.keySet()) {
            if (wood.containsKey(pos.above()) || random.nextFloat() > .28F || config.accents().isEmpty())
                continue;
            BlockState moss = Blocks.MOSS_CARPET.defaultBlockState();
            boolean damp = config.theme() == GensokyoForestType.TAIGA || config.theme() == GensokyoForestType.DARK_FOREST
                    || config.theme() == GensokyoForestType.MAGIC_FOREST || config.theme() == GensokyoForestType.JUNGLE;
            if (damp)
                accent(level, pos.above(), moss);
        }
        int attempts = config.theme() == GensokyoForestType.BIRCH_FOREST ? 3 : 7;
        for (int n = 0; n < attempts; n++) {
            int dx = random.nextInt(7) - 3, dz = random.nextInt(7) - 3;
            BlockPos pos = ground(level, centre.getX() + dx, centre.getZ() + dz, centre.getY());
            if (pos == null || Math.abs(pos.getY() - centre.getY()) > 1 || GensokyoUnderstoryFeature.isNearProtectedGround(level, pos))
                continue;
            BlockState state;
            if (!config.accents().isEmpty() && random.nextInt(3) == 0)
                state = config.accents().get(random.nextInt(config.accents().size()));
            else if (!config.litter().isEmpty()) {
                state = config.litter().get(Math.min(palette, config.litter().size() - 1));
                if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
                    state = state.setValue(BlockStateProperties.HORIZONTAL_FACING,
                            Direction.Plane.HORIZONTAL.getRandomDirection(RandomSource.create(random.nextLong())));
                if (state.hasProperty(BlockStateProperties.SEGMENT_AMOUNT))
                    state = state.setValue(BlockStateProperties.SEGMENT_AMOUNT, 1 + random.nextInt(3));
                if (state.hasProperty(BlockStateProperties.FLOWER_AMOUNT))
                    state = state.setValue(BlockStateProperties.FLOWER_AMOUNT, 1 + random.nextInt(3));
            } else
                continue;
            accent(level, pos, state);
        }
    }

    private static void accent(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (writable(level, pos) && level.getBlockState(pos).isAir() && level.getFluidState(pos).isEmpty()
                && state.canSurvive(level, pos))
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
    }

    private static boolean writable(WorldGenLevel level, BlockPos pos) {
        return !level.isOutsideBuildHeight(pos) && level.ensureCanWrite(pos) && !GensokyoRoadProtection.reserved(level, pos);
    }

    private static boolean clear(WorldGenLevel level, BlockPos pos) {
        if (!writable(level, pos) || !level.getFluidState(pos).isEmpty() || level.getBlockEntity(pos) != null)
            return false;
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN);
    }

    private static BlockPos ground(WorldGenLevel level, int x, int z, int expectedY) {
        for (int y = expectedY + 3; y >= expectedY - 6; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!writable(level, pos) || !writable(level, pos.below()))
                return null;
            BlockState below = level.getBlockState(pos.below());
            if (!level.getFluidState(pos.below()).isEmpty())
                return null;
            if (below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.DIRT) || below.is(Blocks.COARSE_DIRT)
                    || below.is(Blocks.ROOTED_DIRT) || below.is(Blocks.PODZOL) || below.is(Blocks.MYCELIUM)
                    || below.is(Blocks.MOSS_BLOCK))
                return pos;
        }
        return null;
    }
}
