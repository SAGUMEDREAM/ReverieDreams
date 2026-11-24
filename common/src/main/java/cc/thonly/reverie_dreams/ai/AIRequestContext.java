package cc.thonly.reverie_dreams.ai;

import okhttp3.Call;

import java.util.Set;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * One conversation, including queue time. World guards are evaluated only by the server executor.
 */
public final class AIRequestContext implements AutoCloseable {
    public record Settings(String url, String key, String model, String proxy, double temperature, int maxTokens) {
        @Override
        public String toString() {
            return "AI request settings (credentials omitted)";
        }
    }

    private static final ThreadLocal<AIRequestContext> CURRENT = new ThreadLocal<>();
    private static final Set<AIRequestContext> ACTIVE = ConcurrentHashMap.newKeySet();
    private final Executor server;
    private final BooleanSupplier guard;
    private final Function<Integer, AIUsageBudget.Reservation> budget;
    private final long deadline;
    private final Settings settings;
    private final String input;
    private volatile boolean cancelled;
    private volatile boolean budgetDenied;
    private Call call;
    private final Set<CompletableFuture<?>> waiting = ConcurrentHashMap.newKeySet();

    public AIRequestContext(Executor server, BooleanSupplier guard, Settings settings, String input,
                            Function<Integer, AIUsageBudget.Reservation> budget, long timeoutNanos) {
        this.server = server;
        this.guard = guard;
        this.settings = settings;
        this.input = input;
        this.budget = budget;
        deadline = System.nanoTime() + timeoutNanos;
        ACTIVE.add(this);
    }

    public static AIRequestContext current() {
        var request = CURRENT.get();
        if (request == null)
            throw new CancellationException("No active AI conversation");
        return request;
    }

    public static AIRequestContext optional() {
        return CURRENT.get();
    }

    public Settings settings() {
        return settings;
    }

    public String input() {
        return input;
    }

    public long remainingNanos() {
        return Math.max(0, deadline - System.nanoTime());
    }

    public boolean active() {
        return !cancelled && remainingNanos() > 0;
    }

    public boolean validOnServer() {
        return active() && guard.getAsBoolean();
    }

    public boolean budgetDenied() {
        return budgetDenied;
    }

    public <T> T run(Supplier<T> action) {
        CURRENT.set(this);
        try {
            if (!active())
                throw new CancellationException();
            return action.get();
        } finally {
            CURRENT.remove();
        }
    }

    public <T> T onServer(Supplier<T> action) {
        var result = new CompletableFuture<T>();
        waiting.add(result);
        try {
            server.execute(() -> {
                try {
                    if (!validOnServer())
                        throw new CancellationException();
                    result.complete(action.get());
                } catch (Throwable failure) {
                    result.completeExceptionally(failure);
                    if (failure instanceof Error fatal)
                        throw fatal;
                }
            });
            return result.get(Math.max(1, remainingNanos()), TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            cancel();
            throw cancellation("AI request interrupted", e);
        } catch (TimeoutException e) {
            cancel();
            throw cancellation("AI request expired", e);
        } catch (ExecutionException e) {
            cancel();
            Throwable cause = e.getCause();
            if (cause instanceof CancellationException cancelled)
                throw cancelled;
            if (cause instanceof Error fatal)
                throw fatal;
            if (cause instanceof RuntimeException failure)
                throw failure;
            throw new CompletionException("AI server action failed", cause);
        } finally {
            waiting.remove(result);
        }
    }

    private static CancellationException cancellation(String message, Throwable cause) {
        var failure = new CancellationException(message);
        failure.initCause(cause);
        return failure;
    }

    public AIUsageBudget.Reservation reserve(int inputBytes) {
        var reservation = onServer(() -> budget.apply(inputBytes));
        if (reservation == null) {
            budgetDenied = true;
            throw new CancellationException("AI daily budget exhausted");
        }
        return reservation;
    }

    /**
     * Accounting for an already sent request survives cancellation; stopped servers keep the reservation.
     */
    public void account(Runnable receipt) {
        try {
            server.execute(receipt);
        } catch (RejectedExecutionException ignored) { /* The pre-send reservation remains charged. */ }
    }

    public synchronized void attach(Call next) {
        if (!active()) {
            next.cancel();
            throw new CancellationException();
        }
        call = next;
    }

    public synchronized void detach(Call previous) {
        if (call == previous)
            call = null;
    }

    public synchronized void cancel() {
        cancelled = true;
        if (call != null)
            call.cancel();
        waiting.forEach(result -> result.completeExceptionally(new CancellationException()));
    }

    public static void cancelForServer(Executor server) {
        for (var request : ACTIVE)
            if (request.server == server) {
                request.cancel();
                request.close();
            }
    }

    @Override
    public void close() {
        ACTIVE.remove(this);
    }
}
