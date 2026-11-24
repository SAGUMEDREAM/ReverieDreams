package cc.thonly.reverie_dreams.mixin.server;

import cc.thonly.reverie_dreams.world.gensokyo.GensokyoBoundary;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The player position is written back by vanilla right after {@code doTick}, so the
 * clamp runs once the tick's final position is known.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerBoundaryMixin {
    @Shadow
    public ServerPlayer player;

    @Inject(method = "tickPlayer", at = @At("RETURN"))
    private void reverie_dreams$confineToBoundary(CallbackInfoReturnable<Boolean> cir) {
        GensokyoBoundary.confinePlayer(this.player);
    }
}
