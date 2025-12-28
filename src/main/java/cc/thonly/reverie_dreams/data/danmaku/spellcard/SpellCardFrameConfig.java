package cc.thonly.reverie_dreams.data.danmaku.spellcard;

import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SpellCardFrameConfig {
    public static final Codec<SpellCardFrameConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DanmakuType.CODEC.fieldOf("type").forGetter(SpellCardFrameConfig::getType),
            Codec.INT.optionalFieldOf("color", -1).forGetter(SpellCardFrameConfig::getColor),
            Codec.INT.optionalFieldOf("density", 1).forGetter(SpellCardFrameConfig::getDensity),
            Codec.INT.optionalFieldOf("tick_delay", 0).forGetter(SpellCardFrameConfig::getTickDelay),
            Codec.INT.optionalFieldOf("tick_interval", 5).forGetter(SpellCardFrameConfig::getTickInterval),
            Codec.INT.optionalFieldOf("tick_duration", 20).forGetter(SpellCardFrameConfig::getTickDuration),
            Codec.FLOAT.optionalFieldOf("speed", 0.5f).forGetter(SpellCardFrameConfig::getSpeed),
            KeyframeRange.CODEC.optionalFieldOf("pitch_range", KeyframeRange.empty(0, 180)).forGetter(SpellCardFrameConfig::getPitchRange),
            KeyframeRange.CODEC.optionalFieldOf("yaw_range", KeyframeRange.empty(-180, 180)).forGetter(SpellCardFrameConfig::getYawRange),
            Codec.BOOL.fieldOf("sync").forGetter(SpellCardFrameConfig::isSync),
            Codec.BOOL.fieldOf("random_color").forGetter(SpellCardFrameConfig::isRandomColor)
    ).apply(instance, SpellCardFrameConfig::new));

    private final DanmakuType type;
    private int color = -1;
    private int density = 1;
    private int tickDelay = 0;
    private int tickInterval = 5;
    private int tickDuration = 20;
    private float speed = 0.5f;
    private KeyframeRange pitchRange = KeyframeRange.empty(0, 180);
    private KeyframeRange yawRange = KeyframeRange.empty(-180, 180);
    private boolean sync = true;
    private boolean randomColor = false;

    public SpellCardFrameConfig(DanmakuType type) {
        this.type = type;
    }

    public SpellCardFrameConfig copy() {
        return new SpellCardFrameConfig(
                this.type,
                this.color,
                this.density,
                this.tickDelay,
                this.tickInterval,
                this.tickDuration,
                this.speed,
                this.pitchRange.copy(),
                this.yawRange.copy(),
                this.sync,
                this.randomColor
        );
    }

    public SpellCardFrameConfig withColor(int color) {
        this.color = color;
        return this;
    }

    public SpellCardFrameConfig withDensity(int density) {
        this.density = Math.max(1, density); // 修复逻辑
        return this;
    }

    public SpellCardFrameConfig withTickDelay(int tick) {
        this.tickDelay = tick;
        return this;
    }

    public SpellCardFrameConfig withTickInterval(int tick) {
        this.tickInterval = tick;
        return this;
    }

    public SpellCardFrameConfig withTickDuration(int tick) {
        this.tickDuration = tick;
        return this;
    }

    public SpellCardFrameConfig withSpeed(float speed) {
        this.speed = speed;
        return this;
    }

    @Deprecated
    public SpellCardFrameConfig withPitchStartAt(float start, float end) {
        this.pitchRange = KeyframeRange.empty(start, end);
        return this;
    }

    @Deprecated
    public SpellCardFrameConfig withYawStartAt(float start, float end) {
        this.yawRange = KeyframeRange.empty(start, end);
        return this;
    }

    public SpellCardFrameConfig withPitchFunction(KeyframeRange range) {
        this.pitchRange = range;
        return this;
    }

    public SpellCardFrameConfig withYawFunction(KeyframeRange range) {
        this.yawRange = range;
        return this;
    }

    public SpellCardFrameConfig sync() {
        this.sync = true;
        return this;
    }

    public SpellCardFrameConfig async() {
        this.sync = false;
        return this;
    }

    public SpellCardFrameConfig setRandomColor() {
        this.randomColor = true;
        return this;
    }

}
