package cc.thonly.reverie_dreams.inventory;

import cc.thonly.reverie_dreams.entity.ai.work.WorkTestItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NPCWorkInventoryTest {
    @BeforeAll static void bootstrap() { WorkTestItems.bootstrap(); }
    private NPCInventoryImpl full() {
        var inventory = new NPCInventoryImpl(null, 24);
        for (int i = 0; i < 18; i++) inventory.setItem(i, new ItemStack(Items.STONE, 64));
        return inventory;
    }
    @Test void manualPickupOnlyRemovesTheAmountThatActuallyFits() {
        var inventory = full(); inventory.setItem(0, new ItemStack(Items.WHEAT, 60));
        var ground = new ItemStack(Items.WHEAT, 32);
        assertEquals(4, NPCWorkInventory.pickup(inventory, ground, false));
        assertEquals(28, ground.getCount()); assertEquals(64, inventory.getItem(0).getCount());
        for (int slot = 18; slot < 24; slot++) assertTrue(inventory.getItem(slot).isEmpty());
    }
    @Test void emptyEquipmentSlotsDoNotCountAsCargoSpace() {
        var inventory = full(); var ground = new ItemStack(Items.WHEAT, 32);
        assertFalse(inventory.canAddItem(ground)); assertEquals(0, inventory.insertStack(ground));
        assertEquals(32, ground.getCount()); assertEquals(32, inventory.addItem(ground).getCount());
        assertEquals(32, ground.getCount());
        for (int slot = 18; slot < 24; slot++) assertTrue(inventory.getItem(slot).isEmpty());
    }
    @Test void autoEquipmentMovesExactlyOneArmorPieceAndKeepsOverflow() {
        var inventory = full(); var helmets = new ItemStack(Items.IRON_HELMET, 3);
        helmets.set(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).build());
        assertEquals(1, NPCWorkInventory.pickup(inventory, helmets, false));
        assertEquals(1, inventory.getItem(18).getCount()); assertEquals(2, helmets.getCount());
        assertEquals(0, NPCWorkInventory.pickup(inventory, helmets, false));
        assertEquals(2, helmets.getCount());
    }
    @Test void existingEquipmentCannotBeOverwrittenByPickup() {
        var inventory = full(); inventory.setItem(22, new ItemStack(Items.IRON_SWORD));
        var ground = new ItemStack(Items.DIAMOND, 10);
        assertEquals(0, NPCWorkInventory.pickup(inventory, ground, true));
        assertEquals(10, ground.getCount()); assertTrue(inventory.getItem(22).is(Items.IRON_SWORD));
    }
}
