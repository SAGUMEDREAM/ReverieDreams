package cc.thonly.reverie_dreams.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.Goal.Control;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

public class NPCLookAtEntityGoal extends Goal {
    public static final float DEFAULT_CHANCE = 0.02F;
    protected final MobEntity mob;
    @Nullable
    protected Entity target;
    protected final float range;
    private int lookTime;
    protected final float chance;
    private final boolean lookForward;
    protected final Class<? extends LivingEntity> targetType;
    protected final TargetPredicate targetPredicate;

    public NPCLookAtEntityGoal(MobEntity mob, Class<? extends LivingEntity> targetType, float range) {
        this(mob, targetType, range, 0.02F);
    }

    public NPCLookAtEntityGoal(MobEntity mob, Class<? extends LivingEntity> targetType, float range, float chance) {
        this(mob, targetType, range, chance, false);
    }

    public NPCLookAtEntityGoal(MobEntity mob, Class<? extends LivingEntity> targetType, float range, float chance, boolean lookForward) {
        this.mob = mob;
        this.targetType = targetType;
        this.range = range;
        this.chance = chance;
        this.lookForward = lookForward;
        this.setControls(EnumSet.of(Control.LOOK));
        if (targetType == PlayerEntity.class) {
            Predicate<Entity> predicate = EntityPredicates.rides(mob);
            this.targetPredicate = TargetPredicate.createNonAttackable().setBaseMaxDistance((double)range).setPredicate((entity, world) -> {
                return predicate.test(entity);
            });
        } else {
            this.targetPredicate = TargetPredicate.createNonAttackable().setBaseMaxDistance((double)range);
        }

    }

    public boolean canStart() {
        if (this.mob.getRandom().nextFloat() >= this.chance) {
            return false;
        } else {
            if (this.mob.getTarget() != null) {
                this.target = this.mob.getTarget();
            }

            ServerWorld serverWorld = getServerWorld(this.mob);
            if (this.targetType == PlayerEntity.class) {
                this.target = serverWorld.getClosestPlayer(this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
            } else {
                this.target = serverWorld.getClosestEntity(this.mob.getWorld().getEntitiesByClass(this.targetType, this.mob.getBoundingBox().expand((double)this.range, 3.0, (double)this.range), (livingEntity) -> {
                    return true;
                }), this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
            }

            return this.target != null;
        }
    }

    public boolean shouldContinue() {
        if (!this.target.isAlive()) {
            return false;
        } else if (this.mob.squaredDistanceTo(this.target) > (double)(this.range * this.range)) {
            return false;
        } else {
            return this.lookTime > 0;
        }
    }

    public void start() {
        this.lookTime = this.getTickCount(40 + this.mob.getRandom().nextInt(40));
    }

    public void stop() {
        this.target = null;
    }

    public void tick() {
        if (this.target.isAlive()) {
            double d = this.lookForward ? this.mob.getEyeY() : this.target.getEyeY();
            this.mob.getLookControl().lookAt(this.target.getX(), d, this.target.getZ());
            --this.lookTime;
            this.target.setBodyYaw(this.target.getYaw());
        }
    }
}
