package cc.thonly.reverie_dreams.data.danmaku.trajectory;

import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectoryConfig;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Horizontal ring around the caster. Outward bullets face away from the ring center.
 */
public class RingTrajectory extends DanmakuTrajectory {
    private final int bulletCount;
    private final float radius;
    private final boolean faceOutward;

    public RingTrajectory() {
        this(24, 1.0f, true);
    }

    public RingTrajectory(int bulletCount, float radius, boolean faceOutward) {
        this.bulletCount = Math.max(1, bulletCount);
        this.radius = Math.max(0.0f, radius);
        this.faceOutward = faceOutward;
    }

    @Override
    public void run(ServerLevel world, @Nullable LivingEntity livingEntity, ItemStack stack,
                    double x, double y, double z, float xRot, float yRot,
                    float divergence, float offsetDist, IDanmakuItem pThis, DanmakuTrajectoryConfig config) {
        int bullets = config.countOr(this.bulletCount);
        double ringRadius = config.radiusOr(this.radius);
        boolean outward = config.faceOutwardOr(this.faceOutward);
        for (int i = 0; i < bullets; i++) {
            float angleDeg = (360.0f / bullets) * i;
            float angleRad = (float) Math.toRadians(angleDeg);
            double offsetX = ringRadius * Math.cos(angleRad);
            double offsetZ = ringRadius * Math.sin(angleRad);

            // Minecraft yaw 0 faces +Z; the old angleDeg value was off by 90 degrees.
            float bulletYaw = outward
                    ? (float) Math.toDegrees(Math.atan2(offsetZ, offsetX)) - 90.0f
                    : yRot;

            spawnByItemStack(world, livingEntity,
                    x + offsetX, y, z + offsetZ,
                    stack, xRot, bulletYaw, divergence, offsetDist);
        }
    }

    @Override
    public DanmakuTrajectoryConfig defaultConfig() {
        return new DanmakuTrajectoryConfig(
                this.bulletCount,
                DanmakuTrajectoryConfig.UNSET_FLOAT,
                this.radius,
                DanmakuTrajectoryConfig.UNSET_INT,
                DanmakuTrajectoryConfig.UNSET_FLOAT,
                DanmakuTrajectoryConfig.UNSET_FLOAT,
                this.faceOutward ? DanmakuTrajectoryConfig.FACE_TRUE : DanmakuTrajectoryConfig.FACE_FALSE
        );
    }
}
