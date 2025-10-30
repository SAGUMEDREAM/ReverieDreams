package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.entity.ai.goal.util.EntityTargetUtil;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCWorkModes;
import cc.thonly.reverie_dreams.inventory.NPCInventoryImpl;
import cc.thonly.reverie_dreams.server.ItemTagManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

public class NPCChestClassificationGoal extends Goal {

    private final BaseNPCLikeEntity roleEntity;
    @Nullable
    private OperationalTarget currentTarget = null;

    public NPCChestClassificationGoal(BaseNPCLikeEntity roleEntity) {
        this.roleEntity = roleEntity;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private List<BlockPos> findChestBlockPosList() {
        List<BlockPos> list = new LinkedList<>();
        Level world = this.roleEntity.level();
        BlockPos center = this.roleEntity.getWorkingPos();
        int r = 8;
        BoundingBox box = new BoundingBox(center.getX() - r, center.getY() - r, center.getZ() - r,
                center.getX() + r, center.getY() + r, center.getZ() + r);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int y = box.minY(); y <= box.maxY(); y++) {
                for (int z = box.minZ(); z <= box.maxZ(); z++) {
                    pos.set(x, y, z);
                    if (world.getBlockState(pos).getBlock() == Blocks.CHEST &&
                            world.getBlockEntity(pos) instanceof ChestBlockEntity) {
                        list.add(pos.immutable());
                    }
                }
            }
        }

