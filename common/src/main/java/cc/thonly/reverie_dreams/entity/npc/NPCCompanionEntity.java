package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.api.entity.ExperienceOrbEntityDataModifier;
import cc.thonly.reverie_dreams.api.entity.callback.CompatGoalAddedCallback;
import cc.thonly.reverie_dreams.entity.ai.goal.NPCTemptGoal;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

import java.util.List;

@SuppressWarnings("resource")
public class NPCCompanionEntity extends NPCSimpleEntity {

    public NPCCompanionEntity(EntityType<? extends NPCSimpleEntity> entityType, Level world) {
        super(entityType, world);
    }


    @Override
    protected void registerGoals() {
        super.registerGoals();

    }

    @Override
    public void tick() {
        this.attractNearbyExperienceOrbs();
        super.tick();
    }

    public void attractNearbyExperienceOrbs() {
        if (this.level().isClientSide())
            return;

        double radius = 7.0;
        List<ExperienceOrb> orbs = this.level().getEntitiesOfClass(
                ExperienceOrb.class,
                this.getBoundingBox().inflate(radius),
                Entity::isAlive
        );

        for (ExperienceOrb orb : orbs) {
            ((ExperienceOrbEntityDataModifier) (Object) orb).reverie_dreams$setNPCTarget(this);
        }
    }

    @Override
    public boolean isEnableTamableFeature() {
        return true;
    }
}
