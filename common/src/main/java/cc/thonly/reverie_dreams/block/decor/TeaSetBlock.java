package cc.thonly.reverie_dreams.block.decor;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * A wooden tray, a porcelain teapot and three cups; decorative, without collision.
 */
public class TeaSetBlock extends ThinDecorBlock {
    public static final MapCodec<TeaSetBlock> CODEC = simpleCodec(TeaSetBlock::new);

    public record Part(double x1, double y1, double z1, double x2, double y2, double z2, String texture) {
        public VoxelShape shape() {
            return Block.box(x1, y1, z1, x2, y2, z2);
        }
    }

    private static final List<Part> PARTS = buildParts();
    private static final VoxelShape MESH = buildShape();
    private static final Map<Direction, VoxelShape> OUTLINES = buildOutlines();

    public TeaSetBlock(Properties properties) {
        super(properties, MESH, false);
    }

    public static List<Part> parts() {
        return PARTS;
    }

    public static VoxelShape outline(Direction facing) {
        return OUTLINES.get(facing);
    }

    private static Map<Direction, VoxelShape> buildOutlines() {
        Map<Direction, VoxelShape> outlines = new EnumMap<>(Shapes.rotateHorizontal(MESH));
        outlines.replaceAll((facing, shape) -> CachedDecorShape.of(shape));
        return outlines;
    }

    private static VoxelShape buildShape() {
        VoxelShape result = Shapes.empty();
        for (Part part : PARTS)
            result = Shapes.or(result, part.shape());
        return result.optimize();
    }

    private static List<Part> buildParts() {
        List<Part> parts = new ArrayList<>();
        parts.add(new Part(1, 0, 1, 15, .6, 15, "tray"));
        parts.add(new Part(1, .6, 1, 15, 1, 1.5, "tray"));
        parts.add(new Part(1, .6, 14.5, 15, 1, 15, "tray"));
        parts.add(new Part(1, .6, 1.5, 1.5, 1, 14.5, "tray"));
        parts.add(new Part(14.5, .6, 1.5, 15, 1, 14.5, "tray"));
        parts.add(new Part(7.5, 1, 5.5, 11.5, 1.5, 9.5, "rim"));
        parts.add(new Part(7, 1.5, 5, 12, 4.5, 10, "porcelain"));
        parts.add(new Part(7.5, 4.5, 5.5, 11.5, 5, 9.5, "porcelain"));
        parts.add(new Part(7.25, 5, 5.25, 11.75, 5.5, 9.75, "porcelain"));
        parts.add(new Part(9, 5.5, 7, 10, 6.5, 8, "rim"));
        parts.add(new Part(4.5, 2.5, 6.5, 7, 3.5, 8.5, "porcelain"));
        parts.add(new Part(3.75, 3.25, 7, 5.25, 4.25, 8, "porcelain"));
        parts.add(new Part(11.75, 4, 6.5, 14, 4.75, 8.5, "rim"));
        parts.add(new Part(13.25, 2, 6.5, 14, 4.75, 8.5, "rim"));
        parts.add(new Part(11.75, 1.75, 6.5, 14, 2.5, 8.5, "rim"));
        cup(parts, 2, 2);
        cup(parts, 2, 11);
        cup(parts, 10, 11);
        return List.copyOf(parts);
    }

    private static void cup(List<Part> parts, double x, double z) {
        parts.add(new Part(x + .5, 1, z + .5, x + 2.5, 1.5, z + 2.5, "porcelain"));
        parts.add(new Part(x, 1.5, z, x + 3, 3, z + .5, "porcelain"));
        parts.add(new Part(x, 1.5, z + 2.5, x + 3, 3, z + 3, "porcelain"));
        parts.add(new Part(x, 1.5, z + .5, x + .5, 3, z + 2.5, "porcelain"));
        parts.add(new Part(x + 2.5, 1.5, z + .5, x + 3, 3, z + 2.5, "porcelain"));
        parts.add(new Part(x, 3, z, x + 3, 3.3, z + .5, "rim"));
        parts.add(new Part(x, 3, z + 2.5, x + 3, 3.3, z + 3, "rim"));
        parts.add(new Part(x, 3, z + .5, x + .5, 3.3, z + 2.5, "rim"));
        parts.add(new Part(x + 2.5, 3, z + .5, x + 3, 3.3, z + 2.5, "rim"));
        parts.add(new Part(x + .5, 2.25, z + .5, x + 2.5, 2.3, z + 2.5, "tea"));
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction side, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        return side == Direction.DOWN && !canSurvive(state, level, pos) ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return outline(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return outline(state.getValue(FACING));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }
}
