package cc.thonly.reverie_dreams.util;

import java.util.function.Function;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PredicateSlot extends Slot {
    private final Function<ItemStack, Boolean> predicate;
    public PredicateSlot(Container inventory, int index, int x, int y, Function<ItemStack, Boolean> predicate) {
        super(inventory, index, x, y);
        this.predicate = predicate;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.predicate.apply(stack);
    }
}
