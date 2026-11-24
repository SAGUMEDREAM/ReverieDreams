package cc.thonly.reverie_dreams.inventory.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import static cc.thonly.reverie_dreams.inventory.storage.StoragePlannerTest.*;
import static org.junit.jupiter.api.Assertions.*;

class StorageExecutionTest {
    @BeforeAll static void bootstrap() { StorageTestItems.bootstrap(); }

    private StoragePlanner.Job deposit(SimpleContainer cargo, StoragePlanner.Store output) {
        return StoragePlanner.plan(cargo, List.of(output), SETTINGS, Vec3.ZERO, s -> true).job();
    }

    @Test void removedOrReplacedChestsNeverReceiveItemsOrReduceCargo() {
        var cargo = cargo(new ItemStack(Items.WHEAT, 20));
        var old = store(1, new SimpleContainer(27), auto());
        var job = deposit(cargo, old);
        assertEquals(0, StorageExecution.execute(job, cargo, SETTINGS, BlockPos.ZERO, pos -> null).moved());
        var replacement = store(1, new SimpleContainer(27), auto());
        assertEquals(0, StorageExecution.execute(job, cargo, SETTINGS, BlockPos.ZERO, pos -> replacement).moved());
        assertTrue(old.container().isEmpty());
        assertTrue(replacement.container().isEmpty());
        assertEquals(20, cargo.getItem(0).getCount());
    }

    @Test void changedCargoAndChangedRulesAreRevalidatedAtCommit() {
        var cargo = cargo(new ItemStack(Items.WHEAT, 20));
        var output = store(1, new SimpleContainer(27), auto());
        var job = deposit(cargo, output);
        cargo.setItem(0, new ItemStack(Items.DIAMOND, 5));
        assertEquals(0, StorageExecution.execute(job, cargo, SETTINGS, BlockPos.ZERO, pos -> output).moved());
        assertEquals(5, cargo.getItem(0).getCount());
        cargo.setItem(0, new ItemStack(Items.WHEAT, 20));
        var changed = new StoragePlanner.Store(output.pos(), output.parts(), output.container(), frame(new ItemStack(Items.DIAMOND), 0), output.identities());
        assertEquals(0, StorageExecution.execute(job, cargo, SETTINGS, BlockPos.ZERO, pos -> changed).moved());
        assertEquals(20, cargo.getItem(0).getCount());
    }

    @Test void playerMixingAnAutomaticChestInvalidatesTheOldDecision() {
        var cargo = cargo(new ItemStack(Items.WHEAT, 20));
        var output = store(1, new SimpleContainer(27), auto());
        var job = deposit(cargo, output);
        output.container().setItem(0, new ItemStack(Items.DIAMOND));
        assertEquals(0, StorageExecution.execute(job, cargo, SETTINGS, BlockPos.ZERO, pos -> output).moved());
        assertEquals(20, cargo.getItem(0).getCount());
    }

    @Test void pickupThenDeliveryConservesItemsAndDoesNotDrainOutputsAgain() {
        var cargo = new SimpleContainer(24);
        cargo.setItem(22, new ItemStack(Items.DIAMOND));
        var input = store(1, new SimpleContainer(new ItemStack(Items.WHEAT, 40)), frame(new ItemStack(Items.DIAMOND), 4));
        var output = store(4, new SimpleContainer(27), frame(new ItemStack(Items.WHEAT), 0));
        var stores = List.of(input, output);
        var world = Map.of(input.pos(), input, output.pos(), output);
        var job = StoragePlanner.plan(cargo, stores, SETTINGS, Vec3.ZERO, s -> true).job();
        var pickup = StorageExecution.execute(job, cargo, SETTINGS, BlockPos.ZERO, world::get);
        assertEquals(40, pickup.moved());
        assertNotNull(pickup.next());
        assertTrue(input.container().isEmpty());
        var delivered = StorageExecution.execute(pickup.next(), cargo, SETTINGS, BlockPos.ZERO, world::get);
        assertEquals(40, delivered.moved());
        assertNull(delivered.next());
        assertEquals(40, output.container().getItem(0).getCount());
        assertTrue(cargo.getItem(0).isEmpty());
        assertTrue(cargo.getItem(22).is(Items.DIAMOND));
        assertNull(StoragePlanner.plan(cargo, stores, SETTINGS, Vec3.ZERO, s -> true).job());
    }

