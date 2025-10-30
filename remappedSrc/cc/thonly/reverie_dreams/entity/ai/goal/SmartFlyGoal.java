package cc.thonly.reverie_dreams.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.phys.Vec3;

public class SmartFlyGoal extends Goal {
    private final PathfinderMob mob;
    private final double speed;
    private Vec3 movingTarget;

    public SmartFlyGoal(PathfinderMob mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.mob.getTarget() == null && this.mob.onGround();
    }

    @Override
    public void start() {
        Vec3 direction = this.mob.getViewVector(0.0f);
        Vec3 target = HoverRandomPos.getPos(
                this.mob,
                8,
                7,
                direction.x,
                direction.z,
                (float)(Math.PI / 2),
                3,
                1
        );
        if (target != null) {
            this.mob.getNavigation().moveTo(target.x, target.y, target.z, this.speed);
        }
        this.movingTarget = target;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.getNavigation().isDone();
    }
}
