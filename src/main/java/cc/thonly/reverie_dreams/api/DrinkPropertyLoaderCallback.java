package cc.thonly.reverie_dreams.api;

import cc.thonly.reverie_dreams.data.DrinkProperty;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

@FunctionalInterface
public interface DrinkPropertyLoaderCallback {
    Event<DrinkPropertyLoaderCallback> EVENT = EventFactory.createArrayBacked(DrinkPropertyLoaderCallback.class,
            (listeners) -> (world, user, property) -> {
                for (DrinkPropertyLoaderCallback callback : listeners) {
                    callback.onUse(world, user, property);
                }
            }
    );

    void onUse(ServerLevel world, LivingEntity user, DrinkProperty property);
}
