package cc.thonly.reverie_dreams.world.gensokyo.bamboo;

import cc.thonly.reverie_dreams.world.gensokyo.landmark.LandmarkDistribution;

/**
 * World-space clumps and a connected, curved lattice. No chunk-local stripes or recurring teleports.
 */
public final class BambooForestDistribution {
    public static final int MAX_NATURAL_HEIGHT = 48;

    public record Point(double x, double z) {
    }

    public static double hash(long seed, int x, int z) {
        long h = seed + x * 341873128712L + z * 132897987541L;
        h = (h ^ (h >>> 30)) * 0xbf58476d1ce4e5b9L;
        h = (h ^ (h >>> 27)) * 0x94d049bb133111ebL;
        return ((h ^ (h >>> 31)) >>> 11) * 0x1.0p-53;
    }

    public static Point junction(long seed, int x, int z) {
        return new Point(x * 128 + 64 + (hash(seed ^ 0x4e4f4445, x, z) - .5) * 44, z * 128 + 64 + (hash(seed ^ 0x4a554e43, x, z) - .5) * 44);
    }

    private static double segment(double x, double z, double ax, double az, double bx, double bz) {
        double dx = bx - ax, dz = bz - az, t = Math.clamp(((x - ax) * dx + (z - az) * dz) / (dx * dx + dz * dz), 0, 1);
        return Math.hypot(x - ax - t * dx, z - az - t * dz);
    }

    private static double edge(long seed, int cx, int cz, boolean east, double x, double z) {
        Point a = junction(seed, cx, cz), b = junction(seed, cx + (east ? 1 : 0), cz + (east ? 0 : 1));
        double dx = b.x - a.x, dz = b.z - a.z, length = Math.hypot(dx, dz);
        double bend = (hash(seed ^ (east ? 0x45415354 : 0x534f5554), cx, cz) - .5) * 56;
        double mx = (a.x + b.x) * .5 - dz / length * bend, mz = (a.z + b.z) * .5 + dx / length * bend;
        double best = Double.MAX_VALUE, px = a.x, pz = a.z;
        for (int i = 1; i <= 12; i++) {
            double t = i / 12.0, u = 1 - t, qx = u * u * a.x + 2 * u * t * mx + t * t * b.x, qz = u * u * a.z + 2 * u * t * mz + t * t * b.z;
            best = Math.min(best, segment(x, z, px, pz, qx, qz));
            px = qx;
            pz = qz;
        }
        return best;
    }

    public static double pathDistance(long seed, double x, double z) {
        int cx = Math.floorDiv((int) Math.floor(x), 128), cz = Math.floorDiv((int) Math.floor(z), 128);
        double best = Double.MAX_VALUE;
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++) {
                best = Math.min(best, edge(seed, cx + dx, cz + dz, true, x, z));
                best = Math.min(best, edge(seed, cx + dx, cz + dz, false, x, z));
            }
        return best;
    }

    public static boolean glade(long seed, int x, int z) {
        int cx = Math.floorDiv(x, 128), cz = Math.floorDiv(z, 128);
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++) {
                int gx = cx + dx, gz = cz + dz;
                if (hash(seed ^ 0x474c4144, gx, gz) > .38)
                    continue;
                Point p = junction(seed, gx, gz);
                double radius = 7 + hash(seed ^ 0x52414449, gx, gz) * 6;
                if (Math.hypot(x - p.x, z - p.z) < radius)
                    return true;
            }
        return false;
    }

    public static boolean open(long seed, int x, int z) {
        return pathDistance(seed, x + .5, z + .5) < 2.0 || glade(seed, x, z);
    }

    public static double density(long seed, int x, int z) {
        return .32 + .62 * LandmarkDistribution.field(seed ^ 0x44454e53, x, z, 56);
    }

    public static int height(long seed, int x, int z) {
        double age = hash(seed ^ 0x414745, x, z), variation = hash(seed ^ 0x48454947, x, z);
        return age < .12 ? 12 + (int) (variation * 7) : age > .80 ? 40 + (int) (variation * 9) : 28 + (int) (variation * 11);
    }

    private BambooForestDistribution() {
    }
}
