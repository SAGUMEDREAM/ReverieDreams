package cc.thonly.reverie_dreams.paper.world;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Target BiomeManager's zoom selection, used by source ore biome restrictions. */
final class GensokyoBiomeSampling {
    record Quart(int x, int y, int z) {}
    private final long zoomSeed;
    GensokyoBiomeSampling(long worldSeed) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(worldSeed).array());
            zoomSeed = ByteBuffer.wrap(digest).order(ByteOrder.LITTLE_ENDIAN).getLong();
        } catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }
    Quart quart(int x, int y, int z) {
        int sx = x - 2, sy = y - 2, sz = z - 2, qx = sx >> 2, qy = sy >> 2, qz = sz >> 2;
        double dx = (sx & 3) / 4D, dy = (sy & 3) / 4D, dz = (sz & 3) / 4D;
        int closest = 0; double minimum = Double.POSITIVE_INFINITY;
        for (int corner = 0; corner < 8; corner++) {
            boolean west = (corner & 4) == 0, down = (corner & 2) == 0, north = (corner & 1) == 0;
            double distance = distance(west ? qx : qx + 1, down ? qy : qy + 1, north ? qz : qz + 1,
                    west ? dx : dx - 1, down ? dy : dy - 1, north ? dz : dz - 1);
            if (minimum > distance) { minimum = distance; closest = corner; }
        }
        return new Quart((closest & 4) == 0 ? qx : qx + 1, (closest & 2) == 0 ? qy : qy + 1, (closest & 1) == 0 ? qz : qz + 1);
    }
    private double distance(int x, int y, int z, double dx, double dy, double dz) {
        long seed = zoomSeed;
        seed = next(seed, x); seed = next(seed, y); seed = next(seed, z);
        seed = next(seed, x); seed = next(seed, y); seed = next(seed, z);
        double fx = fiddle(seed); seed = next(seed, zoomSeed);
        double fy = fiddle(seed); seed = next(seed, zoomSeed);
        double fz = fiddle(seed);
        return square(dz + fz) + square(dy + fy) + square(dx + fx);
    }
    private static long next(long seed, long salt) { return seed * (seed * 6364136223846793005L + 1442695040888963407L) + salt; }
    private static double fiddle(long seed) { return ((seed >> 24 & 1023L) - 512L) * 8.7890625E-4D; }
    private static double square(double value) { return value * value; }
}
