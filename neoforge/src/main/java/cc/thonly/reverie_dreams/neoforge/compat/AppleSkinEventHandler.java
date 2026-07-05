package cc.thonly.reverie_dreams.neoforge.compat;

import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import squeek.appleskin.api.event.FoodValuesEvent;

import java.util.List;

public class AppleSkinEventHandler {
    @SubscribeEvent
    public void onPreTooltipEvent(FoodValuesEvent foodValuesEvent) {
        ItemStack itemStack = foodValuesEvent.itemStack;
        if (itemStack.has(DataComponents.FOOD) && itemStack.has(RDDataComponents.FOOD_PROPERTIES.value()) && itemStack.has(RDDataComponents.FOOD_ITEM_TYPE.value())) {
            FoodProperties foodProps = itemStack.get(DataComponents.FOOD);
            if (foodProps == null) {
                return;
            }
            List<FoodProperty> foodProperties = itemStack.get(RDDataComponents.FOOD_PROPERTIES.value());
            if (foodProperties == null || foodProperties.isEmpty()) {
                return;
            }
            int size = foodProperties.size();
            foodValuesEvent.defaultFoodProperties =  new FoodProperties(foodProps.nutrition() + size, foodProps.saturation() + size * 1.5f, foodProps.canAlwaysEat());
            foodValuesEvent.modifiedFoodProperties =  new FoodProperties(foodProps.nutrition() + size, foodProps.saturation() + size * 1.5f, foodProps.canAlwaysEat());
        }
    }
}
