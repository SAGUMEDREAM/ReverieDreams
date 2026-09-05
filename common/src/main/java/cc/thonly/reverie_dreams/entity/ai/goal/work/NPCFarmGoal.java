package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.api.block.CustomMatureBlock;
import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.entity.ai.goal.util.EntityTargetUtil;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class NPCFarmGoal extends Goal {


    //计时器 避免种植太快显得不是人
    private int workTimer = 0;


    private final BaseNPCLikeEntity maid;
    private BlockPos targetPos;
    private static final Predicate<ItemStack> IS_SEED = stack -> !stack.isEmpty() && stack.is(ItemTags.VILLAGER_PLANTABLE_SEEDS) && stack.getItem() instanceof BlockItem;


    public NPCFarmGoal(BaseNPCLikeEntity maid) {
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        this.maid = maid;

    }

    @Override
    public boolean canUse() {
        if (!EntityTargetUtil.isThisWorkMode(maid, NPCWorkModes.FARM)) {
            return false;
        }
//        System.out.println("tryStartNPCFarmGoal");
        targetPos = getNearTargetBlock(maid, new BlockPos(maid.getBlockX(), (int) Math.floor(maid.getY()), maid.getBlockZ())
                , true);
//        System.out.println("Target: "+targetPos);
        if (targetPos != null) {
//            System.out.println("ret True");
            return true;
        }


        return false;
    }


    @Override
    public void start() {


    }

    @Override
    public void tick() {
        workTimer++;
        if (maid.getNavigation().isInProgress()) {
            maid.getNavigation().stop();
            workTimer = 0;
        }
        if (isCrop(targetPos, getServerLevel(maid)) || isFarmLandTop(targetPos, getServerLevel(maid))) {
            this.maid.getLookControl().setLookAt(targetPos.getCenter().relative(Direction.DOWN, 0.5));
            if (workTimer % 8 == 0) {
                this.maid.swing(InteractionHand.MAIN_HAND);
                harvest(targetPos);
                planting(targetPos);
            }
        }
        super.tick();
    }

    @Override
    public void stop() {
        workTimer = 0;
        super.stop();
    }

    public static boolean isMature(CustomMatureBlock crop, BlockState cropsState) {
        if (crop instanceof CropBlock cropBlock) {
            return cropBlock.isMaxAge(cropsState);
        } else if (crop instanceof AbstractCropBlock basicCropBlock) {
            return basicCropBlock.isMature(cropsState);
        }
        return false;
    }

    public boolean harvest(BlockPos targetFarmLandTop) {
        ServerLevel serverWorld = getServerLevel(maid);
        BlockState cropsState = serverWorld.getBlockState(targetFarmLandTop);
        if (cropsState.getBlock() instanceof CustomMatureBlock crop && this.isMature(crop, cropsState)) {
            dropItem(targetFarmLandTop);
            //调用breakBlock无法吃到时运 自定义掉落并关闭break的掉落
            serverWorld.destroyBlock(targetFarmLandTop, false, maid);
            maid.getNavigation().createPath(targetFarmLandTop, 10);
            return true;
        }
        return false;
    }

    public boolean planting(BlockPos targetFarmLandTop) {
        if (!isFarmLandTop(targetFarmLandTop, getServerLevel(maid))) return false;
        ServerLevel serverWorld = getServerLevel(maid);
        Integer seedSlot = maid.getInventory().findHand(IS_SEED);
        if (seedSlot == null) return false;
        ItemStack seedStack = maid.getInventory().getItem(seedSlot);
        BlockState statePlant = ((BlockItem) seedStack.getItem()).getBlock().defaultBlockState();
        serverWorld.setBlockAndUpdate(targetFarmLandTop, statePlant);
        serverWorld.gameEvent(GameEvent.BLOCK_PLACE, targetFarmLandTop, GameEvent.Context.of(maid, statePlant));
        serverWorld.playSound(
                null, targetFarmLandTop.getX(), targetFarmLandTop.getY(), targetFarmLandTop.getZ(), SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F
        );
        seedStack.shrink(1);
        if (seedStack.isEmpty()) {
            maid.getInventory().setItem(seedSlot, ItemStack.EMPTY);
        }
        return true;
    }

    //掉落农作物
    public void dropItem(BlockPos cropPos) {
        ServerLevel serverWorld = getServerLevel(maid);
        BlockState cropState = serverWorld.getBlockState(cropPos);
        BlockEntity blockEntity = cropState.hasBlockEntity() ? serverWorld.getBlockEntity(cropPos) : null;

        Block.dropResources(cropState, serverWorld, cropPos, blockEntity, maid, maid.getMainHandItem());
    }


    public static BlockPos getNearTargetBlock(BaseNPCLikeEntity maid, BlockPos origen, boolean random) {
        List<BlockPos> targetFarmlands = new LinkedList<>();
        BlockPos.MutableBlockPos mutable = maid.blockPosition().mutable();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {
                    mutable.set(origen.getX() + i, Math.round(origen.getY()) + j, origen.getZ() + k);
                    if (NPCFarmGoal.isCrop(mutable, getServerLevel(maid)) || (isMaidHasSeeds(maid) && isFarmLandTop(mutable, getServerLevel(maid)))) {
                        if (!random) return new BlockPos(mutable);
//                        this.targetPositions.add(new BlockPos(mutable));
                        targetFarmlands.add(new BlockPos(mutable));
                    }
                }
            }
        }
        return targetFarmlands.isEmpty() ? null : (BlockPos) targetFarmlands.get(getServerLevel(maid).getRandom().nextInt(targetFarmlands.size()));


    }

    public static boolean isMaidHasSeeds(BaseNPCLikeEntity maid) {
        return maid.getInventory().findHand(IS_SEED) != null;
    }

    //这个位置是否可以收割/种植
    public static boolean isCrop(BlockPos pos, ServerLevel world) {
        BlockState blockState = world.getBlockState(pos);
        Block crop = blockState.getBlock();
        if (!(crop instanceof CustomMatureBlock customMatureBlock)) {
            return false;
        }
        return isMature(customMatureBlock, blockState);
//        return is && ((IMatureBlock) crop).isMature(blockState);
    }

    //这个方块下面是不是耕地
    public static boolean isFarmLandTop(BlockPos b, ServerLevel world) {
        Block block = world.getBlockState(b.below()).getBlock();
        return world.getBlockState(b).isAir() && (block instanceof FarmBlock);
    }


}
