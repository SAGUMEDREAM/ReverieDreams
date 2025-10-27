package cc.thonly.reverie_dreams.entity.skin;

import cc.thonly.reverie_dreams.registry.CodecStep;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.OwnerBinding;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

import java.util.Optional;

@Getter
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SkinConfig implements CodecStep<SkinConfig>, OwnerBinding<SkinConfig> {
    public static final Codec<SkinConfig> CODEC = RecordCodecBuilder.create(x -> x.group(
            ModelType.CODEC.fieldOf("type").forGetter(SkinConfig::getType),
            Identifier.CODEC.optionalFieldOf("cape").forGetter(SkinConfig::getCapeTexture),
            Identifier.CODEC.optionalFieldOf("elytra").forGetter(SkinConfig::getElytraTexture)
    ).apply(x, SkinConfig::new));

    @Setter
    private SkinType skin;
    private final ModelType type;
    private final Optional<Identifier> capeTexture;
    private final Optional<Identifier> elytraTexture;
    @Setter
    private IntrinsicalRegister<SkinConfig> owner;

    public SkinConfig(ModelType type, Optional<Identifier> capeTexture, Optional<Identifier> elytraTexture) {
        this.type = type;
        this.capeTexture = capeTexture;
        this.elytraTexture = elytraTexture;
    }

    @Override
    public Codec<SkinConfig> getCodec() {
        return CODEC;
    }

    public enum ModelType implements StringIdentifiable {
        SLIM("slim"),
        WIDE("wide");
        public static final Codec<ModelType> CODEC = StringIdentifiable.createCodec(ModelType::values);
        private final String name;

        ModelType(final String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }
    }
}
