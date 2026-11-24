package cc.thonly.reverie_dreams.util.skin;

import com.mojang.authlib.properties.Property;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SkinCacheStoreTest {
    private static final String URL = "https://textures.minecraft.net/texture/secret-download";
    private static final String SIGNATURE = "private-signature";
    private static final String KEY = "0123456789abcdef0123456789abcdef";
    @TempDir Path directory;

    private static Map<String, SkinFetcherCaches.Entry> entries(String value) {
        Map<String, SkinFetcherCaches.Entry> entries = new LinkedHashMap<>();
        entries.put(KEY, new SkinFetcherCaches.Entry(new Property("textures", value, SIGNATURE)));
        return entries;
    }

    private static String legacyJson(String value) {
        return "{\"" + KEY + "\":{\"property\":{\"name\":\"textures\",\"value\":\"" + value
                + "\",\"signature\":\"" + SIGNATURE + "\"}}}";
    }

    @Test void savedCacheIsEncryptedAndContainsNoDownloadUrl() throws Exception {
        SkinCacheStore store = new SkinCacheStore(directory, new FakeCrypto());
        store.save(entries(URL));
        Path encrypted = directory.resolve("skin-cached.pak");
        assertTrue(Files.isRegularFile(encrypted));
        assertFalse(Files.exists(directory.resolve("skin-cached.json")));
        try (var files = Files.list(directory)) {
            assertEquals(1, files.count());
        }
        String raw = new String(Files.readAllBytes(encrypted), StandardCharsets.ISO_8859_1);
        assertFalse(raw.contains("textures.minecraft.net"));
        assertFalse(raw.contains(SIGNATURE));
        assertEquals(URL, store.load().get(KEY).property().value());
    }

    @Test void legacyPlaintextIsMigratedThenDeleted() throws Exception {
        Path legacy = directory.resolve("skin-cached.json");
        Files.writeString(legacy, legacyJson(URL), StandardCharsets.UTF_8);
        SkinCacheStore store = new SkinCacheStore(directory, new FakeCrypto());
        assertEquals(URL, store.load().get(KEY).property().value());
        assertTrue(Files.isRegularFile(directory.resolve("skin-cached.pak")));
        assertFalse(Files.exists(legacy));
    }

    @Test void encryptedFileWinsAndLegacyPlaintextIsDeleted() throws Exception {
        SkinCacheStore store = new SkinCacheStore(directory, new FakeCrypto());
        store.save(entries(URL));
        Path legacy = directory.resolve("skin-cached.json");
        Files.writeString(legacy, legacyJson("https://legacy.invalid/old"), StandardCharsets.UTF_8);
        assertEquals(URL, store.load().get(KEY).property().value());
        assertFalse(Files.exists(legacy));
    }

    @Test void corruptOrUnsupportedEncryptedCacheIsEmptyAndNeverFallsBackToPlaintext() throws Exception {
        Path legacy = directory.resolve("skin-cached.json");
        Files.writeString(legacy, legacyJson(URL), StandardCharsets.UTF_8);
        Files.write(directory.resolve("skin-cached.pak"), new byte[]{1, 2, 3});
        SkinCacheStore store = new SkinCacheStore(directory, new FakeCrypto());
        assertTrue(store.load().isEmpty());
        assertFalse(Files.exists(legacy));

        FakeCrypto crypto = new FakeCrypto();
        byte[] wrongNamespace = crypto.seal("{\"namespace\":\"other:skin-cache/v1-with-a-long-namespace\",\"version\":1,\"entries\":{}}"
                .getBytes(StandardCharsets.UTF_8));
        Files.write(directory.resolve("skin-cached.pak"), wrongNamespace);
        assertTrue(store.load().isEmpty());
    }

    @Test void failedOpenIsTreatedAsEmpty() throws Exception {
        FakeCrypto crypto = new FakeCrypto();
        SkinCacheStore store = new SkinCacheStore(directory, crypto);
        store.save(entries(URL));
        crypto.failOpen = true;
        assertTrue(store.load().isEmpty());
    }

    @Test void failedSealWritesNothingAndKeepsTheExistingEncryptedCache() throws Exception {
        FakeCrypto crypto = new FakeCrypto();
        SkinCacheStore store = new SkinCacheStore(directory, crypto);
        store.save(entries(URL));
        byte[] original = Files.readAllBytes(directory.resolve("skin-cached.pak"));
        crypto.failSeal = true;
        store.save(entries("https://changed.invalid/new"));
        assertArrayEquals(original, Files.readAllBytes(directory.resolve("skin-cached.pak")));
        try (var files = Files.list(directory)) {
            assertEquals(1, files.count());
        }

        Path fresh = directory.resolve("fresh");
        SkinCacheStore failing = new SkinCacheStore(fresh, crypto);
        failing.save(entries(URL));
        assertFalse(Files.exists(fresh.resolve("skin-cached.pak")));
    }

    @Test void legacyPlaintextIsDeletedEvenWhenSealingFails() throws Exception {
        Path legacy = directory.resolve("skin-cached.json");
        Files.writeString(legacy, legacyJson(URL), StandardCharsets.UTF_8);
        FakeCrypto crypto = new FakeCrypto();
        crypto.failSeal = true;
        SkinCacheStore store = new SkinCacheStore(directory, crypto);
        assertEquals(URL, store.load().get(KEY).property().value());
        assertFalse(Files.exists(legacy));
        assertFalse(Files.exists(directory.resolve("skin-cached.pak")));
    }

    @Test void malformedLegacyPlaintextIsDeletedAndNotRetained() throws Exception {
        Path legacy = directory.resolve("skin-cached.json");
        Files.writeString(legacy, "{\"not\":\"a valid cache entry\"}", StandardCharsets.UTF_8);
        SkinCacheStore store = new SkinCacheStore(directory, new FakeCrypto());
        assertTrue(store.load().isEmpty());
        assertFalse(Files.exists(legacy));
    }

    private static final class FakeCrypto implements SkinCacheStore.Crypto {
        boolean failSeal;
        boolean failOpen;

        @Override public byte[] seal(byte[] plaintext) throws IOException {
            if (failSeal) throw new IOException("seal failed");
            byte[] encoded = plaintext.clone();
            for (int i = 0; i < encoded.length; i++) encoded[i] ^= 0x5A;
            return encoded;
        }

        @Override public byte[] open(byte[] encoded) throws IOException {
            if (failOpen) throw new IOException("open failed");
            byte[] plaintext = encoded.clone();
            for (int i = 0; i < plaintext.length; i++) plaintext[i] ^= 0x5A;
            return plaintext;
        }
    }
}
