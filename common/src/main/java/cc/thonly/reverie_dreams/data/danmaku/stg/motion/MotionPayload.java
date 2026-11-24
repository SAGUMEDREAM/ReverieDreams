package cc.thonly.reverie_dreams.data.danmaku.stg.motion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * {@link MotionSegment} 的副作用参数（STG 弹幕引擎）。
 *
 * <p>供 {@link MotionSegment#SPLIT} 与 {@link MotionSegment#TRANSFORM} 段使用：
 * <ul>
 *     <li>{@code type / color / scale / lifetime} —— 生成或变形子弹时使用的弹幕类型、
 *         染色（{@code -1} 表示类型默认）、缩放（{@code <= 0} 表示 1）与寿命；</li>
 *     <li>{@code speed} —— 出膛速度（格/tick），{@code <= 0} 表示继承当前速度；</li>
 *     <li>{@code count / spread} —— SPLIT 的发数与扇形展开角度（度）。</li>
 * </ul>
 */
public record MotionPayload(@Nullable Identifier type, int color, float scale, float speed, int count, float spread,
                            int lifetime) {
    public static final int DEFAULT_COLOR = -1;

    public static final Codec<MotionPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("type").forGetter(payload -> Optional.ofNullable(payload.type())),
            Codec.INT.optionalFieldOf("color", DEFAULT_COLOR).forGetter(MotionPayload::color),
            Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(MotionPayload::scale),
            Codec.FLOAT.optionalFieldOf("speed", 0f).forGetter(MotionPayload::speed),
            Codec.INT.optionalFieldOf("count", 1).forGetter(MotionPayload::count),
            Codec.FLOAT.optionalFieldOf("spread", 30f).forGetter(MotionPayload::spread),
            Codec.INT.optionalFieldOf("lifetime", 0).forGetter(MotionPayload::lifetime)
    ).apply(instance, (type, color, scale, speed, count, spread, lifetime) ->
            new MotionPayload(type.orElse(null), color, scale, speed, count, spread, lifetime)));

    /**
     * SPLIT 段参数：以当前方向为基准展开 count 发扇形子弹。
     */
    public static MotionPayload split(int count, float spread, float speed) {
        return new MotionPayload(null, DEFAULT_COLOR, 0f, speed, count, spread, 0);
    }

    /**
     * SPLIT 段参数：展开 count 发扇形子弹并指定弹幕类型。
     */
    public static MotionPayload split(Identifier type, int color, float scale, int count, float spread, float speed, int lifetime) {
        return new MotionPayload(type, color, scale, speed, count, spread, lifetime);
    }

    /**
     * TRANSFORM 段参数：把当前子弹变形为新类型（可携带染色与缩放）。
     */
    public static MotionPayload transform(Identifier type, int color, float scale) {
        return new MotionPayload(type, color, scale, 0f, 1, 0f, 0);
    }

    public float effectiveScale() {
        return this.scale > 0f ? this.scale : 1f;
    }
}
