package cc.thonly.reverie_dreams.registry;

import net.minecraft.resource.ResourceManager;

public interface ReloadStep<T> {
    void reload(ResourceManager manager);
}
