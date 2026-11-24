package cc.thonly.reverie_dreams.client.audio.radio;

/**
 * Strongest-path union: adding identical radios never amplifies the signal.
 */
public final class RadioVolume {
    public static final double RANGE = 32;

    private RadioVolume() {
    }

    public static float atDistance(double distance) {
        double t = Math.clamp((distance - 3) / (RANGE - 3), 0, 1);
        return (float) (1 - t * t * (3 - 2 * t));
    }

    public static float merge(float current, float candidate) {
        return Math.max(current, candidate);
    }

    public static float smooth(float current, float target) {
        return current + (target - current) * .28f;
    }
}
