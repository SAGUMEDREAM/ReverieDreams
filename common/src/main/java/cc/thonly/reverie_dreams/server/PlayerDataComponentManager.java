package cc.thonly.reverie_dreams.server;

import cc.thonly.reverie_dreams.server.player.FaithComponent;
import cc.thonly.reverie_dreams.server.player.NameComponent;
import cc.thonly.reverie_dreams.server.player.PlayerComponent;
import cc.thonly.reverie_dreams.server.player.PlayerComponentInitializer;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sun.nio.sctp.IllegalUnbindException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Getter
@Slf4j
@SuppressWarnings({"unchecked", "rawtypes"})
public class PlayerDataComponentManager {
    private PlayerDataComponentManager() {
    }

    private static final Gson GSON = new Gson();
    private static final Map<Class<PlayerComponent<? extends PlayerComponent>>, PlayerComponentInitializer<?>> REGISTRIES = new Object2ObjectOpenHashMap<>();
    private static final PlayerDataComponentManager INSTANCE = new PlayerDataComponentManager();
    private MinecraftServer server;
    private Path baseSavePath;
    private RegistryOps<JsonElement> registryOps;
    private final Map<String, List<ComponentEntry>> dataList = new Object2ObjectOpenHashMap<>();

    public static <T extends PlayerComponent> void registerComponentType(Class<T> key, PlayerComponentInitializer<T> initializer) {
        REGISTRIES.put((Class<PlayerComponent<? extends PlayerComponent>>) key, initializer);
    }

    public static void registers() {
        registerComponentType(NameComponent.class, new NameComponent.NameComponentInitializer());
        registerComponentType(FaithComponent.class, new FaithComponent.FaithComponentInitializer());
    }

    public static void tick(MinecraftServer server) {
        PlayerList playerManager = server.getPlayerList();
        PlayerDataComponentManager playerDataComponentManager = getInstance();
        for (ServerPlayer player : playerManager.getPlayers()) {
            try {
                String uuid = player.getStringUUID();
                List<ComponentEntry> componentEntries =
                        playerDataComponentManager.dataList.getOrDefault(uuid, Collections.emptyList());

                for (ComponentEntry entry : componentEntries) {
                    PlayerComponent<?> component = playerDataComponentManager.getOrCreatePlayerComponent(player, entry.key());
                    component.tick(server);
                }
            } catch (Exception err) {
                log.error("Player Data Component Tick task execution failed: ", err);
            }
        }
    }


    public static Set<Map.Entry<Class<PlayerComponent<? extends PlayerComponent>>, PlayerComponentInitializer<?>>> getComponents() {
        return REGISTRIES.entrySet();
    }

    public static <T> PlayerComponentInitializer<T> getComponentType(Class<? extends PlayerComponent> key) {
        if (!REGISTRIES.containsKey(key)) {
            throw new IllegalUnbindException("Detected unregistered player component type");
        }
        return (PlayerComponentInitializer<T>) REGISTRIES.get(key);
    }

    public static PlayerDataComponentManager getInstance() {
        return INSTANCE;
    }

    public <T extends PlayerComponent> PlayerComponent<T> createComponent(ServerPlayer player, Class<T> key) {
        String uuid = player.getStringUUID();
        List<ComponentEntry> componentEntries = (List<ComponentEntry>) this.dataList.computeIfAbsent(uuid, uid -> new ArrayList<>());
        PlayerComponentInitializer initializer = getComponentType(key);
        PlayerComponent<T> playerComponent = initializer.createAndLoad(player);
        componentEntries.add(new ComponentEntry(key, playerComponent));
        return playerComponent;
    }

    public <T extends PlayerComponent> boolean hasComponent(ServerPlayer player, Class<T> key) {
        return this.getComponent(player, key) != null;
    }

