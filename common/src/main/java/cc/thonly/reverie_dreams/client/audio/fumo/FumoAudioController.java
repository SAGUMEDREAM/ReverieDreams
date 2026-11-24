package cc.thonly.reverie_dreams.client.audio.fumo;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * One capture worker per client; changing devices cancels the old session before starting another.
 */
public final class FumoAudioController implements AutoCloseable {
    public enum Status {IDLE, LISTENING, ERROR}

    private record Selection(AudioSource source, String device) {
    }

    private final Supplier<AudioCaptureBackend> backend;
    private final ExecutorService capture = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "Reverie Fumo audio");
        thread.setDaemon(true);
        return thread;
    });
    private final ExecutorService enumeration = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "Reverie audio devices");
        thread.setDaemon(true);
        return thread;
    });
    private final FumoBeatDetector detector = new FumoBeatDetector();
    private volatile Status status = Status.IDLE;
    private volatile String error = "";
    private Selection selection;
    private Future<?> session;
    private AtomicBoolean running;
    private long retryAfter;
    private boolean closed;

    public FumoAudioController() {
        this(AudioCaptureBackend::create);
    }

    FumoAudioController(Supplier<AudioCaptureBackend> backend) {
        this.backend = backend;
    }

    public CompletableFuture<List<AudioDevice>> devices(AudioSource source) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return backend.get().devices(source);
            } catch (Exception | LinkageError e) {
                throw new CompletionException(e);
            }
        }, enumeration);
    }

    public synchronized void configure(boolean enabled, AudioSource source, String device) {
        if (closed)
            return;
        Selection wanted = new Selection(source, device);
        if (!enabled) {
            stop();
            selection = null;
            return;
        }
        if (wanted.equals(selection) && session != null && !session.isDone())
            return;
        if (wanted.equals(selection) && System.nanoTime() < retryAfter)
            return;
        stop();
        selection = wanted;
        error = "";
        status = Status.LISTENING;
        AtomicBoolean token = new AtomicBoolean(true);
        running = token;
        session = capture.submit(() -> {
            detector.resetAnalysis();
            try {
                backend.get().capture(source, device, detector, token::get);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } catch (Exception | LinkageError e) {
                synchronized (this) {
                    if (running == token && token.get()) {
                        error = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                        status = Status.ERROR;
                        retryAfter = System.nanoTime() + 5_000_000_000L;
                    }
                }
            }
        });
    }

    private void stop() {
        if (running != null)
            running.set(false);
        if (session != null)
            session.cancel(true);
        running = null;
        session = null;
        status = Status.IDLE;
    }

    public float pulse(long now) {
        return detector.pulse(now);
    }

    public Status status() {
        return status;
    }

    public String error() {
        return error;
    }

    @Override
    public synchronized void close() {
        if (closed)
            return;
        stop();
        closed = true;
        capture.shutdownNow();
        enumeration.shutdownNow();
    }
}
