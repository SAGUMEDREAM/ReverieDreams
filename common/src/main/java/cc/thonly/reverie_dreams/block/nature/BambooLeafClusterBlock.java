package cc.thonly.reverie_dreams.block.nature;

import cc.thonly.reverie_dreams.registry.content.block.RDBambooBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

/**
 * Side foliage is supported by exactly one neighbouring culm, with local updates on removal.
 */
public final class BambooLeafClusterBlock extends Block {
    public static final MapCodec<BambooLeafClusterBlock> CODEC = simpleCodec(BambooLeafClusterBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BambooLeafClusterBlock(Properties p) {
        super(p);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public MapCodec<BambooLeafClusterBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.relative(state.getValue(FACING).getOpposite())).is(RDBambooBlocks.LARGE_BAMBOO.asBlock());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        for (Direction d : context.getNearestLookingDirections())
            if (d.getAxis().isHorizontal()) {
                BlockState state = defaultBlockState().setValue(FACING, d.getOpposite());
                if (canSurvive(state, context.getLevel(), context.getClickedPos()))
                    return state;
            }
        return null;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction d, BlockPos other, BlockState neighbour, RandomSource random) {
        return canSurvive(state, level, pos) ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return box(1, 2, 1, 15, 14, 15);
    }

    //    @Override protected int getLightDampening(BlockState state){return 1;}
    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING);
    }
}
