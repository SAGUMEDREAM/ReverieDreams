package cc.thonly.reverie_dreams.client.audio.fumo;

/**
 * Bounded inter-kick interval estimate, not a full-song beat/meter transcription.
 */
public final class FumoTempoTracker {
    public record Estimate(float bpm, float confidence) {
        public static final Estimate UNKNOWN = new Estimate(0, 0);
    }

    private record Published(Estimate estimate, long time) {
    }

    private static final int HISTORY = 12;
    private final double[] intervals = new double[HISTORY];
    private int count, cursor;
    private long previous = Long.MIN_VALUE;
    private volatile Published published = new Published(Estimate.UNKNOWN, Long.MIN_VALUE);

    public void reset() {
        count = cursor = 0;
        previous = Long.MIN_VALUE;
        published = new Published(Estimate.UNKNOWN, Long.MIN_VALUE);
    }

    public void addKick(long time) {
        if (previous == Long.MIN_VALUE) {
            previous = time;
            return;
        }
        double interval = (time - previous) / 1_000_000_000.0;
        previous = time;
        if (interval <= 0 || interval > 3) {
            reset();
            previous = time;
            return;
        }
        intervals[cursor] = interval;
        cursor = (cursor + 1) % HISTORY;
        count = Math.min(count + 1, HISTORY);
        Estimate result = Estimate.UNKNOWN;
        double bestScore = 0;
        if (count >= 3) {
            for (int i = 0; i < count; i++) {
                double candidate = intervals[i];
                if (candidate < .25 || candidate > 1.5)
                    continue; // 40..240 BPM.
                double score = 0, sum = 0;
                int direct = 0;
                for (int j = 0; j < count; j++) {
                    int multiple = (int) Math.round(intervals[j] / candidate);
                    if (multiple < 1 || multiple > 4)
                        continue;
                    double normalized = intervals[j] / multiple;
                    if (Math.abs(normalized - candidate) > Math.max(.015, candidate * .08))
                        continue;
                    double weight = multiple == 1 ? 1 : .6; // A missing kick supports the estimate less strongly.
                    if (multiple == 1)
                        direct++;
                    score += weight;
                    sum += normalized * weight;
                }
                if (direct >= 3 && score > bestScore && score / count >= .7) {
                    bestScore = score;
                    result = new Estimate((float) Math.clamp(60 / (sum / score), 40, 240), (float) (score / count));
                }
            }
        }
        published = new Published(result, time);
    }

    public Estimate estimate(long now) {
        Published snapshot = published;
        double lifetime = snapshot.estimate.bpm > 0 ? Math.max(3, 180 / snapshot.estimate.bpm) : 3;
        return snapshot.time == Long.MIN_VALUE || now < snapshot.time || now - snapshot.time > lifetime * 1_000_000_000L
                ? Estimate.UNKNOWN : snapshot.estimate;
    }
}
