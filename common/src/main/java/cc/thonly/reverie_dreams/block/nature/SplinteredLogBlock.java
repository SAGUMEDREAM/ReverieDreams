package cc.thonly.reverie_dreams.block.nature;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * A solid wooden base with a deep, open split and uneven splinters. No block entity.
 */
public final class SplinteredLogBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<SplinteredLogBlock> CODEC = simpleCodec(SplinteredLogBlock::new);
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 2);

    public record Box(int x0, int y0, int z0, int x1, int y1, int z1) {
    }

    // Also read by the asset exporter: collision and visible wood use exactly the same boxes.
    public static List<Box> boxes(int variant) {
        return switch (variant) {
            case 0 -> List.of(new Box(0, 0, 0, 16, 4, 16), new Box(0, 4, 0, 5, 16, 16),
                    new Box(11, 4, 0, 16, 11, 16), new Box(5, 4, 11, 11, 8, 16));
            case 1 -> List.of(new Box(0, 0, 0, 16, 3, 16), new Box(0, 3, 0, 6, 12, 16),
                    new Box(12, 3, 0, 16, 16, 16), new Box(6, 3, 12, 12, 7, 16));
            case 2 -> List.of(new Box(0, 0, 0, 16, 5, 16), new Box(0, 5, 0, 4, 13, 16),
                    new Box(10, 5, 0, 16, 10, 16), new Box(0, 13, 9, 4, 16, 16), new Box(4, 5, 12, 10, 8, 16));
            default -> throw new IllegalArgumentException("Unknown split variant " + variant);
        };
    }

    private static final List<Map<Direction, VoxelShape>> SHAPES = IntStream.range(0, 3)
            .mapToObj(v -> Shapes.rotateHorizontal(Shapes.or(Shapes.empty(), boxes(v).stream()
                    .map(b -> Block.box(b.x0, b.y0, b.z0, b.x1, b.y1, b.z1)).toArray(VoxelShape[]::new))))
            .toList();

    public SplinteredLogBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(VARIANT, 0));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(VARIANT)).get(state.getValue(FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, VARIANT);
    }
}
