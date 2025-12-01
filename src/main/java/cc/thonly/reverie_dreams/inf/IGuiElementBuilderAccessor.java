package cc.thonly.reverie_dreams.inf;

import net.minecraft.world.item.ItemStack;

public interface IGuiElementBuilderAccessor {
    default ItemStack setItemStack(ItemStack stack) {
        return stack;
    }
    default ItemStack getItemStack() {
        return ItemStack.EMPTY;
    }
}
