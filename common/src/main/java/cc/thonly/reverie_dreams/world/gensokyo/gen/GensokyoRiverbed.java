package cc.thonly.reverie_dreams.world.gensokyo.gen;

/**
 * Derived bathymetry only: the authored water-surface elevation never moves.
 */
public final class GensokyoRiverbed {
    public static final int MIN_DEPTH = 1;
    public static final int INTERIOR_MIN_DEPTH = 3;
    public static final int MAX_DEPTH = 8;

    private GensokyoRiverbed() {
    }

    @FunctionalInterface
    public interface WaterMask {
        boolean isWater(int x, int z);
    }

    @FunctionalInterface
    public interface LowBank {
        boolean isLowBank(int x, int z);
    }

    /**
     * Continuous shore shelves, including diagonal shores and small islands.
     */
    public static int depthAt(int x, int z, WaterMask water) {
        return depthAt(x, z, water, (bankX, bankZ) -> true);
    }

    /**
     * Low shores have wading steps; rock walls retain a deeper natural foot.
     */
    public static int depthAt(int x, int z, WaterMask water, LowBank lowBank) {
        int depth = depthAt(x, z);
        // Search complete square rings, not a few cardinal rays. The distance
        // field changes by at most one across neighbouring water columns.
        // Stop when a farther shore cannot reduce the interior noise depth.
        for (int distance = 1; distance < depth; distance++) {
            for (int offset = -distance; offset <= distance; offset++) {
                if (!water.isWater(x + offset, z - distance)) {
                    if (lowBank.isLowBank(x + offset, z - distance))
                        return distance;
                    depth = Math.min(depth, distance + 2);
                }
                if (!water.isWater(x + offset, z + distance)) {
                    if (lowBank.isLowBank(x + offset, z + distance))
                        return distance;
                    depth = Math.min(depth, distance + 2);
                }
            }
            for (int offset = -distance + 1; offset < distance; offset++) {
                if (!water.isWater(x - distance, z + offset)) {
                    if (lowBank.isLowBank(x - distance, z + offset))
                        return distance;
                    depth = Math.min(depth, distance + 2);
                }
                if (!water.isWater(x + distance, z + offset)) {
                    if (lowBank.isLowBank(x + distance, z + offset))
                        return distance;
                    depth = Math.min(depth, distance + 2);
                }
            }
        }
        return depth;
    }

    public static int depthAt(int x, int z) {
        double depth = 5.0D
                + noise(x, z, 32.0D, 0x6a09e667f3bcc909L) * 2.1D
                + noise(x, z, 10.0D, 0xbb67ae8584caa73bL) * 0.65D
                + noise(x, z, 3.0D, 0x3c6ef372fe94f82bL) * 0.2D;
        return Math.clamp((int) Math.round(depth), INTERIOR_MIN_DEPTH, MAX_DEPTH);
    }

    /**
     * 0 gravel, 1 sand, 2 clay, 3 stone; coherent deposits rather than per-block speckles.
     */
    public static int materialAt(int x, int z) {
        double deposit = noise(x, z, 23.0D, 0xa54ff53a5f1d36f1L);
        if (deposit < -0.38D)
            return 1;
        if (deposit > 0.45D)
            return 2;
        if (noise(x, z, 9.0D, 0x510e527fade682d1L) > 0.58D)
            return 3;
        return 0;
    }

    private static double noise(int x, int z, double scale, long salt) {
        double px = x / scale;
        double pz = z / scale;
        int x0 = (int) Math.floor(px);
        int z0 = (int) Math.floor(pz);
        double tx = smooth(px - x0);
        double tz = smooth(pz - z0);
        double north = lerp(hash(x0, z0, salt), hash(x0 + 1, z0, salt), tx);
        double south = lerp(hash(x0, z0 + 1, salt), hash(x0 + 1, z0 + 1, salt), tx);
        return lerp(north, south, tz);
    }

    private static double smooth(double t) {
        return t * t * (3.0D - 2.0D * t);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static double hash(int x, int z, long salt) {
        long value = x * 341873128712L ^ z * 132897987541L ^ salt;
        value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
        value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
        value ^= value >>> 31;
        return (value >>> 11) * 0x1.0p-53 * 2.0D - 1.0D;
    }
}
