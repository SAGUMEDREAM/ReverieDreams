package cc.thonly.reverie_dreams.client.pak;

import cc.thonly.pak.PakAsset;
import cc.thonly.pak.PakCodec;
import cc.thonly.pak.KeyRegistry;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceFilterSection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class PakSkinPackTest {
    private static final String SKIN_PATH = "textures/entity/player/skin/reimu.png";
    private static final Identifier PNG = Identifier.parse("reverie_dreams:" + SKIN_PATH);
    private static final Identifier CONFIG = Identifier.parse("reverie_dreams:pak_custom/config.json");
    private static final String SKIN_SLOT = "k0123456789abcdef0123456789abcdef";
    private static final String SKIN_FILE = "0123456789abcdef0123456789abcdef.pak";
    private static final String OTHER_SLOT = "kfedcba9876543210fedcba9876543210";
    private static final String OTHER_FILE = "fedcba9876543210fedcba9876543210.pak";
    private static final String CONFIG_JSON = "{\"format\":2}";

    @BeforeAll static void gameVersion() { SharedConstants.tryDetectVersion(); }

    private static byte[] png(int size, int color) throws Exception {
        var image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB); image.setRGB(0, 0, color);
        var out = new ByteArrayOutputStream(); assertTrue(ImageIO.write(image, "png", out)); return out.toByteArray();
    }

    private static String catalog(String rows) {
        return "{\"version\":2,\"owner\":\"reverie_dreams\",\"entries\":[" + rows + "]}";
    }

    private static String row(String path, String file, String slot) {
        return "{\"path\":\"" + path + "\",\"file\":\"" + file + "\",\"slot\":\"" + slot + "\"}";
    }

    private static String defaultCatalog() {
        return catalog(row("reverie_dreams:" + SKIN_PATH, SKIN_FILE, SKIN_SLOT) + ","
                + row("reverie_dreams:pak_custom/config.json", OTHER_FILE, OTHER_SLOT));
    }

    private static PackLocationInfo location(String id) { return new PackLocationInfo(id, Component.literal(id), PackSource.BUILT_IN, Optional.empty()); }

    private static KeyRegistry keys() {
        var keys = new KeyRegistry();
        keys.register("reverie_dreams", SkinPackManifest.CATALOG_SLOT, new byte[32]);
        keys.register("reverie_dreams", SKIN_SLOT, new byte[32]);
        keys.register("reverie_dreams", OTHER_SLOT, new byte[32]);
        return keys;
    }

    private static byte[] encoded(String catalogJson, KeyRegistry keys) throws IOException {
        return PakCodec.encrypt(catalogJson.getBytes(StandardCharsets.UTF_8), SkinPackManifest.CATALOG_ASSET, keys);
    }

    private record Fixture(PakSkinPack pack, byte[] original, AtomicInteger reads, List<byte[]> decoded) {}

    private Fixture fixture(int color, boolean corrupt) throws Exception {
        return fixture(64, color, corrupt, defaultCatalog());
    }

    private Fixture fixture(int size, int color, boolean corrupt, String catalogJson) throws Exception {
        var keys = keys();
        byte[] original = png(size, color);
        byte[] cipher = PakCodec.encrypt(original,
                new PakAsset("reverie_dreams", "reverie_dreams:" + SKIN_PATH, SKIN_SLOT), keys);
        if (corrupt) cipher[cipher.length - 1] ^= 1;
        var reads = new AtomicInteger();
        var decoded = new ArrayList<byte[]>();
        Map<String, byte[]> source = new LinkedHashMap<>();
        source.put(SkinPackManifest.CATALOG, encoded(catalogJson, keys));
        source.put(SkinPackManifest.PACK_DIRECTORY + SKIN_FILE, cipher);
        source.put(SkinPackManifest.PACK_DIRECTORY + OTHER_FILE,
                PakCodec.encrypt(CONFIG_JSON.getBytes(StandardCharsets.UTF_8),
                        new PakAsset("reverie_dreams", CONFIG.toString(), OTHER_SLOT), keys));
        String skinPacked = SkinPackManifest.PACK_DIRECTORY + SKIN_FILE;
        var pack = new PakSkinPack(location(PakSkinPacks.PACK_ID), path -> {
            if (path.equals(skinPacked)) reads.incrementAndGet();
            byte[] value = source.get(path); return value == null ? null : new ByteArrayInputStream(value);
        }, (input, expected) -> {
            byte[] plain = PakCodec.decrypt(input, expected, keys);
            if (expected.resource().equals(PNG.toString())) decoded.add(plain);
            return plain;
        });
        return new Fixture(pack, original, reads, decoded);
    }

    @Test void shippedCatalogRegistersOneActivePackAndDecryptsRealSkin() throws Exception {
        assertNull(PakSkinPackTest.class.getResource("/assets/reverie_dreams/textures/entity/player/skin/reimu.png"),
                "Encrypted skins must not ship a plaintext copy");
        var packs = new ArrayList<Pack>();
        PakSkinPacks.SOURCE.loadPacks(packs::add);
        assertEquals(1, packs.size(), "The shipped catalog must register exactly one client pack");
        Pack pack = packs.getFirst();
        assertEquals(PakSkinPacks.PACK_ID, pack.getId());
        try (PackResources resources = pack.open()) {
            var image = resources.getResource(PackType.CLIENT_RESOURCES, PNG);
            assertNotNull(image);
            try (InputStream in = image.get()) {
                BufferedImage decoded = ImageIO.read(in);
                assertNotNull(decoded);
                assertEquals(64, decoded.getWidth());
                assertEquals(64, decoded.getHeight());
            }
        }
    }

    @Test void cachesCiphertextButNeverRetainsDecodedPngBetweenReads() throws Exception {
        Fixture fixture = fixture(0xff123456, false);
        try (var pack = fixture.pack) {
            for (int i = 0; i < 2; i++) {
                try (var stream = pack.getResource(PackType.CLIENT_RESOURCES, PNG).get()) {
                    assertArrayEquals(fixture.original, stream.readAllBytes());
                }
            }
            assertEquals(1, fixture.reads.get());
            assertEquals(2, fixture.decoded.size());
            for (byte[] image : fixture.decoded) assertArrayEquals(new byte[image.length], image);
        }
    }

    @Test void packCloseErasesAnImageEvenIfItsReaderDidNotFinish() throws Exception {
        Fixture fixture = fixture(0xff123456, false);
        var stream = fixture.pack.getResource(PackType.CLIENT_RESOURCES, PNG).get();
        assertEquals(137, stream.read());
        fixture.pack.close();
        byte[] image = fixture.decoded.getFirst();
        assertArrayEquals(new byte[image.length], image);
        assertThrows(IOException.class, stream::read);
    }

    @Test void realResourceManagerFindsAndEnumeratesVirtualPngAndArbitraryResources() throws Exception {
        Fixture fixture = fixture(0xff123456, false);
        try (var manager = new MultiPackResourceManager(PackType.CLIENT_RESOURCES, List.of(fixture.pack))) {
            assertArrayEquals(fixture.original, manager.getResource(PNG).orElseThrow().open().readAllBytes());
            var listed = manager.listResources("textures/entity/player/skin", id -> id.getPath().endsWith(".png"));
            assertEquals(Set.of(PNG), listed.keySet());
            assertArrayEquals(fixture.original, listed.get(PNG).open().readAllBytes());
            assertEquals(1, fixture.reads.get());
            try (var in = manager.getResource(CONFIG).orElseThrow().open()) {
                assertEquals(CONFIG_JSON, new String(in.readAllBytes(), StandardCharsets.UTF_8));
            }
            assertEquals(1, manager.getResourceStack(PNG).size());
            assertEquals(1, manager.listResourceStacks("textures", id -> id.equals(PNG)).get(PNG).size());
            assertTrue(manager.getResource(PNG.withSuffix(".mcmeta")).isEmpty(),
                    "PAK2 catalogs do not carry a separate metadata resource");
        }
        assertThrows(IOException.class, () -> fixture.pack.getResource(PackType.CLIENT_RESOURCES, PNG).get());
    }

    @Test void arbitraryPackedPngsAreNotForcedToSkinDimensionsButSkinsAre() throws Exception {
        var keys = keys();
        String iconPath = "textures/gui/pak_icon.png";
        String iconFile = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa.pak";
        Identifier icon = Identifier.parse("reverie_dreams:" + iconPath);
        String json = catalog(row("reverie_dreams:" + iconPath, iconFile, OTHER_SLOT) + ","
                + row("reverie_dreams:" + SKIN_PATH, SKIN_FILE, SKIN_SLOT));
        byte[] iconPng = png(32, 0xff00ff00);
        byte[] skinPng = png(64, 0xff0000ff);
        Map<String, byte[]> source = Map.of(
                SkinPackManifest.CATALOG, encoded(json, keys),
                SkinPackManifest.PACK_DIRECTORY + iconFile,
                PakCodec.encrypt(iconPng, new PakAsset("reverie_dreams", icon.toString(), OTHER_SLOT), keys),
                SkinPackManifest.PACK_DIRECTORY + SKIN_FILE,
                PakCodec.encrypt(skinPng, new PakAsset("reverie_dreams", "reverie_dreams:" + SKIN_PATH, SKIN_SLOT), keys));
        var pack = new PakSkinPack(location(PakSkinPacks.PACK_ID),
                path -> source.containsKey(path) ? new ByteArrayInputStream(source.get(path)) : null,
                (input, expected) -> PakCodec.decrypt(input, expected, keys));
        try (pack) {
            assertArrayEquals(iconPng, pack.getResource(PackType.CLIENT_RESOURCES, icon).get().readAllBytes());
            assertArrayEquals(skinPng, pack.getResource(PackType.CLIENT_RESOURCES, PNG).get().readAllBytes());
            Identifier wrongId = Identifier.parse("reverie_dreams:textures/entity/player/skin/wrong.png");
            String wrongJson = catalog(row(wrongId.toString(), SKIN_FILE, SKIN_SLOT));
            byte[] wrongCipher = iconPngEncoded(keys);
            try (var wrongPack = new PakSkinPack(location("test/wrong"),
                    path -> path.equals(SkinPackManifest.CATALOG) ? new ByteArrayInputStream(encoded(wrongJson, keys))
                            : path.endsWith(SKIN_FILE) ? new ByteArrayInputStream(wrongCipher) : null,
                    (input, expected) -> PakCodec.decrypt(input, expected, keys))) {
                assertThrows(IOException.class, () -> wrongPack.getResource(PackType.CLIENT_RESOURCES, wrongId).get());
            }
        }
    }

    private static byte[] iconPngEncoded(KeyRegistry keys) throws Exception {
        return PakCodec.encrypt(png(32, 0xff123456),
                new PakAsset("reverie_dreams", "reverie_dreams:textures/entity/player/skin/wrong.png", SKIN_SLOT), keys);
    }

    @Test void ordinaryHigherPngOverridesWithoutDecryptingLowerPak() throws Exception {
        Fixture fixture = fixture(0xff000001, false); byte[] replacement = png(64, 0xff000002);
        try (var manager = new MultiPackResourceManager(PackType.CLIENT_RESOURCES,
                List.of(fixture.pack, new PlainPack(Map.of(PNG, replacement), null)))) {
            assertArrayEquals(replacement, manager.getResource(PNG).orElseThrow().open().readAllBytes());
            assertEquals(0, fixture.reads.get());
            assertEquals(2, manager.getResourceStack(PNG).size());
            assertArrayEquals(replacement, manager.listResources("textures", id -> id.equals(PNG)).get(PNG).open().readAllBytes());
        }
    }

    @Test void higherPakOverridesLowerPlainPngAndBrokenPakDoesNotSilentlyFallback() throws Exception {
        for (boolean corrupt : new boolean[]{false, true}) {
            Fixture fixture = fixture(0xff000003, corrupt);
            try (var manager = new MultiPackResourceManager(PackType.CLIENT_RESOURCES,
                    List.of(new PlainPack(Map.of(PNG, png(64, 0xff000004)), null), fixture.pack))) {
                var resource = manager.getResource(PNG).orElseThrow();
                if (corrupt) assertThrows(IOException.class, resource::open);
                else assertArrayEquals(fixture.original, resource.open().readAllBytes());
            }
        }
    }

    @Test void vanillaPackFiltersCanHideVirtualPng() throws Exception {
        Fixture fixture = fixture(0xff000005, false);
        var filter = ResourceFilterSection.TYPE.codec().parse(JsonOps.INSTANCE,
                JsonParser.parseString("{\"block\":[{\"namespace\":\"reverie_dreams\",\"path\":\"textures/entity/player/skin/.*\"}]}"))
                .getOrThrow();
        try (var manager = new MultiPackResourceManager(PackType.CLIENT_RESOURCES,
                List.of(fixture.pack, new PlainPack(Map.of(), filter)))) {
            assertTrue(manager.getResource(PNG).isEmpty());
            assertTrue(manager.listResources("textures", id -> id.equals(PNG)).isEmpty());
            assertEquals(0, fixture.reads.get());
        }
    }

    @Test void reloadUsesNewPackAndChangedImageWithoutOldCache() throws Exception {
        Fixture before = fixture(0xff001100, false), after = fixture(0xff002200, false);
        try (var manager = new MultiPackResourceManager(PackType.CLIENT_RESOURCES, List.of(before.pack))) {
            assertArrayEquals(before.original, manager.getResource(PNG).orElseThrow().open().readAllBytes());
        }
        try (var manager = new MultiPackResourceManager(PackType.CLIENT_RESOURCES, List.of(after.pack))) {
            assertArrayEquals(after.original, manager.getResource(PNG).orElseThrow().open().readAllBytes());
        }
        assertFalse(Arrays.equals(before.original, after.original));
    }

    @Test void catalogRejectsUnknownDuplicatesTraversalAndOversizedInput() {
        String base = defaultCatalog();
        for (String bad : new String[]{
                base.replace("\"version\":2", "\"version\":1"),
                base.replace("\"version\":2", "\"version\":\"2\""),
                base.replace("\"owner\":\"reverie_dreams\"", "\"owner\":\"other\""),
                base.replace("{\"version\":2", "{\"extra\":0,\"version\":2"),
                base.replace(",\"slot\":\"" + SKIN_SLOT + "\"", ""),
                base.replace(SKIN_FILE, OTHER_FILE),
                base.replace(SKIN_FILE, "reimu.png"),
                base.replace(SKIN_FILE, "ABCDEF0123456789ABCDEF0123456789.pak"),
                base.replace("reverie_dreams:" + SKIN_PATH, "reverie_dreams:textures/../secret.png"),
                base.replace("reverie_dreams:" + SKIN_PATH, "other:textures/entity/player/skin/reimu.png"),
                "{\"version\":2,\"owner\":\"reverie_dreams\",\"entries\":[]}",
                "[".repeat(100) + "]".repeat(100),
                "{}", "[", base.substring(0, base.length() - 1)}) {
            assertThrows(IOException.class, () -> SkinPackManifest.read(bad.getBytes(StandardCharsets.UTF_8)), bad);
        }
        assertThrows(IOException.class, () -> SkinPackManifest.read(new byte[SkinPackManifest.MAX_CATALOG_BYTES + 1]));
    }

    @Test void missingOrBrokenCatalogProvidesNoResources() throws Exception {
        var missing = new PakSkinPack(location("test/missing"), path -> null,
                (input, expected) -> { throw new AssertionError("No decoder call expected"); });
        assertNull(missing.getResource(PackType.CLIENT_RESOURCES, PNG));
        missing.listResources(PackType.CLIENT_RESOURCES, "reverie_dreams", "textures", (id, supplier) -> fail(id.toString()));
        var broken = new PakSkinPack(location("test/broken"),
                path -> new ByteArrayInputStream(new byte[]{(byte)0xff}),
                (input, expected) -> { throw new cc.thonly.pak.PakException("broken catalog"); });
        assertNull(broken.getResource(PackType.CLIENT_RESOURCES, PNG));
    }

    @Test void serverDataAndUnlistedSkinsAreNeverProvided() throws Exception {
        Fixture fixture = fixture(0, false);
        try (var pack = fixture.pack) {
            assertNull(pack.getResource(PackType.SERVER_DATA, PNG));
            assertTrue(pack.getNamespaces(PackType.SERVER_DATA).isEmpty());
            assertNull(pack.getResource(PackType.CLIENT_RESOURCES, Identifier.parse("reverie_dreams:textures/entity/player/skin/rabbit_unit_0.png")));
            assertEquals(0, fixture.reads.get());
        }
    }

    private static final class PlainPack implements PackResources {
        private final Map<Identifier, byte[]> data;
        private final ResourceFilterSection filter;
        PlainPack(Map<Identifier, byte[]> data, ResourceFilterSection filter) { this.data = data; this.filter = filter; }
        @Override public IoSupplier<InputStream> getRootResource(String... paths) { return null; }
        @Override public IoSupplier<InputStream> getResource(PackType type, Identifier id) {
            byte[] value = data.get(id); return value == null ? null : () -> new ByteArrayInputStream(value);
        }
        @Override public void listResources(PackType type, String ns, String prefix, ResourceOutput out) {
            data.forEach((id, value) -> { if (id.getNamespace().equals(ns) && id.getPath().startsWith(prefix + "/")) out.accept(id, () -> new ByteArrayInputStream(value)); });
        }
        @Override public Set<String> getNamespaces(PackType type) { return Set.of("reverie_dreams"); }
        @Override @SuppressWarnings("unchecked") public <T> T getMetadataSection(MetadataSectionType<T> type) { return type == ResourceFilterSection.TYPE ? (T)filter : null; }
        @Override public PackLocationInfo location() { return PakSkinPackTest.location("test/plain"); }
        @Override public void close() {}
    }
}
