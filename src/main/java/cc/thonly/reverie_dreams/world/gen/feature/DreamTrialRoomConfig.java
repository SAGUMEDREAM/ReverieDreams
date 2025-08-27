package cc.thonly.reverie_dreams.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.FeatureConfig;

public record DreamTrialRoomConfig(Identifier entityTypeId) implements FeatureConfig {
    public static final Codec<DreamTrialRoomConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("entity_type_id").forGetter(cfg -> cfg.entityTypeId)
    ).apply(instance, DreamTrialRoomConfig::new));
}