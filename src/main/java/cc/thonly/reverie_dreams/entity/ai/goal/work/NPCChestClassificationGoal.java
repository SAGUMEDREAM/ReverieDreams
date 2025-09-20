package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.entity.ai.goal.util.EntityTargetUtil;
import cc.thonly.reverie_dreams.entity.npc.NPCEntityImpl;
import cc.thonly.reverie_dreams.entity.npc.NPCWorkModes;
import cc.thonly.reverie_dreams.inventory.NPCInventoryImpl;
import cc.thonly.reverie_dreams.server.ItemTagManager;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

public class NPCChestClassificationGoal extends Goal {

    private final NPCEntityImpl roleEntity;
    @Nullable
    private OperationalTarget currentTarget = null;

    public NPCChestClassificationGoal(NPCEntityImpl roleEntity) {
        this.roleEntity = roleEntity;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    private List<BlockPos> findChestBlockPosList() {
        List<BlockPos> list = new LinkedList<>();
        World world = this.roleEntity.getWorld();
        BlockPos center = this.roleEntity.getWorkingPos();
        int r = 8;
        BlockBox box = new BlockBox(center.getX() - r, center.getY() - r, center.getZ() - r,
                center.getX() + r, center.getY() + r, center.getZ() + r);
        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int x = box.getMinX(); x <= box.getMaxX(); x++) {
            for (int y = box.getMinY(); y <= box.getMaxY(); y++) {
                for (int z = box.getMinZ(); z <= box.getMaxZ(); z++) {
                    pos.set(x, y, z);
                    if (world.getBlockState(pos).getBlock() == Blocks.CHEST &&
                            world.getBlockEntity(pos) instanceof ChestBlockEntity) {
                        list.add(pos.toImmutable());
                    }
                }
            }
        }

        // 按照距离工作点排序，最近的排在前面
        list.sort((a, b) -> Double.compare(
                a.getSquaredDistance(center),
                b.getSquaredDistance(center)
        ));

//        System.out.println("[NPCChestClassificationGoal] Found chests (sorted): " + list.size());
        if (!list.isEmpty()) {
//            System.out.println("[NPCChestClassificationGoal] Nearest chest at: " + list.get(0));
        }
        return list;
    }


