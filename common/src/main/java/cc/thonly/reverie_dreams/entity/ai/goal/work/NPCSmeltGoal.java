package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.entity.ai.goal.util.EntityTargetUtil;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.server.CookingInputRecipeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class NPCSmeltGoal extends Goal {
    private final BaseNPCLikeEntity roleEntity;
    @Nullable
    private OperationalTarget operationalTarget;

    public NPCSmeltGoal(BaseNPCLikeEntity roleEntity) {
        this.roleEntity = roleEntity;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    /** 找到实体身上所有可熔炼的物品 */
    private List<Integer> findAllInputSlots() {
        List<Integer> slots = new ArrayList<>();
        SimpleContainer inventory = this.roleEntity.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && CookingInputRecipeManager.getInstance().contains(stack.getItem())) {
                slots.add(i);
            }
        }
        return slots;
    }

    /** 找到附近的熔炉 */
    private List<BlockPos> findFurnaceBlockPosList() {
        List<BlockPos> blockPosList = new ArrayList<>();
        Level world = this.roleEntity.level();
        BlockPos center = this.roleEntity.getWorkingPos();
        int r = 8;

        BoundingBox box = new BoundingBox(
                center.getX() - r, center.getY() - r, center.getZ() - r,
                center.getX() + r, center.getY() + r, center.getZ() + r
        );
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int y = box.minY(); y <= box.maxY(); y++) {
                for (int z = box.minZ(); z <= box.maxZ(); z++) {
                    pos.set(x, y, z);
                    if (world.getBlockEntity(pos) instanceof AbstractFurnaceBlockEntity) {
                        blockPosList.add(pos.immutable());
                    }
                }
            }
        }
        return blockPosList;
    }

    @Override
    public boolean canUse() {
        if (!EntityTargetUtil.isThisWorkMode(this.roleEntity, NPCWorkModes.SMELT)) {
            return false;
        }

        MinecraftServer server = this.roleEntity.level().getServer();
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
    public boolean canContinueToUse() {
        return this.operationalTarget != null &&
                EntityTargetUtil.isThisWorkMode(this.roleEntity, NPCWorkModes.SMELT);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /** 选择一个熔炉目标 */
    private boolean trySetTarget() {
        Level world = this.roleEntity.level();
        List<BlockPos> furnaceList = this.findFurnaceBlockPosList();
        List<Integer> inputSlots = this.findAllInputSlots();

        for (BlockPos blockPos : furnaceList) {
            BlockEntity be = world.getBlockEntity(blockPos);
            if (!(be instanceof AbstractFurnaceBlockEntity furnace)) continue;

            // ===== 输入槽 (slot 0) =====
            ItemStack furnaceStack = furnace.getItem(0);
            boolean isEmpty = furnaceStack.isEmpty();

            for (int invSlot : inputSlots) {
                ItemStack npcStack = this.roleEntity.getInventory().getItem(invSlot);
                if (npcStack.isEmpty()) continue;
                Item item = npcStack.getItem();

                // 匹配炉子类型
                if (isSmeltingFurnace(furnace) && !CookingInputRecipeManager.getInstance().isSmelting(item)) continue;
                if (isSmokerFurnace(furnace) && !CookingInputRecipeManager.getInstance().isSmoker(item)) continue;
                if (isBlastFurnace(furnace) && !CookingInputRecipeManager.getInstance().isBlast(item)) continue;

                if (isEmpty || (ItemStack.isSameItemSameComponents(furnaceStack, npcStack) &&
                        furnaceStack.getCount() < furnaceStack.getMaxStackSize())) {
                    this.operationalTarget = new OperationalTarget(invSlot, blockPos, furnace, 0);
                    return true;
                }
            }

            // ===== 燃料槽 (slot 1) =====
            ItemStack fuelSlot = furnace.getItem(1);
            for (int i = 0; i < this.roleEntity.getInventory().getContainerSize(); i++) {
                ItemStack npcStack = this.roleEntity.getInventory().getItem(i);
                if (npcStack.isEmpty()) continue;
                if (CookingInputRecipeManager.isFuel(npcStack)) {
                    if (fuelSlot.isEmpty() || (ItemStack.isSameItemSameComponents(fuelSlot, npcStack) &&
                            fuelSlot.getCount() < fuelSlot.getMaxStackSize())) {
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

        ItemStack npcStack = this.roleEntity.getInventory().getItem(npcSlot);
        if (npcStack.isEmpty()) {
            this.operationalTarget = null;
            return;
        }

        if (!isReached(pos)) {
            this.roleEntity.getNavigation().moveTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.0D);
            this.roleEntity.getLookControl().setLookAt(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            return;
        }

        ItemStack furnaceSlot = furnace.getItem(furnaceSlotIndex);

        if (furnaceSlot.isEmpty()) {
            furnace.setItem(furnaceSlotIndex, npcStack.split(1));
        } else if (ItemStack.isSameItemSameComponents(furnaceSlot, npcStack)) {
            int space = furnaceSlot.getMaxStackSize() - furnaceSlot.getCount();
            if (space > 0) {
                int move = Math.min(npcStack.getCount(), space);
                furnaceSlot.grow(move);
                npcStack.shrink(move);
            }
        }

        furnace.setChanged();
        this.roleEntity.swing(InteractionHand.MAIN_HAND);

        // 炉子已满 或者 NPC 没货了 -> 清空目标
        if (npcStack.isEmpty() || furnaceSlot.getCount() >= furnaceSlot.getMaxStackSize()) {
            this.operationalTarget = null;
        }
    }

    private boolean isReached(BlockPos blockPos) {
        double distSq = blockPos.distToCenterSqr(this.roleEntity.position());
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
