package cc.thonly.reverie_dreams.api.registry;

import cc.thonly.reverie_dreams.registry.BiRegistryImpls;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.impl.BiRegistryImpl;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public interface RegistryImplContext {

    // 覆盖已存在的注册项（不会新增，只替换）
    <T> T set(RegistryImpl<T> registry, Identifier key, T value);

    // 向注册表注册一个新对象
    <T> T register(RegistryImpl<T> registry, Identifier key, T value);

    // 向双向注册表注册一个新对象
    <K, V> V register(BiRegistryImpl<K, V> registry, K key, V value);

    // 注册对象并标记为内置内容（通常不会被重载覆盖）
    <T> T registerForBuiltin(RegistryImpl<T> registry, Identifier key, T value);

    // 通过 Identifier 获取注册表实例
    <T> RegistryImpl<T> lookup(Identifier key);

    // 通过 ResourceKey 获取注册表实例
    <T> RegistryImpl<T> lookup(ResourceKey<? extends Registry<T>> key);

    // 通过 Identifier 获取双向注册表实例
    <K, V> BiRegistryImpl<K, V> biLookup(Identifier key, Class<K> kClass, Class<V> vClass);

    // 冻结注册表（停止触发构建逻辑，但不会阻止注册）
    <T> void freeze(RegistryImpl<T> registry);

    // 解冻注册表（恢复构建行为，需谨慎使用）
    <T> void unfreeze(RegistryImpl<T> registry);

    static RegistryImplContext getContext() {
        return Impl.IMPL_CONTEXT;
    }

    @SuppressWarnings("unchecked")
    class Impl implements RegistryImplContext {
        public static final Impl IMPL_CONTEXT = new Impl();

        @Override
        public <T> T set(RegistryImpl<T> registry, Identifier key, T value) {
            return RegistryImpls.set(registry, key, value);
        }

        @Override
        public <T> T register(RegistryImpl<T> registry, Identifier key, T value) {
            return RegistryImpls.register(registry, key, value);
        }

        @Override
        public <K, V> V register(BiRegistryImpl<K, V> registry, K key, V value) {
            return BiRegistryImpls.register(registry, key, value);
        }

        @Override
        public <T> T registerForBuiltin(RegistryImpl<T> registry, Identifier key, T value) {
            return RegistryImpls.registerForBuiltin(registry, key, value);
        }

        @Override
        public <T> RegistryImpl<T> lookup(Identifier key) {
            return (RegistryImpl<T>) RegistryImpls.ROOT.get(ResourceKey.createRegistryKey(key));
        }

        @Override
        public <T> RegistryImpl<T> lookup(ResourceKey<? extends Registry<T>> key) {
            return (RegistryImpl<T>) RegistryImpls.ROOT.get(key);
        }

        @Override
        public <K, V> BiRegistryImpl<K, V> biLookup(Identifier key, Class<K> kClass, Class<V> vClass) {
            return (BiRegistryImpl<K, V>) BiRegistryImpls.ROOT.get(key);
        }

        @Override
        public <T> void freeze(RegistryImpl<T> registry) {
            registry.freeze();
        }

        @Override
        public <T> void unfreeze(RegistryImpl<T> registry) {
            registry.unfreeze();
        }
    }
}
