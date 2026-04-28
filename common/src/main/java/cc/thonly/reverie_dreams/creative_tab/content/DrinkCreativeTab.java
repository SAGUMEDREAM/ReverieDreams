package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.item.RDDrinkItems;
import net.blay09.mods.balm.world.item.BalmCreativeModeTabRegistrar;
import net.blay09.mods.balm.world.item.DeferredItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DrinkCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("09_drink_item_group"));

    public static void bootstrap(BalmCreativeModeTabRegistrar registrar) {
        ItemGroupContentHelper.registerGroup(registrar, DrinkCreativeTab.ITEM_GROUP_KEY, builder -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
                .icon(() -> new ItemStack(RDDrinkItems.GREEN_TEA.asItem()))
                .title(Component.translatable("item_group.drink_item_group"))
                .displayItems((parameters, output) -> {
                    output.accept(Items.BARREL);
                    for (DeferredItem item : RDDrinkItems.DRINK_ITEMS) {
                        output.accept(item);
                    }
                })
        );

    }
}
