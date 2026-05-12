package cc.thonly.reverie_dreams.block.props;

import cc.thonly.reverie_dreams.block.entity.RemoteBlockEntity;
import cc.thonly.reverie_dreams.gui.block.RemoteGui;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import cc.thonly.reverie_dreams.server.RemoteSignalManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class RemoteServerBlock extends Block implements EntityBlock {
    public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;

    public RemoteServerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(OCCUPIED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OCCUPIED);
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(@NonNull BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
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
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                   @Nullable Orientation orientation, boolean bl) {
        if (!level.isClientSide()) {
            boolean hasSignal = level.hasNeighborSignal(pos);
            if (hasSignal != state.getValue(OCCUPIED)) {
                level.setBlock(pos, state.setValue(OCCUPIED, hasSignal), 3);

                RemoteBlockEntity be = getBlockEntity(level, pos);
                if (be != null && !be.isEmpty()) {
                    RemoteSignalManager.access().setValue(be, hasSignal);
                }
            }
        }
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> type) {
        return !level.isClientSide() && type == RDBlockEntityTypes.REMOTE_BLOCK_ENTITY.value()
                ? (l, p, s, be) -> ((RemoteBlockEntity) be).serverTick(l, p, s)
                : null;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RemoteBlockEntity(RemoteBlockEntity.RemoteType.SERVER, pos, state);
    }

    private RemoteBlockEntity getBlockEntity(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof RemoteBlockEntity r ? r : null;
    }
}
