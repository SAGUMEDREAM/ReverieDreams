package cc.thonly.reverie_dreams.danmaku.trajectory;

import cc.thonly.reverie_dreams.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

public class BulletTrajectory extends DanmakuTrajectory {
    @Override
    public void run(ServerWorld world, @Nullable LivingEntity livingEntity, ItemStack stack,
                    Double x, Double y, Double z, float pitch, float yaw,
                    float speed, float acceleration, float divergence, float offsetDist,
                    IDanmakuItem pThis) {

        int maxLayer = 2; // 层级从 -2 到 0
        float layerSpacing = 0.4f; // 层与层之间的距离（Z轴偏移）
        float horizontalSpacing = 0.4f; // 子弹左右间距

        for (int i = -maxLayer; i <= 0; i++) {
            int layer = -i; // 用于控制左右子弹个数（从外往中心）

            for (int j = -layer; j <= layer; j++) {
                // 只在边缘发射，形成镂空结构（跳过中间）
                if (Math.abs(j) != layer) continue;

                // 计算偏移（基于朝向 yaw）
                float xOffset = j * horizontalSpacing;
                float zOffset = i * layerSpacing;

                double rad = Math.toRadians(yaw);
                double dx = -Math.sin(rad) * zOffset + Math.cos(rad) * xOffset;
                double dz = Math.cos(rad) * zOffset + Math.sin(rad) * xOffset;

                // 发射弹幕
                DanmakuTrajectory.spawnByItemStack(
                        world,
                        livingEntity,
                        x + dx,
                        y,
                        z + dz,
                        stack,
                        pitch,
                        yaw,
                        speed,
                        acceleration,
                        divergence,
                        offsetDist
                );
            }
        }
    }


}
