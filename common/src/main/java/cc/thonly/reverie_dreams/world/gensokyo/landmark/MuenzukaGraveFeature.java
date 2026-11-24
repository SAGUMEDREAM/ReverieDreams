package cc.thonly.reverie_dreams.world.gensokyo.landmark;

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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.LinkedHashMap;

/**
 * Small supported burial cairns. No excavation, block entities, loot or terrain flattening.
 */
public final class MuenzukaGraveFeature extends Feature<MuenzukaGraveConfiguration> {
    public static final String PATH = "gensokyo/landmarks/muenzuka/grave_mound";

    public MuenzukaGraveFeature(Codec<MuenzukaGraveConfiguration> codec) {
        super(codec);
    }

    /**
     * One jittered candidate per 48-block cell, belonging to exactly one chunk.
     */
    public static BlockPos candidate(long seed, int cellX, int cellZ) {
        var r = RandomSource.create(seed ^ cellX * 341873128712L ^ cellZ * 132897987541L ^ 0x47524156454cL);
        int x = cellX * 48 + 10 + r.nextInt(28), z = cellZ * 48 + 10 + r.nextInt(28);
        if (r.nextInt(5) == 0 || LandmarkDistribution.openLane(seed, x, z))
            return null;
        return new BlockPos(x, 0, z);
    }

    @Override
    public boolean place(FeaturePlaceContext<MuenzukaGraveConfiguration> c) {
        BlockPos root = c.origin();
        boolean natural = c.level() instanceof WorldGenRegion;
        if (natural) {
            root = candidate(c.level().getSeed(), Math.floorDiv(root.getX(), 48), Math.floorDiv(root.getZ(), 48));
            if (root == null || Math.floorDiv(root.getX(), 16) != Math.floorDiv(c.origin().getX(), 16)
                    || Math.floorDiv(root.getZ(), 16) != Math.floorDiv(c.origin().getZ(), 16))
                return false;
        }
        return placeMound(c.level(), root, c.config().kind(), c.random().nextLong(), natural);
    }

