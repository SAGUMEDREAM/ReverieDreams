package cc.thonly.reverie_dreams.neoforge.mixin;

import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommonHooks.class)
public class CommonHooksMixin {
    @Inject(method = "validateComponent", at = @At("HEAD"), cancellable = true)
    private static void bypassValidateComponent(Object dataComponent, CallbackInfo ci) {
        ci.cancel();
    }
}
