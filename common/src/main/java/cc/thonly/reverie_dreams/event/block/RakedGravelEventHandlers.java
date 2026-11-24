package cc.thonly.reverie_dreams.event.block;

import cc.thonly.reverie_dreams.block.decor.RakedGravelBlock;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Handles both loaders before vanilla's sneak-use bypass and only changes blocks on the server.
 */
public final class RakedGravelEventHandlers {
    public enum Tool {NONE, HOE, SHOVEL}

    private RakedGravelEventHandlers() {
    }

    public static Tool tool(ItemStack stack) {
        if (stack.isEmpty())
            return Tool.NONE;
        if (stack.getItem() instanceof HoeItem || stack.is(ItemTags.HOES))
            return Tool.HOE;
        if (stack.getItem() instanceof ShovelItem || stack.is(ItemTags.SHOVELS))
            return Tool.SHOVEL;
        return Tool.NONE;
    }

    public static BlockState nextState(RakedGravelBlock block, BlockState current, Tool tool, boolean shift,
                                       Direction heading, BlockGetter level, BlockPos pos) {
        if (tool == Tool.HOE && current.is(Blocks.GRAVEL))
            return block.withLayout(block.defaultBlockState().setValue(RakedGravelBlock.AXIS, heading.getAxis()), level, pos);
        if (!current.is(block))
            return null;
        if (tool == Tool.SHOVEL)
            return Blocks.GRAVEL.defaultBlockState();
        return tool == Tool.HOE ? block.rake(current, shift, level, pos) : null;
    }

    public static InteractionResult onRightClickBlock(Player player, InteractionHand hand, BlockPos pos, Direction face) {
        Tool tool = tool(player.getItemInHand(hand));
        if (tool == Tool.NONE)
            return InteractionResult.PASS;
        BlockState state = player.level().getBlockState(pos);
        if (!state.is(Blocks.GRAVEL) && !(state.getBlock() instanceof RakedGravelBlock))
            return InteractionResult.PASS;
        RakedGravelBlock block = state.getBlock() instanceof RakedGravelBlock raked ? raked : (RakedGravelBlock) RDBlocks.RAKED_GRAVEL.asBlock();
        return interact(player, hand, pos, face, block);
    }

    public static InteractionResult interact(Player player, InteractionHand hand, BlockPos pos, Direction face, RakedGravelBlock block) {
        Level level = player.level();
        ItemStack stack = player.getItemInHand(hand);
        Tool tool = tool(stack);
        if (face != Direction.UP || player.isSpectator() || !player.mayBuild() || !level.mayInteract(player, pos)
                || !player.mayUseItemAt(pos, face, stack) || !level.getBlockState(pos.above()).isAir())
            return InteractionResult.PASS;
        BlockState next = nextState(block, level.getBlockState(pos), tool, player.isShiftKeyDown(), player.getDirection(), level, pos);
        if (next == null)
            return InteractionResult.PASS;
        if (level.isClientSide())
            return InteractionResult.SUCCESS;
        if (!level.setBlock(pos, next, Block.UPDATE_ALL))
            return InteractionResult.FAIL;
        block.notifyLayout(level, pos);
        if (!player.getAbilities().instabuild)
            stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
        level.playSound(null, pos, tool == Tool.SHOVEL ? SoundEvents.SHOVEL_FLATTEN : SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1, 1);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, next));
        Component feedback = next.is(block) ? Component.translatable("message.reverie_dreams.raked_gravel.layout",
                Component.translatable("rake_mode.reverie_dreams." + next.getValue(RakedGravelBlock.MODE).getSerializedName()),
                Component.translatable("rake_shape.reverie_dreams." + next.getValue(RakedGravelBlock.SHAPE).getSerializedName()))
                : Component.translatable("message.reverie_dreams.raked_gravel.restored");
        if (player instanceof ServerPlayer serverPlayer)
            serverPlayer.sendSystemMessage(feedback, true);
        return InteractionResult.SUCCESS_SERVER;
    }
}
