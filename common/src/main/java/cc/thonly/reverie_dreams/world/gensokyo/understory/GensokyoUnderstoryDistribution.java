package cc.thonly.reverie_dreams.world.gensokyo.understory;

import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestType;

/**
 * Shared, world-seeded habitat field: all woody scales leave the same open areas.
 */
public final class GensokyoUnderstoryDistribution {
    public record Density(int leafAttempts, int shrubRarity, int thicketRarity, int floorAttempts, double threshold) {
    }

    public static Density density(GensokyoForestType theme) {
        return switch (theme) {
            case FOREST -> new Density(2, 1, 8, 1, .48);
            case BIRCH_FOREST -> new Density(1, 2, 14, 1, .57);
            case TAIGA -> new Density(1, 2, 12, 1, .52);
            case CHERRY_GROVE -> new Density(1, 3, 16, 1, .58);
            case DARK_FOREST -> new Density(2, 1, 7, 1, .44);
            case MAGIC_FOREST -> new Density(3, 1, 6, 2, .42);
            case MAPLE_FOREST -> new Density(2, 2, 10, 1, .50);
            case JUNGLE -> new Density(3, 1, 7, 2, .46);
        };
    }

    public static double habitat(long seed, int x, int z) {
        return .65 * noise(seed ^ 0x4387f36a291bL, x / 48.0, z / 48.0)
                + .35 * noise(seed ^ 0x156a8c49e2dfL, x / 17.0, z / 17.0);
    }

    public static boolean allows(GensokyoForestType theme, GensokyoUnderstoryConfiguration.Kind kind, long seed, int x, int z) {
        double threshold = density(theme).threshold();
        return habitat(seed, x, z) >= switch (kind) {
            case LEAF_PATCH -> threshold;
            case SHRUB -> threshold + .07;
            case THICKET -> threshold + .13;
            case FLOOR_PATCH -> .25;
        };
    }

    private static double noise(long seed, double x, double z) {
        int ix = (int) Math.floor(x), iz = (int) Math.floor(z);
        double fx = smooth(x - ix), fz = smooth(z - iz);
        return lerp(fz, lerp(fx, value(seed, ix, iz), value(seed, ix + 1, iz)),
                lerp(fx, value(seed, ix, iz + 1), value(seed, ix + 1, iz + 1)));
    }

    private static double smooth(double value) {
        return value * value * value * (value * (value * 6 - 15) + 10);
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static double value(long seed, int x, int z) {
        long hash = seed ^ x * 0x632be59bd9b4e019L ^ z * 0x9e3779b97f4a7c15L;
        hash = (hash ^ (hash >>> 30)) * 0xbf58476d1ce4e5b9L;
        hash = (hash ^ (hash >>> 27)) * 0x94d049bb133111ebL;
        return ((hash ^ (hash >>> 31)) >>> 11) * 0x1.0p-53;
    }

    private GensokyoUnderstoryDistribution() {
    }
}
