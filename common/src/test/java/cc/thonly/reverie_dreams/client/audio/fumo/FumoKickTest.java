package cc.thonly.reverie_dreams.client.audio.fumo;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

class FumoKickTest {
    private static final long START = 1_000_000_000L;

    private static double kick(double age) {
        if (age < 0 || age > .14) return 0;
        double envelope = (1 - Math.exp(-age / .002)) * Math.exp(-age / .035);
        double phase = 2 * Math.PI * (55 * age + 85 * .018 * (1 - Math.exp(-age / .018)));
        return .65 * envelope * Math.sin(phase);
    }

    private static double train(double time, double spacing, int beats) {
        double elapsed = time - .25;
        int beat = (int) Math.floor(elapsed / spacing);
        return beat < 0 || beat >= beats ? 0 : kick(elapsed - beat * spacing);
    }

    private static double highHits(double time) {
        double age = time % .25;
        return .7 * Math.exp(-age / .025) * Math.sin(2 * Math.PI * 1600 * time);
    }

    @Test
    void kickBodyTriggersOnceAtEachAttackInsteadOfAtTheHiHatsBetweenThem() {
        Stream stream = new Stream(48000, 2, true);
        stream.feed(5.25, t -> train(t, .5, 10) + highHits(t));
        assertEquals(10, stream.hits.size(), stream.hits.toString());
        for (int i = 0; i < 10; i++) {
            double delay = stream.hits.get(i) - (.25 + i * .5);
            assertTrue(delay >= 0 && delay <= .06, "Kick response delay: " + delay);
        }
        var estimate = stream.detector.tempo(stream.now());
        assertEquals(120, estimate.bpm(), 3);
        assertTrue(estimate.confidence() >= .7);
    }

    @Test
    void loudHighFrequencyAttacksAreRejectedDespiteTheirLargeVolumeJumps() {
        Stream stream = new Stream(48000, 2, false);
        stream.feed(5, FumoKickTest::highHits);
        assertTrue(stream.hits.isEmpty(), stream.hits.toString());
        assertEquals(0, stream.detector.tempo(stream.now()).bpm());
    }

    @Test
    void sustainedBassDcAndSubBassDoNotKeepRetriggering() {
        for (DoubleUnaryOperator signal : List.<DoubleUnaryOperator>of(
                t -> t < .25 ? 0 : .3 * Math.sin(2 * Math.PI * 80 * t),
                t -> .3 + .3 * Math.sin(2 * Math.PI * 10 * t))) {
            Stream stream = new Stream(48000, 1, false);
            stream.feed(5, signal);
            assertTrue(stream.hits.size() <= 1, stream.hits.toString());
            assertEquals(0, stream.detector.pulse(stream.now()));
            assertEquals(0, stream.detector.tempo(stream.now()).bpm());
        }
    }

    @Test
    void kickRemainsDetectableOverSteadyBassAndChangingMidrangeBackground() {
        Stream stream = new Stream(44100, 2, false);
        stream.feed(4.25, t -> train(t, .5, 8) + .055 * Math.sin(2 * Math.PI * 80 * t)
                + (.15 + .1 * Math.sin(t * 4)) * Math.sin(2 * Math.PI * 800 * t));
        assertEquals(8, stream.hits.size(), stream.hits.toString());
        assertEquals(120, stream.detector.tempo(stream.now()).bpm(), 3);
    }

    @Test
    void supportedSampleRatesAndOppositePhaseChannelsKeepTheSameKickCount() {
        for (int rate : new int[]{8000, 22050, 44100, 48000, 96000, 192000, 384000}) {
            Stream stream = new Stream(rate, 2, true);
            stream.feed(2.25, t -> train(t, .5, 4));
            assertEquals(4, stream.hits.size(), "sampleRate=" + rate + " " + stream.hits);
            assertEquals(120, stream.detector.tempo(stream.now()).bpm(), 3);
        }
        Stream surround = new Stream(48000, 8, true);
        surround.feed(2.25, t -> train(t, .5, 4));
        assertEquals(4, surround.hits.size());
    }

    @Test
    void bufferFragmentationDoesNotChangeDetectionOrTempo() {
        Stream regular = new Stream(48000, 2, true);
        Stream irregular = new Stream(48000, 2, true);
        regular.feed(3.25, t -> train(t, .5, 6));
        irregular.feed(3.25, t -> train(t, .5, 6), 137, 53, 823, 17, 480);
        assertEquals(regular.hits, irregular.hits);
        assertEquals(regular.detector.tempo(regular.now()), irregular.detector.tempo(irregular.now()));
    }

    @Test
    void stoppingTheKickStopsMotionEvenWhenTheTempoWasConfident() {
        Stream stream = new Stream(48000, 1, false);
        stream.feed(4.25, t -> train(t, .5, 8));
        assertEquals(120, stream.detector.tempo(stream.now()).bpm(), 3);
        int hits = stream.hits.size();
        stream.feed(4, t -> 0);
        assertEquals(hits, stream.hits.size());
        assertEquals(0, stream.detector.pulse(stream.now()));
        assertEquals(FumoTempoTracker.Estimate.UNKNOWN, stream.detector.tempo(stream.now()));
    }

