package cc.thonly.reverie_dreams.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/**
 * 缓存目标的寻路可达性。createPath 开销较大，因此按时间/目标移动距离缓存结果。
 */
public final class TargetReachability {
    private static final int CHECK_INTERVAL_TICKS = 20;
    private static final double TARGET_MOVE_SQR = 4.0 * 4.0;

    private final Mob mob;
    private LivingEntity target;
    private Vec3 checkedTargetPos = Vec3.ZERO;
    private int cooldown;
    private boolean reachable = true;

    public TargetReachability(Mob mob) {
        this.mob = mob;
    }

    /**
     * 能打到的目标（有视线）永远可以打，哪怕寻路不可达。
     * 只有既看不见、又没有路可以靠近时，才认为不该继续攻击。
     */
    public boolean canEngage(LivingEntity target) {
        if (target == null) {
            return false;
        }
        if (this.mob.getSensing().hasLineOfSight(target)) {
            return true;
        }
        return this.test(target);
    }

    public boolean test(LivingEntity target) {
        if (target == null) {
            return false;
        }
        if (target != this.target
                || this.cooldown-- <= 0
                || this.checkedTargetPos.distanceToSqr(target.position()) > TARGET_MOVE_SQR) {
            this.target = target;
            this.checkedTargetPos = target.position();
            this.cooldown = CHECK_INTERVAL_TICKS;
            this.reachable = this.mob.getNavigation().createPath(target, 2) != null;
        }
        return this.reachable;
    }
}
