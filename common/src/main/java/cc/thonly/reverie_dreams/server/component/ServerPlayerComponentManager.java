package cc.thonly.reverie_dreams.server.component;

import cc.thonly.reverie_dreams.component.BasePlayerComponentManager;
import cc.thonly.reverie_dreams.networking.payload.PlayerComponentUpdatePacket;
import cc.thonly.reverie_dreams.api.player.PlayerComponent;
import cc.thonly.reverie_dreams.api.player.PlayerComponentKey;
import cc.thonly.reverie_dreams.registry.content.PlayerComponentRegistry;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.architectury.networking.NetworkManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Getter
@SuppressWarnings({"unchecked", "rawtypes"})
@Slf4j
public final class ServerPlayerComponentManager extends BasePlayerComponentManager {
    /**
     * Version of the on disk format ({@code world/player-component-data/<uuid>.json}).
     */
    public static final int DATA_VERSION = 1;
    private static final String COMPONENTS_KEY = "components";
    private static final String LEGACY_COMPONENTS_KEY = "legacy_components";

    private static final ServerPlayerComponentManager SERVER_INSTANCE = new ServerPlayerComponentManager();
    private MinecraftServer server;
    private final Map<UUID, SaveRetry> saveRetries = new HashMap<>();
    private final Map<UUID, JsonObject> retainedRoots = new HashMap<>();
    private final Set<UUID> unreadablePlayers = new HashSet<>();
    private long flushTick;

    private record SaveRetry(int failures, long nextAttempt) {
    }

    public static BasePlayerComponentManager serverAccess() {
        return SERVER_INSTANCE;
    }

    public static void tickByServer(MinecraftServer server) {
        PlayerList playerList = server.getPlayerList();
        for (ServerPlayer player : playerList.getPlayers()) {
            try {
                SERVER_INSTANCE.tickPlayer(player, server);
            } catch (Exception err) {
                log.error("Player Data Component Tick task execution failed: ", err);
            }
        }
        SERVER_INSTANCE.flushDirty();
    }

    /**
     * Rewrites and synchronizes every player that was marked dirty since the last tick.
     */
    public void flushDirty() {
        this.flushTick++;
        if (this.dirtyPlayers.isEmpty()) {
            return;
        }
        Set<UUID> pending = new ObjectOpenHashSet<>(this.dirtyPlayers);
        for (UUID playerId : pending) {
            SaveRetry retry = this.saveRetries.get(playerId);
            if (retry != null && this.flushTick < retry.nextAttempt()) {
                continue;
            }
            try {
                if (!this.saveAndTrack(playerId)) {
                    continue;
                }
                ServerPlayer player = this.getOnlinePlayer(playerId);
                if (player != null) {
                    this.sendPlayerData(player);
                }
            } catch (Exception err) {
                log.error("Failed to flush player component data for {}", playerId, err);
            }
        }
    }

    private void tickPlayer(ServerPlayer player, MinecraftServer server) {
        Map<Identifier, PlayerComponent> components = this.dataList.get(player.getUUID());
        if (components == null || components.isEmpty()) {
            return;
        }
        // Copy first: a component tick may create or replace components.
        for (PlayerComponent<?> component : new ArrayList<>(components.values())) {
            this.attachIfNeeded(player, component);
            component.tick(server, false);
        }
    }

    private void sendPlayerData(ServerPlayer player) {
        if (this.server == null) {
            return;
        }
        Map<Identifier, PlayerComponent> components = this.dataList.get(player.getUUID());
        if (components == null || components.isEmpty()) {
            return;
        }
        Map<Identifier, PlayerComponent> shared = new Object2ObjectLinkedOpenHashMap<>();
        Map<Identifier, PlayerComponent> owned = new Object2ObjectLinkedOpenHashMap<>();
        components.forEach((id, component) -> {
            PlayerComponentKey<?> key = PlayerComponentRegistry.get(id);
            if (key != null && key.shared()) {
                shared.put(id, component);
            } else {
                owned.put(id, component);
            }
        });
        if (!shared.isEmpty()) {
            CompoundTag data = this.encodes(player.getUUID(), shared);
            NetworkManager.sendToPlayers(this.server.getPlayerList().getPlayers(),
                    new PlayerComponentUpdatePacket(player.getUUID(), data));
        }
        if (!owned.isEmpty()) {
            CompoundTag data = this.encodes(player.getUUID(), owned);
            NetworkManager.sendToPlayer(player, new PlayerComponentUpdatePacket(player.getUUID(), data));
        }
    }

    private ServerPlayer getOnlinePlayer(UUID playerId) {
        return this.server == null ? null : this.server.getPlayerList().getPlayer(playerId);
    }

    @Override
    public void saveAll() {
        Set<UUID> pending = new ObjectOpenHashSet<>(this.dataList.keySet());
        pending.addAll(this.dirtyPlayers);
        for (UUID playerId : pending) {
            this.saveAndTrack(playerId);
        }
    }

