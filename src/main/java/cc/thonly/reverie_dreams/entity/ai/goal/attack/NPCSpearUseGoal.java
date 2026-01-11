package cc.thonly.reverie_dreams.entity.ai.goal.attack;


import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.item.component.KineticWeapon;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;

public class NPCSpearUseGoal<T extends BaseNPCLikeEntity> extends Goal {
    static final int MIN_REPOSITION_DISTANCE = 6;
    static final int MAX_REPOSITION_DISTANCE = 7;
    static final int MIN_COOLDOWN_DISTANCE = 9;
    static final int MAX_COOLDOWN_DISTANCE = 11;
    static final double MAX_FLEEING_TIME = reducedTickDelay(100);
    private final T mob;
    private NPCSpearUseGoal.@Nullable SpearUseState state;
    double speedModifierWhenCharging;
    double speedModifierWhenRepositioning;
    float approachDistanceSq;
    float targetInRangeRadiusSq;

    public NPCSpearUseGoal(T mob, double speedModifierWhenCharging, double speedModifierWhenRepositioning, float approachDistanceSq, float targetInRangeRadiusSq) {
        this.mob = mob;
        this.speedModifierWhenCharging = speedModifierWhenCharging;
        this.speedModifierWhenRepositioning = speedModifierWhenRepositioning;
        this.approachDistanceSq = approachDistanceSq * approachDistanceSq;
        this.targetInRangeRadiusSq = targetInRangeRadiusSq * targetInRangeRadiusSq;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.ableToAttack() && !this.mob.isUsingItem();
    }

    private boolean ableToAttack() {
        return this.mob.getTarget() != null && this.mob.getMainHandItem().has(DataComponents.KINETIC_WEAPON);
    }

    private int getKineticWeaponUseDuration() {
        int i = (Integer) Optional.ofNullable(this.mob.getMainHandItem().get(DataComponents.KINETIC_WEAPON)).map(KineticWeapon::computeDamageUseDuration).orElse(0);
        return reducedTickDelay(i);
    }

    @Override
    public boolean canContinueToUse() {
        return this.state != null && !this.state.done && this.ableToAttack();
    }

    @Override
    public void start() {
        super.start();
        this.mob.setAggressive(true);
        this.state = new NPCSpearUseGoal.SpearUseState();
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.getNavigation().stop();
        this.mob.setAggressive(false);
        this.state = null;
        this.mob.stopUsingItem();
    }

    @Override
    public void tick() {
        if (this.state != null) {
            LivingEntity livingEntity = this.mob.getTarget();
            double d = this.mob.distanceToSqr(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
            Entity entity = this.mob.getRootVehicle();
            float f = 1.0F;
            if (entity instanceof Mob mob) {
                f = mob.chargeSpeedModifier();
            }

            int i = this.mob.isPassenger() ? 2 : 0;
            this.mob.lookAt(livingEntity, 30.0F, 30.0F);
            this.mob.getLookControl().setLookAt(livingEntity, 30.0F, 30.0F);
            if (this.state.notEngagedYet()) {
                if (d > this.approachDistanceSq) {
                    this.mob.getNavigation().moveTo(livingEntity, f * this.speedModifierWhenRepositioning);
                    return;
                }

                this.state.startEngagement(this.getKineticWeaponUseDuration());
                this.mob.startUsingItem(InteractionHand.MAIN_HAND);
            }

            if (this.state.tickAndCheckEngagement()) {
                this.mob.stopUsingItem();
                double e = Math.sqrt(d);
                this.state.awayPos = LandRandomPos.getPosAway(this.mob, Math.max(0.0, 9 + i - e), Math.max(1.0, 11 + i - e), 7, livingEntity.position());
                this.state.fleeingTime = 1;
            }

            if (!this.state.tickAndCheckFleeing()) {
                if (this.state.awayPos != null) {
                    this.mob.getNavigation().moveTo(this.state.awayPos.x, this.state.awayPos.y, this.state.awayPos.z, f * this.speedModifierWhenRepositioning);
                    if (this.mob.getNavigation().isDone()) {
                        if (this.state.fleeingTime > 0) {
                            this.state.done = true;
                            return;
                        }

                        this.state.awayPos = null;
                    }
                } else {
                    this.mob.getNavigation().moveTo(livingEntity, f * this.speedModifierWhenCharging);
                    if (d < this.targetInRangeRadiusSq || this.mob.getNavigation().isDone()) {
                        double e = Math.sqrt(d);
                        this.state.awayPos = LandRandomPos.getPosAway(this.mob, 6 + i - e, 7 + i - e, 7, livingEntity.position());
                    }
                }
            }
        }
    }

    public static class SpearUseState {
        private int engageTime = -1;
        int fleeingTime = -1;
        @Nullable
        Vec3 awayPos;
        boolean done = false;

        public boolean notEngagedYet() {
            return this.engageTime < 0;
        }

        public void startEngagement(int i) {
            this.engageTime = i;
        }

        public boolean tickAndCheckEngagement() {
            if (this.engageTime > 0) {
                this.engageTime--;
                if (this.engageTime == 0) {
                    return true;
                }
            }

            return false;
        }

        public boolean tickAndCheckFleeing() {
            if (this.fleeingTime > 0) {
                this.fleeingTime++;
                if (this.fleeingTime > NPCSpearUseGoal.MAX_FLEEING_TIME) {
                    this.done = true;
                    return true;
                }
            }

            return false;
        }
    }
}
