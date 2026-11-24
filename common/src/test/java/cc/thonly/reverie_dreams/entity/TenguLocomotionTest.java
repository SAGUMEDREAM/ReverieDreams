package cc.thonly.reverie_dreams.entity;

import org.junit.jupiter.api.Test;

import static cc.thonly.reverie_dreams.entity.TenguLocomotion.Flight.Phase.*;
import static org.junit.jupiter.api.Assertions.*;

class TenguLocomotionTest {
    @Test
    void missedPositionSampleHoldsTheStepAndStationaryNoiseDoesNotRestartIt() {
        var gait = new TenguLocomotion.Gait();
        gait.update(.18, 1, true);
        assertTrue(gait.active());
        assertTrue(gait.phase(1) > gait.phase(0));
        double lastStep = gait.phase(1);

        gait.update(0, 1, true);
        assertTrue(gait.active());
        assertEquals(lastStep, gait.phase(.5), 1e-9);
        gait.update(.002, 1, true);
        assertFalse(gait.active());
        gait.update(.004, 1, true);
        assertFalse(gait.active());
        gait.update(.03, 1, true);
        assertTrue(gait.active());

        gait.update(1.5, 1, true);
        assertFalse(gait.active(), "A position snap does not make a step");
        gait.update(.20, 1, false);
        assertFalse(gait.active(), "Airborne displacement is not a ground step");
    }

    @Test
    void crowLaunchesCoastsAndFoldsOnlyAfterConfirmedGroundContact() {
        var flight = new TenguLocomotion.Flight();
        ticks(flight, 2, true, 0, 0);
        ticks(flight, 1, false, .12, .055);
        assertEquals(GROUND, flight.phase());
        ticks(flight, 1, false, .12, .055);
        assertEquals(TAKEOFF, flight.phase());
        ticks(flight, 8, false, .12, .06);
        assertEquals(FLAP, flight.phase());
        ticks(flight, 12, false, .12, -.015);
        assertEquals(GLIDE, flight.phase());
        ticks(flight, 1, false, .025, 0);
        assertEquals(GLIDE, flight.phase());
        ticks(flight, 1, false, .025, 0);
        assertEquals(FLAP, flight.phase(), "A stationary hover must flap again");
        ticks(flight, 1, true, 0, -.025);
        assertEquals(FLAP, flight.phase());
        ticks(flight, 1, true, 0, 0);
        assertEquals(LAND, flight.phase());
        assertEquals(0, flight.elapsed());
        ticks(flight, 8, true, 0, 0);
        assertEquals(GROUND, flight.phase());
    }

    @Test
    void crowSkipsLaunchWhenFallingOrFirstSeenInAir() {
        var flight = new TenguLocomotion.Flight();
        ticks(flight, 2, true, 0, 0);
        ticks(flight, 4, false, .12, -.10);
        assertEquals(FLAP, flight.phase());
        var alreadyFlying = new TenguLocomotion.Flight();
        ticks(alreadyFlying, 4, false, .12, .08);
        assertEquals(FLAP, alreadyFlying.phase());
    }

    private static void ticks(TenguLocomotion.Flight flight, int count, boolean ground, double horizontal, double vertical) {
        for (int i = 0; i < count; i++) flight.update(ground, true, horizontal, vertical);
    }
}
