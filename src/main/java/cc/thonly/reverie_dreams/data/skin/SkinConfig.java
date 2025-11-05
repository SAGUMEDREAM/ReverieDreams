package cc.thonly.reverie_dreams.data.skin;

import cc.thonly.reverie_dreams.registry.interfaces.CodecStep;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import cc.thonly.reverie_dreams.registry.interfaces.OwnerBinding;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import java.util.Optional;

@Getter
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SkinConfig implements CodecStep<SkinConfig>, OwnerBinding<SkinConfig> {
    public static final Codec<SkinConfig> CODEC = RecordCodecBuilder.create(x -> x.group(
            ModelType.CODEC.fieldOf("type").forGetter(SkinConfig::getType),
            ResourceLocation.CODEC.optionalFieldOf("cape").forGetter(SkinConfig::getCapeTexture),
            ResourceLocation.CODEC.optionalFieldOf("elytra").forGetter(SkinConfig::getElytraTexture)
    ).apply(x, SkinConfig::new));

    @Setter
    private SkinType skin;
    private final ModelType type;
    private final Optional<ResourceLocation> capeTexture;
    private final Optional<ResourceLocation> elytraTexture;
    @Setter
    private RegistryHandler<SkinConfig> owner;

    public SkinConfig(ModelType type, Optional<ResourceLocation> capeTexture, Optional<ResourceLocation> elytraTexture) {
        this.type = type;
        this.capeTexture = capeTexture;
        this.elytraTexture = elytraTexture;
    }

    @Override
    public Codec<SkinConfig> getCodec() {
        return CODEC;
    }

    public enum ModelType implements StringRepresentable {
        SLIM("slim"),
        WIDE("wide");
        public static final Codec<ModelType> CODEC = StringRepresentable.fromEnum(ModelType::values);
        private final String name;

        ModelType(final String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
