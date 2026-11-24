package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.api.block.CustomMatureBlock;
import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.entity.ai.work.FarmTransaction;
import cc.thonly.reverie_dreams.entity.ai.work.ScheduledWorkGoal;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.inventory.NPCWorkInventory;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.List;

import cc.thonly.reverie_dreams.entity.npc.work.NPCProduction;

public class NPCFarmGoal extends ScheduledWorkGoal {
    private BlockPos target;
    private BlockState expected;

    public NPCFarmGoal(BaseNPCLikeEntity npc) {
        super(npc, NPCWorkModes.FARM);
    }

    @Override
    protected boolean findTask(ServerLevel level) {
        target = search.find(site, level.getGameTime(), level::hasChunkAt, pos -> work().available(pos, level.getGameTime())
                && (isCrop(pos, level) || selectedSeed(npc) != null && isFarmLandTop(pos, level)));
        if (target == null) {
            work().status("waiting_for_crops");
            return false;
        }
        expected = level.getBlockState(target);
        return true;
    }

    @Override
    protected boolean validTask(ServerLevel level) {
        return target != null && site.contains(target) && level.hasChunkAt(target) && level.getBlockState(target) == expected;
    }

    @Override
    protected List<?> resources() {
        return target == null ? List.of() : List.of(target);
    }

    @Override
    protected void clearTask() {
        target = null;
        expected = null;
    }

    @Override
    protected void runTask(ServerLevel level) {
        if (!reach(target, 2.0))
            return;
        npc.getLookControl().setLookAt(target.getCenter());
        if (isCrop(target, level))
            harvest(target);
        else
            planting(target);
    }

    public boolean harvest(BlockPos pos) {
        if (!(npc.level() instanceof ServerLevel level) || !level.hasChunkAt(pos))
            return false;
        BlockState mature = level.getBlockState(pos);
        if (!isCrop(pos, level)) {
            finish("waiting_for_crops", 10);
            return false;
        }
        var seed = mature.getCloneItemStack(level, pos, false);
        if (!(seed.getItem() instanceof BlockItem item) || item.getBlock() != mature.getBlock()) {
            finish("need_matching_seeds", 40);
            return false;
        }
        BlockState young = mature.getBlock() instanceof AbstractCropBlock crop
                ? mature.setValue(crop.getAgeProperty(), 0) : ((CropBlock) mature.getBlock()).getStateForAge(0);
        if (!young.canSurvive(level, pos)) {
            finish("work_write_refused", 40);
            return false;
        }
        var drops = Block.getDrops(mature, level, pos, level.getBlockEntity(pos), npc, npc.getMainHandItem());
        var result = FarmTransaction.harvest(mature, young, seed, npc.getInventory(), drops,
                (old, next) -> level.hasChunkAt(pos) && level.getBlockState(pos) == old && next.canSurvive(level, pos)
                        && level.setBlockAndUpdate(pos, next),
                stack -> {
                    NPCProduction.item(npc, "farm", stack);
                    NPCWorkInventory.insert(npc.getInventory(), stack);
                    if (!stack.isEmpty())
                        Block.popResource(level, pos, stack);
                });
        if (result == FarmTransaction.Result.SUCCESS) {
            level.levelEvent(2001, pos, Block.getId(mature));
            effects(level, pos);
            work().action();
            finish("ready", 8);
            return true;
        }
        if (result == FarmTransaction.Result.NO_SEED) {
            work().demand(stack -> stack.is(seed.getItem()), 16, "need_matching_seeds");
            search.reject(pos, level.getGameTime());
            finish("need_matching_seeds", 20);
        } else
            finish("work_write_refused", 40);
        return false;
    }

    /**
     * Empty plots only use an explicitly selected seed in either hand. Harvesting never enters this path.
     */
    public boolean planting(BlockPos pos) {
        if (!(npc.level() instanceof ServerLevel level) || !level.hasChunkAt(pos) || !isFarmLandTop(pos, level))
            return false;
        var selected = selectedSeed(npc);
        if (selected == null || !(selected.getItem() instanceof BlockItem item)) {
            finish("need_seeds", 20);
            return false;
        }
        var next = item.getBlock().defaultBlockState();
        var hand = npc.getMainHandItem() == selected ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        boolean success = FarmTransaction.plant(selected.copy(), npc.getInventory(), hand == InteractionHand.MAIN_HAND ? 22 : 23,
                () -> next.canSurvive(level, pos) && level.getBlockState(pos).isAir() && level.setBlockAndUpdate(pos, next));
        if (success) {
            npc.swing(hand);
            effects(level, pos);
            work().action();
        }
        finish(success ? "ready" : "work_write_refused", success ? 8 : 40);
        return success;
    }

    private void effects(ServerLevel level, BlockPos pos) {
        level.gameEvent(npc, GameEvent.BLOCK_PLACE, pos);
        level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1, 1);
        npc.swing(InteractionHand.MAIN_HAND);
    }

    private static ItemStack selectedSeed(BaseNPCLikeEntity npc) {
        for (var hand : InteractionHand.values()) {
            var stack = npc.getItemInHand(hand);
            if (!stack.isEmpty() && stack.is(ItemTags.VILLAGER_PLANTABLE_SEEDS) && stack.getItem() instanceof BlockItem)
                return stack;
        }
        return null;
    }

    public static boolean isMature(CustomMatureBlock crop, BlockState state) {
        return crop instanceof CropBlock vanilla ? vanilla.isMaxAge(state)
                : crop instanceof AbstractCropBlock custom && custom.isMature(state);
    }

    public static boolean isCrop(BlockPos pos, ServerLevel level) {
        if (!level.hasChunkAt(pos))
            return false;
        var state = level.getBlockState(pos);
        return state.getBlock() instanceof CropBlock vanilla ? vanilla.isMaxAge(state)
                : state.getBlock() instanceof CustomMatureBlock crop && isMature(crop, state);
    }

    public static boolean isFarmLandTop(BlockPos pos, ServerLevel level) {
        return level.hasChunkAt(pos) && level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).getBlock() instanceof FarmlandBlock;
    }

    public static boolean isMaidHasSeeds(BaseNPCLikeEntity npc) {
        return selectedSeed(npc) != null;
    }

    public static BlockPos getNearTargetBlock(BaseNPCLikeEntity npc, BlockPos origin, boolean random) {
        var level = (ServerLevel) npc.level();
        for (var pos : BlockPos.betweenClosed(origin.offset(-1, -1, -1), origin.offset(1, 1, 1)))
            if (isCrop(pos, level) || selectedSeed(npc) != null && isFarmLandTop(pos, level))
                return pos.immutable();
        return null;
    }
}
