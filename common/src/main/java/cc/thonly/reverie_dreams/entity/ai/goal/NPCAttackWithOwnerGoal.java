package cc.thonly.reverie_dreams.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;

public class NPCAttackWithOwnerGoal extends TargetGoal {
    private final TamableAnimal tameable;
    private LivingEntity attacking;
    private int lastAttackTime;

    public NPCAttackWithOwnerGoal(TamableAnimal tameable) {
        super(tameable, false);
        this.tameable = tameable;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!this.tameable.isTame() || this.tameable.isOrderedToSit()) {
            return false;
        }
        LivingEntity livingEntity = this.tameable.getOwner();
        if (livingEntity == null) {
            return false;
        }
        if (livingEntity.getLastHurtMob() instanceof TamableAnimal livingTamable) {
            LivingEntity owner1 = livingTamable.getOwner();
            if (owner1 == livingEntity) {
                return false;
            }
        }
        this.attacking = livingEntity.getLastHurtMob();
        int i = livingEntity.getLastHurtMobTimestamp();
        return i != this.lastAttackTime && this.canAttack(this.attacking, TargetingConditions.DEFAULT) && this.tameable.wantsToAttack(this.attacking, livingEntity);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.attacking);
        LivingEntity livingEntity = this.tameable.getOwner();
        if (livingEntity != null) {
            this.lastAttackTime = livingEntity.getLastHurtMobTimestamp();
        }
        super.start();
    }
}
