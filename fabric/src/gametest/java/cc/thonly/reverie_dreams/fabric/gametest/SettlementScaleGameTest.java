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
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SettlementScaleGameTest implements CustomTestMethodInvoker {
    private static final int RESIDENT_COUNT = 16;
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

    @GameTest(setupTicks = 10, maxTicks = 1800)
    public void sixteenResidentsWalkSixteenBlockIsolatedParallelRoads(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(4, 64, 4));
        List<BlockPos> starts = new ArrayList<>();
        List<BlockPos> waypoints = new ArrayList<>();
        List<BlockPos> posts = new ArrayList<>();
        for (int index = 0; index < RESIDENT_COUNT; index++) {
            BlockPos start = origin.offset(index * LANE_SPACING, 0, 0);
            starts.add(start);
            waypoints.add(start.offset(0, 0, WAYPOINT_DISTANCE));
            posts.add(start.offset(0, 0, LANE_LENGTH));
        }

        loadAround(level, starts.get(0), posts.get(RESIDENT_COUNT - 1));
        for (int index = 0; index < RESIDENT_COUNT; index++) {
            carveLane(level, starts.get(index), LANE_LENGTH);
        }

        SettlementPilotData data = SettlementPilotData.getOrCreate(level);
        List<Identifier> settlements = new ArrayList<>();
        List<HumanEntity> residents = new ArrayList<>();
        long startedAt = level.getGameTime();

        for (int index = 0; index < RESIDENT_COUNT; index++) {
            BlockPos start = starts.get(index);
            BlockPos waypoint = waypoints.get(index);
            BlockPos post = posts.get(index);
            Identifier settlement = id("scale_village/" + UUID.randomUUID());
            SettlementPilotData.Layout layout = new SettlementPilotData.Layout(
                    Map.of(START, start, WAYPOINT, waypoint, POST, post),
                    Map.of(RESIDENT, new SettlementPilotData.Duty(
                            SettlementPilotData.Behavior.HOLD_POST, List.of(POST))),
                    Map.of(),
                    List.of(
                            new SettlementRoadGraph.Edge(START, WAYPOINT),
                            new SettlementRoadGraph.Edge(WAYPOINT, POST)));

            helper.assertTrue(layout.sites().size() == 3 && layout.roads().size() == 2,
                    "each lane has a two-segment road");
            helper.assertTrue(start.distSqr(post) >= LANE_LENGTH * LANE_LENGTH,
                    "each lane is at least sixteen blocks long");
            data.putLayout(settlement, layout);
            settlements.add(settlement);

            HumanEntity resident = spawn(helper, start);
            residents.add(resident);
            helper.assertTrue(SettlementPilotData.assign(resident, settlement, RESIDENT),
                    "the resident joins its isolated lane duty");
        }

        helper.startSequence()
                .thenWaitUntil(() -> {
                    for (int index = 0; index < RESIDENT_COUNT; index++) {
                        assertNear(helper, residents.get(index).position(), posts.get(index),
                                "resident " + index + " reaches its post through the isolated parallel road");
                    }
                })
                .thenExecute(() -> {
                    long completedTicks = level.getGameTime() - startedAt;
                    long averageTickNanos = level.getServer().getAverageTickTimeNanos();
                    System.out.printf(
                            "SETTLEMENT_SCALE %d residents reached their posts in %d ticks (average tick time %d ns)%n",
                            RESIDENT_COUNT, completedTicks, averageTickNanos);
                })
                .thenExecute(() -> {
                    var currentData = SettlementPilotData.getOrCreate(level);
                    for (int index = 0; index < RESIDENT_COUNT; index++) {
                        SettlementPilotData.unassign(residents.get(index));
                        currentData.removeLayout(settlements.get(index));
                        helper.assertTrue(currentData.member(residents.get(index).getUUID()) == null,
                                "resident " + index + " does not leak into SavedData");
                        residents.get(index).discard();
                    }
                })
                .thenSucceed();
    }

    private static void loadAround(ServerLevel level, BlockPos... positions) {
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
                level.setChunkForced(x, z, true);
                level.getChunk(x, z);
            }
        }
    }

    private static HumanEntity spawn(GameTestHelper helper, BlockPos position) {
        ServerLevel level = helper.getLevel();
        HumanEntity resident = RDEntityTypes.HUMAN.get().create(level, EntitySpawnReason.COMMAND);
        resident.snapTo(Vec3.atBottomCenterOf(position));
        helper.assertTrue(level.addFreshEntity(resident), "spawn a scale pilot resident");
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

    private static void assertNear(GameTestHelper helper, Vec3 position, BlockPos target, String message) {
        helper.assertTrue(position.distanceToSqr(Vec3.atBottomCenterOf(target)) <= 1.0, message);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("reverie_dreams_board_client_test", path);
    }
}
