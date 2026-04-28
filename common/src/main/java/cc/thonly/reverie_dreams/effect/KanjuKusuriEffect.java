package cc.thonly.reverie_dreams.effect;

import cc.thonly.reverie_dreams.inf.ILivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class KanjuKusuriEffect extends MobEffect {

    public KanjuKusuriEffect() {
        super(MobEffectCategory.BENEFICIAL, 16262179);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void onEffectStarted(LivingEntity entity, int amplifier) {
        super.onEffectStarted(entity, amplifier);
        ILivingEntity iLivingEntity  = (ILivingEntity) entity;
        if (entity.level() instanceof ServerLevel) {
            iLivingEntity.reverie_dreams$setKanju((ServerLevel) entity.level(), entity.blockPosition());
        }
    }

    @Override
    public boolean applyEffectTick(ServerLevel world, LivingEntity entity, int amplifier) {
        return super.applyEffectTick(world, entity, amplifier);
    }
}
