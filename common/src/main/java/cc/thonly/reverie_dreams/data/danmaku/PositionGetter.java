package cc.thonly.reverie_dreams.data.danmaku;

import net.minecraft.world.phys.Vec3;

@FunctionalInterface
public interface PositionGetter {
    Vec3 getPosition(int tick);
}