    private boolean saveAndTrack(UUID playerId) {
        // Remove before encoding: a component may mark itself dirty again while being encoded.
        this.dirtyPlayers.remove(playerId);
        if (this.savePlayer(playerId)) {
            this.saveRetries.remove(playerId);
            return true;
        }
        this.dirtyPlayers.add(playerId);
        SaveRetry previous = this.saveRetries.get(playerId);
        int failures = previous == null ? 1 : Math.min(previous.failures() + 1, 7);
        long delay = Math.min(20L << (failures - 1), 1200L);
        this.saveRetries.put(playerId, new SaveRetry(failures, this.flushTick + delay));
        return false;
    }

    @Override
    public void loadAll() {
        if (this.baseSavePath == null || !Files.isDirectory(this.baseSavePath)) {
            return;
        }
        try (var paths = Files.list(this.baseSavePath)) {
            paths.filter(path -> path.getFileName().toString().endsWith(".json")).forEach(this::loadPlayer);
        } catch (Exception err) {
            log.error("Error loading player component data from {}", this.baseSavePath, err);
        }
    }

    @Override
    public void onLoad(MinecraftServer server) {
        // A reload must not discard data whose previous save failed, even during retry backoff.
        if (!this.dirtyPlayers.isEmpty()) {
            this.saveAll();
            if (!this.dirtyPlayers.isEmpty()) {
                throw new IllegalStateException("Cannot reload player components while changes remain unsaved");
            }
        }
        RegistryAccess.Frozen registryAccess = server.registryAccess();
        this.server = server;
        this.registryOps = registryAccess.createSerializationContext(JsonOps.INSTANCE);
        this.baseSavePath = server.getWorldPath(LevelResource.ROOT).resolve("player-component-data");
        try {
            Files.createDirectories(this.baseSavePath);
        } catch (Exception err) {
            log.error("Can't create player component data directory {}", this.baseSavePath, err);
        }
        this.dataList.clear();
        this.saveRetries.clear();
        this.retainedRoots.clear();
        this.unreadablePlayers.clear();
        this.flushTick = 0;
        this.loadAll();
        // The manager is also created from the server constructor, where the player list may not be
        // ready yet; online players are rebound again when they join.
        PlayerList playerList = server.getPlayerList();
        if (playerList != null) {
            for (ServerPlayer player : playerList.getPlayers()) {
                this.attachPlayer(player);
            }
        }
    }

    /**
     * Rebinds every component of the player, used after loading data from disk.
     */
    public void attachPlayer(ServerPlayer player) {
        Map<Identifier, PlayerComponent> components = this.dataList.get(player.getUUID());
        if (components == null) {
            return;
        }
        for (PlayerComponent<?> component : new ArrayList<>(components.values())) {
            this.attachIfNeeded(player, component);
        }
    }

    protected boolean savePlayer(UUID playerId) {
        // Defaults created after a failed read must never replace the original file.
        if (this.unreadablePlayers.contains(playerId)) {
            return false;
        }
        if (this.registryOps == null || this.baseSavePath == null) {
            log.warn("Player component data storage is not initialized, skipping save for {}", playerId);
            return false;
        }
        try {
            JsonObject retained = this.retainedRoots.get(playerId);
            boolean modern = retained != null && retained.has(COMPONENTS_KEY);
            JsonObject root = modern ? retained.deepCopy() : new JsonObject();
            root.addProperty("version", DATA_VERSION);
            JsonObject componentObject = modern ? root.getAsJsonObject(COMPONENTS_KEY) : new JsonObject();
            JsonObject legacy = modern
                    ? (root.has(LEGACY_COMPONENTS_KEY) ? root.getAsJsonObject(LEGACY_COMPONENTS_KEY) : new JsonObject())
                    : (retained == null ? new JsonObject() : retained.deepCopy());
            Map<Identifier, PlayerComponent> components = this.dataList.get(playerId);
            if (components != null) {
                for (var entry : components.entrySet()) {
                    DataResult<JsonElement> encoded = (DataResult<JsonElement>) ((Codec) entry.getValue().getCodec())
                            .encodeStart(this.registryOps, entry.getValue());
                    // resultOrPartial is unsafe here: partial data must never replace a complete save.
                    if (encoded.error().isPresent()) {
                        throw new IOException("Failed to encode component " + entry.getKey() + ": "
                                + encoded.error().get().message());
                    }
                    componentObject.add(entry.getKey().toString(), encoded.result().orElseThrow(() ->
                            new IllegalStateException("Missing encoded component " + entry.getKey())));
                }
            }
            // Successfully migrated legacy entries now live under their stable ids. Unknown legacy
            // entries remain opaque, so temporarily missing extensions do not lose their data.
            for (String name : new ArrayList<>(legacy.keySet())) {
                PlayerComponentKey<?> key = PlayerComponentRegistry.getByLegacyClassName(name);
                if (key != null && components != null && components.containsKey(key.id())) {
                    legacy.remove(name);
                }
            }
            if (legacy.isEmpty()) root.remove(LEGACY_COMPONENTS_KEY);
            else root.add(LEGACY_COMPONENTS_KEY, legacy);
            root.add(COMPONENTS_KEY, componentObject);
            writeAtomically(this.withPath(playerId), GSON.toJson(root));
            this.retainedRoots.put(playerId, root.deepCopy());
            return true;
        } catch (Exception err) {
            log.error("Failed to save player component data for {}", playerId, err);
            return false;
        }
    }

