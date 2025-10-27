package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.world.World;

public class WakeUpGoal extends Goal {
    private final BaseNPCLikeEntity entity;

    public WakeUpGoal(BaseNPCLikeEntity entity) {
        this.entity = entity;
    }

    @Override
    public boolean canStart() {
        World world = this.entity.getWorld();
        return !world.isClient && this.entity.getWorld().isDay();
    }

    @Override
    public void start() {
        this.entity.wakeUp();
    }

    @Override
    public void tick() {
        super.tick();
        if (canStart()) {
            start();
        }
    }

    @Override
    public boolean shouldContinue() {
        return true;
    }

}
