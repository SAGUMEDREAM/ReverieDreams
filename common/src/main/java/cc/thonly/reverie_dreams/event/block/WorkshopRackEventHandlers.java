package cc.thonly.reverie_dreams.event.block;

import cc.thonly.reverie_dreams.block.decor.workshop.WorkshopRackBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

/**
 * Runs before vanilla's occupied-hand sneak-use bypass, once per click.
 */
public final class WorkshopRackEventHandlers {
    private WorkshopRackEventHandlers() {
    }

    public static InteractionResult onRightClickBlock(Player player, InteractionHand hand, BlockPos pos, Direction face) {
        if (!player.isShiftKeyDown())
            return InteractionResult.PASS;
        var state = player.level().getBlockState(pos);
        if (!(state.getBlock() instanceof WorkshopRackBlock rack))
            return InteractionResult.PASS;
        if (hand != InteractionHand.MAIN_HAND)
            return InteractionResult.CONSUME;
        return rack.take(player.level(), pos, state, player);
    }
}
