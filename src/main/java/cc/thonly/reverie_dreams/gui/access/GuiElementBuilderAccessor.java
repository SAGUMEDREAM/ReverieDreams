package cc.thonly.reverie_dreams.gui.access;

import net.minecraft.item.ItemStack;

public interface GuiElementBuilderAccessor {
    default ItemStack setItemStack(ItemStack stack) {
        return stack;
    }
    default ItemStack getItemStack() {
        return ItemStack.EMPTY;
    }
}
