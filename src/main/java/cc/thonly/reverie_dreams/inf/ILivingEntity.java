package cc.thonly.reverie_dreams.inf;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public interface ILivingEntity {
    void setMaxHealthModifier(float value);
    float getMaxHealthModifier();
    void setDeathLevel(int value);
    int getDeathLevel();
    void setManpozuchiUsingState(double value);
    void setKanju(ServerLevel world, BlockPos blockPos);
    double getManpozuchiUsingState();
}
