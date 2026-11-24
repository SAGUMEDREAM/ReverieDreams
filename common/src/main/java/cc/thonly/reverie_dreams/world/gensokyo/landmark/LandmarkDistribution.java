package cc.thonly.reverie_dreams.world.gensokyo.landmark;

/**
 * Smooth world-space fields: broad flower patches, irregular edges, and connected open lanes.
 */
public final class LandmarkDistribution {
    public static double field(long seed, double x, double z, double scale) {
        x /= scale;
        z /= scale;
        long ix = (long) Math.floor(x), iz = (long) Math.floor(z);
        double fx = x - ix, fz = z - iz;
        fx = fx * fx * (3 - 2 * fx);
        fz = fz * fz * (3 - 2 * fz);
        double a = hash(seed, ix, iz) * (1 - fx) + hash(seed, ix + 1, iz) * fx;
        double b = hash(seed, ix, iz + 1) * (1 - fx) + hash(seed, ix + 1, iz + 1) * fx;
        return a * (1 - fz) + b * fz;
    }

    private static double hash(long seed, long x, long z) {
        long h = seed + x * 341873128712L + z * 132897987541L;
        h = (h ^ (h >>> 30)) * 0xbf58476d1ce4e5b9L;
        h = (h ^ (h >>> 27)) * 0x94d049bb133111ebL;
        return ((h ^ (h >>> 31)) >>> 11) * 0x1.0p-53;
    }

    public static boolean openLane(long seed, int x, int z) {
        double warp = (field(seed ^ 0x57415250, x, z, 128) - .5) * 24;
        double lane = Math.sin((x + warp) * .055) + Math.sin((z - warp) * .049);
        return Math.abs(lane) < .10;
    }

    public static double density(LandmarkVegetationConfiguration.Theme theme, long seed, int x, int z) {
        if (openLane(seed, x, z))
            return 0;
        double broad = field(seed ^ 0x464c4f574552L, x, z, 44);
        return switch (theme) {
            case PHANTOM_MEADOW -> .10 + .46 * Math.max(0, (broad - .25) / .75);
            case MUENZUKA -> .05 + .52 * Math.max(0, (broad - .30) / .70);
            case GARDEN_OF_THE_SUN -> .22 + .63 * Math.max(0, (broad - .20) / .80);
        };
    }

    private LandmarkDistribution() {
    }
}
