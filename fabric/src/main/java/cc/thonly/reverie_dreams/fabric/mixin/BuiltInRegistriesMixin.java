package cc.thonly.reverie_dreams.fabric.mixin;

import cc.thonly.reverie_dreams.fabric.ReverieDreamsFabric;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltInRegistries.class)
public class BuiltInRegistriesMixin {
    @Inject(method = "freeze", at = @At("HEAD"))
    private static void reverie_dreams$registerAllEvent(CallbackInfo ci) {
        ReverieDreamsFabric.finishRegister();
    }
}
