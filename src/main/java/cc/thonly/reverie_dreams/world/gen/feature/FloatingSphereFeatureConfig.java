package cc.thonly.reverie_dreams.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record FloatingSphereFeatureConfig(Identifier blockId, int minRadius, int maxRadius, int minY, int maxY,
                                          int horizontalRange) implements FeatureConfiguration {
    public static final Codec<FloatingSphereFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("block_id").forGetter(cfg -> cfg.blockId),
            Codec.INT.fieldOf("min_radius").forGetter(cfg -> cfg.minRadius),
            Codec.INT.fieldOf("max_radius").forGetter(cfg -> cfg.maxRadius),
            Codec.INT.fieldOf("min_y").forGetter(cfg -> cfg.minY),
            Codec.INT.fieldOf("max_y").forGetter(cfg -> cfg.maxY),
            Codec.INT.fieldOf("horizontal_range").forGetter(cfg -> cfg.horizontalRange)
    ).apply(instance, FloatingSphereFeatureConfig::new));

}