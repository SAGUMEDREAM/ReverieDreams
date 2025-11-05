package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.data.npc.NPCState;
import cc.thonly.reverie_dreams.registry.content.NPCStates;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

@Getter
public class NPCSheepShearGoal extends TargetGoal {
    private final BaseNPCLikeEntity maid;
    private final TargetingConditions targetPredicate = TargetingConditions.forCombat().range(16).selector((e, w) -> {
        return !e.hasCustomName();
    });
    @Nullable
    private Runnable task;
    private Sheep targetEntity;
    @Nullable
    private ItemStack itemStack;

    public NPCSheepShearGoal(BaseNPCLikeEntity maid) {
        super(maid, false);
        this.maid = maid;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!this.maid.isTame() || this.maid.isOrderedToSit()) {
            return false;
        }
        ItemStack stack = this.maid.getItemInHand(InteractionHand.MAIN_HAND);
        if (stack == null) {
            return false;
        }
        if (stack.isEmpty()) {
            return false;
        }
        NPCState state = this.maid.getNpcState();
        LivingEntity owner = this.maid.getOwner();
        if (owner == null || state != NPCStates.WORKING || this.maid.getWorkMode() != NPCWorkModes.SHEEP_SHEARING || !(stack.getItem() instanceof ShearsItem)) {
            return false;
        }
        BlockPos workPos = this.maid.getWorkingPos();
        ServerLevel serverWorld = getServerLevel(this.maid);

        List<Sheep> targets = this.mob.level().getEntitiesOfClass(Sheep.class, new AABB(workPos).inflate(16, 8, 16), (e) -> {
            boolean alive = e.isAlive();
            if (e.readyForShearing()) {
                return alive;
            }
            return false;
        });
        this.targetEntity = serverWorld.getNearestEntity(targets, this.targetPredicate, this.maid, this.maid.getX(), this.maid.getEyeY(), this.maid.getZ());
        return this.targetEntity != null;
    }

    @Override
    public void start() {
        this.task = () -> {
            if (this.itemStack == null) {
                return;
            }
            if (this.itemStack.isEmpty()) {
                return;
            }
            if (!(this.itemStack.getItem() instanceof ShearsItem)) {
                return;
            }
            ServerLevel world = getServerLevel(this.targetEntity);
            this.maid.swing(InteractionHand.MAIN_HAND);
            this.targetEntity.shear(world, SoundSource.PLAYERS, itemStack);
            this.targetEntity.gameEvent(GameEvent.SHEAR, this.targetEntity);
            this.itemStack.hurtAndBreak(1, this.targetEntity, InteractionHand.MAIN_HAND);
        };
    }

    @Override
    public void tick() {
        this.itemStack = this.maid.getItemInHand(InteractionHand.MAIN_HAND);
        if (this.task == null) {
            return;
        }
        if (this.targetEntity != null && this.targetEntity.isAlive()) {
            this.maid.getNavigation().moveTo(this.targetEntity, 1.0D);

            if (this.maid.distanceToSqr(this.targetEntity) <= 2.0D) {
                this.task.run();
                this.task = null;
                this.stop();
            }
        }
    }

}
