package cc.thonly.reverie_dreams.mixin;

import cc.thonly.reverie_dreams.server.CustomClickActionRegistry;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Slf4j
@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonNetworkHandlerMixin {

    @Inject(method = "handleCustomClickAction", at = @At("TAIL"))
    private void handleCustomClick(ServerboundCustomClickActionPacket packet, CallbackInfo ci) {
        try {
            CustomClickActionRegistry.handle(packet);
        } catch (Exception err) {
            log.error("Can't parse custom click action packet");
        }
    }
}
