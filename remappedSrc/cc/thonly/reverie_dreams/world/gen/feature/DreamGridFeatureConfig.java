package cc.thonly.reverie_dreams.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record DreamGridFeatureConfig(ResourceLocation blockId) implements FeatureConfiguration {
    public static final Codec<DreamGridFeatureConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("block").forGetter(config -> config.blockId)
            ).apply(instance, DreamGridFeatureConfig::new)
    );
}
