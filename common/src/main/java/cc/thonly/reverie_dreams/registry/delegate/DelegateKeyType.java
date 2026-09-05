package cc.thonly.reverie_dreams.registry.delegate;

import net.minecraft.resources.Identifier;

public interface DelegateKeyType {
    void bindKey(Identifier key);

    Identifier getRegistryId();
}
