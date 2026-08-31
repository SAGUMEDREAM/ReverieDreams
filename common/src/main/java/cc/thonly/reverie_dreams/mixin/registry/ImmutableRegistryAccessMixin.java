package cc.thonly.reverie_dreams.mixin.registry;

import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.PlaceholderLookupProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@SuppressWarnings("unchecked")
@Mixin({RegistryAccess.ImmutableRegistryAccess.class, PlaceholderLookupProvider.class})
public class ImmutableRegistryAccessMixin {
    @Inject(method = "lookup", at = @At("HEAD"), cancellable = true)
    public <E> void lookup(ResourceKey<? extends Registry<? extends E>> registryRef, CallbackInfoReturnable<Optional<Registry<E>>> cir) {
        if (BuiltInRegistryProviders.ROOT.containsKey(registryRef)) {
            RegistryProvider<?> register = BuiltInRegistryProviders.ROOT.get(registryRef);
            Registry<E> registry = (Registry<E>) register;
            cir.setReturnValue(Optional.ofNullable(registry));
        }
    }
}
