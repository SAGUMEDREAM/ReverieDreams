package cc.thonly.reverie_dreams.world.gensokyo.understory;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.AbstractHugeMushroomFeature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

import java.util.LinkedHashMap;

import cc.thonly.reverie_dreams.world.gensokyo.road.GensokyoRoadProtection;

/**
 * Broad blue parasols and taller purple bells; validate the complete shape before any write.
 */
public final class MagicHugeMushroomFeature extends AbstractHugeMushroomFeature {
    public MagicHugeMushroomFeature(Codec<HugeMushroomFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<HugeMushroomFeatureConfiguration> context) {
        var level = context.level();
        var origin = context.origin();
        var config = context.config();
        var random = context.random();
        boolean blue = config.foliageRadius() >= 3;
        int height = (blue ? 5 : 6) + random.nextInt(3);
        var plan = new LinkedHashMap<BlockPos, BlockState>();
        BlockState cap = config.capProvider().getState(level, random, origin);
        BlockState stem = config.stemProvider().getState(level, random, origin);
        if (!level.ensureCanWrite(origin.below()) || !config.canPlaceOn().test(level, origin.below()))
            return false;
        BlockState soil = level.getBlockState(origin.below());
        if (soil.is(Blocks.DIRT_PATH) || soil.is(Blocks.FARMLAND))
            return false;
        for (int y = blue ? height - 1 : height - 3; y <= height; y++) {
            int radius = y == height ? config.foliageRadius() - 1 : config.foliageRadius();
            for (int x = -radius; x <= radius; x++)
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) == radius && Math.abs(z) == radius)
                        continue;
                    // Keep a hollow bell below its crown, leaving room around the central stalk.
                    if (!blue && y < height - 1 && Math.abs(x) < radius && Math.abs(z) < radius)
                        continue;
                    plan.put(origin.offset(x, y, z), cap);
                }
        }
        for (int y = 0; y < height - (blue ? 1 : 0); y++)
            plan.putIfAbsent(origin.above(y), stem);
        for (var pos : plan.keySet()) {
            if (level.isOutsideBuildHeight(pos) || !level.ensureCanWrite(pos) || GensokyoRoadProtection.reserved(level, pos) || !level.getFluidState(pos).isEmpty()
                    || level.getBlockEntity(pos) != null)
                return false;
            BlockState current = level.getBlockState(pos);
            if (!current.isAir() && !current.is(Blocks.SHORT_GRASS) && !current.is(Blocks.FERN)
                    && !current.is(BlockTags.LEAVES))
                return false;
        }
        for (var entry : plan.entrySet()) {
            BlockState state = entry.getValue();
            for (Direction face : Direction.values()) {
                var property = PipeBlock.PROPERTY_BY_DIRECTION.get(face);
                if (state.hasProperty(property))
                    state = state.setValue(property,
                            !plan.containsKey(entry.getKey().relative(face)));
            }
            level.setBlock(entry.getKey(), state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
        return true;
    }

    // The native mushroom block recognises AbstractHugeMushroomFeature for bone meal eligibility.
    @Override
    protected int getTreeRadiusForHeight(int trunkHeight, int treeHeight, int leafRadius, int y) {
        return leafRadius;
    }

    @Override
    protected void makeCap(WorldGenLevel level, RandomSource random, BlockPos origin, int height,
                           BlockPos.MutableBlockPos pos, HugeMushroomFeatureConfiguration config) {
        throw new UnsupportedOperationException("Atomic place owns the whole plan");
    }
}
