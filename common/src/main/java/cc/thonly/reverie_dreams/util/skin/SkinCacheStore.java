package cc.thonly.reverie_dreams.util.skin;

import cc.thonly.pak.PakLimits;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Map;

/**
 * Encrypted skin-download cache. The encryption key is derived from the machine
 * fingerprint inside native code and never reaches Java or the file system.
 */
final class SkinCacheStore {
    private static final Logger LOGGER = LoggerFactory.getLogger("reverie_dreams/skin-cache");
    static final String NAMESPACE = "reverie_dreams:skin-cache/v1";
    static final int VERSION = 1;
    private static final String LEGACY_FILE = "skin-cached.json";
    private static final String ENCRYPTED_FILE = "skin-cached.pak";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Codec<Wrapper> WRAPPER_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("namespace").forGetter(Wrapper::namespace),
            Codec.INT.fieldOf("version").forGetter(Wrapper::version),
            SkinFetcherCaches.CODEC_MAP.fieldOf("entries").forGetter(Wrapper::entries)
    ).apply(instance, Wrapper::new));

    private final Path directory;
    private final Crypto crypto;

    SkinCacheStore(Path directory, Crypto crypto) {
        this.directory = directory;
        this.crypto = crypto;
    }

    static SkinCacheStore defaultStore() {
        return new SkinCacheStore(Paths.get("config", "reverie_dreams"), new NativeSkinCacheCrypto());
    }

    synchronized Map<String, SkinFetcherCaches.Entry> load() {
        Path encrypted = directory.resolve(ENCRYPTED_FILE);
        Path legacy = directory.resolve(LEGACY_FILE);
        try {
            if (Files.isRegularFile(encrypted, LinkOption.NOFOLLOW_LINKS)) {
                deleteLegacy(legacy);
                return readEncrypted(encrypted);
            }
            if (Files.isRegularFile(legacy, LinkOption.NOFOLLOW_LINKS)) {
                Map<String, SkinFetcherCaches.Entry> entries;
                try {
                    entries = readLegacy(legacy);
                } catch (IOException | RuntimeException e) {
                    LOGGER.warn("Unable to read the legacy skin cache; discarding it");
                    entries = Map.of();
                }
                try {
                    writeEncrypted(encrypted, entries);
                } catch (IOException | RuntimeException e) {
                    LOGGER.warn("Unable to encrypt the legacy skin cache; keeping the cache in memory only");
                } finally {
                    // The legacy file is plaintext and must not remain even if sealing failed.
                    deleteLegacy(legacy);
                }
                return entries;
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("Unable to open the encrypted skin cache; treating it as empty");
        }
        return Map.of();
    }

    synchronized void save(Map<String, SkinFetcherCaches.Entry> entries) {
        try {
            writeEncrypted(directory.resolve(ENCRYPTED_FILE), entries);
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("Unable to write the encrypted skin cache; keeping it in memory only");
        }
    }

    private Map<String, SkinFetcherCaches.Entry> readEncrypted(Path path) throws IOException {
        byte[] encoded;
        try (InputStream input = Files.newInputStream(path, LinkOption.NOFOLLOW_LINKS)) {
            encoded = input.readNBytes(PakLimits.MAX_FILE_BYTES + 1);
        }
        if (encoded.length < PakLimits.HEADER_BYTES + PakLimits.AUTH_TAG_BYTES + 1
                || encoded.length > PakLimits.MAX_FILE_BYTES) {
            throw new IOException("Invalid encrypted skin cache size");
        }
        byte[] plaintext = crypto.open(encoded);
        try {
            return decode(plaintext);
        } finally {
            Arrays.fill(plaintext, (byte) 0);
        }
    }

    private void writeEncrypted(Path target, Map<String, SkinFetcherCaches.Entry> entries) throws IOException {
        byte[] plaintext = encode(entries);
        byte[] encoded;
        try {
            encoded = crypto.seal(plaintext);
        } finally {
            Arrays.fill(plaintext, (byte) 0);
        }
        if (encoded == null || encoded.length < PakLimits.HEADER_BYTES + PakLimits.AUTH_TAG_BYTES + 1
                || encoded.length > PakLimits.MAX_FILE_BYTES) {
            throw new IOException("Invalid encrypted skin cache payload");
        }
        Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, "skin-cached-", ".tmp");
        try {
            Files.write(temporary, encoded, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static Map<String, SkinFetcherCaches.Entry> readLegacy(Path path) throws IOException {
        byte[] bytes;
        try (InputStream input = Files.newInputStream(path, LinkOption.NOFOLLOW_LINKS)) {
            bytes = input.readNBytes(PakLimits.MAX_PLAINTEXT_BYTES + 1);
        }
        if (bytes.length == 0 || bytes.length > PakLimits.MAX_PLAINTEXT_BYTES) {
            throw new IOException("Invalid legacy skin cache size");
        }
        JsonElement element;
        try {
            element = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8));
        } catch (JsonParseException e) {
            throw new IOException("Invalid legacy skin cache JSON", e);
        }
        if (element.isJsonObject() && element.getAsJsonObject().has("namespace")
                && element.getAsJsonObject().has("entries")) {
            return decode(bytes);
        }
        DataResult<Map<String, SkinFetcherCaches.Entry>> parsed = SkinFetcherCaches.CODEC_MAP.parse(JsonOps.INSTANCE, element);
        return parsed.result().orElseThrow(() -> new IOException("Invalid legacy skin cache entries"));
    }

    private static byte[] encode(Map<String, SkinFetcherCaches.Entry> entries) throws IOException {
        Wrapper wrapper = new Wrapper(NAMESPACE, VERSION, entries);
        DataResult<JsonElement> result = WRAPPER_CODEC.encodeStart(JsonOps.INSTANCE, wrapper);
        JsonElement element = result.result().orElseThrow(() -> new IOException("Unable to encode the skin cache"));
        byte[] plaintext = GSON.toJson(element).getBytes(StandardCharsets.UTF_8);
        if (plaintext.length == 0 || plaintext.length > PakLimits.MAX_PLAINTEXT_BYTES) {
            throw new IOException("Skin cache exceeds the PAK plaintext limit");
        }
        return plaintext;
    }

    private static Map<String, SkinFetcherCaches.Entry> decode(byte[] plaintext) throws IOException {
        JsonElement element;
        try {
            element = JsonParser.parseString(new String(plaintext, StandardCharsets.UTF_8));
        } catch (JsonParseException e) {
            throw new IOException("Invalid skin cache JSON", e);
        }
        DataResult<Wrapper> parsed = WRAPPER_CODEC.parse(JsonOps.INSTANCE, element);
        Wrapper wrapper = parsed.result().orElseThrow(() -> new IOException("Invalid skin cache wrapper"));
        if (!NAMESPACE.equals(wrapper.namespace()) || wrapper.version() != VERSION) {
            throw new IOException("Unsupported skin cache namespace or version");
        }
        return wrapper.entries();
    }

    private void deleteLegacy(Path legacy) {
        try {
            Files.deleteIfExists(legacy);
        } catch (IOException e) {
            LOGGER.warn("Unable to delete the plaintext skin cache");
        }
    }

    interface Crypto {
        byte[] seal(byte[] plaintext) throws IOException;

        byte[] open(byte[] encoded) throws IOException;
    }

    private record Wrapper(String namespace, int version, Map<String, SkinFetcherCaches.Entry> entries) {
    }
}
