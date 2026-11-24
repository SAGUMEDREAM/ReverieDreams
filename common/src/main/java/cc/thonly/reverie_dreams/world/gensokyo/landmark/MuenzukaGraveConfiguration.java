package cc.thonly.reverie_dreams.world.gensokyo.landmark;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.Locale;

public record MuenzukaGraveConfiguration(Kind kind) implements FeatureConfiguration {
    public enum Kind implements StringRepresentable {
        RANDOM, LOW, STONE, WEATHERED;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static final Codec<MuenzukaGraveConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(
            StringRepresentable.fromEnum(Kind::values).fieldOf("kind").forGetter(MuenzukaGraveConfiguration::kind)
    ).apply(i, MuenzukaGraveConfiguration::new));
}