    public <T extends PlayerComponent> PlayerComponent<T> getComponent(ServerPlayer player, Class<T> key) {
        String uuid = player.getStringUUID();
        List<ComponentEntry> componentEntries = this.dataList.computeIfAbsent(uuid, uid -> new ArrayList<>());
        ComponentEntry result = null;
        for (ComponentEntry componentEntry : componentEntries) {
            if (componentEntry.key().equals(key)) {
                result = componentEntry;
                break;
            }
        }
        return Optional.ofNullable(result)
                .map(x -> x.component)
                .orElse(null);
    }

    public <T extends PlayerComponent> PlayerComponent<T> getOrCreatePlayerComponent(ServerPlayer player, Class<T> key) {
        PlayerComponent component = this.getComponent(player, key);
        if (component == null) {
            component = createComponent(player, key);
        }
        return component;
    }

    public Path withPath(String playerUuid) {
        return this.baseSavePath.resolve("./%s.json".formatted(playerUuid));
    }

    public Path withPath(UUID uuid) {
        return this.baseSavePath.resolve("./%s.json".formatted(uuid.toString()));
    }

    public Path withPath(ServerPlayer player) {
        return this.baseSavePath.resolve("./%s.json".formatted(player.getStringUUID()));
    }

    public void saveAll() {
        this.dataList.forEach((uuid, componentEntries) -> {
            Path filePath = this.withPath(uuid);
            JsonObject root = new JsonObject();

            for (ComponentEntry componentEntry : componentEntries) {
                String typeKey = componentEntry.key().getTypeName().trim();
                PlayerComponent<?> component = componentEntry.component();

                Codec<?> codec = component.getCodec();
                DataResult<JsonElement> encoded = ((Codec) codec).encodeStart(this.registryOps, component);

                encoded.resultOrPartial(err -> log.error("Failed to encode {}: {}", typeKey, err))
                        .ifPresent(json -> root.add(typeKey, json));
            }

            try {
                Files.writeString(filePath, GSON.toJson(root));
            } catch (Exception e) {
                log.error("Failed to save player data for {}", uuid, e);
            }
        });
    }

    public void loadAll() {
        try (var list = Files.list(this.baseSavePath)) {
            list.filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        String uuid = path.getFileName().toString().replace(".json", "");
                        try {
                            String jsonStr = Files.readString(path);
                            JsonObject root = GSON.fromJson(jsonStr, JsonObject.class);

                            List<ComponentEntry> entries = new ArrayList<>();

                            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                                String typeKey = entry.getKey();

                                Class<?> clazz = Class.forName(typeKey);
                                Class<PlayerComponent<Object>> castedClass = (Class<PlayerComponent<Object>>) clazz;

                                PlayerComponentInitializer<?> initializer = REGISTRIES.get(castedClass);

                                if (initializer == null) {
                                    log.warn("No initializer registered for {}", typeKey);
                                    continue;
                                }

                                Codec<?> codec = initializer.getCodec();
                                DataResult<?> decoded = codec.parse(this.registryOps, entry.getValue());

                                decoded.resultOrPartial(err -> log.error("Failed to decode {}: {}", typeKey, err))
                                        .ifPresent(component -> entries.add(new ComponentEntry(castedClass, (PlayerComponent<Object>) component)));
                            }

                            this.dataList.put(uuid, entries);

                        } catch (Exception e) {
                            log.error("Failed to load player data file: {}", path, e);
                        }
                    });
        } catch (Exception e) {
            log.error("Error loading player data components", e);
        }
    }

    public void onLoad(MinecraftServer server) {
        RegistryAccess.Frozen registryManager = server.registryAccess();
        RegistryOps<JsonElement> registryOps = registryManager.createSerializationContext(JsonOps.INSTANCE);
        this.server = server;
        this.registryOps = registryOps;
        this.baseSavePath = this.server.getWorldPath(LevelResource.ROOT).resolve("./player-component-data/");
        this.dataList.clear();
        if (!Files.exists(this.baseSavePath)) {
            try {
                Files.createDirectory(this.baseSavePath);
            } catch (Exception err) {
                log.error("Can't create player data component directory");
            }
        }
        this.loadAll();
    }

    public record ComponentEntry(Class<? extends PlayerComponent> key, PlayerComponent component) {
    }
}