package cc.thonly.reverie_dreams.world.gensokyo.rock;

import cc.thonly.reverie_dreams.world.gensokyo.understory.GensokyoUnderstoryDistribution;
import cc.thonly.reverie_dreams.world.gensokyo.understory.GensokyoUnderstoryFeature;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.LinkedHashMap;

import cc.thonly.reverie_dreams.world.gensokyo.road.GensokyoRoadProtection;

/**
 * Adds supported rock volume without excavating, flattening, or replacing more than one soil layer.
 */
public final class GensokyoRockFeature extends Feature<GensokyoRockConfiguration> {
    public GensokyoRockFeature(Codec<GensokyoRockConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<GensokyoRockConfiguration> context) {
        return placeRock(context.level(), context.origin(), context.config(), context.random().nextLong(), context.level() instanceof WorldGenRegion);
    }

    public static boolean allows(GensokyoRockConfiguration.Kind kind, long seed, int x, int z) {
        double threshold = switch (kind) {
            case LOW_OUTCROP -> .38;
            case ROCK_CLUSTER -> .46;
            case LANDMARK_BOULDER -> .54;
        };
        return GensokyoUnderstoryDistribution.habitat(seed, x, z) >= threshold;
    }

    public static boolean placeRock(WorldGenLevel level, BlockPos origin, GensokyoRockConfiguration config, long seed, boolean natural) {
        if (natural && !allows(config.kind(), level.getSeed(), origin.getX(), origin.getZ()))
            return false;
        BlockPos centre = GensokyoUnderstoryFeature.findSoilSurface(level, origin.getX(), origin.getZ());
        if (centre == null)
            return false;
        var columns = GensokyoRockShape.create(config.theme(), config.kind(), seed);
        int extentX = columns.stream().mapToInt(c -> Math.abs(c.point().x())).max().orElse(0);
        int extentZ = columns.stream().mapToInt(c -> Math.abs(c.point().z())).max().orElse(0);
        // Low vegetation, rocks, water and built surfaces keep a margin around the whole footprint.
        for (int x = -extentX - 2; x <= extentX + 2; x++)
            for (int z = -extentZ - 2; z <= extentZ + 2; z++) {
                for (int y = 0; y <= 2; y++) {
                    BlockPos pos = centre.offset(x, y, z);
                    if (!writable(level, pos) || !level.getFluidState(pos).isEmpty())
                        return false;
                    BlockState current = level.getBlockState(pos);
                    if (current.is(BlockTags.LOGS) || current.is(BlockTags.LEAVES) || rock(current) || current.hasBlockEntity())
                        return false;
                }
            }
        var tops = new LinkedHashMap<BlockPos, Integer>();
        var foundations = new LinkedHashMap<BlockPos, Integer>();
        int minSurface = Integer.MAX_VALUE, maxSurface = Integer.MIN_VALUE;
        for (var column : columns) {
            var p = column.point();
            BlockPos soil = GensokyoUnderstoryFeature.findSoilSurface(level, centre.getX() + p.x(), centre.getZ() + p.z());
            if (soil == null || GensokyoUnderstoryFeature.isNearProtectedGround(level, soil))
                return false;
            int surface = soil.getY() - 1;
            minSurface = Math.min(minSurface, surface);
            maxSurface = Math.max(maxSurface, surface);
            if (Math.abs(soil.getY() - centre.getY()) > 1 || maxSurface - minSurface > 1)
                return false;
            BlockPos support = soil.below(2);
            if (!writable(level, support) || !support(level.getBlockState(support)) || !level.getFluidState(support).isEmpty())
                return false;
            var key = new BlockPos(soil.getX(), 0, soil.getZ());
            foundations.put(key, surface);
            tops.put(key, centre.getY() + column.top());
        }
        var plan = new LinkedHashMap<BlockPos, BlockState>();
        for (var entry : tops.entrySet()) {
            BlockPos key = entry.getKey();
            int base = foundations.get(key);
            for (int y = base; y <= entry.getValue(); y++) {
                BlockPos pos = new BlockPos(key.getX(), y, key.getZ());
                if (!writable(level, pos) || !level.getFluidState(pos).isEmpty() || level.getBlockEntity(pos) != null)
                    return false;
                BlockState current = level.getBlockState(pos);
                if (y == base ? !current.is(BlockTags.SUBSTRATE_OVERWORLD) : !replaceable(current))
                    return false;
                boolean band = Math.floorMod(Math.floorDiv(pos.getX() + pos.getZ() * 2 + (int) seed, 3), 5) == 0;
                plan.put(pos, band ? config.variation() : config.stone());
            }
        }
        // Coherent two-block patches of moss, confined to the exposed lower part of each rock.
        for (var entry : plan.entrySet()) {
            BlockPos pos = entry.getKey();
            boolean exposed = !plan.containsKey(pos.above());
            for (Direction direction : Direction.Plane.HORIZONTAL)
                exposed |= !plan.containsKey(pos.relative(direction));
            long hash = seed ^ Math.floorDiv(pos.getX(), 2) * 341873128712L ^ Math.floorDiv(pos.getZ(), 2) * 132897987541L;
            hash = (hash ^ (hash >>> 30)) * 0xbf58476d1ce4e5b9L;
            double patch = ((hash ^ (hash >>> 27)) >>> 11) * 0x1.0p-53;
            if (exposed && pos.getY() >= centre.getY() && pos.getY() <= centre.getY() + 1 && patch < config.mossChance())
                entry.setValue(config.moss());
        }
        for (var entry : plan.entrySet())
            level.setBlock(entry.getKey(), entry.getValue(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        // One sparse matching plant/litter patch outside the footprint, sharing existing ecological rules.
        GensokyoUnderstoryFeature.decorateRockEdge(level, centre.offset(extentX + 2, 0, 0), config.theme(), seed ^ 0x524f434bL);
        return !plan.isEmpty();
    }

    private static boolean writable(WorldGenLevel level, BlockPos pos) {
        return !level.isOutsideBuildHeight(pos) && level.ensureCanWrite(pos) && !GensokyoRoadProtection.reserved(level, pos);
    }

    private static boolean replaceable(BlockState state) {
        return state.isAir() || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN);
    }

    private static boolean rock(BlockState state) {
        return state.is(Blocks.STONE) || state.is(Blocks.ANDESITE) || state.is(Blocks.MOSSY_COBBLESTONE);
    }

    private static boolean support(BlockState state) {
        return state.is(BlockTags.SUBSTRATE_OVERWORLD) || rock(state) || state.is(Blocks.GRANITE) || state.is(Blocks.DIORITE);
    }
}
