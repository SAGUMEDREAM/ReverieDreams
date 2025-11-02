package cc.thonly.reverie_dreams.danmaku.trajectory;

import cc.thonly.reverie_dreams.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.danmaku.Pattern;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import lombok.AllArgsConstructor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
public class PatternTrajectory extends DanmakuTrajectory {
    public final Pattern pattern;

    @Override
    public void run(ServerLevel world, @Nullable LivingEntity livingEntity, ItemStack stack,
                    Double x, Double y, Double z,
                    float pitch, float yaw, float divergence, float offsetDist, IDanmakuItem pThis) {

        double centerX = x;
        double centerY = y;
        double centerZ = z;

        // 转角度为弧度
        double yawRad = Math.toRadians(-yaw);
        double pitchRad = Math.toRadians(-pitch);

        double cosYaw = Math.cos(yawRad);
        double sinYaw = Math.sin(yawRad);
        double cosPitch = Math.cos(pitchRad);
        double sinPitch = Math.sin(pitchRad);

        char[][] chars = this.pattern.getChars();
        int height = chars.length;
        int width = height > 0 ? chars[0].length : 0;

        // 图案中心点
        double centerOffsetX = (width - 1) / 2.0;
        double centerOffsetY = (height - 1) / 2.0;

        // 让图案“平铺”在玩家朝向的平面上
        // 即图案的法线方向 = 玩家朝向
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                char c = chars[row][col];
                if (c != '#') continue;

                // 图案局部坐标
                double localX = col - centerOffsetX;
                double localY = (height - 1 - row) - centerOffsetY;
                double localZ = 0; // 不固定深度，让平面自然朝前

                // --- 先 pitch（上下），再 yaw（左右） ---
                double ry = localY * cosPitch - localZ * sinPitch;
                double rz = localY * sinPitch + localZ * cosPitch;
                double rx = localX;

                double fx = rx * cosYaw - rz * sinYaw;
                double fz = rx * sinYaw + rz * cosYaw;
                double fy = ry;

                // 在玩家前方 2 格绘制
                double dist = 2.0;
                double forwardX = -sinYaw * cosPitch * dist;
                double forwardY = sinPitch * dist;
                double forwardZ = cosYaw * cosPitch * dist;

                double spawnX = centerX + fx + forwardX;
                double spawnY = centerY + fy + forwardY;
                double spawnZ = centerZ + fz + forwardZ;

                spawnByItemStack(world, livingEntity,
                        spawnX, spawnY, spawnZ,
                        stack, pitch, yaw, divergence, offsetDist);
            }
        }
    }
}
