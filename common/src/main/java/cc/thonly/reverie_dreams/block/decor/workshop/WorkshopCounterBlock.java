package cc.thonly.reverie_dreams.block.decor.workshop;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class WorkshopCounterBlock extends WorkshopStorageBlock {
    public static final MapCodec<WorkshopCounterBlock> CODEC = simpleCodec(WorkshopCounterBlock::new);

    public WorkshopCounterBlock(Properties properties) {
        super(WorkshopKind.CONNECTED_COUNTER, properties);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WorkshopConnections.PROPERTIES);
    }

    private BlockState connect(BlockState state, LevelReader level, BlockPos pos) {
        return WorkshopConnections.update(state, level, pos, (direction, other) -> other.getBlock() instanceof WorkshopCounterBlock);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return connect(super.getStateForPlacement(context), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        return direction.getAxis().isHorizontal() ? connect(state, level, pos) : super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
    }

    @Override
    protected String model(BlockState state) {
        return kind.id() + "_" + WorkshopConnections.localMask(state) + (state.getValue(OPEN) ? "_open" : "");
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return WorkshopConnections.rotate(state, rotation);
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return WorkshopConnections.mirror(state, mirror);
    }
}
