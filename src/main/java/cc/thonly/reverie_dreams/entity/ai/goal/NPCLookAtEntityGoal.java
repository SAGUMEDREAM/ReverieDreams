package cc.thonly.reverie_dreams.entity.ai.goal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.function.Predicate;

public class NPCLookAtEntityGoal extends Goal {
    public static final float DEFAULT_CHANCE = 0.02F;
    protected final Mob mob;
    @Nullable
    protected Entity target;
    protected final float range;
    private int lookTime;
    protected final float chance;
    private final boolean lookForward;
    protected final Class<? extends LivingEntity> targetType;
    protected final TargetingConditions targetPredicate;

    public NPCLookAtEntityGoal(Mob mob, Class<? extends LivingEntity> targetType, float range) {
        this(mob, targetType, range, 0.02F);
    }

    public NPCLookAtEntityGoal(Mob mob, Class<? extends LivingEntity> targetType, float range, float chance) {
        this(mob, targetType, range, chance, false);
    }

    public NPCLookAtEntityGoal(Mob mob, Class<? extends LivingEntity> targetType, float range, float chance, boolean lookForward) {
        this.mob = mob;
        this.targetType = targetType;
        this.range = range;
        this.chance = chance;
        this.lookForward = lookForward;
        this.setFlags(EnumSet.of(Flag.LOOK));
        if (targetType == Player.class) {
            Predicate<Entity> predicate = EntitySelector.notRiding(mob);
            this.targetPredicate = TargetingConditions.forNonCombat().range((double)range).selector((entity, world) -> {
                return predicate.test(entity);
            });
        } else {
            this.targetPredicate = TargetingConditions.forNonCombat().range((double)range);
        }

    }

    public boolean canUse() {
        if (this.mob.getRandom().nextFloat() >= this.chance) {
            return false;
        } else {
            if (this.mob.getTarget() != null) {
                this.target = this.mob.getTarget();
            }

            ServerLevel serverWorld = getServerLevel(this.mob);
            if (this.targetType == Player.class) {
                this.target = serverWorld.getNearestPlayer(this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
            } else {
                this.target = serverWorld.getNearestEntity(this.mob.level().getEntitiesOfClass(this.targetType, this.mob.getBoundingBox().inflate((double)this.range, 3.0, (double)this.range), (livingEntity) -> {
                    return true;
                }), this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
            }

            return this.target != null;
        }
    }

    public boolean canContinueToUse() {
        if (!this.target.isAlive()) {
            return false;
        } else if (this.mob.distanceToSqr(this.target) > (double)(this.range * this.range)) {
            return false;
        } else {
            return this.lookTime > 0;
        }
    }

    public void start() {
        this.lookTime = this.adjustedTickDelay(40 + this.mob.getRandom().nextInt(40));
    }

    public void stop() {
        this.target = null;
    }

    public void tick() {
        if (this.target.isAlive()) {
            double d = this.lookForward ? this.mob.getEyeY() : this.target.getEyeY();
            this.mob.getLookControl().setLookAt(this.target.getX(), d, this.target.getZ());
            --this.lookTime;
            this.target.setYBodyRot(this.target.getYRot());
        }
    }
}
