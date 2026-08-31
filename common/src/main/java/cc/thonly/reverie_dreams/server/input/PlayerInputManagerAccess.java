package cc.thonly.reverie_dreams.server.input;

import cc.thonly.reverie_dreams.server.InputKey;
import net.minecraft.server.level.ServerPlayer;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerInputManagerAccess implements cc.thonly.reverie_dreams.api.player.PlayerInputManagerAccess {
    private static PlayerInputManagerAccess INSTANCE;

    public static cc.thonly.reverie_dreams.api.player.PlayerInputManagerAccess getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerInputManagerAccess();
        }
        return INSTANCE;
    }

    private final Map<UUID, EnumMap<InputKey, Boolean>> current = new HashMap<>();
    private final Map<UUID, EnumMap<InputKey, Boolean>> previous = new HashMap<>();

    public synchronized void setKey(ServerPlayer player, InputKey key, boolean down) {
        UUID id = player.getUUID();
        current.computeIfAbsent(id, k -> new EnumMap<>(InputKey.class))
                .put(key, down);
    }

    public synchronized void tick() {
        previous.clear();

        for (var entry : current.entrySet()) {
            previous.put(entry.getKey(), new EnumMap<>(entry.getValue()));
        }
    }

    @Override
    public synchronized boolean isKeyDown(ServerPlayer player, InputKey key) {
        var map = current.get(player.getUUID());
        if (map == null) return false;
        return map.getOrDefault(key, false);
    }

    @Override
    public synchronized boolean isKeyPressed(ServerPlayer player, InputKey key) {
        UUID id = player.getUUID();

        var cur = current.get(id);
        var prev = previous.get(id);

        boolean now = cur != null && cur.getOrDefault(key, false);
        boolean before = prev != null && prev.getOrDefault(key, false);

        return now && !before;
    }
}
