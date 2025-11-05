package cc.thonly.reverie_dreams.api;

import cc.thonly.reverie_dreams.data.FoodProperty;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

@FunctionalInterface
public interface FoodPropertyLoaderCallback {
    Event<FoodPropertyLoaderCallback> EVENT = EventFactory.createArrayBacked(FoodPropertyLoaderCallback.class,
            (listeners) -> (world, user, property) -> {
                for (FoodPropertyLoaderCallback callback : listeners) {
                    callback.onUse(world, user, property);
                }
            }
    );

    void onUse(ServerLevel world, LivingEntity user, FoodProperty property);
}
