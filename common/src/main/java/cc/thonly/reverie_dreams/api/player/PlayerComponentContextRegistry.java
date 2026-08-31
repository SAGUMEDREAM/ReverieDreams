package cc.thonly.reverie_dreams.api.player;

import cc.thonly.reverie_dreams.server.player.PlayerComponent;
import cc.thonly.reverie_dreams.server.player.PlayerComponentInitializer;

@SuppressWarnings("rawtypes")
@FunctionalInterface
public interface PlayerComponentContextRegistry {
    <T extends PlayerComponent> void registerComponentType(Class<T> key, PlayerComponentInitializer<T> initializer);
}
