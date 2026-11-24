package cc.thonly.reverie_dreams.entity.ai.goal.attack;

import cc.thonly.reverie_dreams.item.weapon.LunarWeaponType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class RabbitUnitTacticsTest {
    private static boolean intersects(Vec3 velocity, double deviation, double ticks, AABB box) {
        return RabbitUnitTactics.intersectsSpread(Vec3.ZERO, velocity, deviation, ticks, box);
    }

    @Test void spreadWidensWithDistanceAndCoversShotgunPelletsOutsideTheCenterLine() {
        double deviation = LunarWeaponType.SHOTGUN.speed() * .0172275 * LunarWeaponType.SHOTGUN.spread(false);
        AABB lateralFriend = new AABB(1.5, -.2, 17, 1.9, .2, 19);
        assertTrue(intersects(new Vec3(0, 0, 3), deviation, 7, lateralFriend));
        assertFalse(intersects(new Vec3(0, 0, 3), 0, 7, lateralFriend));
        assertFalse(intersects(new Vec3(0, 0, 3), deviation, 7, new AABB(1.5, -.2, 1, 1.9, .2, 2)));
    }

    @Test void corridorIncludesMissesBeyondTargetButEndsAtTheProjectileLifetime() {
        Vec3 velocity = new Vec3(0, 0, 3);
        assertTrue(intersects(velocity, .04, 30, new AABB(-.3, -.3, 35, .3, .3, 36)));
        assertFalse(intersects(velocity, .04, 30, new AABB(-.3, -.3, 94, .3, .3, 95)));
        assertFalse(intersects(velocity, .04, 30, new AABB(-.3, -.3, -3, .3, .3, -2)));
    }

    @Test void axesMustIntersectAtTheSameTimeRatherThanAnywhereInTheBroadBox() {
        assertFalse(intersects(new Vec3(1, 0, 1), .02, 20, new AABB(1, -.1, 12, 2, .1, 13)));
        assertTrue(intersects(new Vec3(1, 0, 1), .02, 20, new AABB(11, -.1, 12, 13, .1, 13)));
    }

    @Test void inheritedShooterVelocityMovesTheSafetyCorridor() {
        AABB friend = new AABB(1.8, -.2, 28, 2.2, .2, 32);
        assertFalse(intersects(new Vec3(0, 0, 3), .01, 20, friend));
        assertTrue(intersects(new Vec3(.2, 0, 3), .01, 20, friend));
    }

    @Test void handlesNegativeDirectionsStationaryAxesAndMuzzleOverlap() {
        assertTrue(intersects(new Vec3(-3, 0, 0), .1, 10, new AABB(-16, -.2, -.2, -14, .2, .2)));
        assertFalse(intersects(new Vec3(-3, 0, 0), 0, 10, new AABB(-16, 1, -.2, -14, 2, .2)));
        assertTrue(intersects(new Vec3(0, 0, 3), .1, 10, new AABB(-1, -1, -1, 1, 1, 1)));
    }

    @Test void everySampledRealTypeTrajectoryFallsInsideItsFiniteEnvelope() {
        Random random = new Random(0x524142424954L);
        for (LunarWeaponType type : LunarWeaponType.values()) {
            Vec3 nominal = new Vec3(.3, -.1, type.speed());
            double deviation = type.speed() * .0172275 * type.spread(false);
            for (int sample = 0; sample < 200; sample++) {
                Vec3 velocity = nominal.add((random.nextDouble() - random.nextDouble()) * deviation,
                        (random.nextDouble() - random.nextDouble()) * deviation,
                        (random.nextDouble() - random.nextDouble()) * deviation);
                // The real motion engine preserves initial velocity and applies .99 air inertia.
                double tick = .99 * type.lifetime() * random.nextDouble();
                Vec3 point = velocity.scale(tick);
                AABB ally = new AABB(point.subtract(.01, .01, .01), point.add(.01, .01, .01));
                assertTrue(intersects(nominal, deviation, type.lifetime(), ally), type + " sample=" + sample);
            }
        }
    }
}
