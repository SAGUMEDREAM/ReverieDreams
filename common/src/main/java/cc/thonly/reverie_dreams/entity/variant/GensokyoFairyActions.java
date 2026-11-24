package cc.thonly.reverie_dreams.entity.variant;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.util.entity.IAnimationHelper;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public final class GensokyoFairyActions {
    public static final byte RANGED_EVENT = 110;
    public static final int DEATH_TICKS = 36;
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("animation.death");
    private GensokyoFairyAnimation action;
    private int actionUntil;
    private int revision;
    private int previousHurtTime;

    public void begin(GensokyoFairyAnimation action, int tick) {
        if (action == GensokyoFairyAnimation.MELEE && this.action == GensokyoFairyAnimation.RANGED && tick < this.actionUntil) return;
        this.action = action;
        this.actionUntil = tick + action.durationTicks();
        this.revision++;
    }

    public int revision() { return this.revision; }
    public boolean isActive(GensokyoFairyAnimation action, int tick) { return this.action == action && tick < this.actionUntil; }

    public GensokyoFairyAnimation select(LivingEntity entity) {
        if (entity.hurtTime > this.previousHurtTime) this.revision++;
        this.previousHurtTime = entity.hurtTime;
        return select(entity.deathTime > 0 || entity.isDeadOrDying(), entity.hurtTime > 0,
                entity.tickCount, entity.onGround() || entity.isInWater(), entity.getDeltaMovement().horizontalDistanceSqr() > 0.0001);
    }

    public GensokyoFairyAnimation select(boolean dying, boolean hurt, int tick, boolean grounded, boolean moving) {
        if (dying) return GensokyoFairyAnimation.DEATH;
        if (hurt) return GensokyoFairyAnimation.HURT;
        if (this.action != null && tick < this.actionUntil) return this.action;
        return GensokyoFairyAnimation.select(grounded, moving);
    }

    public static <T extends BaseNPCLikeEntity & GensokyoFairyEntity> void registerControllers(T entity, AnimatableManager.ControllerRegistrar controllers) {
        int[] previousRevision = {-1};
        GensokyoFairyAnimation[] previous = {null};
        controllers.add(new AnimationController<T>("fairy", 2, state -> {
            var actions = entity.fairyActions();
            var next = actions.select(entity);
            if (next != previous[0] || (!next.looping() && actions.revision != previousRevision[0])) {
                state.controller().reset();
                state.controller().setTransitionTicks(next == GensokyoFairyAnimation.DEATH ? 0 : 2);
                previous[0] = next;
                previousRevision[0] = actions.revision;
            }
            return state.setAndContinue(next == GensokyoFairyAnimation.DEATH ? DEATH
                    : next.looping() ? IAnimationHelper.presets().loop(next.animationName()) : IAnimationHelper.presets().play(next.animationName()));
        }));
    }

    /** Keep the normal death completion path, with time for the fairy to disperse first. */
    public static boolean advanceDeath(BaseNPCLikeEntity entity) {
        entity.setDeltaMovement(0, 0, 0);
        entity.setNoGravity(true);
        entity.deathTime++;
        if (entity.level() instanceof ServerLevel level && entity.deathTime % 2 == 0) {
            double y = entity.getY() + 1.1 + entity.deathTime * 0.012;
            level.sendParticles(ParticleTypes.GLOW, entity.getX(), y, entity.getZ(), 3, 0.32, 0.4, 0.32, 0.025);
            if (entity.deathTime >= 10) level.sendParticles(ParticleTypes.CHERRY_LEAVES,
                    entity.getX(), y, entity.getZ(), 1, 0.3, 0.3, 0.3, 0.015);
            if (entity.deathTime % 6 == 0) level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    entity.getX(), y, entity.getZ(), 2, 0.35, 0.35, 0.35, 0.01);
        }
        if (entity.deathTime >= DEATH_TICKS) {
            entity.deathTime = 19;
            return true;
        }
        return false;
    }
}
