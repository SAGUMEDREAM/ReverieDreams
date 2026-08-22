package cc.thonly.reverie_dreams.util;

import cc.thonly.reverie_dreams.inventory.InfiniteInventory;
import net.minecraft.world.level.Level;

public interface InfiniteInventoryBlockEntity {
    InfiniteInventory getInventory();

    void setChanged();

    Level getLevel();
}
