package cc.thonly.reverie_dreams.data.danmaku.trajectory;

import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectoryConfig;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * One-shot horizontal fan around the player's view yaw.
 */
public class FanTrajectory extends DanmakuTrajectory {
    private final int count;
    private final float spreadDegrees;

    public FanTrajectory(int count, float spreadDegrees) {
        this.count = Math.max(1, count);
        this.spreadDegrees = Math.max(0.0f, spreadDegrees);
    }

    @Override
    public void run(ServerLevel world, @Nullable LivingEntity livingEntity, ItemStack stack,
                    double x, double y, double z, float xRot, float yRot,
                    float divergence, float offsetDist, IDanmakuItem pThis, DanmakuTrajectoryConfig config) {
        int shots = config.countOr(this.count);
        float spread = config.spreadOr(this.spreadDegrees);
        float step = shots == 1 ? 0.0f : spread / (shots - 1);
        float start = -spread * 0.5f;
        for (int i = 0; i < shots; i++) {
            spawnByItemStack(world, livingEntity, x, y, z, stack,
                    xRot, yRot + start + step * i, divergence, offsetDist);
        }
    }

    @Override
    public DanmakuTrajectoryConfig defaultConfig() {
        return new DanmakuTrajectoryConfig(this.count, this.spreadDegrees,
                DanmakuTrajectoryConfig.UNSET_FLOAT, DanmakuTrajectoryConfig.UNSET_INT,
                DanmakuTrajectoryConfig.UNSET_FLOAT, DanmakuTrajectoryConfig.UNSET_FLOAT,
                DanmakuTrajectoryConfig.FACE_DEFAULT);
    }
}
