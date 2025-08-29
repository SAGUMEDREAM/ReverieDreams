package cc.thonly.reverie_dreams.entity.ai.goal.attack;

import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.entity.MobDanmakuShooter;
import cc.thonly.reverie_dreams.entity.npc.NPCEntityImpl;
import cc.thonly.reverie_dreams.item.danmaku.AbstractDanmakuItem;
import cc.thonly.reverie_dreams.item.danmaku.DanmakuItem;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.EnumSet;

@Setter
@Getter
public class NPCDanmakuItemGoal<T extends NPCEntityImpl> extends Goal {
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
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if ((this.actor).getTarget() == null) {
            return false;
        }
        if (this.actor.getTarget() == this.actor.getOwner()) {
            return false;
        }
        if (this.actor.getTarget() instanceof TameableEntity tameableEntity) {
            if (tameableEntity.getOwner() == this.actor.getOwner()) {
                return false;
            }
        }
        return this.isHoldingDanmaku();
    }

    private boolean isHoldingDanmaku() {
        return this.actor.getMainHandStack().getItem() instanceof DanmakuItem;
    }

    @Override
    public boolean shouldContinue() {
        return (this.canStart() || !this.actor.getNavigation().isIdle()) && this.isHoldingDanmaku();
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void stop() {
        super.stop();
        this.actor.setAttacking(false);
        this.actor.clearActiveItem();
    }

    @Override
    public void tick() {
        LivingEntity target = this.actor.getTarget();
        if (target == null || !target.isAlive()) {
            this.stop();
            return;
        }
        float[] pitchYaw = MobDanmakuShooter.getPitchYaw(this.actor, target);
        this.actor.getLookControl().lookAt(target);
        this.actor.setPitch(pitchYaw[0]);
        this.actor.setYaw(pitchYaw[1]);

        if (!this.canStart()) return;

        double distanceSq = this.actor.squaredDistanceTo(target);
        if (distanceSq > 64.0) {
            if (this.actor.getNavigation().isIdle()) {
                this.actor.getNavigation().startMovingTo(target, 1.5);
            }
        } else {
            this.actor.getNavigation().stop();
        }

        if (--this.updateCountdownTicks <= 0) {
            World world = this.actor.getWorld();
            if (world instanceof ServerWorld serverWorld) {
                ItemStack itemStack = this.actor.getMainHandStack();
                Boolean isInfinite = itemStack.getOrDefault(ModDataComponentTypes.Danmaku.INFINITE, false);
                Item item = itemStack.getItem();
                if (!(item instanceof AbstractDanmakuItem polymerDanmakuItem)) return;
                for (int i = 0; i < itemStack.getOrDefault(ModDataComponentTypes.Danmaku.COUNT, 3); i++) {
                    polymerDanmakuItem.shoot(serverWorld, this.actor, Hand.MAIN_HAND);
                }
                if (!isInfinite) {
                    itemStack.damage(1, this.actor, Hand.MAIN_HAND);
                    if (itemStack.isDamageable() && itemStack.getDamage() >= itemStack.getMaxDamage()) {
                        itemStack.decrement(1);
                    }
                }

                world.playSound(null, this.actor.getX(), this.actor.getY(), this.actor.getZ(), SoundEventInit.FIRE, SoundCategory.NEUTRAL, 1f, 1.0f);
            }
            this.resetCooldown();
        }
    }

    private void resetCooldown() {
        this.updateCountdownTicks = minDelayTicks + this.actor.getRandom().nextInt(maxDelayTicks - minDelayTicks + 1);
    }

}
