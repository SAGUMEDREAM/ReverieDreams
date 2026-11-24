package cc.thonly.reverie_dreams.world.gen.feature;

import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.world.dream.DreamGridLayout;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;


public class DreamGridFeature extends Feature<DreamGridFeatureConfig> {
    public DreamGridFeature(Codec<DreamGridFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<DreamGridFeatureConfig> context) {
        WorldGenLevel world = context.level();
        BlockPos origin = context.origin();
        int chunkX = Math.floorDiv(origin.getX(), 16), chunkZ = Math.floorDiv(origin.getZ(), 16);
        // Full-cube collision is preserved. Blue blocks yield only to geometry at the same
        // X/Y/Z; red planes remain uninterrupted traversal routes.
        for (BlockPos target : DreamGridLayout.blocksInChunk(world.getSeed(), chunkX, chunkZ, world.getMinY(), world.getMaxY())) {
            Block colorBlock = DreamGridLayout.isRedLayer(target.getY())
                    ? RDBlocks.DREAM_RED_BLOCK.asBlock() : RDBlocks.DREAM_BLUE_BLOCK.asBlock();
            world.setBlock(target, colorBlock.defaultBlockState(), Block.UPDATE_KNOWN_SHAPE);
        }

        return true;
    }

}
