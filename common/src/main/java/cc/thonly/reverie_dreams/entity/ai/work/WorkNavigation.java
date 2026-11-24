package cc.thonly.reverie_dreams.entity.ai.work;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;

/**
 * Small navigation component used by work goals; does not select targets or move items.
 */
public final class WorkNavigation {
    public enum Result {MOVING, REACHED, FAILED}

    private final BaseNPCLikeEntity npc;
    private final HashSet<BlockPos> missedApproaches = new HashSet<>();
    private BlockPos target;
    private BlockPos approach;
    private long nextPath;
    private long progressAt;
    private double best;
    private int failures;

    public WorkNavigation(BaseNPCLikeEntity npc) {
        this.npc = npc;
    }

    public Result move(BlockPos pos, double reach) {
        return move(pos, reach, null, false);
    }

    public Result moveForInteraction(BlockPos pos, double reach) {
        return move(pos, reach, null, true);
    }

    private Result move(BlockPos pos, double reach, Entity entity, boolean requireVisibleApproach) {
        if (!(npc.level() instanceof ServerLevel level) || !level.hasChunkAt(pos))
            return Result.FAILED;
        long now = level.getGameTime();
        double distance = entity == null ? pos.distToCenterSqr(npc.position()) : npc.distanceToSqr(entity);
        if (!pos.equals(target)) {
            target = pos.immutable();
            approach = null;
            missedApproaches.clear();
            nextPath = now;
            progressAt = now;
            best = distance;
            failures = 0;
        }
        if (distance <= reach * reach && visible(level, npc.getEyePosition(), entity == null ? pos.getCenter() : entity.getEyePosition(), pos)) {
            npc.getNavigation().stop();
            npc.getWorkController().navigation(this, false);
            return Result.REACHED;
        }
        if (distance < best - .25) {
            best = distance;
            progressAt = now;
        }
        if (now - progressAt >= 100)
            return Result.FAILED;
        npc.getWorkController().navigation(this, true);
        if (now >= nextPath) {
            nextPath = now + 10;
            if (requireVisibleApproach && approach != null && npc.getNavigation().isDone()) {
                missedApproaches.add(approach);
            }
            var path = entity == null ? npc.getNavigation().createPath(
                    requireVisibleApproach ? interactionApproaches(level, pos, reach) : adjacentApproaches(level, pos), 0)
                    : npc.getNavigation().createPath(entity, 0);
            boolean started = path != null && path.canReach() && npc.getNavigation().moveTo(path, 1.0);
            if (requireVisibleApproach && started)
                approach = path.getTarget();
            failures = started ? 0 : failures + 1;
            if (failures >= 3)
                return Result.FAILED;
        }
        return Result.MOVING;
    }

    private HashSet<BlockPos> adjacentApproaches(ServerLevel level, BlockPos pos) {
        var result = new HashSet<BlockPos>();
        for (var direction : Direction.Plane.HORIZONTAL) {
            for (int dy = -1; dy <= 1; dy++) {
                var stand = pos.relative(direction).offset(0, dy, 0);
                if (canStand(level, stand))
                    result.add(stand);
            }
        }
        return result;
    }

    private HashSet<BlockPos> interactionApproaches(ServerLevel level, BlockPos pos, double reach) {
        var result = new HashSet<BlockPos>();
        Vec3 center = pos.getCenter();
        int span = (int) Math.ceil(reach);
        for (BlockPos stand : BlockPos.betweenClosed(pos.offset(-span, -1, -span), pos.offset(span, 1, span))) {
            Vec3 feet = Vec3.atBottomCenterOf(stand);
            double standReach = reach - .2;
            if (stand.equals(npc.blockPosition()) || missedApproaches.contains(stand)
                    || feet.distanceToSqr(center) > standReach * standReach || !canStand(level, stand))
                continue;
            if (visible(level, feet.add(0, npc.getEyeHeight(), 0), center, pos))
                result.add(stand.immutable());
        }
        return result;
    }

    private static boolean canStand(ServerLevel level, BlockPos stand) {
        return level.hasChunkAt(stand)
                && level.getBlockState(stand).getCollisionShape(level, stand).isEmpty()
                && level.getBlockState(stand.above()).getCollisionShape(level, stand.above()).isEmpty()
                && !level.getBlockState(stand.below()).getCollisionShape(level, stand.below()).isEmpty();
    }

    private boolean visible(ServerLevel level, Vec3 from, Vec3 to, BlockPos target) {
        var hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, npc));
        return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(target);
    }

    public Result move(Entity entity, double reach) {
        if (!entity.isAlive() || entity.level() != npc.level())
            return Result.FAILED;
        return move(entity.blockPosition(), reach, entity, false);
    }

    public void stop() {
        target = null;
        npc.getNavigation().stop();
        npc.getWorkController().navigation(this, false);
    }
}
