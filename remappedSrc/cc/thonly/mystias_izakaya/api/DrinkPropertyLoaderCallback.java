package cc.thonly.mystias_izakaya.api;

import cc.thonly.mystias_izakaya.component.DrinkProperty;
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
