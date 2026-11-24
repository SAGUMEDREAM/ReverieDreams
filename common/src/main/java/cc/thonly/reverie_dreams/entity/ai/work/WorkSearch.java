package cc.thonly.reverie_dreams.entity.ai.work;

import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Incremental scan: a bounded amount of work even when the whole site has no useful targets.
 */
public final class WorkSearch {
    private WorkArea area;
    private int cursor;
    private long nextScan;
    private BlockPos cached;
    private final Map<BlockPos, Long> rejected = new HashMap<>();

    public BlockPos find(WorkArea area, long now, Predicate<BlockPos> loaded, Predicate<BlockPos> matches) {
        if (!area.equals(this.area)) {
            clear();
            this.area = area;
        }
        if (cached != null && area.contains(cached) && available(cached, now) && loaded.test(cached) && matches.test(cached))
            return cached;
        cached = null;
        if (now < nextScan)
            return null;
        rejected.entrySet().removeIf(e -> e.getValue() <= now);
        for (int i = 0; i < Math.min(128, area.size()); i++) {
            var pos = area.position(cursor++);
            if (cursor >= area.size()) {
                cursor = 0;
                nextScan = now + 20;
            }
            if (available(pos, now) && loaded.test(pos) && matches.test(pos))
                return cached = pos.immutable();
            if (cursor == 0)
                break;
        }
        return null;
    }

    public boolean available(BlockPos pos, long now) {
        return rejected.getOrDefault(pos, Long.MIN_VALUE) <= now;
    }

    public void reject(BlockPos pos, long now) {
        rejected.put(pos.immutable(), now + 200);
        cached = null;
        nextScan = now + 10;
    }

    public void invalidate() {
        cached = null;
    }

    public void clear() {
        area = null;
        cursor = 0;
        nextScan = 0;
        cached = null;
        rejected.clear();
    }
}
