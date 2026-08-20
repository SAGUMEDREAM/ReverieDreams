package cc.thonly.reverie_dreams.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public interface RegistryExtension<T> {
    void addAliaName(Identifier from, Identifier to);

    Identifier resolveId(Identifier name);

    ResourceKey<T> resolveKey(ResourceKey<T> key);

    Registry<T> unfreeze();

    void unboundTag();
}
