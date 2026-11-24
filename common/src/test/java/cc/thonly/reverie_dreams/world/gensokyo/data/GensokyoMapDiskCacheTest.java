package cc.thonly.reverie_dreams.world.gensokyo.data;

import net.minecraft.resources.Identifier;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class GensokyoMapDiskCacheTest {
    @TempDir Path directory;
    private static final Identifier MAP = Identifier.parse("reverie_dreams:gensokyo");

    private GensokyoMapDiskCache.Layers sample(int marker) {
        return new GensokyoMapDiskCache.Layers(new short[]{0, 1024, (short) marker, -12},
                new byte[]{0, 1, (byte) marker, 15}, new short[]{0, 15, 256, (short) 65535});
    }

    @Test void sharesOneGameDirectoryCacheAndSeparatesMapIdentifiers() {
        Path root = GensokyoMapDiskCache.sharedRoot(directory);
        assertEquals(directory.resolve("cache/reverie_dreams/gensokyo-map"), root);
        var first = GensokyoMapDiskCache.load(root, MAP, "content-A", 4, () -> sample(3));
        var second = GensokyoMapDiskCache.load(root, MAP, "content-A", 4, () -> { throw new AssertionError("Must reuse shared cache"); });
        assertArrayEquals(first.heights(), second.heights());
        assertArrayEquals(first.biomes(), second.biomes());
        assertArrayEquals(first.surfaces(), second.surfaces());
        var otherMap = Identifier.parse("another:gensokyo");
        var other = GensokyoMapDiskCache.load(root, otherMap, "content-A", 4, () -> sample(9));
        assertEquals(9, other.heights()[2]);
        assertNotEquals(GensokyoMapDiskCache.mapDirectory(root, MAP), GensokyoMapDiskCache.mapDirectory(root, otherMap));
        assertFalse(Files.exists(directory.resolve("saves")));
    }

    @Test void loaderReusesSharedFilesAfterSwitchingWorldsAndClearingMemory() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Identifier fixture = Identifier.parse("test:shared_cache");
        try {
            GensokyoMapLoader.clearCache();
            GensokyoMapLoader.configureSharedCache(directory, directory.resolve("saves/first"));
            var first = GensokyoMapLoader.get(fixture);
            Path map = GensokyoMapDiskCache.mapDirectory(GensokyoMapDiskCache.sharedRoot(directory), fixture);
            Path raw = map.resolve("heightmap.bin");
            assertTrue(Files.isRegularFile(raw));
            var originalTime = Files.getLastModifiedTime(raw);
            GensokyoMapLoader.clearCache();
            GensokyoMapLoader.configureSharedCache(directory, directory.resolve("saves/second"));
            var second = GensokyoMapLoader.get(fixture);
            assertArrayEquals(new short[]{4, 5, 6, 7}, first.heights());
            assertArrayEquals(first.heights(), second.heights());
            assertEquals(originalTime, Files.getLastModifiedTime(raw));
            assertFalse(Files.exists(directory.resolve("saves/first/gensokyo-map-cache")));
            assertFalse(Files.exists(directory.resolve("saves/second/gensokyo-map-cache")));
        } finally {
            GensokyoMapLoader.clearCache();
            GensokyoMapLoader.setCacheDirectory(null);
        }
    }

    @Test void contentChangesAndSameSizeCorruptionRebuildTheCache() throws Exception {
        AtomicInteger decodes = new AtomicInteger();
        GensokyoMapDiskCache.load(directory, MAP, "A", 4, () -> { decodes.incrementAndGet(); return sample(1); });
        var newer = GensokyoMapDiskCache.load(directory, MAP, "B", 4, () -> { decodes.incrementAndGet(); return sample(2); });
        assertEquals(2, newer.heights()[2]);
        Path file = GensokyoMapDiskCache.mapDirectory(directory, MAP).resolve("heightmap.bin");
        byte[] corrupt = Files.readAllBytes(file);
        corrupt[4] ^= 1;
        Files.write(file, corrupt);
        var repaired = GensokyoMapDiskCache.load(directory, MAP, "B", 4, () -> { decodes.incrementAndGet(); return sample(2); });
        assertEquals(2, repaired.heights()[2]);
        assertEquals(3, decodes.get());
    }

    @Test void truncatedIncompleteAndInterruptedWritesRecoverWithoutPartialReads() throws Exception {
        GensokyoMapDiskCache.load(directory, MAP, "A", 4, () -> sample(1));
        Path map = GensokyoMapDiskCache.mapDirectory(directory, MAP);
        Files.write(map.resolve("surfaces.bin"), new byte[]{1});
        assertEquals(2, GensokyoMapDiskCache.load(directory, MAP, "A", 4, () -> sample(2)).heights()[2]);
        Files.delete(map.resolve("source.stamp"));
        Files.writeString(map.resolve("heightmap.bin.tmp"), "incomplete");
        assertEquals(3, GensokyoMapDiskCache.load(directory, MAP, "A", 4, () -> sample(3)).heights()[2]);
        Files.writeString(map.resolve("heightmap.bin.tmp"), "stale from an interrupted publication");
        GensokyoMapDiskCache.load(directory, MAP, "A", 4, () -> { throw new AssertionError("Completed cache remains valid"); });
        assertFalse(Files.exists(map.resolve("heightmap.bin.tmp")));
    }

    @Test void decodeFailureDoesNotDestroyThePreviousCompleteCache() {
        GensokyoMapDiskCache.load(directory, MAP, "A", 4, () -> sample(7));
        assertThrows(IllegalStateException.class, () -> GensokyoMapDiskCache.load(directory, MAP, "B", 4,
                () -> { throw new IllegalStateException("Bad compressed source"); }));
        assertEquals(7, GensokyoMapDiskCache.load(directory, MAP, "A", 4,
                () -> { throw new AssertionError("Old version must remain intact"); }).heights()[2]);
    }

    @Test void inaccessibleCacheFallsBackToDecodedArrays() throws Exception {
        Path blocked = directory.resolve("not-a-directory");
        Files.writeString(blocked, "keep");
        assertEquals(4, GensokyoMapDiskCache.load(blocked, MAP, "A", 4, () -> sample(4)).heights()[2]);
        assertEquals("keep", Files.readString(blocked));
    }

    @Test void simultaneousThreadsDecodeOnlyOnce() throws Exception {
        AtomicInteger decodes = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Callable<GensokyoMapDiskCache.Layers> task = () -> {
                start.await();
                return GensokyoMapDiskCache.load(directory, MAP, "A", 4, () -> { decodes.incrementAndGet(); return sample(8); });
            };
            var first = executor.submit(task);
            var second = executor.submit(task);
            start.countDown();
            assertArrayEquals(first.get(10, TimeUnit.SECONDS).heights(), second.get(10, TimeUnit.SECONDS).heights());
            assertEquals(1, decodes.get());
        }
    }

    @Test void anotherJvmFileLockPreventsReadingOrPublishingUntilReleased() throws Exception {
        Path map = GensokyoMapDiskCache.mapDirectory(directory, MAP);
        Files.createDirectories(map);
        Path ready = directory.resolve("ready"), release = directory.resolve("release");
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        String classpath = Path.of(GensokyoMapDiskCacheTest.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        Process process = new ProcessBuilder(java, "-cp", classpath, LockHolder.class.getName(), map.resolve("cache.lock").toString(), ready.toString(), release.toString())
                .redirectErrorStream(true).start();
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            while (!Files.exists(ready) && process.isAlive() && System.nanoTime() < deadline) Thread.sleep(10);
            assertTrue(Files.exists(ready), "Child lock probe failed to acquire the lock");
            CountDownLatch decoded = new CountDownLatch(1);
            var result = executor.submit(() -> GensokyoMapDiskCache.load(directory, MAP, "A", 4, () -> { decoded.countDown(); return sample(6); }));
            assertFalse(decoded.await(200, TimeUnit.MILLISECONDS), "Another JVM still holds the lock");
            Files.writeString(release, "release");
            assertEquals(6, result.get(10, TimeUnit.SECONDS).heights()[2]);
            assertTrue(process.waitFor(10, TimeUnit.SECONDS));
            assertEquals(0, process.exitValue());
        } finally {
            Files.writeString(release, "release");
            process.destroyForcibly();
        }
    }

    @Test void legacyCleanupKeepsSaveDataUnknownFilesAndSymbolicLinks() throws Exception {
        Path world = directory.resolve("saves/example"), legacy = world.resolve("gensokyo-map-cache");
        Files.createDirectories(legacy.resolve("unknown-directory"));
        for (String name : List.of("heightmap.bin", "biomes.bin", "surfaces.bin", "source.stamp")) Files.writeString(legacy.resolve(name), "old cache");
        Files.writeString(world.resolve("level.dat"), "save data");
        Files.writeString(legacy.resolve("unknown.txt"), "keep");
        GensokyoMapDiskCache.retireLegacy(legacy);
        assertEquals("save data", Files.readString(world.resolve("level.dat")));
        assertEquals("keep", Files.readString(legacy.resolve("unknown.txt")));
        assertTrue(Files.isDirectory(legacy.resolve("unknown-directory")));
        assertFalse(Files.exists(legacy.resolve("heightmap.bin")));
        Path outside = directory.resolve("outside");
        Files.createDirectories(outside);
        Files.writeString(outside.resolve("heightmap.bin"), "keep outside");
        try {
            Path link = directory.resolve("linked-cache");
            Files.createSymbolicLink(link, outside);
            GensokyoMapDiskCache.retireLegacy(link);
            assertEquals("keep outside", Files.readString(outside.resolve("heightmap.bin")));
        } catch (FileSystemException | UnsupportedOperationException ignored) {
            // Windows may not grant symlink creation; the nonrecursive path tests still ran.
        }
    }

    public static class LockHolder {
        public static void main(String[] args) throws Exception {
            try (var channel = FileChannel.open(Path.of(args[0]), StandardOpenOption.CREATE, StandardOpenOption.WRITE); var lock = channel.lock()) {
                Files.writeString(Path.of(args[1]), "locked");
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
                while (!Files.exists(Path.of(args[2])) && System.nanoTime() < deadline) Thread.sleep(10);
            }
        }
    }
}
