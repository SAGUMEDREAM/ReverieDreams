package cc.thonly.reverie_dreams.block.decor;

import cc.thonly.reverie_dreams.block.entity.RecordPlayerBlockEntity;
import cc.thonly.reverie_dreams.client.audio.radio.RadioAudioClient;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import cc.thonly.reverie_dreams.server.dialog.RecordPlayerDialog;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class RecordPlayerBlock extends ThinDecorBlock implements EntityBlock {
    public static final MapCodec<RecordPlayerBlock> CODEC = simpleCodec(RecordPlayerBlock::new);
    public static final BooleanProperty PLAYING = BooleanProperty.create("playing");

    public RecordPlayerBlock(Properties properties) {
        super(properties, Block.box(1, 0, 1, 15, 7, 15), true);
        registerDefaultState(defaultBlockState().setValue(PLAYING, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PLAYING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RecordPlayerBlockEntity(pos, state);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide() || type != RDBlockEntityTypes.RECORD_PLAYER.get())
            return null;
        return (world, pos, block, host) -> RecordPlayerBlockEntity.clientTick(world, pos, block);
    }

    public InteractionResult setPlaying(Level level, BlockPos pos, BlockState state, Player player, boolean playing) {
        if (player.isSpectator() || !player.mayBuild() || !level.mayInteract(player, pos))
            return InteractionResult.FAIL;
        if (level.isClientSide() && playing)
            RadioAudioClient.activationHint(player);
        if (!level.isClientSide() && state.getValue(PLAYING) != playing) {
            level.setBlock(pos, state.setValue(PLAYING, playing), UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, .35f, playing ? .65f : .5f);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return interact(level, pos, state, player);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return interact(level, pos, state, player);
    }

    public InteractionResult interact(Level level, BlockPos pos, BlockState state, Player player) {
        if (!player.isShiftKeyDown())
            return setPlaying(level, pos, state, player, !state.getValue(PLAYING));
        if (player.isSpectator() || !player.mayBuild() || !level.mayInteract(player, pos))
            return InteractionResult.FAIL;
        if (player instanceof ServerPlayer serverPlayer)
            RecordPlayerDialog.open(serverPlayer, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(PLAYING) && random.nextInt(8) == 0)
            level.addParticle(ParticleTypes.NOTE, pos.getX() + .5, pos.getY() + .65, pos.getZ() + .5, random.nextInt(24) / 24.0, 0, 0);
    }
}
