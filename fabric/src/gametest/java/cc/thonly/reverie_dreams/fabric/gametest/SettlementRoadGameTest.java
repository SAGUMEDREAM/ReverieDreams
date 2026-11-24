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
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SettlementRoadGameTest implements CustomTestMethodInvoker {
    private static final Identifier HUB = id("hub");
    private static final Identifier NORTH_WAY = id("north_way");
    private static final Identifier NORTH_POST = id("north_post");
    private static final Identifier EAST_WAY = id("east_way");
    private static final Identifier EAST_POST = id("east_post");
    private static final Identifier SOUTH_WAY = id("south_way");
    private static final Identifier SOUTH_POST = id("south_post");
    private static final Identifier SHORT_WAY = id("short_way");
    private static final Identifier ALT_WAY = id("alt_way");
    private static final Identifier ALT_SECOND = id("alt_second");
    private static final Identifier POST = id("post");
    private static final Identifier NORTH_RESIDENT = id("north_resident");
    private static final Identifier EAST_RESIDENT = id("east_resident");
    private static final Identifier SOUTH_RESIDENT = id("south_resident");
    private static final Identifier RESIDENT = id("resident");

    @Override
    public void invokeTestMethod(GameTestHelper helper, Method method) throws ReflectiveOperationException {
        method.invoke(this, helper);
    }

    @GameTest(setupTicks = 10, maxTicks = 720)
    public void threeResidentsReachDistantPostsThroughRegisteredRoads(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Identifier settlement = id("road_village/" + UUID.randomUUID());
        BlockPos hub = helper.absolutePos(new BlockPos(4, 24, 4));
        BlockPos northWay = hub.north(6);
        BlockPos northPost = hub.north(15);
        BlockPos eastWay = hub.east(5);
        BlockPos eastPost = hub.east(11);
        BlockPos southWay = hub.south(5);
        BlockPos southPost = hub.south(11);

        loadAround(level, hub, northWay, northPost, eastWay, eastPost, southWay, southPost);
        carvePlus(level, hub, northWay, northPost, eastWay, eastPost, southWay, southPost);

        helper.assertTrue(hub.distSqr(northPost) > 100
                        && hub.distSqr(eastPost) > 100
                        && hub.distSqr(southPost) > 100,
                "all duty posts are more than ten blocks from the shared road hub");
        helper.assertTrue(northPost.distSqr(eastPost) > 100
                        && northPost.distSqr(southPost) > 100
                        && eastPost.distSqr(southPost) > 100,
                "the three duty posts are separated by more than ten blocks");

        SettlementPilotData data = SettlementPilotData.getOrCreate(level);
        data.putLayout(settlement, new SettlementPilotData.Layout(
                Map.of(
                        HUB, hub,
                        NORTH_WAY, northWay,
                        NORTH_POST, northPost,
                        EAST_WAY, eastWay,
                        EAST_POST, eastPost,
                        SOUTH_WAY, southWay,
                        SOUTH_POST, southPost),
                Map.of(
                        NORTH_RESIDENT, new SettlementPilotData.Duty(
                                SettlementPilotData.Behavior.HOLD_POST, List.of(NORTH_POST)),
                        EAST_RESIDENT, new SettlementPilotData.Duty(
                                SettlementPilotData.Behavior.HOLD_POST, List.of(EAST_POST)),
                        SOUTH_RESIDENT, new SettlementPilotData.Duty(
                                SettlementPilotData.Behavior.HOLD_POST, List.of(SOUTH_POST))),
                Map.of(),
                List.of(
                        new SettlementRoadGraph.Edge(HUB, NORTH_WAY),
                        new SettlementRoadGraph.Edge(NORTH_WAY, NORTH_POST),
                        new SettlementRoadGraph.Edge(HUB, EAST_WAY),
                        new SettlementRoadGraph.Edge(EAST_WAY, EAST_POST),
                        new SettlementRoadGraph.Edge(HUB, SOUTH_WAY),
                        new SettlementRoadGraph.Edge(SOUTH_WAY, SOUTH_POST))));

        HumanEntity north = spawn(helper, hub);
        HumanEntity east = spawn(helper, hub.west());
        HumanEntity south = spawn(helper, hub.east());
        helper.assertTrue(SettlementPilotData.assign(north, settlement, NORTH_RESIDENT),
                "the north resident joins its explicit road duty");
        helper.assertTrue(SettlementPilotData.assign(east, settlement, EAST_RESIDENT),
                "the east resident joins its explicit road duty");
        helper.assertTrue(SettlementPilotData.assign(south, settlement, SOUTH_RESIDENT),
                "the south resident joins its explicit road duty");

        helper.startSequence()
                .thenWaitUntil(() -> assertNear(helper, north.position(), northPost,
                        "the north resident reaches its distant post through the registered road"))
                .thenWaitUntil(() -> assertNear(helper, east.position(), eastPost,
                        "the east resident reaches its distant post through the registered road"))
                .thenWaitUntil(() -> assertNear(helper, south.position(), southPost,
                        "the south resident reaches its distant post through the registered road"))
                .thenExecute(() -> {
                    SettlementPilotData.unassign(north);
                    SettlementPilotData.unassign(east);
                    SettlementPilotData.unassign(south);
                    SettlementPilotData.getOrCreate(level).removeLayout(settlement);
                })
                .thenSucceed();
    }

    @GameTest(setupTicks = 10, maxTicks = 720)
    public void blockedShortRoadUsesAlternateAndRecoversAfterClearing(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Identifier settlement = id("blocked_road_village/" + UUID.randomUUID());
        BlockPos hub = helper.absolutePos(new BlockPos(4, 44, 4));
        BlockPos shortWay = hub.east(5);
        BlockPos post = hub.east(11);
        BlockPos altWay = hub.north(5);
        BlockPos altSecond = post.north(5);
        BlockPos altMiddle = altWay.east(5);

        loadAround(level, hub, shortWay, post, altWay, altSecond);
        carveShortAndAlternate(level, hub, shortWay, post, altWay, altSecond);
        blockNode(level, shortWay);

        helper.assertTrue(hub.distSqr(post) > 100, "the short road destination is more than ten blocks away");
        helper.assertTrue(isBlockedNode(level, shortWay),
                "the short road's intermediate node is blocked by a three-block-high wall");

        SettlementPilotData data = SettlementPilotData.getOrCreate(level);
        data.putLayout(settlement, new SettlementPilotData.Layout(
                Map.of(HUB, hub, SHORT_WAY, shortWay, ALT_WAY, altWay, ALT_SECOND, altSecond,
                        id("alt_middle"), altMiddle, POST, post),
                Map.of(RESIDENT, new SettlementPilotData.Duty(
                        SettlementPilotData.Behavior.HOLD_POST, List.of(POST))),
                Map.of(),
                List.of(
                        new SettlementRoadGraph.Edge(HUB, SHORT_WAY),
                        new SettlementRoadGraph.Edge(SHORT_WAY, POST),
                        new SettlementRoadGraph.Edge(HUB, ALT_WAY),
                        new SettlementRoadGraph.Edge(ALT_WAY, id("alt_middle")),
                        new SettlementRoadGraph.Edge(id("alt_middle"), ALT_SECOND),
                        new SettlementRoadGraph.Edge(ALT_SECOND, POST))));

        HumanEntity resident = spawn(helper, hub);
        helper.assertTrue(SettlementPilotData.assign(resident, settlement, RESIDENT),
                "the resident joins the blocked-road duty");

        helper.startSequence()
                .thenWaitUntil(() -> assertNear(helper, resident.position(), post,
                        "the resident uses the alternate registered road while the short road is blocked; at "
                                + resident.position() + ", hub " + hub + ", post " + post))
                .thenExecute(() -> {
                    clearNode(level, shortWay);
                    helper.assertTrue(!isBlockedNode(level, shortWay),
                            "clearing the obstacle reopens the short road's intermediate node");
                    SettlementPilotData.getOrCreate(level).putLayout(settlement,
                            new SettlementPilotData.Layout(
                                    Map.of(HUB, hub, SHORT_WAY, shortWay, ALT_WAY, altWay, ALT_SECOND, altSecond, POST, post),
                                    Map.of(RESIDENT, new SettlementPilotData.Duty(
                                            SettlementPilotData.Behavior.HOLD_POST, List.of(HUB))),
                                    Map.of(),
                                    List.of(
                                            new SettlementRoadGraph.Edge(HUB, SHORT_WAY),
                                            new SettlementRoadGraph.Edge(SHORT_WAY, POST))));
                })
                .thenWaitUntil(() -> assertNear(helper, resident.position(), hub,
                        "the resident returns through the restored short road after the obstacle is cleared; at "
                                + resident.position() + ", hub " + hub + ", short waypoint " + shortWay))
                .thenExecute(() -> {
                    SettlementPilotData.unassign(resident);
                    SettlementPilotData.getOrCreate(level).removeLayout(settlement);
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
        var level = helper.getLevel();
        var resident = RDEntityTypes.HUMAN.get().create(level, EntitySpawnReason.COMMAND);
        resident.snapTo(Vec3.atBottomCenterOf(position));
        helper.assertTrue(level.addFreshEntity(resident), "spawn a road pilot resident");
        return resident;
    }

    private static void carvePlus(ServerLevel level, BlockPos hub, BlockPos northWay, BlockPos northPost,
                                  BlockPos eastWay, BlockPos eastPost, BlockPos southWay, BlockPos southPost) {
        for (int z = northPost.getZ(); z <= southPost.getZ(); z++) {
            for (int dx = -1; dx <= 1; dx++) openNode(level, new BlockPos(hub.getX() + dx, hub.getY(), z));
        }
        for (int x = hub.getX() - 2; x <= eastPost.getX(); x++) {
            for (int dz = -1; dz <= 1; dz++) openNode(level, new BlockPos(x, hub.getY(), hub.getZ() + dz));
        }
    }

    private static void carveShortAndAlternate(ServerLevel level, BlockPos hub, BlockPos shortWay,
                                                BlockPos post, BlockPos altWay, BlockPos altSecond) {
        for (int x = hub.getX(); x <= post.getX(); x++) {
            for (int dz = -1; dz <= 1; dz++) {
                openNode(level, new BlockPos(x, hub.getY(), hub.getZ() + dz));
            }
        }
        for (int z = altWay.getZ(); z <= hub.getZ(); z++) {
            for (int dx = -1; dx <= 1; dx++) {
                openNode(level, new BlockPos(hub.getX() + dx, hub.getY(), z));
            }
        }
        for (int x = hub.getX(); x <= post.getX(); x++) {
            for (int dz = -1; dz <= 1; dz++) {
                openNode(level, new BlockPos(x, hub.getY(), altSecond.getZ() + dz));
            }
        }
        for (int z = altSecond.getZ(); z <= post.getZ(); z++) {
            for (int dx = -1; dx <= 1; dx++) {
                openNode(level, new BlockPos(post.getX() + dx, hub.getY(), z));
            }
        }
    }

    private static void openNode(ServerLevel level, BlockPos position) {
        setNode(level, position, Blocks.STONE.defaultBlockState(), Blocks.AIR.defaultBlockState());
    }

    private static void blockNode(ServerLevel level, BlockPos position) {
        setNode(level, position, Blocks.STONE.defaultBlockState(), Blocks.OBSIDIAN.defaultBlockState());
    }

    private static void clearNode(ServerLevel level, BlockPos position) {
        setNode(level, position, Blocks.STONE.defaultBlockState(), Blocks.AIR.defaultBlockState());
    }

    private static void setNode(ServerLevel level, BlockPos position, BlockState floor, BlockState column) {
        level.setBlock(position.below(), floor, 3);
        for (int i = 0; i < 3; i++) {
            level.setBlock(position.above(i), column, 3);
        }
    }

    private static boolean isBlockedNode(ServerLevel level, BlockPos position) {
        return !level.getBlockState(position).isAir()
                && !level.getBlockState(position.above()).isAir()
                && !level.getBlockState(position.above(2)).isAir();
    }

    private static void assertNear(GameTestHelper helper, Vec3 position, BlockPos target, String message) {
        helper.assertTrue(position.distanceToSqr(Vec3.atBottomCenterOf(target)) <= 1.0, message);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("reverie_dreams_board_client_test", path);
    }
}
