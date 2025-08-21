package cc.thonly.reverie_dreams.interfaces;

import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unchecked")
public interface IItemStack {
    public static <T> void modifyComponentSafe(@NotNull ItemStack stack, ComponentType<T> key, Object value) {
        stack.set(key, (T) value);
    }

    public default <T> ItemStack modifyComponentSafe(ComponentType<T> key, Object value) {
        assert (Object) this instanceof ItemStack;
        ItemStack stack = (ItemStack) (Object) this;
        stack.set(key, (T) value);
        return stack;
    }

    public boolean isFood();
}
