package cc.thonly.reverie_dreams.mixin.world;

import cc.thonly.reverie_dreams.world.gensokyo.update.StructureUpdateService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class StructureUpdateEntityLoadMixin {
    @Inject(method = "addEntity", at = @At("HEAD"))
    private void reverie$restorePendingActorFlags(Entity entity, CallbackInfoReturnable<Boolean> ci) {
        StructureUpdateService.loaded(entity);
    }
}
