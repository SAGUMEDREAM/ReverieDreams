package cc.thonly.reverie_dreams.paper.world;

import com.google.gson.JsonParser;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class GensokyoBindingsTest {
    private static final String PADDY = "reverie_dreams:paddy_field";
    @TempDir Path temporary;

    @Test void onlyActuallyUsedCustomSurfaceIRsAreRequested() {
        assertEquals(Set.of(PADDY, "example:custom[wet=true]"), GensokyoStateBindings.customSurfaces(
                List.of("minecraft:grass_block", PADDY, "example:unused", "example:custom[wet=true]", PADDY),
                List.of(256L, 1L, 0L, 2L, 5L)));
        assertThrows(UnsupportedOperationException.class, () -> GensokyoStateBindings.customSurfaces(List.of(PADDY), List.of(1L)).clear());
    }

    @Test void invalidBatchCannotPublishPartialBindingsOrReplaceAnExistingIdentity() {
        String second = "example:second";
        var table = new GensokyoStateBindings<>(Set.of("minecraft:stone", PADDY, second), Set.of(PADDY, second), Map.of("minecraft:stone", "stone"));
        Map<String, String> before = table.snapshot();
        var parsed = new AtomicInteger();
        assertThrows(IllegalArgumentException.class, () -> table.installVerifiedBindings(Map.of(PADDY, "craftengine:custom_12", second, "craftengine:custom_14"), encoded -> {
            if (parsed.incrementAndGet() == 2) throw new IllegalArgumentException("unregistered native state");
            return encoded;
        }));
        assertEquals(2, parsed.get(), "A later parse fails after an earlier entry was staged successfully");
        assertSame(before, table.snapshot());
        assertEquals(2, table.unresolved().size());
        assertThrows(IllegalArgumentException.class, () -> table.installVerifiedBindings(Map.of("example:unused", "craftengine:custom_12"), encoded -> encoded));
        assertThrows(IllegalArgumentException.class, () -> table.installVerifiedBindings(Map.of(PADDY, "minecraft:farmland[moisture=7]"), encoded -> encoded));
        table.installVerifiedBindings(Map.of(PADDY, "craftengine:custom_12"), encoded -> encoded);
        assertFalse(before.containsKey(PADDY));
        assertEquals(List.of(second), table.unresolved());
        assertThrows(UnsupportedOperationException.class, () -> table.snapshot().put("example:bad", "bad"));
        assertThrows(IllegalStateException.class, () -> table.installVerifiedBindings(Map.of(PADDY, "craftengine:custom_13"), encoded -> encoded));
        table.installVerifiedBindings(Map.of(PADDY, "craftengine:custom_12", second, "craftengine:custom_14"), encoded -> encoded);
        assertTrue(table.unresolved().isEmpty());
    }

    @Test void sourceChunkWithLastColumnUnboundRefusesBeforeAnyWriteThenAcceptsVerifiedNativeState() throws Exception {
        try (var cache = fixture()) {
            var generator = new GensokyoGenerator(cache, GensokyoBindingsTest::blockData, id -> null);
            assertEquals(Set.of(PADDY), generator.requestedCustomStates());
            var nativeWrites = new ArrayList<String>();
            generator.generateNoise(info(() -> {}), new Random(0), 0, 0, chunkData(nativeWrites));
            assertFalse(nativeWrites.isEmpty());
            var customWrites = new ArrayList<String>();
            assertThrows(IllegalStateException.class, () -> generator.generateNoise(info(() -> {}), new Random(0), 1, 0, chunkData(customWrites)));
            assertTrue(customWrites.isEmpty(), "Earlier native columns must not be written before the last custom column is preflighted");
            assertTrue(generator.unresolved().contains("Unresolved source blockstate: " + PADDY));
            generator.installVerifiedBindings(Map.of(PADDY, "craftengine:custom_543"));
            assertFalse(generator.unresolved().contains("Unresolved source blockstate: " + PADDY));
            generator.generateNoise(info(() -> {}), new Random(0), 1, 0, chunkData(customWrites));
            assertEquals(1, customWrites.stream().filter("craftengine:custom_543"::equals).count());
        }
    }

    @Test void bindingInstalledDuringGenerationIsOnlyVisibleToTheNextInvocation() throws Exception {
        try (var cache = fixture()) {
            var generator = new GensokyoGenerator(cache, GensokyoBindingsTest::blockData, id -> null);
            var writes = new ArrayList<String>();
            WorldInfo publishesDuringHeightCheck = info(() -> generator.installVerifiedBindings(Map.of(PADDY, "craftengine:custom_543")));
            assertThrows(IllegalStateException.class, () -> generator.generateNoise(publishesDuringHeightCheck, new Random(0), 1, 0, chunkData(writes)));
            assertTrue(writes.isEmpty());
            assertFalse(generator.unresolved().contains("Unresolved source blockstate: " + PADDY));
            generator.generateNoise(info(() -> {}), new Random(0), 1, 0, chunkData(writes));
            assertEquals(1, writes.stream().filter("craftengine:custom_543"::equals).count());
        }
    }

    @Test void deferredInitialResolverBindsOnlyVerifiedStatesBeforeTheFirstCustomWrite() throws Exception {
        try (var cache = fixture()) {
            var invocations = new AtomicInteger();
            var pending = new GensokyoGenerator(cache, GensokyoBindingsTest::blockData, id -> null);
            pending.installInitialBindings(requested -> {
                assertEquals(Set.of(PADDY), requested);
                invocations.incrementAndGet();
                return Map.of(); // Engine has not bound its source semantics yet.
            });
            var pendingWrites = new ArrayList<String>();
            assertThrows(IllegalStateException.class, () -> pending.generateNoise(info(() -> {}), new Random(0), 1, 0, chunkData(pendingWrites)));
            assertTrue(pendingWrites.isEmpty());
            assertTrue(pending.unresolved().contains("Unresolved source blockstate: " + PADDY));

            var ready = new GensokyoGenerator(cache, GensokyoBindingsTest::blockData, id -> null);
            ready.installInitialBindings(requested -> {
                assertEquals(Set.of(PADDY), requested);
                invocations.incrementAndGet();
                return Map.of(PADDY, "craftengine:custom_543");
            });
            assertEquals(2, invocations.get());
            var firstWrites = new ArrayList<String>();
            ready.generateNoise(info(() -> {}), new Random(0), 1, 0, chunkData(firstWrites));
            assertEquals(1, firstWrites.stream().filter("craftengine:custom_543"::equals).count());
            assertFalse(ready.unresolved().contains("Unresolved source blockstate: " + PADDY));
        }
    }

    @Test void featureRequestsStaySeparateAndInitialResolverSplitsTheUnionBeforeAnyTerrainWrite() throws Exception {
        try (var cache = fixture()) {
            var baseline = new GensokyoGenerator(cache, GensokyoBindingsTest::blockData, id -> null);
            assertFalse(baseline.shouldGenerateDecorations());
            assertTrue(baseline.getDefaultPopulators(null).isEmpty());
            assertTrue(baseline.requestedFeatureStates().isEmpty());
            var generator = new GensokyoGenerator(cache, GensokyoBindingsTest::blockData, id -> null, true, GensokyoOreFeatures.bundled());
            assertEquals(Set.of(PADDY), generator.requestedCustomStates());
            assertEquals(Set.of("reverie_dreams:silver_ore", "reverie_dreams:deepslate_silver_ore", "reverie_dreams:orb_ore", "reverie_dreams:deepslate_orb_ore"), generator.requestedFeatureStates());
            assertTrue(generator.shouldGenerateDecorations());
            assertEquals(1, generator.getDefaultPopulators(null).size());
            var writes = new ArrayList<String>();
            assertThrows(IllegalStateException.class, () -> generator.generateNoise(info(() -> {}), new Random(0), 0, 0, chunkData(writes)));
            assertTrue(writes.isEmpty(), "Feature readiness is required even for an otherwise native terrain chunk");
            generator.installInitialBindings(requested -> {
                assertEquals(5, requested.size());
                var nativeStates = new HashMap<String, String>();
                int next = 500;
                for (String source : requested.stream().sorted().toList()) nativeStates.put(source, "craftengine:custom_" + next++);
                return nativeStates;
            });
            assertTrue(generator.unresolved().stream().noneMatch(issue -> issue.startsWith("Unresolved feature blockstate:")));
            generator.generateNoise(info(() -> {}), new Random(0), 1, 0, chunkData(writes));
            assertFalse(writes.isEmpty());
            assertEquals(Set.of(PADDY), generator.requestedCustomStates());
        }
    }

    private GensokyoMapCache fixture() throws Exception {
        Path source = temporary.resolve("source"), cache = temporary.resolve("cache");
        Files.createDirectories(source);
        var json = JsonParser.parseString("""
                {"format_version":1,"map_version":2,"id":"reverie_dreams:gensokyo","width":32,"depth":16,
                 "origin":[0,0],"min_y":0,"world_height":1536,"sea_level":1024,"dirt_depth":3,"biome_transition":0,
                 "spawn":{"pos":[0,1064,0]},"biome_palette":["minecraft:plains"],
                 "surface_palette":["minecraft:grass_block","reverie_dreams:paddy_field","example:unused"],
                 "layers":{"heightmap":"heightmap.delta.gz","biomes":"biomes.pal4.gz","surfaces":"surfaces.pal4.gz"},"anchors":[]}
                """);
        Files.writeString(source.resolve("map.json"), json.toString());
        var height = new ByteArrayOutputStream();
        for (int i = 0; i < 512; i++) { int delta = i < 32 ? 1063 : 0; height.write(delta & 255); height.write(delta >>> 8); }
        byte[] surfaces = new byte[256]; surfaces[255] = 1; // Last pixel: low nibble, last column of chunk1/0.
        gzip(source.resolve("heightmap.delta.gz"), height.toByteArray());
        gzip(source.resolve("biomes.pal4.gz"), new byte[256]);
        gzip(source.resolve("surfaces.pal4.gz"), surfaces);
        GensokyoMapCache.prepare(source, cache);
        return GensokyoMapCache.open(source, cache);
    }

    private static void gzip(Path path, byte[] bytes) throws Exception {
        try (var out = new GZIPOutputStream(Files.newOutputStream(path))) { out.write(bytes); }
    }
    private static BlockData blockData(String encoded) {
        return (BlockData) Proxy.newProxyInstance(BlockData.class.getClassLoader(), new Class<?>[]{BlockData.class}, (proxy, method, args) -> switch (method.getName()) {
            case "getAsString", "toString" -> encoded;
            case "clone" -> proxy;
            case "hashCode" -> encoded.hashCode();
            case "equals" -> proxy == args[0];
            default -> throw new UnsupportedOperationException(method.getName());
        });
    }
    private static WorldInfo info(Runnable heightCheck) {
        var invoked = new AtomicBoolean();
        return (WorldInfo) Proxy.newProxyInstance(WorldInfo.class.getClassLoader(), new Class<?>[]{WorldInfo.class}, (proxy, method, args) -> switch (method.getName()) {
            case "getMinHeight" -> { if (invoked.compareAndSet(false, true)) heightCheck.run(); yield 0; }
            case "getMaxHeight" -> 1536;
            default -> throw new UnsupportedOperationException(method.getName());
        });
    }
    private static ChunkGenerator.ChunkData chunkData(List<String> writes) {
        return (ChunkGenerator.ChunkData) Proxy.newProxyInstance(ChunkGenerator.ChunkData.class.getClassLoader(), new Class<?>[]{ChunkGenerator.ChunkData.class}, (proxy, method, args) -> switch (method.getName()) {
            case "getMinHeight" -> 0;
            case "getMaxHeight" -> 1536;
            case "setBlock", "setRegion" -> { writes.add(((BlockData) args[args.length - 1]).getAsString()); yield null; }
            default -> throw new UnsupportedOperationException(method.getName());
        });
    }
}
