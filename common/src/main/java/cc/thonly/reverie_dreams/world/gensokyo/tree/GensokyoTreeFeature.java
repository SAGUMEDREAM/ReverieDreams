package cc.thonly.reverie_dreams.world.gensokyo.tree;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.TreeFeature;

import java.util.Random;

import cc.thonly.reverie_dreams.world.gensokyo.road.GensokyoRoadProtection;

/**
 * Checks the complete footprint before writing: rejected trees leave no partial trunks.
 */
public final class GensokyoTreeFeature extends Feature<GensokyoTreeConfiguration> {
    public GensokyoTreeFeature(Codec<GensokyoTreeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<GensokyoTreeConfiguration> context) {
        return placeTree(context.level(), context.origin(), context.config(), context.random().nextLong());
    }

    public static boolean placeTree(WorldGenLevel level, BlockPos origin, GensokyoTreeConfiguration config, long seed) {
        for (int dx = 0; dx < config.trunkWidth(); dx++) {
            for (int dz = 0; dz < config.trunkWidth(); dz++) {
                BlockPos soil = origin.offset(dx, -1, dz);
                // In 26.1, dirt excludes grass_block, podzol and mycelium.
                if (level.isOutsideBuildHeight(soil) || !level.ensureCanWrite(soil) || GensokyoRoadProtection.reserved(level, soil)
                        || !level.getBlockState(soil).is(BlockTags.SUBSTRATE_OVERWORLD))
                    return false;
            }
        }
        GensokyoTreeShape.Tree tree = GensokyoTreeShape.create(config.settings(), new Random(seed));
        for (GensokyoTreeShape.Voxel voxel : tree.voxels()) {
            BlockPos pos = position(origin, voxel.point());
            if (config.crownStyle() == GensokyoTreeShape.CrownStyle.JUNGLE_CANOPY && voxel.log() && voxel.point().y() == 0
                    && (!level.ensureCanWrite(pos.below())
                    || !level.getBlockState(pos.below()).is(BlockTags.SUBSTRATE_OVERWORLD)))
                return false;
            if (level.isOutsideBuildHeight(pos) || !level.ensureCanWrite(pos) || GensokyoRoadProtection.reserved(level, pos)
                    || !TreeFeature.validTreePos(level, pos) || !level.getFluidState(pos).isEmpty()
                    || level.getBlockEntity(pos) != null)
                return false;
        }
        // Distances are computed by the same planner used for the preview. Leaves are not persistent.
        for (GensokyoTreeShape.Voxel voxel : tree.voxels()) {
            BlockState state = voxel.log()
                    ? config.log().setValue(RotatedPillarBlock.AXIS, Direction.Axis.valueOf(voxel.axis().name()))
                    : config.leaves().setValue(LeavesBlock.DISTANCE, voxel.leafDistance()).setValue(LeavesBlock.PERSISTENT, false);
            level.setBlock(position(origin, voxel.point()), state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
        return true;
    }

    private static BlockPos position(BlockPos origin, GensokyoTreeShape.Point point) {
        return origin.offset(point.x(), point.y(), point.z());
    }
}
