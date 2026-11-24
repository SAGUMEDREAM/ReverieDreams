package cc.thonly.reverie_dreams.world.gen.feature;

import cc.thonly.reverie_dreams.block.nature.LargeBambooBlock;
import cc.thonly.reverie_dreams.block.nature.LargeBambooShootBlock;
import cc.thonly.reverie_dreams.registry.content.block.RDBambooBlocks;
import cc.thonly.reverie_dreams.world.LargeBambooGeneration;
import cc.thonly.reverie_dreams.world.gensokyo.understory.GensokyoUnderstoryFeature;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;

import net.minecraft.tags.BlockTags;

/**
 * Adds small, spaced clumps to newly generated Overworld chunks. Never retrofits existing farms.
 */
public final class LargeBambooShootFeature extends Feature<NoneFeatureConfiguration> {
    public LargeBambooShootFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var level = context.level();
        boolean natural = level instanceof WorldGenRegion;
        if (natural && !level.getLevel().dimension().equals(Level.OVERWORLD))
            return false;
        var targets = new ArrayList<BlockPos>();
        int attempts = 4 + context.random().nextInt(4);
        BlockPos origin = context.origin();
        for (int i = 0; i < attempts; i++) {
            int x = origin.getX() + context.random().nextInt(9) - 4, z = origin.getZ() + context.random().nextInt(9) - 4;
            BlockPos pos = surface(level, x, z);
            if (pos == null)
                continue;
            var state = level.getBlockState(pos);
            if (!state.isAir() && !state.is(Blocks.SHORT_GRASS) && !state.is(Blocks.FERN))
                continue;
            if (Math.abs(pos.getY() - origin.getY()) > 48 || targets.stream().anyMatch(p -> p.distSqr(pos) < 4))
                continue;
            if (natural && !level.getBiome(pos).is(LargeBambooGeneration.HAS_SHOOTS))
                continue;
            if (GensokyoUnderstoryFeature.isNearProtectedGround(level, pos))
                continue;
            boolean gentle = true;
            for (int dx : new int[]{-1, 1})
                for (int dz : new int[]{-1, 1}) {
                    BlockPos p = surface(level, x + dx, z + dz);
                    if (p == null || Math.abs(p.getY() - pos.getY()) > 1)
                        gentle = false;
                }
            if (gentle)
                targets.add(pos);
        }
        for (BlockPos pos : targets)
            level.setBlock(pos, RDBambooBlocks.LARGE_BAMBOO_SHOOT.defaultBlockState()
                    .setValue(LargeBambooShootBlock.AGE, context.random().nextInt(3)), Block.UPDATE_CLIENTS);
        return !targets.isEmpty();
    }

    private static boolean writable(WorldGenLevel level, BlockPos pos) {
        return !level.isOutsideBuildHeight(pos) && level.ensureCanWrite(pos);
    }

    private static BlockPos surface(WorldGenLevel level, int x, int z) {
        if (!writable(level, new BlockPos(x, level.getMinY() + 1, z)))
            return null;
        int top = level.getHeight(level instanceof WorldGenRegion ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.OCEAN_FLOOR, x, z);
        for (int y = top; y >= Math.max(level.getMinY() + 1, top - 48); y--) {
            BlockPos p = new BlockPos(x, y, z);
            if (!writable(level, p) || !writable(level, p.below()))
                return null;
            var below = level.getBlockState(p.below());
            if (!level.getFluidState(p).isEmpty() || !level.getFluidState(p.below()).isEmpty())
                return null;
            if (LargeBambooBlock.soil(below))
                return p;
            // Occupied ground still counts when measuring the slope around a shoot.
            if (!below.isAir() && !below.is(BlockTags.LEAVES) && !below.is(BlockTags.LOGS)
                    && !below.is(Blocks.SHORT_GRASS) && !below.is(Blocks.FERN)
                    && !below.is(Blocks.BAMBOO) && !below.is(Blocks.BAMBOO_SAPLING)
                    && !below.is(Blocks.TALL_GRASS) && !below.is(Blocks.LARGE_FERN))
                return null;
        }
        return null;
    }
}
