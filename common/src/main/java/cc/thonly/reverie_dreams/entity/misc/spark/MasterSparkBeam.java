package cc.thonly.reverie_dreams.entity.misc.spark;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.Arrays;

/** Shared world-space geometry for visuals and server damage; dimensions are in blocks. */
public final class MasterSparkBeam {
    public static final double RANGE = 40;
    public static final double MOUNT_HEIGHT = 0.8;
    public static final double MUZZLE_OFFSET = 0.35;
    public static final double NEAR_RADIUS = 0.45;
    public static final double FAR_RADIUS = 2.8;
    public static final float DAMAGE = 8;
    private MasterSparkBeam() { }

    public static double radiusAt(double distance) {
        return NEAR_RADIUS + (FAR_RADIUS - NEAR_RADIUS) * Math.clamp(distance / RANGE, 0, 1);
    }

    public static AABB bounds(Vec3 start, Vec3 direction, double length) {
        return new AABB(start, start.add(direction.scale(length))).inflate(radiusAt(length));
    }

    /** Minimize box distance minus expanding radius over the axis's slab intervals.
     * Uses the entire target box, so a large mob's limbs do not need its center in the beam. */
    public static boolean intersects(Vec3 start, Vec3 direction, double length, AABB box, double strength) {
        if (!(length > 0) || !(strength > 0) || direction.lengthSqr() < 0.99) return false;
        Vec3 center = box.getCenter().subtract(start);
        double halfProjection = Math.abs(direction.x) * box.getXsize() * 0.5
                + Math.abs(direction.y) * box.getYsize() * 0.5 + Math.abs(direction.z) * box.getZsize() * 0.5;
        double projection = center.dot(direction);
        if (projection + halfProjection < 0 || projection - halfProjection > length) return false;
        double[] points = new double[8];
        points[0] = 0;
        points[1] = length;
        int size = 2;
        size = addSlabs(points, size, start.x, direction.x, box.minX, box.maxX, length);
        size = addSlabs(points, size, start.y, direction.y, box.minY, box.maxY, length);
        size = addSlabs(points, size, start.z, direction.z, box.minZ, box.maxZ, length);
        Arrays.sort(points, 0, size);
        double slope = (FAR_RADIUS - NEAR_RADIUS) / RANGE * strength;
        double radius = NEAR_RADIUS * strength;
        for (int i = 1; i < size; i++) {
            double low = points[i - 1], high = points[i], middle = (low + high) * 0.5;
            double a = -slope * slope, b = -2 * radius * slope, c = -radius * radius;
            for (int axis = 0; axis < 3; axis++) {
                double origin = axis == 0 ? start.x : axis == 1 ? start.y : start.z;
                double velocity = axis == 0 ? direction.x : axis == 1 ? direction.y : direction.z;
                double min = axis == 0 ? box.minX : axis == 1 ? box.minY : box.minZ;
                double max = axis == 0 ? box.maxX : axis == 1 ? box.maxY : box.maxZ;
                double position = origin + velocity * middle;
                if (position >= min && position <= max) continue;
                double delta = origin - (position < min ? min : max);
                a += velocity * velocity;
                b += 2 * velocity * delta;
                c += delta * delta;
            }
            if (evaluate(a, b, c, low) <= 1e-8 || evaluate(a, b, c, high) <= 1e-8) return true;
            if (a > 0) {
                double minimum = -b / (2 * a);
                if (minimum > low && minimum < high && evaluate(a, b, c, minimum) <= 1e-8) return true;
            }
        }
        return false;
    }

    private static double evaluate(double a, double b, double c, double t) { return (a * t + b) * t + c; }
    private static int addSlabs(double[] points, int size, double origin, double direction, double min, double max, double length) {
        if (Math.abs(direction) > 1e-10) {
            double first = (min - origin) / direction, second = (max - origin) / direction;
            if (first > 0 && first < length) points[size++] = first;
            if (second > 0 && second < length) points[size++] = second;
        }
        return size;
    }
}
