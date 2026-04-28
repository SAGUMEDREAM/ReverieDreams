package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.item.base.AlbumItem;
import cc.thonly.reverie_dreams.mixin.accessor.CreativeModeTabsAccessor;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.blay09.mods.balm.world.item.BalmCreativeModeTabRegistrar;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public class BaseCreativeTab implements ItemGroupContentHelper {

    public static void bootstrap(BalmCreativeModeTabRegistrar registrar) {

    }

    public static void busInvoker(CreativeModeTab tab, CreativeModeTab.Output output) {
        final var tabId = BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab);
        if (CreativeModeTabsAccessor.getOpBlocks().identifier().equals(tabId)) {
            output.accept(RDItems.BATTLE_STICK.createStack());
            output.accept(RDItems.OWNER_STICK.createStack());
        }
        if (CreativeModeTabsAccessor.getToolsAndUtilities().identifier().equals(tabId)) {
            for (Item item : AlbumItem.ITEMS) {
                output.accept(item);
            }
        }
    }
}