    @Override
    public boolean canStart() {
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
    public boolean shouldContinue() {
        boolean cont = EntityTargetUtil.isThisWorkMode(this.roleEntity, NPCWorkModes.CHEST_CLASSIFICATION);
        // 少量日志，别太频繁打印（可按需打开）
        // System.out.println("[shouldContinue] " + cont);
        return cont;
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    /**
     * 找到下一个可以放置至少 1 个物品的目标：
     *  返回（inventorySlot, chestPos, chestEntity, slotIndex）
     *  要求： chestSlot 是空的 OR chestSlot 与 heldItem 相同且有剩余空间 (>0)
     */
    private OperationalTarget findNextTarget(NPCInventoryImpl inventory) {
        List<BlockPos> chests = findChestBlockPosList();

        for (int invIndex = 0; invIndex < inventory.size(); invIndex++) {
            ItemStack held = inventory.getStack(invIndex);
            if (held.isEmpty()) continue;

//            System.out.println("[findNextTarget] trying held slot " + invIndex + ": " + held.getCount() + "x " + held.getItem());

            for (BlockPos chestPos : chests) {
                BlockEntity be = this.roleEntity.getWorld().getBlockEntity(chestPos);
                if (!(be instanceof ChestBlockEntity chest)) continue;

                boolean chestEmpty = true;
                boolean chestCompatible = true;

                // 扫一遍箱子，看是否已有不同物品
                for (int s = 0; s < chest.size(); s++) {
                    ItemStack cs = chest.getStack(s);
                    if (!cs.isEmpty()) {
                        chestEmpty = false;
                        if (!ItemStack.areItemsAndComponentsEqual(cs, held)) {
                            chestCompatible = false; // 已有不同物品，不兼容
                            break;
                        }
                    }
                }

                if (!chestCompatible) continue; // 跳过这个箱子

                // 到这里说明箱子是空的，或全是同类物品
                for (int slot = 0; slot < chest.size(); slot++) {
                    ItemStack chestStack = chest.getStack(slot);
                    if (chestStack.isEmpty()) {
//                        System.out.println("[findNextTarget] choose empty slot " + slot + " in chest " + chestPos);
                        return new OperationalTarget(invIndex, chestPos, chest, slot);
                    }
                    if (ItemStack.areItemsAndComponentsEqual(chestStack, held)) {
                        int space = chestStack.getMaxCount() - chestStack.getCount();
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
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
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
        ItemStack held = inventory.getStack(this.currentTarget.inventorySlot);
        if (held.isEmpty()) {
//            System.out.println("[tick] Target's inventory slot is empty, clearing target");
            this.currentTarget = null;
//            System.out.println("=== Tick end ===");
            return;
        }

        // 移动到目标箱子
        if (!isReached(this.currentTarget.blockPos)) {
//            System.out.println("[tick] Moving to chest at " + currentTarget.blockPos);
            this.roleEntity.getNavigation().startMovingTo(
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
        ItemStack chestStack = chest.getStack(slotIndex);
        boolean slotSuitable = false;
        if (chestStack.isEmpty()) {
            slotSuitable = true;
        } else if (ItemStack.areItemsAndComponentsEqual(chestStack, held) && chestStack.getCount() < chestStack.getMaxCount()) {
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
                chestStack = chest.getStack(slotIndex);
            } else {
//                System.out.println("[tick] No workable slot in this chest, clearing current target to find another chest");
                this.currentTarget = null;
//                System.out.println("=== Tick end ===");
                return;
            }
        }

        // 到这里， chestStack 是当前 slot 的最新引用（可能为空或有空间）
        chestStack = chest.getStack(slotIndex);
        held = inventory.getStack(this.currentTarget.inventorySlot); // refresh
        if (held.isEmpty()) {
//            System.out.println("[tick] Held became empty before placing, clearing target");
            this.currentTarget = null;
//            System.out.println("=== Tick end ===");
            return;
        }

        // 放入逻辑：计算实际放入数量 move
        if (chestStack.isEmpty()) {
            int move = Math.min(held.getCount(), held.getMaxCount()); // 放一堆或全部（视需求可改为只放1）
            ItemStack toSet = held.copy();
            toSet.setCount(move);
            chest.setStack(slotIndex, toSet);
            held.decrement(move);
//            System.out.println("[tick] Placed " + move + "x " + toSet.getItem() + " into empty slot " + slotIndex);
        } else { // same item and has space guaranteed
            int space = chestStack.getMaxCount() - chestStack.getCount();
            int move = Math.min(space, held.getCount());
            if (move > 0) {
                chestStack.increment(move);
                held.decrement(move);
//                System.out.println("[tick] Added " + move + "x " + chestStack.getItem() + " to slot " + slotIndex + " (now " + chestStack.getCount() + ")");
            } else {
                // 理论不应到这里（我们以前检查过有空间），但保险处理
//                System.out.println("[tick] Unexpected: computed move == 0, clearing target");
                currentTarget = null;
//                System.out.println("=== Tick end ===");
                return;
            }
        }

        chest.markDirty();
        this.roleEntity.swingHand(Hand.MAIN_HAND);
        this.roleEntity.playSound(SoundEvents.BLOCK_CHEST_OPEN);

        // 如果背包该槽已空 -> 目标完成，马上找下一个目标（下个 tick 也会找）
        if (inventory.getStack(this.currentTarget.inventorySlot).isEmpty()) {
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
        for (int s = 0; s < chest.size(); s++) {
            ItemStack cs = chest.getStack(s);
            if (cs.isEmpty()) return s;
            if (ItemStack.areItemsAndComponentsEqual(cs, held) && cs.getCount() < cs.getMaxCount()) return s;
        }
        return null;
    }

    private boolean isReached(BlockPos pos) {
        return pos.getSquaredDistance(this.roleEntity.getPos()) <= 9;
    }

    private record OperationalTarget(int inventorySlot, BlockPos blockPos, ChestBlockEntity chest, int slotIndex) {}
}
