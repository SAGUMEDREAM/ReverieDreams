package cc.thonly.reverie_dreams.entity;

import java.util.Set;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class KillerBeeEntity extends Bee {
    public KillerBeeEntity(EntityType<? extends Bee> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        Set<WrappedGoal> goals = this.goalSelector.getAvailableGoals();
        TemptGoal temptGoal = null;

        for (var prioritizedGoal: goals) {
            Goal goal = prioritizedGoal.getGoal();
            if (goal instanceof TemptGoal temptGoalTarget) {
                temptGoal = temptGoalTarget;
            }
        }

        if(temptGoal != null) {
            this.goalSelector.removeGoal(temptGoal);
        }

        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<Player>(this, Player.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<AbstractVillager>(this, AbstractVillager.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<IronGolem>(this, IronGolem.class, true));
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<Turtle>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));

    }

    @Override
    public void tick() {
        super.tick();
        this.setHasStung(false);
    }

}
