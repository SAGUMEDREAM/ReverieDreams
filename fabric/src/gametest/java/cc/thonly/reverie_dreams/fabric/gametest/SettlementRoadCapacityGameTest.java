package cc.thonly.reverie_dreams.fabric.gametest;

import cc.thonly.reverie_dreams.entity.HumanEntity;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.world.settlement.SettlementPilotData;
import cc.thonly.reverie_dreams.world.settlement.SettlementRoadGraph;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SettlementRoadCapacityGameTest implements CustomTestMethodInvoker {
    private static final int[] TIER_COUNTS = {16, 32, 64};
    private static final int MAX_RESIDENTS = TIER_COUNTS[TIER_COUNTS.length - 1];
    private static final int LANE_SPACING = 3;
    private static final int LANE_LENGTH = 16;
    private static final int WAYPOINT_DISTANCE = 8;

    private static final Identifier START = id("start");
    private static final Identifier WAYPOINT = id("waypoint");
    private static final Identifier POST = id("post");
    private static final Identifier RESIDENT = id("resident");

    @Override
    public void invokeTestMethod(GameTestHelper helper, Method method) throws ReflectiveOperationException {
        method.invoke(this, helper);
    }

    @GameTest(setupTicks = 10, maxTicks = 4800)
    public void sixteenThirtyTwoAndSixtyFourResidentsUseIsolatedElevatedRoads(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(4, 80, 4));
        List<BlockPos> starts = new ArrayList<>();
        List<BlockPos> waypoints = new ArrayList<>();
        List<BlockPos> posts = new ArrayList<>();
        for (int index = 0; index < MAX_RESIDENTS; index++) {
            BlockPos start = origin.offset(index * LANE_SPACING, 0, 0);
            starts.add(start);
            waypoints.add(start.offset(0, 0, WAYPOINT_DISTANCE));
            posts.add(start.offset(0, 0, LANE_LENGTH));
        }

        List<int[]> acquiredChunks = loadAround(level, starts.get(0), posts.get(MAX_RESIDENTS - 1));
        for (int index = 0; index < MAX_RESIDENTS; index++) {
            carveLane(level, starts.get(index), LANE_LENGTH);
        }

        var sequence = helper.startSequence();
        for (int tierIndex = 0; tierIndex < TIER_COUNTS.length; tierIndex++) {
            int residentCount = TIER_COUNTS[tierIndex];
            Tier[] tierHolder = new Tier[1];
            sequence = sequence
                    .thenExecute(() -> tierHolder[0] = startTier(helper, level, residentCount, starts, waypoints, posts))
                    .thenWaitUntil(() -> waitUntilTierCompletes(helper, tierHolder[0]))
                    .thenExecute(() -> finishTier(helper, level, tierHolder[0]))
                    .thenExecute(() -> cleanupTier(helper, level, tierHolder[0]));
        }
        sequence.thenExecute(() -> releaseChunks(level, acquiredChunks))
                .thenSucceed();
    }

    private static Tier startTier(GameTestHelper helper, ServerLevel level, int residentCount,
                                  List<BlockPos> starts, List<BlockPos> waypoints, List<BlockPos> posts) {
        SettlementPilotData data = SettlementPilotData.getOrCreate(level);
        List<Identifier> settlements = new ArrayList<>();
        List<HumanEntity> residents = new ArrayList<>();
        long startTick = helper.getTick();

        for (int index = 0; index < residentCount; index++) {
            BlockPos start = starts.get(index);
            BlockPos waypoint = waypoints.get(index);
            BlockPos post = posts.get(index);
            Identifier settlement = id("capacity_village/" + UUID.randomUUID());
            SettlementPilotData.Layout layout = new SettlementPilotData.Layout(
                    Map.of(START, start, WAYPOINT, waypoint, POST, post),
                    Map.of(RESIDENT, new SettlementPilotData.Duty(
                            SettlementPilotData.Behavior.HOLD_POST, List.of(POST))),
                    Map.of(),
                    List.of(
                            new SettlementRoadGraph.Edge(START, WAYPOINT),
                            new SettlementRoadGraph.Edge(WAYPOINT, POST)));

            helper.assertTrue(layout.sites().size() == 3 && layout.roads().size() == 2,
                    "each capacity lane has a two-segment road");
            helper.assertTrue(start.distSqr(post) >= LANE_LENGTH * LANE_LENGTH,
                    "each capacity lane is at least sixteen blocks long");
            data.putLayout(settlement, layout);
            settlements.add(settlement);

            HumanEntity resident = spawn(helper, start);
            residents.add(resident);
            helper.assertTrue(SettlementPilotData.assign(resident, settlement, RESIDENT),
                    "resident " + index + " joins its isolated lane duty");
        }
        long[] completionTicks = new long[residentCount];
        Arrays.fill(completionTicks, Long.MIN_VALUE);
        return new Tier(residentCount, startTick, List.copyOf(posts), settlements, residents, completionTicks);
    }

    private static void waitUntilTierCompletes(GameTestHelper helper, Tier tier) {
        long tick = helper.getTick();
        int reached = 0;
        List<String> pending = new ArrayList<>();
        for (int index = 0; index < tier.residentCount(); index++) {
            HumanEntity resident = tier.residents().get(index);
            BlockPos post = tier.posts().get(index);
            helper.assertTrue(resident.isAlive(), "capacity resident " + index + " remains alive");
            if (isNear(resident.position(), post)) {
                if (tier.completionTicks()[index] == Long.MIN_VALUE) {
                    tier.completionTicks()[index] = tick - tier.startTick();
                }
                reached++;
            } else {
                pending.add(index + " at " + resident.blockPosition() + " -> " + post);
            }
        }
        helper.assertTrue(reached == tier.residentCount(),
                "all " + tier.residentCount() + " residents receive a pathfinding opportunity and reach isolated posts; pending: "
                        + summarize(pending));
    }

    private static void finishTier(GameTestHelper helper, ServerLevel level, Tier tier) {
        long[] completionTicks = tier.completionTicks().clone();
        Arrays.sort(completionTicks);
        long[] wholeServerTicks = wholeServerTickNanos(helper, level);
        long durationTicks = helper.getTick() - tier.startTick();
        System.out.printf(
                "SETTLEMENT_ROAD_CAPACITY tier=%d residents=%d duration_ticks=%d "
                        + "completion_ticks[p50=%d,p95=%d,p99=%d,max=%d] "
                        + "whole_server_tick_nanos[count=%d,p50=%d,p95=%d,p99=%d] "
                        + "note=unfiltered server samples include other GameTests and non-settlement work; no TPS threshold applied%n",
                tier.residentCount(), tier.residentCount(), durationTicks,
                percentile(completionTicks, 0.50), percentile(completionTicks, 0.95),
                percentile(completionTicks, 0.99), completionTicks[completionTicks.length - 1],
                wholeServerTicks.length, percentile(wholeServerTicks, 0.50),
                percentile(wholeServerTicks, 0.95), percentile(wholeServerTicks, 0.99));
    }

    private static void cleanupTier(GameTestHelper helper, ServerLevel level, Tier tier) {
        SettlementPilotData data = SettlementPilotData.getOrCreate(level);
        for (int index = 0; index < tier.residentCount(); index++) {
            HumanEntity resident = tier.residents().get(index);
            Identifier settlement = tier.settlements().get(index);
            SettlementPilotData.unassign(resident);
            data.removeLayout(settlement);
            helper.assertTrue(data.member(resident.getUUID()) == null,
                    "capacity resident " + index + " does not leak into SavedData");
            helper.assertTrue(data.layout(settlement) == null,
                    "capacity settlement " + index + " does not leak into SavedData");
            resident.discard();
            helper.assertTrue(!resident.isAlive(), "capacity resident " + index + " is discarded");
        }
    }

    private static long[] wholeServerTickNanos(GameTestHelper helper, ServerLevel level) {
        helper.assertTrue(level.getServer().isSameThread(), "tick samples are captured on the server game thread");
        return Arrays.stream(level.getServer().getTickTimesNanos().clone())
                .filter(value -> value > 0)
                .sorted()
                .toArray();
    }

    private static long percentile(long[] sortedValues, double percentile) {
        if (sortedValues.length == 0) return 0L;
        int rank = Math.max(0, (int) Math.ceil(percentile * sortedValues.length) - 1);
        return sortedValues[rank];
    }

    private static List<int[]> loadAround(ServerLevel level, BlockPos... positions) {
        List<int[]> acquiredChunks = new ArrayList<>();
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (BlockPos position : positions) {
            minX = Math.min(minX, position.getX());
            maxX = Math.max(maxX, position.getX());
            minZ = Math.min(minZ, position.getZ());
            maxZ = Math.max(maxZ, position.getZ());
        }
        for (int x = (minX - 24) >> 4; x <= (maxX + 24) >> 4; x++) {
            for (int z = (minZ - 24) >> 4; z <= (maxZ + 24) >> 4; z++) {
                if (level.setChunkForced(x, z, true)) {
                    acquiredChunks.add(new int[]{x, z});
                }
                level.getChunk(x, z);
            }
        }
        return acquiredChunks;
    }

    private static void releaseChunks(ServerLevel level, List<int[]> acquiredChunks) {
        for (int[] chunk : acquiredChunks) {
            level.setChunkForced(chunk[0], chunk[1], false);
        }
    }

    private static HumanEntity spawn(GameTestHelper helper, BlockPos position) {
        ServerLevel level = helper.getLevel();
        HumanEntity resident = RDEntityTypes.HUMAN.get().create(level, EntitySpawnReason.COMMAND);
        resident.snapTo(Vec3.atBottomCenterOf(position));
        helper.assertTrue(level.addFreshEntity(resident), "spawn a capacity pilot resident");
        return resident;
    }

    private static void carveLane(ServerLevel level, BlockPos start, int length) {
        for (int offset = 0; offset <= length; offset++) {
            BlockPos center = start.offset(0, 0, offset);
            openNode(level, center);
            blockNode(level, center.west());
            blockNode(level, center.east());
        }
    }

    private static void openNode(ServerLevel level, BlockPos position) {
        setNode(level, position, Blocks.STONE.defaultBlockState(), Blocks.AIR.defaultBlockState());
    }

    private static void blockNode(ServerLevel level, BlockPos position) {
        setNode(level, position, Blocks.STONE.defaultBlockState(), Blocks.STONE.defaultBlockState());
    }

    private static void setNode(ServerLevel level, BlockPos position, BlockState floor, BlockState column) {
        level.setBlock(position.below(), floor, 3);
        for (int height = 0; height < 3; height++) {
            level.setBlock(position.above(height), column, 3);
        }
    }

    private static boolean isNear(Vec3 position, BlockPos target) {
        return position.distanceToSqr(Vec3.atBottomCenterOf(target)) <= 1.0;
    }

    private static String summarize(List<String> pending) {
        if (pending.isEmpty()) return "none";
        int limit = Math.min(4, pending.size());
        return String.join("; ", pending.subList(0, limit)) + (pending.size() > limit ? "; ..." : "");
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("reverie_dreams_board_client_test", path);
    }

    private record Tier(int residentCount, long startTick, List<BlockPos> posts,
                        List<Identifier> settlements, List<HumanEntity> residents, long[] completionTicks) {
        private Tier {
            posts = List.copyOf(posts);
            settlements = List.copyOf(settlements);
            residents = List.copyOf(residents);
            completionTicks = completionTicks.clone();
        }
    }
}
