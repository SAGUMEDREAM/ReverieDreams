package cc.thonly.reverie_dreams.inventory;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface SlotFilter {
    boolean test(ItemStack stack);
}