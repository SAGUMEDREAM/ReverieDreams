package cc.thonly.reverie_dreams.entity.ai.goal;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class DifferentRevengeGoal extends TargetGoal {
    private static final TargetingConditions VALID_AVOIDABLES_PREDICATE = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
    private static final int BOX_VERTICAL_EXPANSION = 10;
    private boolean groupRevenge;
    private int lastAttackedTime;
    private final Class<?>[] noRevengeTypes;
    @Nullable
    private Class<?>[] noHelpTypes;

    public DifferentRevengeGoal(PathfinderMob mob, Class<?> ... noRevengeTypes) {
        super(mob, true);
        this.noRevengeTypes = noRevengeTypes;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        int i = this.mob.getLastHurtByMobTimestamp();
        LivingEntity livingEntity = this.mob.getLastHurtByMob();
        if (i == this.lastAttackedTime || livingEntity == null) {
            return false;
        }
        if (livingEntity.getType() == EntityType.PLAYER && HurtByTargetGoal.getServerLevel(this.mob).getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
            return false;
        }
        if (livingEntity.getType() == this.mob.getType()) {
            return false;
        }
        for (Class<?> class_ : this.noRevengeTypes) {
            if (!class_.isAssignableFrom(livingEntity.getClass())) continue;
            return false;
        }
        return this.canAttack(livingEntity, VALID_AVOIDABLES_PREDICATE);
    }

    public DifferentRevengeGoal setGroupRevenge(Class<?> ... noHelpTypes) {
        this.groupRevenge = true;
        this.noHelpTypes = noHelpTypes;
        return this;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.mob.getLastHurtByMob());
        this.targetMob = this.mob.getTarget();
        this.lastAttackedTime = this.mob.getLastHurtByMobTimestamp();
        this.unseenMemoryTicks = 300;
        if (this.groupRevenge) {
            this.callSameTypeForRevenge();
        }
        super.start();
    }

    protected void callSameTypeForRevenge() {
        double d = this.getFollowDistance();
        AABB box = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(d, 10.0, d);
        List<? extends Mob> list = this.mob.level().getEntitiesOfClass(this.mob.getClass(), box, EntitySelector.NO_SPECTATORS);
        for (Mob mobEntity : list) {
            if (this.mob == mobEntity || mobEntity.getTarget() != null || this.mob instanceof TamableAnimal && ((TamableAnimal)this.mob).getOwner() != ((TamableAnimal)mobEntity).getOwner() || mobEntity.isAlliedTo(this.mob.getLastHurtByMob())) continue;
            if (this.noHelpTypes != null) {
                boolean bl = false;
                for (Class<?> class_ : this.noHelpTypes) {
                    if (mobEntity.getClass() != class_) continue;
                    bl = true;
                    break;
                }
                if (bl) continue;
            }
            this.setMobEntityTarget(mobEntity, this.mob.getLastHurtByMob());
        }
    }

    protected void setMobEntityTarget(Mob mob, LivingEntity target) {
        mob.setTarget(target);
    }
}

