package cc.thonly.reverie_dreams.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;


public record CraterFeatureConfig(IntProvider radius, IntProvider depth) implements FeatureConfiguration {
    public static final Codec<CraterFeatureConfig> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(
                        IntProvider.CODEC.fieldOf("radius").forGetter(CraterFeatureConfig::radius),
                        IntProvider.CODEC.fieldOf("depth").forGetter(CraterFeatureConfig::depth)
                ).apply(instance, CraterFeatureConfig::new);
    });

    public CraterFeatureConfig(IntProvider radius, IntProvider depth) {
        this.radius = radius;
        this.depth = depth;
    }

    public IntProvider radius() {
        return this.radius;
    }

    public IntProvider depth() {
        return this.depth;
    }
}
