package cc.thonly.reverie_dreams.world.gensokyo.understory;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Random;

import cc.thonly.reverie_dreams.world.gensokyo.road.GensokyoRoadProtection;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestType;

import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Terrain-aware decoration with bounded footprints and no partial rejected woody clumps.
 */
public final class GensokyoUnderstoryFeature extends Feature<GensokyoUnderstoryConfiguration> {
    public GensokyoUnderstoryFeature(Codec<GensokyoUnderstoryConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<GensokyoUnderstoryConfiguration> context) {
        // /place feature previews one specimen; natural chunk decoration also applies the habitat rules.
        return context.level() instanceof WorldGenRegion
                ? placeNaturalPatch(context.level(), context.origin(), context.config(), context.random().nextLong())
                : placePatch(context.level(), context.origin(), context.config(), context.random().nextLong());
    }

    public static boolean placePatch(WorldGenLevel level, BlockPos origin, GensokyoUnderstoryConfiguration config, long seed) {
        return placePatch(level, origin, config, seed, false);
    }

    public static boolean placeNaturalPatch(WorldGenLevel level, BlockPos origin, GensokyoUnderstoryConfiguration config, long seed) {
        if (!GensokyoUnderstoryDistribution.allows(config.theme(), config.kind(), level.getSeed(), origin.getX(), origin.getZ()))
            return false;
        return placePatch(level, origin, config, seed, true);
    }

