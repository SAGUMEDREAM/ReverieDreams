package cc.thonly.reverie_dreams.item.weapon;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class YoumuSwordGeometryTest {
    @Test void fastSlashHitsBetweenEndpointsButCannotReachBehindOrFarBeyond() {
        Vec3 from = new Vec3(0, 1, 0), to = new Vec3(0, 1, 6);
        assertTrue(YoumuSwordGeometry.intersects(from, to, new AABB(-.3,0,2.7,.3,2,3.3), .4));
        assertFalse(YoumuSwordGeometry.intersects(from, to, new AABB(-.3,0,-2,.3,2,-1), .4));
        assertFalse(YoumuSwordGeometry.intersects(from, to, new AABB(-.3,0,7,.3,2,8), .4));
    }

    @Test void slashUsesEntityBoundsAndWidthWithoutHittingSeparatedTargets() {
        Vec3 from = new Vec3(0, 1, 0), to = new Vec3(0, 1, 6);
        assertTrue(YoumuSwordGeometry.intersects(from, to, new AABB(.5,0,2,1.1,2,3), .6));
        assertFalse(YoumuSwordGeometry.intersects(from, to, new AABB(1.1,0,2,1.7,2,3), .6));
        assertFalse(YoumuSwordGeometry.intersects(from, to, new AABB(-.3,3,2,.3,4,3), .6));
    }

    @Test void parryAcceptsFrontAndRejectsRearFlankAndUnknownDirection() {
        Vec3 look = new Vec3(0,0,1);
        assertTrue(YoumuSwordGeometry.facing(look, new Vec3(0,0,2)));
        assertTrue(YoumuSwordGeometry.facing(look, new Vec3(1,1,2)));
        assertFalse(YoumuSwordGeometry.facing(look, new Vec3(0,0,-2)));
        assertFalse(YoumuSwordGeometry.facing(look, new Vec3(2,0,0)));
        assertFalse(YoumuSwordGeometry.facing(look, Vec3.ZERO));
        assertFalse(YoumuSwordGeometry.facing(look, new Vec3(0,2,0)));
        assertFalse(YoumuSwordGeometry.facing(look, new Vec3(Double.NaN,0,1)));
    }

    @Test void verticalAimStillProducesFiniteHorizontalMovement() {
        Vec3 result = YoumuSwordGeometry.horizontal(new Vec3(0,1,0));
        assertEquals(0, result.y);
        assertEquals(1, result.length(), 1e-8);
        assertEquals(1, YoumuSwordGeometry.horizontal(new Vec3(2,3,4)).length(), 1e-8);
    }
}
