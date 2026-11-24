package cc.thonly.reverie_dreams.registry.content.danmaku.touhou;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

/** One catalog row. The optional factory is only used when registering playable patterns. */
public record TouhouSpell(Identifier id, String chineseName, int originalFrames,
                          @Nullable Supplier<DanmakuPattern> patternFactory, @Nullable String characterKey) {
    public TouhouSpell {
        Objects.requireNonNull(id);
        if (originalFrames <= 0) {
            throw new IllegalArgumentException("Missing ECL spell duration: " + id);
        }
        if (chineseName.isBlank()) {
            throw new IllegalArgumentException("Missing spell name: " + id);
        }
    }

    public static TouhouSpell catalogOnly(String path, String chineseName, int frames) {
        return new TouhouSpell(ReverieDreams.id(path), chineseName, frames, null, null);
    }

    public static TouhouSpell implemented(String path, String chineseName, int frames,
                                          Supplier<DanmakuPattern> patternFactory) {
        return new TouhouSpell(ReverieDreams.id(path), chineseName, frames,
                Objects.requireNonNull(patternFactory), null);
    }

    public TouhouSpell withCharacter(String characterKey) {
        return new TouhouSpell(id, chineseName, originalFrames, patternFactory,
                Objects.requireNonNull(characterKey));
    }

    int durationTicks() {
        return SpellTiming.framesToTicks(this.originalFrames);
    }
}
