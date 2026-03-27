package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.block.entity.MusicBlockEntity;
import cc.thonly.reverie_dreams.block.entity.RDBlockEntityTypes;
import cc.thonly.reverie_dreams.util.TouhouNotaUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import nota.player.SongPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class MusicBlock extends BaseEntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public MusicBlock(Properties settings) {
        super( settings);
        this.registerDefaultState(this.getStateDefinition().any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld) {
            MusicBlockEntity blockEntity = (MusicBlockEntity) serverWorld.getBlockEntity(pos);
            if (blockEntity == null) {
                return InteractionResult.FAIL;
            }
            int index = -1;
            index = player.isShiftKeyDown() ? blockEntity.next() : blockEntity.prev();
            if (blockEntity.getFilenames().isEmpty()) {
                player.displayClientMessage(Component.translatable("item.reverie_dreams.music.no_files"), false);
                return InteractionResult.PASS;
            }
            if (index == -1) {
                player.displayClientMessage(Component.translatable("item.reverie_dreams.music.no_music_selected"), false);
                return InteractionResult.PASS;
            } else if (world.hasNeighborSignal(pos)){
                TouhouNotaUtils.playAt(world, pos, blockEntity.getSelect());
            }
            player.displayClientMessage(Component.translatable("item.reverie_dreams.music.switch_music", blockEntity.getSelect()), false);
            return InteractionResult.SUCCESS_SERVER;
        }
        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof MusicBlockEntity musicBlockEntity) {
            SongPlayer selfPlayer = musicBlockEntity.getSelfPlayer();
            if (selfPlayer != null) {
                selfPlayer.setPlaying(false);
                Map<Long, SongPlayer> blockPos2SongPlayer = TouhouNotaUtils.blockMusicPlayCache.get(world);
                if (blockPos2SongPlayer != null) {
                    blockPos2SongPlayer.remove(pos.asLong());
                }
            }
        }
        return super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, @Nullable Orientation wireOrientation, boolean notify) {
        super.neighborChanged(state, world, pos, sourceBlock, wireOrientation, notify);

        boolean hasPower = world.hasNeighborSignal(pos);
        boolean wasPowered = state.getValue(POWERED);

        if (hasPower != wasPowered) {
            world.setBlock(pos, state.setValue(POWERED, hasPower), Block.UPDATE_ALL);

            if (hasPower) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof MusicBlockEntity musicBlockEntity) {
                    TouhouNotaUtils.playAt(world, pos, musicBlockEntity.getSelect());
                }
            }
        }
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, RDBlockEntityTypes.MUSIC_BLOCK, MusicBlockEntity::tick);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(MusicBlock::new);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MusicBlockEntity(pos, state);
    }
}
