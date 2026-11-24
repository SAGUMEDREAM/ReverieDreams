package cc.thonly.reverie_dreams.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.function.Predicate;
import net.minecraft.world.item.Items;

/** Both swapped stacks remain in the real inventory, including during save/death or manual edits. */
public final class WorkToolLease {
    private int source = -1;
    private ItemStack original = ItemStack.EMPTY;
    private ItemStack tool = ItemStack.EMPTY;

    public InteractionHand ensure(Container inventory, Predicate<ItemStack> wanted) {
        if (wanted.test(inventory.getItem(22)) && !inventory.getItem(22).isEmpty()) return InteractionHand.MAIN_HAND;
        if (wanted.test(inventory.getItem(23)) && !inventory.getItem(23).isEmpty()) return InteractionHand.OFF_HAND;
        restore(inventory);
        int slot = NPCWorkInventory.find(inventory, wanted);
        if (slot < 0 || inventory.getContainerSize() < 24) return null;
        source = slot;
        original = inventory.getItem(22).copy();
        tool = inventory.getItem(slot).copyWithCount(1);
        inventory.setItem(22, inventory.getItem(slot).copy());
        inventory.setItem(slot, original.copy());
        inventory.setChanged();
        return InteractionHand.MAIN_HAND;
    }

    public void restore(Container inventory) {
        if (source < 0 || source >= NPCWorkInventory.CARGO_SIZE || inventory.getContainerSize() < 24) { clear(); return; }
        var hand = inventory.getItem(22);
        var old = inventory.getItem(source);
        boolean stillTool = hand.isEmpty() || !tool.isEmpty() && (hand.is(tool.getItem())
                || tool.is(Items.GLASS_BOTTLE) && hand.is(Items.HONEY_BOTTLE));
        if (stillTool && (original.isEmpty() ? old.isEmpty() : ItemStack.isSameItemSameComponents(old, original))) {
            inventory.setItem(22, old.copy());
            inventory.setItem(source, hand.copy());
            inventory.setChanged();
        } else if (original.isEmpty() && stillTool) {
            var remainder = hand.copy();
            NPCWorkInventory.insert(inventory, remainder);
            inventory.setItem(22, remainder);
        }
        clear();
    }

    public void save(ValueOutput out) {
        if (source < 0) return;
        out.putInt("WorkTool.Source", source);
        out.store("WorkTool.Original", ItemStack.OPTIONAL_CODEC, original);
        out.store("WorkTool.Tool", ItemStack.OPTIONAL_CODEC, tool);
    }
    public void load(ValueInput in, Container inventory) {
        source = in.getIntOr("WorkTool.Source", -1);
        original = in.read("WorkTool.Original", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        tool = in.read("WorkTool.Tool", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        restore(inventory);
    }
    private void clear() { source = -1; original = ItemStack.EMPTY; tool = ItemStack.EMPTY; }
}
