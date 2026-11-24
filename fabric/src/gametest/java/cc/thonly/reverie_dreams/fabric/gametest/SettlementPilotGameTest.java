package cc.thonly.reverie_dreams.fabric.gametest;

import cc.thonly.reverie_dreams.entity.HumanEntity;
import cc.thonly.reverie_dreams.entity.WhiteWolfTengu;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.world.settlement.SettlementPilotData;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SettlementPilotGameTest {
    private static final Identifier POST = id("post");
    private static final Identifier FIRST = id("inspection_start");
    private static final Identifier SECOND = id("inspection_end");
    private static final Identifier GUARD = id("guard");
    private static final Identifier INSPECTOR = id("inspector");
    private static final Identifier RESIDENT = id("resident");
    private static final Identifier WORK = id("work");
    private static final Identifier REST = id("rest");
    private static final Identifier GUARD_POST = id("guard_post");
    private static final Identifier GUARD_REST = id("guard_rest");

    @GameTest(setupTicks = 10, maxTicks = 360)
    public void onlyEnrolledResidentsWorkAndGuardResumesAfterCombatAndReload(GameTestHelper helper) {
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }

        var level = helper.getLevel();
        var data = SettlementPilotData.getOrCreate(level);
        Identifier village = id("test_village/" + UUID.randomUUID());
        Identifier outpost = id("test_outpost/" + UUID.randomUUID());
        BlockPos first = helper.absolutePos(new BlockPos(5, 1, 1));
        BlockPos second = helper.absolutePos(new BlockPos(5, 1, 3));
        BlockPos post = helper.absolutePos(new BlockPos(6, 1, 6));
        BlockPos guardSpawn = new BlockPos(1, 1, 6);
        var human = helper.spawn(RDEntityTypes.HUMAN.get(), new BlockPos(1, 1, 1));
        var guard = helper.spawn(RDEntityTypes.WHITE_WOLF_TENGU.get(), guardSpawn);
        var bystander = helper.spawnWithNoFreeWill(RDEntityTypes.WHITE_WOLF_TENGU.get(), new BlockPos(1, 1, 3));

        data.putLayout(village, new SettlementPilotData.Layout(Map.of(FIRST, first, SECOND, second),
                Map.of(INSPECTOR, new SettlementPilotData.Duty(SettlementPilotData.Behavior.INSPECT_ROUTE, List.of(FIRST, SECOND)))));
        data.putLayout(outpost, new SettlementPilotData.Layout(Map.of(POST, post),
                Map.of(GUARD, new SettlementPilotData.Duty(SettlementPilotData.Behavior.HOLD_POST, List.of(POST)))));

        helper.assertTrue(Math.abs(human.getBbWidth() - 0.6F) < 0.001F
                        && Math.abs(human.getBbHeight() - 1.8F) < 0.001F
                        && Math.abs(human.getEyeHeight() - 1.62F) < 0.001F,
                "human uses the actual player-sized collision box and eye height");
        helper.assertFalse(SettlementPilotData.assign(bystander, outpost, INSPECTOR), "roles must exist in the selected layout");
        helper.assertFalse(SettlementPilotData.hasAssignment(bystander), "an undesignated white wolf stays outside the pilot");
        helper.assertTrue(SettlementPilotData.assign(human, village, INSPECTOR), "human explicitly enrolled without an owner");
        helper.assertTrue(SettlementPilotData.assign(guard, outpost, GUARD), "white wolf explicitly designated as guard");
        helper.assertTrue(!human.isTame() && !guard.isTame(), "assignments do not tame residents");

        var attacker = helper.spawnWithNoFreeWill(EntityType.HUSK, guardSpawn);
        attacker.setNoAi(true);
        float startingHealth = attacker.getHealth();
        var reloaded = new WhiteWolfTengu[1];

        helper.startSequence()
                .thenWaitUntil(() -> assertNear(helper, human.position(), first, "inspector reaches the first site"))
                .thenWaitUntil(() -> assertNear(helper, guard.position(), post, "guard takes the post"))
                .thenIdle(8)
                .thenExecute(() -> {
                    assertNear(helper, guard.position(), post, "guard stays at the post");
                    guard.setTarget(attacker);
                })
                .thenWaitUntil(() -> assertNear(helper, human.position(), second, "inspector advances to the second site"))
                .thenWaitUntil(() -> helper.assertTrue(attacker.getHealth() < startingHealth && guard.isAggressive(),
                        "the original combat goal moves the guard off its post and lands a hit"))
                .thenExecute(() -> {
                    guard.setTarget(null);
                    attacker.discard();
                })
                .thenWaitUntil(() -> assertNear(helper, guard.position(), post, "guard resumes post duty after combat"))
                .thenExecute(() -> {
                    var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
                    helper.assertTrue(guard.save(output), "serialize the guard's entity data");
                    var serialized = SettlementPilotData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
                    var restoredData = SettlementPilotData.CODEC.parse(NbtOps.INSTANCE, serialized).getOrThrow();
                    helper.assertValueEqual(restoredData.member(guard.getUUID()),
                            new SettlementPilotData.Member(outpost, GUARD), "the dimension data retains the guard's role");
                    helper.assertValueEqual(restoredData.member(human.getUUID()),
                            new SettlementPilotData.Member(village, INSPECTOR), "the dimension data retains the inspector's role");

                    guard.discard();
                    var input = TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), output.buildResult());
                    reloaded[0] = RDEntityTypes.WHITE_WOLF_TENGU.get().create(level, EntitySpawnReason.LOAD);
                    helper.assertTrue(reloaded[0] != null, "create the entity from the original registered type");
                    reloaded[0].load(input);
                    reloaded[0].snapTo(Vec3.atBottomCenterOf(helper.absolutePos(guardSpawn)));
                    level.getDataStorage().set(SettlementPilotData.TYPE, restoredData);
                    helper.assertTrue(level.addFreshEntity(reloaded[0]), "load the guard with its original UUID");
                    helper.assertTrue(SettlementPilotData.hasAssignment(reloaded[0]), "loaded entity resolves saved assignment");
                    helper.assertFalse(SettlementPilotData.hasAssignment(bystander), "reload does not opt in other entities");
                })
                .thenWaitUntil(() -> assertNear(helper, reloaded[0].position(), post, "reloaded guard actually walks back to the saved post"))
                .thenExecute(() -> {
                    var restoredData = SettlementPilotData.getOrCreate(level);
                    SettlementPilotData.unassign(reloaded[0]);
                    helper.assertFalse(SettlementPilotData.hasAssignment(reloaded[0]), "explicit unassignment removes the saved role");
                    SettlementPilotData.unassign(human);
                    restoredData.removeLayout(village);
                    restoredData.removeLayout(outpost);
                })
                .thenSucceed();
    }

    @GameTest(setupTicks = 10, maxTicks = 360)
    public void residentChangesWorkAndRestSitesAtRoutineBoundaries(GameTestHelper helper) {
        prepareFloor(helper, 8);

        var level = helper.getLevel();
        var data = SettlementPilotData.getOrCreate(level);
        Identifier settlement = id("routine_village/" + UUID.randomUUID());
        BlockPos humanWork = helper.absolutePos(new BlockPos(6, 1, 2));
        BlockPos humanRest = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos guardPost = helper.absolutePos(new BlockPos(6, 1, 6));
        BlockPos guardRest = helper.absolutePos(new BlockPos(2, 1, 6));
        long clockSnapshot = level.getOverworldClockTime();
        var humanRoutine = new SettlementPilotData.Routine(clockAfter(clockSnapshot, 50), clockAfter(clockSnapshot, 145), REST);
        var guardRoutine = new SettlementPilotData.Routine(clockAfter(clockSnapshot, 145), clockAfter(clockSnapshot, 250), GUARD_REST);
        var resident = helper.spawn(RDEntityTypes.HUMAN.get(), new BlockPos(2, 1, 2));
        var guard = helper.spawn(RDEntityTypes.WHITE_WOLF_TENGU.get(), new BlockPos(2, 1, 8));

        data.putLayout(settlement, new SettlementPilotData.Layout(
                Map.of(WORK, humanWork, REST, humanRest, GUARD_POST, guardPost, GUARD_REST, guardRest),
                Map.of(
                        RESIDENT, new SettlementPilotData.Duty(SettlementPilotData.Behavior.HOLD_POST, List.of(WORK)),
                        GUARD, new SettlementPilotData.Duty(SettlementPilotData.Behavior.HOLD_POST, List.of(GUARD_POST))),
                Map.of(RESIDENT, humanRoutine, GUARD, guardRoutine)));
        helper.assertTrue(SettlementPilotData.assign(resident, settlement, RESIDENT), "resident joins the scheduled pilot role");
        helper.assertTrue(SettlementPilotData.assign(guard, settlement, GUARD), "the designated white wolf joins its guard role");
        helper.assertFalse(resident.isTame() || guard.isTame(), "the assigned residents remain independent");
        helper.assertFalse(humanRoutine.isWorking(level.getOverworldClockTime()), "the human starts before its daytime shift");
        helper.assertFalse(guardRoutine.isWorking(level.getOverworldClockTime()), "the white wolf starts before its nighttime shift");

        helper.startSequence()
                .thenWaitUntil(() -> assertNear(helper, resident.position(), humanRest, "human waits at its rest site"))
                .thenWaitUntil(() -> assertNear(helper, guard.position(), guardRest, "white wolf waits at its separate rest site"))
                .thenWaitUntil(() -> helper.assertTrue(humanRoutine.isWorking(level.getOverworldClockTime()),
                        "the human's first work interval begins"))
                .thenWaitUntil(() -> assertNear(helper, resident.position(), humanWork, "human reaches its work site during its shift"))
                .thenWaitUntil(() -> helper.assertTrue(!humanRoutine.isWorking(level.getOverworldClockTime())
                                && guardRoutine.isWorking(level.getOverworldClockTime()),
                        "the guard's separate shift begins exactly when the human's shift ends"))
                .thenWaitUntil(() -> assertNear(helper, resident.position(), humanRest, "human returns to its rest site after handing off duty"))
                .thenWaitUntil(() -> helper.assertTrue(guardRoutine.isWorking(level.getOverworldClockTime()),
                        "the white wolf's separate guard shift is active"))
                .thenWaitUntil(() -> assertNear(helper, guard.position(), guardPost, "white wolf reaches its guard post during its shift"))
                .thenWaitUntil(() -> helper.assertFalse(guardRoutine.isWorking(level.getOverworldClockTime()),
                        "the white wolf's guard shift ends at its configured boundary"))
                .thenWaitUntil(() -> assertNear(helper, guard.position(), guardRest, "white wolf returns to its own rest site after its shift"))
                .thenExecute(() -> {
                    SettlementPilotData.unassign(resident);
                    SettlementPilotData.unassign(guard);
                    data.removeLayout(settlement);
                })
                .thenSucceed();
    }

    @GameTest(setupTicks = 1, maxTicks = 20)
    public void routineUsesHalfOpenIntervalsAcrossMidnight(GameTestHelper helper) {
        var daytime = new SettlementPilotData.Routine(100, 200, REST);
        var overnightGuard = new SettlementPilotData.Routine(23_900, 100, REST);

        helper.assertFalse(daytime.isWorking(99), "work has not started before the daytime interval");
        helper.assertTrue(daytime.isWorking(100), "work starts on the inclusive boundary");
        helper.assertTrue(daytime.isWorking(199), "work continues through the final tick before its end");
        helper.assertFalse(daytime.isWorking(200), "work ends on the exclusive boundary");
        helper.assertFalse(overnightGuard.isWorking(23_899), "the overnight guard has not started before its shift");
        helper.assertTrue(overnightGuard.isWorking(23_900), "the overnight shift starts on its inclusive boundary");
        helper.assertTrue(overnightGuard.isWorking(23_999), "the overnight guard remains on duty before midnight");
        helper.assertTrue(overnightGuard.isWorking(24_000), "the overnight guard remains on duty at midnight");
        helper.assertTrue(overnightGuard.isWorking(24_099), "the overnight shift includes ticks before its end");
        helper.assertFalse(overnightGuard.isWorking(24_100), "the overnight shift ends on its exclusive boundary");

        helper.startSequence().thenSucceed();
    }

    @GameTest(setupTicks = 10, maxTicks = 360)
    public void sharedBedHasOneOccupantAndInvalidBedUsesRestSite(GameTestHelper helper) {
        prepareFloor(helper, 8);

        var level = helper.getLevel();
        var data = SettlementPilotData.getOrCreate(level);
        Identifier settlement = id("bed_village/" + UUID.randomUUID());
        BlockPos work = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos rest = helper.absolutePos(new BlockPos(6, 1, 6));
        BlockPos spareRest = helper.absolutePos(new BlockPos(2, 1, 6));
        BlockPos bedFootLocal = new BlockPos(4, 1, 4);
        BlockPos bedHead = helper.absolutePos(bedFootLocal.relative(Direction.SOUTH));
        BlockPos invalidBed = helper.absolutePos(new BlockPos(5, 1, 2));
        var footState = Blocks.RED_BED.defaultBlockState()
                .setValue(BedBlock.FACING, Direction.SOUTH)
                .setValue(BedBlock.PART, BedPart.FOOT);
        var headState = footState.setValue(BedBlock.PART, BedPart.HEAD);
        helper.setBlock(bedFootLocal, footState);
        helper.setBlock(bedFootLocal.relative(Direction.SOUTH), headState);

        long clock = level.getOverworldClockTime();
        var routine = new SettlementPilotData.Routine(clockAfter(clock, 2_000), clockAfter(clock, 3_000), REST);
        data.putLayout(settlement, new SettlementPilotData.Layout(
                Map.of(WORK, work, REST, rest, GUARD_REST, spareRest),
                Map.of(RESIDENT, new SettlementPilotData.Duty(SettlementPilotData.Behavior.HOLD_POST, List.of(WORK)),
                        GUARD, new SettlementPilotData.Duty(SettlementPilotData.Behavior.HOLD_POST, List.of(WORK))),
                Map.of(RESIDENT, routine, GUARD,
                        new SettlementPilotData.Routine(routine.workStart(), routine.workEnd(), GUARD_REST))));

        var first = helper.spawn(RDEntityTypes.HUMAN.get(), new BlockPos(2, 1, 4));
        var second = helper.spawn(RDEntityTypes.HUMAN.get(), new BlockPos(6, 1, 4));
        var withoutBed = helper.spawn(RDEntityTypes.HUMAN.get(), new BlockPos(2, 1, 3));
        helper.assertTrue(SettlementPilotData.assign(first, settlement, RESIDENT, bedHead), "first resident receives the shared bed assignment");
        helper.assertTrue(SettlementPilotData.assign(second, settlement, RESIDENT, bedHead), "second resident may share the bed assignment");
        helper.assertTrue(SettlementPilotData.assign(withoutBed, settlement, GUARD, invalidBed), "an invalid bed location remains a valid resident assignment");
        helper.assertFalse(level.getBlockState(bedHead).getValue(BedBlock.OCCUPIED), "assigning a bed does not reserve it before arrival");
        helper.assertTrue(level.getBlockState(bedHead).getBlock() == Blocks.RED_BED
                        && level.getBlockState(bedHead).getValue(BedBlock.PART) == BedPart.HEAD,
                "the configured shared bed exists with the expected head position");

        var attacker = helper.spawnWithNoFreeWill(EntityType.HUSK, new BlockPos(1, 1, 1));
        attacker.setNoAi(true);
        var occupant = new HumanEntity[1];
        var waitingResident = new HumanEntity[1];
        long[] bedFreedAt = {-1};

        helper.startSequence()
                .thenWaitUntil(() -> {
                    int sleepers = (first.isSleeping() ? 1 : 0) + (second.isSleeping() ? 1 : 0);
                    helper.assertTrue(sleepers == 1, "only one resident can claim the shared bed");
                    helper.assertTrue(!withoutBed.isSleeping(), "a resident with no valid bed falls back without sleeping");
                    assertNear(helper, withoutBed.position(), spareRest, "an invalid bed assignment falls back to the configured rest site");
                    if (!first.isSleeping()) {
                        assertNear(helper, first.position(), rest, "the resident who loses the bed contest rests at the fallback site");
                    }
                    if (!second.isSleeping()) {
                        assertNear(helper, second.position(), rest, "the resident who loses the bed contest rests at the fallback site");
                    }
                })
                .thenExecute(() -> {
                    occupant[0] = first.isSleeping() ? first : second;
                    waitingResident[0] = first.isSleeping() ? second : first;
                    helper.assertTrue(level.getBlockState(bedHead).getValue(BedBlock.OCCUPIED), "the bed becomes occupied when its resident arrives");
                    occupant[0].setTarget(attacker);
                })
                .thenWaitUntil(() -> helper.assertFalse(occupant[0].isSleeping(), "an interruption releases the shared bed"))
                .thenExecute(() -> bedFreedAt[0] = level.getGameTime())
                .thenWaitUntil(() -> {
                    helper.assertTrue(level.getGameTime() - bedFreedAt[0] <= 140,
                            "a resident at the fallback retries the newly available bed after its short retry delay");
                    helper.assertTrue(waitingResident[0].isSleeping(), "the waiting resident claims the bed after its occupant leaves");
                })
                .thenExecute(() -> helper.setBlock(bedFootLocal.relative(Direction.SOUTH), Blocks.AIR))
                .thenWaitUntil(() -> helper.assertFalse(waitingResident[0].isSleeping(), "removing a bed wakes its resident"))
                .thenWaitUntil(() -> assertNear(helper, waitingResident[0].position(), rest, "the resident returns to rest after losing the bed"))
                .thenExecute(() -> {
                    occupant[0].setTarget(null);
                    SettlementPilotData.unassign(first);
                    SettlementPilotData.unassign(second);
                    SettlementPilotData.unassign(withoutBed);
                    data.removeLayout(settlement);
                })
                .thenSucceed();
    }

    @GameTest(setupTicks = 10, maxTicks = 360)
    public void dangerWakesSleepingResidentAndCurrentRoutineResumesAfterward(GameTestHelper helper) {
        prepareFloor(helper, 8);

        var level = helper.getLevel();
        var data = SettlementPilotData.getOrCreate(level);
        Identifier settlement = id("wake_village/" + UUID.randomUUID());
        BlockPos work = helper.absolutePos(new BlockPos(6, 1, 2));
        BlockPos rest = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos bedFootLocal = new BlockPos(2, 1, 5);
        BlockPos bedHead = helper.absolutePos(bedFootLocal.relative(Direction.SOUTH));
        var footState = Blocks.RED_BED.defaultBlockState()
                .setValue(BedBlock.FACING, Direction.SOUTH)
                .setValue(BedBlock.PART, BedPart.FOOT);
        helper.setBlock(bedFootLocal, footState);
        helper.setBlock(bedFootLocal.relative(Direction.SOUTH), footState.setValue(BedBlock.PART, BedPart.HEAD));

        long clock = level.getOverworldClockTime();
        var routine = new SettlementPilotData.Routine(clockAfter(clock, 100), clockAfter(clock, 260), REST);
        var resident = helper.spawn(RDEntityTypes.HUMAN.get(), new BlockPos(2, 1, 3));
        data.putLayout(settlement, new SettlementPilotData.Layout(
                Map.of(WORK, work, REST, rest),
                Map.of(RESIDENT, new SettlementPilotData.Duty(SettlementPilotData.Behavior.HOLD_POST, List.of(WORK))),
                Map.of(RESIDENT, routine)));
        helper.assertTrue(SettlementPilotData.assign(resident, settlement, RESIDENT, bedHead), "resident receives a scheduled bed assignment");

        var attacker = helper.spawnWithNoFreeWill(EntityType.HUSK, new BlockPos(6, 1, 6));
        attacker.setNoAi(true);

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(resident.isSleeping(), "resident sleeps while its routine is outside work hours"))
                .thenExecute(() -> resident.setTarget(attacker))
                .thenWaitUntil(() -> helper.assertFalse(resident.isSleeping(), "danger interrupts sleep and wakes the resident"))
                .thenWaitUntil(() -> {
                    helper.assertTrue(routine.isWorking(level.getOverworldClockTime()),
                            "the scheduled work interval begins while danger still interrupts the resident");
                    helper.assertTrue(resident.getTarget() == attacker, "the danger interruption remains active across the schedule boundary");
                })
                .thenExecute(() -> {
                    resident.setTarget(null);
                    attacker.discard();
                })
                .thenWaitUntil(() -> assertNear(helper, resident.position(), work,
                        "after danger clears, the resident follows the current work schedule"))
                .thenExecute(() -> {
                    SettlementPilotData.unassign(resident);
                    data.removeLayout(settlement);
                })
                .thenSucceed();
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

    private static int clockAfter(long clock, long ticks) {
        return (int) Math.floorMod(clock + ticks, SettlementPilotData.Routine.DAY);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("reverie_dreams_board_client_test", path);
    }
}
