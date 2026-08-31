package cc.thonly.reverie_dreams.fabric.impl;

import cc.thonly.reverie_dreams.proxy.RegistryProviderShadowerFactory;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

@SuppressWarnings({"rawtypes", "unchecked"})
public class FabricRegistryProviderShadowerFactory implements RegistryProviderShadowerFactory {
    @Override
    public RegistryProvider<?> apply(ResourceKey<? extends Registry<?>> key, RegistryProvider<?> handler) {
        return new FabricRegistryProvider(key, handler);
    }
}
