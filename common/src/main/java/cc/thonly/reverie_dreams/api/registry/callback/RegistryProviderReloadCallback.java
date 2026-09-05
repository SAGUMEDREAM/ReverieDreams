package cc.thonly.reverie_dreams.api.registry.callback;

import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import net.blay09.mods.balm.platform.event.Event;
import net.blay09.mods.balm.platform.event.EventFactory;

@FunctionalInterface
public interface RegistryProviderReloadCallback {
    void onLoad(RegistryProvider<?> registry);

    Event<RegistryProviderReloadCallback> EVENT = EventFactory.createArrayBacked(
            RegistryProviderReloadCallback.class,
            (listeners) -> (registry) -> {
                for (RegistryProviderReloadCallback callback : listeners) {
                    callback.onLoad(registry);
                }
            }
    );
}
