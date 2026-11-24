package cc.thonly.reverie_dreams.block.redstone;

import cc.thonly.reverie_dreams.block.entity.SpeakerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import cc.thonly.reverie_dreams.server.dialog.RedstoneConfigDialog;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class SpeakerBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;

    public SpeakerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(OCCUPIED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OCCUPIED, FACING);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                   @Nullable Orientation orientation, boolean bl) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            boolean hasSignal = level.hasNeighborSignal(pos);
            if (hasSignal != state.getValue(OCCUPIED)) {
                level.setBlock(pos, state.setValue(OCCUPIED, hasSignal), 3);

                SpeakerBlockEntity be = getBlockEntity(level, pos);
                if (be != null && hasSignal && be.beginBroadcast(level.getGameTime())) {
                    if (hasSignal) {
                        MutableComponent texts = Component.empty();
                        List<String> ls = be.getTexts();
                        if (!ls.isEmpty()) {
                            for (int i = 0; i < ls.size(); i++) {
                                texts.append(ls.get(i));
                                if (i != ls.size() - 1) {
                                    texts.append("\n");
                                }
                            }
                            AABB box = new AABB(pos).inflate(50);

                            serverLevel.getEntitiesOfClass(ServerPlayer.class, box)
                                    .forEach(player -> player.sendSystemMessage(texts));
                        }
                        if (be.isBellSound()) {
                            level.playSound(null, pos, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 2.0f, 1.1f);
                        }
                    }
                }
            }
        }
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (player instanceof ServerPlayer serverPlayer)
            RedstoneConfigDialog.open(serverPlayer, blockPos);
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpeakerBlockEntity(pos, state);
    }

    private SpeakerBlockEntity getBlockEntity(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof SpeakerBlockEntity r ? r : null;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

}
