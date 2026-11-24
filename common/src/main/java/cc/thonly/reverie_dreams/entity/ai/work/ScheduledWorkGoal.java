package cc.thonly.reverie_dreams.entity.ai.work;

import cc.thonly.reverie_dreams.data.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.entity.ai.NPCWorkController;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.List;

import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import net.minecraft.world.entity.Entity;

/**
 * Shared scheduling/stop contract; each goal owns its work-specific selection and execution.
 */
public abstract class ScheduledWorkGoal extends Goal {
    protected final BaseNPCLikeEntity npc;
    protected final NPCWorkMode mode;
    protected final WorkNavigation navigation;
    protected final WorkSearch search = new WorkSearch();
    protected WorkArea site;
    private boolean active;
    private long nextSearch;

    protected ScheduledWorkGoal(BaseNPCLikeEntity npc, NPCWorkMode mode) {
        this.npc = npc;
        this.mode = mode;
        navigation = new WorkNavigation(npc);
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    protected NPCWorkController work() {
        return npc.getWorkController();
    }

    protected boolean canSchedule() {
        return work().working(mode);
    }

    protected boolean sameSite() {
        return site.equals(work().area());
    }

    private boolean productionAllowed() {
        return !(npc instanceof NPCSimpleEntity simple)
                || !simple.getSkillTask().waitingForProducedItems()
                || mode == NPCWorkModes.SMELT;
    }

    @Override
    public final boolean canUse() {
        if (!(npc.level() instanceof ServerLevel level) || !canSchedule() || work().logistics() || !productionAllowed() || level.getGameTime() < nextSearch)
            return false;
        site = work().area();
        nextSearch = level.getGameTime() + searchInterval();
        if (!findTask(level))
            return false;
        if (!WorkClaims.of(level).take(resources(), npc.getUUID(), level.getGameTime())) {
            clearTask();
            return false;
        }
        active = true;
        return true;
    }

    @Override
    public final boolean canContinueToUse() {
        return active && npc.level() instanceof ServerLevel level && canSchedule() && !work().logistics() && productionAllowed()
                && sameSite() && validTask(level);
    }

    @Override
    public final boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public final void tick() {
        if (!canContinueToUse()) {
            stop();
            return;
        }
        var level = (ServerLevel) npc.level();
        if (!WorkClaims.of(level).take(resources(), npc.getUUID(), level.getGameTime())) {
            finish("storage_busy", 20);
            return;
        }
        runTask(level);
    }

    @Override
    public final void stop() {
        active = false;
        navigation.stop();
        WorkClaims.of(npc.level()).release(npc.getUUID());
        work().tools().restore(npc.getInventory());
        clearTask();
        nextSearch = npc.level().getGameTime() + 10;
    }

    protected void finish(String reason, int delay) {
        stop();
        work().status(reason);
        nextSearch = npc.level().getGameTime() + delay;
    }

    protected boolean reach(BlockPos pos, double distance) {
        return motion(navigation.move(pos, distance), pos, true);
    }

    protected boolean reach(Entity entity, double distance) {
        return motion(navigation.move(entity, distance), entity.blockPosition(), true);
    }

    protected boolean reachInteraction(BlockPos pos, double distance) {
        return motion(navigation.moveForInteraction(pos, distance), pos, false);
    }

    private boolean motion(WorkNavigation.Result result, BlockPos pos, boolean rejectAll) {
        if (result == WorkNavigation.Result.FAILED) {
            long now = npc.level().getGameTime();
            if (rejectAll) {
                for (var resource : resources())
                    work().reject(resource, now);
            } else {
                work().reject(pos, now);
            }
            search.reject(pos, now);
            finish("unreachable", 10);
        } else if (result == WorkNavigation.Result.MOVING)
            work().status("walking");
        return result == WorkNavigation.Result.REACHED;
    }

    protected List<?> resources() {
        return List.of();
    }

    protected int searchInterval() {
        return 5;
    }

    protected abstract boolean findTask(ServerLevel level);

    protected abstract boolean validTask(ServerLevel level);

    protected abstract void runTask(ServerLevel level);

    protected abstract void clearTask();
}
