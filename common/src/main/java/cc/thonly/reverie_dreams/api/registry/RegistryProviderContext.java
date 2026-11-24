package cc.thonly.reverie_dreams.api.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public interface RegistryProviderContext {

    // 覆盖已存在的注册项（不会新增，只替换）
    <T> T set(RegistryProviderLookup<T> registry, Identifier key, T value);

    // 向注册表注册一个新对象
    <T> T register(RegistryProviderLookup<T> registry, Identifier key, T value);

    // 向双向注册表注册一个新对象
    <K, V> V register(cc.thonly.reverie_dreams.registry.impl.BiRegistryProvider<K, V> registry, K key, V value);

    // 注册对象并标记为内置内容（通常不会被重载覆盖）
    <T> T registerForBuiltin(RegistryProviderLookup<T> registry, Identifier key, T value);

    // 通过 Identifier 获取注册表实例
    <T> RegistryProviderLookup<T> lookup(Identifier key);

    // 通过 ResourceKey 获取注册表实例
    <T> RegistryProviderLookup<T> lookup(ResourceKey<? extends Registry<T>> key);

    // 通过 Identifier 获取双向注册表实例
    <K, V> BiRegistryProvider<K, V> biLookup(Identifier key, Class<K> kClass, Class<V> vClass);

    // 冻结注册表（停止触发构建逻辑，但不会阻止注册）
    <T> void freeze(RegistryProviderLookup<T> registry);

    // 解冻注册表（恢复构建行为，需谨慎使用）
    <T> void unfreeze(RegistryProviderLookup<T> registry);

    static RegistryProviderContext getContext() {
        RegistryProviderContext context = Holder.context;
        if (context == null) {
            throw new IllegalStateException("Registry implementation context is not registered");
        }
        return context;
    }

    static void setContext(RegistryProviderContext context) {
        Holder.context = context;
    }

    final class Holder {
        private static volatile RegistryProviderContext context;
    }
}
