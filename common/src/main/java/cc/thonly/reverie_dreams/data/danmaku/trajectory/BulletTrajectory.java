package cc.thonly.reverie_dreams.data.danmaku.trajectory;

import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BulletTrajectory extends DanmakuTrajectory {
    @Override
    public void run(ServerLevel world, @Nullable LivingEntity livingEntity, ItemStack stack,
                    double x, double y, double z, float xRot, float yRot,
                    float divergence, float offsetDist,
                    IDanmakuItem pThis) {

        int maxLayer = 2; // 层级从 -2 到 0
        float layerSpacing = 0.4f; // 层与层之间的距离（Z轴偏移）
        float horizontalSpacing = 0.4f; // 子弹左右间距

        for (int i = -maxLayer; i <= 0; i++) {
            int layer = -i; // 用于控制左右子弹个数（从外往中心）

            for (int j = -layer; j <= layer; j++) {
                // 只在边缘发射，形成镂空结构（跳过中间）
                if (Math.abs(j) != layer) continue;

                // 计算偏移（基于朝向 yRot）
                float xOffset = j * horizontalSpacing;
                float zOffset = i * layerSpacing;

                double rad = Math.toRadians(yRot);
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
                        xRot,
                        yRot,
                        divergence,
                        offsetDist
                );
            }
        }
    }


}
