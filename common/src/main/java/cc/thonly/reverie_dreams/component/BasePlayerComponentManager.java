package cc.thonly.reverie_dreams.component;

import cc.thonly.reverie_dreams.api.player.PlayerComponent;
import cc.thonly.reverie_dreams.api.player.PlayerComponentKey;
import cc.thonly.reverie_dreams.api.player.PlayerComponentManager;
import cc.thonly.reverie_dreams.registry.content.PlayerComponentRegistry;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.nio.file.Path;
import java.util.*;

/**
 * Shared in-memory storage for player components, used by both the server and the client manager.
 *
 * <p>Data is keyed by {@link UUID} and then by component {@link Identifier}, so lookups are O(1)
 * and a component decoded twice replaces the previous instance instead of piling up duplicates.</p>
 *
 * <p>Persistence and synchronization are side specific and implemented by the subclasses;
 * {@link #saveAll()}, {@link #loadAll()} and {@link #onLoad(MinecraftServer)} are only supported on
 * the server.</p>
 */
@Getter
@Slf4j
@SuppressWarnings({"unchecked", "rawtypes"})
public class BasePlayerComponentManager implements PlayerComponentManager {
    protected static final Gson GSON = new Gson();

    /**
     * player uuid -> (component id -> component)
     */
    protected final Map<UUID, Map<Identifier, PlayerComponent>> dataList = new Object2ObjectOpenHashMap<>();

    /**
     * Players whose data changed since the last flush. Only these get rewritten and synchronized.
     */
    protected final Set<UUID> dirtyPlayers = new ObjectOpenHashSet<>();

    protected Path baseSavePath;
    protected RegistryOps<JsonElement> registryOps;

    protected BasePlayerComponentManager() {
    }

    @Override
    public <T extends PlayerComponent> PlayerComponent<T> createComponent(Player player, Class<T> type) {
        return this.createComponent(player, this.require(type));
    }

    public <T extends PlayerComponent> PlayerComponent<T> createComponent(Player player, PlayerComponentKey<T> key) {
        PlayerComponent<T> component = key.initializer().create(player);
        component.setPlayer(player);
        this.put(player.getUUID(), key.id(), component);
        this.invoke("onLoad", component, component::onLoad);
        this.markDirty(player.getUUID());
        return component;
    }

    @Override
    public <T extends PlayerComponent> void readComponent(Player player, Class<T> type, PlayerComponent<T> component) {
        this.readComponent(player, this.require(type).id(), component);
    }

    /**
     * Attaches a decoded component to a player and stores it under its id, replacing any previous
     * instance with the same id.
     */
    public void readComponent(Player player, Identifier id, PlayerComponent<?> component) {
        boolean attached = component.getPlayer() != player;
        if (attached) {
            component.setPlayer(player);
        }
        this.put(player.getUUID(), id, component);
        if (attached) {
            this.invoke("onLoad", component, component::onLoad);
        }
        this.invoke("onDataLoaded", component, component::onDataLoaded);
    }

    /**
     * Stores a decoded component for a player that is currently offline.
     */
    protected void storeDecoded(UUID playerId, Identifier id, PlayerComponent<?> component) {
        this.put(playerId, id, component);
        this.invoke("onDataLoaded", component, component::onDataLoaded);
    }

    @Override
    public <T extends PlayerComponent> boolean hasComponent(Player player, Class<T> type) {
        return this.getComponent(player, type) != null;
    }

    @Override
    public <T extends PlayerComponent> PlayerComponent<T> getComponent(Player player, Class<T> type) {
        PlayerComponentKey<T> key = PlayerComponentRegistry.get(type);
        return key == null ? null : this.getComponent(player.getUUID(), key.id());
    }

    public <T extends PlayerComponent> PlayerComponent<T> getComponent(UUID playerId, Identifier id) {
        Map<Identifier, PlayerComponent> components = this.dataList.get(playerId);
        return components == null ? null : (PlayerComponent<T>) components.get(id);
    }

