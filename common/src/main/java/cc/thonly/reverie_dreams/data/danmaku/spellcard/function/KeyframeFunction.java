package cc.thonly.reverie_dreams.data.danmaku.spellcard.function;

public interface KeyframeFunction {
    float sample(float t); // t ∈ [0,1]
}