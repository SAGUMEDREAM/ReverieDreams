package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.data.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.entity.ai.work.ScheduledWorkGoal;
import cc.thonly.reverie_dreams.entity.base.FakePlayer;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.mixin.accessor.BlockBehaviourAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

import com.mojang.logging.LogUtils;

/**
 * Bounded, leased use of optional mod machinery; empty-hand interaction cannot consume NPC equipment.
 */
public class NPCFindBlockGoal extends ScheduledWorkGoal {
    private final Block block;
    private final boolean useAction;
    private BlockPos target;
    private Object incarnation;

    public NPCFindBlockGoal(BaseNPCLikeEntity npc, NPCWorkMode mode, Block block, boolean useAction, boolean exclusive) {
        super(npc, mode);
        this.block = block;
        this.useAction = useAction;
    }

    @Override
    protected boolean findTask(ServerLevel level) {
        target = search.find(site, level.getGameTime(), level::hasChunkAt,
                pos -> level.getBlockState(pos).is(block) && work().available(pos, level.getGameTime()));
        if (target == null) {
            work().status("no_workstation");
            return false;
        }
        incarnation = level.getBlockEntity(target);
        return true;
    }

    @Override
    protected boolean validTask(ServerLevel level) {
        return target != null && site.contains(target) && level.hasChunkAt(target) && level.getBlockState(target).is(block)
                && level.getBlockEntity(target) == incarnation;
    }

    @Override
    protected List<?> resources() {
        return target == null ? List.of() : List.of(target);
    }

    @Override
    protected void clearTask() {
        target = null;
        incarnation = null;
    }

    @Override
    protected void runTask(ServerLevel level) {
        if (!reach(target, 2.5))
            return;
        if (!useAction) {
            finish("ready", 20);
            return;
        }
        var player = FakePlayer.get(level);
        var previous = player.getItemInHand(InteractionHand.MAIN_HAND).copy();
        try {
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            var hit = new BlockHitResult(target.getCenter(), npc.getDirection(), target, false);
            var result = ((BlockBehaviourAccessor) block).reverie_dreams$useWithoutItem(level.getBlockState(target), level, target, player, hit);
            if (result.consumesAction()) {
                npc.swing(InteractionHand.MAIN_HAND);
                work().action();
                finish("ready", 10);
            } else
                finish("work_action_failed", 100);
        } catch (RuntimeException failure) {
            LogUtils.getLogger().error("NPC {} failed workstation interaction at {} (mode={})", npc.getUUID(), target, mode, failure);
            finish("work_action_failed", 100);
        } finally {
            player.setItemInHand(InteractionHand.MAIN_HAND, previous);
        }
    }
}
