package cc.thonly.reverie_dreams.mixin.client;

import cc.thonly.reverie_dreams.client.GensokyoLoadTrackerAccess;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.LevelLoadTracker;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class GensokyoClientPacketListenerMixin {
    @Shadow
    private LevelLoadTracker levelLoadTracker;

    @Inject(method = "handleMovePlayer", at = @At("TAIL"))
    private void rd$position(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        if (this.levelLoadTracker != null) {
            ((GensokyoLoadTrackerAccess) this.levelLoadTracker).reverie_dreams$receivedPosition();
        }
    }

    @Inject(method = "enableChunkLight", at = @At("TAIL"))
    private void rd$litChunk(LevelChunk chunk, int x, int z, CallbackInfo ci) {
        if (this.levelLoadTracker != null) {
            ((GensokyoLoadTrackerAccess) this.levelLoadTracker).reverie_dreams$receivedLitChunk(x, z);
        }
    }
}
