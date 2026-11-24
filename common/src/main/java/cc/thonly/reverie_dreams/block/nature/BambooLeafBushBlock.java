package cc.thonly.reverie_dreams.block.nature;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;

public final class BambooLeafBushBlock extends VegetationBlock {
    public static final MapCodec<BambooLeafBushBlock> CODEC = simpleCodec(BambooLeafBushBlock::new);

    public BambooLeafBushBlock(Properties p) {
        super(p);
    }

    @Override
    public MapCodec<BambooLeafBushBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState s, BlockGetter level, BlockPos pos, CollisionContext context) {
        return box(1, 0, 1, 15, 8, 15);
    }
}
