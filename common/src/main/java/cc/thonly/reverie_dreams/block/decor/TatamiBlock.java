package cc.thonly.reverie_dreams.block.decor;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.UnaryOperator;

/**
 * Local floor connections, without a limit on the room's area.
 */
public class TatamiBlock extends Block {
    public static final MapCodec<TatamiBlock> CODEC = simpleCodec(TatamiBlock::new);
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty NORTH_EAST = BooleanProperty.create("north_east");
    public static final BooleanProperty SOUTH_EAST = BooleanProperty.create("south_east");
    public static final BooleanProperty SOUTH_WEST = BooleanProperty.create("south_west");
    public static final BooleanProperty NORTH_WEST = BooleanProperty.create("north_west");
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    private final VoxelShape shape;
    private final boolean hasCollision, requiresSupport;

    public enum Corner {
        NORTH_EAST(Direction.NORTH, Direction.EAST, TatamiBlock.NORTH_EAST),
        SOUTH_EAST(Direction.SOUTH, Direction.EAST, TatamiBlock.SOUTH_EAST),
        SOUTH_WEST(Direction.SOUTH, Direction.WEST, TatamiBlock.SOUTH_WEST),
        NORTH_WEST(Direction.NORTH, Direction.WEST, TatamiBlock.NORTH_WEST);
        public final Direction first, second;
        public final BooleanProperty property;

        Corner(Direction first, Direction second, BooleanProperty property) {
            this.first = first;
            this.second = second;
            this.property = property;
        }
    }

    public TatamiBlock(Properties properties) {
        this(properties, Block.box(0, 0, 0, 16, 1, 16), true, true);
    }

    public TatamiBlock(Properties properties, VoxelShape shape, boolean hasCollision, boolean requiresSupport) {
        super(properties);
        this.shape = shape;
        this.hasCollision = hasCollision;
        this.requiresSupport = requiresSupport;
        registerDefaultState(stateDefinition.any().setValue(NORTH, false).setValue(EAST, false)
                .setValue(SOUTH, false).setValue(WEST, false).setValue(NORTH_EAST, false)
                .setValue(SOUTH_EAST, false).setValue(SOUTH_WEST, false).setValue(NORTH_WEST, false)
                .setValue(AXIS, Direction.Axis.Z));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(AXIS, context.getHorizontalDirection().getAxis());
        return canSurvive(state, context.getLevel(), context.getClickedPos())
                ? withConnections(state, context.getLevel(), context.getClickedPos()) : null;
    }

    public BlockState withConnections(BlockState state, BlockGetter level, BlockPos pos) {
        for (Direction side : Direction.Plane.HORIZONTAL)
            state = state.setValue(property(side), level.getBlockState(pos.relative(side)).is(this));
        for (Corner corner : Corner.values())
            state = state.setValue(corner.property, state.getValue(property(corner.first))
                    && state.getValue(property(corner.second))
                    && level.getBlockState(pos.relative(corner.first).relative(corner.second)).is(this));
        return state;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return !requiresSupport || level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction side, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (side == Direction.DOWN && !canSurvive(state, level, pos))
            return Blocks.AIR.defaultBlockState();
        return withConnections(state, level, pos);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState previous, boolean moved) {
        super.onPlace(state, level, pos, previous, moved);
        if (!level.isClientSide() && !previous.is(this))
            scheduleNeighbors(level, pos);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        scheduleNeighbors(level, pos);
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
    }

    private void scheduleNeighbors(Level level, BlockPos pos) {
        // Diagonal changes do not receive vanilla's cardinal neighbor notification.
        for (int x = -1; x <= 1; x++)
            for (int z = -1; z <= 1; z++) {
                BlockPos nearby = pos.offset(x, 0, z);
                if (level.getBlockState(nearby).is(this))
                    level.scheduleTick(nearby, this, 1);
            }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState updated = withConnections(state, level, pos);
        if (updated != state)
            level.setBlock(pos, updated, Block.UPDATE_ALL);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.isShiftKeyDown() || !player.getMainHandItem().isEmpty())
            return InteractionResult.PASS;
        if (!level.isClientSide()) {
            Direction.Axis axis = state.getValue(AXIS) == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
            level.setBlock(pos, withConnections(state, level, pos).setValue(AXIS, axis), Block.UPDATE_ALL);
            scheduleNeighbors(level, pos);
        }
        return InteractionResult.SUCCESS;
    }

    public static BooleanProperty property(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> throw new IllegalArgumentException("Not horizontal: " + direction);
        };
    }

    private BlockState transform(BlockState state, UnaryOperator<Direction> transform) {
        BlockState result = state;
        for (Direction side : Direction.Plane.HORIZONTAL)
            result = result.setValue(property(transform.apply(side)), state.getValue(property(side)));
        for (Corner source : Corner.values())
            for (Corner target : Corner.values()) {
                Direction first = transform.apply(source.first), second = transform.apply(source.second);
                if ((target.first == first && target.second == second) || (target.first == second && target.second == first))
                    result = result.setValue(target.property, state.getValue(source.property));
            }
        return result.setValue(AXIS, transform.apply(state.getValue(AXIS) == Direction.Axis.X
                ? Direction.EAST : Direction.SOUTH).getAxis());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return transform(state, rotation::rotate);
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return transform(state, mirror::mirror);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return hasCollision ? shape : Shapes.empty();
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, NORTH_EAST, SOUTH_EAST, SOUTH_WEST, NORTH_WEST, AXIS);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
}
