package cc.thonly.reverie_dreams.item.weapon;

import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class TrumpetGunTest {
    // Vanilla items avoid bootstrapping loader-owned RDItems in plain JUnit.
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

    @Test void oneRoundFiresOnceWithoutRequiringAFullBurst() {
        var inventory = new SimpleContainer(new ItemStack(Items.GLOWSTONE_DUST));
        assertEquals(1, burst(inventory));
        assertTrue(inventory.isEmpty());
    }

    @Test void twoRoundsFireTwiceWithoutRequiringAFullBurst() {
        var inventory = new SimpleContainer(new ItemStack(Items.GLOWSTONE_DUST, 2));
        assertEquals(2, burst(inventory));
        assertTrue(inventory.isEmpty());
    }

    @Test void aBurstConsumesOnlyThreeRoundsFromLargerStock() {
        var inventory = new SimpleContainer(new ItemStack(Items.GLOWSTONE_DUST, 7));
        assertEquals(3, burst(inventory));
        assertEquals(4, inventory.getItem(0).getCount());
    }

    @Test void emptyInventoryNeverInvokesProjectileCreation() {
        assertEquals(0, burst(new SimpleContainer(2)));
    }

    @Test void unrelatedItemsAreNeverUsedAsAmmo() {
        var inventory = new SimpleContainer(new ItemStack(Items.REDSTONE, 3));
        assertEquals(0, burst(inventory));
        assertEquals(3, inventory.getItem(0).getCount());
    }

    @Test void ammoCanBeSplitAcrossSlotsIncludingTheOffhandSlot() {
        var inventory = new SimpleContainer(41);
        inventory.setItem(4, new ItemStack(Items.GLOWSTONE_DUST));
        inventory.setItem(40, new ItemStack(Items.GLOWSTONE_DUST));
        assertEquals(2, burst(inventory));
        assertTrue(inventory.isEmpty());
    }

    @Test void failedProjectileCreationDoesNotConsumeAmmo() {
        var inventory = new SimpleContainer(new ItemStack(Items.GLOWSTONE_DUST, 2));
        assertFalse(TrumpetGun.tryFireShot(inventory, Items.GLOWSTONE_DUST, false, () -> false));
        assertEquals(2, inventory.getItem(0).getCount());
        assertEquals(2, burst(inventory));
    }

    @Test void removedAmmoIsRecheckedBetweenDelayedShots() {
        var inventory = new SimpleContainer(new ItemStack(Items.GLOWSTONE_DUST, 3));
        assertTrue(TrumpetGun.tryFireShot(inventory, Items.GLOWSTONE_DUST, false, () -> true));
        inventory.setItem(0, ItemStack.EMPTY);
        assertEquals(0, burst(inventory));
    }

    @Test void creativeShotsNeedNoAmmoAndDoNotConsumeStoredAmmo() {
        var inventory = new SimpleContainer(1);
        assertTrue(TrumpetGun.tryFireShot(inventory, Items.GLOWSTONE_DUST, true, () -> true));
        inventory.setItem(0, new ItemStack(Items.GLOWSTONE_DUST, 2));
        assertTrue(TrumpetGun.tryFireShot(inventory, Items.GLOWSTONE_DUST, true, () -> true));
        assertEquals(2, inventory.getItem(0).getCount());
    }

    private int burst(SimpleContainer inventory) {
        var spawned = new AtomicInteger();
        for (int shot = 0; shot < 3; shot++) {
            TrumpetGun.tryFireShot(inventory, Items.GLOWSTONE_DUST, false, () -> {
                spawned.incrementAndGet();
                return true;
            });
        }
        return spawned.get();
    }
}
