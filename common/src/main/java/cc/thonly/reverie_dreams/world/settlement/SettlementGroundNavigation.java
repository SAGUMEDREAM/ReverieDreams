package cc.thonly.reverie_dreams.world.settlement;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Ordinary ground navigation except while it owns a short, budgeted pilot path.
 */
public final class SettlementGroundNavigation extends GroundPathNavigation {
    private static final int MAX_LEG = 10;
    static final int MAX_LEG_SQR = MAX_LEG * MAX_LEG;
    private static final int SEARCH_DISTANCE = 12;
    private static final int SEARCH_PADDING = 8;
    private static final int FOLLOW_PADDING = 2;
    private Path pilotPath;
    private boolean pilotControl;

    enum StartResult {STARTED, UNAVAILABLE, BUDGET_EXHAUSTED}

    public SettlementGroundNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    void beginPilotControl() {
        this.stop();
        this.hasDelayedRecomputation = false;
        this.pilotControl = true;
    }

    void endPilotControl() {
        this.stopPilotPath();
        this.pilotControl = false;
    }

    StartResult startPilotPath(ServerLevel level, BlockPos destination, SettlementPilotData data) {
        return this.startPilotPath(level, List.of(destination), data);
    }

    StartResult startPilotPath(ServerLevel level, Collection<BlockPos> destinations, SettlementPilotData data) {
        BlockPos from = this.mob.blockPosition();
        Set<BlockPos> reachableTargets = new HashSet<>();
        for (BlockPos destination : destinations) {
            if (from.distSqr(destination) > MAX_LEG_SQR || !level.isPositionEntityTicking(destination))
                continue;
            var chunk = level.getChunkSource().getChunkNow(destination.getX() >> 4, destination.getZ() >> 4);
            // A station is a feet position. Reject solid targets before vanilla attempts a partial approach.
            if (chunk != null && !chunk.getBlockState(destination).isSolidRender()
                    && !chunk.getBlockState(destination.above()).isSolidRender()) {
                reachableTargets.add(destination);
            }
        }
        if (reachableTargets.isEmpty() || !regionLoaded(level, from, from, SEARCH_DISTANCE + SEARCH_PADDING)) {
            data.withdrawPathRequest(this.mob.getUUID());
            return StartResult.UNAVAILABLE;
        }
        if (!data.takePathBudget(level.getGameTime(), this.mob.getUUID())) {
            return StartResult.BUDGET_EXHAUSTED;
        }

        // Bypass the vanilla target-height search and set an explicit range; sites are feet positions.
        this.stop();
        this.hasDelayedRecomputation = false;
        Path candidate = super.createPath(reachableTargets, SEARCH_PADDING, false, 0, SEARCH_DISTANCE);
        if (candidate == null || !candidate.canReach()) {
            return StartResult.UNAVAILABLE;
        }
        for (int i = 0; i < candidate.getNodeCount(); i++) {
            if (!level.isPositionEntityTicking(candidate.getNodePos(i))) {
                return StartResult.UNAVAILABLE;
            }
        }
        if (!this.moveTo(candidate, 0.9)) {
            return StartResult.UNAVAILABLE;
        }
        this.pilotPath = candidate;
        return StartResult.STARTED;
    }

    @Override
    public void tick() {
        if (this.pilotPath != null && this.getPath() == this.pilotPath) {
            if (!(this.level instanceof ServerLevel server) || SettlementDutyGoal.isInterrupted(this.mob)) {
                this.stopPilotPath();
                return;
            }
            if (!this.pilotPath.isDone()) {
                int index = this.pilotPath.getNextNodeIndex();
                BlockPos from = this.mob.blockPosition();
                // Vanilla may advance past the current node in this tick, so cover the following node too.
                for (int i = index; i <= index + 1 && i < this.pilotPath.getNodeCount(); i++) {
                    BlockPos next = this.pilotPath.getNodePos(i);
                    if (from.distSqr(next) > 16 || !server.isPositionEntityTicking(next)
                            || !regionLoaded(server, from, next, FOLLOW_PADDING)) {
                        this.stopPilotPath();
                        return;
                    }
                }
            }
        }
        super.tick();
    }

    @Override
    public void recomputePath() {
        if (!this.pilotControl) {
            super.recomputePath();
        }
        // Pilot paths never bypass the per-dimension budget via vanilla's block-update callback.
    }

    void stopPilotPath() {
        if (this.pilotPath != null && this.getPath() == this.pilotPath) {
            this.stop();
            this.hasDelayedRecomputation = false;
        }
        this.pilotPath = null;
    }

    private static boolean regionLoaded(ServerLevel level, BlockPos a, BlockPos b, int padding) {
        int minX = (Math.min(a.getX(), b.getX()) - padding) >> 4;
        int maxX = (Math.max(a.getX(), b.getX()) + padding) >> 4;
        int minZ = (Math.min(a.getZ(), b.getZ()) - padding) >> 4;
        int maxZ = (Math.max(a.getZ(), b.getZ()) + padding) >> 4;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (level.getChunkSource().getChunkNow(x, z) == null) {
                    return false;
                }
            }
        }
        return true;
    }
}
