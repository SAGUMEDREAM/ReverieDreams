package cc.thonly.reverie_dreams.effect;

import cc.thonly.reverie_dreams.registry.content.effect.RDStatusEffects;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;

public class BackOfLifeEffect extends MobEffect {

    public BackOfLifeEffect() {
        super(MobEffectCategory.BENEFICIAL, 16262179);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(ServerLevel world, LivingEntity entity, int amplifier) {
        if (!entity.isAlive()) {
            return super.applyEffectTick(world, entity, amplifier);
        }
        if (entity.hasEffect(RDStatusEffects.ELIXIR_OF_LIFE.builtInHolder())) {
            entity.removeEffect(RDStatusEffects.ELIXIR_OF_LIFE.builtInHolder());
        }
        if (entity.tickCount % 20 == 0) {
            entity.heal(1.0F);
        }
        return super.applyEffectTick(world, entity, amplifier);
    }
}
