package cc.thonly.reverie_dreams.entity.misc.spark;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class MasterSparkBeamTest {
    private static final Vec3 FORWARD = new Vec3(0, 0, 1);

    @Test void hitsBodiesInsideTheConeIncludingLargeTargetsWhoseCentersAreOutside() {
        assertTrue(hit(new AABB(-0.3, -0.9, 10, 0.3, 0.9, 10.6)));
        assertTrue(hit(new AABB(1.2, -1, 25, 5.2, 1, 27)));
        assertFalse(hit(new AABB(4, -0.2, 20, 4.4, 0.2, 21)));
    }

    @Test void respectsNearEndFarEndAndAClippedWallDistance() {
        assertFalse(hit(new AABB(-0.2, -0.2, -2, 0.2, 0.2, -0.1)));
        assertFalse(hit(new AABB(-0.2, -0.2, 40.01, 0.2, 0.2, 41)));
        assertFalse(MasterSparkBeam.intersects(Vec3.ZERO, FORWARD, 8, new AABB(-0.3, -0.9, 12, 0.3, 0.9, 13), 1));
        assertTrue(MasterSparkBeam.intersects(Vec3.ZERO, FORWARD, 8, new AABB(-0.3, -0.9, 7, 0.3, 0.9, 8), 1));
    }

    @Test void visualExpansionAlsoControlsTheHitWidth() {
        AABB edge = new AABB(1.5, -0.1, 25, 1.7, 0.1, 26);
        assertTrue(MasterSparkBeam.intersects(Vec3.ZERO, FORWARD, 40, edge, 1));
        assertFalse(MasterSparkBeam.intersects(Vec3.ZERO, FORWARD, 40, edge, 0.25));
        assertFalse(MasterSparkBeam.intersects(Vec3.ZERO, FORWARD, 40, edge, 0));
        assertFalse(MasterSparkBeam.intersects(Vec3.ZERO, FORWARD, 0, edge, 1));
        assertEquals(0.45, MasterSparkBeam.radiusAt(0));
        assertEquals(2.8, MasterSparkBeam.radiusAt(40), 1e-9);
    }

    @Test void handlesUpwardAndObliqueBeamsAtNegativeWorldPositions() {
        Vec3 origin = new Vec3(-123, -50, 987);
        Vec3 direction = new Vec3(1, 1, -1).normalize();
        Vec3 center = origin.add(direction.scale(24));
        AABB target = AABB.ofSize(center, 0.6, 1.8, 0.6);
        assertTrue(MasterSparkBeam.intersects(origin, direction, 40, target, 1));
        assertFalse(MasterSparkBeam.intersects(origin, direction, 40, target.move(10, 0, 10), 1));
        assertTrue(MasterSparkBeam.bounds(origin, direction, 40).intersects(target));
    }

    @Test void analyticIntersectionNeverMissesIndependentlySampledBeamContacts() {
        Random random = new Random(388);
        Vec3 direction = new Vec3(2, 1, -3).normalize();
        int sampledContacts = 0;
        for (int sample = 0; sample < 500; sample++) {
            Vec3 center = direction.scale(random.nextDouble() * 24 + 8)
                    .add(random.nextDouble() * 8 - 4, random.nextDouble() * 8 - 4, random.nextDouble() * 8 - 4);
            AABB box = AABB.ofSize(center, 0.3 + random.nextDouble() * 4, 0.3 + random.nextDouble() * 4, 0.3 + random.nextDouble() * 4);
            boolean sampled = false;
            for (double t = 0; t <= 40; t += 0.1) {
                Vec3 point = direction.scale(t);
                double x = Math.clamp(point.x, box.minX, box.maxX), y = Math.clamp(point.y, box.minY, box.maxY), z = Math.clamp(point.z, box.minZ, box.maxZ);
                double radius = MasterSparkBeam.radiusAt(t);
                if (point.distanceToSqr(new Vec3(x, y, z)) < radius * radius) { sampled = true; break; }
            }
            if (sampled) {
                sampledContacts++;
                assertTrue(MasterSparkBeam.intersects(Vec3.ZERO, direction, 40, box, 1), "Missed sampled contact: " + box);
            }
        }
        assertTrue(sampledContacts > 50);
    }

    @Test void firesImmediatelyAndContinuouslyForTheExtendedThirtySecondLifetime() {
        MasterSparkCycle cycle = new MasterSparkCycle();
        List<Integer> pulses = new ArrayList<>();
        assertEquals(600, MasterSparkCycle.LIFETIME_TICKS);
        for (int tick = 0; tick < MasterSparkCycle.LIFETIME_TICKS; tick++) {
            cycle.tick(true); // Only owner availability matters; no target is required.
            assertEquals(MasterSparkCycle.Phase.FIRE, cycle.phase(), "Beam stopped at tick " + tick);
            assertEquals(tick, cycle.elapsed());
            assertEquals(1, cycle.charge());
            if (cycle.damagePulse()) pulses.add(cycle.elapsed());
        }
        assertEquals(60, pulses.size());
        assertEquals(0, pulses.getFirst());
        assertEquals(590, pulses.getLast());
        for (int i = 1; i < pulses.size(); i++) assertEquals(10, pulses.get(i) - pulses.get(i - 1));
    }

    @Test void losingTheOwnerStopsTheBeamAndReacquiringTheOwnerStartsImmediately() {
        MasterSparkCycle cycle = new MasterSparkCycle();
        for (int i = 0; i < 500; i++) cycle.tick(false);
        assertEquals(MasterSparkCycle.Phase.REST, cycle.phase());
        cycle.tick(true);
        assertEquals(MasterSparkCycle.Phase.FIRE, cycle.phase());
        cycle.tick(false);
        assertEquals(MasterSparkCycle.Phase.REST, cycle.phase());
        assertEquals(0, cycle.charge());
        assertFalse(cycle.damagePulse());
        cycle.tick(true);
        assertEquals(MasterSparkCycle.Phase.FIRE, cycle.phase());
        assertEquals(0, cycle.elapsed());
    }

    @Test void resetUsedOnLoadRequiresOwnerVerificationBeforeStartingAFreshBeam() {
        MasterSparkCycle cycle = new MasterSparkCycle();
        for (int i = 0; i < 40; i++) cycle.tick(true);
        assertEquals(MasterSparkCycle.Phase.FIRE, cycle.phase());
        cycle.reset();
        assertEquals(MasterSparkCycle.Phase.REST, cycle.phase());
        assertFalse(cycle.damagePulse());
        assertEquals(0, cycle.charge());
        cycle.tick(false);
        assertEquals(MasterSparkCycle.Phase.REST, cycle.phase());
        cycle.tick(true);
        assertEquals(MasterSparkCycle.Phase.FIRE, cycle.phase());
        assertEquals(0, cycle.elapsed());
    }

    private static boolean hit(AABB box) { return MasterSparkBeam.intersects(Vec3.ZERO, FORWARD, 40, box, 1); }
}
