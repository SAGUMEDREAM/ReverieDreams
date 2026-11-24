package cc.thonly.reverie_dreams.world.dream.scene;

/**
 * Smoothly varied authored coastlines and tapered undersides, with no per-block random noise.
 */
final class DreamIslandShape {
    private final double[][] points;
    private final double tipX;
    private final double tipZ;
    private final double scale;
    private final int minX;
    private final int maxX;
    private final int minZ;
    private final int maxZ;

    private DreamIslandShape(int[][] outline, long seed, boolean angular) {
        long bits = mix(seed);
        double phase = ((bits >>> 11) * 0x1.0p-53) * Math.PI * 2;
        double phase2 = (((mix(bits) >>> 11) * 0x1.0p-53)) * Math.PI * 2;
        points = new double[outline.length][2];
        int lowX = 28, highX = -28, lowZ = 28, highZ = -28;
        for (int i = 0; i < outline.length; i++) {
            double x = outline[i][0], z = outline[i][1];
            // Shared low-frequency displacement moves whole headlands; it never speckles the shore.
            double dx = 1.4 * Math.sin(z / 11 + phase) + .6 * Math.sin(x / 15 + phase2);
            double dz = 1.3 * Math.cos(x / 13 + phase2) + .5 * Math.sin(z / 17 + phase);
            if (angular) {
                dx = Math.rint(dx);
                dz = Math.rint(dz);
            }
            int px = Math.clamp((int) Math.round(x + dx), -28, 28);
            int pz = Math.clamp((int) Math.round(z + dz), -28, 28);
            points[i][0] = px;
            points[i][1] = pz;
            lowX = Math.min(lowX, px);
            highX = Math.max(highX, px);
            lowZ = Math.min(lowZ, pz);
            highZ = Math.max(highZ, pz);
        }
        minX = lowX;
        maxX = highX;
        minZ = lowZ;
        maxZ = highZ;
        tipX = 3.5 * Math.sin(phase);
        tipZ = 3.5 * Math.cos(phase2);
        scale = Math.max(7, Math.min(maxX - minX, maxZ - minZ) * .36);
    }

    static DreamIslandShape forScene(String id, long seed) {
        int[][] outline = switch (id) {
            case "huaian_station" -> new int[][]{{-24, -6}, {-20, -13}, {-13, -18}, {-4, -18}, {3, -21}, {13, -18},
                    {18, -11}, {20, -5}, {24, -2}, {23, 7}, {16, 11}, {13, 18}, {4, 20}, {0, 21}, {-8, 19}, {-14, 14}, {-20, 13}, {-22, 6}};
            case "bamboo_memory" -> new int[][]{{-17, -4}, {-14, -10}, {-6, -12}, {0, -10}, {7, -14}, {14, -9},
                    {16, -4}, {18, 0}, {15, 5}, {10, 7}, {5, 12}, {-3, 14}, {-9, 10}, {-16, 6}, {-18, 0}};
            case "tea_garden_memory" -> new int[][]{{-15, -9}, {-8, -13}, {-2, -16}, {5, -12}, {13, -11}, {16, -5},
                    {13, 2}, {16, 8}, {10, 13}, {3, 14}, {-4, 16}, {-8, 12}, {-14, 10}, {-16, 3}};
            case "mirror_pond_memory" -> new int[][]{{-17, -4}, {-13, -11}, {-4, -13}, {2, -16}, {12, -12},
                    {16, -5}, {15, 3}, {18, 7}, {10, 12}, {3, 14}, {-4, 16}, {-12, 11}, {-16, 3}, {-18, 0}};
            case "crystal_reef_memory" -> new int[][]{{-26, -3}, {-22, -9}, {-13, -13}, {-1, -15}, {12, -11},
                    {22, -6}, {26, 1}, {23, 8}, {17, 12}, {12, 10}, {12, 4}, {7, 1}, {0, 0}, {-6, 3}, {-8, 9}, {-6, 14}, {-13, 16}, {-23, 10}, {-27, 3}};
            case "cherry_isle_memory" -> new int[][]{{-24, -4}, {-20, -13}, {-12, -16}, {-6, -14}, {0, -21},
                    {10, -19}, {15, -12}, {24, -8}, {25, 0}, {18, 9}, {11, 10}, {5, 18}, {-4, 22}, {-11, 15}, {-19, 13}, {-23, 5}};
            case "archive_ruin_memory" -> new int[][]{{-25, -6}, {-19, -12}, {-8, -12}, {-8, -18}, {5, -18},
                    {5, -14}, {16, -14}, {16, -6}, {23, -6}, {23, 7}, {15, 7}, {15, 15}, {4, 15}, {4, 19}, {-9, 19}, {-9, 11}, {-19, 11}, {-19, 4}, {-25, 4}};
            default -> throw new IllegalArgumentException("Unknown dream island scene: " + id);
        };
        return new DreamIslandShape(outline, seed ^ (long) id.hashCode() * 0x9E3779B97F4A7C15L,
                id.equals("archive_ruin_memory"));
    }

    int minX() {
        return minX;
    }

    int maxX() {
        return maxX;
    }

    int minZ() {
        return minZ;
    }

    int maxZ() {
        return maxZ;
    }

    boolean contains(int x, int z) {
        boolean inside = false;
        for (int i = 0, previous = points.length - 1; i < points.length; previous = i++) {
            double[] a = points[i], b = points[previous];
            if ((a[1] > z) != (b[1] > z)
                    && x < (b[0] - a[0]) * (z - a[1]) / (b[1] - a[1]) + a[0])
                inside = !inside;
        }
        return inside || distanceToEdge(x, z) < .2;
    }

    double distanceToEdge(int x, int z) {
        double distance2 = Double.MAX_VALUE;
        for (int i = 0, previous = points.length - 1; i < points.length; previous = i++) {
            double[] a = points[i], b = points[previous];
            double dx = b[0] - a[0], dz = b[1] - a[1];
            double length2 = dx * dx + dz * dz;
            double fraction = length2 == 0 ? 0 : Math.clamp(((x - a[0]) * dx + (z - a[1]) * dz) / length2, 0, 1);
            double ex = x - a[0] - fraction * dx, ez = z - a[1] - fraction * dz;
            distance2 = Math.min(distance2, ex * ex + ez * ez);
        }
        return Math.sqrt(distance2);
    }

    int depthAt(int x, int z, int maximumDepth) {
        double edge = Math.min(1, distanceToEdge(x, z) / scale);
        double tip = Math.max(0, 1 - Math.hypot(x - tipX, z - tipZ) / (scale * 1.8));
        // The interior descends toward an offset keel; the exposed shore thins to one block.
        double taper = Math.min(edge, .67 * edge + .33 * tip);
        return 1 + (int) Math.floor((maximumDepth - 1) * Math.pow(taper, .82));
    }

    private static long mix(long value) {
        value = (value ^ value >>> 30) * 0xBF58476D1CE4E5B9L;
        value = (value ^ value >>> 27) * 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }
}
