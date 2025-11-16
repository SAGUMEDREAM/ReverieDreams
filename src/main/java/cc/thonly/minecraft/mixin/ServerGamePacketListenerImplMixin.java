package cc.thonly.minecraft.mixin;

import cc.thonly.minecraft.api.ItemLeftClickCallback;
import cc.thonly.reverie_dreams.server.DelayedTask;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    private boolean recentAttackPacket = false;

    @Inject(method = "handlePlayerAction", at = @At("HEAD"))
    private void handle(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        ServerGamePacketListenerImpl self = (ServerGamePacketListenerImpl) (Object) this;
        Player player = self.player;
        Level world = player.level();
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        if (player.isSpectator()) return;
        if (player.isUsingItem()) return;

        if (recentAttackPacket) {
            ItemLeftClickCallback.EVENT.invoker().leftClick(world, player, InteractionHand.MAIN_HAND);
        } else {
            recentAttackPacket = true;
        }
    }
}
