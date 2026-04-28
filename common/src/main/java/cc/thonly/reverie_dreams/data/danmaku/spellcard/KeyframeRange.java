package cc.thonly.reverie_dreams.data.danmaku.spellcard;

import cc.thonly.reverie_dreams.data.danmaku.spellcard.function.KeyframeFunction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceKey;

import java.util.HashMap;
import java.util.Map;

public final class KeyframeRange {
    public static final Codec<KeyframeRange> CODEC = RecordCodecBuilder.create(x -> x.group(
            ResourceKey.codec(KeyframeFunctions.KEY).fieldOf("key").forGetter(KeyframeRange::key),
            Codec.unboundedMap(Codec.STRING, Codec.FLOAT).optionalFieldOf("params", Map.of()).forGetter(KeyframeRange::params)
    ).apply(x, KeyframeRange::new));
    private final ResourceKey<KeyframeFunction> key;
    private final Map<String, Float> params;
    private KeyframeFunction function;

    public KeyframeRange(ResourceKey<KeyframeFunction> key, Map<String, Float> params) {
        this.key = key;
        this.params = params;
    }

    private ResourceKey<KeyframeFunction> key() {
        return this.key;
    }

    private Map<String, Float> params() {
        return this.params;
    }

    public KeyframeFunction createFunction() {
        if (this.function == null) {
            this.function = KeyframeFunctions.getOrEmpty(this.key, this.params);
        }
        return this.function;
    }

    public KeyframeRange copy() {
        return new KeyframeRange(this.key, new HashMap<>(this.params));
    }

    public static KeyframeRange empty(float start, float end) {
        Map<String, Float> params = new HashMap<>();
        params.put("start", start);
        params.put("end", end);
        return new KeyframeRange(KeyframeFunctions.LINEAR, params);
    }

    public static KeyframeRange empty() {
        Map<String, Float> params = new HashMap<>();
        params.put("start", 0f);
        params.put("end", 180f);
        return new KeyframeRange(KeyframeFunctions.LINEAR, params);
    }
}
