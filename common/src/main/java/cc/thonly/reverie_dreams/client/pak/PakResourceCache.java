package cc.thonly.reverie_dreams.client.pak;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

/**
 * Per-pack lifetime, byte-bounded LRU with one in-flight decode per resource.
 */
public final class PakResourceCache implements AutoCloseable {
    @FunctionalInterface
    public interface Loader {
        byte[] load() throws IOException;
    }

    private final long budget;
    private final Map<String, byte[]> entries = new LinkedHashMap<>(16, .75f, true);
    private final ConcurrentHashMap<String, CompletableFuture<byte[]>> inFlight = new ConcurrentHashMap<>();
    private final Semaphore decoding = new Semaphore(4);
    private long bytes;
    private boolean closed;

    public PakResourceCache(long budget) {
        if (budget < 1)
            throw new IllegalArgumentException("Cache budget must be positive");
        this.budget = budget;
    }

    public byte[] get(String key, Loader loader) throws IOException {
        synchronized (this) {
            if (closed)
                throw new IOException("PAK resource pack is closed");
            byte[] cached = entries.get(key);
            if (cached != null)
                return cached.clone();
        }
        var mine = new CompletableFuture<byte[]>();
        var existing = inFlight.putIfAbsent(key, mine);
        if (existing == null) {
            boolean acquired = false;
            try {
                decoding.acquire();
                acquired = true;
                byte[] value;
                synchronized (this) {
                    if (closed)
                        throw new IOException("PAK resource pack is closed");
                    value = entries.get(key);
                }
                if (value == null)
                    value = loader.load();
                synchronized (this) {
                    if (closed)
                        throw new IOException("PAK resource pack closed during decoding");
                    if (!entries.containsKey(key) && value.length <= budget) {
                        while (bytes + value.length > budget && !entries.isEmpty()) {
                            var first = entries.entrySet().iterator();
                            bytes -= first.next().getValue().length;
                            first.remove();
                        }
                        entries.put(key, value);
                        bytes += value.length;
                    }
                }
                mine.complete(value);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                mine.completeExceptionally(new IOException("Interrupted PAK decoding", e));
            } catch (Throwable e) {
                mine.completeExceptionally(e);
                if (e instanceof Error fatal)
                    throw fatal;
            } finally {
                if (acquired)
                    decoding.release();
                inFlight.remove(key, mine);
            }
            existing = mine;
        }
        try {
            byte[] value = existing.get();
            synchronized (this) {
                if (closed)
                    throw new IOException("PAK resource pack is closed");
            }
            return value.clone();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted waiting for PAK asset", e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof IOException io)
                throw io;
            throw new IOException("PAK asset decoding failed", e.getCause());
        }
    }

    public synchronized long cachedBytes() {
        return bytes;
    }

    @Override
    public synchronized void close() {
        closed = true;
        entries.clear();
        bytes = 0;
    }
}
