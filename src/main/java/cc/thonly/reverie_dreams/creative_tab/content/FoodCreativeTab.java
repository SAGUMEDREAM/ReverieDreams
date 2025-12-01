package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.item.RDFoodItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FoodCreativeTab implements ItemGroupContentHelper {

    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("food_item_group"));
    public static final CreativeModeTab ITEM_GROUP = ItemGroupContentHelper.builder()
            .icon(FoodCreativeTab::getFoodItemIcon)
            .title(Component.translatable("item_group.food_item_group"))
            .displayItems((parameters, output) -> {
                for (Item item : RDFoodItems.FOOD_ITEMS) {
                    output.accept(item);
                }
            })
            .build();

    public static void bootstrap() {
        ItemGroupContentHelper.registerGroup(FoodCreativeTab.ITEM_GROUP_KEY, FoodCreativeTab.ITEM_GROUP);
    }

    public static ItemStack getFoodItemIcon() {
        for (Item foodItem : RDFoodItems.FOOD_ITEMS) {
            return new ItemStack(foodItem);
        }
        return new ItemStack(Items.COOKED_BEEF);
    }
}
