package cc.thonly.reverie_dreams.data.danmaku.trajectory;

import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectoryConfig;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Hollow V-shaped bullet formation, aligned to the view plane.
 */
public class BulletTrajectory extends DanmakuTrajectory {
    private final int layers;
    private final float spacing;

    public BulletTrajectory() {
        this(2, 0.4f);
    }

    public BulletTrajectory(int layers, float spacing) {
        this.layers = Math.max(0, layers);
        this.spacing = Math.max(0.0f, spacing);
    }

    @Override
    public void run(ServerLevel world, @Nullable LivingEntity livingEntity, ItemStack stack,
                    double x, double y, double z, float xRot, float yRot,
                    float divergence, float offsetDist, IDanmakuItem pThis, DanmakuTrajectoryConfig config) {
        int layers = config.layersOr(this.layers);
        float spacing = config.spacingOr(this.spacing);
        Vec3 forward = direction(xRot, yRot);
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = forward.cross(up);
        if (right.lengthSqr() < 1.0E-6) {
            right = new Vec3(1, 0, 0);
        } else {
            right = right.normalize();
        }

        Vec3 origin = new Vec3(x, y, z);
        for (int layer = 0; layer <= layers; layer++) {
            for (int side = -layer; side <= layer; side++) {
                if (Math.abs(side) != layer) {
                    continue;
                }
                Vec3 pos = origin
                        .add(right.scale(side * spacing))
                        .add(forward.scale(-layer * spacing));
                spawnByItemStack(world, livingEntity, pos.x, pos.y, pos.z, stack,
                        xRot, yRot, divergence, offsetDist);
            }
        }
    }

    @Override
    public DanmakuTrajectoryConfig defaultConfig() {
        return new DanmakuTrajectoryConfig(
                DanmakuTrajectoryConfig.UNSET_INT,
                DanmakuTrajectoryConfig.UNSET_FLOAT,
                DanmakuTrajectoryConfig.UNSET_FLOAT,
                this.layers,
                this.spacing,
                DanmakuTrajectoryConfig.UNSET_FLOAT,
                DanmakuTrajectoryConfig.FACE_DEFAULT
        );
    }
}
