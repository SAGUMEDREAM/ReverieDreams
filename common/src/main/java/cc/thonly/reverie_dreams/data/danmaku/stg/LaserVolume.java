package cc.thonly.reverie_dreams.data.danmaku.stg;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Finite prism with the same square cross section as the native laser renderer.
 */
public final class LaserVolume {
    private final OrientedBoxVolume volume;

    public LaserVolume(Vec3 origin, float pitch, float yaw, float length, float width) {
        Vec3 forward = DanmakuMath.direction(pitch, yaw);
        double yawRadians = Math.toRadians(yaw);
        Vec3 right = new Vec3(Math.cos(yawRadians), 0, Math.sin(yawRadians));
        Vec3 up = forward.cross(right);
        double radius = width * 0.5;
        double halfLength = length * 0.5;
        this.volume = new OrientedBoxVolume(origin.add(forward.scale(halfLength)),
                new Vec3(radius, radius, halfLength), right, up, forward);
    }

    public AABB bounds() {
        return this.volume.bounds();
    }

    public boolean intersects(AABB box) {
        return this.volume.intersects(box);
    }
}
