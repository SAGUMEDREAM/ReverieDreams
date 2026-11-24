package cc.thonly.reverie_dreams.api.player;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

@SuppressWarnings("rawtypes")
public interface PlayerComponentManager {
    <T extends PlayerComponent> PlayerComponent<T> createComponent(Player player, Class<T> key);

    <T extends PlayerComponent> boolean hasComponent(Player player, Class<T> key);

    <T extends PlayerComponent> void readComponent(Player player, Class<T> key, PlayerComponent<T> component);

    <T extends PlayerComponent> PlayerComponent<T> getComponent(Player player, Class<T> key);

    <T extends PlayerComponent> PlayerComponent<T> getOrCreatePlayerComponent(Player player, Class<T> key);

    /**
     * Marks a player as changed so the next flush only touches that player.
     */
    void markDirty(UUID playerId);

    void loadAll();

    void saveAll();

    void onLoad(MinecraftServer server);

    static PlayerComponentManager serverAccess() {
        return PlayerComponentManagerAccess.serverAccess();
    }

    static PlayerComponentManager clientAccess() {
        return PlayerComponentManagerAccess.clientAccess();
    }

}
