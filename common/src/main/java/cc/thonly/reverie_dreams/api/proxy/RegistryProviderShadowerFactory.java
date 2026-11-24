package cc.thonly.reverie_dreams.api.proxy;

import cc.thonly.reverie_dreams.api.registry.RegistryProviderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.function.BiFunction;

public interface RegistryProviderShadowerFactory extends BiFunction<ResourceKey<? extends Registry<?>>, RegistryProviderLookup<?>, RegistryProviderLookup<?>> {

}