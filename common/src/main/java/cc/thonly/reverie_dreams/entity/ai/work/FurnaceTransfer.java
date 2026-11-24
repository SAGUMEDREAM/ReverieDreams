package cc.thonly.reverie_dreams.entity.ai.work;

import cc.thonly.reverie_dreams.inventory.NPCWorkInventory;
import cc.thonly.reverie_dreams.inventory.storage.StorageTransfer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public final class FurnaceTransfer {
    private FurnaceTransfer() {
    }

    public static int space(Container furnace, int slot, ItemStack stack) {
        var current = furnace.getItem(slot);
        if (!furnace.canPlaceItem(slot, stack) || !current.isEmpty() && !ItemStack.isSameItemSameComponents(current, stack))
            return 0;
        return Math.max(0, Math.min(stack.getMaxStackSize(), furnace.getMaxStackSize(stack)) - current.getCount());
    }

    public static int insert(Container cargo, int sourceSlot, ItemStack expected, Container furnace, int furnaceSlot,
                             Predicate<ItemStack> valid) {
        return insert(cargo, sourceSlot, expected, furnace, furnaceSlot, valid, Integer.MAX_VALUE);
    }

    public static int insert(Container cargo, int sourceSlot, ItemStack expected, Container furnace, int furnaceSlot,
                             Predicate<ItemStack> valid, int maximum) {
        if (sourceSlot < 0 || sourceSlot >= NPCWorkInventory.CARGO_SIZE || furnaceSlot < 0 || furnaceSlot > 1)
            return 0;
        var stack = cargo.getItem(sourceSlot);
        if (stack.isEmpty() || !ItemStack.isSameItemSameComponents(stack, expected) || !valid.test(stack))
            return 0;
        return StorageTransfer.move(cargo, sourceSlot, expected, furnace, slot -> slot == furnaceSlot, Math.min(stack.getCount(), maximum));
    }

    public static int collect(Container furnace, int slot, ItemStack expected, Container cargo) {
        if (slot < 1 || slot > 2)
            return 0;
        return StorageTransfer.move(furnace, slot, expected, cargo, NPCWorkInventory.CARGO_SIZE, expected.getCount());
    }
}
