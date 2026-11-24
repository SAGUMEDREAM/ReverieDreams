package cc.thonly.reverie_dreams.world.moon;

/**
 * Seeded, continuous lunar relief. All relief is subtractive relative to the old surface.
 */
public final class MoonTerrain {
    public static final int MIN_Y = -128;
    public static final int MAX_Y = 224;
    public static final int HEIGHT = MAX_Y - MIN_Y;
    public static final int LEGACY_MIN_Y = -32;
    public static final int SEA_LEVEL = -16;

    public enum Region {HIGHLANDS, BASALT_MARE, SHADOW_ICE_CRATER, SEA_BOUNDARY}

    public record Sample(Region region, double depression, double seaWeight, double iceWeight) {
        public int floor(int oldSurface, double strength) {
            double dryFloor = oldSurface - (depression - seaWeight * 62);
            // The dry shore stays above the sea. Only the continuous wet-basin
            // weight cuts through it; otherwise a dry neighbouring column leaks.
            if (iceWeight == 0)
                dryFloor = Math.max(Math.min(oldSurface, SEA_LEVEL + 4), dryFloor);
            double target = Math.min(oldSurface, dryFloor - seaWeight * 62);
            return Math.max(MIN_Y + 12, oldSurface - (int) Math.round((oldSurface - target) * clamp(strength)));
        }

        public int waterTop(int oldSurface, int floor, double strength) {
            return seaWeight > 0 && strength > 0 && floor < SEA_LEVEL
                    ? Math.min(oldSurface, SEA_LEVEL) : floor;
        }
    }

    private MoonTerrain() {
    }

    public static Sample sample(long seed, int x, int z) {
        double basin = noise(seed, x, z, 768) * 0.75 + noise(seed + 19, x, z, 224) * 0.25;
        double mare = 1 - smooth(-0.30, 0.22, basin);
        double sea = 1 - smooth(-0.52, -0.27, basin);
        double crater = crater(seed, x, z, 144, false);
        // Ice craters belong to the highlands. Fade them out before the wet basins so a
        // dry crater can never punch an open wall into a neighbouring sea column.
        double ice = crater(seed + 9187, x, z, 512, true) * (1 - smooth(0.05, 0.45, mare));
        double depression = 2 + mare * 30 + sea * 62 + crater * 14 + ice * 88;
        Region region = ice > 0.20 ? Region.SHADOW_ICE_CRATER
                : sea > 0.08 ? Region.SEA_BOUNDARY
                : mare > 0.48 ? Region.BASALT_MARE : Region.HIGHLANDS;
        return new Sample(region, depression, sea, ice);
    }

    private static double crater(long seed, int x, int z, int spacing, boolean sparse) {
        int cx = Math.floorDiv(x, spacing), cz = Math.floorDiv(z, spacing);
        double bowl = 0;
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++) {
                int gx = cx + dx, gz = cz + dz;
                if (sparse && unit(seed + 3, gx, gz) > 0.38)
                    continue;
                double px = (gx + 0.18 + unit(seed, gx, gz) * 0.64) * spacing;
                double pz = (gz + 0.18 + unit(seed + 1, gx, gz) * 0.64) * spacing;
                double radius = spacing * (sparse ? 0.13 : 0.15) + unit(seed + 2, gx, gz) * spacing * 0.16;
                double distance = Math.hypot(x - px, z - pz) / radius;
                bowl = Math.max(bowl, 1 - smooth(0, 1, distance));
            }
        return bowl;
    }

    public static double noise(long seed, double x, double z, double scale) {
        double sx = x / scale, sz = z / scale;
        int ix = (int) Math.floor(sx), iz = (int) Math.floor(sz);
        double tx = smooth(0, 1, sx - ix), tz = smooth(0, 1, sz - iz);
        return lerp(tz, lerp(tx, unit(seed, ix, iz), unit(seed, ix + 1, iz)),
                lerp(tx, unit(seed, ix, iz + 1), unit(seed, ix + 1, iz + 1))) * 2 - 1;
    }

    /**
     * Continuous tunnels with a thick roof below every basin and a protected bottom.
     */
    public static boolean cave(long seed, int x, int y, int z, int floor) {
        if (y <= MIN_Y + 10 || y > floor - 12)
            return false;
        double yy = y / 48.0;
        int layer = (int) Math.floor(yy);
        double t = smooth(0, 1, yy - layer);
        double first = lerp(t, noise(seed + layer * 31L, x, z, 80), noise(seed + (layer + 1) * 31L, x, z, 80));
        double second = lerp(t, noise(seed + 613 + layer * 41L, x, z, 64), noise(seed + 613 + (layer + 1) * 41L, x, z, 64));
        return Math.abs(first) < 0.10 && Math.abs(second) < 0.16;
    }

    public static double unit(long seed, int x, int z) {
        long h = seed ^ (x * 0x9E3779B97F4A7C15L) ^ (z * 0xC2B2AE3D27D4EB4FL);
        h = (h ^ h >>> 30) * 0xBF58476D1CE4E5B9L;
        h = (h ^ h >>> 27) * 0x94D049BB133111EBL;
        return ((h ^ h >>> 31) >>> 11) * 0x1.0p-53;
    }

    public static double smooth(double from, double to, double value) {
        double t = clamp((value - from) / (to - from));
        return t * t * (3 - 2 * t);
    }

    private static double clamp(double n) {
        return Math.max(0, Math.min(1, n));
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }
}
