package cc.thonly.reverie_dreams.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.world.ServerWorld;

public class BackOfLifeEffect extends StatusEffect {

    protected BackOfLifeEffect() {
        super(StatusEffectCategory.BENEFICIAL, 16262179);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        if(entity.isAlive() && entity.hasStatusEffect(ModStatusEffects.ELIXIR_OF_LIFE)) {
             entity.removeStatusEffect(ModStatusEffects.ELIXIR_OF_LIFE);
        }
        return super.applyUpdateEffect(world, entity, amplifier);
    }
}
