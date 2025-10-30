package cc.thonly.reverie_dreams.util.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

public class ItemUtils {
    public static boolean isArmorItem(ItemStack stack) {
        return stack.get(DataComponents.EQUIPPABLE) != null;
    }
}
