package cc.thonly.reverie_dreams.api.player;

import cc.thonly.reverie_dreams.client.component.ClientPlayerComponentManager;
import cc.thonly.reverie_dreams.server.component.ServerPlayerComponentManager;
import cc.thonly.reverie_dreams.server.player.PlayerComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

@SuppressWarnings("rawtypes")
public interface PlayerComponentManager{
    <T extends PlayerComponent> PlayerComponent<T> createComponent(Player player, Class<T> key);

    <T extends PlayerComponent> boolean hasComponent(Player player, Class<T> key);

    <T extends PlayerComponent> void readComponent(Player player, Class<T> key, PlayerComponent<T> component);

    <T extends PlayerComponent> PlayerComponent<T> getComponent(Player player, Class<T> key);

    <T extends PlayerComponent> PlayerComponent<T> getOrCreatePlayerComponent(Player player, Class<T> key);

    void loadAll();

    void saveAll();

    void onLoad(MinecraftServer server);

    static PlayerComponentManager serverAccess() {
        return ServerPlayerComponentManager.serverAccess();
    }

    static PlayerComponentManager clientAccess() {
        return ClientPlayerComponentManager.clientAccess();
    }

}
