package cc.thonly.reverie_dreams.api.registry;

import cc.thonly.reverie_dreams.registry.impl.RawIdTypeRegistryImpl;

public interface RawIdTypeRegistry<T> {
    void register(T value);

    void register(String name, T value);

    static <T> RawIdTypeRegistry<T> create(String namespace) {
        return new RawIdTypeRegistryImpl<>(namespace);
    }
}
