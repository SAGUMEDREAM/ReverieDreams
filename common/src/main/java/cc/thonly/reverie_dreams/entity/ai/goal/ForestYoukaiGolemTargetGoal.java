package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.reverie_dreams.entity.ForestYoukai;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.level.Level;

public final class ForestYoukaiGolemTargetGoal extends NearestAttackableTargetGoal<ForestYoukai> {
    public ForestYoukaiGolemTargetGoal(PathfinderMob mob) {
        super(mob, ForestYoukai.class, 10, true, false, (target, level) -> level.dimension().equals(Level.OVERWORLD));
    }

    @Override
    public boolean canUse() {
        return this.mob.level().dimension().equals(Level.OVERWORLD) && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return this.mob.level().dimension().equals(Level.OVERWORLD) && super.canContinueToUse();
    }
}
