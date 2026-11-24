package cc.thonly.reverie_dreams.world.gensokyo.rock;

import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

/**
 * Column tops above the original surface: broad low forms, clipped corners and offset lobes.
 */
public final class GensokyoRockShape {
    public record Point(int x, int z) {
    }

    public record Column(Point point, int top) {
    }

    public static List<Column> create(GensokyoForestType theme, GensokyoRockConfiguration.Kind kind, long seed) {
        var random = new Random(seed);
        var columns = new LinkedHashMap<Point, Integer>();
        boolean tall = theme == GensokyoForestType.TAIGA || theme == GensokyoForestType.DARK_FOREST || theme == GensokyoForestType.MAGIC_FOREST;
        int length = switch (kind) {
            case LOW_OUTCROP -> 3 + random.nextInt(2);
            case ROCK_CLUSTER -> 4 + random.nextInt(2);
            case LANDMARK_BOULDER -> 6 + random.nextInt(3);
        };
        int width = switch (kind) {
            case LOW_OUTCROP -> 2 + random.nextInt(2);
            case ROCK_CLUSTER -> 3 + random.nextInt(2);
            case LANDMARK_BOULDER -> 4 + random.nextInt(3);
        };
        int height = switch (kind) {
            case LOW_OUTCROP -> 1;
            case ROCK_CLUSTER -> tall ? 3 : 2;
            case LANDMARK_BOULDER -> tall ? 4 : 3;
        };
        lobe(columns, 0, 0, length, width, height, random);
        if (kind != GensokyoRockConfiguration.Kind.LOW_OUTCROP) {
            // Secondary stones sit lower and to one side, preserving an asymmetric silhouette.
            int side = random.nextBoolean() ? 1 : -1;
            lobe(columns, side * (length / 2), 1, 3, 2 + random.nextInt(2), kind == GensokyoRockConfiguration.Kind.ROCK_CLUSTER ? 1 : 2, random);
            if (kind == GensokyoRockConfiguration.Kind.ROCK_CLUSTER)
                lobe(columns, -side * 2, -2, 2, 2, 1, random);
        }
        int rotation = random.nextInt(4);
        return columns.entrySet().stream().map(e -> {
            int x = e.getKey().x(), z = e.getKey().z();
            Point p = switch (rotation) {
                case 0 -> new Point(x, z);
                case 1 -> new Point(-z, x);
                case 2 -> new Point(-x, -z);
                default -> new Point(z, -x);
            };
            return new Column(p, e.getValue());
        }).toList();
    }

    private static void lobe(LinkedHashMap<Point, Integer> columns, int cx, int cz, int length, int width, int height, Random random) {
        double skew = random.nextDouble() * .35 - .175;
        for (int x = -length / 2; x < (length + 1) / 2; x++)
            for (int z = -width / 2; z < (width + 1) / 2; z++) {
                double nx = (x + (length % 2 == 0 ? .5 : 0) + skew) / (length * .5);
                double nz = (z + (width % 2 == 0 ? .5 : 0) - skew * .5) / (width * .5);
                double distance = nx * nx + nz * nz + nx * nz * skew;
                if (distance > 1.05 || distance > .78 && x == -length / 2 && z == -width / 2)
                    continue;
                int top = Math.min(height - 1, Math.max(0, (int) Math.floor((1 - distance * .65 + nx * skew) * height)));
                columns.merge(new Point(cx + x, cz + z), top, Math::max);
            }
    }

    private GensokyoRockShape() {
    }
}
