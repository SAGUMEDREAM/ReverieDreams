package cc.thonly.reverie_dreams.client.audio.radio;

import cc.thonly.reverie_dreams.block.decor.RecordPlayerMedia;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import net.minecraft.client.sounds.AudioStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Duration;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * One URL's decoder. HTTP/decoding/seek never run on Minecraft's audio or game thread.
 */
public final class RadioStream implements AudioStream {
    public static final URI STATION = URI.create(RecordPlayerMedia.DEFAULT_URL);
    private static final Logger LOGGER = LoggerFactory.getLogger(RadioStream.class);
    private static final AudioFormat FORMAT = new AudioFormat(RadioPcm.RATE, 16, 1, true, false);

    public enum Status {CONNECTING, PLAYING, RECONNECTING, FINISHED, CLOSED}

    private final ArrayBlockingQueue<short[]> queue = new ArrayBlockingQueue<>(40);
    private final AtomicReference<InputStream> input = new AtomicReference<>();
    private final AtomicReference<CompletableFuture<?>> request = new AtomicReference<>();
    private final HttpClient http;
    private final Thread worker;
    private final ScheduledExecutorService watchdog;
    private volatile Status status = Status.CONNECTING;
    private volatile boolean closed, finite, ended;
    private volatile long lastProgress = System.nanoTime(), framesDecoded, position;
    private short[] pending;
    private int offset;
    private final URI station;
    private final long retryMillis;
    private final long resumePosition;

    public RadioStream() {
        this(STATION, 2000, 0);
    }

    public RadioStream(String url, long samples) {
        this(URI.create(RecordPlayerMedia.normalize(url)), 2000, samples);
    }

    RadioStream(URI station, long retryMillis) {
        this(station, retryMillis, 0);
    }

