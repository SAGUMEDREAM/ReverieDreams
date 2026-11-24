package cc.thonly.reverie_dreams.event.block;

import cc.thonly.reverie_dreams.block.decor.RecordPlayerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;

public final class RecordPlayerEventHandlers {
    private RecordPlayerEventHandlers() {
    }

    public static InteractionResult onRightClickBlock(Player player, InteractionHand hand, BlockPos pos, Direction face) {
        if (!player.isShiftKeyDown())
            return InteractionResult.PASS;
        var state = player.level().getBlockState(pos);
        if (!(state.getBlock() instanceof RecordPlayerBlock record))
            return InteractionResult.PASS;
        return hand == InteractionHand.MAIN_HAND ? record.interact(player.level(), pos, state, player) : InteractionResult.CONSUME;
    }
}
