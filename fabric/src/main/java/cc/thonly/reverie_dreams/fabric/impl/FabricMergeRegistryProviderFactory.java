package cc.thonly.reverie_dreams.fabric.impl;

import cc.thonly.reverie_dreams.proxy.MergeRegistryProviderFactory;
import cc.thonly.reverie_dreams.registry.impl.MergeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked"})
public class FabricMergeRegistryProviderFactory implements MergeRegistryProviderFactory {
    @Override
    public MergeRegistry<?> apply(ResourceKey<? extends Registry<?>> key, List<Registry> registries) {
        return new MergeRegistry(key, registries) {

        };
    }
}
