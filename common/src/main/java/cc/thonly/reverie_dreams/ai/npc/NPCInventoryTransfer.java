package cc.thonly.reverie_dreams.ai.npc;

import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.function.IntPredicate;

/** Moves only what fits; source is reduced by the exact amount inserted, on the server thread. */
public final class NPCInventoryTransfer {
    private NPCInventoryTransfer() {}

    public static boolean equip(Container inventory, Item item, int hand) {
        if (hand != 22 && hand != 23 || inventory.getContainerSize() < 24) throw new IllegalArgumentException("Invalid hand slot");
        if (inventory.getItem(hand).is(item)) return true;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (slot >= 18 && slot < 22 || slot == hand || !inventory.getItem(slot).is(item)) continue;
            var previous = inventory.getItem(hand).copy();
            inventory.setItem(hand, inventory.getItem(slot).copy());
            inventory.setItem(slot, previous);
            inventory.setChanged();
            return true;
        }
        return false;
    }

    public static int move(Container from, Container to, Item item, int requested,
                           IntPredicate sourceSlots, IntPredicate destinationSlots) {
        if (from == to || requested < 1 || requested > 1024) throw new IllegalArgumentException("Invalid transfer");
        int moved = 0;
        for (int source = 0; source < from.getContainerSize() && moved < requested; source++) {
            if (!sourceSlots.test(source)) continue;
            var stack = from.getItem(source);
            if (!stack.is(item)) continue;
            for (int destination = 0; destination < to.getContainerSize() && moved < requested; destination++) {
                if (!destinationSlots.test(destination) || !to.canPlaceItem(destination, stack)) continue;
                var existing = to.getItem(destination);
                if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, stack)) continue;
                int capacity = Math.min(to.getMaxStackSize(), stack.getMaxStackSize()) - existing.getCount();
                int amount = Math.min(Math.min(capacity, stack.getCount()), requested - moved);
                if (amount <= 0) continue;
                if (existing.isEmpty()) to.setItem(destination, stack.copyWithCount(amount));
                else existing.grow(amount);
                stack.shrink(amount);
                moved += amount;
                if (stack.isEmpty()) { from.setItem(source, ItemStack.EMPTY); break; }
            }
        }
        if (moved > 0) { from.setChanged(); to.setChanged(); }
        return moved;
    }
}
