package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

public class WakeUpGoal extends Goal {
    private final BaseNPCLikeEntity entity;

    public WakeUpGoal(BaseNPCLikeEntity entity) {
        this.entity = entity;
    }

    @Override
    public boolean canUse() {
        Level world = this.entity.level();
        return !world.isClientSide && this.entity.level().isDay();
    }

    @Override
    public void start() {
        this.entity.stopSleeping();
    }

    @Override
    public void tick() {
        super.tick();
        if (canUse()) {
            start();
        }
    }

    @Override
    public boolean canContinueToUse() {
        return true;
    }

}
