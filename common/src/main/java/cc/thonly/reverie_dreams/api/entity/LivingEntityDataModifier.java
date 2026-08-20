package cc.thonly.reverie_dreams.api.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public interface LivingEntityDataModifier {

    static LivingEntityDataModifier getMixin(LivingEntity entity) {
        return (LivingEntityDataModifier) entity;
    }

    void reverie_dreams$setMaxHealthModifier(float value);

    float reverie_dreams$getMaxHealthModifier();

    void reverie_dreams$setDeathLevel(int value);

    int reverie_dreams$getDeathLevel();

    void reverie_dreams$setManpozuchiUsingState(double value);

    void reverie_dreams$setKanju(ServerLevel world, BlockPos blockPos);

    double reverie_dreams$getManpozuchiUsingState();
}
