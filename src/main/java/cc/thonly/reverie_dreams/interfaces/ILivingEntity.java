package cc.thonly.reverie_dreams.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public interface ILivingEntity {
    void setMaxHealthModifier(float value);
    float getMaxHealthModifier();
    void setDeathCount(int value);
    int getDeathCount();
    void setManpozuchiUsingState(double value);
    void setKanju(ServerLevel world, BlockPos blockPos);
    double getManpozuchiUsingState();
}
