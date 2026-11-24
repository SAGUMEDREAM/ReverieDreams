package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.reverie_dreams.entity.YoukaiRabbit;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.Comparator;
import java.util.EnumSet;

/**
 * Only higher IDs follow lower IDs, avoiding two rabbits endlessly chasing each other.
 */
public final class YoukaiRabbitGatherGoal extends Goal {
    private final YoukaiRabbit rabbit;
    private YoukaiRabbit leader;
    private int nextSearch;
    private int nextPath;

    public YoukaiRabbitGatherGoal(YoukaiRabbit rabbit) {
        this.rabbit = rabbit;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    private boolean idle(YoukaiRabbit rabbit) {
        return rabbit.isAlive() && rabbit.getTarget() == null && !rabbit.isTrading();
    }

    @Override
    public boolean canUse() {
        if (!this.idle(this.rabbit) || this.rabbit.tickCount < this.nextSearch)
            return false;
        this.nextSearch = this.rabbit.tickCount + 40;
        this.leader = this.rabbit.level().getEntitiesOfClass(YoukaiRabbit.class,
                        this.rabbit.getBoundingBox().inflate(16, 6, 16),
                        other -> other.getId() < this.rabbit.getId() && this.idle(other)
                                && this.rabbit.withinRabbitHome(other.blockPosition()))
                .stream().min(Comparator.comparingInt(YoukaiRabbit::getId)).orElse(null);
        return this.leader != null && this.rabbit.distanceToSqr(this.leader) > 16;
    }

    @Override
    public boolean canContinueToUse() {
        return this.leader != null && this.idle(this.rabbit) && this.idle(this.leader)
                && this.rabbit.distanceToSqr(this.leader) > 9 && this.rabbit.distanceToSqr(this.leader) < 24 * 24
                && this.rabbit.withinRabbitHome(this.leader.blockPosition());
    }

    @Override
    public void start() {
        this.nextPath = 0;
    }

    @Override
    public void tick() {
        if (this.rabbit.tickCount < this.nextPath)
            return;
        this.nextPath = this.rabbit.tickCount + 10;
        this.rabbit.getNavigation().moveTo(this.leader, .75);
    }

    @Override
    public void stop() {
        this.leader = null;
        this.rabbit.getNavigation().stop();
    }
}
