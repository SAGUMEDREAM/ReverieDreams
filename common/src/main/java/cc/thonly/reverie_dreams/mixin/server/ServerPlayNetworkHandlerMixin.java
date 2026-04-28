package cc.thonly.reverie_dreams.mixin.server;

import cc.thonly.reverie_dreams.server.PlayerInputManager;
import cc.thonly.reverie_dreams.util.PairWrapper;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow
    public abstract ServerPlayer getPlayer();

    @Inject(method = "handlePlayerInput", at = @At("TAIL"))
    public void onPlayerInput(ServerboundPlayerInputPacket packet, CallbackInfo ci) {
        ServerPlayer player = this.getPlayer();
        PlayerInputManager.TICK_PLAYER_QUEUE.add(new PairWrapper<>(player, packet));
    }

    @Inject(method = "handlePlayerCommand", at = @At("TAIL"))
    public void onClientCommand(ServerboundPlayerCommandPacket packet, CallbackInfo ci) {
        ServerPlayer player = this.getPlayer();
        PlayerInputManager.TICK_PLAYER_QUEUE.add(new PairWrapper<>(player, packet));
    }
}
