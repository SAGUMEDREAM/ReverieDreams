package cc.thonly.reverie_dreams.util.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;

public class ItemUtils {
    public static boolean isArmorItem(ItemStack stack) {
        return stack.get(DataComponentTypes.EQUIPPABLE) != null;
    }
}
