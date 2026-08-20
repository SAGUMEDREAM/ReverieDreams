package cc.thonly.reverie_dreams.api.registry.callback;

import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;

@FunctionalInterface
public interface RegistryProviderReloadCallback {
    void onLoad(RegistryProvider<?> registry);

    Event<RegistryProviderReloadCallback> EVENT = EventFactory.of(
            (listeners) -> (registry) -> {
                for (RegistryProviderReloadCallback callback : listeners) {
                    callback.onLoad(registry);
                }
            }
    );
}
