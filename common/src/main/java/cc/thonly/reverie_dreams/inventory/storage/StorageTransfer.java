package cc.thonly.reverie_dreams.inventory.storage;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import java.util.function.IntPredicate;

/** Single source stack, exact components, merge before empty slots. Call only on the server thread. */
public final class StorageTransfer {
    private StorageTransfer() {}

    public static boolean hasMergeSpace(Container to, ItemStack stack) {
        for (int i = 0; i < to.getContainerSize(); i++) {
            var existing = to.getItem(i);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, stack)
                    && to.canPlaceItem(i, stack) && room(to, existing, stack) > 0) return true;
        }
        return false;
    }

    public static int capacity(Container to, ItemStack stack, int slotLimit) {
        int total = 0;
        for (int i = 0; i < Math.min(slotLimit, to.getContainerSize()); i++) {
            var existing = to.getItem(i);
            if (to.canPlaceItem(i, stack) && (existing.isEmpty() || ItemStack.isSameItemSameComponents(existing, stack))) {
                total += Math.max(0, room(to, existing, stack));
            }
        }
        return total;
    }

    private static int room(Container to, ItemStack existing, ItemStack stack) {
        return Math.min(to.getMaxStackSize(stack), stack.getMaxStackSize()) - existing.getCount();
    }

    public static int move(Container from, int sourceSlot, ItemStack expected, Container to, int slotLimit, int requested) {
        return move(from, sourceSlot, expected, to, slot -> slot < slotLimit, requested);
    }

    public static int move(Container from, int sourceSlot, ItemStack expected, Container to, IntPredicate slots, int requested) {
        if (from == to || sourceSlot < 0 || sourceSlot >= from.getContainerSize() || requested <= 0) return 0;
        ItemStack source = from.getItem(sourceSlot);
        if (source.isEmpty() || !ItemStack.isSameItemSameComponents(source, expected)) return 0;
        int remaining = Math.min(source.getCount(), requested);
        int moved = 0;
        for (int pass = 0; pass < 2; pass++) {
            for (int i = 0; i < to.getContainerSize() && remaining > 0; i++) {
                var existing = to.getItem(i);
                if (!slots.test(i) || (pass == 0) == existing.isEmpty() || !to.canPlaceItem(i, source)) continue;
                if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, source)) continue;
                int amount = Math.min(remaining, Math.max(0, room(to, existing, source)));
                if (amount == 0) continue;
                to.setItem(i, source.copyWithCount(existing.getCount() + amount));
                remaining -= amount;
                moved += amount;
            }
        }
        if (moved > 0) {
            from.setItem(sourceSlot, source.copyWithCount(source.getCount() - moved));
            from.setChanged();
            to.setChanged();
        }
        return moved;
    }

    public static int cargoSlot(Container cargo, ItemStack sample) {
        for (int pass = 0; pass < 2; pass++) {
            for (int i = 0; i < Math.min(StoragePlanner.CARGO_SLOTS, cargo.getContainerSize()); i++) {
                var stack = cargo.getItem(i);
                if ((pass == 0) == stack.isEmpty() || !cargo.canPlaceItem(i, sample)) continue;
                if ((stack.isEmpty() || ItemStack.isSameItemSameComponents(stack, sample)) && room(cargo, stack, sample) > 0) return i;
            }
        }
        return -1;
    }
}
