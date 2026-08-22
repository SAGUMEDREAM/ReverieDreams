package cc.thonly.reverie_dreams.mixin.block;

import cc.thonly.reverie_dreams.advancement.SimpleTrigger;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChestBlock.class)
public class ChestBlockMixin {
    @Inject(method = "useWithoutItem", at = @At("RETURN"))
    public void reverie_dreams$callTriggerKey(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (cir.getReturnValue() != InteractionResult.FAIL && player instanceof ServerPlayer serverPlayer) {
            SimpleTrigger.trigger(serverPlayer, SimpleTriggerKeys.OPEN_CHEST);
        }
    }
}
