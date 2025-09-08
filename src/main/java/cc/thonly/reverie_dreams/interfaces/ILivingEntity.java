package cc.thonly.reverie_dreams.interfaces;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public interface ILivingEntity {
    void setMaxHealthModifier(float value);
    float getMaxHealthModifier();
    void setDeathCount(int value);
    int getDeathCount();
    void setManpozuchiUsingState(double value);
    void setKanju(ServerWorld world, BlockPos blockPos);
    double getManpozuchiUsingState();
}
