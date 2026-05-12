package cc.thonly.reverie_dreams.server.component;

import cc.thonly.reverie_dreams.api.player.BasePlayerComponentManager;
import cc.thonly.reverie_dreams.networking.payload.PlayerComponentUpdatePacket;
import cc.thonly.reverie_dreams.registry.content.PlayerComponentRegistry;
import cc.thonly.reverie_dreams.server.player.PlayerComponent;
import cc.thonly.reverie_dreams.server.player.PlayerComponentInitializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.blay09.mods.balm.Balm;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Getter
@SuppressWarnings({"unchecked", "rawtypes"})
@Slf4j
public final class ServerPlayerComponentManager extends BasePlayerComponentManager {
    private static final ServerPlayerComponentManager SERVER_INSTANCE = new ServerPlayerComponentManager();
    private MinecraftServer server;

    public static BasePlayerComponentManager serverAccess() {
        return SERVER_INSTANCE;
    }

    public static void tickByServer(MinecraftServer server) {
        PlayerList playerManager = server.getPlayerList();
        BasePlayerComponentManager componentManager = serverAccess();
        for (ServerPlayer player : playerManager.getPlayers()) {
            try {
                String uuid = player.getStringUUID();
                List<ComponentEntry> componentEntries =
                        componentManager.getDataList().getOrDefault(uuid, Collections.emptyList());

                for (ComponentEntry entry : componentEntries) {
                    PlayerComponent<?> component = componentManager.getOrCreatePlayerComponent(player, entry.key());
                    component.tick(server, false);
                }
            } catch (Exception err) {
                log.error("Player Data Component Tick task execution failed: ", err);
            }
        }
    }

    public void updatePlayerData() {
        List<PlayerComponentUpdatePacket> packets = new ArrayList<>();
        this.dataList.forEach((uuidString, componentEntries) -> {
            UUID uuid = UUID.fromString(uuidString);
            ServerPlayer player = this.getServer().getPlayerList().getPlayer(uuid);
            if (player == null) {
                return;
            }
            List<PlayerComponent> list = new ArrayList<>();
            for (ComponentEntry componentEntry : componentEntries) {
                PlayerComponent component = componentEntry.component();
                list.add(component);
            }
            CompoundTag data = this.encodes(player, list);
            PlayerComponentUpdatePacket packet = new PlayerComponentUpdatePacket(uuid, data);
            packets.add(packet);
        });
        for (PlayerComponentUpdatePacket packet : packets) {
            Balm.networking().sendToAll(this.server, packet);
        }
    }

    @Override
    public void saveAll() {
        this.dataList.forEach((uuidString, componentEntries) -> {
            Path filePath = this.withPath(uuidString);
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
                log.error("Failed to save player data for {}", uuidString, e);
            }
        });
    }

    @Override
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
                                Class<PlayerComponent> castedClass = (Class<PlayerComponent>) clazz;

                                PlayerComponentInitializer<?> initializer = PlayerComponentRegistry.classMap().get(castedClass);

                                if (initializer == null) {
                                    log.warn("No initializer registered for {}", typeKey);
                                    continue;
                                }

                                Codec<?> codec = initializer.getCodec();
                                DataResult<?> decoded = codec.parse(this.registryOps, entry.getValue());

                                decoded.resultOrPartial(err -> log.error("Failed to decode {}: {}", typeKey, err))
                                        .ifPresent(component -> entries.add(new ComponentEntry(castedClass, (PlayerComponent) component)));
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

    @Override
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

}