    private void loadPlayer(Path path) {
        String fileName = path.getFileName().toString();
        UUID playerId;
        try {
            playerId = UUID.fromString(fileName.substring(0, fileName.length() - ".json".length()));
        } catch (Exception err) {
            log.warn("Skipping player component data with an invalid file name: {}", path);
            return;
        }
        try {
            JsonObject root = GSON.fromJson(Files.readString(path), JsonObject.class);
            if (root == null) {
                throw new IOException("Player component file is empty or null");
            }
            Map<Identifier, PlayerComponent> decoded = this.decodePlayer(playerId, root);
            this.retainedRoots.put(playerId, root.deepCopy());
            this.unreadablePlayers.remove(playerId);
            // Keep an entry even for an empty file, so legacy files get rewritten in the new format.
            this.dataList.computeIfAbsent(playerId, _ -> new Object2ObjectLinkedOpenHashMap<>());
            decoded.forEach((id, component) -> this.storeDecoded(playerId, id, component));
        } catch (Exception err) {
            this.unreadablePlayers.add(playerId);
            this.backupUnreadable(path);
            log.error("Failed to load player component data file: {}", path, err);
        }
    }

    private Map<Identifier, PlayerComponent> decodePlayer(UUID playerId, JsonObject root) throws IOException {
        Map<Identifier, PlayerComponent> result = new Object2ObjectLinkedOpenHashMap<>();
        if (root.has("version")) {
            int version = root.get("version").getAsInt();
            if (version < 0 || version > DATA_VERSION) {
                throw new IOException("Unsupported player component data version: " + version);
            }
            if (!root.has(COMPONENTS_KEY)) throw new IOException("Missing components object");
        }
        JsonElement stored = root.get(COMPONENTS_KEY);
        if (stored != null) {
            if (!stored.isJsonObject()) throw new IOException("Invalid components object");
            for (var entry : stored.getAsJsonObject().entrySet()) {
                this.decodeComponent(playerId, result, entry.getKey(), entry.getValue(), false);
            }
            if (root.has(LEGACY_COMPONENTS_KEY)) {
                for (var entry : root.getAsJsonObject(LEGACY_COMPONENTS_KEY).entrySet()) {
                    this.decodeComponent(playerId, result, entry.getKey(), entry.getValue(), true);
                }
            }
            return result;
        }
        // Legacy format: the top level is a "fully qualified class name -> data" map.
        log.info("Migrating legacy player component data of {}", playerId);
        for (var entry : root.entrySet()) {
            this.decodeComponent(playerId, result, entry.getKey(), entry.getValue(), true);
        }
        return result;
    }

    private void decodeComponent(UUID playerId, Map<Identifier, PlayerComponent> result, String rawKey, JsonElement data, boolean legacy) throws IOException {
        PlayerComponentKey<?> key = legacy
                ? PlayerComponentRegistry.getByLegacyClassName(rawKey)
                : PlayerComponentRegistry.get(Identifier.tryParse(rawKey));
        if (key == null) {
            log.warn("Preserving unknown player component {} of {}", rawKey, playerId);
            return;
        }
        if (legacy && result.containsKey(key.id())) return;
        DataResult<Object> decoded = (DataResult<Object>) ((Codec) key.initializer().getCodec()).parse(this.registryOps, data);
        if (decoded.error().isPresent()) {
            throw new IOException("Failed to decode player component " + rawKey + ": " + decoded.error().get().message());
        }
        result.put(key.id(), (PlayerComponent) decoded.result().orElseThrow(() ->
                new IllegalStateException("Missing decoded component " + rawKey)));
    }

    private void backupUnreadable(Path path) {
        Path backup = path.resolveSibling(path.getFileName() + ".unreadable.bak");
        try {
            if (!Files.exists(backup)) Files.copy(path, backup);
        } catch (IOException error) {
            log.error("Cannot back up unreadable player component file {}", path, error);
        }
    }

    /**
     * Writes the file through a temporary file so a crash never leaves a truncated json behind.
     */
    private static void writeAtomically(Path target, String content) throws IOException {
        Path parent = target.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
        Files.writeString(temporary, content, StandardCharsets.UTF_8);
        try {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException err) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

}
