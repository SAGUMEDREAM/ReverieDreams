package cc.thonly.reverie_dreams.mixin.registry;

import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@SuppressWarnings("unchecked")
@Mixin(RegistryAccess.ImmutableRegistryAccess.class)
public abstract class DynamicRegistryManagerMixin implements RegistryAccess {
    @Inject(method = "lookup", at = @At("HEAD"), cancellable = true)
    public <E> void lookup(ResourceKey<? extends Registry<? extends E>> registryRef, CallbackInfoReturnable<Optional<Registry<E>>> cir) {
        if (RegistryHandlers.ROOT.containsKey(registryRef)) {
            RegistryHandler<?> register = RegistryHandlers.ROOT.get(registryRef);
            Registry<E> registry = (Registry<E>) register;
            cir.setReturnValue(Optional.ofNullable(registry));
        }
    }
}
