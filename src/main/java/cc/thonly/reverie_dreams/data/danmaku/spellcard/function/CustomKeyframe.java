package cc.thonly.reverie_dreams.data.danmaku.spellcard.function;

import com.mojang.serialization.Codec;

import java.util.function.Function;

public class CustomKeyframe implements KeyframeFunction {
    private final float start;
    private final float end;
    private final Function<Float, Float> customFunction;

    public CustomKeyframe(float start, float end, Function<Float, Float> customFunction) {
        this.start = start;
        this.end = end;
        this.customFunction = customFunction;
    }

    @Override
    public float sample(float t) {
        float value = this.customFunction.apply(t);
        return this.start + (this.end - this.start) * value;
    }

}
