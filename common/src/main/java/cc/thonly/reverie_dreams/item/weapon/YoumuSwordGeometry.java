package cc.thonly.reverie_dreams.item.weapon;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class YoumuSwordGeometry {
    private YoumuSwordGeometry() {
    }

    public static boolean facing(Vec3 look, Vec3 toThreat) {
        Vec3 facing = new Vec3(look.x, 0, look.z);
        Vec3 threat = new Vec3(toThreat.x, 0, toThreat.z);
        return facing.lengthSqr() > 1e-8 && threat.lengthSqr() > 1e-8
                && facing.normalize().dot(threat.normalize()) >= .35;
    }

    public static boolean intersects(Vec3 start, Vec3 end, AABB target, double width) {
        AABB expanded = target.inflate(width);
        return expanded.contains(start) || expanded.contains(end) || expanded.clip(start, end).isPresent();
    }

    public static Vec3 horizontal(Vec3 look) {
        Vec3 horizontal = new Vec3(look.x, 0, look.z);
        return horizontal.lengthSqr() < 1e-8 ? new Vec3(0, 0, 1) : horizontal.normalize();
    }
}
