package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.mystias_izakaya.MystiasIzakaya;
import cc.thonly.mystias_izakaya.item.MIItems;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class IngredientCreativeTab implements ItemGroupContentHelper {

    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, MystiasIzakaya.id("ingredients_item_group"));
    public static final CreativeModeTab ITEM_GROUP = ItemGroupContentHelper.builder()
            .icon(() -> new ItemStack(MIItems.BLACK_PORK))
            .title(Component.translatable("item_group.ingredients_item_group"))
            .build();

    public static void bootstrap() {
        ItemGroupEvents.modifyEntriesEvent(IngredientCreativeTab.ITEM_GROUP_KEY).register(itemGroup -> {
            for (Item item : MIItems.INGREDIENTS) {
                itemGroup.accept(item);
            }
        });
        ItemGroupContentHelper.registerGroup(IngredientCreativeTab.ITEM_GROUP_KEY, IngredientCreativeTab.ITEM_GROUP);

    }
}
