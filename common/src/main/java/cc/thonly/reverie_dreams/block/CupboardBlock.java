package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.block.entity.BrewingBarrelBlockEntity;
import cc.thonly.reverie_dreams.block.entity.CupboardBlockEntity;
import cc.thonly.reverie_dreams.gui.block.CupboardGui;
import cc.thonly.reverie_dreams.gui.container.InfiniteContainerGui;
import cc.thonly.reverie_dreams.inventory.InfiniteInventory;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class CupboardBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    public CupboardBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState playerWillDestroy(
            Level world,
            BlockPos pos,
            BlockState state,
            Player player
    ) {
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld) {
            CupboardBlockEntity blockEntity = this.getBlockEntity(world, pos);
            if (blockEntity != null) {
                InfiniteInventory inventory = blockEntity.getInventory();
                for (int i = 0; i < inventory.getContainerSize(); i++) {
                    ItemStack single = inventory.getSingleItem(i);

                    if (single.isEmpty()) {
                        continue;
                    }

                    int count = inventory.getItemCount(i);
                    int maxStackSize = single.getMaxStackSize();

                    while (count > 0) {
                        int amount = Math.min(count, maxStackSize);
                        ItemStack drop = single.copyWithCount(amount);
                        ItemEntity itemEntity = new ItemEntity(serverWorld, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
                        serverWorld.addFreshEntity(itemEntity);
                        count -= amount;
                    }
                }
            }
        }

        return super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    public InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            CupboardGui gui = new CupboardGui(serverPlayer, state, level, pos);
            gui.open();
            player.swing(hand);
            SoundEventPlayUtils.playSound(player, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    public @Nullable CupboardBlockEntity getBlockEntity(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof CupboardBlockEntity cupboardBlockEntity ? cupboardBlockEntity : null;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction direction = ctx.getHorizontalDirection();
        return this.defaultBlockState().setValue(FACING, direction);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
        return new CupboardBlockEntity(worldPosition, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> type) {
        return createTickerHelper(type, RDBlockEntityTypes.CUPBOARD.get(), CupboardBlockEntity::onBlockEntityTick);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> blockEntityType, BlockEntityType<E> blockEntityType2, BlockEntityTicker<? super E> blockEntityTicker) {
        return blockEntityType2 == blockEntityType ? (BlockEntityTicker<A>) blockEntityTicker : null;
    }
}
