package cc.thonly.reverie_dreams.block.decor;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.Locale;
import java.util.function.UnaryOperator;

/**
 * Rakeable gravel with stable local layout and reciprocal groove connections.
 */
public class RakedGravelBlock extends FallingBlock {
    public static final MapCodec<RakedGravelBlock> CODEC = simpleCodec(RakedGravelBlock::new);
    public static final EnumProperty<Shape> SHAPE = EnumProperty.create("shape", Shape.class);
    public static final EnumProperty<Mode> MODE = EnumProperty.create("rake_mode", Mode.class);
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH, EAST = BlockStateProperties.EAST,
            SOUTH = BlockStateProperties.SOUTH, WEST = BlockStateProperties.WEST;

    public enum Mode implements StringRepresentable {
        AUTO, STRAIGHT, CURVE;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public Mode next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    public enum Shape implements StringRepresentable {
        NORTH_SOUTH(Direction.NORTH, Direction.SOUTH), EAST_WEST(Direction.EAST, Direction.WEST),
        NORTH_EAST(Direction.NORTH, Direction.EAST), SOUTH_EAST(Direction.SOUTH, Direction.EAST),
        SOUTH_WEST(Direction.SOUTH, Direction.WEST), NORTH_WEST(Direction.NORTH, Direction.WEST);
        public final Direction first, second;

        Shape(Direction first, Direction second) {
            this.first = first;
            this.second = second;
        }

        public boolean has(Direction direction) {
            return direction == first || direction == second;
        }

        public boolean straight() {
            return first.getAxis() == second.getAxis();
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public Shape rotate(Rotation rotation) {
            return between(rotation.rotate(first), rotation.rotate(second));
        }

        public Shape mirror(Mirror mirror) {
            return between(mirror.mirror(first), mirror.mirror(second));
        }

        private static Shape between(Direction a, Direction b) {
            for (Shape shape : values())
                if (shape.has(a) && shape.has(b))
                    return shape;
            throw new IllegalArgumentException("Unsupported rake directions " + a + "/" + b);
        }

        public static Shape straight(Direction.Axis axis) {
            return axis == Direction.Axis.X ? EAST_WEST : NORTH_SOUTH;
        }

        public static Shape automatic(int neighbors, Direction.Axis preferred) {
            boolean n = (neighbors & 1) != 0, e = (neighbors & 2) != 0,
                    s = (neighbors & 4) != 0, w = (neighbors & 8) != 0;
            if (n && s && e && w)
                return straight(preferred);
            if (n && s)
                return NORTH_SOUTH;
            if (e && w)
                return EAST_WEST;
            if (n && e)
                return NORTH_EAST;
            if (s && e)
                return SOUTH_EAST;
            if (s && w)
                return SOUTH_WEST;
            if (n && w)
                return NORTH_WEST;
            if (n || s)
                return NORTH_SOUTH;
            if (e || w)
                return EAST_WEST;
            return straight(preferred);
        }
    }

    public RakedGravelBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SHAPE, Shape.NORTH_SOUTH).setValue(MODE, Mode.AUTO)
                .setValue(AXIS, Direction.Axis.Z).setValue(NORTH, false).setValue(EAST, false)
                .setValue(SOUTH, false).setValue(WEST, false));
    }

    public static BooleanProperty link(Direction side) {
        return switch (side) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> throw new IllegalArgumentException("Not horizontal: " + side);
        };
    }

    public static int bit(Direction side) {
        return switch (side) {
            case NORTH -> 1;
            case EAST -> 2;
            case SOUTH -> 4;
            case WEST -> 8;
            default -> 0;
        };
    }

    private int neighbors(BlockGetter level, BlockPos pos, BlockPos prospective) {
        int mask = 0;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = pos.relative(side);
            if (neighbor.equals(prospective) || level.getBlockState(neighbor).is(this))
                mask |= bit(side);
        }
        return mask;
    }

    private Shape resolve(BlockState state, BlockGetter level, BlockPos pos, BlockPos prospective) {
        return state.getValue(MODE) == Mode.AUTO
                ? Shape.automatic(neighbors(level, pos, prospective), state.getValue(AXIS)) : state.getValue(SHAPE);
    }

    public BlockState withLayout(BlockState state, BlockGetter level, BlockPos pos) {
        Shape shape = resolve(state, level, pos, pos);
        state = state.setValue(SHAPE, shape);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos adjacent = pos.relative(side);
            BlockState neighbor = level.getBlockState(adjacent);
            boolean connected = shape.has(side) && neighbor.is(this)
                    && resolve(neighbor, level, adjacent, pos).has(side.getOpposite());
            state = state.setValue(link(side), connected);
        }
        return state;
    }

    public BlockState rake(BlockState state, boolean cycle, BlockGetter level, BlockPos pos) {
        if (cycle) {
            Mode mode = state.getValue(MODE).next();
            Shape shape = state.getValue(SHAPE);
            if (mode == Mode.STRAIGHT && !shape.straight())
                shape = Shape.straight(state.getValue(AXIS));
            if (mode == Mode.CURVE && shape.straight()) {
                shape = Shape.automatic(neighbors(level, pos, pos), state.getValue(AXIS));
                if (shape.straight())
                    shape = Shape.NORTH_EAST;
            }
            state = state.setValue(MODE, mode).setValue(SHAPE, shape);
        } else {
            Shape shape = state.getValue(SHAPE).rotate(Rotation.CLOCKWISE_90);
            state = state.setValue(SHAPE, shape).setValue(MODE, shape.straight() ? Mode.STRAIGHT : Mode.CURVE)
                    .setValue(AXIS, state.getValue(AXIS) == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X);
        }
        return withLayout(state, level, pos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return withLayout(defaultBlockState().setValue(AXIS, context.getHorizontalDirection().getAxis()), context.getLevel(), context.getClickedPos());
    }

    public void notifyLayout(Level level, BlockPos pos) {
        if (level.isClientSide())
            return;
        if (level.getBlockState(pos).is(this))
            level.scheduleTick(pos, this, 1);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = pos.relative(side);
            if (level.getBlockState(neighbor).is(this))
                level.scheduleTick(neighbor, this, 1);
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        if (!old.is(this))
            notifyLayout(level, pos);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction side, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        state = super.updateShape(state, level, ticks, pos, side, neighborPos, neighborState, random);
        return side.getAxis().isHorizontal() ? withLayout(state, level, pos) : state;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        notifyLayout(level, pos);
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        if (level.getBlockState(pos).is(this)) {
            BlockState updated = withLayout(state, level, pos);
            if (updated != state)
                level.setBlock(pos, updated, Block.UPDATE_ALL);
        }
    }

    private BlockState transform(BlockState state, UnaryOperator<Direction> transform, Shape shape) {
        BlockState result = state.setValue(SHAPE, shape);
        for (Direction side : Direction.Plane.HORIZONTAL)
            result = result.setValue(link(transform.apply(side)), state.getValue(link(side)));
        return result.setValue(AXIS, transform.apply(state.getValue(AXIS) == Direction.Axis.X ? Direction.EAST : Direction.SOUTH).getAxis());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return transform(state, rotation::rotate, state.getValue(SHAPE).rotate(rotation));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return transform(state, mirror::mirror, state.getValue(SHAPE).mirror(mirror));
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
        return ((FallingBlock) Blocks.GRAVEL).getDustColor(Blocks.GRAVEL.defaultBlockState(), level, pos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, MODE, AXIS, NORTH, EAST, SOUTH, WEST);
    }

    @Override
    protected MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }

    public static int visualConnections(BlockState state) {
        Shape shape = state.getValue(SHAPE);
        return (state.getValue(link(shape.first)) ? 1 : 0) | (state.getValue(link(shape.second)) ? 2 : 0);
    }

    public static String modelName(Shape shape, int links) {
        return "raked_gravel_" + shape.getSerializedName() + "_" + (links & 3);
    }
}
