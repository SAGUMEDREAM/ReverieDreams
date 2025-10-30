package cc.thonly.reverie_dreams.api;

import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@FunctionalInterface
public interface RegistryManagerReloadCallback {
    void onLoad(IntrinsicalRegister<?> registry);
    Event<RegistryManagerReloadCallback> EVENT = EventFactory.createArrayBacked(
            RegistryManagerReloadCallback.class,
            (listeners)-> (registry) -> {
                for (RegistryManagerReloadCallback callback : listeners) {
                    callback.onLoad(registry);
                }
            }
    );
}
