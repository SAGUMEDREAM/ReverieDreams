package cc.thonly.reverie_dreams.mixin.world;

import cc.thonly.reverie_dreams.world.gensokyo.update.StructureUpdateGate;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class StructureUpdatePlayerMoveMixin {
    @Shadow
    public ServerPlayer player;

    // After vanilla's packet-thread handoff; never inspect mutable world state on the network thread.
    @Inject(method = "handleMovePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket;getX(D)D", ordinal = 0), cancellable = true)
    private void reverie$protectMaintenanceBoundary(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        double x = packet.getX(player.getX()), z = packet.getZ(player.getZ());
        if (!Double.isFinite(x) || !Double.isFinite(z))
            return;
        if (StructureUpdateGate.blocks(player.level(), new ChunkPos((int) Math.floor(x) >> 4, (int) Math.floor(z) >> 4).pack())) {
            player.connection.teleport(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
            ci.cancel();
        }
    }
}
