package cc.thonly.reverie_dreams.api.item;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unchecked")
public interface ItemStackHelper {
    public static <T> void modifyComponentSafe(@NotNull ItemStack stack, DataComponentType<T> key, Object value) {
        stack.set(key, (T) value);
    }

    public default <T> ItemStack modifyComponentSafe(DataComponentType<T> key, Object value) {
        assert (Object) this instanceof ItemStack;
        ItemStack stack = (ItemStack) (Object) this;
        stack.set(key, (T) value);
        return stack;
    }


    public boolean reverie_dreams$isFood();

    public boolean reverie_dreams$isDrink();
}
