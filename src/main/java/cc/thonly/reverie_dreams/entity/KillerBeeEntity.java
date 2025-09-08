package cc.thonly.reverie_dreams.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

import java.util.Set;

public class KillerBeeEntity extends BeeEntity {
    public KillerBeeEntity(EntityType<? extends BeeEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        Set<PrioritizedGoal> goals = this.goalSelector.getGoals();
        TemptGoal temptGoal = null;

        for (var prioritizedGoal: goals) {
            Goal goal = prioritizedGoal.getGoal();
            if (goal instanceof TemptGoal temptGoalTarget) {
                temptGoal = temptGoalTarget;
            }
        }

        if(temptGoal != null) {
            this.goalSelector.remove(temptGoal);
        }

        this.targetSelector.add(4, new ActiveTargetGoal<PlayerEntity>(this, PlayerEntity.class, true));
        this.targetSelector.add(5, new ActiveTargetGoal<MerchantEntity>(this, MerchantEntity.class, true));
        this.targetSelector.add(5, new ActiveTargetGoal<IronGolemEntity>(this, IronGolemEntity.class, true));
        this.targetSelector.add(6, new ActiveTargetGoal<TurtleEntity>(this, TurtleEntity.class, 10, true, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));

    }

    @Override
    public void tick() {
        super.tick();
        this.setHasStung(false);
    }

}
