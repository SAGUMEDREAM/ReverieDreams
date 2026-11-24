package cc.thonly.reverie_dreams.inventory.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class StoragePlannerTest {
    static final StorageSettings SETTINGS = new StorageSettings(true, 0, 0, List.of());
    @BeforeAll static void bootstrap() { StorageTestItems.bootstrap(); }

    static StoragePlanner.Store store(int x, SimpleContainer contents, StorageRule rule) {
        var pos = new BlockPos(x, 0, 0);
        return new StoragePlanner.Store(pos, List.of(pos), contents, rule, List.of(contents));
    }
    static StorageRule frame(ItemStack item, int rotation) { return StorageRule.of(List.of(new StorageRule.Sample(item, rotation))); }
    static StorageRule auto() { return StorageRule.of(List.of()); }
    static SimpleContainer cargo(ItemStack item) { var cargo = new SimpleContainer(24); cargo.setItem(0, item); return cargo; }

    @Test void onlyOrdinaryCargoSlotsAreExported() {
        var cargo = new SimpleContainer(24);
        for (int i = 18; i < 24; i++) cargo.setItem(i, new ItemStack(Items.DIAMOND));
        var box = store(2, new SimpleContainer(27), auto());
        var result = StoragePlanner.plan(cargo, List.of(box), SETTINGS, Vec3.ZERO, s -> true);
        assertNull(result.job());
        assertEquals("sorting_finished", result.code());
        for (int i = 18; i < 24; i++) assertEquals(0, StoragePlanner.exportable(cargo, i, SETTINGS));
    }

    @Test void fartherExistingStacksBeatNearbyEmptyChests() {
        var near = store(1, new SimpleContainer(27), auto());
        var far = store(7, new SimpleContainer(new ItemStack(Items.WHEAT, 30)), auto());
        var result = StoragePlanner.plan(cargo(new ItemStack(Items.WHEAT, 20)), List.of(near, far), SETTINGS, Vec3.ZERO, s -> true);
        assertSame(far, result.job().destination());
    }

    @Test void frameRulesOverrideIncidentalContentsAndOverflowIsLastChoice() {
        var wrong = store(1, new SimpleContainer(new ItemStack(Items.WHEAT, 10)), frame(new ItemStack(Items.DIAMOND), 0));
        var matching = store(6, new SimpleContainer(2), frame(new ItemStack(Items.WHEAT), 0));
        var overflow = store(2, new SimpleContainer(3), frame(new ItemStack(Items.DIAMOND), 6));
        var cargo = cargo(new ItemStack(Items.WHEAT));
        assertSame(matching, StoragePlanner.plan(cargo, List.of(wrong, overflow, matching), SETTINGS, Vec3.ZERO, s -> true).job().destination());
        for (int i = 0; i < 2; i++) matching.container().setItem(i, new ItemStack(Items.WHEAT, 64));
        assertSame(overflow, StoragePlanner.plan(cargo, List.of(wrong, overflow, matching), SETTINGS, Vec3.ZERO, s -> true).job().destination());
        assertSame(overflow, StoragePlanner.plan(cargo, List.of(wrong, overflow), SETTINGS, Vec3.ZERO, s -> true).job().destination());
    }

    @Test void foodAndDurabilityReservesCountAcrossCargoSlots() {
        var food = new ItemStack(Items.APPLE, 10);
        food.set(DataComponents.FOOD, new FoodProperties.Builder().nutrition(4).build());
        var cargo = cargo(food);
        cargo.setItem(1, food.copy());
        var tool = new ItemStack(Items.IRON_PICKAXE);
        tool.set(DataComponents.MAX_DAMAGE, 250);
        cargo.setItem(2, tool);
        cargo.setItem(3, tool.copy());
        var reserve = new StorageSettings(true, 16, 1, List.of());
        assertEquals(0, StoragePlanner.exportable(cargo, 0, reserve));
        assertEquals(4, StoragePlanner.exportable(cargo, 1, reserve));
        assertEquals(0, StoragePlanner.exportable(cargo, 2, reserve));
        assertEquals(1, StoragePlanner.exportable(cargo, 3, reserve));
    }

    @Test void fullNPCInventoryCanStillPlanDeposits() {
        var cargo = new SimpleContainer(24);
        for (int i = 0; i < 18; i++) cargo.setItem(i, new ItemStack(Items.WHEAT, 64));
        var result = StoragePlanner.plan(cargo, List.of(store(1, new SimpleContainer(54), auto())), SETTINGS, Vec3.ZERO, s -> true);
        assertNotNull(result.job());
        assertFalse(result.job().pickup());
    }

    @Test void outputsAreNeverUsedAsSourcesAndInputNeedsAValidDestination() {
        var output = store(1, new SimpleContainer(new ItemStack(Items.WHEAT, 10)), frame(new ItemStack(Items.WHEAT), 0));
        assertNull(StoragePlanner.plan(new SimpleContainer(24), List.of(output), SETTINGS, Vec3.ZERO, s -> true).job());
        var input = store(2, new SimpleContainer(new ItemStack(Items.WHEAT, 10)), frame(new ItemStack(Items.WHEAT), 4));
        assertEquals("no_matching_storage", StoragePlanner.plan(new SimpleContainer(24), List.of(input), SETTINGS, Vec3.ZERO, s -> true).code());
        var job = StoragePlanner.plan(new SimpleContainer(24), List.of(input, output), SETTINGS, Vec3.ZERO, s -> true).job();
        assertTrue(job.pickup());
        assertSame(input, job.source());
        assertSame(output, job.destination());
    }

    @Test void diagnosticsDistinguishMissingFullBusyConflictingAndFinished() {
        var cargo = cargo(new ItemStack(Items.WHEAT));
        assertEquals("no_chest", StoragePlanner.plan(cargo, List.of(), SETTINGS, Vec3.ZERO, s -> true).code());
        var wrong = store(1, new SimpleContainer(1), frame(new ItemStack(Items.DIAMOND), 0));
        assertEquals("no_matching_storage", StoragePlanner.plan(cargo, List.of(wrong), SETTINGS, Vec3.ZERO, s -> true).code());
        var full = store(1, new SimpleContainer(new ItemStack(Items.WHEAT, 64)), auto());
        assertEquals("storage_full", StoragePlanner.plan(cargo, List.of(full), SETTINGS, Vec3.ZERO, s -> true).code());
        var free = store(1, new SimpleContainer(1), auto());
        assertEquals("storage_busy", StoragePlanner.plan(cargo, List.of(free), SETTINGS, Vec3.ZERO, s -> false).code());
        var conflict = store(1, new SimpleContainer(1), StorageRule.of(List.of(new StorageRule.Sample(new ItemStack(Items.WHEAT), 0), new StorageRule.Sample(new ItemStack(Items.WHEAT), 4))));
        assertEquals("storage_rule_conflict", StoragePlanner.plan(cargo, List.of(conflict), SETTINGS, Vec3.ZERO, s -> true).code());
        assertEquals("sorting_finished", StoragePlanner.plan(new SimpleContainer(24), List.of(free), SETTINGS, Vec3.ZERO, s -> true).code());
    }

    @Test void distanceUsesNPCPositionWithinTheSamePriority() {
        var a = store(1, new SimpleContainer(1), auto());
        var b = store(7, new SimpleContainer(1), auto());
        assertSame(b, StoragePlanner.plan(cargo(new ItemStack(Items.WHEAT)), List.of(a, b), SETTINGS, new Vec3(6, 0, 0), s -> true).job().destination());
    }
}
