package cc.thonly.reverie_dreams.loot;

import cc.thonly.reverie_dreams.util.item.CoinValues;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CoinEconomyTest {
    @Test void exchangeValuesStayCompatibleWithExistingWallets() {
        assertEquals(10 * CoinValues.COPPER, CoinValues.SILVER);
        assertEquals(10 * CoinValues.SILVER, CoinValues.GOLD);
        assertTrue(CoinValues.adjacent(1, 10));
        assertTrue(CoinValues.adjacent(100, 10));
        assertFalse(CoinValues.adjacent(1, 100)); // 100 copper exceeds its 96-item stack limit.
        assertFalse(CoinValues.adjacent(0, 0));
        assertEquals(0, CoinValues.denomination("another_mod", "gold_coin"));
        assertEquals(0, CoinValues.denomination("reverie_dreams", "point"));
    }

    @Test void rewardDistributionHasBoundedValueAndExpectedAverage() {
        for (var tier : CoinLootTier.values()) {
            var random = RandomSource.create(20261106L);
            long total = 0;
            int paid = 0, samples = 100_000;
            for (int i = 0; i < samples; i++) {
                int value = tier.roll(random);
                assertTrue(value == 0 || value >= tier.minimum && value <= tier.maximum, tier.name());
                total += value;
                if (value > 0) paid++;
            }
            double probability = tier.chance / 100.0;
            assertEquals(probability, paid / (double) samples, .006, tier.name());
            assertEquals(probability * (tier.minimum + tier.maximum) / 2.0,
                    total / (double) samples, Math.max(.03, tier.maximum * .006), tier.name());
        }
    }
}
