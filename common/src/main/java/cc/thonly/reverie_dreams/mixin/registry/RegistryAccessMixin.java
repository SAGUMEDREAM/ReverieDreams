package cc.thonly.reverie_dreams.mixin.registry;

import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.stream.Stream;

@Mixin(RegistryAccess.class)
public interface RegistryAccessMixin {
    @Inject(method = "fromRegistryOfRegistries", at = @At("RETURN"), cancellable = true)
    private static void reverie_dreams$fromRegistryOfRegistries(Registry<? extends Registry<?>> registries, CallbackInfoReturnable<RegistryAccess.Frozen> cir) {
        RegistryAccess.Frozen frozen = cir.getReturnValue();
        RegistryAccess.Frozen delegate = new RegistryAccess.Frozen() {
            @SuppressWarnings("unchecked")
            @Override
            public <E> Optional<Registry<E>> lookup(ResourceKey<? extends Registry<? extends E>> registryKey) {
                Optional<Registry<E>> lookup = frozen.lookup(registryKey);
                if (lookup.isPresent()) {
                    return lookup;
                } else {
                    RegistryProvider<?> register = BuiltInRegistryProviders.ROOT.get(registryKey);
                    Registry<E> registry = (Registry<E>) register;
                    return Optional.ofNullable(registry);
                }
            }

            @Override
            public Stream<RegistryEntry<?>> registries() {
                return frozen.registries();
            }
        };
        cir.setReturnValue(delegate);
    }
}
