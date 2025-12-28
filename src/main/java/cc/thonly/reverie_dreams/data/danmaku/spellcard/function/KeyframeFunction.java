package cc.thonly.reverie_dreams.data.danmaku.spellcard.function;

import com.mojang.serialization.Codec;

import java.util.Map;

public interface KeyframeFunction {
    float sample(float t); // t ∈ [0,1]
}