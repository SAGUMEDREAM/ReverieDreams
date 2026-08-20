package cc.thonly.reverie_dreams.neoforge.impl;

import cc.thonly.reverie_dreams.proxy.MergeRegistryProviderFactory;
import cc.thonly.reverie_dreams.registry.impl.MergeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked"})
public class NeoMergeRegistryProviderFactory implements MergeRegistryProviderFactory {
    @Override
    public MergeRegistry<?> apply(ResourceKey<? extends Registry<?>> key, List<Registry> registries) {
        return new NeoMergeRegistry(key, registries);
    }
}
