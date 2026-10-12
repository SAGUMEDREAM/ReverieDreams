package cc.thonly.reverie_dreams.data.danmaku.stg;

import cc.thonly.reverie_dreams.registry.BuiltinObject;
import cc.thonly.reverie_dreams.registry.SerializableProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * STG 图案（符卡）：一组 {@link DanmakuEmitter} 的时间线。
 *
 * <p>图案可以通过 Java 注册（内置符卡），也可以通过数据包放在
 * {@code data/<namespace>/danmaku_pattern/<name>.json} 中热重载。
 */
public record DanmakuPattern(List<DanmakuEmitter> emitters) implements SerializableProvider<DanmakuPattern>, BuiltinObject {
    public static final Codec<DanmakuPattern> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DanmakuEmitter.CODEC.listOf().fieldOf("emitters").forGetter(DanmakuPattern::emitters)
    ).apply(instance, DanmakuPattern::new));

    public DanmakuPattern {
        emitters = List.copyOf(emitters);
    }

    public static DanmakuPattern of(DanmakuEmitter... emitters) {
        return new DanmakuPattern(Arrays.asList(emitters));
    }

    public int maxDuration() {
        int max = 0;
        for (DanmakuEmitter emitter : this.emitters) {
            max = Math.max(max, emitter.delay() + Math.max(0, emitter.duration()));
        }
        return max;
    }

    @Override
    public Codec<DanmakuPattern> getCodec() {
        return CODEC;
    }
}
