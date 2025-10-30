package cc.thonly.reverie_dreams.mixin.registry;

import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;

@SuppressWarnings("unchecked")
@Mixin(RegistryAccess.ImmutableRegistryAccess.class)
public abstract class DynamicRegistryManagerMixin implements RegistryAccess {
    @Inject(method = "lookup", at = @At("HEAD"), cancellable = true)
    public <E> void lookup(ResourceKey<? extends Registry<? extends E>> registryRef, CallbackInfoReturnable<Optional<Registry<E>>> cir) {
        if (RegistryManager.ROOT.containsKey(registryRef)) {
            IntrinsicalRegister<?> register = RegistryManager.ROOT.get(registryRef);
            Registry<E> registry = (Registry<E>) register;
            cir.setReturnValue(Optional.ofNullable(registry));
        }
    }
}
