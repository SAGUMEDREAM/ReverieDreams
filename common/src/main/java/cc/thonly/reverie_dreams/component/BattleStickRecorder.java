package cc.thonly.reverie_dreams.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.*;
import org.jetbrains.annotations.NotNull;

@Builder(toBuilder = true)
public record BattleStickRecorder(@NotNull String target_0, @NotNull String target_1) {
    public static final Codec<BattleStickRecorder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("target_0").forGetter(BattleStickRecorder::target_0),
            Codec.STRING.fieldOf("target_1").forGetter(BattleStickRecorder::target_1)
    ).apply(instance, BattleStickRecorder::new));

    public BattleStickRecorder() {
        this("" ,"");
    }

    public static BattleStickRecorder empty() {
        return new BattleStickRecorder();
    }
}
