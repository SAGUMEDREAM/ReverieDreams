package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.world.gensokyo.update.StructureUpdateGate;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class StructureUpdateMobMixin {
    @Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true)
    private void reverie$pauseOwnedStructureActor(CallbackInfo ci) {
        if (StructureUpdateGate.freezes((Entity) (Object) this))
            ci.cancel();
    }
}
