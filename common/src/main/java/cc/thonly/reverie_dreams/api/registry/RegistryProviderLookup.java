package cc.thonly.reverie_dreams.api.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface RegistryProviderLookup<T> {
    ResourceKey<? extends Registry<T>> getRegistryKey();

    ResourceKey<? extends Registry<T>> key();

    Identifier getKey(T value);

    Optional<Holder.Reference<T>> get(Identifier id);

    /**
     * Looks up the requested ID (including registry aliases) without selecting a default.
     */
    default Optional<T> findValue(@Nullable Identifier id) {
        return id == null ? Optional.empty() : this.get(id).map(Holder.Reference::value);
    }

    ResourceKey<T> createKey(Identifier id);

    /**
     * Lifecycle operation. Prefer {@link RegistryProviderContext#freeze(RegistryProviderLookup)} in plugin code.
     */
    Registry<T> freeze();

    /**
     * Lifecycle operation. Prefer {@link RegistryProviderContext#unfreeze(RegistryProviderLookup)} in plugin code.
     */
    Registry<T> unfreeze();
}
