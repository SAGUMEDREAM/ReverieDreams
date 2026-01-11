package cc.thonly.reverie_dreams.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record DreamTrialRoomConfig(Identifier entityTypeId) implements FeatureConfiguration {
    public static final Codec<DreamTrialRoomConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("entity_type_id").forGetter(cfg -> cfg.entityTypeId)
    ).apply(instance, DreamTrialRoomConfig::new));
}