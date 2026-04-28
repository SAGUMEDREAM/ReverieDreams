package cc.thonly.reverie_dreams.data.danmaku.trajectory;

import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class TripleTrajectory extends DanmakuTrajectory {
    @Override
    public void run(ServerLevel world, @Nullable LivingEntity livingEntity, ItemStack stack, double x, double y, double z, float xRot, float yRot, float divergence, float offsetDist, IDanmakuItem pThis) {
        DanmakuTrajectory.spawnByItemStack(world, livingEntity, x, y, z, stack, xRot, yRot - 15.0f, 0f, 0f);
        DanmakuTrajectory.spawnByItemStack(world, livingEntity, x, y, z, stack, xRot, yRot, 0f, 0f);
        DanmakuTrajectory.spawnByItemStack(world, livingEntity, x, y, z, stack, xRot, yRot + 15.0f, 0f, 0f);
    }
}
