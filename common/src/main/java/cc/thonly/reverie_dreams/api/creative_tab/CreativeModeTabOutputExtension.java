package cc.thonly.reverie_dreams.api.creative_tab;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public interface CreativeModeTabOutputExtension extends CreativeModeTab.Output {

    void insertAfter(ItemStack existingEntry, ItemStack newEntry, CreativeModeTab.TabVisibility visibility);

    void insertBefore(ItemStack existingEntry, ItemStack newEntry, CreativeModeTab.TabVisibility visibility);
}