    @Test void sourceReclassificationAndFullDestinationPreventPickup() {
        var cargo = new SimpleContainer(24);
        var input = store(1, new SimpleContainer(new ItemStack(Items.WHEAT, 40)), frame(new ItemStack(Items.WHEAT), 4));
        var output = store(4, new SimpleContainer(1), frame(new ItemStack(Items.WHEAT), 0));
        var job = StoragePlanner.plan(cargo, List.of(input, output), SETTINGS, Vec3.ZERO, s -> true).job();
        var changed = new StoragePlanner.Store(input.pos(), input.parts(), input.container(), auto(), input.identities());
        assertEquals(0, StorageExecution.execute(job, cargo, SETTINGS, BlockPos.ZERO, pos -> pos.equals(input.pos()) ? changed : output).moved());
        output.container().setItem(0, new ItemStack(Items.WHEAT, 64));
        assertEquals(0, StorageExecution.execute(job, cargo, SETTINGS, BlockPos.ZERO, pos -> pos.equals(input.pos()) ? input : output).moved());
        assertEquals(40, input.container().getItem(0).getCount());
        assertTrue(cargo.isEmpty());
    }

    @Test void shipmentBypassesFoodReserveWithoutExportingPreviouslyReservedItems() {
        var food = new ItemStack(Items.APPLE, 20);
        food.set(DataComponents.FOOD, new FoodProperties.Builder().nutrition(4).build());
        var cargo = cargo(food.copyWithCount(16));
        var input = store(1, new SimpleContainer(food), frame(new ItemStack(Items.DIAMOND), 4));
        var output = store(4, new SimpleContainer(1), frame(new ItemStack(Items.APPLE), 0));
        var settings = new StorageSettings(true, 16, 1, List.of());
        var job = StoragePlanner.plan(cargo, List.of(input, output), settings, Vec3.ZERO, s -> true).job();
        var pickup = StorageExecution.execute(job, cargo, settings, BlockPos.ZERO, pos -> pos.equals(input.pos()) ? input : output);
        assertEquals(20, pickup.moved());
        assertEquals(20, StorageExecution.execute(pickup.next(), cargo, settings, BlockPos.ZERO, pos -> pos.equals(input.pos()) ? input : output).moved());
        assertEquals(16, cargo.getItem(0).getCount());
        assertEquals(20, output.container().getItem(0).getCount());
    }

    @Test void doubleChestUsesAll54SlotsAndRejectsEitherHalfBeingReplaced() {
        var left = new SimpleContainer(27);
        var right = new SimpleContainer(27);
        for (int i = 0; i < 27; i++) left.setItem(i, new ItemStack(Items.WHEAT, 64));
        var parts = List.of(new BlockPos(1, 0, 0), new BlockPos(2, 0, 0));
        var output = new StoragePlanner.Store(parts.getFirst(), parts, new CompoundContainer(left, right), auto(), List.of(left, right));
        var cargo = cargo(new ItemStack(Items.WHEAT, 20));
        var job = deposit(cargo, output);
        var replaced = new StoragePlanner.Store(parts.getFirst(), parts, output.container(), auto(), List.of(left, new Object()));
        assertEquals(0, StorageExecution.execute(job, cargo, SETTINGS, BlockPos.ZERO, pos -> replaced).moved());
        assertEquals(20, StorageExecution.execute(job, cargo, SETTINGS, BlockPos.ZERO, pos -> output).moved());
        assertEquals(20, right.getItem(0).getCount());
    }

    @Test void workingAreaChangesPreventOldTargetsBeingUsed() {
        var cargo = cargo(new ItemStack(Items.WHEAT, 20));
        var output = store(1, new SimpleContainer(27), auto());
        var job = deposit(cargo, output);
        assertEquals(0, StorageExecution.execute(job, cargo, SETTINGS, new BlockPos(30, 0, 0), pos -> output).moved());
        assertEquals(20, cargo.getItem(0).getCount());
    }
}
