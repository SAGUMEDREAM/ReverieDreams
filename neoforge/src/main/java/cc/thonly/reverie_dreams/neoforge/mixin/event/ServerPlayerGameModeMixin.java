package cc.thonly.reverie_dreams.neoforge.mixin.event;

import cc.thonly.reverie_dreams.api.block.AttackBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
    @Final
    @Shadow
    protected ServerPlayer player;

    @Shadow
    protected ServerLevel level;

    @Inject(at = @At("HEAD"), method = "handleBlockBreakAction", cancellable = true)
    public void startBlockBreak(BlockPos pos, ServerboundPlayerActionPacket.Action playerAction, Direction direction, int worldHeight, int i, CallbackInfo info) {
        if (playerAction != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) return;
        InteractionResult result = AttackBlockCallback.EVENT.invoker().interact(player, level, InteractionHand.MAIN_HAND, pos, direction);

        if (result != InteractionResult.PASS) {
            // The client might have broken the block on its side, so make sure to let it know.
            this.player.connection.send(new ClientboundBlockUpdatePacket(level, pos));

            if (level.getBlockState(pos).hasBlockEntity()) {
                BlockEntity blockEntity = level.getBlockEntity(pos);

                if (blockEntity != null) {
                    Packet<ClientGamePacketListener> updatePacket = blockEntity.getUpdatePacket();

                    if (updatePacket != null) {
                        this.player.connection.send(updatePacket);
                    }
                }
            }

            info.cancel();
        }
    }
}
