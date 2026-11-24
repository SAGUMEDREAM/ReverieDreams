package cc.thonly.reverie_dreams.paper.world.forest;

import java.util.Random;

/** Paper26.1.2 WorldgenRandom(XoroshiroRandomSource), including its BitRandomSource float-rounded nextDouble. */
public final class GensokyoForestRandom extends Random {
    private long low, high;
    public GensokyoForestRandom(long seed) { super(seed); }

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
    public long decorationSeed(long worldSeed, int blockX, int blockZ) {
        setSeed(worldSeed);
        long a = nextLong() | 1L, b = nextLong() | 1L;
        long result = blockX * a + blockZ * b ^ worldSeed;
        setSeed(result);
        return result;
    }
    public void sourceFeatureSeed(long decorationSeed,int featureIndex) { setSeed(decorationSeed + featureIndex + 90000L); }
    public void adaptationSeed(long decorationSeed,String biomeId){ setSeed(decorationSeed + (biomeId.hashCode() ^ 0x47534654) + 90000L); }
}
