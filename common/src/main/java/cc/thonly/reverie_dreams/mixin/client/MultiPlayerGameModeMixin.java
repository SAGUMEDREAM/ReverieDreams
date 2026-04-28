package cc.thonly.reverie_dreams.mixin.client;

import cc.thonly.reverie_dreams.api.block.AttackBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    protected abstract void startPrediction(ClientLevel clientLevel, PredictiveAction predictiveAction);

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAbilities()Lnet/minecraft/world/entity/player/Abilities;", ordinal = 0), method = "startDestroyBlock", cancellable = true)
    public void attackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> info) {
        api_fireAttackBlockCallback(pos, direction, info);
    }

    @Unique
    private void api_fireAttackBlockCallback(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> info) {
        InteractionResult result = AttackBlockCallback.EVENT.invoker().interact(minecraft.player, minecraft.level, InteractionHand.MAIN_HAND, pos, direction);

        if (result != InteractionResult.PASS) {
            // Returning true will spawn particles and trigger the animation of the hand -> only for SUCCESS.
            info.setReturnValue(result == InteractionResult.SUCCESS);

            // We also need to let the server process the action if it's accepted.
            if (result.consumesAction()) {
                startPrediction(minecraft.level, id -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, direction, id));
            }
        }
    }
}
