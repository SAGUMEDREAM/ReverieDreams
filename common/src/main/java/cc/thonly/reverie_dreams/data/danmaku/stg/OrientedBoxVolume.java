package cc.thonly.reverie_dreams.data.danmaku.stg;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * An orthogonal box, tested against entity AABBs with the separating-axis theorem.
 */
public final class OrientedBoxVolume {
    private final Vec3 center;
    private final Vec3 halfExtents;
    private final Vec3[] axes;
    private final AABB bounds;

    public OrientedBoxVolume(Vec3 center, Vec3 halfExtents, Vec3 right, Vec3 up, Vec3 forward) {
        this.center = center;
        this.halfExtents = halfExtents;
        this.axes = new Vec3[]{right, up, forward};
        Vec3 extent = new Vec3(projectionOn(new Vec3(1, 0, 0)),
                projectionOn(new Vec3(0, 1, 0)), projectionOn(new Vec3(0, 0, 1)));
        this.bounds = new AABB(this.center.subtract(extent), this.center.add(extent));
    }

    public AABB bounds() {
        return this.bounds;
    }

    public Vec3 center() {
        return this.center;
    }

    public boolean intersects(AABB box) {
        if (!this.bounds.intersects(box)) {
            return false;
        }
        Vec3 delta = box.getCenter().subtract(this.center);
        Vec3 targetExtents = new Vec3(box.getXsize() * 0.5, box.getYsize() * 0.5, box.getZsize() * 0.5);
        // Three world axes are covered by bounds. Check the box axes and all nine edge crosses.
        for (Vec3 axis : this.axes) {
            if (separates(axis, delta, targetExtents)
                    || separates(new Vec3(0, axis.z, -axis.y), delta, targetExtents)
                    || separates(new Vec3(-axis.z, 0, axis.x), delta, targetExtents)
                    || separates(new Vec3(axis.y, -axis.x, 0), delta, targetExtents)) {
                return false;
            }
        }
        return true;
    }

    private boolean separates(Vec3 axis, Vec3 delta, Vec3 targetExtents) {
        double targetRadius = targetExtents.x * Math.abs(axis.x)
                + targetExtents.y * Math.abs(axis.y) + targetExtents.z * Math.abs(axis.z);
        return Math.abs(delta.dot(axis)) > targetRadius + this.projectionOn(axis) + 1e-9;
    }

    private double projectionOn(Vec3 axis) {
        return this.halfExtents.x * Math.abs(axis.dot(this.axes[0]))
                + this.halfExtents.y * Math.abs(axis.dot(this.axes[1]))
                + this.halfExtents.z * Math.abs(axis.dot(this.axes[2]));
    }
}
