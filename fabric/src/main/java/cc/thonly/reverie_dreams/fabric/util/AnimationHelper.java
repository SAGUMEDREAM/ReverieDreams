package cc.thonly.reverie_dreams.fabric.util;

import cc.thonly.reverie_dreams.util.entity.IAnimationHelper;
import de.tomalbrc.bil.api.AnimatedHolder;
import de.tomalbrc.bil.api.Animator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class AnimationHelper implements IAnimationHelper {
    public static void updateWalkAnimation(LivingEntity entity, AnimatedHolder holder) {
        updateWalkAnimation(entity, holder, 0);
    }

    public static void updateWalkAnimation(LivingEntity entity, AnimatedHolder holder, int priority) {
        Animator animator = holder.getAnimator();
        if (IAnimationHelper.isActuallyMoving(entity)) {
            animator.playAnimation("walk", priority);
            animator.pauseAnimation("idle");
        } else {
            animator.pauseAnimation("walk");
            animator.playAnimation("idle", priority, true);
        }
    }

    public static void updateAttackAnimation(LivingEntity entity, AnimatedHolder holder) {
        updateAttackAnimation(entity, holder, 0);
    }

    public static void updateAttackAnimation(LivingEntity entity, AnimatedHolder holder, int priority) {
        Animator animator = holder.getAnimator();
        animator.pauseAnimation("idle");
        animator.pauseAnimation("walk");
        animator.playAnimation("attack", priority);
    }

    public static void updateHurtVariant(LivingEntity entity, AnimatedHolder holder) {
        updateHurtColor(entity, holder); // if you are using animated java, you could change to a different variant or use a color like we do here
    }

    public static void updateHurtColor(LivingEntity entity, AnimatedHolder holder) {
        if (entity.hurtTime > 0 || entity.deathTime > 0)
            holder.setColor(0xff7e7e);
        else
            holder.clearColor();
    }

}
