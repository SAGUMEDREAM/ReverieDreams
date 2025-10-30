package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.reverie_dreams.entity.ai.goal.util.EntityTargetUtil;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class NPCTrackOwnerAttackerGoal extends TargetGoal {
    private final TamableAnimal tameable;
    private LivingEntity attacker;
    private int lastAttackedTime;

    public NPCTrackOwnerAttackerGoal(TamableAnimal tameable) {
        super(tameable, false);
        this.tameable = tameable;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
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
        if (livingEntity instanceof OwnableEntity targetEntity) {
            if (this.tameable.getOwner() == targetEntity.getOwner()) {
                return false;
            }
        }
        this.attacker = livingEntity.getLastHurtByMob();
        int i = livingEntity.getLastHurtByMobTimestamp();
        boolean isMaid = tameable instanceof BaseNPCLikeEntity;
        return i != this.lastAttackedTime && (isMaid && EntityTargetUtil.canAttack(attacker, (BaseNPCLikeEntity) tameable)) && this.canAttack(this.attacker, TargetingConditions.DEFAULT) && this.tameable.wantsToAttack(this.attacker, livingEntity);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.attacker);
        LivingEntity livingEntity = this.tameable.getOwner();
        if (livingEntity != null) {
            this.lastAttackedTime = livingEntity.getLastHurtByMobTimestamp();
        }
        super.start();
    }

}

