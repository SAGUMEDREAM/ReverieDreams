package cc.thonly.reverie_dreams.event.block;

import cc.thonly.reverie_dreams.block.kitchen.PlateBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Plate eating must run before vanilla skips block use with occupied hands.
 */
public final class PlateBlockEventHandlers {
    private PlateBlockEventHandlers() {
    }

    public static InteractionResult onRightClickBlock(Player player, InteractionHand hand, BlockPos pos, Direction face) {
        if (!player.isShiftKeyDown() || player.isSpectator()) {
            return InteractionResult.PASS;
        }
        Level level = player.level();
        if (!(level.getBlockState(pos).getBlock() instanceof PlateBlock plate)) {
            return InteractionResult.PASS;
        }
        // A single click must never consume a second serving through the offhand.
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.CONSUME;
        }
        return plate.consumeFood(level, pos, player);
    }
}
