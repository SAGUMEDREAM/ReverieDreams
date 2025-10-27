package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCState;
import cc.thonly.reverie_dreams.entity.npc.NPCStates;
import cc.thonly.reverie_dreams.entity.npc.NPCWorkModes;
import cc.thonly.reverie_dreams.interfaces.IAnimalEntity;
import lombok.Getter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

@Getter
public class NPCBreedGoal extends TrackTargetGoal {
    private final BaseNPCLikeEntity maid;
    private final TargetPredicate targetPredicate = TargetPredicate.createAttackable().setBaseMaxDistance(16).setPredicate((e, w) -> {
        return !e.hasCustomName();
    });
    @Nullable
    private Runnable task;
    private AnimalEntity targetEntity;

    public NPCBreedGoal(BaseNPCLikeEntity maid) {
        super(maid, false);
        this.maid = maid;
        this.setControls(EnumSet.of(Goal.Control.TARGET));
    }

    @Override
    public boolean canStart() {
        if (!this.maid.isTamed() || this.maid.isSitting()) {
            return false;
        }
        NPCState state = maid.getNpcState();
        LivingEntity owner = this.maid.getOwner();
        if (owner == null || state != NPCStates.WORKING || maid.getWorkMode() != NPCWorkModes.BREED) {
            return false;
        }
        BlockPos workPos = maid.getWorkingPos();
        ServerWorld serverWorld = getServerWorld(maid);

        List<AnimalEntity> targets = this.mob.getWorld().getEntitiesByClass(AnimalEntity.class, new Box(workPos).expand(16, 8, 16), (e) -> {
            boolean alive = e.isAlive();
            boolean hasItem = e.isBreedingItem(this.maid.getStackInHand(Hand.MAIN_HAND));
            int i = e.getBreedingAge();
            if (i == 0 && e.canEat() && !e.isBaby()) {
                return alive && hasItem;
            }
            return false;
        });
        this.targetEntity = serverWorld.getClosestEntity(targets, this.targetPredicate, this.maid, this.maid.getX(), this.maid.getEyeY(), this.maid.getZ());
        return this.targetEntity != null;
    }

    @Override
    public void start() {
        this.task = ()-> {
            ((IAnimalEntity)this.targetEntity).eatStackFood(this.maid, Hand.MAIN_HAND, this.maid.getStackInHand(Hand.MAIN_HAND));
            this.targetEntity.playEatSound();
            ((IAnimalEntity)this.targetEntity).loveEntity(this.maid);
        };
    }

    @Override
    public void tick() {
        if (this.task == null) {
            return;
        }
        if (this.targetEntity != null && this.targetEntity.isAlive()) {
            this.maid.getNavigation().startMovingTo(this.targetEntity, 1.0D);

            if (this.maid.squaredDistanceTo(this.targetEntity) <= 4.0D) {
                this.task.run();
                this.task = null;
                this.stop();
            }
        }
    }

}
