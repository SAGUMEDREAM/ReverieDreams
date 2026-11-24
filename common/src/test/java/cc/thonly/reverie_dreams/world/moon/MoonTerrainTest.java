package cc.thonly.reverie_dreams.world.moon;

import org.junit.jupiter.api.Test;
import java.util.EnumMap;
import static org.junit.jupiter.api.Assertions.*;

class MoonTerrainTest {
    @Test void allFourRegionsExistOnBothSidesOfTheOriginAndSeedsMatter() {
        var counts = new EnumMap<MoonTerrain.Region, Integer>(MoonTerrain.Region.class);
        int changed = 0;
        for (int x = -4096; x <= 4096; x += 64) for (int z = -4096; z <= 4096; z += 64) {
            var first = MoonTerrain.sample(20261105L, x, z);
            counts.merge(first.region(), 1, Integer::sum);
            assertEquals(first, MoonTerrain.sample(20261105L, x, z));
            if (!first.equals(MoonTerrain.sample(191919L, x, z))) changed++;
        }
        assertEquals(4, counts.size(), counts.toString());
        assertTrue(counts.values().stream().allMatch(n -> n > 30), counts.toString());
        assertTrue(changed > 10000);
    }
    @Test void reliefNeverRaisesTheLegacySurfaceAndMaskRestoresItExactly() {
        for (int x = -2048; x < 2048; x += 47) for (int z = -2048; z < 2048; z += 53) {
            var sample = MoonTerrain.sample(71, x, z);
            for (int old : new int[]{-32, 0, 32, 64, 127}) {
                int previous = old;
                for (double strength : new double[]{0, 0.1, 0.5, 1}) {
                    int floor = sample.floor(old, strength);
                    assertTrue(floor <= previous && floor >= MoonTerrain.MIN_Y + 12);
                    assertTrue(sample.waterTop(old, floor, strength) <= old);
                    previous = floor;
                }
                assertEquals(old, sample.floor(old, 0));
            }
        }
        assertEquals(224, MoonTerrain.MIN_Y + MoonTerrain.HEIGHT);
        assertEquals(0, MoonTerrain.HEIGHT % 16);
    }
    @Test void reliefIsContinuousAtCellAndNegativeCoordinateBoundaries() {
        for (int x = -1536; x <= 1536; x += 16) for (int z = -1536; z <= 1536; z += 16) {
            double center = MoonTerrain.sample(4, x, z).depression();
            assertTrue(Math.abs(center - MoonTerrain.sample(4, x + 1, z).depression()) < 4);
            assertTrue(Math.abs(center - MoonTerrain.sample(4, x, z + 1).depression()) < 4);
        }
    }
    @Test void seasHaveSealedShoresAndNeverShareTheirBasinsWithDryIceCraters() {
        for (long seed : new long[]{76123, 20261105}) {
            for (int x = -4096; x < 4096; x += 31) for (int z = -4096; z < 4096; z += 37) {
                var sample = MoonTerrain.sample(seed, x, z);
                if (sample.seaWeight() > 0) assertEquals(0, sample.iceWeight());
                if (sample.seaWeight() == 0 && sample.iceWeight() == 0)
                    assertTrue(sample.floor(32, 1) >= MoonTerrain.SEA_LEVEL + 4);
                for (int floor : new int[]{-100, -50, 32}) for (int y = -128; y <= floor; y++)
                    if (y > floor - 12 || y <= -118) assertFalse(MoonTerrain.cave(seed, x, y, z, floor));
            }
        }
    }
}

