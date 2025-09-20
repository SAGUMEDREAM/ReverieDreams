package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.block.entity.CustomChestBlockEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCEntityImpl;
import cc.thonly.reverie_dreams.inventory.NPCInventoryImpl;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

public class NPCOpenSilverChestGoal extends Goal {
    private final NPCEntityImpl roleEntity;
    @Nullable
    private OperationalTarget operationalTarget;
    private int tick = 0;

    public NPCOpenSilverChestGoal(NPCEntityImpl roleEntity) {
        this.roleEntity = roleEntity;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    private List<BlockPos> findSilverChestBlockPosList() {
        List<BlockPos> blockPosList = new LinkedList<>();
        World world = this.roleEntity.getWorld();
        BlockPos center = this.roleEntity.getWorkingPos();
        int r = 8;

        BlockBox box = new BlockBox(
                center.getX() - r,
                center.getY() - r,
                center.getZ() - r,
                center.getX() + r,
                center.getY() + r,
                center.getZ() + r
        );
        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int x = box.getMinX(); x <= box.getMaxX(); x++) {
            for (int y = box.getMinY(); y <= box.getMaxY(); y++) {
                for (int z = box.getMinZ(); z <= box.getMaxZ(); z++) {
                    pos.set(x, y, z);
                    if (!(world.getBlockState(pos).getBlock() == ModBlocks.SILVER_CHEST_BLOCK.getChestBlock())) {
                        continue;
                    }
                    if (world.getBlockEntity(pos) instanceof CustomChestBlockEntity customChestBlockEntity) {
                        blockPosList.add(pos.toImmutable());
                    }
                }
            }
        }
        return blockPosList;
    }

    @Override
    public boolean canStart() {
        if (!this.roleEntity.isTamed()) {
            return false;
        }
        if (this.operationalTarget != null) {
            return true;
        }

        return this.trySetTarget();
    }

    private boolean trySetTarget() {
        World world = this.roleEntity.getWorld();
        List<BlockPos> silverChestBlockPosList = this.findSilverChestBlockPosList();
        for (BlockPos blockPos : silverChestBlockPosList) {
            BlockEntity blockEntity = world.getBlockEntity(blockPos);
            if (!(blockEntity instanceof CustomChestBlockEntity customChestBlockEntity)) {
                continue;
            }
            if (customChestBlockEntity.isEmpty()) {
                continue;
            }
            for (int i = 0; i < customChestBlockEntity.getInventory().size(); i++) {
                ItemStack itemStack = customChestBlockEntity.getStack(i);
                if (itemStack.isEmpty()) {
                    continue;
                }
                NPCInventoryImpl inventory = this.roleEntity.getInventory();
                if (!inventory.canInsert(itemStack)) {
                    continue;
                }
                this.operationalTarget = new OperationalTarget(i, itemStack, blockPos, customChestBlockEntity);
                return true;
            }
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tick < 20 * 1.5) {
            this.tick++;
            return;
        }
        this.tick = 0;
        if (this.operationalTarget != null) {
            BlockPos blockPos = this.operationalTarget.blockPos;
            if (!this.isReached(blockPos)) {
                this.roleEntity.getNavigation().startMovingTo(
                        blockPos.getX() + 0.5,
                        blockPos.getY() + 0.5,
                        blockPos.getZ() + 0.5,
                        1.0D
                );
            } else {
                CustomChestBlockEntity customChestBlockEntity = this.operationalTarget.customChestBlock;
                ItemStack itemStack = this.operationalTarget.itemStack;
                int slotIndex = this.operationalTarget.index;

                NPCInventoryImpl inventory = this.roleEntity.getInventory();

                int inserted = inventory.insertStack(itemStack);
                if (inserted > 0) {
                    itemStack.decrement(inserted);
                    customChestBlockEntity.markDirty();
                    this.roleEntity.playSound(SoundEvents.BLOCK_CHEST_OPEN);
                    this.roleEntity.swingHand(Hand.MAIN_HAND);
                    double x = blockPos.getX() + 0.5;
                    double y = blockPos.getY() + 0.5;
                    double z = blockPos.getZ() + 0.5;
                    this.roleEntity.getLookControl().lookAt(x, y, z);
                }

                if (itemStack.isEmpty()) {
                    this.operationalTarget = null;
                }
            }
        } else {
            this.trySetTarget();
        }
    }

    private boolean isReached(BlockPos blockPos) {
        double distanceSq = blockPos.getSquaredDistance(this.roleEntity.getPos());
        return distanceSq <= 9; // 半径 3 格
    }

    @Override
    public boolean shouldContinue() {
        return this.operationalTarget != null;
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    public record OperationalTarget(int index,
                                    ItemStack itemStack,
                                    BlockPos blockPos,
                                    CustomChestBlockEntity customChestBlock) {
    }
}
