package cc.thonly.reverie_dreams.world.gen.feature;

import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
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
        DreamGridFeatureConfig config = context.config();

        for (int y = world.getMinY(); y < world.getHeight(); y++) {
            if (y % 16 == 0) {
                BlockPos pos = origin.atY(y);
                Block colorBlock = (y / 16 % 2 == 0) ? RDBlocks.DREAM_RED_BLOCK.asBlock() : RDBlocks.DREAM_BLUE_BLOCK.asBlock();
                for (int i = 0; i < 16; i++) {
                    world.setBlock(pos.west(i), colorBlock.defaultBlockState(), Block.UPDATE_KNOWN_SHAPE);
                }
                for (int i = 0; i < 15; i++) {
                    world.setBlock(pos.north(i + 1), colorBlock.defaultBlockState(), Block.UPDATE_KNOWN_SHAPE);
                }
            }
        }

        return true;
    }

}