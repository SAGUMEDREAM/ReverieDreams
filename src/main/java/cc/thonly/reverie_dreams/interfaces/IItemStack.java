package cc.thonly.reverie_dreams.interfaces;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("unchecked")
public interface IItemStack {
    public static <T> void modifyComponentSafe(@NotNull ItemStack stack, DataComponentType<T> key, Object value) {
        stack.set(key, (T) value);
    }

    public default <T> ItemStack modifyComponentSafe(DataComponentType<T> key, Object value) {
        assert (Object) this instanceof ItemStack;
        ItemStack stack = (ItemStack) (Object) this;
        stack.set(key, (T) value);
        return stack;
    }


    public boolean isFood();
    public boolean isDrink();
}
