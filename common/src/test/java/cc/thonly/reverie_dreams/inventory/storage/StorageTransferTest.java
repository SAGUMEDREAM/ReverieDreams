package cc.thonly.reverie_dreams.inventory.storage;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StorageTransferTest {
    @BeforeAll static void bootstrap() { StorageTestItems.bootstrap(); }

    @Test void mergesBeforeUsingAnEarlierEmptySlot() {
        var from = new SimpleContainer(new ItemStack(Items.WHEAT, 32));
        var to = new SimpleContainer(3);
        to.setItem(2, new ItemStack(Items.WHEAT, 60));
        assertEquals(32, StorageTransfer.move(from, 0, from.getItem(0), to, 3, 32));
        assertEquals(64, to.getItem(2).getCount());
        assertEquals(28, to.getItem(0).getCount());
        assertTrue(from.isEmpty());
    }

    @Test void partialMoveConservesCountsAndRespectsContainerLimits() {
        var from = new SimpleContainer(new ItemStack(Items.WHEAT, 20));
        var to = new SimpleContainer(new ItemStack(Items.WHEAT, 62));
        assertEquals(2, StorageTransfer.move(from, 0, from.getItem(0).copy(), to, 1, 20));
        assertEquals(18, from.getItem(0).getCount());
        assertEquals(0, StorageTransfer.move(from, 0, from.getItem(0), to, 1, 20));
        var limited = new SimpleContainer(1) { @Override public int getMaxStackSize() { return 4; } };
        assertEquals(4, StorageTransfer.move(from, 0, from.getItem(0), limited, 1, 20));
        assertEquals(14, from.getItem(0).getCount());
    }

    @Test void componentVariantsNeverMergeAndChangedSourcesAreRejected() {
        var named = new ItemStack(Items.WHEAT, 10);
        named.set(DataComponents.CUSTOM_NAME, Component.literal("seed"));
        var from = new SimpleContainer(named);
        var to = new SimpleContainer(new ItemStack(Items.WHEAT, 30));
        assertEquals(0, StorageTransfer.move(from, 0, named.copy(), to, 1, 10));
        assertEquals(0, StorageTransfer.move(from, 0, new ItemStack(Items.DIAMOND), new SimpleContainer(1), 1, 10));
        assertEquals(10, from.getItem(0).getCount());
        assertEquals(30, to.getItem(0).getCount());
    }

    @Test void placementRestrictionsAndSingleSlotSelectionAreHonored() {
        var from = new SimpleContainer(new ItemStack(Items.WHEAT, 20));
        var rejecting = new SimpleContainer(2) {
            @Override public boolean canPlaceItem(int slot, ItemStack stack) { return slot == 1; }
        };
        assertEquals(20, StorageTransfer.move(from, 0, from.getItem(0), rejecting, i -> i == 1, 20));
        assertTrue(rejecting.getItem(0).isEmpty());
        assertEquals(20, rejecting.getItem(1).getCount());
        assertEquals(0, StorageTransfer.move(rejecting, 1, rejecting.getItem(1), rejecting, 2, 5));
    }

    @Test void cargoCapacityAndSlotSelectionExcludeAllEquipment() {
        var cargo = new SimpleContainer(24);
        for (int i = 0; i < 18; i++) cargo.setItem(i, new ItemStack(Items.DIAMOND, 64));
        assertEquals(-1, StorageTransfer.cargoSlot(cargo, new ItemStack(Items.WHEAT)));
        assertEquals(0, StorageTransfer.capacity(cargo, new ItemStack(Items.WHEAT), 18));
        cargo.setItem(4, new ItemStack(Items.WHEAT, 60));
        assertEquals(4, StorageTransfer.cargoSlot(cargo, new ItemStack(Items.WHEAT)));
    }
}
