package cc.thonly.reverie_dreams.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class IdCompletableFuture {

    private static final Map<Identifier, List<CompletableFuture<?>>> TASKS = new Object2ObjectLinkedOpenHashMap<>();

    private IdCompletableFuture() {
    }

    public static synchronized void register(
            Identifier id,
            CompletableFuture<?> future
    ) {
        TASKS.computeIfAbsent(
                id,
                key -> new ArrayList<>()
        ).add(future);
    }

    public static synchronized CompletableFuture<Void> waitFor(
            Identifier id
    ) {
        List<CompletableFuture<?>> futures = TASKS.get(id);

        if (futures == null || futures.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.allOf(
                futures.toArray(CompletableFuture[]::new)
        );
    }

    public static synchronized void clear(
            Identifier id
    ) {
        TASKS.remove(id);
    }

    public static synchronized void clearAll() {
        TASKS.clear();
    }
}