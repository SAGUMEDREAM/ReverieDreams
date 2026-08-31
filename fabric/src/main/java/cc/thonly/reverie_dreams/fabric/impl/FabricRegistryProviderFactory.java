package cc.thonly.reverie_dreams.fabric.impl;

import cc.thonly.reverie_dreams.proxy.RegistryProviderFactory;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

@SuppressWarnings({"rawtypes", "unchecked"})
public class FabricRegistryProviderFactory implements RegistryProviderFactory {
    @Override
    public RegistryProvider<?> apply(ResourceKey<? extends Registry<?>> key) {
        return new FabricRegistryProvider(key);
    }
}
