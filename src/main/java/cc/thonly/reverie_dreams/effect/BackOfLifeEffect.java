package cc.thonly.reverie_dreams.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class BackOfLifeEffect extends MobEffect {

    protected BackOfLifeEffect() {
        super(MobEffectCategory.BENEFICIAL, 16262179);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(ServerLevel world, LivingEntity entity, int amplifier) {
        if(entity.isAlive() && entity.hasEffect(ModStatusEffects.ELIXIR_OF_LIFE)) {
             entity.removeEffect(ModStatusEffects.ELIXIR_OF_LIFE);
        }
        return super.applyEffectTick(world, entity, amplifier);
    }
}
