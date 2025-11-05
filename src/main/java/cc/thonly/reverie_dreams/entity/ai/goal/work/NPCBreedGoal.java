package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.data.npc.NPCState;
import cc.thonly.reverie_dreams.registry.content.NPCStates;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.interfaces.IAnimalEntity;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

@Getter
public class NPCBreedGoal extends TargetGoal {
    private final BaseNPCLikeEntity maid;
    private final TargetingConditions targetPredicate = TargetingConditions.forCombat().range(16).selector((e, w) -> {
        return !e.hasCustomName();
    });
    @Nullable
    private Runnable task;
    private Animal targetEntity;

    public NPCBreedGoal(BaseNPCLikeEntity maid) {
        super(maid, false);
        this.maid = maid;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!this.maid.isTame() || this.maid.isOrderedToSit()) {
            return false;
        }
        NPCState state = maid.getNpcState();
        LivingEntity owner = this.maid.getOwner();
        if (owner == null || state != NPCStates.WORKING || maid.getWorkMode() != NPCWorkModes.BREED) {
            return false;
        }
        BlockPos workPos = maid.getWorkingPos();
        ServerLevel serverWorld = getServerLevel(maid);

        List<Animal> targets = this.mob.level().getEntitiesOfClass(Animal.class, new AABB(workPos).inflate(16, 8, 16), (e) -> {
            boolean alive = e.isAlive();
            boolean hasItem = e.isFood(this.maid.getItemInHand(InteractionHand.MAIN_HAND));
            int i = e.getAge();
            if (i == 0 && e.canFallInLove() && !e.isBaby()) {
                return alive && hasItem;
            }
            return false;
        });
        this.targetEntity = serverWorld.getNearestEntity(targets, this.targetPredicate, this.maid, this.maid.getX(), this.maid.getEyeY(), this.maid.getZ());
        return this.targetEntity != null;
    }

    @Override
    public void start() {
        this.task = ()-> {
            ((IAnimalEntity)this.targetEntity).eatStackFood(this.maid, InteractionHand.MAIN_HAND, this.maid.getItemInHand(InteractionHand.MAIN_HAND));
            this.targetEntity.playEatingSound();
            ((IAnimalEntity)this.targetEntity).loveEntity(this.maid);
        };
    }

    @Override
    public void tick() {
        if (this.task == null) {
            return;
        }
        if (this.targetEntity != null && this.targetEntity.isAlive()) {
            this.maid.getNavigation().moveTo(this.targetEntity, 1.0D);

            if (this.maid.distanceToSqr(this.targetEntity) <= 4.0D) {
                this.task.run();
                this.task = null;
                this.stop();
            }
        }
    }

}
