package cc.thonly.reverie_dreams.api.item.callback;

import cc.thonly.reverie_dreams.data.FoodProperty;
import net.blay09.mods.balm.platform.event.Event;
import net.blay09.mods.balm.platform.event.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface FoodPropertyItemUseCallback {
    Event<FoodPropertyItemUseCallback> EVENT = EventFactory.createArrayBacked(
            FoodPropertyItemUseCallback.class,
            (listeners) -> (world, user, itemStack, property) -> {
                for (FoodPropertyItemUseCallback callback : listeners) {
                    callback.onUse(world, user, itemStack, property);
                }
            }
    );

    void onUse(ServerLevel world, LivingEntity user, ItemStack itemStack, FoodProperty property);
}
