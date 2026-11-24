package cc.thonly.reverie_dreams.client.audio.fumo;

import org.junit.jupiter.api.Test;
import java.nio.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.BooleanSupplier;
import static org.junit.jupiter.api.Assertions.*;

class FumoAudioTest {
    private static byte[] window(double amplitude, boolean oppositeStereo) {
        return window(amplitude, oppositeStereo, 0, 1000);
    }
    private static byte[] window(double amplitude, boolean oppositeStereo, long offset, double frequency) {
        ByteBuffer bytes = ByteBuffer.allocate(480 * 8).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < 480; i++) {
            float value = (float) (amplitude * Math.sin((offset + i) * Math.PI * 2 * frequency / 48000));
            bytes.putFloat(value).putFloat(oppositeStereo ? -value : value);
        }
        return bytes.array();
    }
    @Test void pcmDecodesSignedIntegerFloatAndInvalidFloatSafely() {
        assertEquals(-1f, new PcmFormat(48000, 1, 16, 2, false).sample(new byte[] {0, (byte) 128}, 0));
        assertEquals(-1f, new PcmFormat(48000, 1, 24, 3, false).sample(new byte[] {0, 0, (byte) 128}, 0));
        assertEquals(-1f, new PcmFormat(48000, 1, 32, 4, false).sample(new byte[] {0, 0, 0, (byte) 128}, 0));
        assertEquals(0f, new PcmFormat(48000, 1, 8, 1, false).sample(new byte[] {(byte) 128}, 0));
        ByteBuffer bytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putDouble(.25);
        assertEquals(.25f, new PcmFormat(48000, 1, 64, 8, true).sample(bytes.array(), 0));
        bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(Float.NaN);
        assertEquals(0, new PcmFormat(48000, 1, 32, 4, true).sample(bytes.array(), 0));
        assertThrows(IllegalArgumentException.class, () -> new PcmFormat(48000, 2, 24, 2, false));
    }
    @Test void repeatedOnsetsAnimateEvenWithOppositeStereoPhaseAndThenSettle() {
        FumoBeatDetector detector = new FumoBeatDetector(); PcmFormat format = new PcmFormat(48000, 2, 32, 8, true);
        long time = 1_000_000_000L; int peaks = 0;
        for (int beat = 0; beat < 6; beat++) {
            for (int i = 0; i < 30; i++) {
                time += 10_000_000; detector.accept(window(i < 6 ? .45 : 0, true, (beat * 30L + i) * 480, 80), 480, format, time);
                if (i == 3 && detector.pulse(time) > .05) peaks++;
            }
        }
        assertTrue(peaks >= 5, "Detect repeated low-frequency attacks even with opposite-phase stereo");
        assertEquals(0, detector.pulse(time + 2_000_000_000L));
    }
    @Test void silenceAndSteadyToneDoNotKeepRetriggering() {
        FumoBeatDetector detector = new FumoBeatDetector(); PcmFormat format = new PcmFormat(48000, 2, 32, 8, true);
        long time = 1_000_000_000L;
        for (int i = 0; i < 100; i++) { time += 10_000_000; detector.accept(window(0, false), 480, format, time); }
        assertEquals(0, detector.pulse(time));
        for (int i = 0; i < 300; i++) { time += 10_000_000; detector.accept(window(.2, false), 480, format, time); }
        assertEquals(0, detector.pulse(time));
    }
    @Test void squashReboundIsBoundedAtMaximumGrowth() {
        for (float base : new float[] {1, 1.5f, 2}) for (int i = -100; i <= 100; i++) {
            float pulse = i / 100f, x = FumoBeatDetector.horizontalScale(base, pulse), y = FumoBeatDetector.verticalScale(base, pulse);
            assertTrue(x > 0 && x <= 2); assertTrue(y > 0 && y <= 2);
        }
        assertEquals(2, FumoBeatDetector.horizontalScale(2, 1));
        assertTrue(FumoBeatDetector.verticalScale(2, 1) < 2);
    }
    @Test void deviceSwitchAndDisableCloseSessionsWithoutOverlap() throws Exception {
        AtomicInteger open = new AtomicInteger(), total = new AtomicInteger(), max = new AtomicInteger();
        AudioCaptureBackend backend = new AudioCaptureBackend() {
            @Override public List<AudioDevice> devices(AudioSource source) { return List.of(new AudioDevice("x", "Friendly name")); }
            @Override public void capture(AudioSource source, String id, FumoBeatDetector detector, BooleanSupplier running) throws Exception {
                int count = open.incrementAndGet(); max.accumulateAndGet(count, Math::max); total.incrementAndGet();
                try { while (running.getAsBoolean()) Thread.sleep(2); } finally { open.decrementAndGet(); }
            }
        };
        try (FumoAudioController controller = new FumoAudioController(() -> backend)) {
            assertEquals("Friendly name", controller.devices(AudioSource.OUTPUT).get(1, TimeUnit.SECONDS).getFirst().name());
            controller.configure(true, AudioSource.OUTPUT, "one"); await(() -> total.get() == 1);
            controller.configure(true, AudioSource.OUTPUT, "one"); assertEquals(1, total.get());
            controller.configure(true, AudioSource.INPUT, "two"); await(() -> total.get() == 2);
            controller.configure(false, AudioSource.INPUT, "two"); await(() -> open.get() == 0);
            assertEquals(1, max.get()); assertEquals(FumoAudioController.Status.IDLE, controller.status());
        }
        assertEquals(0, open.get());
    }
    @Test void failedDeviceUsesBackoffAndCanBeDisabled() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        AudioCaptureBackend backend = new AudioCaptureBackend() {
            @Override public List<AudioDevice> devices(AudioSource source) { return List.of(); }
            @Override public void capture(AudioSource source, String id, FumoBeatDetector detector, BooleanSupplier running) {
                calls.incrementAndGet(); throw new IllegalStateException("Disconnected test endpoint");
            }
        };
        try (FumoAudioController controller = new FumoAudioController(() -> backend)) {
            controller.configure(true, AudioSource.OUTPUT, "missing");
            await(() -> controller.status() == FumoAudioController.Status.ERROR);
            for (int i = 0; i < 10; i++) controller.configure(true, AudioSource.OUTPUT, "missing");
            assertEquals(1, calls.get()); controller.configure(false, AudioSource.OUTPUT, "missing");
            assertEquals(FumoAudioController.Status.IDLE, controller.status());
        }
    }
    private static void await(BooleanSupplier condition) throws Exception {
        long end = System.nanoTime() + 1_000_000_000L;
        while (!condition.getAsBoolean() && System.nanoTime() < end) Thread.sleep(2);
        assertTrue(condition.getAsBoolean(), "Timed out waiting for capture lifecycle");
    }
}
