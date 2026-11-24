package cc.thonly.reverie_dreams.world.gensokyo.gen;

import cc.thonly.reverie_dreams.world.gensokyo.GensokyoBoundaryCollision;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class GensokyoBorderTerrainTest {
    record FlatMap(int width, int depth, int originX, int originZ, int minY, int worldHeight, int seaLevel,
                   int top, String surface, String biome) implements GensokyoBorderTerrain.MapView {
        @Override public int heightAt(int x, int z) { return top; }
        @Override public String surfaceAt(int x, int z) { return surface; }
        @Override public String biomeAt(int x, int z) { return biome; }
    }
    static FlatMap map(String surface, String biome) {
        return new FlatMap(384, 256, -127, 75, 0, 1536, 1024, 1050, surface, biome);
    }
    @Test void allFourEdgesMeetAuthoredHeightAndCornersStayContinuous() {
        var m = map("minecraft:grass_block", "reverie_dreams:gensokyo_forest");
        var t = new GensokyoBorderTerrain(m);
        for (int z = 75; z < 331; z += 7) {
            assertEquals(1050, t.sample(-128, z).groundY());
            assertEquals(1050, t.sample(257, z).groundY());
        }
        for (int x = -127; x < 257; x += 7) {
            assertEquals(1050, t.sample(x, 74).groundY());
            assertEquals(1050, t.sample(x, 331).groundY());
        }
        assertTrue(Math.abs(t.sample(-128, 74).groundY() - 1050) <= 1);
        assertEquals(Math.sqrt(2), t.distance(-128, 74), 1e-9);
    }
    @Test void channelsKeepWaterLevelAndHaveGroundUnderTheirWholeWaterColumn() {
        var t = new GensokyoBorderTerrain(map("minecraft:water", "minecraft:river"));
        for (int d = 1; d <= 24; d++) {
            var c = t.sample(-127 - d, 170);
            assertTrue(c.water(), "distance=" + d);
            assertEquals(1050, c.waterY());
            assertTrue(c.waterY() - c.groundY() >= 3);
        }
        assertEquals(1047, t.sample(-128, 170).groundY());
        assertFalse(t.sample(-600, 170).water());
        int previous = t.sample(-128, 170).groundY();
        for (int d = 2; d < 500; d++) {
            int next = t.sample(-127 - d, 170).groundY();
            assertTrue(Math.abs(next - previous) <= 5, "abrupt river closure at " + d);
            previous = next;
        }
    }
    @Test void flowersAndFarmsBecomeSoilAndTransitionDoesNotStopAtTwentyFive() {
        for (var input : new String[]{"minecraft:poppy", "minecraft:red_tulip", "reverie_dreams:paddy_field"}) {
            var t = new GensokyoBorderTerrain(map(input, "minecraft:plains"));
            assertEquals("minecraft:grass_block", t.sample(-128, 170).surface());
        }
        var t = new GensokyoBorderTerrain(new FlatMap(384, 4096, -127, 75, 0, 1536, 1024, 1050, "minecraft:red_sand", "minecraft:badlands"));
        int a = 0, b = 0;
        for (int z = 75; z < 4171; z++) {
            if (t.sample(-151, z).surface().equals("minecraft:red_sand")) a++;
            if (t.sample(-152, z).surface().equals("minecraft:red_sand")) b++;
        }
        assertTrue(Math.abs(a - b) < 4096 * .12, a + " vs " + b);
        assertTrue(a > 2048 && b > 2048);
        assertNotEquals("minecraft:red_sand", t.sample(-500, 170).surface());
    }
    @Test void forestAndSnowEcologyExtendAndTurnIntoHighlandsInsteadOfRiverEverywhere() {
        var forest = new GensokyoBorderTerrain(map("minecraft:grass_block", "reverie_dreams:gensokyo_maple_forest"));
        assertEquals("reverie_dreams:gensokyo_maple_forest", forest.sample(-128, 170).biome());
        assertEquals("minecraft:frozen_peaks", forest.sample(-600, 170).biome());
        var snow = new GensokyoBorderTerrain(map("minecraft:snow_block", "minecraft:snowy_plains"));
        assertEquals("minecraft:snow_block", snow.sample(-128, 170).surface());
        assertEquals("minecraft:frozen_peaks", snow.sample(-600, 170).biome());
        var frozenWater = new GensokyoBorderTerrain(map("minecraft:water", "minecraft:frozen_river"));
        assertEquals("minecraft:frozen_river", frozenWater.sample(-128, 170).biome());
    }
    @Test void distantSceneryKeepsReliefWithoutAFlatHeightLimitOrInvisibleFullColumnWall() {
        var t = new GensokyoBorderTerrain(map("minecraft:grass_block", "minecraft:forest"));
        var heights = new HashSet<Integer>();
        for (int x = -1500; x < -500; x += 13) for (int z = -800; z < 800; z += 19) {
            var c = t.sample(x, z);
            assertTrue(c.groundY() > 1024 && c.groundY() < 1524);
            heights.add(c.groundY());
        }
        assertTrue(heights.size() > 100, "background mountains must have real relief");
    }
    @Test void collisionWallsStopWalkingFlyingAndWideVehicleBoxesOnEverySideAndAtCorners() {
        var bounds = new GensokyoBoundaryCollision.Bounds(-127, 75, 384, 256);
        for (double y : new double[]{-100, 1050, 2000}) {
            var west = new AABB(-126, y, 160, -125, y + 2, 161);
            assertEquals(-1, Shapes.collide(Direction.Axis.X, west, bounds.touching(west.expandTowards(-20, 0, 0)), -20), 1e-7);
            var east = new AABB(255, y, 160, 256, y + 2, 161);
            assertEquals(1, Shapes.collide(Direction.Axis.X, east, bounds.touching(east.expandTowards(20, 0, 0)), 20), 1e-7);
            var north = new AABB(0, y, 76, 2, y + 2, 78);
            assertEquals(-1, Shapes.collide(Direction.Axis.Z, north, bounds.touching(north.expandTowards(0, 0, -20)), -20), 1e-7);
            var south = new AABB(0, y, 328, 2, y + 2, 330);
            assertEquals(1, Shapes.collide(Direction.Axis.Z, south, bounds.touching(south.expandTowards(0, 0, 20)), 20), 1e-7);
        }
        assertEquals(2, bounds.touching(new AABB(-129, 500, 73, -126, 503, 76)).size());
        assertTrue(bounds.touching(new AABB(0, 0, 100, 1, 2, 101)).isEmpty());
    }
}
