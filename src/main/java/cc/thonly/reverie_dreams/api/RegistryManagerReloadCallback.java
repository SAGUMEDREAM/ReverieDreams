package cc.thonly.reverie_dreams.api;

import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@FunctionalInterface
public interface RegistryManagerReloadCallback {
    void onLoad(RegistryHandler<?> registry);
    Event<RegistryManagerReloadCallback> EVENT = EventFactory.createArrayBacked(
            RegistryManagerReloadCallback.class,
            (listeners)-> (registry) -> {
                for (RegistryManagerReloadCallback callback : listeners) {
                    callback.onLoad(registry);
                }
            }
    );
}
