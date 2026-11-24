package cc.thonly.reverie_dreams.data.danmaku.stg.runtime;

import net.minecraft.world.phys.Vec3;

/**
 * Caster-view coordinates: +Z forward, +Y up at level pitch.
 */
final class SpellAimFrame {
    private final double sinPitch;
    private final double cosPitch;
    private final double sinYaw;
    private final double cosYaw;

    SpellAimFrame(float pitch, float yaw) {
        double pitchRadians = Math.toRadians(pitch);
        double yawRadians = Math.toRadians(yaw);
        this.sinPitch = Math.sin(pitchRadians);
        this.cosPitch = Math.cos(pitchRadians);
        this.sinYaw = Math.sin(yawRadians);
        this.cosYaw = Math.cos(yawRadians);
    }

    Vec3 toWorld(Vec3 local) {
        if (local.lengthSqr() < 1.0E-12D) {
            return local;
        }
        double y = local.y * this.cosPitch - local.z * this.sinPitch;
        double z = local.y * this.sinPitch + local.z * this.cosPitch;
        return new Vec3(local.x * this.cosYaw - z * this.sinYaw, y, local.x * this.sinYaw + z * this.cosYaw);
    }
}
