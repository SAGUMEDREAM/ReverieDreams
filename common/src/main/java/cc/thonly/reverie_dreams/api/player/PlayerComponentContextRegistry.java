package cc.thonly.reverie_dreams.api.player;

@SuppressWarnings("rawtypes")
@FunctionalInterface
public interface PlayerComponentContextRegistry {
    <T extends PlayerComponent> void registerComponentType(Class<T> key, PlayerComponentInitializer<T> initializer);
}
