package cc.thonly.reverie_dreams.api.registry.callback;

import cc.thonly.reverie_dreams.api.registry.RegistryProviderLookup;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.core.HolderSet;

@SuppressWarnings({"rawtypes", "unchecked"})
@FunctionalInterface
public interface RegistryProviderTagReloadCallback<T> {
    void onLoad(RegistryProviderLookup<T> registry, HolderSet.Named<T> holders);

    Event<RegistryProviderTagReloadCallback<?>> EVENT = EventFactory.of(
            (listeners) -> (registry, holders) -> {
                for (RegistryProviderTagReloadCallback<?> callback : listeners) {
                    callback.onLoad((RegistryProviderLookup) registry, (HolderSet.Named) holders);
                }
            }
    );
}
