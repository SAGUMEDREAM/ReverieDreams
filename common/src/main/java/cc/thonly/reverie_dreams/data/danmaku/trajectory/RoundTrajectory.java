package cc.thonly.reverie_dreams.data.danmaku.trajectory;

import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectoryConfig;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * View-aligned radial burst. Each bullet flies outward from the ring.
 */
public class RoundTrajectory extends DanmakuTrajectory {
    private final int bulletCount;
    private final float radius;

    public RoundTrajectory() {
        this(12, 1.0f);
    }

    public RoundTrajectory(int bulletCount, float radius) {
        this.bulletCount = Math.max(1, bulletCount);
        this.radius = Math.max(0.0f, radius);
    }

    @Override
    public void run(ServerLevel world, @Nullable LivingEntity livingEntity, ItemStack stack,
                    double x, double y, double z, float xRot, float yRot,
                    float divergence, float offsetDist, IDanmakuItem pThis, DanmakuTrajectoryConfig config) {
        int bullets = config.countOr(this.bulletCount);
        float ringRadius = config.radiusOr(this.radius);
        for (int i = 0; i < bullets; i++) {
            float angleDeg = (360.0f / bullets) * i;
            float angleRad = (float) Math.toRadians(angleDeg);

            Vector3f offset = new Vector3f((float) Math.cos(angleRad), 0, (float) Math.sin(angleRad));
            Quaternionf rotation = new Quaternionf()
                    .rotateY((float) Math.toRadians(-yRot))
                    .rotateX((float) Math.toRadians(-xRot));
            offset.rotate(rotation);

            Vec3 dir = new Vec3(offset.x, offset.y, offset.z);
            if (dir.lengthSqr() < 1.0E-6) {
                continue;
            }
            dir = dir.normalize();

            // Minecraft yaw: (-sin(yaw), cos(yaw)) in the X/Z plane.
            float bulletYaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
            float bulletPitch = (float) Math.toDegrees(-Math.asin(Mth.clamp(dir.y, -1.0, 1.0)));

            spawnByItemStack(world, livingEntity,
                    x + dir.x * ringRadius,
                    y + dir.y * ringRadius,
                    z + dir.z * ringRadius,
                    stack, bulletPitch, bulletYaw, divergence, offsetDist);
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
                DanmakuTrajectoryConfig.FACE_DEFAULT
        );
    }
}
