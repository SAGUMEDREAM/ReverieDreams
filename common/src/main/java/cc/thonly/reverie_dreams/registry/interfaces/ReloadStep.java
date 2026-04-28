package cc.thonly.reverie_dreams.registry.interfaces;

import net.minecraft.server.packs.resources.ResourceManager;

public interface ReloadStep<T> {
    void reload(ResourceManager manager);
}
