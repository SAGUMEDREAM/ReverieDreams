package cc.thonly.reverie_dreams.block.restaurant;

import cc.thonly.reverie_dreams.block.entity.RestaurantBlockEntity;
import cc.thonly.reverie_dreams.gui.block.RestaurantGui;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

public abstract class RestaurantBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    protected RestaurantBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(OPEN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
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

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (placer instanceof Player player && level.getBlockEntity(pos) instanceof RestaurantBlockEntity be)
            be.claim(player);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return interact(level, pos, player);
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return hand == InteractionHand.MAIN_HAND ? interact(level, pos, player) : InteractionResult.SUCCESS;
    }

    private InteractionResult interact(Level level, BlockPos pos, Player player) {
        if (level.isClientSide())
            return InteractionResult.SUCCESS;
        if (player instanceof ServerPlayer server && level.getBlockEntity(pos) instanceof RestaurantBlockEntity be) {
            if (be.canEdit(server)) {
                be.claim(server);
                if (be.isBell() && !player.isShiftKeyDown())
                    be.toggleBusiness(server);
                else
                    new RestaurantGui(server, be).open();
            } else
                server.sendSystemMessage(Component.translatable("restaurant.reverie_dreams.owner_only"));
        }
        return InteractionResult.SUCCESS_SERVER;
    }
}
