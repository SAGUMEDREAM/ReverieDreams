package cc.thonly.reverie_dreams.world.gensokyo.bamboo;

import cc.thonly.reverie_dreams.block.nature.*;
import cc.thonly.reverie_dreams.registry.content.block.*;
import cc.thonly.reverie_dreams.world.gensokyo.understory.GensokyoUnderstoryFeature;
import com.mojang.serialization.Codec;
import net.minecraft.core.*;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.*;

import java.util.LinkedHashMap;

import cc.thonly.reverie_dreams.world.gensokyo.road.GensokyoRoadProtection;

/**
 * Connected clear routes and supported foliage; generated columns never replace authored soil.
 */
public final class BambooForestFeature extends Feature<BambooForestConfiguration> {
    public BambooForestFeature(Codec<BambooForestConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<BambooForestConfiguration> c) {
        boolean natural = c.level() instanceof WorldGenRegion;
        int sx = natural ? Math.floorDiv(c.origin().getX(), 16) * 16 : c.origin().getX() - 8;
        int sz = natural ? Math.floorDiv(c.origin().getZ(), 16) * 16 : c.origin().getZ() - 8;
        return c.config().mode() == BambooForestConfiguration.Mode.CULMS ? culms(c.level(), sx, sz, natural) : ground(c.level(), sx, sz, natural);
    }

    public static boolean culms(WorldGenLevel level, int sx, int sz, boolean natural) {
        int placed = 0;
        long seed = level.getSeed();
        int[][] offsets = {{-2, -1}, {1, 1}, {2, -2}, {-1, 3}, {3, 2}};
        for (int cx = Math.floorDiv(sx - 4, 8); cx <= Math.floorDiv(sx + 19, 8); cx++)
            for (int cz = Math.floorDiv(sz - 4, 8); cz <= Math.floorDiv(sz + 19, 8); cz++) {
                int bx = cx * 8 + 4 + (int) (BambooForestDistribution.hash(seed ^ 0x43454e58, cx, cz) * 3) - 1;
                int bz = cz * 8 + 4 + (int) (BambooForestDistribution.hash(seed ^ 0x43454e5a, cx, cz) * 3) - 1;
                int rotation = (int) (BambooForestDistribution.hash(seed ^ 0x524f5441, cx, cz) * 4);
                for (int i = 0; i < offsets.length; i++) {
                    int dx = offsets[i][0], dz = offsets[i][1];
                    for (int r = 0; r < rotation; r++) {
                        int t = dx;
                        dx = -dz;
                        dz = t;
                    }
                    int x = bx + dx, z = bz + dz;
                    if (x < sx || x >= sx + 16 || z < sz || z >= sz + 16)
                        continue;
                    if (BambooForestDistribution.open(seed, x, z) || BambooForestDistribution.hash(seed ^ 0x5354454d, x, z) > BambooForestDistribution.density(seed, x, z))
                        continue;
                    BlockPos root = surface(level, x, z);
                    if (root == null)
                        continue;
                    if (natural && !level.getBiome(root).is(BambooForestBootstrap.BIOME))
                        continue;
                    boolean edge = false;
                    if (natural)
                        for (Direction d : Direction.Plane.HORIZONTAL)
                            if (!level.getBiome(root.relative(d, 12)).is(BambooForestBootstrap.BIOME))
                                edge = true;
                    if (edge && i > 1)
                        continue;
                    int height = BambooForestDistribution.height(seed, x, z);
                    if (edge)
                        height = Math.min(height, 18);
                    if (placeCulm(level, root, height, seed ^ x * 341873128712L ^ z * 132897987541L, natural))
                        placed++;
                }
            }
        return placed > 0;
    }

    public static boolean placeCulm(WorldGenLevel level, BlockPos root, int height, long seed, boolean natural) {
        if (height < 4 || height > BambooForestDistribution.MAX_NATURAL_HEIGHT || !writable(level, root) || !LargeBambooBlock.soil(level.getBlockState(root.below()))
                || !level.getFluidState(root.below()).isEmpty() || GensokyoUnderstoryFeature.isNearProtectedGround(level, root))
            return false;
        if (natural && !level.getBiome(root).is(BambooForestBootstrap.BIOME))
            return false;
        // Two adjacent culms cannot squeeze the player into a one-block slit.
        for (Direction d : Direction.Plane.HORIZONTAL) {
            BlockPos p = root.relative(d);
            if (!writable(level, p) || !level.getFluidState(p).isEmpty() || level.getBlockState(p).is(RDBambooBlocks.LARGE_BAMBOO.asBlock()))
                return false;
            BlockPos floor = surface(level, p.getX(), p.getZ());
            if (floor == null || Math.abs(floor.getY() - root.getY()) > 1)
                return false;
        }
        var plan = new LinkedHashMap<BlockPos, BlockState>();
        for (int y = 0; y < height; y++) {
            BlockPos p = root.above(y);
            if (!writable(level, p) || !clear(level, p) || (natural && !level.getBiome(p).is(BambooForestBootstrap.BIOME)))
                return false;
            BambooLeaves leaves = y == height - 1 ? BambooLeaves.LARGE : y >= height - 5 ? BambooLeaves.SMALL : BambooLeaves.NONE;
            plan.put(p, RDBambooBlocks.LARGE_BAMBOO.defaultBlockState().setValue(LargeBambooBlock.GROWING, false).setValue(LargeBambooBlock.LEAVES, leaves));
        }
        // Foliage is in adjacent cells, with an explicit supporting culm and no ground-level obstruction.
        for (int y = Math.max(5, height / 2); y < height - 1; y += 3) {
            int turn = (int) (BambooForestDistribution.hash(seed, y, height) * 4);
            Direction dir = Direction.from2DDataValue(turn);
            BlockPos leaf = root.above(y).relative(dir);
            if (writable(level, leaf) && clear(level, leaf) && (!natural || level.getBiome(leaf).is(BambooForestBootstrap.BIOME)))
                plan.put(leaf, RDBambooForestBlocks.LEAF_CLUSTER.defaultBlockState().setValue(BambooLeafClusterBlock.FACING, dir));
        }
        plan.forEach((p, state) -> level.setBlock(p, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE));
        return true;
    }

