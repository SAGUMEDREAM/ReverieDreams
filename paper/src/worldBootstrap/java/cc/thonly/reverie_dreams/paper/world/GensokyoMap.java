package cc.thonly.reverie_dreams.paper.world;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Source map metadata and authored overrides; deliberately has no Minecraft/loader types. */
public record GensokyoMap(int formatVersion, int mapVersion, String id, int width, int depth,
                          int originX, int originZ, int minY, int height, int seaLevel,
                          int dirtDepth, int biomeTransition, Spawn spawn,
                          List<String> biomePalette, List<String> surfacePalette,
                          String heightsFile, String biomesFile, String surfacesFile,
                          String undergroundFile, int anchorCount,
                          Map<Long, List<Override>> underground) {
    public static final int NO_SURFACE = Short.MIN_VALUE;
    public record Spawn(int x, int y, int z, float yaw, float pitch) {}
    public record Override(int x, int y, int z, String state) {}

    public static GensokyoMap read(Path directory) throws IOException {
        JsonObject root;
        try (var reader = Files.newBufferedReader(directory.resolve("map.json"), StandardCharsets.UTF_8)) {
            root = JsonParser.parseReader(reader).getAsJsonObject();
        }
        int width = root.get("width").getAsInt(), depth = root.get("depth").getAsInt();
        int minY = root.get("min_y").getAsInt(), height = root.get("world_height").getAsInt();
        int version = root.get("format_version").getAsInt(), mapVersion = root.get("map_version").getAsInt();
        int sea = root.get("sea_level").getAsInt();
        int dirt = integer(root, "dirt_depth", 3), transition = integer(root, "biome_transition", 0);
        if (version != 1 || mapVersion < 1 || width < 1 || depth < 1 || width > 32768 || depth > 32768
                || (long) width * depth > 134217728L || height != 1536 || minY != 0
                || sea < minY || sea >= minY + height || dirt < 0 || dirt > 32 || transition < 0 || transition > 64) {
            throw new IOException("Unsupported/invalid Gensokyo metadata; requires original unscaled 1536-high format1");
        }
        var origin = root.getAsJsonArray("origin");
        var spawn = root.getAsJsonObject("spawn");
        var pos = spawn.getAsJsonArray("pos");
        if (origin.size() != 2 || pos.size() != 3) throw new IOException("Invalid origin/spawn coordinates");
        int ox = origin.get(0).getAsInt(), oz = origin.get(1).getAsInt();
        Spawn start = new Spawn(pos.get(0).getAsInt(), pos.get(1).getAsInt(), pos.get(2).getAsInt(),
                decimal(spawn, "yaw", 0), decimal(spawn, "pitch", 0));
        if (start.x < ox || start.x >= (long) ox + width || start.z < oz || start.z >= (long) oz + depth
                || start.y < minY || start.y > minY + height) throw new IOException("Spawn outside source map bounds");
        List<String> biomes = strings(root, "biome_palette"), surfaces = strings(root, "surface_palette");
        if (biomes.isEmpty() || biomes.size() > 256 || surfaces.isEmpty() || surfaces.size() > 65536)
            throw new IOException("Invalid source palette width");
        JsonObject layers = root.getAsJsonObject("layers");
        if (layers == null) throw new IOException("This bounded-memory adapter requires external source layers");
        String heights = filename(directory, layers.get("heightmap").getAsString());
        String biomeLayer = filename(directory, layers.get("biomes").getAsString());
        String surfaceLayer = filename(directory, layers.get("surfaces").getAsString());
        if (!heights.contains(".delta") || !(biomeLayer.contains(".pal4") || biomeLayer.contains(".bin")) || !surfaceLayer.contains(".pal4")) {
            throw new IOException("Requires source delta heights, pal4/raw-byte biomes and pal4 surfaces");
        }
        String overrides = root.has("underground") ? filename(directory, root.get("underground").getAsString()) : null;
        Map<Long, List<Override>> underground = new HashMap<>();
        if (overrides != null) {
            JsonObject object;
            try (var reader = Files.newBufferedReader(directory.resolve(overrides), StandardCharsets.UTF_8)) {
                object = JsonParser.parseReader(reader).getAsJsonObject();
            }
            if (object.get("format_version").getAsInt() != 1) throw new IOException("Unsupported underground format");
            for (var element : object.getAsJsonArray("blocks")) {
                var entry = element.getAsJsonObject();
                var coordinates = entry.getAsJsonArray("pos");
                if (coordinates.size() != 3) throw new IOException("Invalid underground position");
                int x = Math.addExact(ox, coordinates.get(0).getAsInt());
                int y = coordinates.get(1).getAsInt();
                int z = Math.addExact(oz, coordinates.get(2).getAsInt());
                if (x < ox || x >= (long) ox + width || z < oz || z >= (long) oz + depth || y < minY || y >= minY + height)
                    throw new IOException("Underground override outside source bounds");
                underground.computeIfAbsent(chunkKey(x >> 4, z >> 4), ignored -> new ArrayList<>())
                        .add(new Override(x, y, z, entry.get("block").getAsString()));
            }
        }
        underground.replaceAll((key, list) -> List.copyOf(list));
        return new GensokyoMap(version, mapVersion, root.get("id").getAsString(), width, depth, ox, oz, minY, height,
                sea, dirt, transition, start, biomes, surfaces, heights, biomeLayer, surfaceLayer, overrides,
                root.has("anchors") ? root.getAsJsonArray("anchors").size() : 0, Map.copyOf(underground));
    }

    public boolean contains(int x, int z) {
        return x >= originX && (long) x < (long) originX + width && z >= originZ && (long) z < (long) originZ + depth;
    }
    public long cells() { return (long) width * depth; }
    public int maxY() { return minY + height - 1; }
    public static long chunkKey(int x, int z) { return (x & 0xffffffffL) | ((z & 0xffffffffL) << 32); }
    public List<String> sourceFiles() {
        var result = new ArrayList<>(List.of("map.json", heightsFile, biomesFile, surfacesFile));
        if (undergroundFile != null) result.add(undergroundFile);
        return List.copyOf(result);
    }

    private static String filename(Path root, String name) throws IOException {
        Path absolute = root.toAbsolutePath().normalize(), resolved = absolute.resolve(name).normalize();
        if (name.isBlank() || Path.of(name).isAbsolute() || !resolved.startsWith(absolute) || resolved.equals(absolute))
            throw new IOException("Layer path escapes source directory: " + name);
        return name;
    }
    private static int integer(JsonObject object, String key, int fallback) { return object.has(key) ? object.get(key).getAsInt() : fallback; }
    private static float decimal(JsonObject object, String key, float fallback) { return object.has(key) ? object.get(key).getAsFloat() : fallback; }
    private static List<String> strings(JsonObject object, String key) {
        var result = new ArrayList<String>();
        for (var value : object.getAsJsonArray(key)) result.add(value.getAsString());
        return List.copyOf(result);
    }
}
