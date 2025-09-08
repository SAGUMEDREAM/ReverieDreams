package cc.thonly.reverie_dreams.util;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.util.function.Function;

public class PredicateSlot extends Slot {
    private final Function<ItemStack, Boolean> predicate;
    public PredicateSlot(Inventory inventory, int index, int x, int y, Function<ItemStack, Boolean> predicate) {
        super(inventory, index, x, y);
        this.predicate = predicate;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return this.predicate.apply(stack);
    }
}
