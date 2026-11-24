package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.entity.ai.work.WorkArea;
import cc.thonly.reverie_dreams.entity.ai.work.WorkClaims;
import cc.thonly.reverie_dreams.entity.ai.work.WorkNavigation;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.inventory.NPCWorkInventory;
import cc.thonly.reverie_dreams.inventory.storage.StorageTransfer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import cc.thonly.reverie_dreams.data.npc.NPCWorkMode;

/**
 * Collect actual production drops; the pickup toggle and real cargo capacity remain authoritative.
 */
public final class NPCWorkCollectGoal extends Goal {
    private final BaseNPCLikeEntity npc;
    private final WorkNavigation navigation;
    private ItemEntity item;
    private WorkArea area;
    private NPCWorkMode originalMode;
    private long nextSearch;

    public NPCWorkCollectGoal(BaseNPCLikeEntity npc) {
        this.npc = npc;
        navigation = new WorkNavigation(npc);
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        var work = npc.getWorkController();
        if (!(npc.level() instanceof ServerLevel level) || !work.working() || !NPCWorkLogisticsGoal.production(npc.getWorkMode())
                || !npc.canPickUpLoot() || work.logistics() || level.getGameTime() < nextSearch)
            return false;
        nextSearch = level.getGameTime() + 20;
        area = work.area();
        originalMode = npc.getWorkMode();
        item = level.getEntitiesOfClass(ItemEntity.class, area.box(), e -> !e.isRemoved() && !e.hasPickUpDelay()
                        && area.contains(e.blockPosition()) && !e.getItem().isEmpty() && work.available(e.getUUID(), level.getGameTime())
                        && StorageTransfer.capacity(npc.getInventory(), e.getItem(), NPCWorkInventory.CARGO_SIZE) > 0)
                .stream().min(Comparator.comparingDouble(npc::distanceToSqr)).orElse(null);
        return item != null && WorkClaims.of(level).take(List.of(item.getUUID()), npc.getUUID(), level.getGameTime());
    }

    @Override
    public boolean canContinueToUse() {
        return item != null && !item.isRemoved() && !item.getItem().isEmpty() && !item.hasPickUpDelay() && npc.getWorkController().working()
                && npc.getWorkMode() == originalMode && area.equals(npc.getWorkController().area()) && area.contains(item.blockPosition());
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (!canContinueToUse() || !WorkClaims.of(npc.level()).take(List.of(item.getUUID()), npc.getUUID(), npc.level().getGameTime())) {
            stop();
            return;
        }
        var result = navigation.move(item, 1.5);
        if (result == WorkNavigation.Result.FAILED) {
            npc.getWorkController().reject(item.getUUID(), npc.level().getGameTime());
            stop();
            return;
        }
        if (result != WorkNavigation.Result.REACHED) {
            npc.getWorkController().status("collecting_outputs");
            return;
        }
        var stack = item.getItem();
        int moved = NPCWorkInventory.insert(npc.getInventory(), stack);
        if (moved > 0) {
            npc.take(item, moved);
            if (stack.isEmpty())
                item.discard();
            else
                item.setItem(stack);
        }
        stop();
    }

    @Override
    public void stop() {
        item = null;
        navigation.stop();
        WorkClaims.of(npc.level()).release(npc.getUUID());
        nextSearch = npc.level().getGameTime() + 10;
    }
}
