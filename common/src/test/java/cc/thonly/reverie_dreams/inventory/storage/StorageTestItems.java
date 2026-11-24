package cc.thonly.reverie_dreams.inventory.storage;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;

final class StorageTestItems {
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        for (var item : new Item[]{Items.WHEAT, Items.DIAMOND, Items.OAK_LOG, Items.BIRCH_LOG, Items.APPLE, Items.IRON_PICKAXE}) {
            if (!item.builtInRegistryHolder().areComponentsBound()) item.builtInRegistryHolder().bindComponents(
                    DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, item == Items.IRON_PICKAXE ? 1 : 64).build());
        }
    }
}
