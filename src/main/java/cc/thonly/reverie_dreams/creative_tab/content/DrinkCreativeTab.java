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
import net.minecraft.world.item.Items;

public class DrinkCreativeTab implements ItemGroupContentHelper {

    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, MystiasIzakaya.id("drink_item_group"));
    public static final CreativeModeTab ITEM_GROUP = ItemGroupContentHelper.builder()
            .icon(() -> new ItemStack(MIItems.GREEN_TEA))
            .title(Component.translatable("item_group.drink_item_group"))
            .build();

    public static void bootstrap() {
        ItemGroupEvents.modifyEntriesEvent(DrinkCreativeTab.ITEM_GROUP_KEY).register(itemGroup -> {
            itemGroup.accept(Items.BARREL);
            for (Item item : MIItems.DRINK_ITEMS) {
                itemGroup.accept(item);
            }
        });
        ItemGroupContentHelper.registerGroup(DrinkCreativeTab.ITEM_GROUP_KEY, DrinkCreativeTab.ITEM_GROUP);

    }
}
