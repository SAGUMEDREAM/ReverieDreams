package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.world.gensokyo.update.StructureUpdateService;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class StructureUpdateEntityMixin {
    @Inject(method = "remove", at = @At("HEAD"))
    private void reverie$recordActorState(Entity.RemovalReason reason, CallbackInfo ci) {
        StructureUpdateService.removed((Entity) (Object) this, reason);
    }
}
