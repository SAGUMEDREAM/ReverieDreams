package cc.thonly.reverie_dreams.registry.interfaces;

import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;

public interface OwnerBinding<T> {
    public void setOwner(RegistryImpl<T> register);
    public RegistryImpl<T> getOwner();

}
