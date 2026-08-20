package cc.thonly.reverie_dreams.registry;

import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;

public interface RegistryBootstrap<T> {
    void bootstrap(RegistryProvider<T> registry);
}
