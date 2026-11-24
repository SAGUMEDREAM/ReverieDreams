package cc.thonly.reverie_dreams.entity.ai.work;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Reservations are server-thread leases, never permanent strong references to entities or levels.
 */
public final class WorkClaims {
    private static final Map<Object, WorkClaims> WORLDS = new WeakHashMap<>();
    private final Map<Object, Lease> leases = new HashMap<>();

    private record Lease(UUID owner, long until) {
    }

    public static WorkClaims of(Object level) {
        return WORLDS.computeIfAbsent(level, ignored -> new WorkClaims());
    }

    public boolean available(Object resource, UUID owner, long now) {
        var lease = leases.get(resource);
        return lease == null || lease.until <= now || lease.owner.equals(owner);
    }

    public boolean take(List<?> resources, UUID owner, long now) {
        leases.values().removeIf(l -> l.until <= now);
        if (resources.stream().anyMatch(r -> !available(r, owner, now)))
            return false;
        for (var resource : resources)
            leases.put(resource, new Lease(owner, now + 200));
        return true;
    }

    public void release(UUID owner) {
        leases.values().removeIf(l -> l.owner.equals(owner));
    }
}
