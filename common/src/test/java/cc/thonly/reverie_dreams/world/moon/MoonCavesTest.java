package cc.thonly.reverie_dreams.world.moon;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class MoonCavesTest {
    @Test void entranceFormsAContinuousDescendingPassageIntoNegativeY() {
        var tunnel = new MoonCaves.Passage(false, 0, 0, 1, 0, 128, 90, 32);
        MoonCaves.Cut previous = null;
        for (int x = 0; x <= 128; x++) {
            int z = (int) Math.round(Math.sin(x / 128.0 * Math.PI * 2) * 6);
            var cut = tunnel.cut(x, z, 32, true);
            assertNotNull(cut, "Unbroken tunnel at x=" + x);
            if (previous != null) assertTrue(Math.max(previous.bottom(), cut.bottom()) <= Math.min(previous.top(), cut.top()));
            if (x == 0) assertTrue(cut.contains(32), "Mouth opens through the surface");
            if (x == 128) assertTrue(cut.top() < -32, "Reach the newly extended depth");
            assertTrue(cut.bottom() > MoonTerrain.MIN_Y + 10);
            previous = cut;
        }
    }

    @Test void ravinesOpenAtTheSurfaceButWetAndLegacyBuffersRetainTheirRoof() {
        var ravine = new MoonCaves.Passage(true, -160, -80, 1, 0, 160, 48, 32);
        var open = ravine.cut(-80, -80, 32, true);
        assertNotNull(open);
        assertTrue(open.contains(32));
        assertTrue(open.bottom() <= -15);
        for (var passage : new MoonCaves.Passage[]{ravine,
                new MoonCaves.Passage(false, -160, -80, 1, 0, 160, 90, 32)}) {
            for (int x = -160; x <= 0; x++) for (int z = -96; z <= -64; z++) {
                for (int floor : new int[]{-114, -48, 32}) {
                    var sealed = passage.cut(x, z, floor, false);
                    if (sealed != null) {
                        assertTrue(sealed.top() <= floor - 12);
                        assertTrue(sealed.bottom() > MoonTerrain.MIN_Y + 10);
                    }
                }
            }
        }
    }

    @Test void roundedDiagonalTunnelEndpointsStayOpen() {
        for (int index = 0; index < 48; index++) {
            double angle = index * Math.PI / 24;
            var tunnel = new MoonCaves.Passage(false, -193, -377, Math.cos(angle), Math.sin(angle), 137.4, 84, 32);
            int x = (int) Math.round(tunnel.x() + tunnel.dx() * tunnel.length());
            int z = (int) Math.round(tunnel.z() + tunnel.dz() * tunnel.length());
            var end = tunnel.cut(x, z, 32, true);
            assertNotNull(end);
            assertTrue(end.contains(-51));
        }
    }

    @Test void seededPassagesCrossNegativeChunkEdgesWithoutChangingShape() {
        var all = new HashSet<MoonCaves.Passage>();
        boolean crossed = false;
        for (int cx = -32; cx <= 32; cx += 2) for (int cz = -32; cz <= 32; cz += 2) {
            var plan = MoonCaves.passages(20261105, cx, cz, (x, z) -> 32);
            assertEquals(plan, MoonCaves.passages(20261105, cx, cz, (x, z) -> 32));
            all.addAll(plan);
            var next = MoonCaves.passages(20261105, cx + 1, cz, (x, z) -> 32);
            int edge = cx * 16 + 16;
            for (var p : plan) for (int z = cz * 16; z < cz * 16 + 16; z++) {
                if (p.cut(edge, z, 32, true) != null) {
                    assertTrue(next.contains(p), "Missing neighbouring chunk's continuation");
                    crossed = true;
                }
            }
        }
        assertTrue(crossed);
        assertTrue(all.stream().anyMatch(MoonCaves.Passage::ravine));
        assertTrue(all.stream().anyMatch(p -> !p.ravine()));
    }

    @Test void seaAndIceHaveNoSurfaceOpeningsAndUpperUndergroundCavesExist() {
        int protectedColumns = 0, dryColumns = 0, upperCaves = 0, deepCaves = 0;
        for (int x = -2048; x <= 2048; x += 31) for (int z = -2048; z <= 2048; z += 37) {
            var sample = MoonTerrain.sample(20261105, x, z);
            boolean dry = MoonCaves.canOpenSurface(20261105, x, z);
            if (sample.seaWeight() > 0 || sample.iceWeight() > 0) {
                assertFalse(dry);
                protectedColumns++;
            }
            if (dry) dryColumns++;
            if (MoonTerrain.cave(20261105, x, -16, z, 32)) upperCaves++;
            if (MoonTerrain.cave(20261105, x, -64, z, 32)) deepCaves++;
        }
        assertTrue(protectedColumns > 100 && dryColumns > 100);
        assertTrue(upperCaves > 10 && deepCaves > 10);
    }
}
