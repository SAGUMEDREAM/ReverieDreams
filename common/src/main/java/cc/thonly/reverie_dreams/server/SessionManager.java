package cc.thonly.reverie_dreams.server;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {
    private static final Map<UUID, UUID> SESSIONS = new HashMap<>();

    public static void startSession(UUID player, UUID session) {
        SESSIONS.put(player, session);
    }

    public static UUID getSession(UUID player) {
        return SESSIONS.get(player);
    }

    public static void clear(UUID player) {
        SESSIONS.remove(player);
    }

    public static void clear() {
        SESSIONS.clear();
    }
}
