package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.reverie_dreams.entity.ForestYoukai;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/** Direct injury and a nearby species' alarm both take priority over ordinary hunting. */
public final class ForestYoukaiRetaliationGoal extends Goal {
    private final ForestYoukai mob;
    private @Nullable LivingEntity attacker;

    public ForestYoukaiRetaliationGoal(ForestYoukai mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override public boolean canUse() {
        this.attacker = this.mob.retaliationTarget();
        return this.attacker != null && this.mob.canContinueFight(this.attacker);
    }

    @Override public void start() { this.mob.setTarget(this.attacker); }

    @Override public boolean canContinueToUse() {
        return this.attacker != null && this.mob.isRetaliatingAgainst(this.attacker)
                && this.mob.getTarget() == this.attacker && this.mob.canContinueFight(this.attacker);
    }

    @Override public void stop() {
        if (this.mob.getTarget() == this.attacker) this.mob.setTarget(null);
        this.attacker = null;
    }
}
