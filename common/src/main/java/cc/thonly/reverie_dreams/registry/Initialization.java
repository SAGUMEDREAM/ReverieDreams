package cc.thonly.reverie_dreams.registry;

import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;

public interface Initialization<T> {
    void bootstrap(RegistryImpl<T> registry);
}
