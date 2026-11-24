package cc.thonly.reverie_dreams.entity.ai.work;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class AnimalFeedingTest {
    @BeforeAll static void bootstrap() { WorkTestItems.bootstrap(); }
    @Test void changedFoodOrAnimalStateCannotConsumeOrForceLove() {
        var inventory = new SimpleContainer(24); inventory.setItem(22, new ItemStack(Items.DIAMOND, 3));
        var love = new AtomicInteger();
        assertFalse(AnimalFeeding.feed(inventory, 22, s -> s.is(Items.WHEAT), () -> true, 0, s -> s.shrink(1), love::incrementAndGet));
        inventory.setItem(22, new ItemStack(Items.WHEAT, 3));
        assertFalse(AnimalFeeding.feed(inventory, 22, s -> s.is(Items.WHEAT), () -> false, 0, s -> s.shrink(1), love::incrementAndGet));
        assertEquals(3, inventory.getItem(22).getCount()); assertEquals(0, love.get());
    }
    @Test void respectsReserveAndOnlyRecordsLoveAfterRealConsumption() {
        var inventory = new SimpleContainer(24); inventory.setItem(22, new ItemStack(Items.WHEAT, 3));
        var love = new AtomicInteger();
        assertTrue(AnimalFeeding.feed(inventory, 22, s -> s.is(Items.WHEAT), () -> true, 2, s -> s.shrink(1), love::incrementAndGet));
        assertFalse(AnimalFeeding.feed(inventory, 22, s -> s.is(Items.WHEAT), () -> true, 2, s -> s.shrink(1), love::incrementAndGet));
        assertEquals(2, inventory.getItem(22).getCount()); assertEquals(1, love.get());
        assertFalse(AnimalFeeding.feed(inventory, 22, s -> s.is(Items.WHEAT), () -> true, 0, s -> {}, love::incrementAndGet));
        assertEquals(1, love.get());
    }
    @Test void breedingLimitIncludesAlreadyExpectedOffspring() {
        assertTrue(AnimalFeeding.withinLimit(30, 2, 32));
        assertFalse(AnimalFeeding.withinLimit(30, 4, 32));
        assertFalse(AnimalFeeding.withinLimit(31, 1, 32));
    }
}
