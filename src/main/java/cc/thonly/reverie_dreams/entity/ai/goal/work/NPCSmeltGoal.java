package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.entity.ai.goal.util.EntityTargetUtil;
import cc.thonly.reverie_dreams.entity.npc.NPCEntityImpl;
import cc.thonly.reverie_dreams.entity.npc.NPCWorkModes;
import cc.thonly.reverie_dreams.server.CookingInputRecipeManager;
import net.minecraft.block.entity.*;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class NPCSmeltGoal extends Goal {
    private final NPCEntityImpl roleEntity;
    @Nullable
    private OperationalTarget operationalTarget;

    public NPCSmeltGoal(NPCEntityImpl roleEntity) {
        this.roleEntity = roleEntity;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    /** 找到实体身上所有可熔炼的物品 */
    private List<Integer> findAllInputSlots() {
        List<Integer> slots = new ArrayList<>();
        SimpleInventory inventory = this.roleEntity.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty() && CookingInputRecipeManager.getInstance().contains(stack.getItem())) {
                slots.add(i);
            }
        }
        return slots;
    }

    /** 找到附近的熔炉 */
    private List<BlockPos> findFurnaceBlockPosList() {
        List<BlockPos> blockPosList = new ArrayList<>();
        World world = this.roleEntity.getWorld();
        BlockPos center = this.roleEntity.getWorkingPos();
        int r = 8;

        BlockBox box = new BlockBox(
                center.getX() - r, center.getY() - r, center.getZ() - r,
                center.getX() + r, center.getY() + r, center.getZ() + r
        );
        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int x = box.getMinX(); x <= box.getMaxX(); x++) {
            for (int y = box.getMinY(); y <= box.getMaxY(); y++) {
                for (int z = box.getMinZ(); z <= box.getMaxZ(); z++) {
                    pos.set(x, y, z);
                    if (world.getBlockEntity(pos) instanceof AbstractFurnaceBlockEntity) {
                        blockPosList.add(pos.toImmutable());
                    }
                }
            }
        }
        return blockPosList;
    }

    @Override
    public boolean canStart() {
        if (!EntityTargetUtil.isThisWorkMode(this.roleEntity, NPCWorkModes.SMELT)) {
            return false;
        }

        MinecraftServer server = this.roleEntity.getServer();
        CookingInputRecipeManager instance = CookingInputRecipeManager.getInstance();
        if (server != null && instance.isEmpty()) {
            instance.load(server);
        }

        // 如果已有目标继续执行
        if (this.operationalTarget != null) {
            return true;
        }

        // 尝试设置新目标
        return this.trySetTarget();
    }

    @Override
    public boolean shouldContinue() {
        return this.operationalTarget != null &&
                EntityTargetUtil.isThisWorkMode(this.roleEntity, NPCWorkModes.SMELT);
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    /** 选择一个熔炉目标 */
    private boolean trySetTarget() {
        World world = this.roleEntity.getWorld();
        List<BlockPos> furnaceList = this.findFurnaceBlockPosList();
        List<Integer> inputSlots = this.findAllInputSlots();

        for (BlockPos blockPos : furnaceList) {
            BlockEntity be = world.getBlockEntity(blockPos);
            if (!(be instanceof AbstractFurnaceBlockEntity furnace)) continue;

            // ===== 输入槽 (slot 0) =====
            ItemStack furnaceStack = furnace.getStack(0);
            boolean isEmpty = furnaceStack.isEmpty();

            for (int invSlot : inputSlots) {
                ItemStack npcStack = this.roleEntity.getInventory().getStack(invSlot);
                if (npcStack.isEmpty()) continue;
                Item item = npcStack.getItem();

                // 匹配炉子类型
                if (isSmeltingFurnace(furnace) && !CookingInputRecipeManager.getInstance().isSmelting(item)) continue;
                if (isSmokerFurnace(furnace) && !CookingInputRecipeManager.getInstance().isSmoker(item)) continue;
                if (isBlastFurnace(furnace) && !CookingInputRecipeManager.getInstance().isBlast(item)) continue;

                if (isEmpty || (ItemStack.areItemsAndComponentsEqual(furnaceStack, npcStack) &&
                        furnaceStack.getCount() < furnaceStack.getMaxCount())) {
                    this.operationalTarget = new OperationalTarget(invSlot, blockPos, furnace, 0);
                    return true;
                }
            }

            // ===== 燃料槽 (slot 1) =====
            ItemStack fuelSlot = furnace.getStack(1);
            for (int i = 0; i < this.roleEntity.getInventory().size(); i++) {
                ItemStack npcStack = this.roleEntity.getInventory().getStack(i);
                if (npcStack.isEmpty()) continue;
                if (CookingInputRecipeManager.isFuel(npcStack)) {
                    if (fuelSlot.isEmpty() || (ItemStack.areItemsAndComponentsEqual(fuelSlot, npcStack) &&
                            fuelSlot.getCount() < fuelSlot.getMaxCount())) {
                        this.operationalTarget = new OperationalTarget(i, blockPos, furnace, 1);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isSmeltingFurnace(AbstractFurnaceBlockEntity be) {
        return be instanceof FurnaceBlockEntity;
    }
    private boolean isSmokerFurnace(AbstractFurnaceBlockEntity be) {
        return be instanceof SmokerBlockEntity;
    }
    private boolean isBlastFurnace(AbstractFurnaceBlockEntity be) {
        return be instanceof BlastFurnaceBlockEntity;
    }

    @Override
    public void tick() {
        if (this.operationalTarget == null) {
            this.trySetTarget();
            return;
        }

        BlockPos pos = this.operationalTarget.blockPos();
        AbstractFurnaceBlockEntity furnace = this.operationalTarget.furnaceBlockEntity();
        int npcSlot = this.operationalTarget.inventorySlot();
        int furnaceSlotIndex = this.operationalTarget.slotIndex();

        ItemStack npcStack = this.roleEntity.getInventory().getStack(npcSlot);
        if (npcStack.isEmpty()) {
            this.operationalTarget = null;
            return;
        }

        if (!isReached(pos)) {
            this.roleEntity.getNavigation().startMovingTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.0D);
            this.roleEntity.getLookControl().lookAt(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            return;
        }

        ItemStack furnaceSlot = furnace.getStack(furnaceSlotIndex);

        if (furnaceSlot.isEmpty()) {
            furnace.setStack(furnaceSlotIndex, npcStack.split(1));
        } else if (ItemStack.areItemsAndComponentsEqual(furnaceSlot, npcStack)) {
            int space = furnaceSlot.getMaxCount() - furnaceSlot.getCount();
            if (space > 0) {
                int move = Math.min(npcStack.getCount(), space);
                furnaceSlot.increment(move);
                npcStack.decrement(move);
            }
        }

        furnace.markDirty();
        this.roleEntity.swingHand(Hand.MAIN_HAND);

        // 炉子已满 或者 NPC 没货了 -> 清空目标
        if (npcStack.isEmpty() || furnaceSlot.getCount() >= furnaceSlot.getMaxCount()) {
            this.operationalTarget = null;
        }
    }

    private boolean isReached(BlockPos blockPos) {
        double distSq = blockPos.getSquaredDistance(this.roleEntity.getPos());
        return distSq <= 9; // 半径 3 格
    }

    /** 保存目标信息 */
    public record OperationalTarget(
            int inventorySlot, // NPC 背包槽位
            BlockPos blockPos,
            AbstractFurnaceBlockEntity furnaceBlockEntity,
            int slotIndex // 0 = 输入, 1 = 燃料
    ) {}
}
