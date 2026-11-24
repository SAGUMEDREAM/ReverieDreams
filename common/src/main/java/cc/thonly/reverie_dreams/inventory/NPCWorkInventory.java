package cc.thonly.reverie_dreams.inventory;

import cc.thonly.reverie_dreams.inventory.storage.StorageTransfer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import java.util.function.Predicate;

/** Cargo is 0..17. Equipment is never an implicit overflow buffer. */
public final class NPCWorkInventory {
    public static final int CARGO_SIZE = 18;
    private NPCWorkInventory() {}
    public static int pickup(Container inventory, ItemStack stack, boolean allowHands) {
        int before = stack.getCount();
        if (inventory.getContainerSize() >= 24 && !stack.isEmpty()) {
            var equippable = stack.get(DataComponents.EQUIPPABLE);
            int slot = equippable == null ? -1 : switch (equippable.slot()) {
                case HEAD -> 18; case CHEST -> 19; case LEGS -> 20; case FEET -> 21; default -> -1;
            };
            if (slot >= 0 && inventory.getItem(slot).isEmpty()) {
                inventory.setItem(slot, stack.copyWithCount(1)); stack.shrink(1);
            } else if (allowHands) {
                int hand = stack.is(Items.SHIELD) || stack.is(Items.TORCH) ? 23 : 22;
                if (inventory.getItem(hand).isEmpty()) {
                    int count = Math.min(stack.getCount(), stack.getMaxStackSize());
                    inventory.setItem(hand, stack.copyWithCount(count)); stack.shrink(count);
                }
            }
        }
        insert(inventory, stack);
        return before - stack.getCount();
    }
    public static int insert(Container inventory, ItemStack stack) {
        if (stack.isEmpty()) return 0;
        var source = new SimpleContainer(stack.copy());
        int moved = StorageTransfer.move(source, 0, stack.copy(), inventory, CARGO_SIZE, stack.getCount());
        stack.shrink(moved);
        return moved;
    }
    public static int count(Container inventory, Predicate<ItemStack> matches) {
        int total = 0;
        for (int i = 0; i < Math.min(CARGO_SIZE, inventory.getContainerSize()); i++) if (matches.test(inventory.getItem(i))) total += inventory.getItem(i).getCount();
        return total;
    }
    public static int find(Container inventory, Predicate<ItemStack> matches) {
        for (int i = 0; i < Math.min(CARGO_SIZE, inventory.getContainerSize()); i++) if (!inventory.getItem(i).isEmpty() && matches.test(inventory.getItem(i))) return i;
        return -1;
    }
    public static int occupied(Container inventory) {
        int count = 0;
        for (int i = 0; i < Math.min(CARGO_SIZE, inventory.getContainerSize()); i++) if (!inventory.getItem(i).isEmpty()) count++;
        return count;
    }
    public static boolean hasEmptyCargo(Container inventory) { return occupied(inventory) < Math.min(CARGO_SIZE, inventory.getContainerSize()); }
}
