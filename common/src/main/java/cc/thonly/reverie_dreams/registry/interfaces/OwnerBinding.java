package cc.thonly.reverie_dreams.registry.interfaces;

import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;

public interface OwnerBinding<T> {
    public void setOwner(RegistryHandler<T> register);
    public RegistryHandler<T> getOwner();

}
