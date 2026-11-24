package cc.thonly.reverie_dreams.entity.ai.work;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FurnaceTransferTest {
    @Test void boundedProductionOnlyIssuesTheRequestedInputBatch() {
        var cargo = new SimpleContainer(24); cargo.setItem(0, new ItemStack(Items.COAL, 64));
        var furnace = new SimpleContainer(3);
        assertEquals(2, FurnaceTransfer.insert(cargo, 0, cargo.getItem(0).copy(), furnace, 0, s -> true, 2));
        assertEquals(62, cargo.getItem(0).getCount());
        assertEquals(2, furnace.getItem(0).getCount());
        assertEquals(0, FurnaceTransfer.insert(cargo, 0, cargo.getItem(0).copy(), furnace, 0, s -> true, 0));
    }
    @BeforeAll static void bootstrap() { WorkTestItems.bootstrap(); }
    @Test void cannotFeedEquipmentOrAChangedSourceIntoTheFurnace() {
        var cargo = new SimpleContainer(24); cargo.setItem(18, new ItemStack(Items.IRON_HELMET)); cargo.setItem(22, new ItemStack(Items.IRON_SWORD));
        var furnace = new SimpleContainer(3);
        assertEquals(0, FurnaceTransfer.insert(cargo, 18, cargo.getItem(18).copy(), furnace, 0, s -> true));
        assertEquals(0, FurnaceTransfer.insert(cargo, 22, cargo.getItem(22).copy(), furnace, 0, s -> true));
        cargo.setItem(0, new ItemStack(Items.DIAMOND));
        assertEquals(0, FurnaceTransfer.insert(cargo, 0, new ItemStack(Items.COAL), furnace, 1, s -> true));
        assertTrue(furnace.isEmpty()); assertTrue(cargo.getItem(18).is(Items.IRON_HELMET));
    }
    @Test void validatesFuelAndRecipeAgainAndLimitsTheSelectedSlot() {
        var cargo = new SimpleContainer(24); cargo.setItem(0, new ItemStack(Items.COAL, 32)); var furnace = new SimpleContainer(3);
        assertEquals(0, FurnaceTransfer.insert(cargo, 0, cargo.getItem(0).copy(), furnace, 1, s -> false));
        assertEquals(32, FurnaceTransfer.insert(cargo, 0, cargo.getItem(0).copy(), furnace, 1, s -> s.is(Items.COAL)));
        assertEquals(32, furnace.getItem(1).getCount()); assertTrue(furnace.getItem(0).isEmpty()); assertTrue(furnace.getItem(2).isEmpty());
    }
    @Test void outputAndContainerRemainderCollectionKeepsOverflowInTheFurnace() {
        var cargo = new SimpleContainer(24); for (int i = 0; i < 18; i++) cargo.setItem(i, new ItemStack(Items.STONE, 64));
        cargo.setItem(0, new ItemStack(Items.IRON_INGOT, 62));
        var furnace = new SimpleContainer(3); furnace.setItem(2, new ItemStack(Items.IRON_INGOT, 20)); furnace.setItem(1, new ItemStack(Items.BUCKET));
        assertEquals(2, FurnaceTransfer.collect(furnace, 2, furnace.getItem(2).copy(), cargo));
        assertEquals(18, furnace.getItem(2).getCount()); assertEquals(0, FurnaceTransfer.collect(furnace, 1, furnace.getItem(1).copy(), cargo));
        assertTrue(furnace.getItem(1).is(Items.BUCKET));
        for (int slot = 18; slot < 24; slot++) assertTrue(cargo.getItem(slot).isEmpty());
    }
}