    public static boolean placeMound(WorldGenLevel level, BlockPos origin, MuenzukaGraveConfiguration.Kind kind, long seed, boolean natural) {
        var random = RandomSource.create(seed);
        if (kind == MuenzukaGraveConfiguration.Kind.RANDOM)
            kind = MuenzukaGraveConfiguration.Kind.values()[1 + random.nextInt(3)];
        BlockPos centre = ground(level, origin.getX(), origin.getZ());
        if (centre == null)
            return false;
        Direction forward = Direction.Plane.HORIZONTAL.getRandomDirection(random), side = forward.getClockWise();
        var plan = new LinkedHashMap<BlockPos, BlockState>();
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        boolean longMound = random.nextBoolean();
        // Compact asymmetric footprint; column supports follow a maximum one-block terrain step.
        for (int a = -2; a <= 2; a++)
            for (int b = -2; b <= 2; b++) {
                double distance = a * a / (longMound ? 5.5 : 4.2) + b * b / 3.2;
                if (distance > 1.0 || (distance > .65 && random.nextInt(4) == 0))
                    continue;
                BlockPos p = centre.relative(forward, a).relative(side, b);
                BlockPos floor = ground(level, p.getX(), p.getZ());
                if (floor == null || GensokyoUnderstoryFeature.isNearProtectedGround(level, floor))
                    return false;
                minY = Math.min(minY, floor.getY());
                maxY = Math.max(maxY, floor.getY());
                if (maxY - minY > 1 || Math.abs(floor.getY() - centre.getY()) > 1)
                    return false;
                if (natural && !level.getBiome(floor).is(GensokyoLandmarkBootstrap.biome(LandmarkVegetationConfiguration.Theme.MUENZUKA)))
                    return false;
                int height = distance < .29 ? (kind == MuenzukaGraveConfiguration.Kind.STONE ? 2 : 1) : 0;
                if (kind == MuenzukaGraveConfiguration.Kind.WEATHERED && distance < .29 && random.nextBoolean())
                    height = 0;
                int top = Math.max(centre.getY(), floor.getY()) + height;
                for (int y = floor.getY(); y <= top; y++) {
                    BlockPos block = new BlockPos(p.getX(), y, p.getZ());
                    Block material;
                    int roll = random.nextInt(10);
                    if (kind == MuenzukaGraveConfiguration.Kind.LOW)
                        material = roll < 4 ? Blocks.COARSE_DIRT : roll < 6 ? Blocks.MOSS_BLOCK : roll < 8 ? Blocks.MOSSY_COBBLESTONE : Blocks.ANDESITE;
                    else
                        material = roll < 4 ? Blocks.MOSSY_COBBLESTONE : roll < 7 ? Blocks.COBBLESTONE : roll < 9 ? Blocks.ANDESITE : Blocks.MOSS_BLOCK;
                    boolean cap = y == top && distance > .45 && random.nextBoolean();
                    plan.put(block, (cap ? (kind == MuenzukaGraveConfiguration.Kind.LOW ? Blocks.MOSSY_COBBLESTONE_SLAB : Blocks.COBBLESTONE_SLAB) : material).defaultBlockState());
                }
            }
        if (plan.isEmpty())
            return false;
        // Occasional remains of a wooden marker, attached to the mound rather than floating nearby.
        if (random.nextInt(3) == 0) {
            BlockPos marker = centre.relative(forward, -1);
            while (plan.containsKey(marker))
                marker = marker.above();
            plan.put(marker.below(), Blocks.MOSSY_COBBLESTONE.defaultBlockState());
            plan.put(marker, Blocks.OAK_FENCE.defaultBlockState());
            if (kind != MuenzukaGraveConfiguration.Kind.WEATHERED && random.nextBoolean())
                plan.put(marker.above(), Blocks.OAK_FENCE.defaultBlockState());
        }
        // Check a margin for roots, water and built ground, then every write, before changing anything.
        for (int x = -3; x <= 3; x++)
            for (int z = -3; z <= 3; z++) {
                BlockPos p = centre.offset(x, 0, z);
                if (!writable(level, p) || GensokyoUnderstoryFeature.isNearProtectedGround(level, p))
                    return false;
                for (int dy = -1; dy <= 3; dy++) {
                    BlockPos q = p.above(dy);
                    BlockState state = level.getBlockState(q);
                    if (!writable(level, q) || !level.getFluidState(q).isEmpty() || state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || state.hasBlockEntity())
                        return false;
                }
            }
        for (var e : plan.entrySet())
            if (!writable(level, e.getKey()) || !clear(level.getBlockState(e.getKey()))
                    || !level.getFluidState(e.getKey()).isEmpty() || level.getBlockEntity(e.getKey()) != null)
                return false;
        plan.forEach((p, state) -> level.setBlock(p, state, Block.UPDATE_CLIENTS));
        return true;
    }

    private static boolean writable(WorldGenLevel level, BlockPos p) {
        return !level.isOutsideBuildHeight(p) && level.ensureCanWrite(p);
    }

    private static boolean clear(BlockState s) {
        return s.isAir() || s.is(Blocks.SHORT_GRASS) || s.is(Blocks.FERN);
    }

    private static BlockPos ground(WorldGenLevel level, int x, int z) {
        if (!writable(level, new BlockPos(x, level.getMinY() + 1, z)))
            return null;
        int top = level.getHeight(level instanceof WorldGenRegion ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.OCEAN_FLOOR, x, z);
        for (int y = top; y >= Math.max(level.getMinY() + 1, top - 32); y--) {
            BlockPos p = new BlockPos(x, y, z);
            if (!writable(level, p) || !writable(level, p.below()) || !level.getFluidState(p.below()).isEmpty())
                return null;
            BlockState below = level.getBlockState(p.below());
            if (below.is(BlockTags.SUBSTRATE_OVERWORLD))
                return p;
            if (!clear(below))
                return null;
        }
        return null;
    }
}
