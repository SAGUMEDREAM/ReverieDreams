package cc.thonly.reverie_dreams.mixin.server;

import cc.thonly.reverie_dreams.world.gensokyo.GensokyoBoundary;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Natural spawning, spawners and spawn eggs all end up here.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelSpawnBoundaryMixin {
    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
    private void reverie_dreams$refuseMobOutsideMap(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (GensokyoBoundary.refuseMobSpawn((ServerLevel) (Object) this, entity)) {
            cir.setReturnValue(false);
        }
    }
}
