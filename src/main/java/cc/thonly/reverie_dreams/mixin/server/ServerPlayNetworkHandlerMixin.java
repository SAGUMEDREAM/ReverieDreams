package cc.thonly.reverie_dreams.mixin.server;

import cc.thonly.reverie_dreams.server.PlayerInputManager;
import cc.thonly.reverie_dreams.util.PairWrapper;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow
    public abstract ServerPlayerEntity getPlayer();

    @Inject(method = "onPlayerInput", at = @At("TAIL"))
    public void onPlayerInput(PlayerInputC2SPacket packet, CallbackInfo ci) {
        ServerPlayerEntity player = this.getPlayer();
        PlayerInputManager.TICK_PLAYER_QUEUE.add(new PairWrapper<>(player, packet));
    }

    @Inject(method = "onClientCommand", at = @At("TAIL"))
    public void onClientCommand(ClientCommandC2SPacket packet, CallbackInfo ci) {
        ServerPlayerEntity player = this.getPlayer();
        PlayerInputManager.TICK_PLAYER_QUEUE.add(new PairWrapper<>(player, packet));
    }
}
