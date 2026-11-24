package cc.thonly.reverie_dreams.world.gensokyo.bamboo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.Locale;

public record BambooForestConfiguration(Mode mode) implements FeatureConfiguration {
    public enum Mode implements StringRepresentable {
        CULMS, GROUND;

        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static final Codec<BambooForestConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(
            StringRepresentable.fromEnum(Mode::values).fieldOf("mode").forGetter(BambooForestConfiguration::mode)).apply(i, BambooForestConfiguration::new));
}
