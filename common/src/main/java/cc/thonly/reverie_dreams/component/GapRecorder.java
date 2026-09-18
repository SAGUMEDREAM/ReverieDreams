package cc.thonly.reverie_dreams.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Objects;

@Builder(toBuilder = true)
public record GapRecorder(String name, String world, BlockPos value, boolean enable) {
    public static final Codec<GapRecorder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(GapRecorder::name),
            Codec.STRING.optionalFieldOf("world", "minecraft:overworld").forGetter(GapRecorder::world),
            BlockPos.CODEC.fieldOf("value").forGetter(GapRecorder::value),
            Codec.BOOL.fieldOf("enable").forGetter(GapRecorder::enable)
    ).apply(instance, GapRecorder::new));
    public static final Codec<List<GapRecorder>> LIST_CODEC = Codec.list(CODEC);

}
