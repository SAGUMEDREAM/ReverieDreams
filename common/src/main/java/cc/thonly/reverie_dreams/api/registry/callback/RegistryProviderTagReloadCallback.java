package cc.thonly.reverie_dreams.api.registry.callback;

import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import net.blay09.mods.balm.platform.event.Event;
import net.blay09.mods.balm.platform.event.EventFactory;
import net.minecraft.core.HolderSet;

@SuppressWarnings({"rawtypes", "unchecked"})
@FunctionalInterface
public interface RegistryProviderTagReloadCallback<T> {
    void onLoad(RegistryProvider<T> registry, HolderSet.Named<T> holders);

    Event<RegistryProviderTagReloadCallback<?>> EVENT = EventFactory.createArrayBacked(
            RegistryProviderTagReloadCallback.class,
            (listeners) -> (registry, holders) -> {
                for (RegistryProviderTagReloadCallback<?> callback : listeners) {
                    callback.onLoad((RegistryProvider) registry, (HolderSet.Named) holders);
                }
            }
    );
}
