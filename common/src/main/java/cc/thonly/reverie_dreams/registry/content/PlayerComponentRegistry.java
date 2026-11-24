package cc.thonly.reverie_dreams.registry.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.player.PlayerComponent;
import cc.thonly.reverie_dreams.api.player.PlayerComponentInitializer;
import cc.thonly.reverie_dreams.api.player.PlayerComponentKey;
import cc.thonly.reverie_dreams.server.player.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.Set;

/**
 * Player component registry.
 *
 * <p>Components are identified by an {@link Identifier} (persisted, network stable) and by their
 * {@link Class} (used by the java API and by legacy auto-registrations).</p>
 */
@Slf4j
@SuppressWarnings({"unchecked", "rawtypes"})
public class PlayerComponentRegistry {
    private static final Map<Identifier, PlayerComponentKey<?>> BY_ID = new Object2ObjectOpenHashMap<>();
    private static final Map<Class<?>, PlayerComponentKey<?>> BY_CLASS = new Object2ObjectOpenHashMap<>();

    public static Set<PlayerComponentKey<?>> components() {
        return Set.copyOf(BY_ID.values());
    }

    public static <T extends PlayerComponent> PlayerComponentKey<T> register(
            Identifier id,
            Class<T> type,
            PlayerComponentInitializer<T> initializer,
            boolean shared
    ) {
        if (BY_ID.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate player component id: " + id);
        }
        if (BY_CLASS.containsKey(type)) {
            throw new IllegalArgumentException("Duplicate player component type: " + type.getName());
        }
        PlayerComponentKey<T> key = new PlayerComponentKey<>(id, type, initializer, shared);
        BY_ID.put(id, key);
        BY_CLASS.put(type, key);
        return key;
    }

    public static <T extends PlayerComponent> PlayerComponentKey<T> register(
            Identifier id,
            Class<T> type,
            PlayerComponentInitializer<T> initializer
    ) {
        return register(id, type, initializer, false);
    }

    /**
     * Legacy entry point kept for {@code ReverieDreamsPluginLoader} and third party plugins:
     * the id is derived from the class name, so a renamed class is a new component.
     * Prefer {@link #register(Identifier, Class, PlayerComponentInitializer, boolean)}.
     */
    @Deprecated
    public static <T extends PlayerComponent> void registerComponentType(Class<T> key, PlayerComponentInitializer<T> initializer) {
        if (BY_CLASS.containsKey(key)) {
            log.warn("Player component type {} is already registered, ignore duplicate registration", key.getName());
            return;
        }
        Identifier id = legacyId(key);
        if (BY_ID.containsKey(id)) {
            log.warn("Player component id {} derived from {} is already taken, ignore registration", id, key.getName());
            return;
        }
        register(id, (Class) key, (PlayerComponentInitializer) initializer, false);
    }

    public static PlayerComponentKey<?> get(Identifier id) {
        return BY_ID.get(id);
    }

    public static <T extends PlayerComponent> PlayerComponentKey<T> get(Class<?> type) {
        return (PlayerComponentKey<T>) BY_CLASS.get(type);
    }

    public static <T extends PlayerComponent> PlayerComponentKey<T> getOrThrow(Class<?> type) {
        PlayerComponentKey<T> key = get(type);
        if (key == null) {
            throw new IllegalStateException("Player component is not registered: " + type.getName());
        }
        return key;
    }

    /**
     * Resolves the class-name key of the pre-Identifier data format, used only while migrating old
     * {@code world/player-component-data/*.json} files.
     */
    public static PlayerComponentKey<?> getByLegacyClassName(String className) {
        try {
            return BY_CLASS.get(Class.forName(className));
        } catch (ClassNotFoundException err) {
            return null;
        }
    }

    public static void registerDefaultComponents() {
        register(ReverieDreams.id("name"), NameComponent.class, new NameComponent.NameComponentInitializer(), true);
        register(ReverieDreams.id("faith"), FaithComponent.class, new FaithComponent.FaithComponentInitializer());
        register(ReverieDreams.id("boundary_return"), BoundaryReturnComponent.class, new BoundaryReturnComponent.BoundaryReturnComponentInitializer());
        register(ReverieDreams.id("faction_reputation"), FactionReputationComponent.class, new FactionReputationComponent.FactionReputationComponentInitializer());

    }

    private static Identifier legacyId(Class<?> type) {
        String name = type.getSimpleName();
        if (name.endsWith("Component")) {
            name = name.substring(0, name.length() - "Component".length());
        }
        StringBuilder path = new StringBuilder();
        for (char character : name.toCharArray()) {
            if (Character.isUpperCase(character)) {
                if (!path.isEmpty()) {
                    path.append('_');
                }
                path.append(Character.toLowerCase(character));
            } else {
                path.append(character);
            }
        }
        return ReverieDreams.id(path.toString());
    }
}
