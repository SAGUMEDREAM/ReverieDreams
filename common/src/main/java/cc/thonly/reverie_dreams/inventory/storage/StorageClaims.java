package cc.thonly.reverie_dreams.inventory.storage;

import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Server-thread leases; weak world keys and expiry also cover unload/death without a stop callback.
 */
public final class StorageClaims {
    private static final Map<Object, StorageClaims> WORLDS = new WeakHashMap<>();
    private static final long LEASE_TICKS = 200;
    private final Map<BlockPos, Claim> claims = new HashMap<>();

    private record Claim(UUID owner, long expires) {
    }

    public static StorageClaims forWorld(Object world) {
        return WORLDS.computeIfAbsent(world, ignored -> new StorageClaims());
    }

    public boolean available(BlockPos pos, UUID owner, long now) {
        var claim = claims.get(pos);
        if (claim != null && claim.expires <= now) {
            claims.remove(pos);
            claim = null;
        }
        return claim == null || claim.owner.equals(owner);
    }

    public boolean acquire(List<BlockPos> positions, UUID owner, long now) {
        claims.entrySet().removeIf(e -> e.getValue().expires <= now);
        if (positions.stream().anyMatch(p -> !available(p, owner, now)))
            return false;
        for (var pos : positions)
            claims.put(pos.immutable(), new Claim(owner, now + LEASE_TICKS));
        return true;
    }

    public void release(UUID owner) {
        claims.values().removeIf(c -> c.owner.equals(owner));
    }
}
