package cc.thonly.reverie_dreams.block.decor;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * Decorative blocks with a capped 2x2 connection group.
 *
 * <p>Low tables retain this cap independently of the unlimited floor mats.
 * Different registered blocks never connect to each other.</p>
 */
public class GroupedDecorBlock extends Block {
    public static final MapCodec<GroupedDecorBlock> CODEC = simpleCodec(GroupedDecorBlock::new);
    public static final BooleanProperty NORTH = TatamiBlock.NORTH;
    public static final BooleanProperty EAST = TatamiBlock.EAST;
    public static final BooleanProperty SOUTH = TatamiBlock.SOUTH;
    public static final BooleanProperty WEST = TatamiBlock.WEST;

    private static final VoxelShape CARPET_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    private static final VoxelShape FULL_SHAPE = Shapes.block();
    private static final double MIN_SELECTION_THICKNESS = 4.0 / 16.0;

    private final VoxelShape shape;
    private final boolean hasCollision;
    private final boolean requiresSupport;

    public GroupedDecorBlock(Properties properties) {
        this(properties, CARPET_SHAPE, false, true);
    }

    public GroupedDecorBlock(Properties properties, VoxelShape shape, boolean hasCollision, boolean requiresSupport) {
        super(properties);
        this.shape = shape;
        this.hasCollision = hasCollision;
        this.requiresSupport = requiresSupport;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!this.canSurvive(this.defaultBlockState(), level, pos)) {
            return null;
        }
        BlockState state = this.defaultBlockState();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(direction);
            if (!connects(level.getBlockState(neighborPos), state.getBlock())) {
                continue;
            }
            BlockState candidate = state.setValue(property(direction), true);
            if (canConnect(level, pos, candidate, neighborPos)) {
                state = candidate;
            }
        }
        return state;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return !this.requiresSupport
                || level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    /**
     * A connected tatami group may not grow past a 2x2 footprint. Flood the
     * already-connected component from the candidate neighbor, include the new
     * block, and reject the edge if the resulting bounds are larger than 2x2.
     */
    private static boolean canConnect(Level level, BlockPos newPos, BlockState candidate, BlockPos neighborPos) {
        Block expected = candidate.getBlock();
        if (!connects(level.getBlockState(neighborPos), expected)) {
            return false;
        }
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(neighborPos);
        queue.add(newPos);
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        while (!queue.isEmpty()) {
            BlockPos pos = queue.removeFirst();
            if (!visited.add(pos)) {
                continue;
            }
            BlockState state = pos.equals(newPos) ? candidate : level.getBlockState(pos);
            if (state.getBlock() != expected) {
                continue;
            }
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                if (state.getValue(property(direction))) {
                    queue.addLast(pos.relative(direction));
                }
            }
        }
        return maxX - minX <= 1 && maxZ - minZ <= 1;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        if (direction == Direction.DOWN && !this.canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (direction.getAxis().isHorizontal() && !connects(neighborState, state.getBlock())) {
            return state.setValue(property(direction), false);
        }
        return state;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide() && !oldState.is(state.getBlock())) {
            updateNeighbors(level, pos, state);
        }
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighbor = level.getBlockState(neighborPos);
            if (neighbor.getBlock() != state.getBlock()) {
                continue;
            }
            BlockState updated = neighbor.setValue(property(direction.getOpposite()), false);
            if (updated != neighbor) {
                level.setBlock(neighborPos, updated, Block.UPDATE_ALL);
            }
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    private static void updateNeighbors(Level level, BlockPos pos, BlockState state) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighbor = level.getBlockState(neighborPos);
            if (neighbor.getBlock() != state.getBlock()) {
                continue;
            }
            BlockState updated = neighbor.setValue(
                    property(direction.getOpposite()), state.getValue(property(direction)));
            if (updated != neighbor) {
                level.setBlock(neighborPos, updated, Block.UPDATE_ALL);
            }
        }
    }

    private static boolean connects(BlockState state, Block expected) {
        return state.getBlock() == expected;
    }

    private static BooleanProperty property(Direction direction) {
        return switch (direction) {
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> NORTH;
        };
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (this.hasCollision) {
            return this.shape;
        }
        double minX = this.shape.min(Direction.Axis.X);
        double minY = this.shape.min(Direction.Axis.Y);
        double minZ = this.shape.min(Direction.Axis.Z);
        double maxX = this.shape.max(Direction.Axis.X);
        double maxY = this.shape.max(Direction.Axis.Y);
        double maxZ = this.shape.max(Direction.Axis.Z);
        if (maxY - minY < MIN_SELECTION_THICKNESS) {
            maxY = Math.min(1.0, minY + MIN_SELECTION_THICKNESS);
        }
        return Shapes.box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.hasCollision ? this.shape : Shapes.empty();
    }

    /**
     * The default visual shape is the collision shape. Variants without
     * collision keep the real shape for visual queries instead of falling
     * back to an empty shape.
     */
    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.shape;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
}
