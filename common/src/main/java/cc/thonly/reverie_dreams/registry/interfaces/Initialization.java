package cc.thonly.reverie_dreams.registry.interfaces;

import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;

public interface Initialization<T> {
    void bootstrap(RegistryHandler<T> registry);
}
