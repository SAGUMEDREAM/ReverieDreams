package cc.thonly.minecraft.mixin;

import cc.thonly.minecraft.api.ItemLeftClickCallback;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
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
    @Inject(method = "handleAnimate", at = @At("HEAD"))
    public void onItemLeftClick(ServerboundSwingPacket serverboundSwingPacket, CallbackInfo ci) {
        ServerGamePacketListenerImpl self = (ServerGamePacketListenerImpl) (Object) this;
        InteractionHand hand = serverboundSwingPacket.getHand();
        Player player = self.player;
        Level world = player.level();
        if (hand != InteractionHand.MAIN_HAND) return;
        ItemLeftClickCallback.EVENT.invoker().leftClick(world, player, hand);

    }
}
