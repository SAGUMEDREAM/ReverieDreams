package cc.thonly.reverie_dreams.world.gen.feature;

import cc.thonly.reverie_dreams.block.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;


public class DreamGridFeature extends Feature<DreamGridFeatureConfig> {
    public DreamGridFeature(Codec<DreamGridFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DreamGridFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        BlockPos origin = context.getOrigin();
        DreamGridFeatureConfig config = context.getConfig();

        for (int y = world.getBottomY(); y < world.getHeight(); y++) {
            if (y % 16 == 0) {
                BlockPos pos = origin.withY(y);
                Block colorBlock = (y / 16 % 2 == 0) ? ModBlocks.DREAM_RED_BLOCK : ModBlocks.DREAM_BLUE_BLOCK;
                for (int i = 0; i < 16; i++) {
                    world.setBlockState(pos.west(i), colorBlock.getDefaultState(), Block.FORCE_STATE);
                }
                for (int i = 0; i < 15; i++) {
                    world.setBlockState(pos.north(i + 1), colorBlock.getDefaultState(), Block.FORCE_STATE);
                }
            }
        }

        return true;
    }

}