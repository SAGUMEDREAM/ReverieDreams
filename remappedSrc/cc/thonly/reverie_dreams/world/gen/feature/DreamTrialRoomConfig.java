package cc.thonly.reverie_dreams.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record DreamTrialRoomConfig(ResourceLocation entityTypeId) implements FeatureConfiguration {
    public static final Codec<DreamTrialRoomConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("entity_type_id").forGetter(cfg -> cfg.entityTypeId)
    ).apply(instance, DreamTrialRoomConfig::new));
}