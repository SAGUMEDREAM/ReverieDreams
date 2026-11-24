package cc.thonly.reverie_dreams.paper.world;

import java.util.Random;

/** Paper26.1.2 WorldgenRandom(XoroshiroRandomSource), including its BitRandomSource float-rounded nextDouble. */
final class GensokyoFeatureRandom extends Random {
    private long low, high;
    GensokyoFeatureRandom(long seed) { super(seed); }

    @Override public void setSeed(long seed) {
        long unmixed = seed ^ 7640891576956012809L;
        low = mix(unmixed); high = mix(unmixed - 7046029254386353131L);
        if ((low | high) == 0) { low = -7046029254386353131L; high = 7640891576956012809L; }
    }
    private static long mix(long value) {
        value = (value ^ value >>> 30) * -4658895280553007687L;
        value = (value ^ value >>> 27) * -7723592293110705685L;
        return value ^ value >>> 31;
    }
    @Override protected int next(int bits) {
        long a = low, b = high, result = Long.rotateLeft(a + b, 17) + a;
        b ^= a;
        low = Long.rotateLeft(a, 49) ^ b ^ b << 21;
        high = Long.rotateLeft(b, 28);
        return (int) (result >>> (64 - bits));
    }
    @Override public double nextDouble() {
        long value = ((long) next(26) << 27) + next(27);
        return (float) value * 1.110223E-16F;
    }
    long decorationSeed(long worldSeed, int blockX, int blockZ) {
        setSeed(worldSeed);
        long a = nextLong() | 1L, b = nextLong() | 1L;
        long result = blockX * a + blockZ * b ^ worldSeed;
        setSeed(result);
        return result;
    }
    void bandSeed(long decorationSeed, String name) { setSeed(decorationSeed + (name.hashCode() ^ 0x47534F52) + 60000L); }
}
