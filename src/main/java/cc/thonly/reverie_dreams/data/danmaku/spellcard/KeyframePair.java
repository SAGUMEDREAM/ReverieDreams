package cc.thonly.reverie_dreams.data.danmaku.spellcard;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;

@Deprecated
@Setter
@Getter
public class KeyframePair {
    public static final Codec<KeyframePair> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("start").forGetter(KeyframePair::getStart),
            Codec.FLOAT.fieldOf("end").forGetter(KeyframePair::getEnd)
    ).apply(instance, KeyframePair::new));

    private float start;
    private float end;

    public KeyframePair(float start, float end) {
        this.start = start;
        this.end = end;
        if (start > end) {
            throw new IllegalArgumentException("start must be less than end");
        }
    }
}
