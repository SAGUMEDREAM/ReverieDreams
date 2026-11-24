package cc.thonly.reverie_dreams.restaurant;

import cc.thonly.reverie_dreams.block.entity.RestaurantBlockEntity;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/** Short-lived per-level cache so every chef and waiter shares one restaurant scan. */
public final class RestaurantSceneCache {
    private static final int LIFETIME_TICKS = 20;
    private static final Map<ServerLevel, Map<UUID, Entry>> CACHE = new WeakHashMap<>();

    private RestaurantSceneCache() {
    }

    public static RestaurantScene get(ServerLevel level, RestaurantBlockEntity bell) {
        long now = level.getGameTime();
        Map<UUID, Entry> shops = CACHE.computeIfAbsent(level, ignored -> new HashMap<>());
        Entry entry = shops.get(bell.id());
        if (entry != null && entry.expires() > now) {
            return entry.scene();
        }
        RestaurantScene scene = RestaurantScene.scan(level, bell);
        shops.put(bell.id(), new Entry(scene, now + LIFETIME_TICKS));
        shops.entrySet().removeIf(cached -> cached.getValue().expires() <= now);
        return scene;
    }

    private record Entry(RestaurantScene scene, long expires) {
    }
}
