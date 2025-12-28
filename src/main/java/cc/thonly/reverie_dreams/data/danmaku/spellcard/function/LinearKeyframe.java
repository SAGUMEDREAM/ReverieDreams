package cc.thonly.reverie_dreams.data.danmaku.spellcard.function;

public class LinearKeyframe implements KeyframeFunction {

    private final float start;
    private final float end;

    public LinearKeyframe(float start, float end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public float sample(float t) {
        return start + (end - start) * t;
    }
}