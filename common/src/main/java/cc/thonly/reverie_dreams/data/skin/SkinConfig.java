package cc.thonly.reverie_dreams.data.skin;

import cc.thonly.reverie_dreams.registry.CodecStep;
import cc.thonly.reverie_dreams.registry.RegistryEntryOwnerBindable;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;

import java.util.Optional;

@Getter
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@ToString
public class SkinConfig implements CodecStep<SkinConfig>, RegistryEntryOwnerBindable<SkinConfig> {
    public static final Codec<SkinConfig> CODEC = RecordCodecBuilder.create(x -> x.group(
            Identifier.CODEC.fieldOf("registry_key").forGetter(SkinConfig::getRegistryKey),
            ModelType.CODEC.fieldOf("type").forGetter(SkinConfig::getType),
            Identifier.CODEC.optionalFieldOf("cape").forGetter(SkinConfig::getCapeTexture),
            Identifier.CODEC.optionalFieldOf("elytra").forGetter(SkinConfig::getElytraTexture)
    ).apply(x, SkinConfig::new));

    @Setter
    private SkinType skin = null;
    private Identifier registryKey = null;
    private final ModelType type;
    private final Optional<Identifier> capeTexture;
    private final Optional<Identifier> elytraTexture;
    @ToString.Exclude
    @Setter
    private RegistryImpl<SkinConfig> owner;

    public SkinConfig(ModelType type, Optional<Identifier> capeTexture, Optional<Identifier> elytraTexture) {
        this.registryKey = null;
        this.type = type;
        this.capeTexture = capeTexture;
        this.elytraTexture = elytraTexture;
    }

    public SkinConfig(Identifier registryKey, ModelType type, Optional<Identifier> capeTexture, Optional<Identifier> elytraTexture) {
        this.registryKey = registryKey;
        this.type = type;
        this.capeTexture = capeTexture;
        this.elytraTexture = elytraTexture;
    }

    @Override
    public Codec<SkinConfig> getCodec() {
        return CODEC;
    }

    public void bindRegistryKey(Identifier key) {
        if (this.registryKey != null) {
            throw new IllegalCallerException("The registry_key has already been bound.");
        }
        this.registryKey = key;
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
