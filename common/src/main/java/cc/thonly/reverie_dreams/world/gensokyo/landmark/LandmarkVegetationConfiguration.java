package cc.thonly.reverie_dreams.world.gensokyo.landmark;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.Locale;

public record LandmarkVegetationConfiguration(Theme theme) implements FeatureConfiguration {
    public enum Theme implements StringRepresentable {
        PHANTOM_MEADOW, MUENZUKA, GARDEN_OF_THE_SUN;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static final Codec<LandmarkVegetationConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(
            StringRepresentable.fromEnum(Theme::values).fieldOf("theme").forGetter(LandmarkVegetationConfiguration::theme)
    ).apply(i, LandmarkVegetationConfiguration::new));
}
