package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.item.base.AlbumItem;
import cc.thonly.reverie_dreams.mixin.accessor.CreativeModeTabsAccessor;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.impl.BlockDelegate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import java.util.List;

public class BaseCreativeTab implements ItemGroupContentHelper {

    public static void bootstrap() {

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
        if (CreativeModeTabsAccessor.getRedstoneBlocks().identifier().equals(tabId)) {
            List<BlockDelegate> list = List.of(
                    RDBlocks.RAIL_CONTROLLER_BLOCK,
                    RDBlocks.SIGNAL_RAIL_BLOCK,
                    RDBlocks.SIGNAL_DELAYER_BLOCK,
                    RDBlocks.REMOTE_CLIENT,
                    RDBlocks.REMOTE_SERVER,
                    RDBlocks.SPEAKER
            );
            for (int i = list.size() - 1; i >= 0; i--) {
                output.accept(list.get(i));
            }
        }
    }
}
