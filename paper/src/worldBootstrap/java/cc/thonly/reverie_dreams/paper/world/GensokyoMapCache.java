package cc.thonly.reverie_dreams.paper.world;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

/** Five bytes per original column on disk; at most sixteen16-row stripes in RAM. */
public final class GensokyoMapCache implements GensokyoColumns, AutoCloseable {
    public static final int STRIPE_ROWS = 16, MAX_STRIPES = 16, BYTES_PER_CELL = 5;
    private final GensokyoMap map;
    private final FileChannel channel;
    private final JsonObject manifest;
    private final Map<Integer, byte[]> stripes = new LinkedHashMap<>(16, 0.75f, true);

    private GensokyoMapCache(GensokyoMap map, FileChannel channel, JsonObject manifest) {
        this.map = map;
        this.channel = channel;
        this.manifest = manifest;
    }

    /** Explicit preparation only: decoding is never hidden in a generation callback. */
    public static JsonObject prepare(Path source, Path cache) throws IOException {
        GensokyoMap map = GensokyoMap.read(source);
        Files.createDirectories(cache);
        try (FileChannel lockChannel = FileChannel.open(cache.resolve("prepare.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             var ignored = lockChannel.lock()) {
            String stamp = fingerprint(source, map);
            Path raw = cache.resolve("columns.bin"), marker = cache.resolve("manifest.json");
            if (Files.isRegularFile(raw) && Files.isRegularFile(marker)) {
                try {
                    JsonObject old = JsonParser.parseString(Files.readString(marker)).getAsJsonObject();
                    if (valid(old, stamp, map) && Files.size(raw) == map.cells() * BYTES_PER_CELL
                            && checksum(raw).equals(old.get("raw_sha256").getAsString())) return old;
                } catch (RuntimeException ignoredInvalid) { /* Rebuild only this disposable cache. */ }
            }
            long required = Math.addExact(map.cells() * BYTES_PER_CELL, 16L * 1024 * 1024);
            if (Files.getFileStore(cache).getUsableSpace() < required) throw new IOException("Insufficient space for " + required + " byte map cache");
            Path temporary = cache.resolve("columns-" + UUID.randomUUID() + ".tmp");
            MessageDigest digest = sha256();
            long[] biomeCounts = new long[map.biomePalette().size()], surfaceCounts = new long[map.surfacePalette().size()];
            int minimum = Integer.MAX_VALUE, maximum = Integer.MIN_VALUE;
            try {
                try (InputStream heights = layer(source.resolve(map.heightsFile()));
                     InputStream biomes = layer(source.resolve(map.biomesFile()));
                     InputStream surfaces = layer(source.resolve(map.surfacesFile()));
                     OutputStream output = new DigestOutputStream(new BufferedOutputStream(Files.newOutputStream(temporary,
                             StandardOpenOption.CREATE_NEW), 1024 * 1024), digest)) {
                    short[] previous = new short[map.width()];
                    byte[] heightRow = new byte[map.width() * 2], row = new byte[map.width() * BYTES_PER_CELL];
                    PaletteInput biomeInput = new PaletteInput(biomes, map.biomesFile().contains(".pal4"));
                    PaletteInput surfaceInput = new PaletteInput(surfaces, true);
                    for (int z = 0; z < map.depth(); z++) {
                        exactly(heights, heightRow, heightRow.length);
                        for (int x = 0; x < map.width(); x++) {
                            short delta = (short) ((heightRow[x * 2] & 255) | ((heightRow[x * 2 + 1] & 255) << 8));
                            short height = z == 0 ? delta : (short) (previous[x] + delta);
                            previous[x] = height;
                            if (height != GensokyoMap.NO_SURFACE && (height < map.minY() || height > map.maxY()))
                                throw new IOException("Height outside source dimension at " + x + "," + z + ": " + height);
                            if (height != GensokyoMap.NO_SURFACE) { minimum = Math.min(minimum, height); maximum = Math.max(maximum, height); }
                            int biome = biomeInput.next(), surface = surfaceInput.next();
                            if (biome >= biomeCounts.length || surface >= surfaceCounts.length) throw new IOException("Palette index outside source palette");
                            biomeCounts[biome]++; surfaceCounts[surface]++;
                            int offset = x * BYTES_PER_CELL;
                            row[offset] = (byte) height; row[offset + 1] = (byte) (height >>> 8);
                            row[offset + 2] = (byte) biome; row[offset + 3] = (byte) surface; row[offset + 4] = 0;
                        }
                        output.write(row);
                    }
                    end(heights); biomeInput.end(); surfaceInput.end();
                }
                JsonObject result = new JsonObject();
                result.addProperty("version", 1); result.addProperty("source_sha256", stamp);
                result.addProperty("raw_sha256", HexFormat.of().formatHex(digest.digest()));
                result.addProperty("cells", map.cells()); result.addProperty("bytes", map.cells() * BYTES_PER_CELL);
                result.addProperty("width", map.width()); result.addProperty("depth", map.depth());
                result.addProperty("minimum_height", minimum); result.addProperty("maximum_height", maximum);
                result.add("biome_counts", counts(biomeCounts)); result.add("surface_counts", counts(surfaceCounts));
                Files.deleteIfExists(marker); // Readers reject a partially published generation.
                move(temporary, raw);
                Path tempMarker = cache.resolve("manifest-" + UUID.randomUUID() + ".tmp");
                Files.writeString(tempMarker, result + "\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
                move(tempMarker, marker);
                return result;
            } finally {
                Files.deleteIfExists(temporary);
            }
        }
    }

    public static GensokyoMapCache open(Path source, Path cache) throws IOException {
        GensokyoMap map = GensokyoMap.read(source);
        JsonObject marker;
        try {
            marker = JsonParser.parseString(Files.readString(cache.resolve("manifest.json"))).getAsJsonObject();
        } catch (RuntimeException error) { throw new IOException("Invalid prepared map manifest; run /rdworld prepare", error); }
        Path raw = cache.resolve("columns.bin");
        if (!valid(marker, fingerprint(source, map), map) || Files.size(raw) != map.cells() * BYTES_PER_CELL)
            throw new IOException("Missing/stale map cache; run /rdworld prepare before enabling a world");
        if (!checksum(raw).equals(marker.get("raw_sha256").getAsString())) throw new IOException("Prepared map checksum mismatch; run /rdworld prepare");
        return new GensokyoMapCache(map, FileChannel.open(raw, StandardOpenOption.READ), marker);
    }

    public GensokyoMap map() { return map; }
    public JsonObject manifest() { return manifest.deepCopy(); }

    /** Bits0..15 signed height,16..23 biome index,24..39 unsigned surface index. */
    public synchronized long cell(int worldX, int worldZ) {
        if (!map.contains(worldX, worldZ)) return Short.toUnsignedLong((short) GensokyoMap.NO_SURFACE);
        int x = worldX - map.originX(), z = worldZ - map.originZ();
        int stripe = z / STRIPE_ROWS;
        byte[] bytes = stripes.get(stripe);
        if (bytes == null) {
            int rows = Math.min(STRIPE_ROWS, map.depth() - stripe * STRIPE_ROWS);
            bytes = new byte[Math.multiplyExact(rows * map.width(), BYTES_PER_CELL)];
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            long offset = (long) stripe * STRIPE_ROWS * map.width() * BYTES_PER_CELL;
            try {
                while (buffer.hasRemaining()) {
                    int count = channel.read(buffer, offset + buffer.position());
                    if (count < 0) throw new EOFException("Truncated prepared map cache");
                }
            } catch (IOException error) { throw new UncheckedIOException("Cannot read map cache stripe " + stripe, error); }
            if (stripes.size() >= MAX_STRIPES) stripes.remove(stripes.keySet().iterator().next());
            stripes.put(stripe, bytes);
        }
        int offset = ((z % STRIPE_ROWS) * map.width() + x) * BYTES_PER_CELL;
        return (bytes[offset] & 255L) | ((bytes[offset + 1] & 255L) << 8) | ((bytes[offset + 2] & 255L) << 16)
                | ((bytes[offset + 3] & 255L) << 24) | ((bytes[offset + 4] & 255L) << 32);
    }

    public int heightAt(int x, int z) { return (short) cell(x, z); }
    public int biomeAt(int x, int z) { return (int) ((cell(x, z) >>> 16) & 255); }
    public String surfaceAt(int x, int z) {
        return map.contains(x, z) ? map.surfacePalette().get((int) ((cell(x, z) >>> 24) & 65535)) : "minecraft:air";
    }
    public synchronized int retainedBytes() { return stripes.values().stream().mapToInt(a -> a.length).sum(); }
    @Override public synchronized void close() throws IOException { stripes.clear(); channel.close(); }

    public static String fingerprint(Path source, GensokyoMap map) throws IOException {
        MessageDigest digest = sha256();
        for (String name : map.sourceFiles()) {
            digest.update(name.getBytes(StandardCharsets.UTF_8)); digest.update((byte) 0);
            try (InputStream input = Files.newInputStream(source.resolve(name))) {
                input.transferTo(new DigestOutputStream(OutputStream.nullOutputStream(), digest));
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static boolean valid(JsonObject marker, String stamp, GensokyoMap map) {
        return marker.get("version").getAsInt() == 1 && marker.get("source_sha256").getAsString().equals(stamp)
                && marker.get("cells").getAsLong() == map.cells() && marker.get("width").getAsInt() == map.width()
                && marker.get("depth").getAsInt() == map.depth();
    }
    private static InputStream layer(Path path) throws IOException {
        InputStream raw = new BufferedInputStream(Files.newInputStream(path), 65536);
        try { return path.toString().endsWith(".gz") ? new GZIPInputStream(raw, 65536) : raw; }
        catch (IOException error) { raw.close(); throw error; }
    }
    private static void exactly(InputStream input, byte[] buffer, int size) throws IOException {
        if (input.readNBytes(buffer, 0, size) != size) throw new EOFException("Truncated source layer");
    }
    private static void end(InputStream input) throws IOException { if (input.read() != -1) throw new IOException("Source layer contains excess data"); }
    private static JsonArray counts(long[] values) { var result = new JsonArray(); for (long value : values) result.add(value); return result; }
    private static MessageDigest sha256() { try { return MessageDigest.getInstance("SHA-256"); } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); } }
    private static String checksum(Path path) throws IOException {
        MessageDigest digest = sha256();
        try (InputStream input = new BufferedInputStream(Files.newInputStream(path), 1024 * 1024)) {
            input.transferTo(new DigestOutputStream(OutputStream.nullOutputStream(), digest));
        }
        return HexFormat.of().formatHex(digest.digest());
    }
    private static void move(Path from, Path to) throws IOException {
        try { Files.move(from, to, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
        catch (AtomicMoveNotSupportedException ignored) { Files.move(from, to, StandardCopyOption.REPLACE_EXISTING); }
    }
    private static final class PaletteInput {
        private final InputStream input;
        private final boolean palette4;
        private int packed;
        private boolean low;
        private final byte[] buffer = new byte[8192];
        private int cursor, limit;
        PaletteInput(InputStream input, boolean palette4) { this.input = input; this.palette4 = palette4; }
        int next() throws IOException {
            if (low) { low = false; return packed & 15; }
            if (cursor == limit) { limit = input.read(buffer); cursor = 0; }
            packed = limit < 0 ? -1 : buffer[cursor++] & 255;
            if (packed < 0) throw new EOFException("Truncated source palette layer");
            if (!palette4) return packed;
            low = true; return packed >>> 4;
        }
        void end() throws IOException {
            if (cursor != limit) throw new IOException("Source palette layer contains excess data");
            GensokyoMapCache.end(input);
        }
    }
}
