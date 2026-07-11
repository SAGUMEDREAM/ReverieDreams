package cc.thonly.reverie_dreams.api.item.callback;

import cc.thonly.reverie_dreams.data.DrinkProperty;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

@FunctionalInterface
public interface DrinkPropertyItemUseCallback {
    Event<DrinkPropertyItemUseCallback> EVENT = EventFactory.of(
            (listeners) -> (world, user, property) -> {
                for (DrinkPropertyItemUseCallback callback : listeners) {
                    callback.onUse(world, user, property);
                }
            }
    );

    void onUse(ServerLevel world, LivingEntity user, DrinkProperty property);
}
