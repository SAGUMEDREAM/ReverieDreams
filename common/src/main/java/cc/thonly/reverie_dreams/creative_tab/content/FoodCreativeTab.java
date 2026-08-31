package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDCuisineItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Collection;

public class FoodCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("08_food_item_group"));

    public static void bootstrap() {
        ItemGroupContentHelper.registerGroup(FoodCreativeTab.ITEM_GROUP_KEY, builder -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
                .icon(FoodCreativeTab::getFoodItemIcon)
                .title(Component.translatable("item_group.food_item_group"))
                .displayItems((parameters, output) -> {
                    for (var item : RDCuisineItems.CUISINE_ITEMS) {
                        ItemStack itemStack = item.createStack();
                        Collection<FoodProperty> foodProperties = FoodProperties.get(itemStack);
                        itemStack.set(RDDataComponentTypes.FOOD_ITEM_TYPE.value(), Unit.INSTANCE);
                        itemStack.set(RDDataComponentTypes.FOOD_PROPERTIES.value(), foodProperties.stream().toList());
                        output.accept(itemStack);
                    }
                })
        );
    }

    public static ItemStack getFoodItemIcon() {
        for (var foodItem : RDCuisineItems.CUISINE_ITEMS) {
            return foodItem.createStack();
        }
        return new ItemStack(Items.COOKED_BEEF);
    }
}
