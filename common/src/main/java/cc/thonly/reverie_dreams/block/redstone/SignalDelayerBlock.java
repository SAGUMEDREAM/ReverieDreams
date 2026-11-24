package cc.thonly.reverie_dreams.block.redstone;

import cc.thonly.reverie_dreams.block.entity.SignalDelayerBlockEntity;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
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
import cc.thonly.reverie_dreams.server.dialog.RedstoneConfigDialog;

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

        if (level.isClientSide())
            return;

        refreshInput(level, pos, state);
    }

    private boolean hasInput(Level level, BlockPos pos, BlockState state) {
        for (Direction side : Direction.values()) {
            if (side != state.getValue(FACING) && level.getSignal(pos.relative(side), side) > 0)
                return true;
        }
        return false;
    }

    public void refreshInput(Level level, BlockPos pos, BlockState state) {
        if (hasInput(level, pos, state) != state.getValue(OCCUPIED))
            restart(level, pos, state);
    }

    public void restart(Level level, BlockPos pos, BlockState state) {
        SignalDelayerBlockEntity be = getBlockEntity(level, pos);
        if (be == null)
            return;
        boolean input = hasInput(level, pos, state);
        int delay = be.getMaxDelayTick();
        be.setNowTick(0);
        be.setActivationTick(input ? level.getGameTime() + delay : -1);
        be.setChanged();
        BlockState next = state.setValue(OCCUPIED, input).setValue(POWERED, input && delay == 0);
        if (state != next) {
            level.setBlock(pos, next, 3);
            level.updateNeighbourForOutputSignal(pos, this);
        }
        if (input && delay > 0)
            level.scheduleTick(pos, this, delay);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        refreshInput(level, pos, state);
        state = level.getBlockState(pos);
        var be = getBlockEntity(level, pos);
        if (be == null || !state.getValue(OCCUPIED) || state.getValue(POWERED))
            return;
        long remaining = be.getActivationTick() - level.getGameTime();
        if (remaining > 0) {
            level.scheduleTick(pos, this, (int) Math.min(remaining, RedstoneSettings.MAX_DELAY));
            return;
        }
        level.setBlock(pos, state.setValue(POWERED, true), 3);
        level.updateNeighbourForOutputSignal(pos, this);
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
        if (!state.getValue(POWERED))
            return 0;
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
        return level.isClientSide() ? null : createTickerHelper(type, RDBlockEntityTypes.SIGNAL_DELAYER_BLOCK_ENTITY.value(), SignalDelayerBlockEntity::onBlockEntityTick);
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (player instanceof ServerPlayer serverPlayer)
            RedstoneConfigDialog.open(serverPlayer, blockPos);
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
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
