package cc.thonly.reverie_dreams.entity.misc.spark;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MasterSparkAimTest {
    @Test void acquiringARightAngleTargetTakesSeveralBoundedStepsInsteadOfSnapping() {
        Vec3 target = new Vec3(10, 0, 0);
        var aim = MasterSparkAim.towards(0, 0, target);
        assertEquals(-8, aim.yaw());
        assertEquals(0, aim.pitch(), 1e-4);
        for (int i = 0; i < 20; i++) {
            var next = MasterSparkAim.towards(aim.pitch(), aim.yaw(), target);
            assertTrue(Math.abs(Mth.wrapDegrees(next.yaw() - aim.yaw())) <= MasterSparkAim.YAW_STEP + 1e-4);
            assertTrue(Math.abs(Mth.wrapDegrees(next.yaw() + 90)) <= Math.abs(Mth.wrapDegrees(aim.yaw() + 90)) + 1e-4);
            aim = next;
        }
        assertEquals(-90, aim.yaw(), 1e-4);
    }

    @Test void crossesTheYawBoundaryAlongTheShortArc() {
        Vec3 target = Vec3.directionFromRotation(0, -170);
        var first = MasterSparkAim.towards(0, 170, target);
        assertEquals(178, first.yaw(), 1e-4);
        var second = MasterSparkAim.towards(first.pitch(), first.yaw(), target);
        assertEquals(-174, second.yaw(), 1e-4);
    }

    @Test void verticalAndCoincidentTargetsDoNotCauseYawJumpsOrInvalidAngles() {
        var vertical = MasterSparkAim.towards(0, 75, new Vec3(0, 10, 0));
        assertEquals(-6, vertical.pitch());
        assertEquals(75, vertical.yaw());
        var coincident = MasterSparkAim.towards(20, 75, Vec3.ZERO);
        assertEquals(20, coincident.pitch());
        assertEquals(75, coincident.yaw());
    }

    @Test void renderingInterpolatesWithinEachTickEvenWhileFiring() {
        var aim = new MasterSparkAim.Interpolation();
        aim.tick(0, 0);
        aim.tick(-60, 90);
        assertEquals(0, aim.pitch(0));
        assertEquals(-3, aim.pitch(0.5f));
        assertEquals(-6, aim.pitch(1));
        assertEquals(0, aim.yaw(0));
        assertEquals(4, aim.yaw(0.5f));
        assertEquals(8, aim.yaw(1));
    }

    @Test void initialPlacementIsNotAnimatedFromZeroAndClientInterpolationWrapsCorrectly() {
        var aim = new MasterSparkAim.Interpolation();
        assertFalse(aim.initialized());
        aim.tick(20, 179);
        assertEquals(20, aim.pitch(0.5f));
        assertEquals(179, aim.yaw(0.5f));
        aim.tick(20, -179);
        assertEquals(1, Math.abs(Mth.wrapDegrees(aim.yaw(0.5f) - 179)), 1e-4);
        assertEquals(-179, aim.yaw(1), 1e-4);
    }
}
