package cc.thonly.reverie_dreams.api.item.callback;

import cc.thonly.reverie_dreams.data.FoodProperty;
import dev.architectury.event.Event;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface FoodPropertyItemUseCallback {
    /**
     * @deprecated Use {@link ItemEvent#FOOD_PROPERTY_USE}.
     */
    @Deprecated
    Event<FoodPropertyItemUseCallback> EVENT = ItemEvent.FOOD_PROPERTY_USE;

    void onUse(ServerLevel world, LivingEntity user, ItemStack itemStack, FoodProperty property);
}
