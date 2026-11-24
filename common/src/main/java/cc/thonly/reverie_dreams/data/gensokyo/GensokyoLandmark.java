package cc.thonly.reverie_dreams.data.gensokyo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;

import java.util.Optional;

/**
 * A named point inside {@code reverie_dreams:gensokyo}. Coordinates are local
 * X/Z block coordinates of the Gensokyo map, not world coordinates.
 *
 * <p>{@code order} keeps the map legend order from the source dataset stable
 * across resource reloads; the registry identifier is not necessarily ordered.</p>
 */
public record GensokyoLandmark(
        int order,
        LandmarkKind kind,
        int x,
        int z,
        String name,
        Optional<String> note
) {
    public static final Codec<GensokyoLandmark> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("order").forGetter(GensokyoLandmark::order),
            LandmarkKind.CODEC.fieldOf("kind").forGetter(GensokyoLandmark::kind),
            Codec.INT.fieldOf("x").forGetter(GensokyoLandmark::x),
            Codec.INT.fieldOf("z").forGetter(GensokyoLandmark::z),
            Codec.STRING.fieldOf("name").forGetter(GensokyoLandmark::name),
            Codec.STRING.optionalFieldOf("note").forGetter(GensokyoLandmark::note)
    ).apply(instance, GensokyoLandmark::new));

    public Component displayName() {
        return Component.translatable(this.name);
    }

    public Optional<Component> displayNote() {
        return this.note.map(Component::translatable);
    }

    public double horizontalDistance(double fromX, double fromZ) {
        double dx = this.x + 0.5D - fromX;
        double dz = this.z + 0.5D - fromZ;
        return Math.sqrt(dx * dx + dz * dz);
    }
}
