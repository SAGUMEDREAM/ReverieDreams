package cc.thonly.reverie_dreams.entity.ai.goal;

import java.util.List;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;

public class UniversalLivingAngerGoal<T extends LivingEntity>
        extends Goal {
    private static final int BOX_VERTICAL_EXPANSION = 10;
    private final T mob;
    private final boolean triggerOthers;
    private int lastAttackedTime;

    public UniversalLivingAngerGoal(T mob, boolean triggerOthers) {
        this.mob = mob;
        this.triggerOthers = triggerOthers;
    }

    @Override
    public boolean canUse() {
        return net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal.getServerLevel(this.mob).getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER) && this.canStartUniversalAnger();
    }

    private boolean canStartUniversalAnger() {
        return this.mob.getLastHurtByMob() != null && this.mob.getLastHurtByMob().getType() == EntityType.PLAYER && ((LivingEntity)this.mob).getLastHurtByMobTimestamp() > this.lastAttackedTime;
    }

    @Override
    public void start() {
        this.lastAttackedTime = this.mob.getLastHurtByMobTimestamp();
        ((NeutralMob)this.mob).forgetCurrentTargetAndRefreshUniversalAnger();
        if (this.triggerOthers) {
            this.getOthersInRange().stream().filter(entity -> entity != this.mob).map(entity -> (NeutralMob) entity).forEach(NeutralMob::forgetCurrentTargetAndRefreshUniversalAnger);
        }
        super.start();
    }

    private List<? extends LivingEntity> getOthersInRange() {
        double d = this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        AABB box = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(d, 10.0, d);
        return this.mob.level().getEntitiesOfClass(this.mob.getClass(), box, EntitySelector.NO_SPECTATORS);
    }
}

