package cc.thonly.reverie_dreams.entity.ai.goal.attack;

import cc.thonly.reverie_dreams.api.entity.type.DanmakuShooter;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuVolleyBudget;
import cc.thonly.reverie_dreams.entity.ai.TargetReachability;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.item.danmaku.AbstractDanmakuItem;
import cc.thonly.reverie_dreams.item.danmaku.DanmakuItem;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.sound.RDSoundEvents;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.EnumSet;

import cc.thonly.reverie_dreams.entity.ai.goal.util.EntityTargetUtil;
import cc.thonly.reverie_dreams.entity.variant.GensokyoFairyEntity;

@Setter
@Getter
public class NPCDanmakuItemGoal<T extends BaseNPCLikeEntity> extends Goal {
    private final T actor;
    private final double speed;
    private final TargetReachability reachability;
    private int attackInterval;
    private final float squaredRange;

    private int updateCountdownTicks = -1;

    public NPCDanmakuItemGoal(T actor, double speed, int attackInterval, float range) {
        this.actor = actor;
        this.speed = speed;
        this.reachability = new TargetReachability(this.actor);
        this.attackInterval = attackInterval;
        this.squaredRange = range * range;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.actor.getTarget();
        if (target == null || !EntityTargetUtil.canAttack(target, this.actor)) {
            return false;
        }
        if (target == this.actor.getOwner()) {
            return false;
        }
        if (target instanceof TamableAnimal tameableEntity) {
            if (tameableEntity.getOwner() == this.actor.getOwner()) {
                return false;
            }
        }
        return this.isHoldingDanmaku() && this.reachability.canEngage(target);
    }

    private boolean isHoldingDanmaku() {
        return this.actor.getMainHandItem().getItem() instanceof DanmakuItem;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.actor.getTarget();
        return target != null
                && target.isAlive()
                && this.isHoldingDanmaku()
                && this.reachability.canEngage(target)
                && (this.canUse() || !this.actor.getNavigation().isDone());
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
        if (target == null || !target.isAlive()) {
            this.stop();
            return;
        }
        float[] pitchYaw = DanmakuShooter.getPitchYaw(this.actor, target);
        this.actor.getLookControl().setLookAt(target);
        this.actor.setXRot(pitchYaw[0]);
        this.actor.setYRot(pitchYaw[1]);

        if (!this.canUse())
            return;

        boolean hasLineOfSight = this.actor.getSensing().hasLineOfSight(target);
        double distanceSq = this.actor.distanceToSqr(target);
        if (distanceSq > squaredRange || !hasLineOfSight) {
            if (this.actor.getNavigation().isDone() && this.reachability.test(target)) {
                this.actor.getNavigation().moveTo(target, speed);
            }
        } else {
            this.actor.getNavigation().stop();
        }

        if (--this.updateCountdownTicks <= 0) {
            Level world = this.actor.level();
            if (world instanceof ServerLevel serverWorld && hasLineOfSight && distanceSq <= squaredRange) {
                ItemStack itemStack = this.actor.getMainHandItem();
                DanmakuProperties properties = itemStack.get(RDDataComponentTypes.DANMAKU_PROPERTIES.value());
                if (properties == null) {
                    return;
                }
                Item item = itemStack.getItem();
                if (!(item instanceof AbstractDanmakuItem danmakuItem)) {
                    return;
                }
                DanmakuVolleyBudget.begin(DanmakuVolleyBudget.MAX_BULLETS_PER_USE);
                GensokyoFairyEntity.beginRangedAttack(this.actor);
                try {
                    for (int i = 0; i < properties.count(); i++) {
                        danmakuItem.shoot(serverWorld, this.actor, InteractionHand.MAIN_HAND);
                    }
                } finally {
                    DanmakuVolleyBudget.end();
                }
                if (!properties.infinite()) {
                    itemStack.hurtAndBreak(1, this.actor, InteractionHand.MAIN_HAND);
                }

                this.actor.swing(InteractionHand.MAIN_HAND);
                world.playSound(null, this.actor.getX(), this.actor.getY(), this.actor.getZ(), RDSoundEvents.FIRE, SoundSource.NEUTRAL, 1f, 1.0f);
            }
            this.resetCooldown();
        }
    }

    private void resetCooldown() {
        this.updateCountdownTicks = Math.max(1, attackInterval);
    }

}
