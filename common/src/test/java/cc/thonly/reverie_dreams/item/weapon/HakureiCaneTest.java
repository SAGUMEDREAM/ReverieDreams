package cc.thonly.reverie_dreams.item.weapon;

import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HakureiCaneTest {
    @BeforeAll static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        for (Item item : new Item[]{Items.GLOWSTONE_DUST, Items.REDSTONE}) {
            if (!item.builtInRegistryHolder().areComponentsBound()) {
                item.builtInRegistryHolder().bindComponents(DataComponentMap.builder()
                        .set(DataComponents.MAX_STACK_SIZE, 64).build());
            }
        }
    }

    @Test void zeroThroughThreePowerDetermineTheLanesAndAreFullyConsumed() {
        var expected = List.of(List.of(), List.of(0), List.of(0, -1), List.of(0, -1, 1));
        for (int available = 0; available <= 3; available++) {
            var inventory = new SimpleContainer(1);
            if (available > 0) inventory.setItem(0, new ItemStack(Items.GLOWSTONE_DUST, available));
            int count = HakureiCane.consumePower(inventory, Items.GLOWSTONE_DUST, false);
            var fired = new ArrayList<Integer>();
            HakureiCane.forEachLane(count, fired::add);
            assertEquals(expected.get(available), fired);
            assertTrue(inventory.isEmpty());
        }
    }

    @Test void activationConsumesAtMostThreeAcrossSeparateSlotsAndOffhand() {
        var inventory = new SimpleContainer(41);
        inventory.setItem(0, new ItemStack(Items.GLOWSTONE_DUST));
        inventory.setItem(4, new ItemStack(Items.REDSTONE, 8));
        inventory.setItem(40, new ItemStack(Items.GLOWSTONE_DUST, 5));
        assertEquals(3, HakureiCane.consumePower(inventory, Items.GLOWSTONE_DUST, false));
        assertTrue(inventory.getItem(0).isEmpty());
        assertEquals(8, inventory.getItem(4).getCount());
        assertEquals(3, inventory.getItem(40).getCount());
    }

    @Test void unrelatedItemsCannotPayForAShot() {
        var inventory = new SimpleContainer(new ItemStack(Items.REDSTONE, 64));
        assertEquals(0, HakureiCane.consumePower(inventory, Items.GLOWSTONE_DUST, false));
        assertEquals(64, inventory.getItem(0).getCount());
    }

    @Test void creativeFiresAllLanesWithoutAnyPowerOrConsumption() {
        var inventory = new SimpleContainer(1);
        assertEquals(3, HakureiCane.consumePower(inventory, Items.GLOWSTONE_DUST, true));
        inventory.setItem(0, new ItemStack(Items.GLOWSTONE_DUST, 2));
        assertEquals(3, HakureiCane.consumePower(inventory, Items.GLOWSTONE_DUST, true));
        assertEquals(2, inventory.getItem(0).getCount());
    }

    @Test void allThreeWavesReuseThePaidLanesWithoutPayingAgain() {
        var inventory = new SimpleContainer(new ItemStack(Items.GLOWSTONE_DUST, 2));
        int lanes = HakureiCane.consumePower(inventory, Items.GLOWSTONE_DUST, false);
        var fired = new ArrayList<Integer>();
        for (int wave = 0; wave < 3; wave++) HakureiCane.forEachLane(lanes, fired::add);
        assertEquals(List.of(0, -1, 0, -1, 0, -1), fired);
        assertTrue(inventory.isEmpty());
    }
}
