package cc.thonly.reverie_dreams.danmaku.trajectory;

import cc.thonly.reverie_dreams.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.danmaku.Pattern;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import lombok.AllArgsConstructor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.phys.Vec3;

@AllArgsConstructor
public class PatternTrajectory extends DanmakuTrajectory {
    public final Pattern pattern;

    @Override
    public void run(ServerLevel world, @Nullable LivingEntity livingEntity, ItemStack stack,
                    Double x, Double y, Double z,
                    float pitch, float yaw, float divergence, float offsetDist, IDanmakuItem pThis) {

        char[][] chars = this.pattern.getChars();
        int height = chars.length;
        int width = height > 0 ? chars[0].length : 0;
        if (width == 0 || height == 0) return;

        double centerX = (width - 1) / 2.0;
        double centerY = (height - 1) / 2.0;

        // 弧度
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);

        // 玩家前方向量
        Vec3 forward = new Vec3(
                -Math.sin(yawRad) * Math.cos(pitchRad),
                Math.sin(pitchRad),
                Math.cos(yawRad) * Math.cos(pitchRad)
        ).normalize();

        // 世界上方向
        Vec3 up = new Vec3(0, 1, 0);

        // 玩家右方向
        Vec3 right = forward.cross(up);
        if (right.length() < 0.001) {
            right = forward.cross(new Vec3(0, 0, 1));
        }
        right = right.normalize();

        // 前方偏移
        double forwardDist = Math.max(offsetDist, 2.0);

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (chars[row][col] != '#') continue;

                // 局部坐标 Y–Z 平面
                double localUp = centerY - row;    // 垂直
                double localRight = col - centerX; // 横向

                // 世界坐标
                Vec3 spawnPos = new Vec3(x, y, z)
                        .add(forward.scale(forwardDist))       // 前方偏移
                        .add(right.scale(localRight))          // 横向
                        .add(up.scale(localUp));               // 垂直

                DanmakuEntity e = spawnByItemStack(world, livingEntity,
                        spawnPos.x, spawnPos.y, spawnPos.z,
                        stack, pitch, yaw, divergence, offsetDist);

//                if (e != null)
//                    System.out.printf("(%.3f, %.3f, %.3f)%n",
//                            spawnPos.x - x, spawnPos.y - y, spawnPos.z - z);
            }
            System.out.println();
        }
    }
}
