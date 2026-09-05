package cc.thonly.reverie_dreams.api.player;

import cc.thonly.reverie_dreams.registry.content.PlayerComponentRegistry;
import cc.thonly.reverie_dreams.server.component.ComponentEntry;
import cc.thonly.reverie_dreams.server.player.PlayerComponent;
import cc.thonly.reverie_dreams.server.player.PlayerComponentInitializer;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;

import java.nio.file.Path;
import java.util.*;

@Getter
@Slf4j
@SuppressWarnings({"unchecked", "rawtypes"})
public class BasePlayerComponentManager implements PlayerComponentManager {
    protected BasePlayerComponentManager() {
    }

    protected static final Gson GSON = new Gson();
    protected final Map<String, List<ComponentEntry>> dataList = new Object2ObjectOpenHashMap<>();
    protected Path baseSavePath;
    protected RegistryOps<JsonElement> registryOps;

    @Override
    public <T extends PlayerComponent> PlayerComponent<T> createComponent(Player player, Class<T> key) {
        String uuid = player.getStringUUID();
        List<ComponentEntry> componentEntries = this.dataList.computeIfAbsent(uuid, inst -> new ArrayList<>());
        PlayerComponentInitializer initializer = PlayerComponentRegistry.getComponentType(key);
        PlayerComponent<T> playerComponent = initializer.createAndLoad(player);
        componentEntries.add(new ComponentEntry(key, playerComponent));
        return playerComponent;
    }

    @Override
    public <T extends PlayerComponent> void readComponent(Player player, Class<T> key, PlayerComponent<T> component) {
        List<ComponentEntry> componentEntries = this.dataList.computeIfAbsent(player.getStringUUID(), inst -> new ArrayList<>());
        componentEntries.add(new ComponentEntry(key, component));
    }

    public <T extends PlayerComponent> boolean hasComponent(Player player, Class<T> key) {
        return this.getComponent(player, key) != null;
    }

    @Override
    public <T extends PlayerComponent> PlayerComponent<T> getComponent(Player player, Class<T> key) {
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
                .map(ComponentEntry::component)
                .orElse(null);
    }

    @Override
    public <T extends PlayerComponent> PlayerComponent<T> getOrCreatePlayerComponent(Player player, Class<T> key) {
        PlayerComponent component = this.getComponent(player, key);
        if (component == null) {
            component = createComponent(player, key);
        }
        return component;
    }

    public <T extends PlayerComponent> CompoundTag encodes(
            Player player, List<T> list
    ) {
        CompoundTag root = new CompoundTag();

        root.putString("player_id", player.getStringUUID());
        root.putInt("size", list.size());

        ListTag components = new ListTag();

        for (T component : list) {

            CompoundTag tag = new CompoundTag();

            Class<?> key = component.getClass();
            tag.putString("key", key.getName());

            Codec<T> codec = component.getCodec();

            codec.encodeStart(NbtOps.INSTANCE, component)
                    .result()
                    .ifPresent(nbt -> tag.put("data", nbt));

            components.add(tag);
        }

        root.put("components", components);

        return root;
    }

    public <T extends PlayerComponent> Tuple<String, List<T>> decodes(CompoundTag tag) {

        String playerId = tag.getStringOr("player_id", "unknown");

        List<T> result = new ArrayList<>();

        ListTag components = tag.getListOrEmpty("components");

        for (int i = 0, size = components.size(); i < size; i++) {

            CompoundTag compTag = components.getCompoundOrEmpty(i);
            if (compTag.isEmpty()) continue;

            String keyClassName = compTag.getStringOr("key", "");
            if (keyClassName.isEmpty()) continue;

            try {
                Class<?> keyClass = Class.forName(keyClassName);

                Codec<T> codec = PlayerComponentRegistry
                        .getCodec((Class<? extends PlayerComponent>) keyClass);

                if (codec == null) {
                    log.warn("Unknown codec for component: {}", keyClassName);
                    continue;
                }

                if (!compTag.contains("data")) continue;

                codec.parse(NbtOps.INSTANCE, compTag.get("data"))
                        .result()
                        .ifPresent(result::add);

            } catch (Exception e) {
                log.error("Failed to decode component {}", keyClassName, e);
            }
        }

        return new Tuple<>(playerId, result);
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

    @Override
    public void saveAll() {
        log.error("This operation is not supported.");
    }

    @Override
    public void loadAll() {
        log.error("This operation is not supported.");
    }

    @Override
    public void onLoad(MinecraftServer server) {
        log.error("This operation is not supported.");
    }

}