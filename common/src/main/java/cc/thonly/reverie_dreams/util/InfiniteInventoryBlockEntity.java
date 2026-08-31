package cc.thonly.reverie_dreams.util;

import cc.thonly.reverie_dreams.inventory.InfiniteInventory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface InfiniteInventoryBlockEntity {
    InfiniteInventory getInventory();

    default BlockEntity asBlockEntity() {
        return (BlockEntity) this;
    }
}
