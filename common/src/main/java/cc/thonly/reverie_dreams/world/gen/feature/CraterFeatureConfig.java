package cc.thonly.reverie_dreams.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;


public record CraterFeatureConfig(UniformInt radius, UniformInt depth) implements FeatureConfiguration {
    public static final Codec<CraterFeatureConfig> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(
                UniformInt.CODEC.codec().fieldOf("radius").forGetter(CraterFeatureConfig::radius),
                UniformInt.CODEC.codec().fieldOf("depth").forGetter(CraterFeatureConfig::depth)
        ).apply(instance, CraterFeatureConfig::new);
    });

    public CraterFeatureConfig(UniformInt radius, UniformInt depth) {
        this.radius = radius;
        this.depth = depth;
    }

    public UniformInt radius() {
        return this.radius;
    }

    public UniformInt depth() {
        return this.depth;
    }
}
