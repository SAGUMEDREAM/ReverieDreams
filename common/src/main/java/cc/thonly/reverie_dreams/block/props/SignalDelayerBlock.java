package cc.thonly.reverie_dreams.block.props;

import cc.thonly.reverie_dreams.block.entity.SignalDelayerBlockEntity;
import cc.thonly.reverie_dreams.gui.block.SignalDelayerGui;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class SignalDelayerBlock extends Block implements EntityBlock {
    public static final MapCodec<SignalDelayerBlock> CODEC = simpleCodec(SignalDelayerBlock::new);
    public static final EnumProperty<Direction> FACING = DirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;

    public SignalDelayerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false)
                .setValue(OCCUPIED, false)
        );
    }

    @Override
    protected void neighborChanged(@NonNull BlockState state, @NonNull Level level, @NonNull BlockPos pos,
                                   @NonNull Block block, @Nullable Orientation orientation, boolean moved) {
        super.neighborChanged(state, level, pos, block, orientation, moved);

        if (level.isClientSide()) return;

        boolean hasSignal = level.hasNeighborSignal(pos);
        boolean occupied = state.getValue(OCCUPIED);

        if (hasSignal != occupied) {
            SignalDelayerBlockEntity be = getBlockEntity(level, pos);

            BlockState newState = state.setValue(OCCUPIED, hasSignal);

            if (hasSignal) {
                if (be != null) {
                    be.setNowTick(0);
                }
                newState = newState.setValue(POWERED, false);
            } else {
                if (be != null) {
                    be.setNowTick(0);
                }
                newState = newState.setValue(POWERED, false);
            }

            level.setBlock(pos, newState, 3);
            level.updateNeighbourForOutputSignal(pos, this);
        }
    }


    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (!state.getValue(POWERED)) return 0;
        Direction facing = state.getValue(FACING);
        return direction == facing.getOpposite() ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(state, level, pos, direction);
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos blockPos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> type) {
        return createTickerHelper(type, RDBlockEntityTypes.SIGNAL_DELAYER_BLOCK_ENTITY.value(), SignalDelayerBlockEntity::onBlockEntityTick);
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (!level.isClientSide()) {
            SignalDelayerBlockEntity blockEntity = this.getBlockEntity(level, blockPos);
            if (blockEntity == null) {
                return InteractionResult.FAIL;
            }
            SimpleGui inputGui = new SignalDelayerGui((ServerPlayer) player, blockEntity);
            inputGui.open();
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, OCCUPIED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getNearestLookingDirection().getOpposite())
                .setValue(POWERED, false)
                .setValue(OCCUPIED, false);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> blockEntityType, BlockEntityType<E> blockEntityType2, BlockEntityTicker<? super E> blockEntityTicker) {
        return blockEntityType2 == blockEntityType ? (BlockEntityTicker<A>) blockEntityTicker : null;
    }

    @Override
    public @NotNull MapCodec<? extends SignalDelayerBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new SignalDelayerBlockEntity(blockPos, blockState);
    }

    public SignalDelayerBlockEntity getBlockEntity(Level level, BlockPos blockPos) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        return blockEntity instanceof SignalDelayerBlockEntity ? (SignalDelayerBlockEntity) blockEntity : null;
    }
}
