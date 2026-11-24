package cc.thonly.reverie_dreams.openai;

import cc.thonly.reverie_dreams.ai.npc.NPCInventoryTransfer;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import static org.junit.jupiter.api.Assertions.*;

class NPCInventoryTransferTest {
    @BeforeAll static void bootstrap() {
        SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
        // 26.1 binds default item components during data loading, after Bootstrap.
        // This container test needs only real stack-size prototypes, not a running data-pack loader.
        for (var item : new Item[]{Items.WHEAT, Items.DIAMOND}) {
            if (!item.builtInRegistryHolder().areComponentsBound()) item.builtInRegistryHolder().bindComponents(
                    DataComponentMap.builder()
                            .set(DataComponents.MAX_STACK_SIZE, 64).build());
        }
    }

    @Test void partialTransferKeepsOverflowAndConservesItems() {
        var from = new SimpleContainer(new ItemStack(Items.WHEAT, 32));
        var to = new SimpleContainer(new ItemStack(Items.WHEAT, 60));
        assertEquals(4, NPCInventoryTransfer.move(from, to, Items.WHEAT, 32, i -> true, i -> true));
        assertEquals(28, from.getItem(0).getCount());
        assertEquals(64, to.getItem(0).getCount());
        assertEquals(0, NPCInventoryTransfer.move(from, to, Items.WHEAT, 28, i -> true, i -> true));
        assertEquals(28, from.getItem(0).getCount());
    }

    @Test void movesExactlyRequestedAndRespectsExcludedEquipmentSlots() {
        var from = new SimpleContainer(new ItemStack(Items.WHEAT, 64), new ItemStack(Items.WHEAT, 20));
        var to = new SimpleContainer(2);
        assertEquals(12, NPCInventoryTransfer.move(from, to, Items.WHEAT, 12, i -> i == 1, i -> i == 0));
        assertEquals(64, from.getItem(0).getCount());
        assertEquals(8, from.getItem(1).getCount());
        assertEquals(12, to.getItem(0).getCount());
        assertTrue(to.getItem(1).isEmpty());
    }

    @Test void neverOverwritesDifferentItemsOrBypassesSlotRules() {
        var from = new SimpleContainer(new ItemStack(Items.WHEAT, 32));
        var to = new SimpleContainer(new ItemStack(Items.DIAMOND, 1));
        assertEquals(0, NPCInventoryTransfer.move(from, to, Items.WHEAT, 32, i -> true, i -> true));
        assertTrue(to.getItem(0).is(Items.DIAMOND));
        var rejecting = new SimpleContainer(1) {
            @Override public boolean canPlaceItem(int slot, ItemStack stack) { return false; }
        };
        assertEquals(0, NPCInventoryTransfer.move(from, rejecting, Items.WHEAT, 1, i -> true, i -> true));
        assertThrows(IllegalArgumentException.class, () -> NPCInventoryTransfer.move(from, from, Items.WHEAT, 1, i -> true, i -> true));
    }

    @Test void equippingSwapsInsteadOfDeletingPreviousHandItem() {
        var inventory = new SimpleContainer(24);
        inventory.setItem(0, new ItemStack(Items.WHEAT, 32));
        inventory.setItem(23, new ItemStack(Items.DIAMOND, 2));
        assertTrue(NPCInventoryTransfer.equip(inventory, Items.WHEAT, 23));
        assertEquals(32, inventory.getItem(23).getCount());
        assertTrue(inventory.getItem(0).is(Items.DIAMOND));
        assertEquals(2, inventory.getItem(0).getCount());
        assertTrue(NPCInventoryTransfer.equip(inventory, Items.WHEAT, 23));
        assertEquals(32, inventory.getItem(23).getCount());
    }
}
