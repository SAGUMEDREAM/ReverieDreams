package cc.thonly.reverie_dreams.block.decor;

import cc.thonly.reverie_dreams.block.base.SeatBlock;
import cc.thonly.reverie_dreams.entity.CushionSeatEntity;
import cc.thonly.reverie_dreams.entity.SeatEntity;
import com.google.common.collect.ImmutableList;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.function.Predicate;
import com.sun.management.ThreadMXBean;
import org.junit.jupiter.api.Assumptions;
import static org.junit.jupiter.api.Assertions.*;

class JapaneseInteriorTest {
    private static Unsafe unsafe;
    private static TatamiBlock thin, full;
    private static ChabudaiBlock table;
    private static ZabutonBlock cushion;
    private static TeaSetBlock tea;
    private static volatile VoxelShape shapeQueryResult;

    @BeforeAll static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
        var field = Unsafe.class.getDeclaredField("theUnsafe"); field.setAccessible(true); unsafe = (Unsafe) field.get(null);
        var holders = MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
        var frozen = MappedRegistry.class.getDeclaredField("frozen");
        holders.setAccessible(true); frozen.setAccessible(true);
        Object previous = holders.get(BuiltInRegistries.BLOCK);
        boolean wasFrozen = frozen.getBoolean(BuiltInRegistries.BLOCK);
        try {
            frozen.setBoolean(BuiltInRegistries.BLOCK, false); holders.set(BuiltInRegistries.BLOCK, new IdentityHashMap<>());
            thin = new TatamiBlock(properties("mat"));
            full = new TatamiBlock(properties("floor"), Shapes.block(), true, false);
            table = new ChabudaiBlock(properties("table")); cushion = new ZabutonBlock(properties("cushion"));
            tea = new TeaSetBlock(properties("tea"));
        } finally { holders.set(BuiltInRegistries.BLOCK, previous); frozen.setBoolean(BuiltInRegistries.BLOCK, wasFrozen); }
    }
    private static BlockBehaviour.Properties properties(String id) {
        return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, Identifier.parse("interior_test:" + id)));
    }
    private static FixtureWorld world() throws Exception {
        FixtureWorld world = (FixtureWorld) unsafe.allocateInstance(FixtureWorld.class);
        world.states = new HashMap<>(); world.entities = new ArrayList<>(); return world;
    }

    @Test void floorConnectsAcrossAnEntireRoomWithoutTheTableGroupLimit() throws Exception {
        FixtureWorld world = world();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 5; z++) world.states.put(new BlockPos(x, 0, z), thin.defaultBlockState());
        for (int x = 0; x < 8; x++) for (int z = 0; z < 5; z++) {
            BlockState state = thin.withConnections(thin.defaultBlockState(), world, new BlockPos(x, 0, z));
            assertEquals(z > 0, state.getValue(TatamiBlock.NORTH)); assertEquals(x < 7, state.getValue(TatamiBlock.EAST));
            assertEquals(z < 4, state.getValue(TatamiBlock.SOUTH)); assertEquals(x > 0, state.getValue(TatamiBlock.WEST));
            assertEquals(x < 7 && z > 0, state.getValue(TatamiBlock.NORTH_EAST));
        }
    }
    @Test void missingDiagonalCreatesAnInnerCornerAndReplacingItClosesThatCorner() throws Exception {
        FixtureWorld world = world(); BlockPos center = BlockPos.ZERO;
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) world.states.put(new BlockPos(x, 0, z), thin.defaultBlockState());
        BlockPos missing = new BlockPos(1, 0, -1); world.states.remove(missing);
        BlockState open = thin.withConnections(thin.defaultBlockState(), world, center);
        assertTrue(open.getValue(TatamiBlock.NORTH)); assertTrue(open.getValue(TatamiBlock.EAST));
        assertFalse(open.getValue(TatamiBlock.NORTH_EAST));
        world.states.put(missing, thin.defaultBlockState());
        assertTrue(thin.withConnections(open, world, center).getValue(TatamiBlock.NORTH_EAST));
        world.states.remove(center.east());
        BlockState removed = thin.withConnections(open, world, center);
        assertFalse(removed.getValue(TatamiBlock.EAST)); assertFalse(removed.getValue(TatamiBlock.NORTH_EAST));
    }
    @Test void fullAndThinMatsAndDifferentLevelsDoNotJoin() throws Exception {
        FixtureWorld world = world();
        world.states.put(BlockPos.ZERO.north(), full.defaultBlockState());
        world.states.put(BlockPos.ZERO.east().above(), thin.defaultBlockState());
        BlockState state = thin.withConnections(thin.defaultBlockState(), world, BlockPos.ZERO);
        assertFalse(state.getValue(TatamiBlock.NORTH)); assertFalse(state.getValue(TatamiBlock.EAST));
    }
    @Test void rotationsAndMirrorsMoveConnectionsAndWeaveTogether() {
        BlockState state = thin.defaultBlockState().setValue(TatamiBlock.NORTH, true).setValue(TatamiBlock.NORTH_EAST, true);
        BlockState rotated = state.rotate(Rotation.CLOCKWISE_90);
        assertTrue(rotated.getValue(TatamiBlock.EAST)); assertTrue(rotated.getValue(TatamiBlock.SOUTH_EAST));
        assertEquals(Direction.Axis.X, rotated.getValue(TatamiBlock.AXIS));
        assertEquals(state, rotated.rotate(Rotation.COUNTERCLOCKWISE_90));
        BlockState mirrored = state.mirror(Mirror.LEFT_RIGHT);
        assertTrue(mirrored.getValue(TatamiBlock.SOUTH)); assertTrue(mirrored.getValue(TatamiBlock.SOUTH_EAST));
    }
    @Test void tableStillStopsJoiningAfterTwoBlocks() throws Exception {
        FixtureWorld world = world(); FixtureContext context = (FixtureContext) unsafe.allocateInstance(FixtureContext.class); context.world = world;
        for (int x = 0; x < 3; x++) {
            context.pos = new BlockPos(x, 0, 0);
            BlockState state = table.getStateForPlacement(context); assertNotNull(state);
            world.states.put(context.pos, state); table.onPlace(state, world, context.pos, Blocks.AIR.defaultBlockState(), false);
        }
        assertTrue(world.getBlockState(BlockPos.ZERO).getValue(TatamiBlock.EAST));
        assertTrue(world.getBlockState(new BlockPos(1, 0, 0)).getValue(TatamiBlock.WEST));
        assertFalse(world.getBlockState(new BlockPos(1, 0, 0)).getValue(TatamiBlock.EAST));
        assertFalse(world.getBlockState(new BlockPos(2, 0, 0)).getValue(TatamiBlock.WEST));
        assertFalse(table.defaultBlockState().hasProperty(TatamiBlock.AXIS));
    }
    @Test void tableLeavesSpaceBelowTheMiddleAndOnlyKeepsOuterLegs() throws Exception {
        FixtureWorld world = world();
        for (int mask = 0; mask < 16; mask++) {
            BlockState state = table.defaultBlockState().setValue(TatamiBlock.NORTH, (mask & 1) != 0)
                    .setValue(TatamiBlock.EAST, (mask & 2) != 0).setValue(TatamiBlock.SOUTH, (mask & 4) != 0)
                    .setValue(TatamiBlock.WEST, (mask & 8) != 0);
            var shape = state.getCollisionShape(world, BlockPos.ZERO);
            assertFalse(shape.toAabbs().stream().anyMatch(box -> box.contains(.5, .1, .5)));
            assertEquals((mask & 9) == 0, shape.toAabbs().stream().anyMatch(box -> box.contains(.125, .1, .125)));
            assertTrue(shape.max(Direction.Axis.Y) <= .5);
            assertTrue(shape.toAabbs().stream().anyMatch(box -> box.contains(.5, 7.8 / 16, .5)));
        }
    }
    @Test void tablePartsMeetWithoutOverlappingAtAnyConnectionMask() {
        for (int mask = 0; mask < 16; mask++) {
            var parts = ChabudaiBlock.parts(mask);
            for (int i = 0; i < parts.size(); i++) for (int j = i + 1; j < parts.size(); j++) {
                var a = parts.get(i); var b = parts.get(j);
                boolean overlaps = Math.min(a.x2(), b.x2()) > Math.max(a.x1(), b.x1())
                        && Math.min(a.y2(), b.y2()) > Math.max(a.y1(), b.y1())
                        && Math.min(a.z2(), b.z2()) > Math.max(a.z1(), b.z1());
                assertFalse(overlaps, "Intersecting table parts at mask " + mask + ": " + i + ", " + j);
            }
        }
    }
    @Test void cushionRidingPointMatchesItsVisibleTopAndOutline() throws Exception {
        FixtureWorld world = world(); BlockState state = cushion.defaultBlockState();
        assertEquals(1.0 / 16, state.getShape(world, BlockPos.ZERO).max(Direction.Axis.Y));
        assertEquals(1.0 / 16, state.getCollisionShape(world, BlockPos.ZERO).max(Direction.Axis.Y));
        CushionSeatEntity seat = (CushionSeatEntity) unsafe.allocateInstance(CushionSeatEntity.class);
        Vec3 origin = new Vec3(3.5, 10 + cushion.seatHeight(), 7.5);
        set(seat, "position", origin);
        assertEquals(origin, seat.getPassengerRidingPosition(null));
        assertEquals(10 + state.getCollisionShape(world, BlockPos.ZERO).max(Direction.Axis.Y), seat.getPassengerRidingPosition(null).y);
    }
    @Test void adjacentOrEmptySeatsDoNotKeepAnUnrelatedCushionOccupied() throws Exception {
        FixtureWorld world = world(); SeatEntity neighboring = seatAt(new BlockPos(1, 0, 0), true);
        world.entities.add(neighboring); assertFalse(SeatBlock.hasSeatEntity(world, BlockPos.ZERO));
        SeatEntity ownEmpty = seatAt(BlockPos.ZERO, false); world.entities.add(ownEmpty);
        assertFalse(SeatBlock.hasSeatEntity(world, BlockPos.ZERO));
        set(ownEmpty, "passengers", ImmutableList.of(neighboring));
        assertTrue(SeatBlock.hasSeatEntity(world, BlockPos.ZERO));
    }
    @Test void teaSetIsSupportedOnASolidSurfaceAndStaysAtTheTableSurface() throws Exception {
        FixtureWorld world = world();
        assertFalse(tea.canSurvive(tea.defaultBlockState(), world, BlockPos.ZERO));
        world.states.put(BlockPos.ZERO.below(), Blocks.STONE.defaultBlockState());
        assertTrue(tea.canSurvive(tea.defaultBlockState(), world, BlockPos.ZERO));
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            BlockState loose = tea.defaultBlockState().setValue(TeaSetBlock.FACING, facing);
            assertEquals(6.5 / 16, loose.getShape(world, BlockPos.ZERO).max(Direction.Axis.Y));
            assertTrue(loose.getCollisionShape(world, BlockPos.ZERO).isEmpty());
            BlockState decorated = table.defaultBlockState().setValue(ChabudaiBlock.HAS_TEA_SET, true)
                    .setValue(ChabudaiBlock.TEA_FACING, facing);
            assertEquals(ChabudaiBlock.TOP_SURFACE + 6.5 / 16, decorated.getShape(world, BlockPos.ZERO).max(Direction.Axis.Y), 1e-9);
            assertSame(table.defaultBlockState().getCollisionShape(world, BlockPos.ZERO), decorated.getCollisionShape(world, BlockPos.ZERO));
        }
    }
    @Test void changingATableConnectionKeepsItsTeaSetAndDirection() throws Exception {
        FixtureWorld world = world();
        world.states.put(BlockPos.ZERO, table.defaultBlockState().setValue(ChabudaiBlock.HAS_TEA_SET, true)
                .setValue(ChabudaiBlock.TEA_FACING, Direction.WEST));
        FixtureContext context = (FixtureContext) unsafe.allocateInstance(FixtureContext.class);
        context.world = world; context.pos = BlockPos.ZERO.east();
        BlockState neighbor = table.getStateForPlacement(context); assertNotNull(neighbor);
        world.states.put(context.pos, neighbor); table.onPlace(neighbor, world, context.pos, Blocks.AIR.defaultBlockState(), false);
        BlockState decorated = world.getBlockState(BlockPos.ZERO);
        assertTrue(decorated.getValue(TatamiBlock.EAST));
        assertTrue(decorated.getValue(ChabudaiBlock.HAS_TEA_SET));
        assertEquals(Direction.WEST, decorated.getValue(ChabudaiBlock.TEA_FACING));
    }
    @Test void teaInteractionOutlinesKeepExactGeometryAndEdgesInEveryDirection() {
        VoxelShape visible = Shapes.empty();
        for (TeaSetBlock.Part part : TeaSetBlock.parts()) visible = Shapes.or(visible, part.shape());
        var rotated = Shapes.rotateHorizontal(visible);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            VoxelShape outline = TeaSetBlock.outline(facing);
            assertFalse(Shapes.joinIsNotEmpty(rotated.get(facing), outline, BooleanOp.NOT_SAME), facing.toString());
            assertEquals(edgesOf(rotated.get(facing)), edgesOf(outline));
            for (Direction.Axis axis : Direction.Axis.values()) {
                assertEquals(rotated.get(facing).min(axis), outline.min(axis), 1e-9);
                assertEquals(rotated.get(facing).max(axis), outline.max(axis), 1e-9);
            }
        }
    }

    @Test void decoratedTableOutlinesKeepTheVesselsSelectableAndTheSpaceBelowClear() throws Exception {
        FixtureWorld world = world();
        for (BlockState state : table.getStateDefinition().getPossibleStates()) {
            if (!state.getValue(ChabudaiBlock.HAS_TEA_SET)) continue;
            VoxelShape outline = state.getShape(world, BlockPos.ZERO);
            VoxelShape teaOutline = TeaSetBlock.outline(state.getValue(ChabudaiBlock.TEA_FACING))
                    .move(0, ChabudaiBlock.TOP_SURFACE, 0);
            assertFalse(Shapes.joinIsNotEmpty(teaOutline, outline, BooleanOp.ONLY_FIRST));
            VoxelShape bareCollision = state.setValue(ChabudaiBlock.HAS_TEA_SET, false)
                    .getCollisionShape(world, BlockPos.ZERO);
            VoxelShape expected = Shapes.or(bareCollision, teaOutline);
            assertFalse(Shapes.joinIsNotEmpty(expected, outline, BooleanOp.NOT_SAME));
            assertEquals(edgesOf(expected), edgesOf(outline));
            assertEquals(expected.toAabbs(), outline.toAabbs());
            assertFalse(Shapes.joinIsNotEmpty(bareCollision, state.getCollisionShape(world, BlockPos.ZERO), BooleanOp.NOT_SAME));
            outline.toAabbs().clear();
            assertNotNull(outline.clip(new Vec3(.5, 2, .5), new Vec3(.5, 0, .5), BlockPos.ZERO));
            assertNull(outline.clip(new Vec3(-1, .1, .5), new Vec3(2, .1, .5), BlockPos.ZERO));
        }
    }

    @Test void targetingShapeQueriesDoNotAllocateVoxelMeshesEveryFrame() throws Exception {
        var vm = ManagementFactory.getThreadMXBean();
        Assumptions.assumeTrue(vm instanceof ThreadMXBean);
        var allocations = (ThreadMXBean) vm;
        Assumptions.assumeTrue(allocations.isThreadAllocatedMemorySupported());
        if (!allocations.isThreadAllocatedMemoryEnabled()) allocations.setThreadAllocatedMemoryEnabled(true);
        FixtureWorld world = world();
        BlockState[] decorated = table.getStateDefinition().getPossibleStates().stream()
                .filter(state -> state.getValue(ChabudaiBlock.HAS_TEA_SET)).toArray(BlockState[]::new);
        CollisionContext context = CollisionContext.empty();
        for (int warmup = 0; warmup < 4; warmup++) for (BlockState state : decorated) {
            shapeQueryResult = state.getShape(world, BlockPos.ZERO, context);
            shapeQueryResult = state.getVisualShape(world, BlockPos.ZERO, context);
        }
        long thread = Thread.currentThread().threadId();
        long before = allocations.getThreadAllocatedBytes(thread);
        for (BlockState state : decorated) {
            shapeQueryResult = state.getShape(world, BlockPos.ZERO, context);
            shapeQueryResult = state.getVisualShape(world, BlockPos.ZERO, context);
        }
        long allocated = allocations.getThreadAllocatedBytes(thread) - before;
        // Covers selection/camera queries for every connection mask and tea facing.
        assertTrue(allocated <= 64 * 1024, () -> "Shape queries allocated " + allocated + " bytes");
    }

    private static Set<List<Double>> edgesOf(VoxelShape shape) {
        Set<List<Double>> edges = new HashSet<>();
        shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> edges.add(List.of(x1, y1, z1, x2, y2, z2)));
        return edges;
    }

    private static void set(Object target, String name, Object value) throws Exception {
        var field = Entity.class.getDeclaredField(name); field.setAccessible(true); field.set(target, value);
    }
    private static SeatEntity seatAt(BlockPos pos, boolean occupied) throws Exception {
        SeatEntity seat = (SeatEntity) unsafe.allocateInstance(SeatEntity.class);
        set(seat, "position", new Vec3(pos.getX() + .5, pos.getY() + .0625, pos.getZ() + .5));
        set(seat, "blockPosition", pos);
        set(seat, "passengers", occupied ? ImmutableList.of(seat) : ImmutableList.of());
        seat.setBoundingBox(new AABB(pos)); return seat;
    }
    private static class FixtureContext extends BlockPlaceContext {
        FixtureWorld world; BlockPos pos;
        private FixtureContext() { super(null, InteractionHand.MAIN_HAND, ItemStack.EMPTY, null); }
        @Override public Level getLevel() { return world; }
        @Override public BlockPos getClickedPos() { return pos; }
        @Override public Direction getHorizontalDirection() { return Direction.NORTH; }
    }
    private static class FixtureWorld extends ServerLevel {
        Map<BlockPos, BlockState> states; List<SeatEntity> entities;
        private FixtureWorld() { super(null, null, null, null, null, null, false, 0, List.of(), false); }
        @Override public BlockState getBlockState(BlockPos pos) { return states.getOrDefault(pos, Blocks.AIR.defaultBlockState()); }
        @Override public boolean setBlock(BlockPos pos, BlockState state, int flags) { states.put(pos.immutable(), state); return true; }
        @Override public <T extends Entity> List<T> getEntitiesOfClass(Class<T> type, AABB box, Predicate<? super T> filter) {
            return entities.stream().filter(type::isInstance).map(type::cast).filter(filter).toList();
        }
    }
}
