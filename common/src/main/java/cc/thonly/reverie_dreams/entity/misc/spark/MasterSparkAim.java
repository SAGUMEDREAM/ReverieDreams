package cc.thonly.reverie_dreams.entity.misc.spark;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Bounded server steering and smooth client interpolation along the shortest yaw arc. */
public final class MasterSparkAim {
    public static final float YAW_STEP = 8;
    public static final float PITCH_STEP = 6;
    private MasterSparkAim() { }

    public record Angles(float pitch, float yaw) { }

    public static Angles towards(float pitch, float yaw, Vec3 delta) {
        if (delta.lengthSqr() < 1e-10) return new Angles(pitch, yaw);
        double horizontal = Math.hypot(delta.x, delta.z);
        float desiredYaw = horizontal < 1e-8 ? yaw : (float) Math.toDegrees(Math.atan2(-delta.x, delta.z));
        float desiredPitch = (float) -Math.toDegrees(Math.atan2(delta.y, horizontal));
        return approach(pitch, yaw, desiredPitch, desiredYaw);
    }

    private static Angles approach(float pitch, float yaw, float desiredPitch, float desiredYaw) {
        return new Angles(Mth.approach(pitch, desiredPitch, PITCH_STEP),
                Mth.wrapDegrees(Mth.approachDegrees(yaw, desiredYaw, YAW_STEP)));
    }

    public static final class Interpolation {
        private boolean initialized;
        private float previousPitch, previousYaw, pitch, yaw;

        public void tick(float desiredPitch, float desiredYaw) {
            if (!initialized) {
                initialized = true;
                previousPitch = pitch = desiredPitch;
                previousYaw = yaw = desiredYaw;
                return;
            }
            previousPitch = pitch;
            previousYaw = yaw;
            Angles next = approach(pitch, yaw, desiredPitch, desiredYaw);
            pitch = next.pitch();
            yaw = next.yaw();
        }

        public boolean initialized() { return initialized; }
        public float pitch(float partialTick) { return Mth.lerp(Mth.clamp(partialTick, 0, 1), previousPitch, pitch); }
        public float yaw(float partialTick) { return Mth.wrapDegrees(Mth.rotLerp(Mth.clamp(partialTick, 0, 1), previousYaw, yaw)); }
    }
}
