package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.item.RDDrinkItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DrinkCreativeTab implements ItemGroupContentHelper {

    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("drink_item_group"));
    public static final CreativeModeTab ITEM_GROUP = ItemGroupContentHelper.builder()
            .icon(() -> new ItemStack(RDDrinkItems.GREEN_TEA))
            .title(Component.translatable("item_group.drink_item_group"))
            .displayItems((parameters, output) -> {
                output.accept(Items.BARREL);
                for (Item item : RDDrinkItems.DRINK_ITEMS) {
                    output.accept(item);
                }
            })
            .build();

    public static void bootstrap() {
        ItemGroupContentHelper.registerGroup(DrinkCreativeTab.ITEM_GROUP_KEY, DrinkCreativeTab.ITEM_GROUP);

    }
}
