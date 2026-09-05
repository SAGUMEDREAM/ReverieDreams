package cc.thonly.reverie_dreams.api.polymer.callback;

import net.blay09.mods.balm.platform.event.Event;
import net.blay09.mods.balm.platform.event.EventFactory;
import net.minecraft.world.entity.Entity;

@FunctionalInterface
public interface PolymerEntityGetterCallback {
    Event<PolymerEntityGetterCallback> EVENT = EventFactory.createArrayBacked(
            PolymerEntityGetterCallback.class,
            (listeners) -> (entity) -> {
                for (PolymerEntityGetterCallback callback : listeners) {
                    Object obj = callback.handle(entity);
                    if (obj == null) {
                        continue;
                    }
                    return obj;
                }
                return null;
            }
    );

    Object handle(Entity entity);

    static Object get(Entity entity) {
        return getPolymerEntity(entity);
    }

    static Object getPolymerEntity(Entity entity) {
        return EVENT.invoker().handle(entity);
    }
}
