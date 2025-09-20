package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.compat.BorukvaFoodCompatImpl;
import cc.thonly.reverie_dreams.entity.ai.goal.util.EntityTargetUtil;
import cc.thonly.reverie_dreams.entity.npc.NPCEntityImpl;
import cc.thonly.reverie_dreams.entity.npc.NPCWorkModes;
import cc.thonly.reverie_dreams.interfaces.IMatureBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.event.GameEvent;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class NPCFarmGoal extends Goal {


    //计时器 避免种植太快显得不是人
    private int workTimer = 0;


    private final NPCEntityImpl maid;
    private BlockPos targetPos;
    private static final Predicate<ItemStack> IS_SEED = stack -> !stack.isEmpty() && stack.isIn(ItemTags.VILLAGER_PLANTABLE_SEEDS) && stack.getItem() instanceof BlockItem;


    public NPCFarmGoal(NPCEntityImpl maid) {
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        this.maid = maid;

    }

    @Override
    public boolean canStart() {
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
        if (maid.getNavigation().isFollowingPath()) {
            maid.getNavigation().stop();
            workTimer = 0;
        }
        if (isCrop(targetPos, getServerWorld(maid)) || isFarmLandTop(targetPos, getServerWorld(maid))) {
            this.maid.getLookControl().lookAt(targetPos.toCenterPos().offset(Direction.DOWN, 0.5));
            if (workTimer % 8 == 0) {
                this.maid.swingHand(Hand.MAIN_HAND);
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

    public static boolean isMature(IMatureBlock crop, BlockState cropsState) {
        if (crop instanceof CropBlock cropBlock) {
            return cropBlock.isMature(cropsState);
        } else if (crop instanceof AbstractCropBlock basicCropBlock) {
            return basicCropBlock.isMature(cropsState);
        }
        return false;
    }

    public boolean harvest(BlockPos targetFarmLandTop) {
        ServerWorld serverWorld = getServerWorld(maid);
        BlockState cropsState = serverWorld.getBlockState(targetFarmLandTop);
        if (cropsState.getBlock() instanceof IMatureBlock crop && this.isMature(crop, cropsState)) {
            dropItem(targetFarmLandTop);
            //调用breakBlock无法吃到时运 自定义掉落并关闭break的掉落
            serverWorld.breakBlock(targetFarmLandTop, false, maid);
            maid.getNavigation().findPathTo(targetFarmLandTop, 10);
            return true;
        }
        return false;
    }

    public boolean planting(BlockPos targetFarmLandTop) {
        if (!isFarmLandTop(targetFarmLandTop, getServerWorld(maid))) return false;
        ServerWorld serverWorld = getServerWorld(maid);
        Integer seedSlot = maid.getInventory().findHand(IS_SEED);
        if (seedSlot == null) return false;
        ItemStack seedStack = maid.getInventory().getStack(seedSlot);
        BlockState statePlant = ((BlockItem) seedStack.getItem()).getBlock().getDefaultState();
        serverWorld.setBlockState(targetFarmLandTop, statePlant);
        serverWorld.emitGameEvent(GameEvent.BLOCK_PLACE, targetFarmLandTop, GameEvent.Emitter.of(maid, statePlant));
        serverWorld.playSound(
                null, targetFarmLandTop.getX(), targetFarmLandTop.getY(), targetFarmLandTop.getZ(), SoundEvents.ITEM_CROP_PLANT, SoundCategory.BLOCKS, 1.0F, 1.0F
        );
        seedStack.decrement(1);
        if (seedStack.isEmpty()) {
            maid.getInventory().setStack(seedSlot, ItemStack.EMPTY);
        }
        return true;
    }

    //掉落农作物
    public void dropItem(BlockPos cropPos) {
        ServerWorld serverWorld = getServerWorld(maid);
        BlockState cropState = serverWorld.getBlockState(cropPos);
        BlockEntity blockEntity = cropState.hasBlockEntity() ? serverWorld.getBlockEntity(cropPos) : null;

        Block.dropStacks(cropState, serverWorld, cropPos, blockEntity, maid, maid.getMainHandStack());
    }


    public static BlockPos getNearTargetBlock(NPCEntityImpl maid, BlockPos origen, boolean random) {
        List<BlockPos> targetFarmlands = new LinkedList<>();
        BlockPos.Mutable mutable = maid.getBlockPos().mutableCopy();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {
                    mutable.set(origen.getX() + i, Math.round(origen.getY()) + j, origen.getZ() + k);
                    if (NPCFarmGoal.isCrop(mutable, getServerWorld(maid)) || (isMaidHasSeeds(maid) && isFarmLandTop(mutable, getServerWorld(maid)))) {
                        if (!random) return new BlockPos(mutable);
//                        this.targetPositions.add(new BlockPos(mutable));
                        targetFarmlands.add(new BlockPos(mutable));
                    }
                }
            }
        }
        return targetFarmlands.isEmpty() ? null : (BlockPos) targetFarmlands.get(getServerWorld(maid).getRandom().nextInt(targetFarmlands.size()));


    }

    public static boolean isMaidHasSeeds(NPCEntityImpl maid) {
        return maid.getInventory().findHand(IS_SEED) != null;
    }

    //这个位置是否可以收割/种植
    public static boolean isCrop(BlockPos pos, ServerWorld world) {
        BlockState blockState = world.getBlockState(pos);
        Block crop = blockState.getBlock();
        if (!(crop instanceof IMatureBlock iMatureBlock)) {
            return false;
        }
        return isMature(iMatureBlock, blockState);
//        return is && ((IMatureBlock) crop).isMature(blockState);
    }

    //这个方块下面是不是耕地
    public static boolean isFarmLandTop(BlockPos b, ServerWorld world) {
        Block block = world.getBlockState(b.down()).getBlock();
        return world.getBlockState(b).isAir() && (block instanceof FarmlandBlock || (BorukvaFoodCompatImpl.hasBorukvaFood() && block == BorukvaFoodCompatImpl.BETTER_FARMLAND));
    }


}
