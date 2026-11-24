package cc.thonly.reverie_dreams.data.danmaku.spellcard;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * The frame data stored on an old spellcard item. Each cast gets its own working copy.
 */
public final class LegacySpellcardDefinition {
    private static final Codec<List<List<SpellCardFrameConfig>>> FRAMES_CODEC =
            Codec.list(Codec.list(SpellCardFrameConfig.COMPONENT_CODEC));
    public static final Codec<LegacySpellcardDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FRAMES_CODEC.fieldOf("frames").forGetter(definition -> definition.frames)
    ).apply(instance, LegacySpellcardDefinition::new));

    private final List<List<SpellCardFrameConfig>> frames;
    private final int durationTicks;

    public LegacySpellcardDefinition(List<List<SpellCardFrameConfig>> frames) {
        this.frames = copyFrames(frames);
        this.durationTicks = this.frames.stream().flatMap(List::stream)
                .mapToInt(frame -> frame.getTickDelay() + frame.getTickDuration()).reduce(0, Math::max);
    }

    int durationTicks() {
        return this.durationTicks;
    }

    List<List<SpellCardFrameConfig>> copyFrames() {
        return new ArrayList<>(copyFrames(this.frames));
    }

    private static List<List<SpellCardFrameConfig>> copyFrames(List<List<SpellCardFrameConfig>> frames) {
        return frames.stream().map(group -> group.stream().map(SpellCardFrameConfig::copy).toList()).toList();
    }
}
