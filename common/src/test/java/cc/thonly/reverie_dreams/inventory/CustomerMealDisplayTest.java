package cc.thonly.reverie_dreams.inventory;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerMealDisplayTest {
    @BeforeAll static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        for (Item item : new Item[]{Items.WHEAT, Items.DIAMOND, Items.IRON_SWORD}) {
            if (!item.builtInRegistryHolder().areComponentsBound()) {
                item.builtInRegistryHolder().bindComponents(DataComponentMap.builder()
                        .set(DataComponents.MAX_STACK_SIZE, item == Items.IRON_SWORD ? 1 : 64).build());
            }
        }
    }

    @Test void fullInventoryAndExistingEquipmentRemainUnchanged() {
        var inventory = new SimpleContainer(24);
        for (int slot = 0; slot < 18; slot++) inventory.setItem(slot, new ItemStack(Items.WHEAT, 64));
        inventory.setItem(22, new ItemStack(Items.IRON_SWORD));
        inventory.setItem(23, new ItemStack(Items.WHEAT, 16));
        var meal = new CustomerMealDisplay();
        meal.show(inventory, InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND));
        meal.show(inventory, InteractionHand.OFF_HAND, new ItemStack(Items.WHEAT));
        meal.clear(inventory);
        meal.clear(inventory);
        assertTrue(inventory.getItem(22).is(Items.IRON_SWORD));
        assertEquals(16, inventory.getItem(23).getCount());
        for (int slot = 0; slot < 18; slot++) assertEquals(64, inventory.getItem(slot).getCount());
    }

    @Test void onlyItsOwnUnchangedVisualsAreCleared() {
        var inventory = new SimpleContainer(24);
        var meal = new CustomerMealDisplay();
        meal.show(inventory, InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT));
        meal.show(inventory, InteractionHand.OFF_HAND, new ItemStack(Items.DIAMOND));
        assertEquals(1, inventory.getItem(22).getCount());
        assertFalse(ItemStack.isSameItemSameComponents(inventory.getItem(22), new ItemStack(Items.WHEAT)),
                "Visual meal items must not merge with real food picked up by the NPC");
        inventory.setItem(23, new ItemStack(Items.IRON_SWORD));
        meal.clear(inventory);
        assertTrue(inventory.getItem(22).isEmpty());
        assertTrue(inventory.getItem(23).is(Items.IRON_SWORD));
    }

    @Test void savedMealRestoresCleanupMarkersWithoutTouchingEquipment() {
        var inventory = new SimpleContainer(24);
        inventory.setItem(23, new ItemStack(Items.DIAMOND, 5));
        var meal = new CustomerMealDisplay();
        meal.show(inventory, InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT));
        meal.show(inventory, InteractionHand.OFF_HAND, new ItemStack(Items.WHEAT));
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        meal.save(output);
        var restored = new CustomerMealDisplay();
        restored.load(TagValueInput.create(ProblemReporter.DISCARDING, registries, output.buildResult()));
        assertTrue(restored.meal(InteractionHand.OFF_HAND).is(Items.WHEAT));
        restored.clear(inventory);
        assertTrue(inventory.getItem(22).isEmpty());
        assertEquals(5, inventory.getItem(23).getCount());
    }

    @Test void legacyRecoveryNeverTreatsEquipmentOrAWholeStackAsAMealVisual() {
        var inventory = new SimpleContainer(24);
        inventory.setItem(22, new ItemStack(Items.IRON_SWORD));
        inventory.setItem(23, new ItemStack(Items.WHEAT, 16));
        var meal = new CustomerMealDisplay();
        meal.recoverLegacy(inventory, false, true);
        meal.clear(inventory);
        assertTrue(inventory.getItem(22).is(Items.IRON_SWORD));
        assertEquals(16, inventory.getItem(23).getCount());
    }
}
