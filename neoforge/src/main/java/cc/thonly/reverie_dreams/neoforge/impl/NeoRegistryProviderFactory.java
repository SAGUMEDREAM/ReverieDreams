package cc.thonly.reverie_dreams.neoforge.impl;

import cc.thonly.reverie_dreams.proxy.RegistryProviderFactory;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

@SuppressWarnings({"rawtypes", "unchecked"})
public class NeoRegistryProviderFactory implements RegistryProviderFactory {
    @Override
    public RegistryProvider<?> apply(ResourceKey<? extends Registry<?>> key) {
        return new NeoRegistryProvider(key);
    }
}
