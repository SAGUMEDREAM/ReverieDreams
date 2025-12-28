package cc.thonly.reverie_dreams.data.danmaku.spellcard.function;

public class TriangleKeyframe implements KeyframeFunction {
    private final float start;
    private final float end;
    private final float frequency;

    public TriangleKeyframe(float start, float end, float frequency) {
        this.start = start;
        this.end = end;
        this.frequency = frequency;
    }

    @Override
    public float sample(float t) {
        float value = 1f - Math.abs((t * frequency * 2 % 2) - 1); // Triangle wave formula
        return start + (end - start) * value;
    }
}