    @Test
    void resettingOrChangingFormatCannotCarryOldTempoAndFilterStateIntoAnotherDevice() {
        Stream stream = new Stream(48000, 2, false);
        stream.feed(3.25, t -> train(t, .5, 6));
        assertTrue(stream.detector.tempo(stream.now()).bpm() > 0);
        stream.detector.resetAnalysis();
        assertEquals(FumoTempoTracker.Estimate.UNKNOWN, stream.detector.tempo(stream.now()));
        byte[] silence = new byte[441 * 4];
        PcmFormat changed = new PcmFormat(44100, 1, 32, 4, true);
        stream.detector.accept(silence, 441, changed, stream.now() + 10_000_000L);
        assertEquals(FumoTempoTracker.Estimate.UNKNOWN, stream.detector.tempo(stream.now() + 10_000_000L));
        assertThrows(IllegalArgumentException.class, () -> stream.detector.accept(new byte[1], 1, changed, stream.now()));
    }

    @Test
    void tempoNeedsConsistentIntervalsAndExpiresAfterSilence() {
        FumoTempoTracker tracker = new FumoTempoTracker();
        long time = START;
        tracker.addKick(time);
        for (int i = 0; i < 2; i++) { time += 500_000_000L; tracker.addKick(time); }
        assertEquals(FumoTempoTracker.Estimate.UNKNOWN, tracker.estimate(time));
        for (int i = 0; i < 8; i++) { time += (i % 2 == 0 ? 490 : 510) * 1_000_000L; tracker.addKick(time); }
        assertEquals(120, tracker.estimate(time).bpm(), 2);
        assertTrue(tracker.estimate(time).confidence() > .9);
        assertEquals(FumoTempoTracker.Estimate.UNKNOWN, tracker.estimate(time + 4_000_000_000L));
        tracker.reset();
        assertEquals(FumoTempoTracker.Estimate.UNKNOWN, tracker.estimate(time));
    }

    @Test
    void aMissedKickDoesNotHalveAnEstablishedTempoAndNewTempoCanReplaceTheOldOne() {
        FumoTempoTracker tracker = new FumoTempoTracker();
        long time = START;
        tracker.addKick(time);
        for (int i = 0; i < 8; i++) { time += 500_000_000L; tracker.addKick(time); }
        time += 1_000_000_000L;
        tracker.addKick(time);
        assertEquals(120, tracker.estimate(time).bpm(), 1);
        for (int i = 0; i < 14; i++) { time += 333_333_333L; tracker.addKick(time); }
        assertEquals(180, tracker.estimate(time).bpm(), 1);
        tracker.addKick(time - 1); // Clock discontinuity must invalidate the estimate.
        assertEquals(FumoTempoTracker.Estimate.UNKNOWN, tracker.estimate(time));
    }

    @Test
    void irregularIntervalsDoNotInventAConfidentSongBpm() {
        FumoTempoTracker tracker = new FumoTempoTracker();
        long time = START;
        tracker.addKick(time);
        for (int millis : new int[]{310, 475, 807, 1160, 625, 277, 1410}) {
            time += millis * 1_000_000L;
            tracker.addKick(time);
        }
        assertEquals(FumoTempoTracker.Estimate.UNKNOWN, tracker.estimate(time));
    }

    private static final class Stream {
        final FumoBeatDetector detector = new FumoBeatDetector();
        final List<Double> hits = new ArrayList<>();
        final PcmFormat format;
        final boolean opposite;
        long frames, lastHit = Long.MIN_VALUE;
        Stream(int rate, int channels, boolean opposite) {
            format = new PcmFormat(rate, channels, 32, channels * 4, true);
            this.opposite = opposite;
        }
        long now() { return START + frames * 1_000_000_000L / format.sampleRate(); }
        void feed(double seconds, DoubleUnaryOperator signal, int... sizes) {
            long end = frames + Math.round(seconds * format.sampleRate());
            int cursor = 0;
            while (frames < end) {
                int count = (int) Math.min(end - frames, sizes.length == 0 ? format.sampleRate() / 100 : sizes[cursor++ % sizes.length]);
                ByteBuffer bytes = ByteBuffer.allocate(count * format.frameBytes()).order(ByteOrder.LITTLE_ENDIAN);
                for (int i = 0; i < count; i++) {
                    float sample = (float) signal.applyAsDouble((frames + i) / (double) format.sampleRate());
                    for (int channel = 0; channel < format.channels(); channel++) bytes.putFloat(opposite && channel % 2 != 0 ? -sample : sample);
                }
                frames += count;
                detector.accept(bytes.array(), count, format, now());
                long hit = detector.lastKickTime();
                if (hit != Long.MIN_VALUE && hit != lastHit) { hits.add((hit - START) / 1_000_000_000.0); lastHit = hit; }
            }
        }
    }
}
