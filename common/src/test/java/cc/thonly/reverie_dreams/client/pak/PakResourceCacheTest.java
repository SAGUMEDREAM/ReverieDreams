package cc.thonly.reverie_dreams.client.pak;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class PakResourceCacheTest {
    @Test void simultaneousReadsDecodeOnceAndDoNotExposeMutableCachedArray() throws Exception {
        try (var cache = new PakResourceCache(16); var pool = Executors.newFixedThreadPool(12)) {
            var calls = new AtomicInteger(); var started = new CountDownLatch(1); var release = new CountDownLatch(1);
            var results = new ArrayList<Future<byte[]>>();
            for (int i = 0; i < 12; i++) results.add(pool.submit(() -> cache.get("skin", () -> {
                calls.incrementAndGet(); started.countDown();
                try { if (!release.await(5, TimeUnit.SECONDS)) throw new IOException("timeout"); }
                catch (InterruptedException e) { throw new IOException(e); }
                return new byte[]{1, 2, 3};
            })));
            assertTrue(started.await(5, TimeUnit.SECONDS)); release.countDown();
            for (var result : results) assertArrayEquals(new byte[]{1, 2, 3}, result.get(5, TimeUnit.SECONDS));
            assertEquals(1, calls.get());
            byte[] mutable = cache.get("skin", () -> { throw new AssertionError(); }); mutable[0] = 9;
            assertEquals(1, cache.get("skin", () -> { throw new AssertionError(); })[0]);
        }
    }
    @Test void byteBudgetEvictsLeastRecentlyUsedAndRetriesFailures() throws Exception {
        try (var cache = new PakResourceCache(4)) {
            cache.get("a", () -> new byte[2]); cache.get("b", () -> new byte[2]);
            cache.get("a", () -> { throw new AssertionError(); }); cache.get("c", () -> new byte[2]);
            var reload = new AtomicInteger(); cache.get("b", () -> { reload.incrementAndGet(); return new byte[2]; });
            assertEquals(1, reload.get()); assertEquals(4, cache.cachedBytes());
            assertThrows(IOException.class, () -> cache.get("bad", () -> { throw new IOException("bad"); }));
            assertArrayEquals(new byte[]{7}, cache.get("bad", () -> new byte[]{7}));
        }
    }
    @Test void closedGenerationCannotRepopulateAndNextGenerationLoadsFreshBytes() throws Exception {
        var old = new PakResourceCache(16);
        try (var pool = Executors.newSingleThreadExecutor()) {
            var started = new CountDownLatch(1); var release = new CountDownLatch(1);
            Future<?> result = pool.submit(() -> assertThrows(IOException.class, () -> old.get("skin", () -> {
                started.countDown();
                try { release.await(5, TimeUnit.SECONDS); } catch (InterruptedException e) { throw new IOException(e); }
                return new byte[]{1};
            })));
            assertTrue(started.await(5, TimeUnit.SECONDS)); old.close(); release.countDown(); result.get(5, TimeUnit.SECONDS);
            assertEquals(0, old.cachedBytes());
            assertThrows(IOException.class, () -> old.get("skin", () -> new byte[]{1}));
            try (var next = new PakResourceCache(16)) { assertArrayEquals(new byte[]{2}, next.get("skin", () -> new byte[]{2})); }
        }
    }
}
