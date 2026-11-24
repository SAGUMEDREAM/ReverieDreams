package cc.thonly.reverie_dreams.openai;

import cc.thonly.reverie_dreams.ai.AIChatPolicy;
import cc.thonly.reverie_dreams.ai.AIRequestContext;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import static org.junit.jupiter.api.Assertions.*;

class AIRequestContextTest {
    @Test void internalServerFailureKeepsItsCauseInsteadOfBecomingCancellation() {
        var failure = new IllegalStateException("fixture internal failure");
        try (var request = context(Runnable::run, () -> true, TimeUnit.MINUTES.toNanos(1))) {
            assertSame(failure, assertThrows(IllegalStateException.class, () -> request.onServer(() -> { throw failure; })));
        }
    }

    @Test void asynchronousFailureKeepsTheOriginalException() throws Exception {
        var failure = new NullPointerException("fixture internal failure");
        try (var server = Executors.newSingleThreadExecutor(); var request = context(server, () -> true, TimeUnit.MINUTES.toNanos(1))) {
            assertSame(failure, assertThrows(NullPointerException.class, () -> request.onServer(() -> { throw failure; })));
        }
    }

    @Test void serverCancellationRemainsCancellation() {
        var failure = new CancellationException("fixture invalidated");
        try (var request = context(Runnable::run, () -> true, TimeUnit.MINUTES.toNanos(1))) {
            var actual = assertThrows(CancellationException.class, () -> request.onServer(() -> { throw failure; }));
            assertTrue(actual == failure || actual.getCause() == failure);
        }
    }

    private AIRequestContext context(Executor server, BooleanSupplier guard, long duration) {
        return new AIRequestContext(server, guard, new AIRequestContext.Settings("http://127.0.0.1", "fixture-only", "fixture", "", .7, 512),
                "old order", n -> null, duration);
    }
    @Test void expiredQueueEntryCannotStartTheProvider() {
        try (var request = context(Runnable::run, () -> true, -1)) {
            var calls = new AtomicInteger();
            assertThrows(CancellationException.class, () -> request.run(calls::incrementAndGet));
            assertEquals(0, calls.get());
        }
    }
    @Test void manualControlCancelsQueuedWorldMutationAndReleasesWorker() throws Exception {
        var serverQueue = new LinkedBlockingQueue<Runnable>();
        try (var worker = Executors.newSingleThreadExecutor(); var request = context(serverQueue::add, () -> true, TimeUnit.MINUTES.toNanos(1))) {
            var mutations = new AtomicInteger();
            var result = worker.submit(() -> request.run(() -> request.onServer(mutations::incrementAndGet)));
            var queued = serverQueue.poll(2, TimeUnit.SECONDS);
            assertNotNull(queued);
            request.cancel();
            assertInstanceOf(CancellationException.class, assertThrows(ExecutionException.class, () -> result.get(2, TimeUnit.SECONDS)).getCause());
            queued.run();
            assertEquals(0, mutations.get());
        }
    }
    @Test void serverGuardIsRecheckedInsideTheQueuedAction() throws Exception {
        var queue = new LinkedBlockingQueue<Runnable>();
        var revision = new AtomicInteger();
        try (var worker = Executors.newSingleThreadExecutor(); var request = context(queue::add, () -> revision.get() == 0, TimeUnit.MINUTES.toNanos(1))) {
            var actions = new AtomicInteger();
            var result = worker.submit(() -> request.run(() -> request.onServer(actions::incrementAndGet)));
            var queued = queue.poll(2, TimeUnit.SECONDS); assertNotNull(queued);
            revision.incrementAndGet(); queued.run();
            assertThrows(ExecutionException.class, () -> result.get(2, TimeUnit.SECONDS));
            assertEquals(0, actions.get());
        }
    }
    @Test void cancellationStopsAnAttachedHttpCallAndRejectsReplacement() {
        try (var request = context(Runnable::run, () -> true, TimeUnit.MINUTES.toNanos(1))) {
            var call = new OkHttpClient().newCall(new Request.Builder().url("http://127.0.0.1/").build());
            request.attach(call); request.cancel();
            assertTrue(call.isCanceled());
            var replacement = call.clone();
            assertThrows(CancellationException.class, () -> request.attach(replacement));
            assertTrue(replacement.isCanceled());
        }
    }
    @Test void serverShutdownInvalidatesOnlyItsOwnConversations() {
        Executor first = Runnable::run, second = command -> command.run();
        try (var a = context(first, () -> true, TimeUnit.MINUTES.toNanos(1)); var b = context(second, () -> true, TimeUnit.MINUTES.toNanos(1))) {
            AIRequestContext.cancelForServer(first);
            assertFalse(a.active()); assertTrue(b.active());
        }
    }
    @Test void everyPermissionGateIsRequiredEvenWithSavedCredentials() {
        assertTrue(AIChatPolicy.enabled(true, false, "http://fixture", "fixture-only", true));
        assertFalse(AIChatPolicy.enabled(false, false, "http://fixture", "fixture-only", true));
        assertFalse(AIChatPolicy.enabled(true, true, "http://fixture", "fixture-only", true));
        assertFalse(AIChatPolicy.enabled(true, false, "http://fixture", " ", true));
        assertFalse(AIChatPolicy.enabled(true, false, "", "fixture-only", true));
        assertFalse(AIChatPolicy.enabled(true, false, "http://fixture", "fixture-only", false));
    }
}
