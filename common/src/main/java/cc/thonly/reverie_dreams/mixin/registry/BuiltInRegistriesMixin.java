package cc.thonly.reverie_dreams.mixin.registry;

import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltInRegistries.class)
public class BuiltInRegistriesMixin {
    @Inject(method = "bindBootstrappedTagsToEmpty", at = @At("HEAD"), cancellable = true)
    private static void reverie_dreams$ensureClassType(Registry<?> registry, CallbackInfo ci){
        if (registry instanceof RegistryProvider<?> provider) {
            ci.cancel();
        }
    }
}