    RadioStream(URI station, long retryMillis, long samples) {
        this.station = URI.create(RecordPlayerMedia.normalize(station.toString()));
        this.retryMillis = retryMillis;
        this.resumePosition = Math.max(0, samples);
        // Redirects are followed manually so every hop passes the HTTP/HTTPS-only check.
        http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).followRedirects(HttpClient.Redirect.NEVER).build();
        watchdog = Executors.newSingleThreadScheduledExecutor(task -> {
            var t = new Thread(task, "reverie-radio-watchdog");
            t.setDaemon(true);
            return t;
        });
        worker = new Thread(this::decode, "reverie-radio-decoder");
        worker.setDaemon(true);
        watchdog.scheduleAtFixedRate(() -> {
            if (!closed && !ended && queue.remainingCapacity() > 0 && System.nanoTime() - lastProgress > TimeUnit.SECONDS.toNanos(15))
                cancelConnection();
        }, 5, 5, TimeUnit.SECONDS);
        worker.start();
    }

    public Status status() {
        return status;
    }

    public long framesDecoded() {
        return framesDecoded;
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean workerAlive() {
        return worker.isAlive();
    }

    public boolean isFinite() {
        return finite;
    }

    public synchronized boolean isFinished() {
        return ended && queue.isEmpty() && (pending == null || offset == pending.length);
    }

    /**
     * PCM consumed by Minecraft, excluding buffering silence and frames decoded ahead.
     */
    public long positionSamples() {
        return position;
    }

    @Override
    public AudioFormat getFormat() {
        return FORMAT;
    }

    @Override
    public synchronized ByteBuffer read(int requested) {
        int samples = Math.max(0, Math.min(requested / 2, RadioPcm.RATE / 10));
        ByteBuffer result = ByteBuffer.allocateDirect(samples * 2).order(ByteOrder.LITTLE_ENDIAN);
        if (closed)
            return result.flip();
        for (int n = 0; n < samples; n++) {
            if (pending == null || offset == pending.length) {
                pending = queue.poll();
                offset = 0;
            }
            if (pending == null && ended)
                break;
            result.putShort(pending == null ? 0 : pending[offset++]);
            if (pending != null && finite)
                position++;
        }
        return result.flip();
    }

    private HttpResponse<InputStream> connect() throws Exception {
        URI url = station;
        for (int redirects = 0; redirects <= 5 && !closed; redirects++) {
            var future = http.sendAsync(HttpRequest.newBuilder(url).timeout(Duration.ofSeconds(10))
                    .header("Icy-MetaData", "0").header("User-Agent", "ReverieDreams-RecordPlayer/2").GET().build(), HttpResponse.BodyHandlers.ofInputStream());
            request.set(future);
            if (closed) {
                future.cancel(true);
                throw new IOException("Closed");
            }
            var response = future.get();
            input.set(response.body());
            if (closed) {
                cancelConnection();
                throw new IOException("Closed");
            }
            if (response.statusCode() == 301 || response.statusCode() == 302 || response.statusCode() == 303
                    || response.statusCode() == 307 || response.statusCode() == 308) {
                String location = response.headers().firstValue("location").orElseThrow(() -> new IOException("Missing redirect URL"));
                url = URI.create(RecordPlayerMedia.normalize(url.resolve(location).toString()));
                cancelConnection();
            } else {
                if (response.statusCode() != 200)
                    throw new IOException("Audio HTTP " + response.statusCode());
                return response;
            }
        }
        throw new IOException("Too many audio redirects");
    }

    private void decode() {
        int failures = 0;
        long decodedPosition = 0;
        boolean classified = false;
        while (!closed) {
            long before = framesDecoded;
            try {
                lastProgress = System.nanoTime();
                var response = connect();
                String type = response.headers().firstValue("content-type").orElse("").toLowerCase(Locale.ROOT);
                if (type.contains("text/") || type.contains("json"))
                    throw new IOException("Non-audio response");
                int interval = response.headers().firstValueAsLong("icy-metaint").stream().filter(n -> n > 0 && n <= 16_777_216).mapToInt(n -> (int) n).findFirst().orElse(0);
                boolean file = response.headers().firstValueAsLong("content-length").orElse(-1) > 0
                        && response.headers().map().keySet().stream().noneMatch(key -> key.toLowerCase(Locale.ROOT).startsWith("icy-"));
                if (!classified || file != finite) {
                    finite = file;
                    position = file && !classified ? resumePosition : 0;
                    decodedPosition = position;
                    classified = true;
                }
                long skip = finite ? decodedPosition : 0, decoded = 0;
                Bitstream bitstream = new Bitstream(new BufferedInputStream(new IcyInputStream(response.body(), interval), 8192));
                try {
                    Decoder decoder = new Decoder();
                    RadioPcm pcm = new RadioPcm();
                    Header header;
                    while (!closed && (header = bitstream.readFrame()) != null) {
                        SampleBuffer frame = (SampleBuffer) decoder.decodeFrame(header, bitstream);
                        short[] chunk = pcm.convert(frame.getBuffer(), frame.getBufferLength(), frame.getChannelCount(), frame.getSampleFrequency());
                        lastProgress = System.nanoTime();
                        long end = decoded + chunk.length;
                        if (end > skip) {
                            int discard = (int) Math.max(0, skip - decoded);
                            short[] audible = discard == 0 ? chunk : Arrays.copyOfRange(chunk, discard, chunk.length);
                            if (audible.length > 0)
                                queue.put(audible);
                            decodedPosition = end;
                        }
                        decoded = end;
                        bitstream.closeFrame();
                        framesDecoded++;
                        status = Status.PLAYING;
                    }
                    if (!closed && finite && decoded > 0) {
                        // A changed/shorter file may invalidate a bookmark. Restart once at zero.
                        if (skip >= decoded && queue.isEmpty()) {
                            position = decodedPosition = 0;
                            continue;
                        }
                        ended = true;
                        status = Status.FINISHED;
                        break;
                    }
                    if (!closed)
                        throw new EOFException("Live audio ended");
                } finally {
                    bitstream.close();
                }
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception error) {
                // Never log user URL/query tokens or request/redirect exception messages.
                if (!closed)
                    LOGGER.debug("Network audio disconnected ({}); retrying", error.getClass().getSimpleName());
            } finally {
                cancelConnection();
            }
            if (closed)
                break;
            status = Status.RECONNECTING;
            failures = framesDecoded - before >= 100 ? 0 : Math.min(failures + 1, 6);
            try {
                Thread.sleep(retryDelay(retryMillis, failures));
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        if (!ended)
            status = Status.CLOSED;
        watchdog.shutdownNow();
        http.shutdownNow();
    }

    static long retryDelay(long base, int failures) {
        return Math.min(60000, base * (1L << Math.min(5, Math.max(0, failures - 1))));
    }

    private void cancelConnection() {
        var future = request.getAndSet(null);
        if (future != null)
            future.cancel(true);
        var body = input.getAndSet(null);
        if (body != null)
            try {
                body.close();
            } catch (IOException ignored) {
            }
    }

    @Override
    public void close() {
        if (closed)
            return;
        closed = true;
        status = Status.CLOSED;
        cancelConnection();
        worker.interrupt();
        watchdog.shutdownNow();
        http.shutdownNow();
        queue.clear();
        synchronized (this) {
            pending = null;
            offset = 0;
        }
    }
}
