package cc.thonly.reverie_dreams.api.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public interface LivingEntityDataModifier {
    void reverie_dreams$setMaxHealthModifier(float value);

    float reverie_dreams$getMaxHealthModifier();

    void reverie_dreams$setDeathLevel(int value);

    int reverie_dreams$getDeathLevel();

    void reverie_dreams$setManpozuchiUsingState(double value);

    void reverie_dreams$setKanju(ServerLevel world, BlockPos blockPos);

    double reverie_dreams$getManpozuchiUsingState();
}
