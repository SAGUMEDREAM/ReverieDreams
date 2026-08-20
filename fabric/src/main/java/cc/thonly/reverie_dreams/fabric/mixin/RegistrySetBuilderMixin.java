package cc.thonly.reverie_dreams.fabric.mixin;


import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@SuppressWarnings({"unchecked", "rawtypes", "OptionalOfNullableMisuse"})
@Slf4j
@Pseudo
@Mixin(targets = "net.minecraft.core.RegistrySetBuilder$3")
public class RegistrySetBuilderMixin {
    @Inject(method = "lookup", at = @At("RETURN"), cancellable = true)
    private <T> void reverie_dreams$lookup(ResourceKey<? extends Registry<? extends T>> key, CallbackInfoReturnable<Optional<HolderLookup.RegistryLookup<T>>> cir) {
        if (!BuiltInRegistryProviders.LOOKUP) {
            return;
        }
        this.reverie_dreams$RedirectLookup(key, cir);
    }

    @Unique
    private <T> void reverie_dreams$RedirectLookup(ResourceKey<? extends Registry<? extends T>> key, CallbackInfoReturnable<Optional<HolderLookup.RegistryLookup<T>>> cir) {
        try {
            Optional<HolderLookup.RegistryLookup<T>> lookup = cir.getReturnValue();
            if (lookup.isPresent()) {
                return;
            }
            RegistryProvider<?> registryProvider = BuiltInRegistryProviders.ROOT.get(key);
            if (registryProvider == null) {
                return;
            }
            RegistryProvider<T> cast = (RegistryProvider) registryProvider;
            System.out.println(cast);
            Optional<HolderLookup.RegistryLookup<T>> value = Optional.ofNullable(cast);
            cir.setReturnValue(value);
        } catch (Exception e) {
            log.error("Error: ", e);
        }
    }
}