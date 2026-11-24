package cc.thonly.reverie_dreams.block.decor.workshop;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.function.BiPredicate;

public final class WorkshopConnections {
    public static final BooleanProperty[] PROPERTIES = {BlockStateProperties.NORTH, BlockStateProperties.EAST, BlockStateProperties.SOUTH, BlockStateProperties.WEST};
    public static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    public static BlockState update(BlockState state, LevelReader level, BlockPos pos, BiPredicate<Direction, BlockState> accepts) {
        for (int i = 0; i < 4; i++)
            state = state.setValue(PROPERTIES[i], accepts.test(DIRECTIONS[i], level.getBlockState(pos.relative(DIRECTIONS[i]))));
        return state;
    }

    public static int localMask(BlockState state) {
        int mask = 0;
        Direction north = state.getValue(HorizontalDirectionalBlock.FACING);
        Direction[] directions = {north, north.getClockWise(), north.getOpposite(), north.getCounterClockWise()};
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                if (directions[i] == DIRECTIONS[j] && state.getValue(PROPERTIES[j]))
                    mask |= 1 << i;
        return mask;
    }

    public static BlockState rotate(BlockState state, Rotation rotation) {
        BlockState result = state.setValue(HorizontalDirectionalBlock.FACING, rotation.rotate(state.getValue(HorizontalDirectionalBlock.FACING)));
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                if (rotation.rotate(DIRECTIONS[i]) == DIRECTIONS[j])
                    result = result.setValue(PROPERTIES[j], state.getValue(PROPERTIES[i]));
        return result;
    }

    public static BlockState mirror(BlockState state, Mirror mirror) {
        BlockState result = state.setValue(HorizontalDirectionalBlock.FACING, mirror.mirror(state.getValue(HorizontalDirectionalBlock.FACING)));
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                if (mirror.mirror(DIRECTIONS[i]) == DIRECTIONS[j])
                    result = result.setValue(PROPERTIES[j], state.getValue(PROPERTIES[i]));
        return result;
    }

    private WorkshopConnections() {
    }
}