    private static boolean placePatch(WorldGenLevel level, BlockPos origin, GensokyoUnderstoryConfiguration config, long seed, boolean natural) {
        Random random = new Random(seed);
        BlockPos centre = ground(level, origin.getX(), origin.getZ());
        if (centre == null || nearPath(level, centre))
            return false;
        if (config.kind() == GensokyoUnderstoryConfiguration.Kind.FLOOR_PATCH)
            return floor(level, centre, config, random, natural) > 0;
        if (natural && !growingSpace(level, centre, config.kind()))
            return false;
        var planned = new LinkedHashMap<BlockPos, BlockState>();
        var logs = new HashSet<BlockPos>();
        for (var clump : GensokyoUnderstoryShape.create(config, seed)) {
            BlockPos root = ground(level, origin.getX() + clump.x(), origin.getZ() + clump.z());
            if (root == null || Math.abs(root.getY() - centre.getY()) > 2 || nearPath(level, root))
                return false;
            for (var cell : clump.cells()) {
                var p = cell.point();
                BlockPos pos = root.offset(p.x(), p.y(), p.z());
                if (!writable(level, pos))
                    return false;
                BlockState current = level.getBlockState(pos);
                // Crop foliage into uphill soil; never carve terrain or cut branches.
                if (!cell.log() && soil(current))
                    continue;
                if (!replaceable(current) || !level.getFluidState(pos).isEmpty() || level.getBlockEntity(pos) != null)
                    return false;
                if (cell.log()) {
                    logs.add(pos);
                    planned.put(pos, config.logs().get(clump.palette()).setValue(RotatedPillarBlock.AXIS, Direction.Axis.valueOf(cell.axis())));
                } else if (!logs.contains(pos)) {
                    planned.putIfAbsent(pos, config.leaves().get(clump.palette()));
                }
            }
        }
        // Calculate distances after slope cropping and clump merging, using real face adjacency.
        var distances = new HashMap<BlockPos, Integer>();
        var queue = new ArrayDeque<BlockPos>();
        for (var pos : logs) {
            distances.put(pos, 0);
            queue.add(pos);
        }
        while (!queue.isEmpty()) {
            BlockPos pos = queue.removeFirst();
            int next = distances.get(pos) + 1;
            if (next > 6)
                continue;
            for (Direction direction : Direction.values()) {
                BlockPos neighbour = pos.relative(direction);
                if (planned.containsKey(neighbour) && !distances.containsKey(neighbour)) {
                    distances.put(neighbour, next);
                    queue.add(neighbour);
                }
            }
        }
        planned.entrySet().removeIf(entry -> !logs.contains(entry.getKey()) && !distances.containsKey(entry.getKey()));
        for (var entry : planned.entrySet()) {
            BlockState state = logs.contains(entry.getKey()) ? entry.getValue() : entry.getValue()
                    .setValue(LeavesBlock.DISTANCE, distances.get(entry.getKey())).setValue(LeavesBlock.PERSISTENT, false);
            level.setBlock(entry.getKey(), state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
        // A few matching plants at the edge integrate the woody clump into the forest floor.
        floor(level, centre, config, random, natural);
        return !planned.isEmpty();
    }

    private static int floor(WorldGenLevel level, BlockPos centre, GensokyoUnderstoryConfiguration config, Random random, boolean natural) {
        int placed = 0;
        boolean standalone = config.kind() == GensokyoUnderstoryConfiguration.Kind.FLOOR_PATCH;
        int attempts = standalone ? 26 : 10;
        int radius = standalone ? 4 : 3;
        int limit = natural ? (standalone ? 8 : 3) : attempts;
        boolean stonePlaced = false;
        BlockState flower = config.flowers().get(random.nextInt(config.flowers().size()));
        BlockState litter = config.litter().get(random.nextInt(config.litter().size()));
        for (int i = 0; i < attempts && placed < limit; i++) {
            // Triangular offsets create loose clusters with an irregular edge and open gaps.
            int dx = random.nextInt(radius + 1) - random.nextInt(radius + 1);
            int dz = random.nextInt(radius + 1) - random.nextInt(radius + 1);
            BlockPos pos = ground(level, centre.getX() + dx, centre.getZ() + dz);
            if (pos == null || Math.abs(pos.getY() - centre.getY()) > 2 || nearPath(level, pos)
                    || !replaceable(level.getBlockState(pos)) || !level.getFluidState(pos).isEmpty()
                    || level.getBlockEntity(pos) != null)
                continue;
            int choice = random.nextInt(10);
            BlockState state = choice < 3 ? flower : choice < 7 ? litter : config.accents().get(random.nextInt(config.accents().size()));
            if (natural) {
                BlockState canopy = canopyAbove(level, pos);
                boolean covered = canopy != null;
                // Leaves collect below crowns. Open ground favours flowers and grasses instead.
                state = covered && choice < 7 ? litterUnder(config, canopy)
                        : !covered && choice < 3 ? flower : config.accents().get(random.nextInt(config.accents().size()));
                if (state.getBlock() instanceof MushroomBlock && !covered
                        && !level.getBlockState(pos.below()).is(BlockTags.OVERRIDES_MUSHROOM_LIGHT_REQUIREMENT))
                    continue;
                if (state.is(Blocks.MOSS_CARPET) && !covered)
                    continue;
                if (state.is(Blocks.MOSSY_COBBLESTONE)) {
                    if (stonePlaced || random.nextInt(4) != 0)
                        continue;
                    stonePlaced = true;
                }
            }
            if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
                state = state.setValue(BlockStateProperties.HORIZONTAL_FACING,
                        Direction.from2DDataValue(random.nextInt(4)));
            // Leaf litter and petals both expose a 1..4 segment count, with different property names.
            for (var property : state.getProperties())
                if (property instanceof IntegerProperty integer
                        && (property.getName().equals("segment_amount") || property.getName().equals("flower_amount"))) {
                    state = state.setValue(integer, natural ? 1 + random.nextInt(Math.abs(dx) + Math.abs(dz) <= 2 ? 4 : 2) : 1 + random.nextInt(4));
                }
            if (!state.canSurvive(level, pos))
                continue;
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
            placed++;
        }
        return placed;
    }

    private static BlockState litterUnder(GensokyoUnderstoryConfiguration config, BlockState canopy) {
        for (int i = 0; i < config.leaves().size(); i++) {
            if (!config.leaves().get(i).is(canopy.getBlock()))
                continue;
            if (config.theme() == GensokyoForestType.MAPLE_FOREST)
                return i < config.litter().size() ? config.litter().get(i) : Blocks.LEAF_LITTER.defaultBlockState();
            if (config.theme() == GensokyoForestType.CHERRY_GROVE)
                return canopy.is(Blocks.CHERRY_LEAVES) ? config.litter().getFirst() : Blocks.LEAF_LITTER.defaultBlockState();
            return config.litter().getFirst();
        }
        return Blocks.LEAF_LITTER.defaultBlockState();
    }

    private static BlockState canopyAbove(WorldGenLevel level, BlockPos pos) {
        for (int y = 1; y <= 40; y++) {
            BlockPos probe = pos.above(y);
            if (!writable(level, probe))
                break;
            BlockState state = level.getBlockState(probe);
            if (state.is(BlockTags.LEAVES))
                return state;
            if (!state.isAir() && !state.is(BlockTags.LOGS))
                break;
        }
        return null;
    }

    private static boolean growingSpace(WorldGenLevel level, BlockPos centre, GensokyoUnderstoryConfiguration.Kind kind) {
        int radius = switch (kind) {
            case LEAF_PATCH -> 2;
            case SHRUB -> 3;
            case THICKET -> 4;
            default -> 0;
        };
        // A gap around existing low woody vegetation stops neighbouring patches joining into walls.
        for (int x = -radius; x <= radius; x++)
            for (int z = -radius; z <= radius; z++) {
                for (int y = 0; y <= 2; y++) {
                    BlockPos pos = centre.offset(x, y, z);
                    if (!writable(level, pos))
                        return false;
                    BlockState state = level.getBlockState(pos);
                    if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || !level.getFluidState(pos).isEmpty())
                        return false;
                }
            }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighbour = ground(level, centre.getX() + direction.getStepX() * 2, centre.getZ() + direction.getStepZ() * 2);
            if (neighbour == null || Math.abs(neighbour.getY() - centre.getY()) > 1)
                return false;
        }
        return true;
    }

