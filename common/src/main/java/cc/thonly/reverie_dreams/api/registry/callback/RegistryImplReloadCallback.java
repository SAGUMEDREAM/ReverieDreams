package cc.thonly.reverie_dreams.api.registry.callback;

import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import net.blay09.mods.balm.platform.event.Event;
import net.blay09.mods.balm.platform.event.EventFactory;

@FunctionalInterface
public interface RegistryImplReloadCallback {
    void onLoad(RegistryImpl<?> registry);

    Event<RegistryImplReloadCallback> EVENT = EventFactory.createArrayBacked(
            RegistryImplReloadCallback.class,
            (listeners) -> (registry) -> {
                for (RegistryImplReloadCallback callback : listeners) {
                    callback.onLoad(registry);
                }
            }
    );
}
