package cc.thonly.reverie_dreams.data.danmaku.spellcard.function;

public class SawKeyframe implements KeyframeFunction {
    private final float start;
    private final float end;
    private final float frequency;

    public SawKeyframe(float start, float end, float frequency) {
        this.start = start;
        this.end = end;
        this.frequency = frequency;
    }

    @Override
    public float sample(float t) {
        float value = (t * frequency) % 1f; // Sawtooth wave
        return start + (end - start) * value;
    }
}
