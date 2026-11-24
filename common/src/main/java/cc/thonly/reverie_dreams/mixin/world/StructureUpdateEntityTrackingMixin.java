package cc.thonly.reverie_dreams.mixin.world;

import cc.thonly.reverie_dreams.world.gensokyo.update.StructureUpdateService;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Covers disk-loaded entities as well as new addFreshEntity calls.
 */
@Mixin(targets = "net.minecraft.server.level.ServerLevel$EntityCallbacks")
public abstract class StructureUpdateEntityTrackingMixin {
    @Inject(method = "onTrackingStart(Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
    private void reverie$restoreLoadedActorFlags(Entity entity, CallbackInfo ci) {
        StructureUpdateService.loaded(entity);
    }
}
