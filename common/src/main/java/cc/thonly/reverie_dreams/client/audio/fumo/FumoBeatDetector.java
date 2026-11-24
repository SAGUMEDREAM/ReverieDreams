package cc.thonly.reverie_dreams.client.audio.fumo;

/**
 * Low-frequency kick onsets drive motion; a tempo estimate only adjusts the rebound duration.
 */
public final class FumoBeatDetector {
    private record Beat(long time, float strength, double period) {
    }

    private volatile Beat lastBeat = new Beat(Long.MIN_VALUE, 0, .28);
    private final FumoTempoTracker tempo = new FumoTempoTracker();
    private PcmFormat currentFormat;
    private KickBand[] bands;
    private double energy, fullEnergy, average, previous, onsetAverage, pendingRms, pendingStrength, peakRms, valleyRms;
    private int frames, windows;
    private long lastOnset = Long.MIN_VALUE, pendingTime = Long.MIN_VALUE;

    public void resetAnalysis() {
        energy = fullEnergy = average = previous = onsetAverage = pendingRms = pendingStrength = 0;
        peakRms = valleyRms = 0;
        frames = windows = 0;
        lastOnset = pendingTime = Long.MIN_VALUE;
        currentFormat = null;
        bands = null;
        tempo.reset();
        // An existing animation is allowed to decay smoothly when switching devices.
    }

    public void accept(byte[] pcm, int count, PcmFormat format, long now) {
        if (count < 0 || (long) count * format.frameBytes() > pcm.length)
            throw new IllegalArgumentException("Truncated PCM");
        if (!format.equals(currentFormat)) {
            resetAnalysis();
            currentFormat = format;
            bands = new KickBand[format.channels()];
            for (int i = 0; i < bands.length; i++)
                bands[i] = new KickBand(format.sampleRate());
        }
        int window = Math.max(1, format.sampleRate() / 100);
        for (int frame = 0; frame < count; frame++) {
            double frameEnergy = 0, frameFullEnergy = 0;
            for (int channel = 0; channel < format.channels(); channel++) {
                float value = format.sample(pcm, frame * format.frameBytes() + channel * format.bits() / 8);
                double low = bands[channel].accept(value);
                frameEnergy += low * low;
                frameFullEnergy += value * value;
            }
            energy += frameEnergy / format.channels();
            fullEnergy += frameFullEnergy / format.channels();
            if (++frames >= window) {
                long end = now - (long) (count - frame - 1) * 1_000_000_000L / format.sampleRate();
                observe(Math.sqrt(energy / frames), Math.sqrt(fullEnergy / frames), end);
                energy = fullEnergy = 0;
                frames = 0;
            }
        }
    }

    private void observe(double rms, double fullRms, long time) {
        double onset = Math.max(0, rms - previous);
        boolean lowPresent = rms >= .004 && fullRms >= .002 && rms >= fullRms * .18;
        if (pendingTime != Long.MIN_VALUE) {
            // Confirm low-frequency body in the next window, rather than a single broadband click.
            if (lowPresent && rms >= pendingRms * .55) {
                lastOnset = pendingTime;
                peakRms = valleyRms = Math.max(pendingRms, rms);
                tempo.addKick(lastOnset);
                float bpm = tempo.estimate(time).bpm();
                double period = bpm > 0 ? Math.clamp(60 / bpm * .56, .14, .34) : .28;
                lastBeat = new Beat(lastOnset, (float) pendingStrength, period);
            }
            pendingTime = Long.MIN_VALUE;
        }
        if (lastOnset != Long.MIN_VALUE) {
            if (time - lastOnset <= 100_000_000L)
                peakRms = Math.max(peakRms, rms);
            valleyRms = Math.min(valleyRms, rms);
        }
        boolean separated = lastOnset == Long.MIN_VALUE || time - lastOnset >= 140_000_000L;
        boolean rearmed = lastOnset == Long.MIN_VALUE || valleyRms < peakRms * .6;
        if (windows >= 5 && lowPresent && separated && rearmed
                && onset > Math.max(.0015, onsetAverage * 2.5)
                && rms > Math.max(.005, average * 1.45)) {
            pendingTime = time;
            pendingRms = rms;
            pendingStrength = Math.clamp(.35 + (rms - average) * 5, .35, 1);
        }
        // Learn an already-playing bass during warm-up instead of comparing it with silence.
        average += (rms - average) * (windows < 5 ? .5 : .1);
        onsetAverage += (onset - onsetAverage) * .05;
        previous = rms;
        windows++;
    }

    public float pulse(long now) {
        Beat beat = lastBeat;
        if (beat.time == Long.MIN_VALUE || now < beat.time)
            return 0;
        double seconds = (now - beat.time) / 1_000_000_000.0;
        if (seconds > beat.period * 3)
            return 0;
        return (float) (beat.strength * Math.sin(seconds * Math.PI * 2 / beat.period) * Math.exp(-seconds / (beat.period * .57)));
    }

    public FumoTempoTracker.Estimate tempo(long now) {
        return tempo.estimate(now);
    }

    /**
     * Capture-clock timestamp of the last confirmed kick, or MIN_VALUE before the first one.
     */
    public long lastKickTime() {
        return lastBeat.time;
    }

    public static float horizontalScale(float base, float pulse) {
        return Math.clamp(base * (1 + .10f * pulse), .5f, 2f);
    }

    public static float verticalScale(float base, float pulse) {
        return Math.clamp(base * (1 - .16f * pulse), .5f, 2f);
    }

    private static final class KickBand {
        private final Biquad high, low1, low2;

        KickBand(int sampleRate) {
            high = new Biquad(sampleRate, 35, Math.sqrt(.5), true);
            low1 = new Biquad(sampleRate, 180, .541196100146197, false);
            low2 = new Biquad(sampleRate, 180, 1.306562964876377, false);
        }

        double accept(double sample) {
            return low2.accept(low1.accept(high.accept(sample)));
        }
    }

    /**
     * RBJ/W3C Audio EQ Cookbook high/low-pass coefficients, normalized by a0.
     */
    private static final class Biquad {
        private final double b0, b1, b2, a1, a2;
        private double z1, z2;

        Biquad(int sampleRate, double frequency, double q, boolean high) {
            double omega = 2 * Math.PI * frequency / sampleRate;
            double cos = Math.cos(omega), alpha = Math.sin(omega) / (2 * q), a0 = 1 + alpha;
            b0 = (high ? 1 + cos : 1 - cos) / (2 * a0);
            b1 = (high ? -(1 + cos) : 1 - cos) / a0;
            b2 = b0;
            a1 = -2 * cos / a0;
            a2 = (1 - alpha) / a0;
        }

        double accept(double value) {
            double result = b0 * value + z1;
            z1 = b1 * value - a1 * result + z2;
            z2 = b2 * value - a2 * result;
            if (Math.abs(z1) + Math.abs(z2) < 1e-24)
                z1 = z2 = 0; // Flush inaudible tails before denormals.
            return result;
        }
    }
}