    private static boolean writable(WorldGenLevel level, BlockPos pos) {
        return !level.isOutsideBuildHeight(pos) && level.ensureCanWrite(pos) && !GensokyoRoadProtection.reserved(level, pos);
    }

    public static BlockPos findSoilSurface(WorldGenLevel level, int x, int z) {
        return ground(level, x, z);
    }

    public static boolean isNearProtectedGround(WorldGenLevel level, BlockPos root) {
        return nearPath(level, root);
    }

    public static int decorateRockEdge(WorldGenLevel level, BlockPos centre, GensokyoForestType theme, long seed) {
        var config = GensokyoUnderstoryBootstrap.configuration(new GensokyoUnderstoryBootstrap.Profile(theme, GensokyoUnderstoryConfiguration.Kind.SHRUB));
        return floor(level, centre, config, new Random(seed), true);
    }

    private static boolean replaceable(BlockState state) {
        return state.isAir() || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN);
    }

    private static boolean soil(BlockState state) {
        return state.is(BlockTags.SUBSTRATE_OVERWORLD) && !state.is(Blocks.FARMLAND) && !state.is(Blocks.DIRT_PATH);
    }

    private static BlockPos ground(WorldGenLevel level, int x, int z) {
        if (!level.ensureCanWrite(new BlockPos(x, level.getMinY() + 1, z)))
            return null;
        // Walk through canopies, never use their leaves as ground. Bounded by the largest forest tree.
        int top = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
        for (int y = top; y >= Math.max(level.getMinY() + 1, top - 48); y--) {
            var pos = new BlockPos(x, y, z);
            if (!writable(level, pos) || !writable(level, pos.below()))
                return null;
            BlockState below = level.getBlockState(pos.below());
            if (soil(below))
                return pos;
            if (!below.isAir() && !below.is(BlockTags.LEAVES) && !below.is(BlockTags.LOGS)
                    && !replaceable(below))
                return null;
        }
        return null;
    }

    private static boolean nearPath(WorldGenLevel level, BlockPos root) {
        for (int x = -2; x <= 2; x++)
            for (int z = -2; z <= 2; z++)
                for (int y = -1; y <= 0; y++) {
                    BlockPos pos = root.offset(x, y, z);
                    if (!writable(level, pos))
                        return true;
                    BlockState state = level.getBlockState(pos);
                    if (state.is(Blocks.DIRT_PATH) || state.is(Blocks.FARMLAND) || state.is(BlockTags.PLANKS)
                            || state.is(Blocks.STONE_BRICKS) || state.is(Blocks.COBBLESTONE) || state.hasBlockEntity())
                        return true;
                }
        return false;
    }
}
