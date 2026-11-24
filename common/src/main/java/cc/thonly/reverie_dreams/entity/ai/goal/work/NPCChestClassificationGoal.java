package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.ai.goal.util.EntityTargetUtil;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.inventory.storage.*;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cc.thonly.reverie_dreams.entity.ai.work.WorkArea;
import cc.thonly.reverie_dreams.entity.npc.work.NPCWorkFeedback;

/**
 * Navigation owns no inventory mutations: live resolution and rules precede every transfer.
 */
public class NPCChestClassificationGoal extends Goal {
    private final BaseNPCLikeEntity npc;
    private final WorkNavigationProgress progress = new WorkNavigationProgress();
    private final Map<BlockPos, Long> rejected = new HashMap<>();
    private ServerLevel contextLevel;
    private BlockPos center;
    private WorkArea area;
    private StoragePlanner.Job job;
    private long nextSearch;
    private long nextPath;
    private String status = "sorting_finished";

    public NPCChestClassificationGoal(BaseNPCLikeEntity npc) {
        this.npc = npc;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private StorageSettings settings() {
        return StorageSettings.from(ReverieDreams.config());
    }

    private boolean working() {
        return npc.isAlive() && npc.level() instanceof ServerLevel
                && EntityTargetUtil.isThisWorkMode(npc, NPCWorkModes.CHEST_CLASSIFICATION);
    }

    private void context(ServerLevel level) {
        if (contextLevel != level || !npc.getWorkController().area().equals(area)) {
            if (contextLevel != null)
                StorageClaims.forWorld(contextLevel).release(npc.getUUID());
            npc.getNavigation().stop();
            contextLevel = level;
            center = npc.getWorkingPos().immutable();
            area = npc.getWorkController().area();
            job = null;
            rejected.clear();
            nextSearch = level.getGameTime() + Math.floorMod(npc.getId(), 20);
        }
    }

    private boolean available(StoragePlanner.Store store, ServerLevel level, long now) {
        return rejected.getOrDefault(store.pos(), Long.MIN_VALUE) <= now
                && StorageClaims.forWorld(level).available(store.pos(), npc.getUUID(), now);
    }

    private List<BlockPos> claims(StoragePlanner.Job candidate) {
        var positions = new ArrayList<BlockPos>();
        positions.add(candidate.destination().pos());
        if (candidate.pickup())
            positions.add(candidate.source().pos());
        return positions;
    }

    @Override
    public boolean canUse() {
        if (!working())
            return false;
        ServerLevel level = (ServerLevel) npc.level();
        context(level);
        long now = level.getGameTime();
        if (now < nextSearch)
            return false;
        rejected.entrySet().removeIf(e -> e.getValue() <= now);
        var result = StoragePlanner.plan(npc.getInventory(), StorageWorld.scan(level, area), settings(), npc.position(),
                store -> available(store, level, now));
        job = result.job();
        status = result.code();
        npc.getWorkController().status(status);
        if (job == null) {
            if (!rejected.isEmpty() && status.equals("storage_busy"))
                status = "unreachable";
            nextSearch = now + (status.equals("sorting_finished") ? 40 : 20);
            return false;
        }
        if (!StorageClaims.forWorld(level).acquire(claims(job), npc.getUUID(), now)) {
            job = null;
            nextSearch = now + 20;
            return false;
        }
        progress.reset(now, distance(job.visiting()));
        nextPath = now;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return job != null && working() && contextLevel == npc.level() && npc.getWorkController().area().equals(area);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void stop() {
        if (contextLevel != null)
            StorageClaims.forWorld(contextLevel).release(npc.getUUID());
        npc.getNavigation().stop();
        job = null;
    }

    private double distance(StoragePlanner.Store store) {
        return store.parts().stream().mapToDouble(p -> p.distToCenterSqr(npc.position())).min().orElse(Double.MAX_VALUE);
    }

    private boolean reached(ServerLevel level, StoragePlanner.Store store) {
        for (var part : store.parts()) {
            if (part.distToCenterSqr(npc.position()) > 9)
                continue;
            var hit = level.clip(new ClipContext(npc.getEyePosition(), part.getCenter(),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, npc));
            if (hit.getType() == HitResult.Type.MISS || store.parts().contains(hit.getBlockPos()))
                return true;
        }
        return false;
    }

    private Set<BlockPos> approaches(ServerLevel level, StoragePlanner.Store store) {
        var positions = new HashSet<BlockPos>();
        for (var part : store.parts())
            for (var direction : Direction.Plane.HORIZONTAL) {
                for (int dy = -1; dy <= 1; dy++) {
                    var pos = part.relative(direction).offset(0, dy, 0);
                    if (!level.hasChunkAt(pos) || !level.hasChunkAt(pos.above()) || !level.hasChunkAt(pos.below()))
                        continue;
                    if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                            || !level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()
                            || level.getBlockState(pos.below()).getCollisionShape(level, pos.below()).isEmpty())
                        continue;
                    positions.add(pos);
                }
            }
        return positions;
    }

    private StoragePlanner.Store live(ServerLevel level, StoragePlanner.Store planned) {
        return StorageExecution.live(planned, area, pos -> StorageWorld.resolve(level, pos));
    }

    private void finish(ServerLevel level, String reason) {
        stop();
        status = reason;
        npc.getWorkController().status(reason);
        nextSearch = level.getGameTime() + 10;
    }

    private void reject(ServerLevel level) {
        rejected.put(job.visiting().pos(), level.getGameTime() + 200);
        finish(level, "unreachable");
    }

    @Override
    public void tick() {
        if (!canContinueToUse()) {
            stop();
            return;
        }
        ServerLevel level = (ServerLevel) npc.level();
        long now = level.getGameTime();
        if (!StorageClaims.forWorld(level).acquire(claims(job), npc.getUUID(), now)) {
            finish(level, "storage_busy");
            return;
        }
        var destination = live(level, job.destination());
        var source = job.pickup() ? live(level, job.source()) : null;
        if (destination == null || job.pickup() && (source == null || source.rule().mode() != StorageRule.Mode.INPUT)
                || !destination.rule().accepts(job.sample(), destination.container(), settings())) {
            finish(level, "no_matching_storage");
            return;
        }
        var visiting = job.pickup() ? source : destination;
        var target = visiting.pos().getCenter();
        npc.getLookControl().setLookAt(target.x, target.y, target.z);
        if (!reached(level, visiting)) {
            if (progress.stalled(now, distance(visiting))) {
                reject(level);
                return;
            }
            if (now >= nextPath) {
                nextPath = now + 10;
                var path = npc.getNavigation().createPath(approaches(level, visiting), 0);
                if (progress.pathFailed(path != null && path.canReach() && npc.getNavigation().moveTo(path, 1.0)))
                    reject(level);
            }
            return;
        }
        npc.getNavigation().stop();
        var outcome = StorageExecution.execute(job, npc.getInventory(), settings(), area, pos -> StorageWorld.resolve(level, pos));
        int moved = outcome.moved();
        if (outcome.next() != null) {
            // Carry input cargo directly to its reserved output; reserves do not consume this shipment.
            StorageClaims.forWorld(level).release(npc.getUUID());
            job = outcome.next();
            StorageClaims.forWorld(level).acquire(claims(job), npc.getUUID(), now);
            progress.reset(now, distance(destination));
            nextPath = now + 10;
        } else {
            finish(level, outcome.code());
        }
        if (moved > 0) {
            NPCWorkFeedback.recordAction(npc);
            npc.swing(InteractionHand.MAIN_HAND);
            npc.makeSound(SoundEvents.CHEST_OPEN);
        }
    }

    /**
     * Uses the same planner as execution, including active leases and failed-path cooldowns.
     */
    public String inspect(BlockPos site) {
        if (!(npc.level() instanceof ServerLevel level))
            return "no_chest";
        if (job != null && site.equals(center) && job.routed())
            return "ready";
        long now = level.getGameTime();
        var inspectArea = new WorkArea(site, ReverieDreams.config().npcWorkRange, ReverieDreams.config().npcWorkVerticalRange);
        var result = StoragePlanner.plan(npc.getInventory(), StorageWorld.scan(level, inspectArea), settings(), npc.position(),
                store -> !site.equals(center) ? StorageClaims.forWorld(level).available(store.pos(), npc.getUUID(), now) : available(store, level, now));
        return result.job() == null && site.equals(center) && rejected.values().stream().anyMatch(until -> until > now)
                && result.code().equals("storage_busy")
                ? "unreachable" : result.code();
    }
}
