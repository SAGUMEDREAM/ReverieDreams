package cc.thonly.reverie_dreams.api.registry;

import cc.thonly.reverie_dreams.registry.BuiltInBiRegistryProviders;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.impl.BiRegistryProvider;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public interface RegistryImplContext {

    // 覆盖已存在的注册项（不会新增，只替换）
    <T> T set(RegistryProvider<T> registry, Identifier key, T value);

    // 向注册表注册一个新对象
    <T> T register(RegistryProvider<T> registry, Identifier key, T value);

    // 向双向注册表注册一个新对象
    <K, V> V register(BiRegistryProvider<K, V> registry, K key, V value);

    // 注册对象并标记为内置内容（通常不会被重载覆盖）
    <T> T registerForBuiltin(RegistryProvider<T> registry, Identifier key, T value);

    // 通过 Identifier 获取注册表实例
    <T> RegistryProvider<T> lookup(Identifier key);

    // 通过 ResourceKey 获取注册表实例
    <T> RegistryProvider<T> lookup(ResourceKey<? extends Registry<T>> key);

    // 通过 Identifier 获取双向注册表实例
    <K, V> BiRegistryProvider<K, V> biLookup(Identifier key, Class<K> kClass, Class<V> vClass);

    // 冻结注册表（停止触发构建逻辑，但不会阻止注册）
    <T> void freeze(RegistryProvider<T> registry);

    // 解冻注册表（恢复构建行为，需谨慎使用）
    <T> void unfreeze(RegistryProvider<T> registry);

    static RegistryImplContext getContext() {
        return Impl.IMPL_CONTEXT;
    }

    @SuppressWarnings("unchecked")
    class Impl implements RegistryImplContext {
        public static final Impl IMPL_CONTEXT = new Impl();

        @Override
        public <T> T set(RegistryProvider<T> registry, Identifier key, T value) {
            return BuiltInRegistryProviders.set(registry, key, value);
        }

        @Override
        public <T> T register(RegistryProvider<T> registry, Identifier key, T value) {
            return BuiltInRegistryProviders.register(registry, key, value);
        }

        @Override
        public <K, V> V register(BiRegistryProvider<K, V> registry, K key, V value) {
            return BuiltInBiRegistryProviders.register(registry, key, value);
        }

        @Override
        public <T> T registerForBuiltin(RegistryProvider<T> registry, Identifier key, T value) {
            return BuiltInRegistryProviders.registerForBuiltin(registry, key, value);
        }

        @Override
        public <T> RegistryProvider<T> lookup(Identifier key) {
            return (RegistryProvider<T>) BuiltInRegistryProviders.ROOT.get(ResourceKey.createRegistryKey(key));
        }

        @Override
        public <T> RegistryProvider<T> lookup(ResourceKey<? extends Registry<T>> key) {
            return (RegistryProvider<T>) BuiltInRegistryProviders.ROOT.get(key);
        }

        @Override
        public <K, V> BiRegistryProvider<K, V> biLookup(Identifier key, Class<K> kClass, Class<V> vClass) {
            return (BiRegistryProvider<K, V>) BuiltInBiRegistryProviders.ROOT.get(key);
        }

        @Override
        public <T> void freeze(RegistryProvider<T> registry) {
            registry.freeze();
        }

        @Override
        public <T> void unfreeze(RegistryProvider<T> registry) {
            registry.unfreeze();
        }
    }
}
