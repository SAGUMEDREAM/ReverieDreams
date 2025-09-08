package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.mystias_izakaya.MystiasIzakaya;
import cc.thonly.mystias_izakaya.item.MIItems;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public class IngredientCreativeTab implements ItemGroupContent {

    public static final RegistryKey<ItemGroup> ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, MystiasIzakaya.id("ingredients_item_group"));
    public static final ItemGroup ITEM_GROUP = ItemGroupContent.builder()
            .icon(() -> new ItemStack(MIItems.BLACK_PORK))
            .displayName(Text.translatable("item_group.ingredients_item_group"))
            .build();

    public static void bootstrap() {
        ItemGroupEvents.modifyEntriesEvent(IngredientCreativeTab.ITEM_GROUP_KEY).register(itemGroup -> {
            for (Item item : MIItems.INGREDIENTS) {
                itemGroup.add(item);
            }
        });
        ItemGroupContent.registerGroup(IngredientCreativeTab.ITEM_GROUP_KEY, IngredientCreativeTab.ITEM_GROUP);

    }
}
