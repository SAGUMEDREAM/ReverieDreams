package cc.thonly.reverie_dreams.entity.ai.work;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.world.level.block.state.BlockState;
import static org.junit.jupiter.api.Assertions.*;

class FarmTransactionTest {
    @Test void everyVanillaSeedCropReplantsItsOwnBlockEvenWhenOtherSeedsAreAvailable() {
        var crops = List.of(Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS);
        var seeds = List.of(Items.WHEAT_SEEDS, Items.CARROT, Items.POTATO, Items.BEETROOT_SEEDS);
        for (int i = 0; i < crops.size(); i++) {
            var crop = (CropBlock) crops.get(i);
            var seed = seeds.get(i);
            var inventory = new SimpleContainer(24);
            inventory.setItem(0, new ItemStack(Items.WHEAT_SEEDS, 32));
            inventory.setItem(1, new ItemStack(Items.CARROT, 32));
            var young = crop.getStateForAge(0);
            assertEquals(FarmTransaction.Result.SUCCESS, FarmTransaction.harvest(crop.getStateForAge(crop.getMaxAge()), young,
                    new ItemStack(seed), inventory, List.of(new ItemStack(seed, 2)), (a,b) -> {
                        assertSame(crop, b.getBlock()); assertSame(young, b); return true;
                    }, s -> { assertTrue(s.is(seed)); assertEquals(1, s.getCount()); }));
            assertEquals(32, inventory.getItem(0).getCount()); assertEquals(32, inventory.getItem(1).getCount());
        }
    }
    @BeforeAll static void bootstrap() { WorkTestItems.bootstrap(); }
    private final BlockState mature = Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7);
    private final BlockState young = Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 0);

    @Test void replantsTheExactCropAndReservesItsOwnLootSeed() {
        var cargo = new SimpleContainer(24);
        cargo.setItem(0, new ItemStack(Items.CARROT, 32)); cargo.setItem(22, new ItemStack(Items.POTATO, 10));
        var delivered = new ArrayList<ItemStack>();
        var result = FarmTransaction.harvest(mature, young, new ItemStack(Items.WHEAT_SEEDS), cargo,
                List.of(new ItemStack(Items.WHEAT, 1), new ItemStack(Items.WHEAT_SEEDS, 3)),
                (old, next) -> { assertSame(Blocks.WHEAT, next.getBlock()); assertEquals(0, next.getValue(CropBlock.AGE)); return true; }, delivered::add);
        assertEquals(FarmTransaction.Result.SUCCESS, result);
        assertEquals(2, delivered.stream().filter(s -> s.is(Items.WHEAT_SEEDS)).mapToInt(ItemStack::getCount).sum());
        assertEquals(1, delivered.stream().filter(s -> s.is(Items.WHEAT)).mapToInt(ItemStack::getCount).sum());
        assertEquals(32, cargo.getItem(0).getCount()); assertEquals(10, cargo.getItem(22).getCount());
    }
    @Test void neverSubstitutesDifferentSeedsOrDifferentCrops() {
        var cargo = new SimpleContainer(new ItemStack(Items.CARROT, 32));
        var called = new AtomicBoolean();
        assertEquals(FarmTransaction.Result.WRONG_CROP, FarmTransaction.harvest(mature, young, new ItemStack(Items.CARROT), cargo, List.of(),
                (a, b) -> { called.set(true); return true; }, s -> fail()));
        assertEquals(FarmTransaction.Result.WRONG_CROP, FarmTransaction.harvest(mature, Blocks.CARROTS.defaultBlockState(), new ItemStack(Items.WHEAT_SEEDS), cargo, List.of(),
                (a, b) -> { called.set(true); return true; }, s -> fail()));
        assertFalse(called.get()); assertEquals(32, cargo.getItem(0).getCount());
    }
    @Test void missingMatchingSeedsLeavesTheMatureCropAndOtherSeedsUntouched() {
        var cargo = new SimpleContainer(new ItemStack(Items.CARROT, 32));
        assertEquals(FarmTransaction.Result.NO_SEED, FarmTransaction.harvest(mature, young, new ItemStack(Items.WHEAT_SEEDS), cargo,
                List.of(new ItemStack(Items.WHEAT)), (a, b) -> { fail("Must not break without a matching seed"); return false; }, s -> fail()));
        assertEquals(32, cargo.getItem(0).getCount());
    }
    @Test void refusedWriteRestoresReservedSeedAndNeverProducesDrops() {
        var cargo = new SimpleContainer(new ItemStack(Items.WHEAT_SEEDS, 1));
        var deliveries = new ArrayList<ItemStack>();
        for (int attempt = 0; attempt < 8; attempt++) assertEquals(FarmTransaction.Result.REFUSED,
                FarmTransaction.harvest(mature, young, new ItemStack(Items.WHEAT_SEEDS), cargo, List.of(new ItemStack(Items.WHEAT, 3)), (a, b) -> false, deliveries::add));
        assertEquals(1, cargo.getItem(0).getCount()); assertTrue(deliveries.isEmpty());
    }
    @Test void refusedWriteAlsoKeepsLootSeedsUnmaterialized() {
        var cargo = new SimpleContainer(24);
        var deliveries = new ArrayList<ItemStack>();
        assertEquals(FarmTransaction.Result.REFUSED, FarmTransaction.harvest(mature, young, new ItemStack(Items.WHEAT_SEEDS), cargo,
                List.of(new ItemStack(Items.WHEAT_SEEDS, 5)), (a, b) -> false, deliveries::add));
        assertTrue(cargo.isEmpty()); assertTrue(deliveries.isEmpty());
    }
    @Test void onlyOneCorrespondingInventorySeedIsSpentAfterSuccess() {
        var cargo = new SimpleContainer(24); cargo.setItem(0, new ItemStack(Items.WHEAT_SEEDS, 4)); cargo.setItem(1, new ItemStack(Items.CARROT, 9));
        assertEquals(FarmTransaction.Result.SUCCESS, FarmTransaction.harvest(mature, young, new ItemStack(Items.WHEAT_SEEDS), cargo,
                List.of(new ItemStack(Items.WHEAT)), (a, b) -> true, s -> {}));
        assertEquals(3, cargo.getItem(0).getCount()); assertEquals(9, cargo.getItem(1).getCount());
    }
    @Test void exactMatchingHandSeedCanBeUsedWithoutTouchingTheOtherHand() {
        var cargo = new SimpleContainer(24); cargo.setItem(22, new ItemStack(Items.WHEAT_SEEDS, 2)); cargo.setItem(23, new ItemStack(Items.CARROT, 9));
        assertEquals(FarmTransaction.Result.SUCCESS, FarmTransaction.harvest(mature, young, new ItemStack(Items.WHEAT_SEEDS), cargo,
                List.of(new ItemStack(Items.WHEAT)), (a, b) -> true, s -> {}));
        assertEquals(1, cargo.getItem(22).getCount()); assertEquals(9, cargo.getItem(23).getCount());
    }
    @Test void emptyPlotPlantingDoesNotChargeForRefusedWritesOrChangedSources() {
        var cargo = new SimpleContainer(new ItemStack(Items.WHEAT_SEEDS, 3));
        assertFalse(FarmTransaction.plant(new ItemStack(Items.WHEAT_SEEDS), cargo, 0, () -> false));
        assertFalse(FarmTransaction.plant(new ItemStack(Items.CARROT), cargo, 0, () -> { fail(); return true; }));
        assertEquals(3, cargo.getItem(0).getCount());
        assertTrue(FarmTransaction.plant(new ItemStack(Items.WHEAT_SEEDS), cargo, 0, () -> true));
        assertEquals(2, cargo.getItem(0).getCount());
    }
}
