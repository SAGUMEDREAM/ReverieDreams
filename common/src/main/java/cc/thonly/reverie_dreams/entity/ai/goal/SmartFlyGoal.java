package cc.thonly.reverie_dreams.entity.ai.goal;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class SmartFlyGoal extends Goal {
    private final PathfinderMob mob;
    private final double speed;
    private Vec3 movingTarget;
    private int nextStartTick;

    public SmartFlyGoal(PathfinderMob mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.mob.tickCount >= this.nextStartTick
                && this.mob.getTarget() == null
                && !this.mob.isPassenger()
                && !(this.mob instanceof TamableAnimal tameable && tameable.isOrderedToSit())
                && this.mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.nextStartTick = this.mob.tickCount + 80 + this.mob.getRandom().nextInt(120);
        Vec3 direction = this.mob.getViewVector(0.0f);
        // Keep idle hovering close to the ground. The old vertical search
        // range let fairies drift far above the player and become unhittable.
        Vec3 target = HoverRandomPos.getPos(
                this.mob,
                8,
                3,
                direction.x,
                direction.z,
                (float) (Math.PI / 2),
                2,
                1
        );
        if (target != null) {
            this.mob.getNavigation().moveTo(target.x, target.y, target.z, this.speed);
        }
        this.movingTarget = target;
    }

    @Override
    public boolean canContinueToUse() {
        return this.mob.getTarget() == null && !this.mob.getNavigation().isDone();
    }
}