    @Override
    public <T extends PlayerComponent> PlayerComponent<T> getOrCreatePlayerComponent(Player player, Class<T> type) {
        PlayerComponentKey<T> key = this.require(type);
        PlayerComponent<T> component = this.getComponent(player.getUUID(), key.id());
        if (component == null) {
            return this.createComponent(player, key);
        }
        this.attachIfNeeded(player, component);
        return component;
    }

    @Override
    public void markDirty(UUID playerId) {
        this.dirtyPlayers.add(playerId);
    }

    public Path withPath(UUID playerId) {
        return this.baseSavePath.resolve(playerId + ".json");
    }

    public Path withPath(ServerPlayer player) {
        return this.withPath(player.getUUID());
    }

    /**
     * Encodes the given components into the network payload format.
     */
    public CompoundTag encodes(UUID playerId, Map<Identifier, PlayerComponent> components) {
        CompoundTag root = new CompoundTag();
        root.putString("player_id", playerId.toString());
        ListTag encoded = new ListTag();
        components.forEach((id, component) -> {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", id.toString());
            DataResult<Tag> result = (DataResult<Tag>) ((Codec) component.getCodec()).encodeStart(NbtOps.INSTANCE, component);
            result.resultOrPartial(message -> log.warn("Failed to encode player component {} of {}: {}", id, playerId, message))
                    .ifPresent(nbt -> {
                        tag.put("data", nbt);
                        encoded.add(tag);
                    });
        });
        root.put("components", encoded);
        return root;
    }

    /**
     * Decodes all components of a network payload.
     */
    public Map<Identifier, PlayerComponent> decodes(CompoundTag tag) {
        Map<Identifier, PlayerComponent> result = new Object2ObjectLinkedOpenHashMap<>();
        ListTag encoded = tag.getListOrEmpty("components");
        for (int i = 0, size = encoded.size(); i < size; i++) {
            CompoundTag componentTag = encoded.getCompoundOrEmpty(i);
            if (componentTag.isEmpty()) {
                continue;
            }
            Identifier id = Identifier.tryParse(componentTag.getStringOr("id", ""));
            if (id == null) {
                log.warn("Ignoring player component payload with an invalid id");
                continue;
            }
            PlayerComponentKey<?> key = PlayerComponentRegistry.get(id);
            if (key == null) {
                log.warn("Ignoring unknown player component payload: {}", id);
                continue;
            }
            Tag data = componentTag.get("data");
            if (data == null) {
                continue;
            }
            DataResult<Object> decoded = (DataResult<Object>) ((Codec) key.initializer().getCodec()).parse(NbtOps.INSTANCE, data);
            decoded.resultOrPartial(message -> log.error("Failed to decode player component {}: {}", id, message))
                    .ifPresent(component -> result.put(id, (PlayerComponent) component));
        }
        return result;
    }

    protected <T extends PlayerComponent> PlayerComponentKey<T> require(Class<T> type) {
        return PlayerComponentRegistry.getOrThrow(type);
    }

    protected void put(UUID playerId, Identifier id, PlayerComponent<?> component) {
        this.dataList.computeIfAbsent(playerId, _ -> new Object2ObjectLinkedOpenHashMap<>())
                .put(id, (PlayerComponent) component);
    }

    protected void attachIfNeeded(Player player, PlayerComponent<?> component) {
        if (component.getPlayer() == player) {
            return;
        }
        component.setPlayer(player);
        this.invoke("onLoad", component, component::onLoad);
    }

    protected void invoke(String action, PlayerComponent<?> component, Runnable task) {
        try {
            task.run();
        } catch (Exception err) {
            Player player = component.getPlayer();
            log.error("Player component {} failed during {} (player={})",
                    component.getClass().getName(), action,
                    player == null ? "unbound" : player.getGameProfile().name(), err);
        }
    }

    @Override
    public void saveAll() {
        throw new UnsupportedOperationException("Only the server player component manager can save data");
    }

    @Override
    public void loadAll() {
        throw new UnsupportedOperationException("Only the server player component manager can load data");
    }

    @Override
    public void onLoad(MinecraftServer server) {
        throw new UnsupportedOperationException("Only the server player component manager can load data");
    }

}
