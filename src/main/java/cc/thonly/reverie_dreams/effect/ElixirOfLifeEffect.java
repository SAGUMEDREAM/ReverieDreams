package cc.thonly.reverie_dreams.effect;

import eu.pb4.polymer.core.api.other.PolymerPotion;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class ElixirOfLifeEffect extends MobEffect implements PolymerPotion {

    public ElixirOfLifeEffect() {
        super(MobEffectCategory.BENEFICIAL, 16262179);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(ServerLevel world, LivingEntity entity, int amplifier) {
        return super.applyEffectTick(world, entity, amplifier);
    }
}
