package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.item.RDCuisineItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FoodCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("08_food_item_group"));

    public static void bootstrap() {
        ItemGroupContentHelper.registerGroup(FoodCreativeTab.ITEM_GROUP_KEY, builder -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
                .icon(FoodCreativeTab::getFoodItemIcon)
                .title(Component.translatable("item_group.food_item_group"))
                .displayItems((parameters, output) -> {
                    for (var item : RDCuisineItems.CUISINE_ITEMS) {
                        output.accept(item.createStack());
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
