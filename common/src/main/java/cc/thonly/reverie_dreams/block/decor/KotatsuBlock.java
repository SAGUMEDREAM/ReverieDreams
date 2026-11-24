package cc.thonly.reverie_dreams.block.decor;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Low quilted table; grows to a capped 2x2 group like the other low furniture.
 */
public class KotatsuBlock extends GroupedDecorBlock {
    public static final MapCodec<KotatsuBlock> CODEC = simpleCodec(KotatsuBlock::new);

    public record Part(double x1, double y1, double z1, double x2, double y2, double z2, String texture) {
        public VoxelShape shape() {
            return Block.box(x1, y1, z1, x2, y2, z2);
        }
    }

    private static final List<List<Part>> PARTS = IntStream.range(0, 16)
            .mapToObj(KotatsuBlock::buildParts).toList();
    private static final VoxelShape[] SHAPES = IntStream.range(0, 16).mapToObj(mask -> {
        VoxelShape shape = Shapes.empty();
        for (Part part : PARTS.get(mask)) {
            shape = Shapes.or(shape, part.shape());
        }
        return shape.optimize();
    }).toArray(VoxelShape[]::new);

    public KotatsuBlock(Properties properties) {
        super(properties, SHAPES[0], true, false);
    }

    public static List<Part> parts(int mask) {
        return PARTS.get(mask & 15);
    }

    private static List<Part> buildParts(int mask) {
        boolean n = (mask & 1) != 0, e = (mask & 2) != 0, s = (mask & 4) != 0, w = (mask & 8) != 0;
        List<Part> parts = new ArrayList<>();
        // The quilt reaches the block edges so connected tables form one
        // continuous padded surface.
        parts.add(new Part(0, 7, 0, 16, 8, 16, "indigo"));
        parts.add(new Part(w ? 0 : 1.25, 8, n ? 0 : 1.25,
                e ? 16 : 14.75, 8.9, s ? 16 : 14.75, "ginkgo"));
        parts.add(new Part(w ? 0 : 1.25, 6.25, n ? 0 : 1.25,
                e ? 16 : 14.75, 7, s ? 16 : 14.75, "peach"));
        // Only outer sides drape; the corner split keeps adjacent skirts
        // from becoming coplanar with each other.
        if (!n)
            parts.add(new Part(0.8, 1, 0, 15.2, 7, 0.8, "indigo"));
        if (!s)
            parts.add(new Part(0.8, 1, 15.2, 15.2, 7, 16, "indigo"));
        if (!w)
            parts.add(new Part(0, 1, 0, 0.8, 7, 16, "indigo"));
        if (!e)
            parts.add(new Part(15.2, 1, 0, 16, 7, 16, "indigo"));
        // One heater per group: the block owning the north-west outer corner
        // carries it, so a 2x2 table does not repeat four stoves.
        if (!n && !w) {
            parts.add(new Part(5, 2, 5, 11, 6.25, 11, "ink"));
            parts.add(new Part(5.5, 1.75, 5.5, 10.5, 2, 10.5, "iron"));
        }
        if (!n && !w)
            leg(parts, 1, 1);
        if (!n && !e)
            leg(parts, 13, 1);
        if (!s && !w)
            leg(parts, 1, 13);
        if (!s && !e)
            leg(parts, 13, 13);
        return List.copyOf(parts);
    }

    private static void leg(List<Part> parts, double x, double z) {
        parts.add(new Part(x, 0, z, x + 2, 6.25, z + 2, "peach"));
    }

    public static int mask(BlockState state) {
        return (state.getValue(NORTH) ? 1 : 0) | (state.getValue(EAST) ? 2 : 0)
                | (state.getValue(SOUTH) ? 4 : 0) | (state.getValue(WEST) ? 8 : 0);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[mask(state)];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[mask(state)];
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[mask(state)];
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
}
