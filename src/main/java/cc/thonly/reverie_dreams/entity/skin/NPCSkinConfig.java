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
public class NPCSkinConfig implements CodecStep<NPCSkinConfig>, OwnerBinding<NPCSkinConfig> {
    public static final Codec<NPCSkinConfig> CODEC = RecordCodecBuilder.create(x -> x.group(
            ModelType.CODEC.fieldOf("type").forGetter(NPCSkinConfig::getType),
            Identifier.CODEC.optionalFieldOf("cape").forGetter(NPCSkinConfig::getCapeTexture),
            Identifier.CODEC.optionalFieldOf("elytra").forGetter(NPCSkinConfig::getElytraTexture)
    ).apply(x, NPCSkinConfig::new));

    private final ModelType type;
    private final Optional<Identifier> capeTexture;
    private final Optional<Identifier> elytraTexture;
    @Setter
    private IntrinsicalRegister<NPCSkinConfig> owner;

    public NPCSkinConfig(ModelType type, Optional<Identifier> capeTexture, Optional<Identifier> elytraTexture) {
        this.type = type;
        this.capeTexture = capeTexture;
        this.elytraTexture = elytraTexture;
    }

    @Override
    public Codec<NPCSkinConfig> getCodec() {
        return CODEC;
    }

    public enum ModelType implements StringIdentifiable {
        SLIM("slim"),
        WIDE("wide");
        public static final Codec<ModelType> CODEC = StringIdentifiable.createCodec(ModelType::values);
        private final String name;

        private ModelType(final String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }
    }
}
