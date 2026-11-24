package cc.thonly.reverie_dreams.world.gensokyo.gen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GensokyoRiverbankAccessTest {
    @Test void naturalCliffBanksKeepTheirDeepFoot() {
        GensokyoRiverbed.WaterMask halfPlane = (x, z) -> x >= 0;
        for (int z = -80; z <= 80; z++) {
            assertEquals(3, GensokyoRiverbed.depthAt(0, z, halfPlane, (x, bankZ) -> false));
            assertEquals(1, GensokyoRiverbed.depthAt(0, z, halfPlane, (x, bankZ) -> true));
        }
    }

    @Test void lowBayBlendsBackIntoDeepCliffWaterWithoutAnAbruptBedWall() {
        GensokyoRiverbed.WaterMask halfPlane = (x, z) -> x >= 0;
        GensokyoRiverbed.LowBank bay = (x, z) -> z >= 0;
        for (int x = 0; x < 8; x++) for (int z = -10; z < 10; z++) {
            int depth = GensokyoRiverbed.depthAt(x, z, halfPlane, bay);
            assertTrue(Math.abs(depth-GensokyoRiverbed.depthAt(x, z+1, halfPlane, bay)) <= 1);
        }
    }

    @Test void shorelineHasAnActualOneBlockWadingStep() {
        GensokyoRiverbed.WaterMask halfPlane = (x, z) -> x >= 0;
        for (int z = -80; z <= 80; z++) {
            assertEquals(1, GensokyoRiverbed.depthAt(0, z, halfPlane));
            assertEquals(2, GensokyoRiverbed.depthAt(1, z, halfPlane));
            assertEquals(3, GensokyoRiverbed.depthAt(2, z, halfPlane));
        }
    }

    @Test void diagonalLandAlsoProvidesTheShallowStep() {
        GensokyoRiverbed.WaterMask island = (x, z) -> x != 0 || z != 0;
        assertEquals(1, GensokyoRiverbed.depthAt(1, 1, island));
        assertEquals(2, GensokyoRiverbed.depthAt(2, 2, island));
    }

    @Test void interiorDepthAndMaterialAreUnaffectedByTheNewShoreRamp() {
        GensokyoRiverbed.WaterMask allWater = (x, z) -> true;
        for (int z = -100; z < 100; z += 3) for (int x = -100; x < 100; x += 3) {
            int interior = GensokyoRiverbed.depthAt(x, z);
            assertTrue(interior >= 3 && interior <= 8);
            assertEquals(interior, GensokyoRiverbed.depthAt(x, z, allWater));
        }
    }

    @Test void windingBanksAndNarrowChannelsHaveNoDeepVerticalLip() {
        GensokyoRiverbed.WaterMask river = (x, z) -> Math.abs(x - (int) Math.round(4*Math.sin(z/8.0))) <= 8;
        for (int z = -100; z <= 100; z++) for (int x = -13; x <= 13; x++) {
            if (!river.isWater(x, z)) continue;
            int depth = GensokyoRiverbed.depthAt(x, z, river);
            for (int[] move : new int[][]{{1, 0}, {0, 1}}) {
                if (river.isWater(x+move[0], z+move[1])) {
                    assertTrue(Math.abs(depth-GensokyoRiverbed.depthAt(x+move[0], z+move[1], river)) <= 1);
                }
            }
            for (int dz = -1; dz <= 1; dz++) for (int dx = -1; dx <= 1; dx++) {
                if (!river.isWater(x+dx, z+dz)) assertEquals(1, depth);
            }
        }
    }
}
