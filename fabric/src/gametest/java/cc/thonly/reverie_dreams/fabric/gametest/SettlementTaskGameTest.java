package cc.thonly.reverie_dreams.fabric.gametest;

import cc.thonly.reverie_dreams.entity.HumanEntity;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.world.settlement.SettlementPilotData;
import cc.thonly.reverie_dreams.world.settlement.SettlementTask;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SettlementTaskGameTest {
    private static final Identifier DEFAULT_POST = id("task_default_post");
    private static final Identifier TASK_SITE = id("task_visit_site");
    private static final Identifier REST_SITE = id("task_rest_site");
    private static final Identifier RESIDENT = id("task_resident");

    @GameTest(setupTicks = 10, maxTicks = 420)
    public void visitSiteTaskWaitsForWorkSurvivesDangerAndClearsAfterArrival(GameTestHelper helper) {
        prepareFloor(helper, 8);

        var level = helper.getLevel();
        var data = SettlementPilotData.getOrCreate(level);
        Identifier settlement = id("task_village/" + UUID.randomUUID());
        BlockPos rest = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos taskSite = helper.absolutePos(new BlockPos(6, 1, 2));
        BlockPos defaultPost = helper.absolutePos(new BlockPos(2, 1, 6));
        long clock = level.getOverworldClockTime();
        var routine = new SettlementPilotData.Routine(clockAfter(clock, 80), clockAfter(clock, 320), REST_SITE);
        data.putLayout(settlement, new SettlementPilotData.Layout(
                Map.of(REST_SITE, rest, TASK_SITE, taskSite, DEFAULT_POST, defaultPost),
                Map.of(RESIDENT, new SettlementPilotData.Duty(
                        SettlementPilotData.Behavior.HOLD_POST, List.of(DEFAULT_POST))),
                Map.of(RESIDENT, routine)));

        var resident = helper.spawn(RDEntityTypes.HUMAN.get(), new BlockPos(2, 1, 2));
        var bystander = helper.spawn(RDEntityTypes.HUMAN.get(), new BlockPos(6, 1, 6));
        helper.assertTrue(SettlementPilotData.assign(resident, settlement, RESIDENT),
                "the resident joins the scheduled settlement role");
        helper.assertFalse(SettlementPilotData.queueTask(bystander, TASK_SITE),
                "only enrolled residents can receive a task");
        helper.assertFalse(SettlementPilotData.queueTask(resident, id("missing_task_site")),
                "a task must name a site in the resident's layout");
        helper.assertFalse(routine.isWorking(level.getOverworldClockTime()),
                "the task is queued before the resident's work window");
        helper.assertTrue(SettlementPilotData.queueTask(resident, TASK_SITE),
                "the enrolled resident accepts one visit-site task");
        helper.assertFalse(SettlementPilotData.queueTask(resident, TASK_SITE),
                "a resident cannot receive a second active task");

        SettlementTask queued = data.task(resident.getUUID());
        helper.assertTrue(queued != null, "the queued task is saved for the resident");
        helper.assertTrue(queued != null && queued.type() == SettlementTask.Type.VISIT_SITE
                        && queued.settlement().equals(settlement) && queued.role().equals(RESIDENT)
                        && queued.site().equals(TASK_SITE) && queued.position().equals(taskSite),
                "the saved task records the member, type, site, and position");
        SettlementTask restoredTask = roundTrip(data).task(resident.getUUID());
        helper.assertTrue(restoredTask != null && restoredTask.equals(queued),
                "the active task survives an optional-field codec round trip");

        helper.startSequence()
                .thenWaitUntil(() -> {
                    helper.assertTrue(routine.isWorking(level.getOverworldClockTime()),
                            "task movement waits for the resident's work window");
                    helper.assertTrue(data.task(resident.getUUID()) != null,
                            "the task remains queued until work begins");
                })
                .thenExecute(() -> resident.setRemainingFireTicks(80))
                .thenIdle(20)
                .thenExecute(() -> {
                    helper.assertTrue(data.task(resident.getUUID()) != null,
                            "danger pauses the task without discarding it");
                    helper.assertTrue(resident.position().distanceToSqr(Vec3.atBottomCenterOf(taskSite)) > 1.0,
                            "a burning resident does not continue task movement");
                    resident.clearFire();
                })
                .thenWaitUntil(() -> {
                    assertNear(helper, resident.position(), taskSite,
                            "the visit-site task runs before the resident's default post");
                    helper.assertTrue(data.task(resident.getUUID()) == null,
                            "the visit-site task completes after reaching its waypoint");
                })
                .thenExecute(() -> {
                    helper.assertTrue(data.task(resident.getUUID()) == null,
                            "the task is removed after arrival");
                    helper.assertTrue(roundTrip(data).task(resident.getUUID()) == null,
                            "the completed task is not restored from saved data");
                })
                .thenWaitUntil(() -> assertNear(helper, resident.position(), defaultPost,
                        "default hold-post duty resumes after the task completes"))
                .thenExecute(() -> {
                    helper.assertTrue(SettlementPilotData.queueTask(resident, TASK_SITE),
                            "a completed task frees the resident's queue slot");
                    SettlementPilotData.unassign(resident);
                    helper.assertTrue(data.task(resident.getUUID()) == null,
                            "unassignment clears an active task");
                    data.removeLayout(settlement);
                })
                .thenSucceed();
    }

    private static SettlementPilotData roundTrip(SettlementPilotData data) {
        var encoded = SettlementPilotData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
        return SettlementPilotData.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();
    }

    private static void assertNear(GameTestHelper helper, Vec3 position, BlockPos target, String message) {
        helper.assertTrue(position.distanceToSqr(Vec3.atBottomCenterOf(target)) <= 1.0, message);
    }

    private static void prepareFloor(GameTestHelper helper, int size) {
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                helper.setBlock(new BlockPos(x, 0, z), net.minecraft.world.level.block.Blocks.STONE);
            }
        }
    }

    private static int clockAfter(long clock, long ticks) {
        return (int) Math.floorMod(clock + ticks, SettlementPilotData.Routine.DAY);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("reverie_dreams_board_client_test", path);
    }
}
