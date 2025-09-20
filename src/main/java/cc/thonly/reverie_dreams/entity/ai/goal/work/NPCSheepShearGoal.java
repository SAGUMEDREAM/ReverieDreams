package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.entity.npc.NPCEntityImpl;
import cc.thonly.reverie_dreams.entity.npc.NPCState;
import cc.thonly.reverie_dreams.entity.npc.NPCStates;
import cc.thonly.reverie_dreams.entity.npc.NPCWorkModes;
import cc.thonly.reverie_dreams.interfaces.IAnimalEntity;
import lombok.Getter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

@Getter
public class NPCSheepShearGoal extends TrackTargetGoal {
    private final NPCEntityImpl maid;
    private final TargetPredicate targetPredicate = TargetPredicate.createAttackable().setBaseMaxDistance(16).setPredicate((e, w) -> {
        return !e.hasCustomName();
    });
    @Nullable
    private Runnable task;
    private SheepEntity targetEntity;
    @Nullable
    private ItemStack itemStack;

    public NPCSheepShearGoal(NPCEntityImpl maid) {
        super(maid, false);
        this.maid = maid;
        this.setControls(EnumSet.of(Control.TARGET));
    }

    @Override
    public boolean canStart() {
        if (!this.maid.isTamed() || this.maid.isSitting()) {
            return false;
        }
        ItemStack stack = this.maid.getStackInHand(Hand.MAIN_HAND);
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
        ServerWorld serverWorld = getServerWorld(this.maid);

        List<SheepEntity> targets = this.mob.getWorld().getEntitiesByClass(SheepEntity.class, new Box(workPos).expand(16, 8, 16), (e) -> {
            boolean alive = e.isAlive();
            if (e.isShearable()) {
                return alive;
            }
            return false;
        });
        this.targetEntity = serverWorld.getClosestEntity(targets, this.targetPredicate, this.maid, this.maid.getX(), this.maid.getEyeY(), this.maid.getZ());
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
            ServerWorld world = getServerWorld(this.targetEntity);
            this.targetEntity.sheared(world, SoundCategory.PLAYERS, itemStack);
            this.targetEntity.emitGameEvent(GameEvent.SHEAR, this.targetEntity);
            this.itemStack.damage(1, this.targetEntity, Hand.MAIN_HAND);
        };
    }

    @Override
    public void tick() {
        this.itemStack = this.maid.getStackInHand(Hand.MAIN_HAND);
        if (this.task == null) {
            return;
        }
        if (this.targetEntity != null && this.targetEntity.isAlive()) {
            this.maid.getNavigation().startMovingTo(this.targetEntity, 1.0D);

            if (this.maid.squaredDistanceTo(this.targetEntity) <= 2.0D) {
                this.task.run();
                this.task = null;
                this.stop();
            }
        }
    }

}
