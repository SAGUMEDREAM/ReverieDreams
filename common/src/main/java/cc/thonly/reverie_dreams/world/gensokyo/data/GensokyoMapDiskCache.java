package cc.thonly.reverie_dreams.world.gensokyo.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Disposable shared arrays. A process lock covers both reads and publication.
 */
final class GensokyoMapDiskCache {
    private static final Logger LOGGER = LoggerFactory.getLogger("GensokyoMapCache");
    private static final List<String> FILES = List.of("heightmap.bin", "biomes.bin", "surfaces.bin", "source.stamp");
    private static final Map<Path, Object> JVM_LOCKS = new ConcurrentHashMap<>();

    record Layers(short[] heights, byte[] biomes, short[] surfaces) {
    }

    private GensokyoMapDiskCache() {
    }

    static Path sharedRoot(Path gameDirectory) {
        return gameDirectory.toAbsolutePath().normalize().resolve("cache/reverie_dreams/gensokyo-map");
    }

    static Path mapDirectory(Path root, Identifier map) {
        Path absolute = root.toAbsolutePath().normalize();
        Path result = absolute.resolve(map.getNamespace()).resolve(map.getPath()).normalize();
        if (!result.startsWith(absolute) || result.equals(absolute))
            throw new IllegalArgumentException("Map cache is outside its root: " + map);
        return result;
    }

