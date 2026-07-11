package cc.thonly.reverie_dreams.api.registry.callback;

import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;

@FunctionalInterface
public interface RegistryImplReloadCallback {
    void onLoad(RegistryImpl<?> registry);

    Event<RegistryImplReloadCallback> EVENT = EventFactory.of(
            (listeners) -> (registry) -> {
                for (RegistryImplReloadCallback callback : listeners) {
                    callback.onLoad(registry);
                }
            }
    );
}
