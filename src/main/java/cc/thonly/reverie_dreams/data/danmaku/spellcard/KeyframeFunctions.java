package cc.thonly.reverie_dreams.data.danmaku.spellcard;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.function.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class KeyframeFunctions {
    private static final Map<ResourceKey<KeyframeFunction>, KeyframeFunctionFactory> FUNCTIONS = new Object2ObjectOpenHashMap<>();
    public static final ResourceKey<Registry<KeyframeFunction>> KEY = ResourceKey.createRegistryKey(ReverieDreams.id("keyframe_function"));

    // 线性
    public static final ResourceKey<KeyframeFunction> LINEAR = key("linear");
    // 正弦
    public static final ResourceKey<KeyframeFunction> SINE = key("sine");
    // 余弦
    public static final ResourceKey<KeyframeFunction> COSINE = key("cosine");
    // 三角波
    public static final ResourceKey<KeyframeFunction> TRIANGLE = key("triangle");
    // 锯齿波
    public static final ResourceKey<KeyframeFunction> SAW = key("saw");

    public static void bootstrap() {
        KeyframeFunctions.register(LINEAR, params ->
                new LinearKeyframe(
                        getFloat(params, "start", 0f),
                        getFloat(params, "end", 1f)
                )
        );
        KeyframeFunctions.register(SINE, params ->
                new SineKeyframe(
                        getFloat(params, "start", 0f),
                        getFloat(params, "end", 1f),
                        getFloat(params, "frequency", 1f),
                        getFloat(params, "phase", 0f)
                )
        );
        KeyframeFunctions.register(COSINE, params ->
                new CosineKeyframe(
                        getFloat(params, "start", 0f),
                        getFloat(params, "end", 1f),
                        getFloat(params, "frequency", 1f),
                        getFloat(params, "phase", 0f)
                )
        );
        KeyframeFunctions.register(TRIANGLE, params ->
                new TriangleKeyframe(
                        getFloat(params, "start", 0f),
                        getFloat(params, "end", 1f),
                        getFloat(params, "frequency", 1f)
                )
        );
        KeyframeFunctions.register(SAW, params ->
                new SawKeyframe(
                        getFloat(params, "start", 0f),
                        getFloat(params, "end", 1f),
                        getFloat(params, "frequency", 1f)
                )
        );
    }

    public static void register(ResourceKey<KeyframeFunction> key,
                                KeyframeFunctionFactory factory) {
        FUNCTIONS.put(key, factory);
    }

    public static ResourceKey<KeyframeFunction> key(String name) {
        return ResourceKey.create(KEY, ReverieDreams.id(name));
    }

    public static ResourceKey<KeyframeFunction> key(ResourceLocation location) {
        return ResourceKey.create(KEY, location);
    }

    public static KeyframeFunction getOrEmpty(ResourceKey<KeyframeFunction> key,
                                              Map<String, Float> parameters) {
        KeyframeFunctionFactory factory = FUNCTIONS.get(key);
        if (factory == null) {
            return null;
        }
        return factory.create(parameters);
    }

    public static KeyframeFunction getOrThrow(ResourceKey<KeyframeFunction> key,
                                              Map<String, Float> parameters) {
        KeyframeFunctionFactory factory = FUNCTIONS.get(key);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown keyframe function: " + key.location());
        }
        return factory.create(parameters);
    }

    public static float getFloat(Map<String, Float> params, String key, float def) {
        Object v = params.get(key);
        if (v instanceof Number n) {
            return n.floatValue();
        }
        return def;
    }

    @FunctionalInterface
    public interface KeyframeFunctionFactory {
        KeyframeFunction create(Map<String, Float> params);
    }
}
