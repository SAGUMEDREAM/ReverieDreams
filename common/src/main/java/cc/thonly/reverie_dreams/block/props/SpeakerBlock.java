package cc.thonly.reverie_dreams.block.props;

import cc.thonly.reverie_dreams.block.entity.SpeakerBlockEntity;
import cc.thonly.reverie_dreams.gui.block.SpeakerGui;
import cc.thonly.reverie_dreams.server.DelayedTask;
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

public class SpeakerBlock extends Block implements EntityBlock {
    public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;

    public SpeakerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(OCCUPIED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OCCUPIED);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                   @Nullable Orientation orientation, boolean bl) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            boolean hasSignal = level.hasNeighborSignal(pos);
            if (hasSignal != state.getValue(OCCUPIED)) {
                level.setBlock(pos, state.setValue(OCCUPIED, hasSignal), 3);

                SpeakerBlockEntity be = getBlockEntity(level, pos);
                if (be != null) {
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
                            for (int i = 0; i <= 20 * 10 * 2; i += 2) {
                                int delay = i;
                                DelayedTask.create(serverLevel.getServer(), delay, () -> {
                                    level.playSound(null, pos, SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 4.0f, 1.1f);
                                });
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (!level.isClientSide()) {
            SpeakerBlockEntity be = getBlockEntity(level, blockPos);
            if (player.isShiftKeyDown()) {
                if (be == null) {
                    return InteractionResult.FAIL;
                }
                be.setBellSound(!be.isBellSound());
                be.setChanged();
                ((ServerPlayer) player).sendSystemMessage(Component.literal("§aBell Sound: " + be.isBellSound()));
                return InteractionResult.SUCCESS_SERVER;
            }
            SpeakerGui speakerGui = new SpeakerGui(be, (ServerPlayer) player);
            speakerGui.open();
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpeakerBlockEntity(pos, state);
    }

    private SpeakerBlockEntity getBlockEntity(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof SpeakerBlockEntity r ? r : null;
    }
}
