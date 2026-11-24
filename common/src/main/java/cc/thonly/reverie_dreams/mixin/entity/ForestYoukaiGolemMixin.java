package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.entity.ai.goal.ForestYoukaiGolemTargetGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A narrow extra goal avoids giving passive Gensokyo residents the global Enemy marker.
 */
@Mixin(IronGolem.class)
public abstract class ForestYoukaiGolemMixin extends PathfinderMob {
    protected ForestYoukaiGolemMixin(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void reverie_dreams$guardHumansFromYoukai(CallbackInfo ci) {
        this.targetSelector.addGoal(3, new ForestYoukaiGolemTargetGoal(this));
    }
}
