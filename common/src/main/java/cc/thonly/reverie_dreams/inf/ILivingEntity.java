package cc.thonly.reverie_dreams.inf;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public interface ILivingEntity {
    void reverie_dreams$setMaxHealthModifier(float value);
    float reverie_dreams$getMaxHealthModifier();
    void reverie_dreams$setDeathLevel(int value);
    int reverie_dreams$getDeathLevel();
    void reverie_dreams$setManpozuchiUsingState(double value);
    void reverie_dreams$setKanju(ServerLevel world, BlockPos blockPos);
    double reverie_dreams$getManpozuchiUsingState();
}
