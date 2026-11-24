package cc.thonly.reverie_dreams.world.gensokyo.deadwood;

import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

/**
 * Relative, face-connected geometry. +X is the fall/impact direction, +Z its left side.
 */
public final class GensokyoDeadwoodShape {
    public record Point(int x, int y, int z) {
    }

    public record Cell(Point point, char axis, boolean splinter, int variant) {
    }

    public record Shape(List<Cell> cells, int width, int length) {
    }

    public static Shape create(GensokyoForestType theme, GensokyoDeadwoodConfiguration.Kind kind, long seed) {
        var random = new Random(seed);
        var cells = new LinkedHashMap<Point, Cell>();
        int width = switch (theme) {
            case DARK_FOREST, MAGIC_FOREST -> 2;
            case MAPLE_FOREST -> random.nextInt(3) == 0 ? 2 : 1;
            default -> 1;
        };
        int length = 0;
        if (kind == GensokyoDeadwoodConfiguration.Kind.FALLEN_LOG) {
            length = (theme == GensokyoForestType.TAIGA ? 8 : 5) + random.nextInt(5);
            if (theme == GensokyoForestType.TAIGA && random.nextInt(8) == 0)
                length += 3;
            int bend = (theme == GensokyoForestType.CHERRY_GROVE || theme == GensokyoForestType.MAPLE_FOREST)
                    ? (random.nextBoolean() ? 1 : -1) : 0;
            int elbow = length * 3 / 5;
            for (int x = 0; x < length; x++) {
                int z = x >= elbow ? bend : 0;
                for (int dx = 0; dx < width; dx++)
                    for (int dz = 0; dz < width; dz++)
                        log(cells, x, dx, z + dz, 'X');
                if (x == elbow && bend != 0)
                    for (int y = 0; y < width; y++)
                        log(cells, x, y, 0, 'X');
            }
            if (theme != GensokyoForestType.BIRCH_FOREST) {
                int branchX = length / 2, side = random.nextBoolean() ? 1 : -1;
                int startZ = side > 0 ? width : -1;
                for (int n = 0; n < (theme == GensokyoForestType.CHERRY_GROVE ? 3 : 2); n++)
                    log(cells, branchX, 0, startZ + n * side, 'Z');
                if (theme == GensokyoForestType.TAIGA)
                    log(cells, branchX + 2, width, 0, 'Y');
            }
        } else {
            int height = kind == GensokyoDeadwoodConfiguration.Kind.CUT_STUMP ? 1 + random.nextInt(3)
                    : (theme == GensokyoForestType.MAGIC_FOREST ? 5 : 3) + random.nextInt(3);
            for (int x = 0; x < width; x++)
                for (int z = 0; z < width; z++) {
                    int top = kind == GensokyoDeadwoodConfiguration.Kind.CUT_STUMP ? height : height - ((x + z) % 3);
                    for (int y = 0; y < top; y++)
                        log(cells, x, y, z, 'Y');
                    if (kind == GensokyoDeadwoodConfiguration.Kind.SPLINTERED_TRUNK) {
                        var p = new Point(x, top, z);
                        cells.put(p, new Cell(p, 'Y', true, random.nextInt(3)));
                    }
                }
            // Low, short buttresses keep broad trunks rooted; never become long surface roots.
            if (width == 2) {
                log(cells, -1, 0, 0, 'X');
                log(cells, 0, 0, -1, 'Z');
            }
            if (kind == GensokyoDeadwoodConfiguration.Kind.SPLINTERED_TRUNK) {
                // One attached broken limb points in the same direction as the associated fallen piece.
                if (theme != GensokyoForestType.BIRCH_FOREST)
                    log(cells, width, Math.max(1, height - 3), 0, 'X');
                length = (theme == GensokyoForestType.MAGIC_FOREST ? 5 : 3) + random.nextInt(3);
                for (int x = width + 2; x < width + 2 + length; x++)
                    log(cells, x, 0, 0, 'X');
            }
        }
        return new Shape(List.copyOf(cells.values()), width, length);
    }

    private static void log(LinkedHashMap<Point, Cell> cells, int x, int y, int z, char axis) {
        var p = new Point(x, y, z);
        cells.put(p, new Cell(p, axis, false, 0));
    }

    private GensokyoDeadwoodShape() {
    }
}
