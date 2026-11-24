package cc.thonly.reverie_dreams.util.skin;

import cc.thonly.pak.ClassResourceSource;
import cc.thonly.pak.NativePak;
import cc.thonly.reverie_dreams.client.pak.PakSkinPacks;
import cc.thonly.reverie_dreams.client.pak.SkinCatalog;
import cc.thonly.reverie_dreams.client.pak.SkinPackManifest;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PakSkinAssetsTest {
    private static final String SKIN_PREFIX = "reverie_dreams:textures/entity/player/skin/";

    @Test void decryptsEveryCataloguedSkinToItsRecordedPlaintextHash() throws Exception {
        Map<String, String> expected = new LinkedHashMap<>();
        try (var in = PakSkinAssetsTest.class.getResourceAsStream("/pak-skins.sha256")) {
            assertNotNull(in);
            var reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            for (String line; (line = reader.readLine()) != null; ) {
                String[] parts = line.strip().split("\\s+");
                if (parts.length == 1 && parts[0].isEmpty()) continue;
                assertEquals(2, parts.length, line);
                assertNull(expected.put(parts[0], parts[1]), "Duplicate hash entry " + parts[0]);
            }
        }
        var container = ClassResourceSource.of(PakSkinPacks.class);
        SkinPackManifest catalog = SkinCatalog.load(container::open, new NativePak(container));
        int decoded = 0;
        for (SkinPackManifest.Entry entry : catalog.entries().values()) {
            String resource = entry.asset().resource();
            if (!resource.startsWith(SKIN_PREFIX) || !resource.endsWith(".png")) continue;
            String skin = resource.substring(SKIN_PREFIX.length(), resource.length() - 4);
            byte[] actual = PakSkinAssets.decrypt(skin).orElseThrow(() -> new AssertionError("Missing PAK skin " + skin));
            String sha256 = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(actual));
            assertEquals(expected.get(skin), sha256, skin);
            var image = ImageIO.read(new ByteArrayInputStream(actual));
            assertEquals(64, image.getWidth(), skin);
            assertEquals(64, image.getHeight(), skin);
            decoded++;
        }
        assertEquals(119, decoded);
        assertEquals(119, expected.size());
    }

    @Test void unlistedSkinHasNoEncryptedSource() throws Exception {
        assertTrue(PakSkinAssets.decrypt("test_custom_skin").isEmpty());
    }
}