    static Layers load(Path root, Identifier map, String source, int expected, Supplier<Layers> decode) {
        Path directory = mapDirectory(root, map);
        synchronized (JVM_LOCKS.computeIfAbsent(directory, ignored -> new Object())) {
            try {
                Files.createDirectories(directory);
                try (FileChannel channel = FileChannel.open(directory.resolve("cache.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                     var lock = channel.lock()) {
                    cleanTemporaryFiles(directory);
                    Layers cached = read(directory, source, expected);
                    if (cached != null) {
                        LOGGER.info("Loaded shared Gensokyo map cache: {}", directory);
                        return cached;
                    }
                    Layers layers = decode.get();
                    if (layers.heights.length != expected || layers.biomes.length != expected || layers.surfaces.length != expected)
                        throw new IllegalStateException("Decoded map layer lengths do not match cache metadata");
                    try {
                        write(directory, source, expected, layers);
                        LOGGER.info("Wrote shared Gensokyo map cache: {}", directory);
                    } catch (IOException e) {
                        LOGGER.warn("Cannot persist shared Gensokyo cache {}; using decoded arrays", directory, e);
                        cleanTemporaryFiles(directory);
                    }
                    return layers;
                }
            } catch (IOException e) {
                LOGGER.warn("Cannot access shared Gensokyo cache {}; loading without disk cache", directory, e);
                return decode.get();
            }
        }
    }

    private static Layers read(Path directory, String source, int expected) {
        try {
            Path stamp = directory.resolve("source.stamp");
            if (!Files.isRegularFile(stamp))
                return null;
            JsonObject manifest = JsonParser.parseString(Files.readString(stamp, StandardCharsets.UTF_8)).getAsJsonObject();
            if (manifest.get("version").getAsInt() != 1 || manifest.get("cells").getAsInt() != expected
                    || !manifest.get("source").getAsString().equals(source))
                return null;
            JsonObject hashes = manifest.getAsJsonObject("sha256");
            short[] heights = readShorts(directory.resolve("heightmap.bin"), expected, hashes.get("heightmap.bin").getAsString());
            byte[] biomes;
            Path biomeFile = directory.resolve("biomes.bin");
            if (Files.size(biomeFile) != expected)
                return null;
            try (DigestInputStream input = new DigestInputStream(Files.newInputStream(biomeFile), sha256())) {
                biomes = GensokyoLayerDecoder.readBytes(input, expected, false);
                verify(input.getMessageDigest(), hashes.get("biomes.bin").getAsString());
            }
            short[] surfaces = readShorts(directory.resolve("surfaces.bin"), expected, hashes.get("surfaces.bin").getAsString());
            return new Layers(heights, biomes, surfaces);
        } catch (IOException | RuntimeException e) {
            LOGGER.debug("Invalid shared Gensokyo map cache {}; rebuilding", directory, e);
            return null;
        }
    }

    private static short[] readShorts(Path file, int expected, String hash) throws IOException {
        if (Files.size(file) != (long) expected * 2)
            throw new IOException("Invalid raw cache length: " + file);
        try (DigestInputStream input = new DigestInputStream(new BufferedInputStream(Files.newInputStream(file), 1 << 20), sha256())) {
            short[] values = GensokyoLayerDecoder.readShorts(input, expected, 0);
            verify(input.getMessageDigest(), hash);
            return values;
        }
    }

    private static void write(Path directory, String source, int expected, Layers layers) throws IOException {
        JsonObject hashes = new JsonObject();
        hashes.addProperty("heightmap.bin", writeShorts(directory.resolve("heightmap.bin.tmp"), layers.heights));
        hashes.addProperty("surfaces.bin", writeShorts(directory.resolve("surfaces.bin.tmp"), layers.surfaces));
        try (DigestOutputStream output = new DigestOutputStream(Files.newOutputStream(directory.resolve("biomes.bin.tmp")), sha256())) {
            output.write(layers.biomes);
            hashes.addProperty("biomes.bin", HexFormat.of().formatHex(output.getMessageDigest().digest()));
        }
        JsonObject manifest = new JsonObject();
        manifest.addProperty("version", 1);
        manifest.addProperty("source", source);
        manifest.addProperty("cells", expected);
        manifest.add("sha256", hashes);
        Files.writeString(directory.resolve("source.stamp.tmp"), manifest + "\n", StandardCharsets.UTF_8);
        // Readers hold the same lock. Invalidate the old marker before replacing
        // any array, and publish the new marker only after all arrays are ready.
        Files.deleteIfExists(directory.resolve("source.stamp"));
        for (String name : FILES)
            move(directory.resolve(name + ".tmp"), directory.resolve(name));
    }

    private static String writeShorts(Path file, short[] values) throws IOException {
        MessageDigest digest = sha256();
        try (OutputStream output = new DigestOutputStream(new BufferedOutputStream(Files.newOutputStream(file), 1 << 20), digest)) {
            byte[] buffer = new byte[8192];
            for (int index = 0; index < values.length; ) {
                int count = Math.min(buffer.length / 2, values.length - index);
                for (int i = 0; i < count; i++) {
                    short value = values[index + i];
                    buffer[i * 2] = (byte) value;
                    buffer[i * 2 + 1] = (byte) (value >>> 8);
                }
                output.write(buffer, 0, count * 2);
                index += count;
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static void move(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private static void verify(MessageDigest digest, String expected) throws IOException {
        if (!HexFormat.of().formatHex(digest.digest()).equals(expected))
            throw new IOException("Map cache checksum mismatch");
    }

    private static void cleanTemporaryFiles(Path directory) {
        for (String name : FILES) {
            try {
                Files.deleteIfExists(directory.resolve(name + ".tmp"));
            } catch (IOException e) {
                LOGGER.debug("Cannot remove temporary map cache file {}", directory.resolve(name + ".tmp"), e);
            }
        }
    }

    /**
     * Never recurse into a save; only the four disposable legacy cache files.
     */
    static void retireLegacy(Path directory) {
        if (!Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS))
            return;
        for (String name : FILES) {
            Path file = directory.resolve(name);
            if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS))
                continue;
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                LOGGER.warn("Cannot remove legacy map cache {}", file, e);
            }
        }
        try {
            Files.delete(directory); // Succeeds only for an empty directory.
        } catch (DirectoryNotEmptyException ignored) {
            // Unknown files are deliberately retained.
        } catch (IOException e) {
            LOGGER.debug("Cannot remove empty legacy map cache directory {}", directory, e);
        }
    }
}
