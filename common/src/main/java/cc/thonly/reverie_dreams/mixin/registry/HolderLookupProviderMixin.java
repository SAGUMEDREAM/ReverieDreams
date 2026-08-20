package cc.thonly.reverie_dreams.mixin.registry;

import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings({"unchecked", "rawtypes"})
@Mixin(HolderLookup.Provider.class)
public interface HolderLookupProviderMixin {
    @Inject(method = "create", at = @At("RETURN"), cancellable = true)
    private static void reverie_dreams$create(Stream<HolderLookup.RegistryLookup<?>> lookups, CallbackInfoReturnable<HolderLookup.Provider> cir) {
        HolderLookup.Provider provider = cir.getReturnValue();
        HolderLookup.Provider delegate = new HolderLookup.Provider() {
            @Override
            public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
                return provider.listRegistryKeys();
            }

            @Override
            public <T> Optional<? extends HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> key) {
                Optional<? extends HolderLookup.RegistryLookup<T>> lookup = provider.lookup(key);
                if (lookup.isPresent()) {
                    return lookup;
                } else {
                    RegistryProvider<?> registryProvider = BuiltInRegistryProviders.ROOT.get(key);
                    return (Optional) Optional.ofNullable(registryProvider);
                }
            }
        };
        cir.setReturnValue(delegate);
    }
}
