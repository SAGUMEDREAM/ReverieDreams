package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.reverie_dreams.block.entity.CustomChestBlockEntity;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.inventory.NPCInventoryImpl;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import cc.thonly.reverie_dreams.entity.ai.goal.util.EntityTargetUtil;
import cc.thonly.reverie_dreams.entity.ai.goal.work.NPCWorkLogisticsGoal;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.restaurant.RestaurantService;

@SuppressWarnings("resource")
public class NPCOpenSilverChestGoal extends Goal {
    private final BaseNPCLikeEntity npc;
    @Nullable
    private OperationalTarget operationalTarget;
    private int tick = 0;

    public NPCOpenSilverChestGoal(BaseNPCLikeEntity npc) {
        this.npc = npc;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @SuppressWarnings("deprecation")
    private List<BlockPos> findSilverChestBlockPosList() {
        List<BlockPos> blockPosList = new LinkedList<>();
        Level world = this.npc.level();
        BlockPos center = this.npc.getWorkingPos();
        int r = 8;

        BoundingBox box = new BoundingBox(
                center.getX() - r,
                center.getY() - r,
                center.getZ() - r,
                center.getX() + r,
                center.getY() + r,
                center.getZ() + r
        );
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int y = box.minY(); y <= box.maxY(); y++) {
                for (int z = box.minZ(); z <= box.maxZ(); z++) {
                    pos.set(x, y, z);
                    if (!(world.getBlockState(pos).getBlock().builtInRegistryHolder().is(RDBlocks.SILVER_CHEST_BLOCK.getChestBlock()))) {
                        continue;
                    }
                    if (world.getBlockEntity(pos) instanceof CustomChestBlockEntity customChestBlockEntity) {
                        blockPosList.add(pos.immutable());
                    }
                }
            }
        }
        return blockPosList;
    }

    @Override
    public boolean canUse() {
        if (this.npc.getWorkController().working() && NPCWorkLogisticsGoal.production(this.npc.getWorkMode()))
            return false;
        if (this.usesSilverChests())
            return false;
        if (EntityTargetUtil.isThisWorkMode(this.npc,
                NPCWorkModes.CHEST_CLASSIFICATION))
            return false;
        if (!this.npc.isTame()) {
            return false;
        }
        if (this.operationalTarget != null) {
            return true;
        }

        return this.trySetTarget();
    }

    private boolean trySetTarget() {
        Level world = this.npc.level();
        List<BlockPos> silverChestBlockPosList = this.findSilverChestBlockPosList();
        for (BlockPos blockPos : silverChestBlockPosList) {
            BlockEntity blockEntity = world.getBlockEntity(blockPos);
            if (!(blockEntity instanceof CustomChestBlockEntity customChestBlockEntity)) {
                continue;
            }
            if (customChestBlockEntity.isEmpty()) {
                continue;
            }
            for (int i = 0; i < customChestBlockEntity.getInventory().getContainerSize(); i++) {
                ItemStack itemStack = customChestBlockEntity.getItem(i);
                if (itemStack.isEmpty()) {
                    continue;
                }
                NPCInventoryImpl inventory = this.npc.getInventory();
                if (!inventory.canAddItem(itemStack)) {
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
                this.npc.getNavigation().moveTo(
                        blockPos.getX() + 0.5,
                        blockPos.getY() + 0.5,
                        blockPos.getZ() + 0.5,
                        1.0D
                );
            } else {
                CustomChestBlockEntity customChestBlockEntity = this.operationalTarget.customChestBlock;
                ItemStack itemStack = this.operationalTarget.itemStack;
                int slotIndex = this.operationalTarget.index;

                NPCInventoryImpl inventory = this.npc.getInventory();

                int inserted = inventory.insertStack(itemStack);
                if (inserted > 0) {
                    // insertStack 内部已经执行 shrink；这里再 shrink 会删除物品。
                    customChestBlockEntity.setChanged();
                    this.npc.makeSound(SoundEvents.CHEST_OPEN);
                    this.npc.swing(InteractionHand.MAIN_HAND);
                    double x = blockPos.getX() + 0.5;
                    double y = blockPos.getY() + 0.5;
                    double z = blockPos.getZ() + 0.5;
                    this.npc.getLookControl().setLookAt(x, y, z);
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
        double distanceSq = blockPos.distToCenterSqr(this.npc.position());
        return distanceSq <= 9; // 半径 3 格
    }

    @Override
    public boolean canContinueToUse() {
        if (this.npc.getWorkController().working() && NPCWorkLogisticsGoal.production(this.npc.getWorkMode()))
            return false;
        if (this.usesSilverChests())
            return false;
        if (EntityTargetUtil.isThisWorkMode(this.npc,
                NPCWorkModes.CHEST_CLASSIFICATION))
            return false;
        return this.operationalTarget != null;
    }

    /**
     * Chefs and waiters retrieve specific silver chest items themselves.
     */
    private boolean usesSilverChests() {
        return RestaurantService.isVisiting(this.npc)
                || EntityTargetUtil.isThisWorkMode(this.npc, NPCWorkModes.COOKING)
                || EntityTargetUtil.isThisWorkMode(this.npc, NPCWorkModes.SERVING)
                || EntityTargetUtil.isThisWorkMode(this.npc, NPCWorkModes.CUSTOMER);
    }

    @Override
    public void stop() {
        this.operationalTarget = null;
        this.tick = 0;
        this.npc.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public record OperationalTarget(int index,
                                    ItemStack itemStack,
                                    BlockPos blockPos,
                                    CustomChestBlockEntity customChestBlock) {
    }
}
