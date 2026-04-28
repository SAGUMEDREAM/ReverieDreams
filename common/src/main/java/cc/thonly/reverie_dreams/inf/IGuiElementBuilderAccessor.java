package cc.thonly.reverie_dreams.inf;

import net.minecraft.world.item.ItemStack;

public interface IGuiElementBuilderAccessor {
    default ItemStack reverie_dreams$setItemStack(ItemStack stack) {
        return stack;
    }
    default ItemStack reverie_dreams$getItemStack() {
        return ItemStack.EMPTY;
    }
}
