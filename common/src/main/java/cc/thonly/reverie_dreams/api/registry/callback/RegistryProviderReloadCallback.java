package cc.thonly.reverie_dreams.api.registry.callback;

import cc.thonly.reverie_dreams.api.registry.RegistryProviderLookup;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;

@FunctionalInterface
public interface RegistryProviderReloadCallback {
    void onLoad(RegistryProviderLookup<?> registry);

    Event<RegistryProviderReloadCallback> EVENT = EventFactory.of(
            (listeners) -> (registry) -> {
                for (RegistryProviderReloadCallback callback : listeners) {
                    callback.onLoad(registry);
                }
            }
    );
}
