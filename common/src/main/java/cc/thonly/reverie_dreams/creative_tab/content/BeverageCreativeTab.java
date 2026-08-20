package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.item.RDBeverageItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BeverageCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("09_drink_item_group"));

    public static void bootstrap() {
        ItemGroupContentHelper.registerGroup(BeverageCreativeTab.ITEM_GROUP_KEY, builder -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
                .icon(() -> new ItemStack(RDBeverageItems.GREEN_TEA.asItem()))
                .title(Component.translatable("item_group.drink_item_group"))
                .displayItems((parameters, output) -> {
                    output.accept(Items.BARREL);
                    for (var item : RDBeverageItems.DRINK_ITEMS) {
                        output.accept(item);
                    }
                })
        );

    }
}
