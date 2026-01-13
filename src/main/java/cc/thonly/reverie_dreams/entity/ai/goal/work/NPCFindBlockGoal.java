package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.data.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.entity.ai.goal.util.EntityTargetUtil;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class NPCFindBlockGoal extends Goal {
    public static final Map<ServerLevel, Map<Long, BaseNPCLikeEntity>> EXCLUSIONS = new HashMap<>(8);
    private final BaseNPCLikeEntity roleEntity;
    @Nullable
    private BlockPos currentTarget = null;
    private final NPCWorkMode workMode;
    private final Block targetBlock;
    private final boolean useAction;
    private final boolean exclusive;

    public NPCFindBlockGoal(BaseNPCLikeEntity roleEntity, NPCWorkMode workMode, Block targetBlock, boolean useAction, boolean exclusive) {
        this.roleEntity = roleEntity;
        this.workMode = workMode;
        this.targetBlock = targetBlock;
        this.useAction = useAction;
        this.exclusive = exclusive;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.TARGET));
    }

    private List<BlockPos> findBlockPosList() {
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
                    if (world.getBlockState(pos).getBlock() == this.targetBlock) {
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

//        System.out.println("[NPCFindBlockGoal] Found chests (sorted): " + list.size());
        if (!list.isEmpty()) {
//            System.out.println("[NPCFindBlockGoal] Nearest chest at: " + list.get(0));
        }
        return list;
    }


    @Override
    public boolean canUse() {
        if (!EntityTargetUtil.isThisWorkMode(this.roleEntity, this.workMode)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        boolean cont = EntityTargetUtil.isThisWorkMode(this.roleEntity, this.workMode);
        // 少量日志，别太频繁打印（可按需打开）
//         System.out.println("[shouldContinue] " + cont);
        return cont;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private BlockPos findNextTarget() {
        List<BlockPos> blockPoses = findBlockPosList();
        ServerLevel serverLevel = getServerLevel(this.roleEntity);
        for (BlockPos blockPos : blockPoses) {
            BlockState blockState = serverLevel.getBlockState(blockPos);
            if (!blockState.getBlock().equals(this.targetBlock)) {
//                System.out.println("不是方块");
                continue;
            }
//            return blockPos;
            Map<Long, BaseNPCLikeEntity> map = EXCLUSIONS.computeIfAbsent(serverLevel, x -> new HashMap<>());
            if (this.exclusive && map.containsKey(blockPos.asLong())) {
                BaseNPCLikeEntity baseNPCLikeEntity = map.get(blockPos.asLong());
                if (baseNPCLikeEntity != this.roleEntity) {
//                    System.out.println("已被别的实体占有");
                    continue;
                } else {
//                    System.out.println("找到找有的");
                    return blockPos;
                }
            } else {
//                System.out.println("创建占有");
                map.put(blockPos.asLong(), this.roleEntity);
                return blockPos;
            }
        }

//        System.out.println("[findNextTarget] no suitable target found");
        return null;
    }

    @Override
    public void tick() {
//        System.out.println("=== Tick start ===");
        ServerLevel serverLevel = getServerLevel(this.roleEntity);
        if (!EntityTargetUtil.isThisWorkMode(this.roleEntity, this.workMode)) {
            Map<Long, BaseNPCLikeEntity> map = EXCLUSIONS.computeIfAbsent(serverLevel, x -> new HashMap<>(16));
            if (this.currentTarget != null) {
                map.remove(this.currentTarget.asLong());
            }
            return;
        }

        var x = this.findNextTarget();
        if (x != null && this.currentTarget != null && this.currentTarget.asLong() != x.asLong()) {
            Map<Long, BaseNPCLikeEntity> map = EXCLUSIONS.computeIfAbsent(serverLevel, e -> new HashMap<>(16));
            if (this.currentTarget != null) {
                map.remove(this.currentTarget.asLong());
            }
        }
        this.currentTarget = x;
//        System.out.println(this.roleEntity.toString() + this.currentTarget);
        if (this.currentTarget == null) {
            return;
        }

        // 移动到目标箱子
        if (!isReached(this.currentTarget)) {
//            System.out.println("[tick] Moving to block at " + this.currentTarget);
            this.roleEntity.getNavigation().moveTo(
                    this.currentTarget.getX() + 0.5,
                    this.currentTarget.getY() + 0.5,
                    this.currentTarget.getZ() + 0.5,
                    1.0D
            );
//            System.out.println("=== Tick end ===");
            return;
        } else {
            BlockState blockState = serverLevel.getBlockState(this.currentTarget);
            if (this.useAction && !blockState.isAir()) {
                BlockPos pos = this.currentTarget;
                Direction direction = this.roleEntity.getDirection();
                Vec3 hitVec = Vec3.atCenterOf(pos);
                BlockHitResult hitResult = new BlockHitResult(
                        hitVec,
                        direction,
                        pos,
                        false // isInside
                );
                this.targetBlock.useItemOn(this.roleEntity.getItemBySlot(EquipmentSlot.MAINHAND),
                        blockState,
                        serverLevel,
                        this.currentTarget,
                        FakePlayer.get(serverLevel), InteractionHand.MAIN_HAND,
                        hitResult
                );
                this.targetBlock.useWithoutItem(
                        blockState,
                        serverLevel,
                        this.currentTarget,
                        FakePlayer.get(serverLevel),
                        hitResult
                );
                this.roleEntity.getLookControl().setLookAt(
                        pos.getX(),
                        pos.getY(),
                        pos.getZ()
                );
                float headYaw = this.roleEntity.getYHeadRot();
                this.roleEntity.yBodyRot = approachAngle(
                        this.roleEntity.yBodyRot,
                        headYaw,
                        10.0F // 每 tick 最大旋转角度
                );
            }
        }
        this.roleEntity.swing(InteractionHand.MAIN_HAND);

//        System.out.println("=== Tick end ===");
    }

    private static float approachAngle(float current, float target, float maxChange) {
        float delta = Mth.wrapDegrees(target - current);
        if (delta > maxChange) delta = maxChange;
        if (delta < -maxChange) delta = -maxChange;
        return current + delta;
    }


    private boolean isReached(BlockPos pos) {
        return pos.distToCenterSqr(this.roleEntity.position()) <= 9;
    }

}