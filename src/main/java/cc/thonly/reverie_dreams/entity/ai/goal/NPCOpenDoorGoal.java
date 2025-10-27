package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;

public class NPCOpenDoorGoal extends Goal {
    private final BaseNPCLikeEntity roleEntity;
    private BlockPos doorPos;
    private boolean hasOpened;

    public NPCOpenDoorGoal(BaseNPCLikeEntity roleEntity) {
        this.roleEntity = roleEntity;
        this.setControls(EnumSet.of(Control.LOOK));
    }

    @Override
    public boolean canStart() {
        BlockPos front = this.roleEntity.getBlockPos().offset(this.roleEntity.getHorizontalFacing());
        BlockState state = this.roleEntity.getWorld().getBlockState(front);

        if (state.getBlock() instanceof DoorBlock door && !state.get(DoorBlock.OPEN)) {
            this.doorPos = front;
            return true;
        }

        return false;
    }

    @Override
    public boolean shouldContinue() {
        if (this.doorPos == null || !this.hasOpened) return false;
        return roleEntity.squaredDistanceTo(this.doorPos.getX() + 0.5, this.doorPos.getY(), this.doorPos.getZ() + 0.5) <= 4.0;
    }

    @Override
    public void start() {
        if (this.doorPos == null) return;

        BlockState state = this.roleEntity.getWorld().getBlockState(this.doorPos);
        if (state.getBlock() instanceof DoorBlock doorBlock && !state.get(DoorBlock.OPEN)) {
            this.roleEntity.getWorld().setBlockState(
                    this.doorPos,
                    state.with(DoorBlock.OPEN, true),
                    10
            );
            BlockSetType blockSetType = doorBlock.getBlockSetType();
            this.roleEntity.playSound(blockSetType.doorOpen());
            this.hasOpened = true;
        }
    }

    @Override
    public void tick() {
        if (this.doorPos == null || !this.hasOpened) return;

        if (this.roleEntity.squaredDistanceTo(this.doorPos.getX() + 0.5, this.doorPos.getY(), this.doorPos.getZ() + 0.5) > 4.0) {
            BlockState state = this.roleEntity.getWorld().getBlockState(this.doorPos);
            if (state.getBlock() instanceof DoorBlock doorBlock && state.get(DoorBlock.OPEN)) {
                this.roleEntity.getWorld().setBlockState(
                        this.doorPos,
                        state.with(DoorBlock.OPEN, false),
                        10
                );
                BlockSetType blockSetType = doorBlock.getBlockSetType();
                this.roleEntity.playSound(blockSetType.doorClose());
            }
            this.doorPos = null;
            this.hasOpened = false;
        }
    }

    @Override
    public void stop() {
        if (this.doorPos != null && this.hasOpened) {
            // 停止时如果门还开着就关门
            BlockState state = this.roleEntity.getWorld().getBlockState(this.doorPos);
            Boolean isOpen = state.get(DoorBlock.OPEN);
            if (state.getBlock() instanceof DoorBlock doorBlock && isOpen) {
                this.roleEntity.getWorld().setBlockState(
                        this.doorPos,
                        state.with(DoorBlock.OPEN, false),
                        10
                );
                BlockSetType blockSetType = doorBlock.getBlockSetType();
                this.roleEntity.playSound(blockSetType.doorClose());
            }
        }
        this.doorPos = null;
        this.hasOpened = false;
    }
}
