package cc.thonly.reverie_dreams.api.item.callback;

import cc.thonly.reverie_dreams.data.BeverageProperty;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

@FunctionalInterface
public interface BeveragePropertyItemUseCallback {
    Event<BeveragePropertyItemUseCallback> EVENT = EventFactory.of(
            (listeners) -> (world, user, property) -> {
                for (BeveragePropertyItemUseCallback callback : listeners) {
                    callback.onUse(world, user, property);
                }
            }
    );

    void onUse(ServerLevel world, LivingEntity user, BeverageProperty property);
}
