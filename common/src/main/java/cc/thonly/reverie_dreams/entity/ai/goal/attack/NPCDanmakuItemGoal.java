package cc.thonly.reverie_dreams.entity.ai.goal.attack;

import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.entity.interfaces.DanmakuShooter;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.item.danmaku.AbstractDanmakuItem;
import cc.thonly.reverie_dreams.item.danmaku.DanmakuItem;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.sound.RDSoundEvents;
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

@Setter
@Getter
public class NPCDanmakuItemGoal<T extends BaseNPCLikeEntity> extends Goal {
    private final T actor;
    private final double speed;
    private int attackInterval;
    private final float squaredRange;

    private final int minDelayTicks = 10;
    private final int maxDelayTicks = 10 * 2;
    private int updateCountdownTicks = -1;

    public NPCDanmakuItemGoal(T actor, double speed, int attackInterval, float range) {
        this.actor = actor;
        this.speed = speed;
        this.attackInterval = attackInterval;
        this.squaredRange = range * range;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
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
        return this.isHoldingDanmaku();
    }

    private boolean isHoldingDanmaku() {
        return this.actor.getMainHandItem().getItem() instanceof DanmakuItem;
    }

    @Override
    public boolean canContinueToUse() {
        return (this.canUse() || !this.actor.getNavigation().isDone()) && this.isHoldingDanmaku();
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

        if (!this.canUse()) return;

        double distanceSq = this.actor.distanceToSqr(target);
        if (distanceSq > 64.0) {
            if (this.actor.getNavigation().isDone()) {
                this.actor.getNavigation().moveTo(target, 1.5);
            }
        } else {
            this.actor.getNavigation().stop();
        }

        if (--this.updateCountdownTicks <= 0) {
            Level world = this.actor.level();
            if (world instanceof ServerLevel serverWorld) {
                ItemStack itemStack = this.actor.getMainHandItem();
                DanmakuProperties properties = itemStack.get(RDDataComponents.DANMAKU_PROPERTIES.value());
                if (properties == null) {
                    return;
                }
                Item item = itemStack.getItem();
                if (!(item instanceof AbstractDanmakuItem danmakuItem)) {
                    return;
                }
                for (int i = 0; i < properties.count(); i++) {
                    danmakuItem.shoot(serverWorld, this.actor, InteractionHand.MAIN_HAND);
                }
                if (!properties.infinite()) {
                    itemStack.hurtAndBreak(1, this.actor, InteractionHand.MAIN_HAND);
                    if (itemStack.isDamageableItem() && itemStack.getDamageValue() >= itemStack.getMaxDamage()) {
                        itemStack.shrink(1);
                    }
                }

                this.actor.swing(InteractionHand.MAIN_HAND);
                world.playSound(null, this.actor.getX(), this.actor.getY(), this.actor.getZ(), RDSoundEvents.FIRE, SoundSource.NEUTRAL, 1f, 1.0f);
            }
            this.resetCooldown();
        }
    }

    private void resetCooldown() {
        this.updateCountdownTicks = minDelayTicks + this.actor.getRandom().nextInt(maxDelayTicks - minDelayTicks + 1);
    }

}
