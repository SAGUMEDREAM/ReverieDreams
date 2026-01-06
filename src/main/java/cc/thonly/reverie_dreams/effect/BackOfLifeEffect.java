package cc.thonly.reverie_dreams.effect;

import cc.thonly.reverie_dreams.registry.content.effect.RDStatusEffects;
import eu.pb4.polymer.core.api.other.PolymerPotion;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class BackOfLifeEffect extends MobEffect implements PolymerPotion {

    public BackOfLifeEffect() {
        super(MobEffectCategory.BENEFICIAL, 16262179);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(ServerLevel world, LivingEntity entity, int amplifier) {
        if(entity.isAlive() && entity.hasEffect(RDStatusEffects.ELIXIR_OF_LIFE)) {
             entity.removeEffect(RDStatusEffects.ELIXIR_OF_LIFE);
        }
        return super.applyEffectTick(world, entity, amplifier);
    }
}
