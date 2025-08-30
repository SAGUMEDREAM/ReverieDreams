package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.mystias_izakaya.MystiasIzakaya;
import cc.thonly.mystias_izakaya.item.MIItems;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public class FoodCreativeTab implements ItemGroupContent {

    public static final RegistryKey<ItemGroup> ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, MystiasIzakaya.id("food_item_group"));
    public static final ItemGroup ITEM_GROUP = ItemGroupContent.builder()
            .icon(FoodCreativeTab::getFoodItemIcon)
            .displayName(Text.translatable("item_group.food_item_group"))
            .build();

    public static void bootstrap() {
        ItemGroupEvents.modifyEntriesEvent(FoodCreativeTab.ITEM_GROUP_KEY).register(itemGroup -> {
            for (Item item : MIItems.FOOD_ITEMS) {
                itemGroup.add(item);
            }
        });
        ItemGroupContent.registerGroup(FoodCreativeTab.ITEM_GROUP_KEY, FoodCreativeTab.ITEM_GROUP);

    }

    public static ItemStack getFoodItemIcon() {
        for (Item foodItem : MIItems.FOOD_ITEMS) {
            return new ItemStack(foodItem);
        }
        return new ItemStack(Items.COOKED_BEEF);
    }
}
