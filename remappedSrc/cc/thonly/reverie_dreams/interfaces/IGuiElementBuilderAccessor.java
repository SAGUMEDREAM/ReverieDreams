package cc.thonly.reverie_dreams.interfaces;

import net.minecraft.world.item.ItemStack;

public interface IGuiElementBuilderAccessor {
    default ItemStack setItemStack(ItemStack stack) {
        return stack;
    }
    default ItemStack getItemStack() {
        return ItemStack.EMPTY;
    }
}
