package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.block.BeehiveBlockProxy;
import cc.thonly.reverie_dreams.data.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.entity.ai.goal.util.EntityTargetUtil;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Slf4j
public class NPCHoneycombHarvestingGoal extends Goal {
    public static final Map<ServerLevel, Map<Long, BaseNPCLikeEntity>> EXCLUSIONS = new HashMap<>(8);

    private final BaseNPCLikeEntity roleEntity;

    @Nullable
    private BlockPos currentTarget = null;

    private final NPCWorkMode workMode;
    private final boolean useAction;
    private final boolean exclusive;

    public NPCHoneycombHarvestingGoal(BaseNPCLikeEntity roleEntity) {
        this.roleEntity = roleEntity;
        this.workMode = NPCWorkModes.HONEYCOMB_HARVESTING;
        this.useAction = true;
        this.exclusive = true;

        this.setFlags(EnumSet.of(
                Flag.MOVE,
                Flag.LOOK,
                Flag.TARGET
        ));
    }

    private List<BlockPos> findBlockPosList() {
        List<BlockPos> list = new LinkedList<>();

        Level world = this.roleEntity.level();
        BlockPos center = this.roleEntity.getWorkingPos();

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

                    BlockState blockState = world.getBlockState(pos);

                    if (blockState.getBlock() instanceof BeehiveBlock
                            && blockState.getValue(BeehiveBlock.HONEY_LEVEL)
                            >= BeehiveBlock.MAX_HONEY_LEVELS) {

                        list.add(pos.immutable());
                    }
                }
            }
        }

        list.sort(Comparator.comparingDouble(
                blockPos -> blockPos.distSqr(center)
        ));

        return list;
    }

    @Override
    public boolean canUse() {
        return EntityTargetUtil.isThisWorkMode(
                this.roleEntity,
                this.workMode
        );
    }

    @Override
    public boolean canContinueToUse() {
        return EntityTargetUtil.isThisWorkMode(
                this.roleEntity,
                this.workMode
        );
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Nullable
    private BlockPos findNextTarget() {
        List<BlockPos> blockPoses = findBlockPosList();

        ServerLevel serverLevel = getServerLevel(this.roleEntity);

        Map<Long, BaseNPCLikeEntity> map = EXCLUSIONS.computeIfAbsent(
                serverLevel,
                x -> new HashMap<>()
        );

        for (BlockPos blockPos : blockPoses) {
            BlockState blockState = serverLevel.getBlockState(blockPos);

            if (!(blockState.getBlock() instanceof BeehiveBlock)
                    || blockState.getValue(BeehiveBlock.HONEY_LEVEL) < BeehiveBlock.MAX_HONEY_LEVELS) {
                continue;
            }

            if (this.exclusive && map.containsKey(blockPos.asLong())) {
                BaseNPCLikeEntity owner = map.get(blockPos.asLong());

                if (owner != this.roleEntity) {
                    continue;
                }

                return blockPos;
            }

            map.put(
                    blockPos.asLong(),
                    this.roleEntity
            );

            return blockPos;
        }

        return null;
    }

    @Override
    public void tick() {
        ServerLevel serverLevel = getServerLevel(this.roleEntity);

        if (!EntityTargetUtil.isThisWorkMode(
                this.roleEntity,
                this.workMode
        )) {
            this.releaseCurrentTarget(serverLevel);
            return;
        }

        BlockPos nextTarget = this.findNextTarget();
        if (nextTarget != null
                && this.currentTarget != null
                && !this.currentTarget.equals(nextTarget)) {

            this.releaseCurrentTarget(serverLevel);
        }

        this.currentTarget = nextTarget;
        if (this.currentTarget == null) {
            return;
        }

        BlockPos target = this.currentTarget;
        if (!this.isReached(target)) {
            this.roleEntity.getNavigation().moveTo(
                    target.getX() + 0.5D,
                    target.getY() + 0.5D,
                    target.getZ() + 0.5D,
                    1.0D
            );

            return;
        }

        BlockState blockState = serverLevel.getBlockState(target);
        Block block = blockState.getBlock();

        if (!this.useAction
                || !(block instanceof BeehiveBlock)
                || blockState.getValue(BeehiveBlock.HONEY_LEVEL)
                < BeehiveBlock.MAX_HONEY_LEVELS) {
            return;
        }

        InteractionHand hand = findOrTakeHarvestTool();
        if (hand == null) {
            return;
        }

        this.roleEntity.getLookControl().setLookAt(
                target.getX() + 0.5D,
                target.getY() + 0.5D,
                target.getZ() + 0.5D
        );

        try {
            BeehiveBlockProxy proxy = (BeehiveBlockProxy) block;
            proxy.reverie_dreams$onInteractUse(
                    this.roleEntity,
                    serverLevel,
                    blockState,
                    target
            );

            this.roleEntity.swing(hand);

        } catch (Exception e) {
            log.error(
                    "Failed to harvest beehive at {}",
                    target,
                    e
            );
        }

        float headYaw = this.roleEntity.getYHeadRot();

        this.roleEntity.yBodyRot = approachAngle(
                this.roleEntity.yBodyRot,
                headYaw,
                10.0F
        );
    }

    /**
     * 查找 NPC 当前可用的采集工具。
     * <p>
     * 优先级：
     * 主手剪刀
     * 主手玻璃瓶
     * 副手剪刀
     * 副手玻璃瓶
     * 背包中的剪刀
     * 背包中的玻璃瓶
     */
    @Nullable
    private InteractionHand findOrTakeHarvestTool() {
        ItemStack mainHand = this.roleEntity.getMainHandItem();
        ItemStack offHand = this.roleEntity.getOffhandItem();

        // 主手已经有可用工具
        if (isHarvestTool(mainHand)) {
            return InteractionHand.MAIN_HAND;
        }

        // 副手已经有可用工具
        if (isHarvestTool(offHand)) {
            return InteractionHand.OFF_HAND;
        }

        /*
         * 当前双手没有工具，从背包取。
         *
         * 优先剪刀，其次玻璃瓶。
         */
        int shearsSlot = findInventoryItem(Items.SHEARS);

        if (shearsSlot >= 0) {
            return moveInventoryItemToMainHand(shearsSlot);
        }

        int bottleSlot = findInventoryItem(Items.GLASS_BOTTLE);

        if (bottleSlot >= 0) {
            return moveInventoryItemToMainHand(bottleSlot);
        }

        return null;
    }

    private boolean isHarvestTool(ItemStack stack) {
        return stack.is(Items.SHEARS)
                || stack.is(Items.GLASS_BOTTLE);
    }

    /**
     * 在 NPC 背包中寻找指定物品。
     */
    private int findInventoryItem(net.minecraft.world.item.Item item) {
        var inventory = this.roleEntity.getInventory();

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);

            if (!stack.isEmpty() && stack.is(item)) {
                return slot;
            }
        }

        return -1;
    }

    /**
     * 把背包中的物品移动到主手。
     * <p>
     * 如果主手为空，直接交换。
     * 如果主手有其它物品，则将主手物品放回原背包槽位，
     * 再将目标物品拿到主手。
     */
    @Nullable
    private InteractionHand moveInventoryItemToMainHand(int slot) {
        var inventory = this.roleEntity.getInventory();

        ItemStack inventoryStack =
                inventory.getItem(slot);

        if (inventoryStack.isEmpty()) {
            return null;
        }

        ItemStack mainHand =
                this.roleEntity.getMainHandItem();

        /*
         * 主手为空，直接拿出来。
         */
        if (mainHand.isEmpty()) {
            ItemStack taken = inventoryStack.copy();

            inventory.setItem(
                    slot,
                    ItemStack.EMPTY
            );

            this.roleEntity.setItemInHand(
                    InteractionHand.MAIN_HAND,
                    taken
            );

            inventory.setChanged();

            return InteractionHand.MAIN_HAND;
        }

        /*
         * 主手有东西：
         * 与背包槽位交换。
         */
        ItemStack oldMainHand = mainHand.copy();
        ItemStack taken = inventoryStack.copy();

        inventory.setItem(
                slot,
                oldMainHand
        );

        this.roleEntity.setItemInHand(
                InteractionHand.MAIN_HAND,
                taken
        );

        inventory.setChanged();

        return InteractionHand.MAIN_HAND;
    }

    private void releaseCurrentTarget(ServerLevel serverLevel) {
        if (this.currentTarget == null) {
            return;
        }

        Map<Long, BaseNPCLikeEntity> map = EXCLUSIONS.get(serverLevel);

        if (map != null) {
            BaseNPCLikeEntity owner =
                    map.get(this.currentTarget.asLong());

            if (owner == this.roleEntity) {
                map.remove(this.currentTarget.asLong());
            }

            if (map.isEmpty()) {
                EXCLUSIONS.remove(serverLevel);
            }
        }

        this.currentTarget = null;
    }

    private static float approachAngle(
            float current,
            float target,
            float maxChange
    ) {
        float delta = Mth.wrapDegrees(
                target - current
        );

        if (delta > maxChange) {
            delta = maxChange;
        }

        if (delta < -maxChange) {
            delta = -maxChange;
        }

        return current + delta;
    }

    private boolean isReached(BlockPos pos) {
        return pos.distToCenterSqr(
                this.roleEntity.position()
        ) <= 9.0D;
    }
}