    public static boolean ground(WorldGenLevel level, int sx, int sz, boolean natural) {
        int placed = 0;
        long seed = level.getSeed();
        for (int dz = 0; dz < 16; dz++)
            for (int dx = 0; dx < 16; dx++) {
                int x = sx + dx, z = sz + dz;
                boolean open = BambooForestDistribution.open(seed, x, z);
                double roll = BambooForestDistribution.hash(seed ^ 0x47524f554e44L, x, z), density = BambooForestDistribution.density(seed, x, z);
                if (open && roll > .025)
                    continue;
                if (!open && roll > .25 + .40 * density)
                    continue;
                BlockPos p = surface(level, x, z);
                if (p == null || !clear(level, p) || GensokyoUnderstoryFeature.isNearProtectedGround(level, p)
                        || (natural && !level.getBiome(p).is(BambooForestBootstrap.BIOME)))
                    continue;
                BlockState state;
                Direction facing = Direction.from2DDataValue((int) (BambooForestDistribution.hash(seed ^ 0x46414345, x, z) * 4));
                if (!open && roll < .0006) {
                    var forms = BambooDebrisBlock.Form.values();
                    state = RDBambooForestBlocks.DEBRIS.defaultBlockState().setValue(BambooDebrisBlock.FORM, forms[(int) (BambooForestDistribution.hash(seed ^ 0x44454252, x, z) * forms.length)])
                            .setValue(BambooDebrisBlock.FACING, facing);
                } else if (!open && roll < .018)
                    state = RDBambooBlocks.LARGE_BAMBOO_SHOOT.defaultBlockState().setValue(LargeBambooShootBlock.AGE, (int) (BambooForestDistribution.hash(seed ^ 0x53484f4f, x, z) * 3));
                else if (!open && roll < .075)
                    state = RDBambooForestBlocks.LEAF_BUSH.defaultBlockState();
                else
                    state = RDBambooForestBlocks.LEAF_LITTER.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                            .setValue(BlockStateProperties.SEGMENT_AMOUNT, 1 + (int) (BambooForestDistribution.hash(seed ^ 0x4c495454, x, z) * 3));
                if (state.canSurvive(level, p)) {
                    level.setBlock(p, state, Block.UPDATE_CLIENTS);
                    placed++;
                }
            }
        return placed > 0;
    }

    public static BlockPos surface(WorldGenLevel level, int x, int z) {
        if (!writable(level, new BlockPos(x, level.getMinY() + 1, z)))
            return null;
        int top = level.getHeight(level instanceof WorldGenRegion ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.OCEAN_FLOOR, x, z);
        for (int y = top; y >= Math.max(level.getMinY() + 1, top - BambooForestDistribution.MAX_NATURAL_HEIGHT - 1); y--) {
            BlockPos p = new BlockPos(x, y, z);
            if (!writable(level, p) || !writable(level, p.below()) || !level.getFluidState(p).isEmpty() || !level.getFluidState(p.below()).isEmpty())
                return null;
            BlockState below = level.getBlockState(p.below());
            if (LargeBambooBlock.soil(below))
                return p;
            if (!below.isAir() && !below.is(BlockTags.LOGS) && !below.is(BlockTags.LEAVES) && !below.is(RDBambooBlocks.LARGE_BAMBOO.asBlock())
                    && !below.is(RDBambooForestBlocks.LEAF_CLUSTER.asBlock()) && !below.canBeReplaced())
                return null;
        }
        return null;
    }

    private static boolean writable(WorldGenLevel level, BlockPos p) {
        return !level.isOutsideBuildHeight(p) && level.ensureCanWrite(p) && !GensokyoRoadProtection.reserved(level, p);
    }

    private static boolean clear(WorldGenLevel level, BlockPos p) {
        BlockState s = level.getBlockState(p);
        return level.getFluidState(p).isEmpty() && level.getBlockEntity(p) == null && (s.isAir() || s.is(Blocks.SHORT_GRASS) || s.is(Blocks.FERN));
    }
}
