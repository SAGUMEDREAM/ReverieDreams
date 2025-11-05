package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.reverie_dreams.entity.interfaces.DanmakuShooter;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

@Getter
@Setter
@ToString
public class DanmakuGoal extends Goal {
    public static final DanmakuShooter DEFAULT_MOB_DANMAKU_FIRE_LAUNCHER = DanmakuShooter.DEFAULT;
    private final LivingEntity self;
    private final Mob mob;
    @Nullable
    private LivingEntity attackTarget;
    private final DanmakuShooter launcher;

    private final int minDelayTicks;
    private final int maxDelayTicks;
    private int updateCountdownTicks = -1;

    public DanmakuGoal(LivingEntity self, @Nullable DanmakuShooter launcher) {
        this(self, launcher, 20, 20 * 3);
    }

    public DanmakuGoal(LivingEntity self, @Nullable DanmakuShooter launcher, int minDelayTicks, int maxDelayTicks) {
        this.self = self;
        this.mob = (Mob) self;
        this.launcher = launcher != null ? launcher : DEFAULT_MOB_DANMAKU_FIRE_LAUNCHER;
        this.minDelayTicks = minDelayTicks;
        this.maxDelayTicks = maxDelayTicks;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity == null || !livingEntity.isAlive()) {
            return false;
        }
        if (livingEntity instanceof Player player) {
            if (player.hasInfiniteMaterials()) {
                return false;
            }
        }
        this.attackTarget = livingEntity;
        return true;
    }

    @Override
    public void start() {
        this.resetCooldown();
    }

    @Override
    public void stop() {
        this.attackTarget = null;
        this.updateCountdownTicks = -1;
    }

    @Override
    public boolean canContinueToUse() {
        return this.attackTarget != null && this.attackTarget.isAlive();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private void resetCooldown() {
        this.updateCountdownTicks = minDelayTicks + this.self.getRandom().nextInt(maxDelayTicks - minDelayTicks + 1);
    }

    @Override
    public void tick() {
        if (this.attackTarget == null || !this.attackTarget.isAlive()) {
            return;
        }

        float[] pitchYaw = DanmakuShooter.getPitchYaw(this.self, this.attackTarget);
        this.mob.getLookControl().setLookAt(this.attackTarget);
        this.mob.setXRot(pitchYaw[0]);
        this.mob.setYRot(pitchYaw[1]);

        double distanceSq = this.self.distanceToSqr(this.attackTarget);
        if (distanceSq > 64.0) {
            if (this.mob.getNavigation().isDone()) {
                this.mob.getNavigation().moveTo(this.attackTarget, 1.5);
            }
        } else {
            this.mob.getNavigation().stop();
        }

        if (--this.updateCountdownTicks <= 0) {
            Level world = this.self.level();
            if (world instanceof ServerLevel serverWorld) {
                this.launcher.fire(this.self, this.attackTarget, serverWorld);
                this.launcher.sound(this.self);
            }
            this.resetCooldown();
        }
    }
}
