package cc.thonly.reverie_dreams.mixin.compat;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(remap = false, value = PolymerCommonUtils.class)
public class PolymerCommonUtilsMixin {
//    @Inject(method = "isServerNetworkingThread", at = @At("HEAD"), cancellable = true)
//    private static void isServerNetworkingThread(CallbackInfoReturnable<Boolean> cir) {
//        cir.setReturnValue(true);
//    }
}
