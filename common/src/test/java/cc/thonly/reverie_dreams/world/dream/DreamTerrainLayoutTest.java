package cc.thonly.reverie_dreams.world.dream;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Geometry contracts do not initialize the mod registry or allocate a whole voxel volume. */
class DreamTerrainLayoutTest {
    @Test void allLandformsOccurAndRemainInsideTheSeparatedActivityBandAndTheirOwnCell() {
        var kinds = EnumSet.noneOf(DreamTerrainLayout.Kind.class);
        for (long seed = 0; seed < 24; seed++) for (int cx : new int[]{-1, 0, 2}) {
            int cz = -cx;
            var islands = DreamTerrainLayout.candidateIslands(seed, cx, cz);
            assertTrue(islands.size() == 2 || islands.size() == 3);
            for (var island : islands) {
                kinds.add(island.kind());
                assertTrue(island.minX() >= cx * 512 - 246 && island.maxX() <= cx * 512 + 246);
                assertTrue(island.minZ() >= cz * 512 - 246 && island.maxZ() <= cz * 512 + 246);
                assertTrue(island.minY() >= DreamGridLayout.ACTIVITY_MIN_Y);
                assertTrue(island.maxY() <= DreamGridLayout.ACTIVITY_MAX_Y);
                int minimumSurface = Integer.MAX_VALUE, maximumSurface = Integer.MIN_VALUE;
                for (int x = island.minX(); x <= island.maxX(); x++) for (int z = island.minZ(); z <= island.maxZ(); z++) {
                    if (!island.containsColumn(x, z)) continue;
                    assertTrue(island.bottomY(x, z) <= island.surfaceY(x, z));
                    assertTrue(island.bottomY(x, z) >= island.minY());
                    assertTrue(island.surfaceY(x, z) <= island.maxY());
                    minimumSurface = Math.min(minimumSurface, island.surfaceY(x, z));
                    maximumSurface = Math.max(maximumSurface, island.surfaceY(x, z));
                }
                assertTrue(maximumSurface - minimumSurface >= switch (island.kind()) {
                    case LOW_HILL -> 5;
                    case RIDGE -> 12;
                    case LONE_PEAK -> 20;
                }, "An island must have real surface relief, not just a tapered flat platform: " + island.id());
                assertFalse(island.containsColumn(island.maxX() + 1, island.maxZ() + 1));
            }
        }
        assertEquals(EnumSet.allOf(DreamTerrainLayout.Kind.class), kinds);
    }

    @Test void summitRoutesAreContinuousThreeWideAndNeverRequireAFullBlockJump() {
        for (long seed = 0; seed < 24; seed++) for (var island : DreamTerrainLayout.candidateIslands(seed, 0, 0)) {
            List<BlockPos> route = island.route();
            assertTrue(route.size() >= 25);
            var seen = new HashSet<BlockPos>();
            int longestStraight = 0, straight = 0;
            int previousDx = 0, previousDz = 0;
            double longestRise = 0, straightStart = 0;
            for (int i = 0; i < route.size(); i++) {
                BlockPos current = route.get(i);
                assertTrue(seen.add(current), "No route loop or repeated point");
                assertEquals(current.getY(), island.surfaceY(current.getX(), current.getZ()));
                for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++)
                    assertTrue(island.containsColumn(current.getX() + dx, current.getZ() + dz), "Three-wide route stays on supported land");
                if (i == 0) continue;
                BlockPos previous = route.get(i - 1);
                int dx = current.getX() - previous.getX(), dz = current.getZ() - previous.getZ();
                assertEquals(1, Math.abs(dx) + Math.abs(dz));
                double previousHeight = island.surfaceHeight(previous.getX(), previous.getZ());
                double height = island.surfaceHeight(current.getX(), current.getZ());
                assertTrue(height >= previousHeight && height - previousHeight <= .5,
                        "Both ascending and descending traversal use at most half-block steps");
                if (dx == previousDx && dz == previousDz) straight++;
                else { straight = 1; straightStart = previousHeight; }
                if (straight > longestStraight) { longestStraight = straight; longestRise = height - straightStart; }
                previousDx = dx; previousDz = dz;
            }
            assertTrue(longestStraight >= 6 && longestRise >= 1,
                    "A real straight ascending section is available for native no-auto-jump traversal QA");
            double peak = Double.NEGATIVE_INFINITY;
            for (int x = island.minX(); x <= island.maxX(); x++) for (int z = island.minZ(); z <= island.maxZ(); z++)
                if (island.containsColumn(x, z)) peak = Math.max(peak, island.surfaceHeight(x, z));
            BlockPos end = route.getLast();
            assertEquals(peak, island.surfaceHeight(end.getX(), end.getZ()));
            assertThrows(UnsupportedOperationException.class, () -> route.clear());
        }
    }

    @Test void seedAndAbsoluteCoordinatesDetermineGeometryWithoutRequestOrderOrRegistryState() {
        var initial = DreamTerrainLayout.candidateIslands(913, -2, 3);
        long fingerprint = fingerprint(initial);
        for (int i = 0; i < 12; i++) DreamTerrainLayout.candidateIslands(1000 + i, i, -i);
        assertEquals(fingerprint, fingerprint(DreamTerrainLayout.candidateIslands(913, -2, 3)));
        assertNotEquals(fingerprint, fingerprint(DreamTerrainLayout.candidateIslands(914, -2, 3)));
        assertNotEquals(fingerprint, fingerprint(DreamTerrainLayout.candidateIslands(913, 2, -3)));
        assertThrows(UnsupportedOperationException.class, () -> initial.clear());
    }

    private static long fingerprint(List<DreamTerrainLayout.Island> islands) {
        long result = 1;
        for (var island : islands) {
            result = result * 31 + island.kind().ordinal();
            for (int x = island.minX(); x <= island.maxX(); x++) for (int z = island.minZ(); z <= island.maxZ(); z++) {
                if (!island.containsColumn(x, z)) continue;
                result = result * 31 + x;
                result = result * 31 + z;
                result = result * 31 + island.bottomY(x, z);
                result = result * 31 + Double.doubleToLongBits(island.surfaceHeight(x, z));
            }
        }
        return result;
    }
}
