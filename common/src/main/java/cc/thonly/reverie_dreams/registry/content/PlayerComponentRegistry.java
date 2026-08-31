package cc.thonly.reverie_dreams.registry.content;

import cc.thonly.reverie_dreams.server.player.FaithComponent;
import cc.thonly.reverie_dreams.server.player.NameComponent;
import cc.thonly.reverie_dreams.server.player.PlayerComponent;
import cc.thonly.reverie_dreams.server.player.PlayerComponentInitializer;
import com.mojang.serialization.Codec;
import com.sun.nio.sctp.IllegalUnbindException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unchecked", "rawtypes"})
public class PlayerComponentRegistry {
    static final Map<Class<PlayerComponent<? extends PlayerComponent>>, PlayerComponentInitializer<?>> REGISTRIES = new Object2ObjectOpenHashMap<>();

    public static Set<Map.Entry<Class<PlayerComponent<? extends PlayerComponent>>, PlayerComponentInitializer<?>>> getComponents() {
        return REGISTRIES.entrySet();
    }

    public static <T extends PlayerComponent> PlayerComponentInitializer<T> getComponentTypeOrThrow(Class<? extends PlayerComponent> key) {
        if (!REGISTRIES.containsKey(key)) {
            throw new IllegalUnbindException("Detected unregistered player component type");
        }
        return (PlayerComponentInitializer<T>) REGISTRIES.get(key);
    }

    public static <T extends PlayerComponent> Codec<T> getCodec(Class<? extends PlayerComponent> key) {
        PlayerComponentInitializer<PlayerComponent> componentType = getComponentType(key);
        return componentType != null ? (Codec<T>) componentType.getCodec() : null;
    }

    public static <T extends PlayerComponent> PlayerComponentInitializer<T> getComponentType(Class<? extends PlayerComponent> key) {
        return (PlayerComponentInitializer<T>) REGISTRIES.get(key);
    }

    public static <T extends PlayerComponent> void registerComponentType(Class<T> key, PlayerComponentInitializer<T> initializer) {
        REGISTRIES.put((Class<PlayerComponent<? extends PlayerComponent>>) key, initializer);
    }

    public static void registerDefaultComponents() {
        registerComponentType(NameComponent.class, new NameComponent.NameComponentInitializer());
        registerComponentType(FaithComponent.class, new FaithComponent.FaithComponentInitializer());
    }

    public static Map<
            Class<PlayerComponent<? extends PlayerComponent>>,
            PlayerComponentInitializer<?>> classMap() {
        return Map.copyOf(REGISTRIES);
    }
}
