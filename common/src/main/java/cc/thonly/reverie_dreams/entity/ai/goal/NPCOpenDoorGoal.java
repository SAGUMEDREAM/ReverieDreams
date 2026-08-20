package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;

import java.util.EnumSet;

@SuppressWarnings("resource")
public class NPCOpenDoorGoal extends Goal {
    private final BaseNPCLikeEntity npc;
    private BlockPos doorPos;
    private boolean hasOpened;

    public NPCOpenDoorGoal(BaseNPCLikeEntity npc) {
        this.npc = npc;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        BlockPos front = this.npc.blockPosition().relative(this.npc.getDirection());
        BlockState state = this.npc.level().getBlockState(front);

        if (state.getBlock() instanceof DoorBlock door && !state.getValue(DoorBlock.OPEN)) {
            this.doorPos = front;
            return true;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.doorPos == null || !this.hasOpened) return false;
        return npc.distanceToSqr(this.doorPos.getX() + 0.5, this.doorPos.getY(), this.doorPos.getZ() + 0.5) <= 4.0;
    }

    @Override
    public void start() {
        if (this.doorPos == null) return;

        BlockState state = this.npc.level().getBlockState(this.doorPos);
        if (state.getBlock() instanceof DoorBlock doorBlock && !state.getValue(DoorBlock.OPEN)) {
            this.npc.level().setBlock(
                    this.doorPos,
                    state.setValue(DoorBlock.OPEN, true),
                    10
            );
            BlockSetType blockSetType = doorBlock.type();
            this.npc.makeSound(blockSetType.doorOpen());
            this.hasOpened = true;
        }
    }

    @Override
    public void tick() {
        if (this.doorPos == null || !this.hasOpened) return;

        if (this.npc.distanceToSqr(this.doorPos.getX() + 0.5, this.doorPos.getY(), this.doorPos.getZ() + 0.5) > 4.0) {
            BlockState state = this.npc.level().getBlockState(this.doorPos);
            if (state.getBlock() instanceof DoorBlock doorBlock && state.getValue(DoorBlock.OPEN)) {
                this.npc.level().setBlock(
                        this.doorPos,
                        state.setValue(DoorBlock.OPEN, false),
                        10
                );
                BlockSetType blockSetType = doorBlock.type();
                this.npc.makeSound(blockSetType.doorClose());
            }
            this.doorPos = null;
            this.hasOpened = false;
        }
    }

    @Override
    public void stop() {
        if (this.doorPos != null && this.hasOpened) {
            // 停止时如果门还开着就关门
            BlockState state = this.npc.level().getBlockState(this.doorPos);
            Boolean isOpen = state.getValue(DoorBlock.OPEN);
            if (state.getBlock() instanceof DoorBlock doorBlock && isOpen) {
                this.npc.level().setBlock(
                        this.doorPos,
                        state.setValue(DoorBlock.OPEN, false),
                        10
                );
                BlockSetType blockSetType = doorBlock.type();
                this.npc.makeSound(blockSetType.doorClose());
            }
        }
        this.doorPos = null;
        this.hasOpened = false;
    }
}
