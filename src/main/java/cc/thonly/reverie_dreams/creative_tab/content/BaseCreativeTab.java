package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.registry.content.RDEnchantments;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

public class BaseCreativeTab implements ItemGroupContentHelper {

    public static void bootstrap() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(itemGroup -> {
            CreativeModeTab.ItemDisplayParameters context = itemGroup.getContext();
            HolderLookup.Provider registryAccess = context.holders();
            for (ResourceKey<Enchantment> key : RDEnchantments.KEYS) {
                List<ItemStack> books = RDEnchantments.getEnchantmentBook(registryAccess, key);
                books.forEach(itemStack -> {
                    itemGroup.addAfter(Items.ENCHANTED_BOOK, itemStack);
                });
            }
        });
    }
}
