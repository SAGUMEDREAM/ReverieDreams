package cc.thonly.reverie_dreams.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
@AllArgsConstructor
public class BattleStickRecorder {
    public static final Codec<BattleStickRecorder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("target_0").forGetter(BattleStickRecorder::getTarget_0),
            Codec.STRING.fieldOf("target_1").forGetter(BattleStickRecorder::getTarget_1)
    ).apply(instance, BattleStickRecorder::new));
    @NotNull
    public String target_0 = "";
    @NotNull
    public String target_1 = "";

    public static BattleStickRecorder empty() {
        return new BattleStickRecorder("", "");
    }
}
