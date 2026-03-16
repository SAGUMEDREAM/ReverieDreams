package cc.thonly.reverie_dreams.entity.ai.goal.attack;

import cc.thonly.reverie_dreams.entity.RabbitUnitEntity;
import cc.thonly.reverie_dreams.entity.interfaces.DanmakuShooter;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.item.weapon.WeaponOfTheMoon;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

import java.util.EnumSet;

@Setter
@Getter
public class NPCWeaponOfTheMoonGoal<T extends BaseNPCLikeEntity> extends Goal {
    private final double fleeDistance = 8.0; // 逃跑拉开的距离
    private final double fleeSpeed = 1.6;    // 逃跑速度
    private final T actor;
    private int attackInterval;
    private final float range;
    private int bullet = 30;
    private int btCd = 20 * 5;
    private int atCd;

    public NPCWeaponOfTheMoonGoal(T actor, int attackInterval, float range) {
        this.actor = actor;
        this.range = range;
        this.attackInterval = attackInterval;
        this.atCd = this.attackInterval;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if ((this.actor).getTarget() == null) {
            return false;
        }
        if (this.actor.getTarget() == this.actor.getOwner()) {
            return false;
        }
        if (this.actor.getTarget() instanceof TamableAnimal tameableEntity) {
            if (tameableEntity.getOwner() == this.actor.getOwner()) {
                return false;
            }
        }
        return this.isHoldingWeaponOfTheMoon();
    }

    private boolean isHoldingWeaponOfTheMoon() {
        return this.actor.getMainHandItem().getItem() instanceof WeaponOfTheMoon;
    }

    @Override
    public boolean canContinueToUse() {
        return (this.canUse() || !this.actor.getNavigation().isDone()) && this.isHoldingWeaponOfTheMoon();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void stop() {
        super.stop();
        this.actor.setAggressive(false);
        this.actor.stopUsingItem();
    }


    @Override
    public void tick() {
        LivingEntity target = this.actor.getTarget();
        if (target == null || !target.isAlive() || target instanceof RabbitUnitEntity) {
            this.stop();
            return;
        }
        float[] pitchYaw = DanmakuShooter.getPitchYaw(this.actor, target);
        this.actor.getLookControl().setLookAt(target);
        this.actor.setXRot(pitchYaw[0]);
        this.actor.setYRot(pitchYaw[1]);

        if (!this.canUse()) return;

        double distanceSq = this.actor.distanceToSqr(target);
        if (distanceSq > this.range * this.range) {
            if (this.actor.getNavigation().isDone()) {
                this.actor.getNavigation().moveTo(target, 1.5);
            }
        } else {
            this.actor.getNavigation().stop();
        }

        if (this.btCd <= 0) {
            this.btCd = 20 * 5;
            this.bullet = 30;
        }
        if (this.bullet <= 0) {
            this.btCd--;
            this.fleeFromTarget(target);
        }
        Level world = this.actor.level();
        if (this.bullet > 0 && world instanceof ServerLevel serverLevel) {
            if (this.atCd <= 0) {
                this.actor.setAggressive(true);
                this.actor.startUsingItem(InteractionHand.MAIN_HAND);
                this.atCd = this.attackInterval;
                this.bullet--;
                if (RDItems.WEAPON_OF_THE_MOON instanceof WeaponOfTheMoon weaponOfTheMoon) {
                    weaponOfTheMoon.tryShoot(this.actor, serverLevel, InteractionHand.MAIN_HAND, 0.2f);
                }
            } else {
                this.atCd--;
            }
        }
    }

    private void fleeFromTarget(LivingEntity target) {
        double dx = this.actor.getX() - target.getX();
        double dz = this.actor.getZ() - target.getZ();

        double len = Math.sqrt(dx * dx + dz * dz);
        if (len == 0) {
            return;
        }
        if (len < 0.001) return;

        dx /= len;
        dz /= len;

        double fleeX = this.actor.getX() + dx * fleeDistance;
        double fleeZ = this.actor.getZ() + dz * fleeDistance;
        double fleeY = this.actor.getY();

        this.actor.getNavigation().moveTo(fleeX, fleeY, fleeZ, fleeSpeed);
    }

}
