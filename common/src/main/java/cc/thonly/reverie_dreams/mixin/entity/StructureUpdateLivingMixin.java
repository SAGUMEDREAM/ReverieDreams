package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.world.gensokyo.update.StructureUpdateGate;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class StructureUpdateLivingMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void reverie$pauseOwnedActorPhysics(CallbackInfo ci) {
        if (StructureUpdateGate.freezes((Entity) (Object) this))
            ci.cancel();
    }
}
