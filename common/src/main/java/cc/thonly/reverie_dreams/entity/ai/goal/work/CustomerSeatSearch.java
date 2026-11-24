package cc.thonly.reverie_dreams.entity.ai.goal.work;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.HashMap;
import java.util.Map;

/**
 * Keeps a valid seat and bounds how often an unsuccessful search is repeated.
 */
final class CustomerSeatSearch {
    static final int RADIUS = 8;
    static final int SEARCH_INTERVAL = 20;
    static final int REJECT_INTERVAL = 200;
    private final Map<BlockPos, Long> rejected = new HashMap<>();
    @Nullable
    private BlockPos target;
    private long nextSearchTick;

    void reset(long gameTime, int phase) {
        this.target = null;
        this.rejected.clear();
        this.nextSearchTick = gameTime + Math.floorMod(phase, SEARCH_INTERVAL);
    }

    @Nullable
    BlockPos target() {
        return this.target;
    }

    void reject(long gameTime, BlockPos pos) {
        this.rejected.put(pos.immutable(), gameTime + REJECT_INTERVAL);
        this.target = null;
        this.nextSearchTick = gameTime;
    }

    @Nullable
    BlockPos find(long gameTime, BlockPos center, Predicate<BlockPos> available) {
        this.rejected.entrySet().removeIf(entry -> entry.getValue() <= gameTime);
        if (this.target != null && inRange(this.target, center) && available.test(this.target)) {
            return this.target;
        }
        this.target = null;
        if (gameTime < this.nextSearchTick) {
            return null;
        }
        this.nextSearchTick = gameTime + SEARCH_INTERVAL;
        double nearestDistance = Double.POSITIVE_INFINITY;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = center.getX() - RADIUS; x <= center.getX() + RADIUS; x++) {
            for (int y = center.getY() - RADIUS; y <= center.getY() + RADIUS; y++) {
                for (int z = center.getZ() - RADIUS; z <= center.getZ() + RADIUS; z++) {
                    cursor.set(x, y, z);
                    double distance = cursor.distSqr(center);
                    if (distance < nearestDistance && !this.rejected.containsKey(cursor) && available.test(cursor)) {
                        this.target = cursor.immutable();
                        nearestDistance = distance;
                    }
                }
            }
        }
        return this.target;
    }

    private static boolean inRange(BlockPos pos, BlockPos center) {
        return Math.abs(pos.getX() - center.getX()) <= RADIUS
                && Math.abs(pos.getY() - center.getY()) <= RADIUS
                && Math.abs(pos.getZ() - center.getZ()) <= RADIUS;
    }
}
