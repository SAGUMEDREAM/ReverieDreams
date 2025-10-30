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
                    Double x, Double y, Double z, float pitch, float yaw,
                    float speed, float acceleration, float divergence, float offsetDist,
                    IDanmakuItem pThis) {

        double centerX = x;
        double centerY = y;
        double centerZ = z;

        double yawRad = Math.toRadians(-yaw);     // 左手坐标系，yaw绕Y轴
        double pitchRad = Math.toRadians(-pitch); // pitch向下为正，绕X轴

        double cosYaw = Math.cos(yawRad);
        double sinYaw = Math.sin(yawRad);
        double cosPitch = Math.cos(pitchRad);
        double sinPitch = Math.sin(pitchRad);

        String[] array = this.pattern.getPatternString().toArray(new String[0]);

        for (int dy = 0; dy < array.length; dy++) {
            String row = array[dy];
            int yOffset = dy;

            for (int dx = 0; dx < row.length(); dx++) {
                char c = row.charAt(dx);
                if (c == '#') {
                    double localX = dx - (row.length() / 2.0); // 横向
                    double localY = yOffset;                   // 竖直方向
                    double localZ = 0;                         // 原始为正前方

                    // --- 三维旋转：先 pitch 后 yaw ---
                    // pitch（绕 X 轴，俯仰）
                    double ry = localY * cosPitch - localZ * sinPitch;
                    double rz = localY * sinPitch + localZ * cosPitch;
                    double rx = localX;

                    // yaw（绕 Y 轴，左右）
                    double fx = rx * cosYaw - rz * sinYaw;
                    double fz = rx * sinYaw + rz * cosYaw;
                    double fy = ry;

                    spawnByItemStack(world, livingEntity,
                            centerX + fx,
                            centerY + fy,
                            centerZ + fz,
                            stack,
                            pitch, yaw,
                            speed, acceleration,
                            divergence, offsetDist);
                }
            }
        }
    }


}
