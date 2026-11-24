package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.reverie_dreams.entity.ForestYoukai;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.gamerules.GameRules;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;

/**
 * One short local search, then a bounded path; never scans items throughout the level.
 */
public final class ForestYoukaiFeedingGoal extends Goal {
    private final ForestYoukai mob;
    private @Nullable ItemEntity target;
    private int nextSearch;
    private int until;

    public ForestYoukaiFeedingGoal(ForestYoukai mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.mob.getTarget() != null || this.mob.tickCount < this.nextSearch
                || !(this.mob.level() instanceof ServerLevel level) || !level.getGameRules().get(GameRules.MOB_GRIEFING))
            return false;
        this.nextSearch = this.mob.tickCount + 30 + this.mob.getRandom().nextInt(20);
        this.target = level.getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(10, 4, 10), this.mob::canAcceptMeat)
                .stream().min(Comparator.comparingDouble(this.mob::distanceToSqr)).orElse(null);
        if (this.target == null)
            return false;
        var path = this.mob.getNavigation().createPath(this.target, 0);
        return path != null && path.canReach() && this.mob.getNavigation().moveTo(path, 1);
    }

    @Override
    public void start() {
        this.until = this.mob.tickCount + 100;
    }

    @Override
    public boolean canContinueToUse() {
        return this.target != null && this.mob.getTarget() == null && this.mob.tickCount < this.until
                && this.mob.canAcceptMeat(this.target) && !this.mob.getNavigation().isDone();
    }

    @Override
    public void tick() {
        if (this.target != null)
            this.mob.getLookControl().setLookAt(this.target, 20, 20);
    }

    @Override
    public void stop() {
        this.target = null;
        this.mob.getNavigation().stop();
    }
}
