package cc.thonly.reverie_dreams.inventory;

import cc.thonly.reverie_dreams.entity.ai.work.WorkTestItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WorkToolLeaseTest {
    @BeforeAll static void bootstrap() { WorkTestItems.bootstrap(); }
    private SimpleContainer inventory() {
        var inventory = new SimpleContainer(24);
        var sword = new ItemStack(Items.IRON_SWORD); sword.set(DataComponents.CUSTOM_NAME, Component.literal("owner blade"));
        inventory.setItem(22, sword); inventory.setItem(0, new ItemStack(Items.SHEARS));
        return inventory;
    }
    @Test void swapsInRealInventoryAndRestoresTheOriginalWeapon() {
        var inventory = inventory(); var lease = new WorkToolLease();
        var original = inventory.getItem(22).copy();
        assertEquals(InteractionHand.MAIN_HAND, lease.ensure(inventory, s -> s.is(Items.SHEARS)));
        assertTrue(ItemStack.isSameItemSameComponents(original, inventory.getItem(0)));
        inventory.getItem(22).set(DataComponents.DAMAGE, 4);
        lease.restore(inventory);
        assertTrue(ItemStack.isSameItemSameComponents(original, inventory.getItem(22)));
        assertTrue(inventory.getItem(0).is(Items.SHEARS));
        assertEquals(4, inventory.getItem(0).getOrDefault(DataComponents.DAMAGE, 0));
    }
    @Test void restoresWeaponEvenWhenBorrowedToolBreaks() {
        var inventory = inventory(); var lease = new WorkToolLease();
        lease.ensure(inventory, s -> s.is(Items.SHEARS)); inventory.setItem(22, ItemStack.EMPTY); lease.restore(inventory);
        assertTrue(inventory.getItem(22).is(Items.IRON_SWORD)); assertTrue(inventory.getItem(0).isEmpty());
    }
    @Test void manualEditsAreNeverOverwritten() {
        var inventory = inventory(); var lease = new WorkToolLease();
        lease.ensure(inventory, s -> s.is(Items.SHEARS));
        inventory.setItem(3, inventory.getItem(0)); inventory.setItem(0, new ItemStack(Items.DIAMOND, 4));
        lease.restore(inventory);
        assertTrue(inventory.getItem(3).is(Items.IRON_SWORD)); assertEquals(4, inventory.getItem(0).getCount());
        assertTrue(inventory.getItem(22).is(Items.SHEARS));
    }
    @Test void aBottleResultIsReturnedToCargoWhenRestoringTheWeapon() {
        var inventory = inventory(); inventory.setItem(0, new ItemStack(Items.GLASS_BOTTLE)); var lease = new WorkToolLease();
        lease.ensure(inventory, s -> s.is(Items.GLASS_BOTTLE)); inventory.setItem(22, new ItemStack(Items.HONEY_BOTTLE));
        lease.restore(inventory);
        assertTrue(inventory.getItem(22).is(Items.IRON_SWORD)); assertTrue(inventory.getItem(0).is(Items.HONEY_BOTTLE));
    }
    @Test void saveReloadRestoresTheWeaponFromItsRealStoredSlot() {
        var inventory = inventory(); var lease = new WorkToolLease(); lease.ensure(inventory, s -> s.is(Items.SHEARS));
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries); lease.save(output);
        new WorkToolLease().load(TagValueInput.create(ProblemReporter.DISCARDING, registries, output.buildResult()), inventory);
        assertTrue(inventory.getItem(22).is(Items.IRON_SWORD)); assertTrue(inventory.getItem(0).is(Items.SHEARS));
    }
}
