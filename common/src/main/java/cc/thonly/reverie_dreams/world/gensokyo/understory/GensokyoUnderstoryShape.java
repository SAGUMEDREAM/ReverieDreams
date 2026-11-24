package cc.thonly.reverie_dreams.world.gensokyo.understory;

import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

/**
 * Small, irregular, low-branching forms, separate from the protected full-size tree planner.
 */
public final class GensokyoUnderstoryShape {
    public record Point(int x, int y, int z) {
    }

    public record Cell(Point point, boolean log, String axis) {
    }

    public record Clump(int x, int z, int palette, List<Cell> cells) {
    }

    public static List<Clump> create(GensokyoUnderstoryConfiguration config, long seed) {
        Random random = new Random(seed);
        var result = new ArrayList<Clump>();
        if (config.kind() == GensokyoUnderstoryConfiguration.Kind.FLOOR_PATCH)
            return result;
        int count = config.kind() == GensokyoUnderstoryConfiguration.Kind.THICKET ? 2 + random.nextInt(3) : 1;
        int rotation = random.nextInt(4);
        int[][] roots = {{-2, -1}, {1, 2}, {2, -2}, {-2, 2}};
        for (int n = 0; n < count; n++) {
            int height = switch (config.kind()) {
                case LEAF_PATCH -> 2;
                case SHRUB -> 2 + random.nextInt(3);
                case THICKET -> 3 + random.nextInt(3);
                default -> throw new IllegalStateException();
            };
            var cells = new LinkedHashMap<Point, Cell>();
            int bendX = config.theme() == GensokyoForestType.MAGIC_FOREST ? random.nextInt(3) - 1 : 0;
            int bendZ = config.theme() == GensokyoForestType.MAGIC_FOREST ? random.nextInt(3) - 1 : 0;
            int trunk = config.kind() == GensokyoUnderstoryConfiguration.Kind.LEAF_PATCH ? 1 : height - 1;
            for (int y = 0; y < trunk; y++)
                wood(cells, 0, y, 0, "Y");
            if (trunk > 1) {
                // Branches begin low and merge into several off-centre leaf lobes.
                for (int side = 0; side < 4; side++) {
                    int dx = side == 0 ? 1 : side == 1 ? -1 : 0;
                    int dz = side == 2 ? 1 : side == 3 ? -1 : 0;
                    if (random.nextInt(4) == 0)
                        continue;
                    int y = 1 + random.nextInt(trunk - 1);
                    wood(cells, dx, y, dz, dx == 0 ? "Z" : "X");
                }
                if (bendX != 0)
                    wood(cells, bendX, trunk - 1, 0, "X");
                if (bendZ != 0)
                    wood(cells, bendX, trunk - 1, bendZ, "Z");
                if (config.theme() == GensokyoForestType.DARK_FOREST && random.nextInt(3) == 0) {
                    wood(cells, 1, 1, 0, "X");
                    wood(cells, 2, 1, 0, "X");
                    wood(cells, 3, 1, 0, "X");
                }
            }
            int radius = config.theme() == GensokyoForestType.BIRCH_FOREST ? 1 : 2;
            for (int y = 0; y < height; y++) {
                if (y == 0 && height > 2)
                    continue;
                int layerRadius = config.theme() == GensokyoForestType.TAIGA ? (y == height - 1 ? 0 : Math.min(2, height - y))
                        : (y == height - 1 ? 1 : radius);
                for (int x = -layerRadius; x <= layerRadius; x++)
                    for (int z = -layerRadius; z <= layerRadius; z++) {
                        if (x * x + z * z > layerRadius * layerRadius + random.nextInt(2))
                            continue;
                        if (Math.abs(x) + Math.abs(z) > 1 && random.nextInt(7) == 0)
                            continue;
                        var p = new Point(x + (y > 1 ? bendX : 0), y, z + (y > 1 ? bendZ : 0));
                        cells.putIfAbsent(p, new Cell(p, false, "Y"));
                    }
            }
            int x = count == 1 ? 0 : roots[(n + rotation) % 4][0];
            int z = count == 1 ? 0 : roots[(n + rotation) % 4][1];
            result.add(new Clump(x, z, random.nextInt(config.logs().size()), List.copyOf(cells.values())));
        }
        return List.copyOf(result);
    }

    private static void wood(LinkedHashMap<Point, Cell> cells, int x, int y, int z, String axis) {
        var p = new Point(x, y, z);
        cells.put(p, new Cell(p, true, axis));
    }

    private GensokyoUnderstoryShape() {
    }
}
