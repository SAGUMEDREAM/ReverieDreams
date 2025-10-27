package cc.thonly.reverie_dreams.mixin.registry;

import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@SuppressWarnings("unchecked")
@Mixin(DynamicRegistryManager.ImmutableImpl.class)
public abstract class DynamicRegistryManagerMixin implements DynamicRegistryManager {
    @Inject(method = "getOptional", at = @At("HEAD"), cancellable = true)
    public <E> void getOptional(RegistryKey<? extends Registry<? extends E>> registryRef, CallbackInfoReturnable<Optional<Registry<E>>> cir) {
        if (RegistryManager.ROOT.containsKey(registryRef)) {
            IntrinsicalRegister<?> register = RegistryManager.ROOT.get(registryRef);
            Registry<E> registry = (Registry<E>) register;
            cir.setReturnValue(Optional.ofNullable(registry));
        }
    }
}
