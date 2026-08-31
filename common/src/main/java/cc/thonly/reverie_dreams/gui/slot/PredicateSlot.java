package cc.thonly.reverie_dreams.gui.slot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public class PredicateSlot extends Slot {
    private final Function<ItemStack, Boolean> predicate;

    public PredicateSlot(Container inventory, int index, int x, int y, Function<ItemStack, Boolean> predicate) {
        super(inventory, index, x, y);
        this.predicate = predicate;
    }

    @Override
    public boolean allowModification(Player player) {
        return super.allowModification(player) && this.predicate.apply(ItemStack.EMPTY);
    }

    @Override
    public boolean mayPickup(Player player) {
        return super.mayPickup(player) && this.predicate.apply(ItemStack.EMPTY);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.predicate.apply(stack);
    }
}