        // 按照距离工作点排序，最近的排在前面
        list.sort((a, b) -> Double.compare(
                a.distSqr(center),
                b.distSqr(center)
        ));

//        System.out.println("[NPCChestClassificationGoal] Found chests (sorted): " + list.size());
        if (!list.isEmpty()) {
//            System.out.println("[NPCChestClassificationGoal] Nearest chest at: " + list.get(0));
        }
        return list;
    }


    @Override
    public boolean canUse() {
        boolean can = EntityTargetUtil.isThisWorkMode(this.roleEntity, NPCWorkModes.CHEST_CLASSIFICATION);
//        System.out.println("[canStart] Work mode check: " + can);
        MinecraftServer server = this.roleEntity.getServer();
        if (server != null && ItemTagManager.getInstance().isEmpty()) {
            ItemTagManager.getInstance().load(server);
//            System.out.println("[canStart] ItemTagManager loaded");
        }
        return can;
    }

    @Override
    public boolean canContinueToUse() {
        boolean cont = EntityTargetUtil.isThisWorkMode(this.roleEntity, NPCWorkModes.CHEST_CLASSIFICATION);
        // 少量日志，别太频繁打印（可按需打开）
        // System.out.println("[shouldContinue] " + cont);
        return cont;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /**
     * 找到下一个可以放置至少 1 个物品的目标：
     *  返回（inventorySlot, chestPos, chestEntity, slotIndex）
     *  要求： chestSlot 是空的 OR chestSlot 与 heldItem 相同且有剩余空间 (>0)
     */
    private OperationalTarget findNextTarget(NPCInventoryImpl inventory) {
        List<BlockPos> chests = findChestBlockPosList();

        for (int invIndex = 0; invIndex < inventory.getContainerSize(); invIndex++) {
            ItemStack held = inventory.getItem(invIndex);
            if (held.isEmpty()) continue;

//            System.out.println("[findNextTarget] trying held slot " + invIndex + ": " + held.getCount() + "x " + held.getItem());

            for (BlockPos chestPos : chests) {
                BlockEntity be = this.roleEntity.level().getBlockEntity(chestPos);
                if (!(be instanceof ChestBlockEntity chest)) continue;

                boolean chestEmpty = true;
                boolean chestCompatible = true;

                // 扫一遍箱子，看是否已有不同物品
                for (int s = 0; s < chest.getContainerSize(); s++) {
                    ItemStack cs = chest.getItem(s);
                    if (!cs.isEmpty()) {
                        chestEmpty = false;
                        if (!ItemStack.isSameItemSameComponents(cs, held)) {
                            chestCompatible = false; // 已有不同物品，不兼容
                            break;
                        }
                    }
                }

                if (!chestCompatible) continue; // 跳过这个箱子

                // 到这里说明箱子是空的，或全是同类物品
                for (int slot = 0; slot < chest.getContainerSize(); slot++) {
                    ItemStack chestStack = chest.getItem(slot);
                    if (chestStack.isEmpty()) {
//                        System.out.println("[findNextTarget] choose empty slot " + slot + " in chest " + chestPos);
                        return new OperationalTarget(invIndex, chestPos, chest, slot);
                    }
                    if (ItemStack.isSameItemSameComponents(chestStack, held)) {
                        int space = chestStack.getMaxStackSize() - chestStack.getCount();
                        if (space > 0) {
//                            System.out.println("[findNextTarget] choose partial slot " + slot + " in chest " + chestPos + ", space=" + space);
                            return new OperationalTarget(invIndex, chestPos, chest, slot);
                        }
                    }
                }
            }
        }

//        System.out.println("[findNextTarget] no suitable target found");
        return null;
    }

    @Override
    public void tick() {
//        System.out.println("=== Tick start ===");

        NPCInventoryImpl inventory = this.roleEntity.getInventory();
//        System.out.println("[tick] Inventory items:");
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
//                System.out.println("  Slot " + i + ": " + stack.getCount() + "x " + stack.getItem().toString());
            }
        }

        // 如果没有当前目标或持有物为空 -> 重新选择
        if (this.currentTarget == null) {
            this.currentTarget = findNextTarget(inventory);
            if (this.currentTarget == null) {
//                System.out.println("[tick] No target found -> nothing to do");
//                System.out.println("=== Tick end ===");
                return;
            } else {
//                System.out.println("[tick] New target: invSlot=" + currentTarget.inventorySlot
//                        + " item=" + inventory.getStack(currentTarget.inventorySlot).getItem()
//                        + " count=" + inventory.getStack(currentTarget.inventorySlot).getCount()
//                        + " chest=" + currentTarget.blockPos
//                        + " slot=" + currentTarget.slotIndex);
            }
        }

        // 再次校验：目标对应的背包槽是否还有物品（物品可能在别处被用掉）
        ItemStack held = inventory.getItem(this.currentTarget.inventorySlot);
        if (held.isEmpty()) {
//            System.out.println("[tick] Target's inventory slot is empty, clearing target");
            this.currentTarget = null;
//            System.out.println("=== Tick end ===");
            return;
        }

        // 移动到目标箱子
        if (!isReached(this.currentTarget.blockPos)) {
//            System.out.println("[tick] Moving to chest at " + currentTarget.blockPos);
            this.roleEntity.getNavigation().moveTo(
                    this.currentTarget.blockPos.getX() + 0.5,
                    this.currentTarget.blockPos.getY() + 0.5,
                    this.currentTarget.blockPos.getZ() + 0.5,
                    1.0D
            );
//            System.out.println("=== Tick end ===");
            return;
        } else {
//            System.out.println("[tick] Reached chest at " + currentTarget.blockPos);
        }

        // 在到达箱子且仍有持物时，尝试放入 —— 如果当前 slot 不可用，先在同箱体内找下一个可用槽
        ChestBlockEntity chest = this.currentTarget.chest;
        int slotIndex = this.currentTarget.slotIndex;

        // 如果当前 slot 不可用（不同物、或相同但无空间），尝试同箱内下一个可用槽
        ItemStack chestStack = chest.getItem(slotIndex);
        boolean slotSuitable = false;
        if (chestStack.isEmpty()) {
            slotSuitable = true;
        } else if (ItemStack.isSameItemSameComponents(chestStack, held) && chestStack.getCount() < chestStack.getMaxStackSize()) {
            slotSuitable = true;
        }

        if (!slotSuitable) {
//            System.out.println("[tick] Current slot " + slotIndex + " not suitable (item=" + (chestStack.isEmpty() ? "empty" : chestStack.getItem())
//                    + ", count=" + (chestStack.isEmpty() ? 0 : chestStack.getCount()) + "), searching next slot in same chest");
            Integer nextSlot = findNextSlotInSameChest(chest, held);
            if (nextSlot != null) {
//                System.out.println("[tick] Found next workable slot " + nextSlot + " in same chest");
                this.currentTarget = new OperationalTarget(this.currentTarget.inventorySlot, this.currentTarget.blockPos, chest, nextSlot);
                slotIndex = nextSlot;
                chestStack = chest.getItem(slotIndex);
            } else {
//                System.out.println("[tick] No workable slot in this chest, clearing current target to find another chest");
                this.currentTarget = null;
//                System.out.println("=== Tick end ===");
                return;
            }
        }

        // 到这里， chestStack 是当前 slot 的最新引用（可能为空或有空间）
        chestStack = chest.getItem(slotIndex);
        held = inventory.getItem(this.currentTarget.inventorySlot); // refresh
        if (held.isEmpty()) {
//            System.out.println("[tick] Held became empty before placing, clearing target");
            this.currentTarget = null;
//            System.out.println("=== Tick end ===");
            return;
        }

        // 放入逻辑：计算实际放入数量 move
        if (chestStack.isEmpty()) {
            int move = Math.min(held.getCount(), held.getMaxStackSize()); // 放一堆或全部（视需求可改为只放1）
            ItemStack toSet = held.copy();
            toSet.setCount(move);
            chest.setItem(slotIndex, toSet);
            held.shrink(move);
//            System.out.println("[tick] Placed " + move + "x " + toSet.getItem() + " into empty slot " + slotIndex);
        } else { // same item and has space guaranteed
            int space = chestStack.getMaxStackSize() - chestStack.getCount();
            int move = Math.min(space, held.getCount());
            if (move > 0) {
                chestStack.grow(move);
                held.shrink(move);
//                System.out.println("[tick] Added " + move + "x " + chestStack.getItem() + " to slot " + slotIndex + " (now " + chestStack.getCount() + ")");
            } else {
                // 理论不应到这里（我们以前检查过有空间），但保险处理
//                System.out.println("[tick] Unexpected: computed move == 0, clearing target");
                currentTarget = null;
//                System.out.println("=== Tick end ===");
                return;
            }
        }

        chest.setChanged();
        this.roleEntity.swing(InteractionHand.MAIN_HAND);
        this.roleEntity.makeSound(SoundEvents.CHEST_OPEN);

        // 如果背包该槽已空 -> 目标完成，马上找下一个目标（下个 tick 也会找）
        if (inventory.getItem(this.currentTarget.inventorySlot).isEmpty()) {
//            System.out.println("[tick] Finished placing current held slot " + currentTarget.inventorySlot + ", clearing target");
            this.currentTarget = null;
        } else {
//            System.out.println("[tick] Still items left in inv slot " + currentTarget.inventorySlot + ", keep target for next tick");
            // 保留 currentTarget（仍指向同箱同槽），下一 tick 继续（如果槽填满，会自动跳到 next slot）
        }

//        System.out.println("=== Tick end ===");
    }

    /**
     * 在同一个 chest 中查找下一个可放的位置（空槽或同类且有空间）。
     * 返回 slot index 或 null（没有可用槽）。
     */
    private Integer findNextSlotInSameChest(ChestBlockEntity chest, ItemStack held) {
        for (int s = 0; s < chest.getContainerSize(); s++) {
            ItemStack cs = chest.getItem(s);
            if (cs.isEmpty()) return s;
            if (ItemStack.isSameItemSameComponents(cs, held) && cs.getCount() < cs.getMaxStackSize()) return s;
        }
        return null;
    }

    private boolean isReached(BlockPos pos) {
        return pos.distToCenterSqr(this.roleEntity.position()) <= 9;
    }

    private record OperationalTarget(int inventorySlot, BlockPos blockPos, ChestBlockEntity chest, int slotIndex) {}
}
