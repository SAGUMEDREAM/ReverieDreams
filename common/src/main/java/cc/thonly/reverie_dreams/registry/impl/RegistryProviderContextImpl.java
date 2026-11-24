package cc.thonly.reverie_dreams.registry.impl;

import cc.thonly.reverie_dreams.api.registry.RegistryProviderContext;
import cc.thonly.reverie_dreams.api.registry.RegistryProviderLookup;
import cc.thonly.reverie_dreams.registry.BuiltInBiRegistryProviders;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

@SuppressWarnings("unchecked")
public class RegistryProviderContextImpl implements RegistryProviderContext {

    @Override
    public <T> T set(RegistryProviderLookup<T> registry, Identifier key, T value) {
        return BuiltInRegistryProviders.set((cc.thonly.reverie_dreams.registry.impl.RegistryProvider<T>) registry, key, value);
    }

    @Override
    public <T> T register(RegistryProviderLookup<T> registry, Identifier key, T value) {
        return BuiltInRegistryProviders.register((cc.thonly.reverie_dreams.registry.impl.RegistryProvider<T>) registry, key, value);
    }

    @Override
    public <K, V> V register(BiRegistryProvider<K, V> registry, K key, V value) {
        return BuiltInBiRegistryProviders.register(registry, key, value);
    }

    @Override
    public <T> T registerForBuiltin(RegistryProviderLookup<T> registry, Identifier key, T value) {
        return BuiltInRegistryProviders.registerForBuiltin((cc.thonly.reverie_dreams.registry.impl.RegistryProvider<T>) registry, key, value);
    }

    @Override
    public <T> RegistryProviderLookup<T> lookup(Identifier key) {
        return (RegistryProviderLookup<T>) BuiltInRegistryProviders.ROOT.get(ResourceKey.createRegistryKey(key));
    }

    @Override
    public <T> RegistryProviderLookup<T> lookup(ResourceKey<? extends Registry<T>> key) {
        return (RegistryProviderLookup<T>) BuiltInRegistryProviders.ROOT.get(key);
    }

    @Override
    public <K, V> cc.thonly.reverie_dreams.api.registry.BiRegistryProvider<K, V> biLookup(Identifier key, Class<K> kClass, Class<V> vClass) {
        return (cc.thonly.reverie_dreams.api.registry.BiRegistryProvider<K, V>) BuiltInBiRegistryProviders.ROOT.get(key);
    }

    @Override
    public <T> void freeze(RegistryProviderLookup<T> registry) {
        registry.freeze();
    }

    @Override
    public <T> void unfreeze(RegistryProviderLookup<T> registry) {
        registry.unfreeze();
    }
}
