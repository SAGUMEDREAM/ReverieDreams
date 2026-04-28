package cc.thonly.reverie_dreams.data.danmaku.spellcard.function;

public class SineKeyframe implements KeyframeFunction {
    private final float start;
    private final float end;
    private final float frequency;
    private final float phase;

    public SineKeyframe(float start, float end, float frequency, float phase) {
        this.start = start;
        this.end = end;
        this.frequency = frequency;
        this.phase = phase;
    }

    @Override
    public float sample(float t) {
        float value = (float) Math.sin(t * Math.PI * 2 * frequency + phase) * 0.5f + 0.5f; // Sine curve formula
        return start + (end - start) * value;
    }
}
