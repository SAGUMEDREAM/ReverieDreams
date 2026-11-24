package cc.thonly.reverie_dreams.block.decor;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChochinBlock extends LanternBlock {
    public static final MapCodec<ChochinBlock> CODEC = simpleCodec(ChochinBlock::new);

    // Match the three elements in models/block/chochin.json.
    private static final VoxelShape STANDING_SHAPE = Shapes.or(
            Block.box(4.0, 0.0, 4.0, 12.0, 1.0, 12.0),
            Block.box(3.0, 1.0, 3.0, 13.0, 15.0, 13.0),
            Block.box(4.0, 15.0, 4.0, 12.0, 16.0, 12.0));

    // Match the body, cap and hanger in models/block/chochin_hanging.json.
    private static final VoxelShape HANGING_SHAPE = Shapes.or(
            Block.box(3.0, 0.0, 3.0, 13.0, 14.0, 13.0),
            Block.box(4.0, 14.0, 4.0, 12.0, 15.0, 12.0),
            Block.box(7.0, 15.0, 7.0, 9.0, 16.0, 9.0));

    public ChochinBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<ChochinBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HANGING) ? HANGING_SHAPE : STANDING_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.getShape(state, level, pos, context);
    }
}
