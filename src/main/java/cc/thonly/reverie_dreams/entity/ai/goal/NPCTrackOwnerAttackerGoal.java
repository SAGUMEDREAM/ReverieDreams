package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.reverie_dreams.entity.ai.goal.util.EntityTargetUtil;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.passive.TameableEntity;

import java.util.EnumSet;

public class NPCTrackOwnerAttackerGoal extends TrackTargetGoal {
    private final TameableEntity tameable;
    private LivingEntity attacker;
    private int lastAttackedTime;

    public NPCTrackOwnerAttackerGoal(TameableEntity tameable) {
        super(tameable, false);
        this.tameable = tameable;
        this.setControls(EnumSet.of(Goal.Control.TARGET));
    }

    @Override
    public boolean canStart() {
        if (!this.tameable.isTamed() || this.tameable.isSitting()) {
            return false;
        }
        LivingEntity livingEntity = this.tameable.getOwner();
        if (livingEntity == null) {
            return false;
        }
        if (livingEntity instanceof Tameable targetEntity) {
            if (this.tameable.getOwner() == targetEntity.getOwner()) {
                return false;
            }
        }
        this.attacker = livingEntity.getAttacker();
        int i = livingEntity.getLastAttackedTime();
        boolean isMaid = tameable instanceof BaseNPCLikeEntity;
        return i != this.lastAttackedTime && (isMaid && EntityTargetUtil.canAttack(attacker, (BaseNPCLikeEntity) tameable)) && this.canTrack(this.attacker, TargetPredicate.DEFAULT) && this.tameable.canAttackWithOwner(this.attacker, livingEntity);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.attacker);
        LivingEntity livingEntity = this.tameable.getOwner();
        if (livingEntity != null) {
            this.lastAttackedTime = livingEntity.getLastAttackedTime();
        }
        super.start();
    }

}

