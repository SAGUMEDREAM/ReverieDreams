package cc.thonly.reverie_dreams.block.props;

import cc.thonly.reverie_dreams.block.entity.RemoteBlockEntity;
import cc.thonly.reverie_dreams.gui.block.RemoteGui;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class RemoteClientBlock extends Block implements EntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public RemoteClientBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(@NonNull BlockState blockState, Level level, @NonNull BlockPos blockPos, @NonNull Player player, @NonNull BlockHitResult blockHitResult) {
        if (!level.isClientSide()) {
            RemoteBlockEntity blockEntity = getBlockEntity(level, blockPos);
            if (blockEntity == null) {
                return InteractionResult.FAIL;
            }
            RemoteGui remoteGui = new RemoteGui(blockEntity, (ServerPlayer) player);
            remoteGui.open();
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public boolean isSignalSource(@NonNull BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, @NonNull BlockGetter world, @NonNull BlockPos pos, @NonNull Direction dir) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(@NonNull BlockState state, @NonNull BlockGetter world, @NonNull BlockPos pos, @NonNull Direction dir) {
        return getSignal(state, world, pos, dir);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NonNull BlockState blockState, BlockEntityType<T> type) {
        return !level.isClientSide() && type == RDBlockEntityTypes.REMOTE_BLOCK_ENTITY.value()
                ? (l, p, s, be) -> ((RemoteBlockEntity) be).clientTick(l, p, s)
                : null;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new RemoteBlockEntity(RemoteBlockEntity.RemoteType.CLIENT, pos, state);
    }

    private RemoteBlockEntity getBlockEntity(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof RemoteBlockEntity r ? r : null;
    }
}
