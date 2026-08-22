package cc.thonly.reverie_dreams.gui.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class LimitedSlot extends Slot {

    private final int maxStackSize;

    public LimitedSlot(
            Container container,
            int slot,
            int maxStackSize
    ) {
        super(container, slot, 0, 0);
        this.maxStackSize = maxStackSize;
    }

    @Override
    public int getMaxStackSize() {
        return this.maxStackSize;
    }

    @Override
    public int getMaxStackSize(
            net.minecraft.world.item.ItemStack stack
    ) {
        return this.maxStackSize;
    }
}
