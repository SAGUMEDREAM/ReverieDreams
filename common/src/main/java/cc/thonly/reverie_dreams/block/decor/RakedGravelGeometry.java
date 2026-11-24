package cc.thonly.reverie_dreams.block.decor;

import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * Native mesh authoring data: a gravel surface with quarter-pixel shallow rake grooves.
 */
public final class RakedGravelGeometry {
    public static final double BASE = 15.75, GROOVE = 15.8, RIDGE = 16;
    private static final int[] LANES = {1, 5, 10, 14};

    public record Patch(int x1, int z1, int x2, int z2, boolean groove) {
    }

    private RakedGravelGeometry() {
    }

    private static boolean lane(int pixel) {
        for (int p : LANES)
            if (pixel == p)
                return true;
        return false;
    }

    public static boolean groove(RakedGravelBlock.Shape shape, int connections, int x, int z) {
        if (z == 0 && shape.has(Direction.NORTH))
            return connected(shape, connections, Direction.NORTH) && lane(x);
        if (z == 15 && shape.has(Direction.SOUTH))
            return connected(shape, connections, Direction.SOUTH) && lane(x);
        if (x == 0 && shape.has(Direction.WEST))
            return connected(shape, connections, Direction.WEST) && lane(z);
        if (x == 15 && shape.has(Direction.EAST))
            return connected(shape, connections, Direction.EAST) && lane(z);
        if (shape == RakedGravelBlock.Shape.NORTH_SOUTH)
            return lane(x);
        if (shape == RakedGravelBlock.Shape.EAST_WEST)
            return lane(z);
        double cx = shape.has(Direction.EAST) ? 16 : 0, cz = shape.has(Direction.SOUTH) ? 16 : 0;
        double radius = Math.hypot(x + .5 - cx, z + .5 - cz);
        for (int p : LANES)
            if (Math.abs(radius - (p + .5)) < .5)
                return true;
        return false;
    }

    private static boolean connected(RakedGravelBlock.Shape shape, int links, Direction side) {
        return (links & (side == shape.first ? 1 : 2)) != 0;
    }

    public static List<Patch> patches(RakedGravelBlock.Shape shape, int connections) {
        boolean[][] used = new boolean[16][16];
        List<Patch> result = new ArrayList<>();
        for (int z = 0; z < 16; z++)
            for (int x = 0; x < 16; x++) {
                if (used[z][x])
                    continue;
                boolean value = groove(shape, connections, x, z);
                int width = 1, depth = 1;
                while (x + width < 16 && !used[z][x + width] && groove(shape, connections, x + width, z) == value)
                    width++;
                outer:
                while (z + depth < 16) {
                    for (int dx = 0; dx < width; dx++)
                        if (used[z + depth][x + dx] || groove(shape, connections, x + dx, z + depth) != value)
                            break outer;
                    depth++;
                }
                for (int dz = 0; dz < depth; dz++)
                    for (int dx = 0; dx < width; dx++)
                        used[z + dz][x + dx] = true;
                result.add(new Patch(x, z, x + width, z + depth, value));
            }
        return List.copyOf(result);
    }
}
