package cc.thonly.reverie_dreams.block.restaurant;

import cc.thonly.reverie_dreams.block.entity.RestaurantBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;

public final class MenuBookBlock extends RestaurantBlock {
    public static final MapCodec<MenuBookBlock> CODEC = simpleCodec(MenuBookBlock::new);
    private static final VoxelShape CLOSED_NORTH_SOUTH = box(1, 0, 2, 15, 3, 14);
    private static final VoxelShape CLOSED_EAST_WEST = box(2, 0, 1, 14, 3, 15);
    private static final VoxelShape OPEN_NORTH_SOUTH = box(1, 0, 2, 15, 6, 14);
    private static final VoxelShape OPEN_EAST_WEST = box(2, 0, 1, 14, 6, 15);

    public MenuBookBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<MenuBookBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RestaurantBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        boolean eastWest = state.getValue(FACING).getAxis() == Direction.Axis.X;
        return state.getValue(OPEN) ? (eastWest ? OPEN_EAST_WEST : OPEN_NORTH_SOUTH)
                : (eastWest ? CLOSED_EAST_WEST : CLOSED_NORTH_SOUTH);
    }
}
