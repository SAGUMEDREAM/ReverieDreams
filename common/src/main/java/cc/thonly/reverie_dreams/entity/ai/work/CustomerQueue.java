package cc.thonly.reverie_dreams.entity.ai.work;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import net.minecraft.core.BlockPos;

import java.util.Objects;

/**
 * FIFO only among customers competing for a given seat. No world/entity retained in entries.
 */
public final class CustomerQueue {
    private static final Map<Object, CustomerQueue> WORLDS = new WeakHashMap<>();

    private record Entry(WorkArea area, long until, UUID shop) {
    }

    private final Map<UUID, Entry> waiting = new LinkedHashMap<>();

    public static synchronized CustomerQueue of(Object world) {
        return WORLDS.computeIfAbsent(world, ignored -> new CustomerQueue());
    }

    public void join(UUID customer, WorkArea area, long now) {
        join(customer, area, now, null);
    }

    public void join(UUID customer, WorkArea area, long now, UUID shop) {
        waiting.values().removeIf(e -> e.until <= now);
        var previous = waiting.get(customer);
        if (previous != null && (!previous.area.equals(area) || !Objects.equals(shop, previous.shop)))
            waiting.remove(customer);
        waiting.put(customer, new Entry(area, now + 100, shop));
    }

    public boolean turn(UUID customer, BlockPos seat, long now) {
        waiting.values().removeIf(e -> e.until <= now);
        var current = waiting.get(customer);
        if (current == null)
            return false;
        for (var entry : waiting.entrySet()) {
            if (entry.getKey().equals(customer))
                return true;
            if (Objects.equals(entry.getValue().shop, current.shop) && entry.getValue().area.contains(seat))
                return false;
        }
        return false;
    }

    public int position(UUID customer, long now) {
        waiting.values().removeIf(e -> e.until <= now);
        var current = waiting.get(customer);
        if (current == null)
            return 0;
        int position = 1;
        for (var entry : waiting.entrySet()) {
            if (entry.getKey().equals(customer))
                return position;
            WorkArea other = entry.getValue().area;
            if (Objects.equals(entry.getValue().shop, current.shop) && Math.abs(other.center().getX() - current.area.center().getX()) <= other.radius() + current.area.radius()
                    && Math.abs(other.center().getZ() - current.area.center().getZ()) <= other.radius() + current.area.radius()
                    && Math.abs(other.center().getY() - current.area.center().getY()) <= other.height() + current.area.height())
                position++;
        }
        return 0;
    }

    public void leave(UUID customer) {
        waiting.remove(customer);
    }
}
