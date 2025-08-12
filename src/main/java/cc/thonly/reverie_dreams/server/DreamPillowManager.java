package cc.thonly.reverie_dreams.server;

import cc.thonly.mystias_izakaya.component.CraftingConflict;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Slf4j
public class DreamPillowManager {
    public static final Codec<List<WorldEntry>> CODEC = Codec.list(WorldEntry.CODEC);
    private static final Gson GSON = new Gson();
    private static DreamPillowManager INSTANCE;
    private final MinecraftServer server;
    private final Path savedPath;
    private final Map<World, WorldEntry> data;

    public DreamPillowManager(MinecraftServer server) {
        this.server = server;
        this.savedPath = server.getSavePath(WorldSavePath.ROOT).resolve("data/dream_pillow.json");
        this.data = new Object2ObjectOpenHashMap<>();
        INSTANCE = this;
    }

    public void init() {
        for (ServerWorld world : this.server.getWorlds()) {
            WorldEntry worldEntry = new WorldEntry(world.getRegistryKey(), new ArrayList<>());
            worldEntry.setWorld(world);
            this.data.put(world, worldEntry);
        }
        this.load();
    }

    public WorldEntry get(ServerWorld world) {
        return this.data.get(world);
    }

    public void load() {
        try {
            Files.createDirectories(this.savedPath.getParent());
        } catch (Exception err) {
            log.error("Can't create dir", err);
        }
        if (Files.exists(this.savedPath)) {
            try (BufferedReader reader = Files.newBufferedReader(this.savedPath, StandardCharsets.UTF_8)) {
                JsonElement json = GSON.fromJson(reader, JsonElement.class);
                var result = CODEC.parse(JsonOps.INSTANCE, json);
                Optional<List<WorldEntry>> optional = result.result();
                if (optional.isPresent()) {
                    for (WorldEntry worldEntry : optional.get()) {
                        for (Map.Entry<World, WorldEntry> managerEntry : this.data.entrySet()) {
                            final WorldEntry value = managerEntry.getValue();
                            if (value.registryKey.equals(worldEntry.registryKey)) {
                                value.loadData(worldEntry.signLocations);
                                break;
                            }
                        }
                    }
                }
            } catch (IOException ioException) {
                log.error("Can't load dream pillow data", ioException);
            }
        }
    }

    public void save() {
        try {
            Files.createDirectories(this.savedPath.getParent());
            List<WorldEntry> values = this.data.values().stream().toList();
            DataResult<JsonElement> result = CODEC.encodeStart(JsonOps.INSTANCE, values);
            Optional<JsonElement> optional = result.result();
            if (optional.isPresent()) {
                JsonElement element = optional.get();
                String json = GSON.toJson(element);
                try (BufferedWriter writer = Files.newBufferedWriter(this.savedPath, StandardCharsets.UTF_8)) {
                    writer.write(json);
                }
            }
        } catch (IOException ioException) {
            log.error("Can't save dream pillow data", ioException);
        }
    }

    public static Optional<DreamPillowManager> getInstance() {
        return Optional.ofNullable(INSTANCE);
    }

    @Getter
    public static class WorldEntry {
        public static final Codec<WorldEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                RegistryKey.createCodec(RegistryKeys.WORLD).fieldOf("world").forGetter(WorldEntry::getRegistryKey),
                Codec.list(BlockPos.CODEC).fieldOf("sign_locations").forGetter(WorldEntry::getSignLocations)
        ).apply(instance, WorldEntry::new));

        private RegistryKey<World> registryKey;
        private ServerWorld world;
        private final List<BlockPos> signLocations;

        public WorldEntry(RegistryKey<World> registryKey, Collection<BlockPos> signLocations) {
            this.registryKey = registryKey;
            this.signLocations = new ArrayList<>(new ObjectOpenHashSet<>(signLocations));
        }

        public void add(BlockPos pos) {
            this.signLocations.add(pos);
        }

        public boolean contains(BlockPos pos) {
            return this.signLocations.contains(pos) || this.signLocations.stream().filter(Objects::nonNull).map(BlockPos::asLong).collect(Collectors.toSet()).contains(pos.asLong());
        }

        public void remove(BlockPos pos) {
            long target = pos.asLong();
            this.signLocations.removeIf(p -> p.asLong() == target);
        }

        public void loadData(List<BlockPos> signLocations) {
            Set<BlockPos> merged = new ObjectOpenHashSet<>(this.signLocations);
            merged.addAll(signLocations);
            this.signLocations.clear();
            this.signLocations.addAll(merged);
        }

        public void setWorld(ServerWorld world) {
            if (this.world == null) {
                this.registryKey = world.getRegistryKey();
                this.world = world;
            }
        }
    }
}
