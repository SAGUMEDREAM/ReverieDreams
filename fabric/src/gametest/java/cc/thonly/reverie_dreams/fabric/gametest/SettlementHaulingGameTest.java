package cc.thonly.reverie_dreams.fabric.gametest;

import cc.thonly.reverie_dreams.entity.HumanEntity;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.world.settlement.SettlementHaul;
import cc.thonly.reverie_dreams.world.settlement.SettlementPilotData;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SettlementHaulingGameTest implements CustomTestMethodInvoker {
    private static final Identifier SOURCE = id("haul_source");
    private static final Identifier DESTINATION = id("haul_destination");
    private static final Identifier RESIDENT = id("resident");
    private static final int BATCH = 7;

    @Override
    public void invokeTestMethod(GameTestHelper helper, Method method) throws ReflectiveOperationException {
        method.invoke(this, helper);
    }

    @GameTest(setupTicks = 10, maxTicks = 480)
    public void residentHaulsWholeBatchAndCargoCopyIsDefensive(GameTestHelper helper) {
        var fixture = prepareHaul(helper);
        ItemStack expected = batch();
        fixture.sourceContainer().setItem(0, new ItemStack(Items.PAPER, 1));
        fixture.sourceContainer().setItem(1, expected.copyWithCount(3));
        fixture.sourceContainer().setItem(2, expected.copyWithCount(4));
        helper.assertTrue(queueHaul(fixture, expected), "the enrolled resident accepts the haul");
        SettlementHaul haul = currentHaul(helper, fixture);
        helper.assertTrue(haul.cargo().isEmpty(), "cargo is empty before the resident reaches the source chest");

        helper.startSequence()
                .thenWaitUntil(() -> {
                    SettlementHaul activeHaul = currentHaul(helper, fixture);
                    helper.assertTrue(ItemStack.matches(activeHaul.cargo(), expected),
                            "the resident picks up the complete batch after reaching the source station");
                    helper.assertTrue(fixture.sourceContainer().getItem(0).getCount() == 1
                                    && fixture.sourceContainer().getItem(1).isEmpty() && fixture.sourceContainer().getItem(2).isEmpty(),
                            "pickup aggregates matching slots and leaves incompatible components alone");
                })
                .thenExecute(() -> {
                    ItemStack cargo = currentHaul(helper, fixture).cargo();
                    cargo.setCount(0);
                    helper.assertTrue(ItemStack.matches(currentHaul(helper, fixture).cargo(), expected),
                            "cargo returns a defensive copy of the active batch");
                })
                .thenWaitUntil(() -> helper.assertTrue(fixture.data().haul(fixture.resident().getUUID()) == null,
                        "the haul is removed after delivery"))
                .thenExecute(() -> {
                    helper.assertTrue(fixture.data().haul(fixture.resident().getUUID()) == null,
                            "a completed haul cannot be queried again");
                    assertSingleMatchingStack(helper, fixture.destinationContainer(), expected,
                            "delivery leaves one exact batch in the destination chest");
                    helper.assertTrue(fixture.sourceContainer().getItem(0).getCount() == 1
                                    && fixture.sourceContainer().getItem(1).isEmpty() && fixture.sourceContainer().getItem(2).isEmpty(),
                            "delivery does not duplicate the batch in the source chest");
                })
                .thenExecute(() -> cleanup(fixture))
                .thenSucceed();
    }

    @GameTest(setupTicks = 10, maxTicks = 600)
    public void fullDestinationDoesNotPartiallyDepositAndDangerResumes(GameTestHelper helper) {
        var fixture = prepareHaul(helper);
        ItemStack expected = batch();
        ItemStack blocker = expected.copyWithCount(61);
        fixture.sourceContainer().setItem(0, expected.copy());
        fixture.destinationContainer().setItem(0, blocker);
        for (int slot = 1; slot < fixture.destinationContainer().getContainerSize(); slot++) {
            fixture.destinationContainer().setItem(slot, new ItemStack(Items.STONE, 64));
        }
        helper.assertTrue(queueHaul(fixture, expected), "the enrolled resident accepts the haul to a nearly full chest");

        helper.startSequence()
                .thenWaitUntil(() -> {
                    SettlementHaul activeHaul = currentHaul(helper, fixture);
                    helper.assertTrue(ItemStack.matches(activeHaul.cargo(), expected),
                            "the resident carries the complete batch after pickup");
                    assertNear(helper, fixture.resident().position(), fixture.destinationSite(),
                            "the resident reaches the destination station");
                    helper.assertTrue(ItemStack.matches(fixture.destinationContainer().getItem(0), blocker),
                            "a destination with room for only part of the batch receives no partial deposit");
                })
                .thenIdle(45)
                .thenExecute(() -> {
                    helper.assertTrue(ItemStack.matches(fixture.destinationContainer().getItem(0), blocker),
                            "the resident has time to try the full destination without a partial deposit");
                    fixture.resident().setRemainingFireTicks(100);
                })
                .thenIdle(20)
                .thenExecute(() -> {
                    helper.assertTrue(fixture.resident().isOnFire(), "danger keeps the haul paused");
                    helper.assertTrue(ItemStack.matches(currentHaul(helper, fixture).cargo(), expected),
                            "danger keeps the complete batch in transit");
                    helper.assertTrue(ItemStack.matches(fixture.destinationContainer().getItem(0), blocker),
                            "the paused resident does not partially deposit into the full destination");
                    fixture.destinationContainer().setItem(0, ItemStack.EMPTY);
                })
                .thenIdle(45)
                .thenExecute(() -> {
                    helper.assertTrue(fixture.resident().isOnFire(),
                            "the danger interruption remains active after destination space is restored");
                    helper.assertTrue(ItemStack.matches(currentHaul(helper, fixture).cargo(), expected),
                            "the resident retains the complete batch while danger is active");
                    helper.assertTrue(fixture.destinationContainer().getItem(0).isEmpty(),
                            "the paused haul does not deliver after destination space is restored");
                })
                .thenExecute(() -> {
                    fixture.resident().clearFire();
                })
                .thenWaitUntil(() -> {
                    var resident = fixture.resident();
                    helper.assertTrue(fixture.data().haul(resident.getUUID()) == null,
                            "the haul resumes and completes after danger clears, resident=" + resident.position()
                                    + ", destination=" + fixture.destinationSite() + ", target=" + resident.getTarget()
                                    + ", navigation done=" + resident.getNavigation().isDone() + ", destination slot="
                                    + fixture.destinationContainer().getItem(0));
                })
                .thenExecute(() -> {
                    assertSingleMatchingStack(helper, fixture.destinationContainer(), expected,
                            "the resumed haul delivers the complete original batch");
                    helper.assertTrue(fixture.sourceContainer().isEmpty(),
                            "the resumed haul does not return or duplicate cargo at the source");
                })
                .thenExecute(() -> cleanup(fixture))
                .thenSucceed();
    }

    @GameTest(setupTicks = 10, maxTicks = 600)
    public void destroyedDestinationRetainsCargoAndRestoredChestResumes(GameTestHelper helper) {
        var fixture = prepareHaul(helper);
        ItemStack expected = batch();
        fixture.sourceContainer().setItem(0, expected.copy());
        helper.assertTrue(queueHaul(fixture, expected), "the enrolled resident accepts the haul");

        helper.startSequence()
                .thenWaitUntil(() -> {
                    SettlementHaul activeHaul = currentHaul(helper, fixture);
                    helper.assertTrue(ItemStack.matches(activeHaul.cargo(), expected),
                            "the resident picks up the complete batch before the destination is destroyed");
                    helper.assertTrue(fixture.sourceContainer().isEmpty(), "pickup empties the source chest");
                })
                .thenExecute(() -> helper.destroyBlock(fixture.destinationChestLocal()))
                .thenWaitUntil(() -> helper.assertTrue(
                        helper.getBlockState(fixture.destinationChestLocal()).isAir(),
                        "the destination chest is destroyed"))
                .thenExecute(() -> helper.assertTrue(
                        ItemStack.matches(currentHaul(helper, fixture).cargo(), expected),
                        "destroying the destination retains the complete batch in transit"))
                .thenIdle(20)
                .thenExecute(() -> {
                    helper.assertTrue(ItemStack.matches(currentHaul(helper, fixture).cargo(), expected),
                            "the in-transit batch remains intact while the destination is missing");
                    helper.setBlock(fixture.destinationChestLocal(), Blocks.CHEST);
                })
                .thenWaitUntil(() -> {
                    var restoredChest = helper.getBlockEntity(fixture.destinationChestLocal(), ChestBlockEntity.class);
                    helper.assertTrue(restoredChest != null, "the destination chest is restored in place");
                    helper.assertTrue(restoredChest.isEmpty(), "the restored chest starts empty");
                })
                .thenWaitUntil(() -> helper.assertTrue(fixture.data().haul(fixture.resident().getUUID()) == null,
                        "the haul completes after the same-position chest is restored, resident=" + fixture.resident().position()
                                + ", destination=" + fixture.destinationSite() + ", navigation done="
                                + fixture.resident().getNavigation().isDone()))
                .thenExecute(() -> {
                    var restoredChest = helper.getBlockEntity(fixture.destinationChestLocal(), ChestBlockEntity.class);
                    helper.assertTrue(restoredChest != null, "the delivered chest remains available");
                    assertSingleMatchingStack(helper, restoredChest, expected,
                            "the restored destination receives the complete original batch");
                    helper.assertTrue(fixture.sourceContainer().isEmpty(),
                            "restored delivery does not duplicate cargo at the source");
                })
                .thenExecute(() -> cleanup(fixture))
                .thenSucceed();
    }

    private static HaulFixture prepareHaul(GameTestHelper helper) {
        prepareFloor(helper, 8);
        BlockPos sourceChestLocal = new BlockPos(1, 1, 3);
        BlockPos destinationChestLocal = new BlockPos(6, 1, 3);
        helper.setBlock(sourceChestLocal, Blocks.CHEST);
        helper.setBlock(destinationChestLocal, Blocks.CHEST);

        var level = helper.getLevel();
        var data = SettlementPilotData.getOrCreate(level);
        Identifier settlement = id("haul_village/" + UUID.randomUUID());
        BlockPos sourceSite = helper.absolutePos(new BlockPos(1, 1, 4));
        BlockPos destinationSite = helper.absolutePos(new BlockPos(6, 1, 4));
        // The GameTest runner only guarantees the arena. Match the loaded search area of a nearby player.
        // The shared test world is discarded after the run. Nearby cases can share these tickets.
        for (int x = (Math.min(sourceSite.getX(), destinationSite.getX()) - 24) >> 4;
             x <= (Math.max(sourceSite.getX(), destinationSite.getX()) + 24) >> 4; x++) {
            for (int z = (Math.min(sourceSite.getZ(), destinationSite.getZ()) - 24) >> 4;
                 z <= (Math.max(sourceSite.getZ(), destinationSite.getZ()) + 24) >> 4; z++) {
                level.setChunkForced(x, z, true);
                level.getChunk(x, z);
            }
        }
        data.putLayout(settlement, new SettlementPilotData.Layout(
                Map.of(SOURCE, sourceSite, DESTINATION, destinationSite),
                Map.of(RESIDENT, new SettlementPilotData.Duty(
                        SettlementPilotData.Behavior.HOLD_POST, List.of(SOURCE)))));

        var resident = helper.spawn(RDEntityTypes.HUMAN.get(), new BlockPos(4, 1, 6));
        helper.assertTrue(SettlementPilotData.assign(resident, settlement, RESIDENT),
                "the actual human resident joins the hauling pilot");
        var sourceContainer = helper.getBlockEntity(sourceChestLocal, ChestBlockEntity.class);
        var destinationContainer = helper.getBlockEntity(destinationChestLocal, ChestBlockEntity.class);
        helper.assertTrue(sourceContainer != null && destinationContainer != null,
                "both hauling stations have adjacent vanilla single chests");
        return new HaulFixture(
                level,
                settlement,
                resident,
                helper.absolutePos(sourceChestLocal),
                destinationSite,
                helper.absolutePos(destinationChestLocal),
                destinationChestLocal,
                sourceContainer,
                destinationContainer);
    }

    private static boolean queueHaul(HaulFixture fixture, ItemStack expected) {
        return SettlementPilotData.queueHaul(
                fixture.resident(),
                SOURCE,
                fixture.sourceChestPos(),
                DESTINATION,
                fixture.destinationChestPos(),
                expected.getItem(),
                expected.getCount());
    }

    private static SettlementHaul currentHaul(GameTestHelper helper, HaulFixture fixture) {
        SettlementHaul haul = fixture.data().haul(fixture.resident().getUUID());
        helper.assertTrue(haul != null, "the resident has an active haul");
        return haul;
    }

    private static void cleanup(HaulFixture fixture) {
        SettlementPilotData.unassign(fixture.resident());
        fixture.data().removeLayout(fixture.settlement());
    }

    private static ItemStack batch() {
        ItemStack stack = new ItemStack(Items.PAPER, BATCH);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("2B sealed manifest"));
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString("batch", "2b"));
        return stack;
    }

    private static void assertSingleMatchingStack(GameTestHelper helper, ChestBlockEntity chest,
                                                   ItemStack expected, String message) {
        ItemStack delivered = ItemStack.EMPTY;
        int matchingSlots = 0;
        int matchingCount = 0;
        for (int slot = 0; slot < chest.getContainerSize(); slot++) {
            ItemStack stack = chest.getItem(slot);
            if (ItemStack.isSameItemSameComponents(stack, expected)) {
                matchingSlots++;
                matchingCount += stack.getCount();
                delivered = stack;
            }
        }
        helper.assertTrue(matchingSlots == 1 && matchingCount == expected.getCount(),
                message + " (one stack with the full count)");
        helper.assertTrue(ItemStack.matches(delivered, expected), message + " (exact components)");
    }

    private static void assertNear(GameTestHelper helper, Vec3 position, BlockPos target, String message) {
        helper.assertTrue(position.distanceToSqr(Vec3.atBottomCenterOf(target)) <= 1.0, message);
    }

    private static void prepareFloor(GameTestHelper helper, int size) {
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("reverie_dreams_board_client_test", path);
    }

    private record HaulFixture(
            ServerLevel level,
            Identifier settlement,
            HumanEntity resident,
            BlockPos sourceChestPos,
            BlockPos destinationSite,
            BlockPos destinationChestPos,
            BlockPos destinationChestLocal,
            ChestBlockEntity sourceContainer,
            ChestBlockEntity destinationContainer) {
        SettlementPilotData data() {
            // The existing pilot reload test replaces the dimension's SavedData during this batch.
            return SettlementPilotData.getOrCreate(this.level);
        }
    }
}
