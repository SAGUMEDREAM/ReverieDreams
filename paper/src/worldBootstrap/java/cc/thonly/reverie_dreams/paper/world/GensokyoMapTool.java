package cc.thonly.reverie_dreams.paper.world;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;
import java.io.EOFException;
import java.io.IOException;
import java.util.Map;

/** Offline preparation and source-derived terrain fixtures; never creates or loads a Minecraft world. */
public final class GensokyoMapTool {
    private GensokyoMapTool() { }
    public static void main(String[] args) throws Exception {
        if (args.length != 2 && args.length != 3)
            throw new IllegalArgumentException("source-directory cache-directory [terrain-fixture-directory]");
        Path source = Path.of(args[0]), cache = Path.of(args[1]);
        GensokyoMapCache.prepare(source, cache);
        if (args.length == 2) return;
        Path output = Path.of(args[2]); Files.createDirectories(output);
        try (var data = GensokyoMapCache.open(source, cache)) {
            var terrain = new GensokyoTerrain(data); var map = data.map();
            var nativePoints = new JsonArray();
            int x = map.spawn().x(), z = map.spawn().z();
            for (int[] p : new int[][]{{x,z},{x-16,z},{x,z-16},{x+16,z},{x,z+16},
                    {map.originX(),map.originZ()},{map.originX()+map.width()-1,map.originZ()+map.depth()-1}})
                nativePoints.add(point(data, terrain, p[0], p[1]));
            write(output.resolve("gensokyo-native-points.json"), document(data, nativePoints));
            var representatives = new LinkedHashMap<Integer, int[]>(); int[] paddy = null;
            // Stream cache rows once, with bounded memory; quarter-aligned interior representatives
            // match the actual generator biome lookup without transition/border sampling ambiguity.
            try (var channel = FileChannel.open(cache.resolve("columns.bin"), StandardOpenOption.READ)) {
                var buffer = ByteBuffer.allocate(map.width() * GensokyoMapCache.BYTES_PER_CELL);
                for (int row = 0; row < map.depth(); row++) {
                    buffer.clear(); while (buffer.hasRemaining()) if (channel.read(buffer) < 0)
                        throw new EOFException("Truncated prepared columns");
                    byte[] values = buffer.array();
                    for (int column = 0; column < map.width(); column++) {
                        int offset = column * 5, biome = values[offset+2] & 255;
                        int surface = (values[offset+3] & 255) | ((values[offset+4] & 255) << 8);
                        String state = map.surfacePalette().get(surface);
                        if (paddy == null && state.equals("reverie_dreams:paddy_field"))
                            paddy = new int[]{map.originX()+column,map.originZ()+row};
                        if (column >= 64 && row >= 64 && column < map.width()-64 && row < map.depth()-64
                                && (column & 3) == 0 && (row & 3) == 0 && state.startsWith("minecraft:"))
                            representatives.putIfAbsent(biome, new int[]{map.originX()+column,map.originZ()+row});
                    }
                }
            }
            var active = new JsonArray();
            representatives.entrySet().stream().sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> active.add(point(data, terrain, entry.getValue()[0], entry.getValue()[1])));
            write(output.resolve("gensokyo-active-biome-points.json"), document(data, active));
            if (paddy == null) throw new IllegalStateException("Source no longer contains a paddy; revise the explicit custom-surface probe");
            var custom = new JsonArray(); custom.add(point(data, terrain, paddy[0], paddy[1]));
            write(output.resolve("gensokyo-paddy-point.json"), document(data, custom));
        }
    }
    private static JsonObject document(GensokyoMapCache data, JsonArray points) {
        var root = new JsonObject(); root.addProperty("minecraft", "26.1.2");
        root.addProperty("source_sha256", data.manifest().get("source_sha256").getAsString());
        root.addProperty("minimum_y", data.map().minY()); root.addProperty("maximum_y_exclusive", data.map().minY()+data.map().height());
        root.addProperty("sea_level", data.map().seaLevel()); root.addProperty("basis", "Current decoded source columns, not a runtime feature-order oracle");
        root.add("points", points); return root;
    }
    private static JsonObject point(GensokyoMapCache data, GensokyoTerrain terrain, int x, int z) {
        var point = new JsonObject(); int height = data.heightAt(x,z);
        point.addProperty("x",x); point.addProperty("z",z); point.addProperty("source_height",height);
        point.addProperty("source_surface",data.surfaceAt(x,z));
        point.addProperty("source_biome",data.map().biomePalette().get(data.biomeAt(x,z)));
        point.addProperty("generator_biome",data.map().biomePalette().get(terrain.biomeIndex(x,z)));
        var samples = new TreeSet<>(List.of(0,1,height-4,height-3,height-2,height-1,height,height+1,1535));
        for (var run : terrain.column(x,z)) { samples.add(run.minY()); samples.add(run.maxYExclusive()-1); }
        for (var override : data.map().underground().getOrDefault(GensokyoMap.chunkKey(x>>4,z>>4), List.of()))
            if (override.x()==x && override.z()==z) samples.add(override.y());
        var blocks = new JsonArray();
        for (int y : samples) if (y>=data.map().minY() && y<data.map().minY()+data.map().height()) {
            var block = new JsonObject(); block.addProperty("y",y); block.addProperty("state",terrain.stateAt(x,y,z)); blocks.add(block);
        }
        point.add("blocks",blocks); return point;
    }
    private static void write(Path target, JsonObject value) throws IOException {
        Files.writeString(target, new GsonBuilder().setPrettyPrinting().create().toJson(value)+"\n");
    }
}
