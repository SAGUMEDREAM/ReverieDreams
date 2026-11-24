package cc.thonly.reverie_dreams.mixin.server;

import cc.thonly.reverie_dreams.world.gensokyo.GensokyoBoundary;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Ender pearls, chorus fruit and other teleports bypass the collision perimeter.
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerBoundaryMixin {
    @ModifyVariable(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At("HEAD"),
            argsOnly = true
    )
    private TeleportTransition reverie_dreams$clampTeleport(TeleportTransition transition) {
        return GensokyoBoundary.clampTeleport((ServerPlayer) (Object) this, transition);
    }
}
