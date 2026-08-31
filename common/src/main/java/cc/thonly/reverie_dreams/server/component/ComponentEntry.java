package cc.thonly.reverie_dreams.server.component;

import cc.thonly.reverie_dreams.server.player.PlayerComponent;

@SuppressWarnings("rawtypes")
public record ComponentEntry(Class<? extends PlayerComponent> key, PlayerComponent component) {
}
