package cc.thonly.reverie_dreams.neoforge.impl;

import cc.thonly.reverie_dreams.proxy.MergeRegistryProviderFactory;
import cc.thonly.reverie_dreams.proxy.RegistryProviderShadowerFactory;
import cc.thonly.reverie_dreams.registry.impl.MergeRegistry;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked"})
public class NeoRegistryProviderShadowerFactory implements RegistryProviderShadowerFactory {
    @Override
    public RegistryProvider<?> apply(ResourceKey<? extends Registry<?>> key, RegistryProvider<?> handler) {
        return new NeoRegistryProvider(key, handler);
    }
}
