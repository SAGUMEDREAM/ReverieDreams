package cc.thonly.reverie_dreams.server;

import cc.thonly.reverie_dreams.block.entity.RemoteBlockEntity;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Slf4j
public class RemoteSignalManager {
    private static final RemoteSignalManager INSTANCE = new RemoteSignalManager();
    public static final Codec<Map<String, Map<String, Set<String>>>> CODEC =
            Codec.unboundedMap(
                    Codec.STRING,
                    Codec.unboundedMap(
                            Codec.STRING,
                            Codec.unboundedMap(
                                    Codec.STRING,
                                    Codec.BOOL
                            ).xmap(
                                    map -> new HashSet<>(map.keySet()),
                                    set -> {
                                        Map<String, Boolean> map = new HashMap<>();
                                        for (String s : set) map.put(s, true);
                                        return map;
                                    }
                            )
                    )
            );

    // signalName -> token -> Set<uid>
    private final Map<String, Map<String, Set<String>>> map = new HashMap<>(128);

    public synchronized void reloadAll(MinecraftServer server) {
        this.map.clear();

        Path file = server.getWorldPath(LevelResource.ROOT).resolve("remote_signal.json");

        if (!Files.exists(file)) {
            this.saveAll(server);
            return;
        }

        try {
            String json = Files.readString(file);
            JsonElement element = JsonParser.parseString(json);

            CODEC.parse(JsonOps.INSTANCE, element).resultOrPartial(err ->
                    log.error("Decode Error: {}", err)
            ).ifPresent(decoded -> {
                this.map.clear();
                decoded.forEach((signalName, tokenMap) -> {
                    Map<String, Set<String>> newTokenMap = new HashMap<>();
                    tokenMap.forEach((token, uidSet) -> {
                        newTokenMap.put(token, new HashSet<>(uidSet));
                    });
                    this.map.put(signalName, newTokenMap);
                });
            });

        } catch (IOException e) {
            log.error("Error: ", e);
            this.saveAll(server);
        }
    }


    public synchronized void saveAll(MinecraftServer server) {
        Path file = server.getWorldPath(LevelResource.ROOT).resolve("remote_signal.json");

        try {
            JsonElement element = CODEC.encodeStart(JsonOps.INSTANCE, this.map).getOrThrow();

            Files.createDirectories(file.getParent());
            Files.writeString(file, new Gson().toJson(element));

        } catch (IOException e) {
            log.error("Error: ", e);
        }
    }

    public synchronized void setValue(RemoteBlockEntity entity, boolean powered) {
        if (entity.isEmpty()) return;

        Map<String, Set<String>> nameMap =
                this.map.computeIfAbsent(entity.getSignalName(), x -> new HashMap<>(8));
        Set<String> uidSet =
                nameMap.computeIfAbsent(entity.getSignalToken(), x -> new HashSet<>());

        String uid = entity.getUid();

        if (powered) {
            uidSet.add(uid);
        } else {
            uidSet.remove(uid);
            if (uidSet.isEmpty()) {
                nameMap.remove(entity.getSignalToken());
            }
            if (nameMap.isEmpty()) {
                this.map.remove(entity.getSignalName());
            }
        }
    }

    /** 是否有任意 Server 激活该 token */
    public synchronized boolean isOccupied(RemoteBlockEntity entity) {
        Map<String, Set<String>> nameMap = this.map.get(entity.getSignalName());
        if (nameMap == null) return false;

        Set<String> uids = nameMap.get(entity.getSignalToken());
        if (uids == null) return false;

        return !uids.isEmpty();
    }

    /** 方块移除时清理 */
    public synchronized void remove(RemoteBlockEntity entity) {
        if (entity.isEmpty()) return;

        Map<String, Set<String>> nameMap = map.get(entity.getSignalName());
        if (nameMap == null) return;

        Set<String> set = nameMap.get(entity.getSignalToken());
        if (set == null) return;

        set.remove(entity.getUid());
        if (set.isEmpty()) {
            nameMap.remove(entity.getSignalToken());
        }
        if (nameMap.isEmpty()) {
            map.remove(entity.getSignalName());
        }
    }

    public static RemoteSignalManager access() {
        return INSTANCE;
    }
}
