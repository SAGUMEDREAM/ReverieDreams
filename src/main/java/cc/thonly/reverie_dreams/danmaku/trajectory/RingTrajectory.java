package cc.thonly.reverie_dreams.danmaku.trajectory;

import cc.thonly.reverie_dreams.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class RingTrajectory extends DanmakuTrajectory {
    private static final int BULLET_COUNT = 24;     // 子弹数量
    private static final float RADIUS = 1f;        // 半径（格）
    private static final float ANGLE_OFFSET = 0f;    // 起始角度偏移
    private static final boolean FACE_OUTWARD = true; // 是否朝外发射

    @Override
    public void run(ServerLevel world, @Nullable LivingEntity livingEntity, ItemStack stack,
                    Double x, Double y, Double z,
                    float pitch, float yaw,
                    float divergence, float offsetDist,
                    IDanmakuItem pThis) {
        double centerX = x;
        double centerY = y;
        double centerZ = z;

        for (int i = 0; i < BULLET_COUNT; i++) {
            float angleDeg = ((360f / BULLET_COUNT) * i) + ANGLE_OFFSET;
            float angleRad = (float) Math.toRadians(angleDeg);

            // 计算圆环上的点（以 yaw 平面展开）
            double offsetX = RADIUS * Math.cos(angleRad);
            double offsetZ = RADIUS * Math.sin(angleRad);

            // 计算子弹发射方向：朝外 or 固定方向
            float bulletYaw = FACE_OUTWARD ? angleDeg : yaw;

            spawnByItemStack(
                    world,
                    livingEntity,
                    centerX + offsetX,
                    centerY,
                    centerZ + offsetZ,
                    stack,
                    pitch,
                    bulletYaw,
                    divergence,
                    offsetDist
            );
        }
    }


}
