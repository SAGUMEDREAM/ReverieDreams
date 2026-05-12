package cc.thonly.reverie_dreams.api.item.callback;

import cc.thonly.reverie_dreams.data.DrinkProperty;
import net.blay09.mods.balm.platform.event.Event;
import net.blay09.mods.balm.platform.event.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

@FunctionalInterface
public interface DrinkPropertyItemUseCallback {
    Event<DrinkPropertyItemUseCallback> EVENT = EventFactory.createArrayBacked(DrinkPropertyItemUseCallback.class,
            (listeners) -> (world, user, property) -> {
                for (DrinkPropertyItemUseCallback callback : listeners) {
                    callback.onUse(world, user, property);
                }
            }
    );

    void onUse(ServerLevel world, LivingEntity user, DrinkProperty property);
}
