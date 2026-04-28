package cc.thonly.reverie_dreams.api.registry;

import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import net.blay09.mods.balm.platform.event.Event;
import net.blay09.mods.balm.platform.event.EventFactory;

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
