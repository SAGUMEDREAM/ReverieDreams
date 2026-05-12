package cc.thonly.reverie_dreams.registry;

import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;

public interface RegistryEntryOwnerBindable<T> {
    public void setOwner(RegistryImpl<T> register);

    public RegistryImpl<T> getOwner();

}
