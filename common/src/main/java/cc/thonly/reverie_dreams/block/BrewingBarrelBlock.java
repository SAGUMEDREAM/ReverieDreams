package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.block.entity.BrewingBarrelBlockEntity;
import cc.thonly.reverie_dreams.gui.block.BrewingBarrelGui;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class BrewingBarrelBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public BrewingBarrelBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld) {
            BrewingBarrelBlockEntity blockEntity = this.getBlockEntity(world, pos);
            if (blockEntity != null) {
                SimpleContainer inventory = blockEntity.getInventory();
                for (int i = 0; i < inventory.getContainerSize(); i++) {
                    ItemStack stack = inventory.getItem(i);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    ItemEntity itemEntity = new ItemEntity(serverWorld, pos.getX(), pos.getY(), pos.getZ(), stack);
                    serverWorld.addFreshEntity(itemEntity);
                }
            }
        }
        return super.playerWillDestroy(world, pos, state, player);
    }

    public @Nullable BrewingBarrelBlockEntity getBlockEntity(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof BrewingBarrelBlockEntity brewingBarrelBlockEntity ? brewingBarrelBlockEntity : null;
    }

    @Override
    public InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            BrewingBarrelBlockEntity blockEntity = this.getBlockEntity(serverLevel, pos);
            if (blockEntity == null) {
                return InteractionResult.FAIL;
            }
            if (!player.isShiftKeyDown()) {
                BrewingBarrelGui gui = new BrewingBarrelGui(serverPlayer, state, level, pos);
                gui.open();
                player.swing(hand);
                SoundEventPlayUtils.playSound(player, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS);
                return InteractionResult.SUCCESS_SERVER;
            }
            boolean brewing = blockEntity.isBrewing();
            if (brewing) {
                return InteractionResult.FAIL;
            }
            boolean started = blockEntity.startMatchesBrewing();
            if (player.isShiftKeyDown() && started && itemStack.isEmpty()) {
                return InteractionResult.SUCCESS_SERVER;
            }
            if (blockEntity.onUseItem(player, itemStack)) {
                return InteractionResult.SUCCESS_SERVER;
            }
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> type) {
        return createTickerHelper(type, RDBlockEntityTypes.BREWING_BARREL.get(), BrewingBarrelBlockEntity::onBlockEntityTick);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> blockEntityType, BlockEntityType<E> blockEntityType2, BlockEntityTicker<? super E> blockEntityTicker) {
        return blockEntityType2 == blockEntityType ? (BlockEntityTicker<A>) blockEntityTicker : null;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
        return new BrewingBarrelBlockEntity(worldPosition, blockState);
    }
}
