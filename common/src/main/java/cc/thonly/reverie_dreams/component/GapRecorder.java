package cc.thonly.reverie_dreams.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@Setter
@Getter
public class GapRecorder {
    public static final Codec<GapRecorder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(GapRecorder::getName),
            Codec.STRING.optionalFieldOf("world", "minecraft:overworld").forGetter(GapRecorder::getWorld),
            BlockPos.CODEC.fieldOf("value").forGetter(GapRecorder::getValue),
            Codec.BOOL.fieldOf("enable").forGetter(GapRecorder::isEnable)
    ).apply(instance, GapRecorder::new));
    public static final Codec<List<GapRecorder>> LIST_CODEC = Codec.list(CODEC);

    public String name;
    public String world = "minecraft:overworld";
    public BlockPos value;
    public boolean enable = false;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GapRecorder)) {
            return false;
        }
        return this == obj || this.hashCode() == obj.hashCode() || super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                this.name,
                this.world,
                this.value,
                this.enable
        );
    }
}
