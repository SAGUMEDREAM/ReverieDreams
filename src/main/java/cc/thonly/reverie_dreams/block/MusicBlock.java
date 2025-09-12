package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.block.entity.ModBlockEntities;
import cc.thonly.reverie_dreams.block.entity.MusicBlockEntity;
import cc.thonly.reverie_dreams.util.TouhouNotaUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import nota.player.SongPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class MusicBlock extends BlockWithEntity {
    public static final BooleanProperty POWERED = Properties.POWERED;

    public MusicBlock(Settings settings) {
        super( settings);
        this.setDefaultState(this.getStateManager().getDefaultState().with(POWERED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(POWERED);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            MusicBlockEntity blockEntity = (MusicBlockEntity) serverWorld.getBlockEntity(pos);
            if (blockEntity == null) {
                return ActionResult.FAIL;
            }
            int index = -1;
            index = player.isSneaking() ? blockEntity.next() : blockEntity.prev();
            if (blockEntity.getFilenames().isEmpty()) {
                player.sendMessage(Text.translatable("item.reverie_dreams.music.no_files"), false);
                return ActionResult.PASS;
            }
            if (index == -1) {
                player.sendMessage(Text.translatable("item.reverie_dreams.music.no_music_selected"), false);
                return ActionResult.PASS;
            } else if (world.isReceivingRedstonePower(pos)){
                TouhouNotaUtils.playAt(world, pos, blockEntity.getSelect());
            }
            player.sendMessage(Text.translatable("item.reverie_dreams.music.switch_music", blockEntity.getSelect()), false);
            return ActionResult.SUCCESS_SERVER;
        }
        return super.onUse(state, world, pos, player, hit);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
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
        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, wireOrientation, notify);

        boolean hasPower = world.isReceivingRedstonePower(pos);
        boolean wasPowered = state.get(POWERED);

        if (hasPower != wasPowered) {
            world.setBlockState(pos, state.with(POWERED, hasPower), Block.NOTIFY_ALL);

            if (hasPower) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof MusicBlockEntity musicBlockEntity) {
                    TouhouNotaUtils.playAt(world, pos, musicBlockEntity.getSelect());
                }
            }
        }
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, ModBlockEntities.MUSIC_BLOCK_ENTITY, MusicBlockEntity::tick);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(MusicBlock::new);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MusicBlockEntity(pos, state);
    }
}
