package cc.thonly.reverie_dreams.world.gensokyo.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.DigestOutputStream;
import java.util.HexFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

import cc.thonly.reverie_dreams.world.gensokyo.road.GensokyoRoadNetwork;

import java.util.function.Supplier;

/**
 * 从 mod classpath 读取固定地图元数据与二进制图层。
 * 小地图仍兼容 map.json 内联数组；正式地图使用 heightmap/biomes/surfaces .bin。
 */
public final class GensokyoMapLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger("GensokyoMapLoader");
    private static final Map<Identifier, GensokyoMapData> CACHE = new ConcurrentHashMap<>();
    private static final Map<Identifier, CompletableFuture<GensokyoMapData>> LOADING = new ConcurrentHashMap<>();
    private static final ExecutorService LOAD_EXECUTOR = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "Gensokyo-Map-Preload");
        thread.setDaemon(true);
        return thread;
    });
    private static volatile Path cacheDirectory;

    private GensokyoMapLoader() {
    }

    /**
     * Shared root; each map gets its own namespace/path subdirectory.
     */
    public static void setCacheDirectory(Path directory) {
        cacheDirectory = directory;
    }

    /**
     * Called before createLevels on both integrated and dedicated servers.
     */
    public static void configureSharedCache(Path gameDirectory, Path worldDirectory) {
        setCacheDirectory(GensokyoMapDiskCache.sharedRoot(gameDirectory));
        Path legacy = worldDirectory.resolve("gensokyo-map-cache");
        LOAD_EXECUTOR.execute(() -> GensokyoMapDiskCache.retireLegacy(legacy));
    }

    /**
     * Start loading in the background. Does not block the server thread. If a
     * player enters the dimension while this is running, get() joins this future.
     */
    public static void preloadAsync(Identifier id) {
        if (CACHE.containsKey(id)) {
            return;
        }
        startLoad(id);
    }

    public static GensokyoMapData get(Identifier id) {
        GensokyoMapData data = CACHE.get(id);
        if (data != null) {
            return data;
        }
        CompletableFuture<GensokyoMapData> future = LOADING.get(id);
        if (future == null) {
            future = startLoad(id);
        }
        try {
            data = future.join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            throw new IllegalStateException("Failed to load Gensokyo map " + id, cause);
        }
        GensokyoMapData existing = CACHE.putIfAbsent(id, data);
        return existing == null ? data : existing;
    }

    public static void clearCache() {
        CACHE.clear();
        LOADING.clear();
    }

    private static CompletableFuture<GensokyoMapData> startLoad(Identifier id) {
        return LOADING.computeIfAbsent(id, key -> {
            Path directory = cacheDirectory;
            LOGGER.info("Gensokyo map preload started: {} (cache={})", key, directory);
            CompletableFuture<GensokyoMapData> future = CompletableFuture.supplyAsync(() -> {
                GensokyoMapData data = load(key, directory);
                CACHE.put(key, data);
                LOGGER.info("Gensokyo map preload finished: {}", key);
                return data;
            }, LOAD_EXECUTOR);
            future.whenComplete((data, error) -> {
                if (error != null) {
                    LOADING.remove(key, future);
                    LOGGER.error("Gensokyo map preload failed: {}", key, error);
                }
            });
            return future;
        });
    }

    private static GensokyoMapData load(Identifier id, Path cacheDir) {
        String path = "/data/" + id.getNamespace() + "/" + id.getPath() + "/map.json";
        try (InputStream stream = GensokyoMapLoader.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("Missing Gensokyo map data: " + path);
            }

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            int expected = GensokyoMapValidator.validateHeader(root);
            int[] origin = intArray(root, "origin");
            if (origin.length != 2) {
                throw new IllegalStateException("origin must contain exactly 2 integers");
            }

            JsonObject spawnObject = root.getAsJsonObject("spawn");
            int[] spawnPos = intArray(spawnObject, "pos");
            if (spawnPos.length != 3) {
                throw new IllegalStateException("spawn.pos must contain exactly 3 integers");
            }

            int width = root.get("width").getAsInt();
            int depth = root.get("depth").getAsInt();
            JsonObject layers = root.has("layers") ? root.getAsJsonObject("layers") : null;
            short[] heights;
            byte[] biomes;
            short[] surfaces;
            if (layers != null) {
                Supplier<GensokyoMapDiskCache.Layers> decode = () -> new GensokyoMapDiskCache.Layers(
                        readShortLayer(id, layers, "heightmap", expected, width, depth),
                        readByteLayer(id, layers, "biomes", expected),
                        readSurfaceLayer(id, layers, "surfaces", expected));
                var arrays = cacheDir == null ? decode.get()
                        : GensokyoMapDiskCache.load(cacheDir, id, sourceStamp(id, layers), expected, decode);
                heights = arrays.heights();
                biomes = arrays.biomes();
                surfaces = arrays.surfaces();
            } else {
                heights = shortArray(root, "heights", expected);
                biomes = byteArray(root, "biomes", expected);
                byte[] surfaceBytes = byteArray(root, "surfaces", expected);
                surfaces = new short[expected];
                for (int i = 0; i < expected; i++) {
                    surfaces[i] = (short) (surfaceBytes[i] & 0xFF);
                }
            }

            GensokyoMapData data = new GensokyoMapData(
                    root.get("format_version").getAsInt(),
                    root.get("map_version").getAsInt(),
                    Identifier.parse(root.get("id").getAsString()),
                    width,
                    depth,
                    origin[0],
                    origin[1],
                    root.get("min_y").getAsInt(),
                    root.get("world_height").getAsInt(),
                    root.get("sea_level").getAsInt(),
                    root.has("dirt_depth") ? root.get("dirt_depth").getAsInt() : 3,
                    root.has("biome_transition") ? root.get("biome_transition").getAsInt() : 0,
                    new GensokyoMapData.Spawn(
                            new BlockPos(spawnPos[0], spawnPos[1], spawnPos[2]),
                            floatOr(spawnObject, "yaw", 0.0F),
                            floatOr(spawnObject, "pitch", 0.0F)
                    ),
                    idList(root, "biome_palette"),
                    blockStateList(root, "surface_palette"),
                    heights,
                    biomes,
                    surfaces,
                    anchorList(root),
                    undergroundList(root),
                    GensokyoRoadNetwork.decode(
                            root.has("road_network") ? root.getAsJsonObject("road_network") : null,
                            width, depth, origin[0], origin[1], root.get("min_y").getAsInt(), root.get("world_height").getAsInt())
            );
            GensokyoMapValidator.validate(data);
            return data;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read Gensokyo map data: " + path, e);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Invalid Gensokyo map data: " + path, e);
        }
    }

    private static String sourceStamp(Identifier id, JsonObject layers) {
        String base = "/data/" + id.getNamespace() + "/" + id.getPath() + "/";
        var digest = GensokyoMapDiskCache.sha256();
        List<String> files = new ArrayList<>();
        files.add("map.json");
        for (String key : List.of("heightmap", "biomes", "surfaces"))
            files.add(layerFileName(layers, key));
        for (String file : files) {
            digest.update(file.getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            try (InputStream input = GensokyoMapLoader.class.getResourceAsStream(base + file)) {
                if (input == null)
                    throw new IOException("Missing map resource: " + base + file);
                input.transferTo(new DigestOutputStream(OutputStream.nullOutputStream(), digest));
            } catch (IOException e) {
                throw new IllegalStateException("Failed to fingerprint map resource " + base + file, e);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static short[] readShortLayer(Identifier id, JsonObject layers, String key, int expected, int width, int depth) {
        String filename = layerFileName(layers, key);
        if (Math.multiplyExact(width, depth) != expected) {
            throw new IllegalStateException("Heightmap dimensions do not match its cell count");
        }
        try (InputStream input = openLayer(id, filename)) {
            return GensokyoLayerDecoder.readShorts(input, expected, filename.contains(".delta") ? width : 0);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to decode map layer " + key + " (" + filename + ")", e);
        }
    }

    private static byte[] readByteLayer(Identifier id, JsonObject layers, String key, int expected) {
        String filename = layerFileName(layers, key);
        try (InputStream input = openLayer(id, filename)) {
            return GensokyoLayerDecoder.readBytes(input, expected, filename.contains(".pal4"));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to decode map layer " + key + " (" + filename + ")", e);
        }
    }

    private static short[] readSurfaceLayer(Identifier id, JsonObject layers, String key, int expected) {
        String filename = layerFileName(layers, key);
        try (InputStream input = openLayer(id, filename)) {
            return GensokyoLayerDecoder.readSurfaces(input, expected, filename.contains(".pal4"));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to decode map layer " + key + " (" + filename + ")", e);
        }
    }

    private static String layerFileName(JsonObject layers, String key) {
        if (!layers.has(key)) {
            throw new IllegalStateException("Missing layer entry: " + key);
        }
        String filename = layers.get(key).getAsString();
        if (filename.isBlank() || filename.contains("..") || filename.startsWith("/")) {
            throw new IllegalStateException("Invalid layer filename: " + filename);
        }
        return filename;
    }

    private static InputStream openLayer(Identifier id, String filename) throws IOException {
        String path = "/data/" + id.getNamespace() + "/" + id.getPath() + "/" + filename;
        InputStream stream = GensokyoMapLoader.class.getResourceAsStream(path);
        if (stream == null) {
            throw new IOException("Missing Gensokyo map layer: " + path);
        }
        try {
            return filename.endsWith(".gz") ? new GZIPInputStream(stream, 64 * 1024)
                    : new BufferedInputStream(stream, 64 * 1024);
        } catch (IOException e) {
            try {
                stream.close();
            } catch (IOException closeError) {
                e.addSuppressed(closeError);
            }
            throw e;
        }
    }

    private static short[] shortArray(JsonObject object, String key, int expected) {
        int[] values = intArray(object, key);
        if (values.length != expected) {
            throw new IllegalStateException(key + " length mismatch: expected " + expected + ", got " + values.length);
        }
        short[] result = new short[expected];
        for (int i = 0; i < values.length; i++) {
            int value = values[i];
            if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
                throw new IllegalStateException(key + "[" + i + "]=" + value + " does not fit in a short");
            }
            result[i] = (short) value;
        }
        return result;
    }

    private static byte[] byteArray(JsonObject object, String key, int expected) {
        int[] values = intArray(object, key);
        if (values.length != expected) {
            throw new IllegalStateException(key + " length mismatch: expected " + expected + ", got " + values.length);
        }
        byte[] result = new byte[expected];
        for (int i = 0; i < values.length; i++) {
            int value = values[i];
            if (value < 0 || value > 255) {
                throw new IllegalStateException(key + "[" + i + "]=" + value + " is outside 0..255");
            }
            result[i] = (byte) value;
        }
        return result;
    }

    private static int[] intArray(JsonObject object, String key) {
        JsonArray array = object.getAsJsonArray(key);
        int[] result = new int[array.size()];
        for (int i = 0; i < array.size(); i++) {
            result[i] = array.get(i).getAsInt();
        }
        return result;
    }

    private static List<Identifier> idList(JsonObject object, String key) {
        JsonArray array = object.getAsJsonArray(key);
        List<Identifier> result = new ArrayList<>(array.size());
        for (JsonElement element : array) {
            result.add(Identifier.parse(element.getAsString()));
        }
        return List.copyOf(result);
    }

    private static List<BlockState> blockStateList(JsonObject object, String key) {
        JsonArray array = object.getAsJsonArray(key);
        List<BlockState> result = new ArrayList<>(array.size());
        for (JsonElement element : array) {
            result.add(GensokyoBlockStates.parse(element.getAsString()));
        }
        return List.copyOf(result);
    }

    private static Map<Long, List<GensokyoMapData.UndergroundBlock>> undergroundList(JsonObject root) {
        if (!root.has("underground")) {
            return Map.of();
        }
        String filename = root.get("underground").getAsString();
        if (filename.isBlank() || filename.contains("..") || filename.startsWith("/")) {
            throw new IllegalStateException("Invalid underground filename: " + filename);
        }
        Identifier mapId = Identifier.parse(root.get("id").getAsString());
        String path = "/data/" + mapId.getNamespace() + "/" + mapId.getPath() + "/" + filename;
        try (InputStream stream = GensokyoMapLoader.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("Missing Gensokyo underground map: " + path);
            }
            JsonObject object = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonArray blocks = object.has("blocks") ? object.getAsJsonArray("blocks") : new JsonArray();
            int[] origin = intArray(root, "origin");
            Map<Long, List<GensokyoMapData.UndergroundBlock>> result = new HashMap<>();
            for (JsonElement element : blocks) {
                JsonObject entry = element.getAsJsonObject();
                int[] pos = intArray(entry, "pos");
                if (pos.length != 3) {
                    throw new IllegalStateException("underground block pos must contain exactly 3 integers");
                }
                String blockText = entry.get("block").getAsString();
                BlockState state = GensokyoBlockStates.parse(blockText);
                int worldX = origin[0] + pos[0];
                int worldY = pos[1];
                int worldZ = origin[1] + pos[2];
                long key = ChunkPos.pack(worldX >> 4, worldZ >> 4);
                result.computeIfAbsent(key, ignored -> new ArrayList<>())
                        .add(new GensokyoMapData.UndergroundBlock(worldX, worldY, worldZ, state));
            }
            Map<Long, List<GensokyoMapData.UndergroundBlock>> immutable = new HashMap<>();
            result.forEach((key, value) -> immutable.put(key, List.copyOf(value)));
            return Map.copyOf(immutable);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read Gensokyo underground map: " + path, e);
        }
    }

    private static List<GensokyoMapData.Anchor> anchorList(JsonObject root) {
        if (!root.has("anchors")) {
            return List.of();
        }
        JsonArray array = root.getAsJsonArray("anchors");
        List<GensokyoMapData.Anchor> result = new ArrayList<>(array.size());
        for (JsonElement element : array) {
            JsonObject anchor = element.getAsJsonObject();
            int[] pos = intArray(anchor, "pos");
            if (pos.length != 3) {
                throw new IllegalStateException("anchor.pos must contain exactly 3 integers");
            }
            result.add(new GensokyoMapData.Anchor(
                    Identifier.parse(anchor.get("id").getAsString()),
                    stringOr(anchor, "type", "template"),
                    optionalIdentifier(anchor, "template"),
                    optionalIdentifier(anchor, "structure"),
                    new BlockPos(pos[0], pos[1], pos[2]),
                    stringOr(anchor, "rotation", "none"),
                    stringOr(anchor, "mirror", "none"),
                    floatOr(anchor, "integrity", 1.0F),
                    floatOr(anchor, "scale", 1.0F)
            ));
        }
        return List.copyOf(result);
    }

    @Nullable
    private static Identifier optionalIdentifier(JsonObject object, String key) {
        return object.has(key) ? Identifier.parse(object.get(key).getAsString()) : null;
    }

    private static String stringOr(JsonObject object, String key, String fallback) {
        return object.has(key) ? object.get(key).getAsString() : fallback;
    }

    private static float floatOr(JsonObject object, String key, float fallback) {
        return object.has(key) ? object.get(key).getAsFloat() : fallback;
    }
}
