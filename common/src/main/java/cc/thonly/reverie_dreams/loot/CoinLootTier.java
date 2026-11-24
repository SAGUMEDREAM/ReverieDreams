package cc.thonly.reverie_dreams.loot;

import net.minecraft.util.RandomSource;

/**
 * Ranges are copper values, never counts of randomly chosen denominations.
 */
public enum CoinLootTier {
    NONE(0, 0, 0),
    SMALL(35, 1, 2),
    COMMON(60, 2, 5),
    FAIRY(75, 3, 8),
    ELITE(85, 8, 16),
    WARDEN(100, 100, 160),
    WITHER(100, 150, 250);

    public final int chance, minimum, maximum;

    CoinLootTier(int chance, int minimum, int maximum) {
        this.chance = chance;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public int roll(RandomSource random) {
        if (chance == 0 || random.nextInt(100) >= chance)
            return 0;
        return random.nextIntBetweenInclusive(minimum, maximum);
    }
}
