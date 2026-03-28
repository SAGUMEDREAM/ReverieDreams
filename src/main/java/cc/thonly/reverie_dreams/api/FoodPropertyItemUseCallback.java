package cc.thonly.reverie_dreams.api;

import cc.thonly.reverie_dreams.data.FoodProperty;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

@FunctionalInterface
public interface FoodPropertyItemUseCallback {
    Event<FoodPropertyItemUseCallback> EVENT = EventFactory.createArrayBacked(FoodPropertyItemUseCallback.class,
            (listeners) -> (world, user, property) -> {
                for (FoodPropertyItemUseCallback callback : listeners) {
                    callback.onUse(world, user, property);
                }
            }
    );

    void onUse(ServerLevel world, LivingEntity user, FoodProperty property);
}
