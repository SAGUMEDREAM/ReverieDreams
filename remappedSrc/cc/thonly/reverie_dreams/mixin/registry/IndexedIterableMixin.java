package cc.thonly.reverie_dreams.mixin.registry;

import net.minecraft.core.IdMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IdMap.class)
public interface IndexedIterableMixin<T> {
//    @Inject(method = "getOrThrow", at = @At("HEAD"))
//    private void onGetOrThrow(int index, CallbackInfoReturnable<T> cir) {
//        Object self = (Object) this;
//        System.out.println("[DEBUG] IndexedIterable.getOrThrow called with id=" + index
//                + " on " + self.getClass().getName());
//        System.out.println("[DEBUG] Registry Name: " + self);
//    }
//    @Inject(method = "getOrThrow", at = @At("TAIL"))
//    private void onGetOrThrowTail(int index, CallbackInfoReturnable<T> cir) {
//        if (cir.getReturnValue() != null) {
//            System.out.println("[DEBUG] Resolved id=" + index + " -> " + cir.getReturnValue());
//        }
//    }
}
